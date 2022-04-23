package Video;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
/**
 * 
 * <Replace this with a short description of the class.>
 * 
 * @author Giulio
 */
public class PlaySound {

    private InputStream waveStream;

    private final int EXTERNAL_BUFFER_SIZE = 16384; //  sec, before: 524288 -> 128Kb
	private static final float NORMALIZATION_FACTOR_2_BYTES = Short.MAX_VALUE + 1.0f;
	private final int sampleRate = 48000;
	private final int sampleSize = 2;  // 2 byte
	private int pitch_below = 2000;  // human pitch 40-4000 hz
	private int pitch_above = 2400;
	private int bin_size = sampleRate / 2 / (EXTERNAL_BUFFER_SIZE / 2);
	private int bin_below = pitch_below / bin_size, bin_above = pitch_above / bin_size;
	private int bin_count = bin_above - bin_below;

	/**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream) {
	this.waveStream = waveStream;
    }

    public void play() throws PlayWaveException {

		AudioInputStream audioInputStream = null;
		try {
			//add buffer for mark/reset support, modified by Jian
			InputStream bufferedIn = new BufferedInputStream(this.waveStream);
			audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);

		} catch (UnsupportedAudioFileException e1) {
			throw new PlayWaveException(e1);
		} catch (IOException e1) {
			throw new PlayWaveException(e1);
		}

		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);
		// opens the audio channel
		SourceDataLine dataLine = null;
		try {
			dataLine = (SourceDataLine) AudioSystem.getLine(info);
			dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
		} catch (LineUnavailableException e1) {
			throw new PlayWaveException(e1);
		}

		// Starts the music :P
		dataLine.start();
		int readBytes = 0;
		byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
		final FFTFactory.JavaFFT fft = new FFTFactory.JavaFFT(audioBuffer.length / sampleSize);
		double prev_E = 0, curr_E;
		int timer = 0;

		try {
			while (readBytes != -1) {
				readBytes = audioInputStream.read(audioBuffer, 0,audioBuffer.length);
				if (readBytes >= 0){
					// TODO: check every 1 second, save each part in array; if ads, don't play

					final float[] samples = decode(audioBuffer, audioFormat);
					final float[][] transformed = fft.transform(samples);
					final float[] realPart = transformed[0];
					final float[] imaginaryPart = transformed[1];
					final double[] magnitudes = toMagnitudes(realPart, imaginaryPart);

					// get the avg occurring frequency within selected range
					double sum = 0.0;
//					double max = 0.0;
					int max_idx = -1;
					for (int i = bin_below; i <= bin_above; i++) {
//						if (max < magnitudes[i]) {
//							max = magnitudes[i];
//							max_idx = i;
//						}
						sum += magnitudes[i] * magnitudes[i];
					}
					double rms_freq = Math.sqrt(sum / bin_count);
					double rms = RMS(audioBuffer);

					dataLine.write(audioBuffer, 0, readBytes);

					// check audio level
					System.out.println(timer++ + ", "  + rms_freq + ", " + rms);
//					timer++;
//					if (timer== 10 || timer == 30 || timer == 150) {
//						for (int i = bin_below; i <= bin_above; ++i) System.out.println(i + ": " + magnitudes[i]);
//					}
				}
			}
		} catch (IOException e1) {
			throw new PlayWaveException(e1);
		} finally {
			// plays what's left and and closes the audioChannel
			dataLine.drain();
			dataLine.close();
		}

    }

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
