//package Video;
//
//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;
//import org.opencv.core.*;
//import org.opencv.highgui.HighGui;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.LineUnavailableException;
//import javax.sound.sampled.SourceDataLine;
//import javax.sound.sampled.UnsupportedAudioFileException;
//import javax.sound.sampled.DataLine.Info;
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import java.awt.image.BufferedImage;
//import java.io.*;
//import java.sql.Time;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.ReentrantLock;
//import java.util.stream.Collectors;
//
//
//import static org.opencv.core.CvType.CV_8UC1;
//import static org.opencv.core.CvType.CV_8UC3;
//
//public class Player {
//    public static int play=0;
//    public static int stop=0;
//
//    public static void main(String[] args){
//        try {
//            nu.pattern.OpenCV.loadLocally();
//            File audioFile = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Videos\\data_test1.wav");
//            InputStream audioIS = new FileInputStream(audioFile);
//            VideoPlay videoPlay=new VideoPlay(480, 270,"Synchronized Video");
//            Thread videoThread = new Thread(videoPlay);
//            Thread audioThread = new Thread(new AudioPlay(audioIS));
//            Thread ocrDetect = new Thread(new OCR());
//            audioThread.start();
//            videoThread.start();
//            ocrDetect.start();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
//class AudioPlay implements Runnable{
//    private InputStream waveStream;
//
//    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
//
//    public AudioPlay(InputStream waveStream) {
//        this.waveStream = waveStream;
//    }
//    public void run() {
//        SourceDataLine dataLine = null;
//        try {
//            AudioInputStream audioInputStream = null;
//            InputStream bufferedIn = new BufferedInputStream(this.waveStream);
//            audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
//            AudioFormat audioFormat = audioInputStream.getFormat();
//            Info info = new Info(SourceDataLine.class, audioFormat);
//            dataLine = null;
//            dataLine = (SourceDataLine) AudioSystem.getLine(info);
//            dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
//            // Starts the music :P
//            dataLine.start();
//            int readBytes = 0;
//            byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
//
//            while (readBytes != -1) {
//                readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
//                Player.play = 1;
//                if (readBytes >= 0) {
//                    dataLine.write(audioBuffer, 0, readBytes);
//                }
//            }
//
//        } catch (UnsupportedAudioFileException e1) {
////            throw new PlayWaveException(e1);
//        } catch (IOException e1) {
////            throw new PlayWaveException(e1);
//        } catch (LineUnavailableException e1) {
//
//        } finally {
//            // plays what's left and and closes the audioChannel
//            dataLine.drain();
//            dataLine.close();
//        }
//
//    }
//}
////class MultithreadingDemo1 implements Runnable {
////    ReentrantLock lock = new ReentrantLock();
////    public void run()
////    {
////        lock.lock();
////        try {
////            count++;
////        } finally {
////            lock.unlock();
////        }
////    }
////}
//class MultithreadingDemo2 implements Runnable {
//    ReentrantLock lock = new ReentrantLock();
//    public void run()
//    {
//        try {
//            // Displaying the thread that is running
//            System.out.println(
//                    "Thread " + Thread.currentThread().getId()
//                            + " is running");
//        }
//        catch (Exception e) {
//            // Throwing an exception
//            System.out.println("Exception is caught");
//        }
//    }
//}
//
//class OCR implements Runnable{
//    public void run(){
//        try {
//            while(true) {
//                Tesseract tesseract = new Tesseract();
//                tesseract.setDatapath("C:\\Users\\larry\\Documents\\VideoGenerator\\tessdata");
//                String result = null;
////                try (FileChannel channel = FileChannel.open(path, StandardOpenOption.APPEND)) {
////                    // write to channel
////                }
//                File tmpFile=new File("1.png");
////                if(tmpFile==null){
////                    continue;
////                }
//
//                result = tesseract.doOCR(ImageIO.read(tmpFile));
//
////                System.out.println(result);
//                TimeUnit.SECONDS.sleep(1);
//            }
//        } catch (TesseractException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            System.out.println("error");
////            e.printStackTrace();
////            continue;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }
//}
//
//class VideoPlay implements Runnable {
//    private JFrame frame;
//    private JLabel lbIm1;
//    private int width;
//    private int height;
//    private HashMap<String, ArrayList<Scalar>> brands;
////    private Thread audioThread;
//
//    public VideoPlay(int width,int height,String windowName) throws FileNotFoundException {
//        this.width=width;
//        this.height =height;
//        frame=new JFrame(windowName);
//        lbIm1=new JLabel(new ImageIcon("C:\\Users\\larry\\Documents\\VideoGenerator\\firstframe.png"));
//        frame.getContentPane().add(lbIm1);
//        frame.add(lbIm1);
//        frame.pack();
//        frame.setVisible(true);
//    }
//
//    private HashMap<String, ArrayList<Scalar>> readBrands() {
//        HashMap<String, ArrayList<Scalar>> res = new HashMap<>();
//        try
//        {
//            File dir = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Brand Images");
//            File[] files = dir.listFiles((d, name) -> name.endsWith(".rgb"));
//
//            for (File file : files) {
//                RandomAccessFile raf = new RandomAccessFile(file, "r");
//                raf.seek(0);
//
//                Mat mat = new Mat(height, width, CV_8UC3, new Scalar(0,0,0));
//
//                byte[] bytes = new byte[width * height * 3];
//                raf.read(bytes);
//
//                int gap = 256 / 16;
//                ArrayList<Integer> quant_vals = new ArrayList<Integer>();
//                for (int i = gap - 1; i < 256; i += gap) {
//                    quant_vals.add(i - gap / 2);
//                }
//                int r, g, b;
//                for (int x = 0; x < width; x++) {
//                    for (int y = 0; y < height; y++) {
//                        r = bytes[x + y * width];
//                        g = bytes[x + y * width + height * width];
//                        b = bytes[x + y * width + height * width * 2];
//
//                        if (r < 0) r += 256;
//                        if (g < 0) g += 256;
//                        if (b < 0) b += 256;
//
//                        // map to corresponding range
//                        r = quant_vals.get(r / gap);
//                        g = quant_vals.get(g / gap);
//                        b = quant_vals.get(b / gap);
//
//                        double[] pixel = {b, g, r};
//                        mat.put(y, x, pixel);
//                    }
//                }
//
//                Mat hsvImage = new Mat();
//                Imgproc.cvtColor(mat, hsvImage, Imgproc.COLOR_BGR2HSV);
//
//                Imgcodecs imageCodecs = new Imgcodecs();
//                //Writing the image
//                imageCodecs.imwrite("output.jpg", hsvImage);
//
//                // get all color
//                HashMap<Integer, int[]> colors = new HashMap<Integer, int[]>();  // map<hue, [count, s, v]>
//                for (int x = 0; x < width; x++) {
//                    for (int y = 0; y < height; y++) {
//                        double[] hsv_double = hsvImage.get(y, x);
//                        int[] hsv = new int[]{(int)hsv_double[0], (int)hsv_double[1], (int)hsv_double[2]};
//
//                        if (!colors.containsKey(hsv[0])) {
//                            colors.put(hsv[0], new int[]{1, hsv[1], hsv[2]});
//                        } else {
//                            colors.put(hsv[0], new int[]{colors.get(hsv[0])[0] + 1, hsv[1], hsv[2]});
//                        }
//                    }
//                }
//
//                // get top n dominant colors -> at least 70% of all pixels
//                Map<Integer, int[]> sorted_colors = colors.entrySet().stream()
//                        .sorted((e1, e2) -> Integer.compare(e2.getValue()[0], e1.getValue()[0]))
//                        .collect(Collectors.toMap(Map.Entry::getKey,
//                                Map.Entry::getValue,
//                                (e1, e2) -> e1, LinkedHashMap::new));
//
//                ArrayList<Scalar> brand_values = new ArrayList<>();
//                int pix_count = 0, pix_threshold = (int)(0.9 * width * height);
//                for (Map.Entry<Integer, int[]> e : sorted_colors.entrySet()) {
//                    if (pix_count > pix_threshold) break;
//                    // skip white, avoid detecting sky
//                    if (e.getKey() != 0) brand_values.add(new Scalar(e.getKey(), e.getValue()[1], e.getValue()[2]));
//                    pix_count += e.getValue()[0];
////                    System.out.println(e.getKey() + ": " + e.getValue()[0] + ", " + e.getValue()[1] + ", " + e.getValue()[2]);
//                }
//
//                res.put(file.getName(), brand_values);
//                System.out.println("start parsing next");
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return res;
//    }
//
//    public void run()
//    {
//        try {
//            File videoFile = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Videos\\data_test1.rgb");
//            InputStream videoIS = new FileInputStream(videoFile);
//            //BGR
//            byte[] videoBuffer = new byte[width * height * 3];
//            BufferedInputStream videoInputStream = new BufferedInputStream(videoIS);
//            Mat mat = new Mat(height, width, CV_8UC3, new Scalar(0,0,0));
//            int frameIndex=0;
//            int r,g,b;
//            long start = System.currentTimeMillis();
//            double fps = (1.0 / 30) * 1000 -5;
//            int readBytes=0;
//
//            double deltaNanos=0.0;
//            long millis_prev = System.currentTimeMillis();
//            while (readBytes!= -1) {
//                readBytes=videoInputStream.read(videoBuffer, 0, videoBuffer.length);
//                // read current frame
//                for (int x = 0; x < width; x++) {
//                    for (int y = 0; y < height; y++) {
//                        r = videoBuffer[x + y * width];
//                        g = videoBuffer[x + y * width + height * width];
//                        b = videoBuffer[x + y * width + height * width * 2];
//                        if (r < 0) r += 256;
//                        if (g < 0) g += 256;
//                        if (b < 0) b += 256;
//                        double[] pixel = {b, g, r};
//                        mat.put(y, x, pixel);
//                    }
//                }
////                if(frameIndex==0)
////                Imgcodecs.imwrite("firstframe.png", mat);
//                frameIndex++;
//
//                //opencv image to swing
//                MatOfByte matOfByte = new MatOfByte();
//                Imgcodecs.imencode(".jpg", mat, matOfByte);
//                byte[] byteArray = matOfByte.toArray();
//                InputStream in = new ByteArrayInputStream(byteArray);
//                BufferedImage bufImage = ImageIO.read(in);
//                while(Player.play==0){
//                    System.out.println(Player.play);
//                }
//                lbIm1.setIcon(new ImageIcon(bufImage));
//                frame.repaint();
//
//                long millis_spent = System.currentTimeMillis() - millis_prev;
//                if(millis_spent<=34){
//                    TimeUnit mills=TimeUnit.MILLISECONDS;
//                    TimeUnit nanos=TimeUnit.NANOSECONDS;
//                    int waitMills= (int) (fps-millis_spent);
//                    int waitNanos= (int)(1000*(fps-(int)fps));
//                    deltaNanos+=fps-(int)fps-(double)waitNanos/1000;
//                    if (deltaNanos>0.001){
//                        waitNanos+=1;
//                        deltaNanos-=0.001;
//                    }
//                    mills.sleep(waitMills);
//                    nanos.sleep(waitNanos);
//                }
//                millis_prev = System.currentTimeMillis();
//
//
//            }
//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            System.out.println("timeElapsed:" + timeElapsed);
//
//            videoIS.close();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
