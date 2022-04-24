package Video;

import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound {
    private final int EXTERNAL_BUFFER_SIZE = 16384; //  sec, before: 524288 -> 128Kb
	private static final float NORMALIZATION_FACTOR_2_BYTES = Short.MAX_VALUE + 1.0f;
	private final int sampleRate = 48000;
	private final double sampleSize = 2.0;  // 2 byte
	private AudioFormat audioFormat;
	private SourceDataLine dataLine = null;
	private int pitch_below = 2000;  // human pitch 40-4000 hz
	private int pitch_above = 2180;
	private int bin_size = sampleRate / 2 / (EXTERNAL_BUFFER_SIZE / 2);
	private int bin_below = pitch_below / bin_size, bin_above = pitch_above / bin_size;
	private int bin_count = bin_above - bin_below;
	private int duration = (int)(15 * (sampleRate * sampleSize / EXTERNAL_BUFFER_SIZE));
	private HashMap<Integer, ArrayList<Double>> hz_to_mag = new HashMap<Integer,  ArrayList<Double>>();
	private ArrayList<byte[]> allBytes;

    /**
     * Read input file and process with FTT and RMS
     */
    public void play(File audioFile) throws PlayWaveException, FileNotFoundException {
    	allBytes = readWav(audioFile);  // read file and save

		final FFTFactory.JavaFFT fft = new FFTFactory.JavaFFT(EXTERNAL_BUFFER_SIZE / (int)sampleSize);
		double prev_E = 0, curr_E;
		int timer = 0;
		for (byte[] audioBuffer : allBytes) {
			final float[] samples = decode(audioBuffer, audioFormat);
			final float[][] transformed = fft.transform(samples);
			final float[] realPart = transformed[0];
			final float[] imaginaryPart = transformed[1];
			final double[] magnitudes = toMagnitudes(realPart, imaginaryPart);

			// get the avg occurring frequency within selected range
			double sum = 0.0;
			for (int i = bin_below; i <= bin_above; i++) {
//				updateFreqMap(i, magnitudes[i]);
				sum += magnitudes[i] * magnitudes[i];
			}
			double rms_freq = Math.sqrt(sum / bin_count);
			double rms = RMS(audioBuffer);

//			System.out.println(timer++ + ", "  + rms_freq + ", " + rms);
		}
//			saveFreqMap();


    }

    /**
     * Read input wav file and return as list of byte array
     */
	private ArrayList<byte[]> readWav(File audioFile) throws PlayWaveException, FileNotFoundException {
		ArrayList<byte[]> res = new ArrayList<byte[]>();
		AudioInputStream audioInputStream = null;
		try {
			InputStream audioIS = new FileInputStream(audioFile);

			//add buffer for mark/reset support, modified by Jian
			InputStream bufferedIn = new BufferedInputStream(audioIS);
			audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);

		} catch (UnsupportedAudioFileException e1) {
			throw new PlayWaveException(e1);
		} catch (IOException e1) {
			throw new PlayWaveException(e1);
		}

		// Obtain the information about the AudioInputStream
		audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);

		// opens the audio channel
		try {
			dataLine = (SourceDataLine) AudioSystem.getLine(info);
			dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
			throw new PlayWaveException(e1);
		}

		// Starts the music :P
		dataLine.start();
		int readBytes = 0;

		try {
			while (readBytes != -1) {
				byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
				readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
				if (readBytes >= 0) {
					res.add(audioBuffer);
//					dataLine.write(audioBuffer, 0, readBytes);
				}
			}
		} catch (IOException e1) {
			throw new PlayWaveException(e1);
		} finally {
			// plays what's left and closes the audioChannel
			dataLine.drain();
			dataLine.close();
		}
		return res;
	}

	/**
	 * Replace two ads with given ads at 2 timestamps
	 * @param timeStamp_1: starting frame of ad1
	 * @param timeStamp_2: starting frame of ad2
	 * @return 2 or 3 clips w/o ads
	 */
    private ArrayList<byte[]> removeAds(int timeStamp_1, int timeStamp_2) {
		ArrayList<byte[]> res = new ArrayList<byte[]>();
		byte[] clip;
		clip = concatBetween(0, timeStamp_1);	// before first ad
		if (clip.length > 0) res.add(clip);
		clip = concatBetween(timeStamp_1 + duration, timeStamp_2);	// between first ad and second ad
		if (clip.length > 0) res.add(clip);
		clip = concatBetween(timeStamp_2 + duration, allBytes.size());	// after second ad
		if (clip.length > 0) res.add(clip);

		saveWav(res, "removeAds.wav");
		return res;
	}

	/**
	 * Replace two ads with given ads at 2 timestamps
	 * @param timeStamp_1: starting frame of ad1
	 * @param timeStamp_2: starting frame of ad2
	 * @param path_1: file path of given ads
	 * @return 4 or 5 clips w/ replaced ads
	 */
	private ArrayList<byte[]> replaceAds(int timeStamp_1, int timeStamp_2, String path_1, String path_2) throws PlayWaveException, FileNotFoundException {
		byte[] ad_1 = readAd(path_1), ad_2 = readAd(path_2);

		ArrayList<byte[]> res = new ArrayList<byte[]>();
		byte[] clip;
		clip = concatBetween(0, timeStamp_1);	// before first ad
		if (clip.length > 0) res.add(clip);
		res.add(ad_1);	// first ad
		clip = concatBetween(timeStamp_1 + duration, timeStamp_2);	// between first ad and second ad
		if (clip.length > 0) res.add(clip);
		res.add(ad_2);	// second ad
		clip = concatBetween(timeStamp_2 + duration, allBytes.size());	// after second ad
		if (clip.length > 0) res.add(clip);

		saveWav(res, "replaceAds.wav");
		return res;
	}

	/**
	 * Read ad with given filename
	 * @param filename
	 * @return single byte array carrying all frames in ad
	 */
	private byte[] readAd(String filename) throws PlayWaveException, FileNotFoundException {
		File audioFile = new File(filename);
		ArrayList<byte[]> byte_arr= readWav(audioFile);

		byte[] res = new byte[0];
		try{
			ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
			for (int ts = 0; ts < byte_arr.size(); ++ts) {
				byte_stream.write(byte_arr.get(ts));
			}
			res = byte_stream.toByteArray();
		} catch (IOException e) {

		}
		return res;
	}

	/**
	 * Save given byte array to wav file.
	 * @param input: list of byte array
	 * @param filename: wav file name
	 */
	private void saveWav(ArrayList<byte[]> input, String filename) {
		byte[] out = new byte[0];
    	for (int i = 0; i < input.size(); ++i) {
    		out = concatByte(out, input.get(i));
		}
    	ByteArrayInputStream bais = new ByteArrayInputStream(out);
    	AudioInputStream ais = new AudioInputStream(bais, audioFormat, out.length);
    	try {
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filename));
		} catch (IOException e) {
    		e.printStackTrace();
		}
	}

	/**
	 * Concatenate frames between two timestamp
	 * @param start: start timestamp
	 * @param end: end timestamp
	 * @return byte array carrying frames between two given timestamps
	 */
	private byte[] concatBetween(int start, int end) {
    	byte[] res = new byte[0];
    	try{
			ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
			for (int ts = start; ts < end; ++ts) {
				byte_stream.write(allBytes.get(ts));
			}
			res = byte_stream.toByteArray();
		} catch (IOException e) {

		}
		return res;
	}

	/**
	 * Concatenate two byte array
	 */
	private byte[] concatByte(byte[] a, byte[] b) {
		byte[] destination = new byte[a.length + b.length];
		System.arraycopy(a, 0, destination, 0, a.length);
		System.arraycopy(b, 0, destination, a.length, b.length);
		return destination;
	}

	/**
	 * Update frequency value in the HashMap
	 * @param i: key
	 * @param mag: value
	 */
    private void updateFreqMap(int i, double mag) {
		int pitch = i * bin_size;
		ArrayList<Double> val;
		if (hz_to_mag.containsKey(pitch)) {
			val = hz_to_mag.get(pitch);
		} else {
			val = new ArrayList<Double>();
		}
		val.add(mag);
		hz_to_mag.put(pitch, val);
	}

	/**
	 * Save <frequency: magnitudes> into text file
	 */
    private void saveFreqMap() {
		ArrayList<Integer> sortedKeys
				= new ArrayList<Integer>(hz_to_mag.keySet());

		Collections.sort(sortedKeys);

		File outputfile = new File("output.txt");
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(outputfile));
			for (Integer k : sortedKeys) {
				bf.write(k + ":");
				for (Double v : hz_to_mag.get(k)) bf.write(v + ",");
				bf.newLine();
			}
			bf.flush();
		} catch (IOException exp) {

		}
	}

	/**
	 * Calculate magnitude for each frequency
	 * @return magnitudes
	 */
	private static double[] toMagnitudes(final float[] realPart, final float[] imaginaryPart) {
		final double[] powers = new double[realPart.length / 2];
		for (int i = 0; i < powers.length; i++) {
			powers[i] = Math.sqrt(realPart[i] * realPart[i] + imaginaryPart[i] * imaginaryPart[i]);
		}
		return powers;
	}
	private static float[] decode(final byte[] buf, final AudioFormat format) {
		final float[] fbuf = new float[buf.length / format.getFrameSize()];
		for (int pos = 0; pos < buf.length; pos += format.getFrameSize()) {
			final int sample = format.isBigEndian()
					? byteToIntBigEndian(buf, pos, format.getFrameSize())
					: byteToIntLittleEndian(buf, pos, format.getFrameSize());
			// normalize to [0,1] (not strictly necessary, but makes things easier)
			fbuf[pos / format.getFrameSize()] = sample / NORMALIZATION_FACTOR_2_BYTES;
		}
		return fbuf;
	}
	private static int byteToIntLittleEndian(final byte[] buf, final int offset, final int bytesPerSample) {
		int sample = 0;
		for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
			final int aByte = buf[offset + byteIndex] & 0xff;
			sample += aByte << 8 * (byteIndex);
		}
		return sample;
	}
	private static int byteToIntBigEndian(final byte[] buf, final int offset, final int bytesPerSample) {
		int sample = 0;
		for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
			final int aByte = buf[offset + byteIndex] & 0xff;
			sample += aByte << (8 * (bytesPerSample - byteIndex - 1));
		}
		return sample;
	}

	/**
	 * Calculate Root Mean Square over given byte array
	 */
    private double RMS(byte[] raw) {
		double sum = 0.0;
		for (byte num : raw)
			sum += (double)num * (double)num;
		return Math.sqrt(sum / raw.length);
	}

    private double volumeRMS(byte[] raw) {
		double sum = 0d;
		if (raw.length==0) {
			return sum;
		} else {
			for (int ii = 0; ii < raw.length; ii++) {
				sum += raw[ii];
			}
		}
		double average = sum / raw.length;

		double sumMeanSquare = 0d;
		for (int ii = 0; ii < raw.length; ii++) {
			sumMeanSquare += Math.pow(raw[ii] - average, 2d);
		}
		double averageMeanSquare = sumMeanSquare / raw.length;
		double rootMeanSquare = Math.sqrt(averageMeanSquare);

		return rootMeanSquare;
	}
}
