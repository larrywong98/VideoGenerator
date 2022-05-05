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
//import javax.sound.sampled.*;
//import javax.sound.sampled.DataLine.Info;
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import javax.xml.transform.Source;
//import java.awt.image.BufferedImage;
//import java.io.*;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.ReentrantLock;
//import java.util.stream.Collectors;
//
//
//import static org.opencv.core.CvType.CV_8UC1;
//import static org.opencv.core.CvType.CV_8UC3;
//
//public class Player1 {
//    public static int play=0;
//    public static int stop=0;
//    public static final int EXTERNAL_BUFFER_SIZE= 4800; // 128Kb //12kb
//    public static final int audioFrameCount=5999;
//    public static byte[][] audioData=new byte[audioFrameCount][EXTERNAL_BUFFER_SIZE];//0.05s
//    public static int frameIndex=0;
//    public static int currentTime;
//
//
//    public static void main(String[] args){
//        try {
//            nu.pattern.OpenCV.loadLocally();
//            File audioFile = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Videos\\data_test1.wav");
//            InputStream audioIS = new FileInputStream(audioFile);
//            InputStream bufferedIn = new BufferedInputStream(audioIS);
//            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
//            AudioFormat audioFormat = audioInputStream.getFormat();
//            Info info = new Info(SourceDataLine.class, audioFormat);
//            SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
//            dataLine.open(audioFormat, Player1.EXTERNAL_BUFFER_SIZE);
//            dataLine.start();
//            int readBytes = 0;
//            int second=0;
//            while(readBytes!=-1){
//                byte[] audioBuffer = new byte[Player1.EXTERNAL_BUFFER_SIZE];
//                readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
//                audioData[second]= Arrays.copyOf(audioBuffer, audioBuffer.length);
//                second++;
//            }
//            Thread audioThread=new Thread(new AudioPlay(dataLine));
//            audioThread.start();
//
////            long timestart=System.currentTimeMillis();
////            for(int i=0;i<second;i++){
////                dataLine.write(audioData[i],0,Player1.EXTERNAL_BUFFER_SIZE);
////            }
////            long endtime=System.currentTimeMillis();
////            System.out.print(timestart-endtime);
//
//
////            AudioPlay audioplay=new AudioPlay(audioIS);
////            Thread audioThread = new Thread(new AudioPlay(audioIS));
////            audioThread.start();
//
//            VideoPlay videoPlay=new VideoPlay(480, 270,"Synchronized Video",dataLine);
//            Thread videoThread = new Thread(videoPlay);
//            videoThread.start();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (UnsupportedAudioFileException e) {
//            e.printStackTrace();
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
//class AudioPlay implements Runnable{
//    private SourceDataLine dataLine;
//    private int audioFrame;
//    public AudioPlay(SourceDataLine dataLine){
//        this.dataLine=dataLine;
//        this.audioFrame=0;
//    }
//    public void run() {
//        try {
//            while(audioFrame<Player1.audioFrameCount){
//                if(Player1.frameIndex%3==0 && audioFrame!=Player1.frameIndex%3*2){
//                    audioFrame=Player1.frameIndex/3*2;
//                }
//                dataLine.write(Player1.audioData[audioFrame], 0,Player1.EXTERNAL_BUFFER_SIZE);
//                audioFrame++;
//            }
//
//        } finally{
//            dataLine.drain();
//            dataLine.close();
//        }
//
//    }
//}
//class VideoPlay implements Runnable {
//    private JFrame frame;
//    private JLabel lbIm1;
//    private int width;
//    private int height;
//
//    private SourceDataLine dataLine;
//    private AudioInputStream audioInputStream;
//    private InputStream bufferedIn;
//    private AudioFormat audioFormat;
//    private Info info;
//    private InputStream waveStream;
////    private final int EXTERNAL_BUFFER_SIZE = 96000; // 128Kb //12kb
////    public byte[][] audioData=new byte[310][this.EXTERNAL_BUFFER_SIZE];
////    private Thread audioThread;
//
//    public VideoPlay(int width, int height, String windowName, SourceDataLine dataLine) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
//        this.width=width;
//        this.height =height;
//        frame=new JFrame(windowName);
//        lbIm1=new JLabel(new ImageIcon("C:\\Users\\larry\\Documents\\VideoGenerator\\firstframe.png"));
//        frame.getContentPane().add(lbIm1);
//        frame.add(lbIm1);
//        frame.pack();
//        frame.setVisible(true);
//        this.dataLine=dataLine;
//    }
//
//    public void run() {
//        try {
//            File videoFile = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Videos\\data_test1.rgb");
//            InputStream videoIS = new FileInputStream(videoFile);
//            //BGR
//            byte[] videoBuffer = new byte[width * height * 3];
//            BufferedInputStream videoInputStream = new BufferedInputStream(videoIS);
//            Mat mat = new Mat(height, width, CV_8UC3, new Scalar(0, 0, 0));
////            int frameIndex = 0;
//            int r, g, b;
//            long start = System.currentTimeMillis();
//            double fps = (1.0 / 30) * 1000 - 5;
//            int readBytes = 0;
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
////                if (frameIndex%30==0){
////                    Thread audioThread=new Thread(new AudioPlay(frameIndex/30,this.dataLine));
////                    audioThread.start();
//
////                    AudioPlay audioPlay=new AudioPlay(frameIndex/30,this.dataLine);
////                    audioPlay.audiostart(frameIndex/30);
////                }
//                Player1.frameIndex++;
//                //opencv image to swing
//                MatOfByte matOfByte = new MatOfByte();
//                Imgcodecs.imencode(".jpg", mat, matOfByte);
//                byte[] byteArray = matOfByte.toArray();
//                InputStream in = new ByteArrayInputStream(byteArray);
//                BufferedImage bufImage = ImageIO.read(in);
////                while(Player1.play==0){
////                    System.out.println(Player1.play);
////                }
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
//            }
//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//
//            System.out.println("timeElapsed:" + timeElapsed);
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



//        Image img = new ImageIcon("play.png").getImage() ;
//        Image playImg = img.getScaledInstance( 45, 40,  java.awt.Image.SCALE_SMOOTH) ;
//        img = new ImageIcon("stop.png").getImage() ;
//        Image stopImg = img.getScaledInstance( 45, 40,  java.awt.Image.SCALE_SMOOTH) ;

//        playBtn.setOpaque(false);
//        playBtn.setContentAreaFilled(false);
//        playBtn.setBorderPainted(false);
//        stopBtn.setOpaque(false);
//        stopBtn.setContentAreaFilled(false);
//        stopBtn.setBorderPainted(false);


//                    byte[] byteArray = matOfByte.toArray();
//                    InputStream in = new ByteArrayInputStream(byteArray);
//                    BufferedImage bufImage = ImageIO.read(in);
//                    lbIm1.setIcon(new ImageIcon(bufImage));


//    private ArrayList<Integer> DetectSceneChange(Mat diffMat){
//        ArrayList<Integer> timestamps=new ArrayList<Integer>();
////        Mat diffMat=new Mat(height, width, CV_8UC1, new Scalar(0, 0, 0));
////        Core.subtract(currentMat, preMat,diffMat);
//        System.out.println(diffMat.get(0,0)[0]);
//
//
//        return timestamps;
//    }


//                    sumdiff=Math.abs(sumdiff);
//                    int changed=0;
//                    if(sumdiff-prevSumDiff>300000){
//                        changed=1;
//                    }
//
//                    if(changed==1)
//                    System.out.println(sumdiff+" "+(sumdiff-prevSumDiff)+" "+Player.videoFrameIndex);
//                    prevSumDiff=sumdiff;
//                    if(Player.videoFrameIndex>1800 && Player.videoFrameIndex<2300){
//                        String filename="dataset\\ad1\\ad"+Player.videoFrameIndex+".png";
//                        Imgcodecs.imwrite(filename,mat);
//                    }

//                    Imgproc.cvtColor(mat,previousMat, Imgproc.COLOR_BGR2GRAY);
//                    Imgproc.cvtColor(currentMat,previousMat, Imgproc.COLOR_BGR2GRAY);
//                    DetectSceneChange(diffMat);



//                            diffMat.put(y,x,new double[]{b-preb,b-preb,b-preb});
//                            sumdiff+=Math.abs(b-preb);
//                            preb=b;



//                Mat previousMat=new Mat(height, width, CV_8UC3, new Scalar(0, 0, 0));
//                        Mat currentMat=new Mat(height, width, CV_8UC3, new Scalar(0, 0, 0));
//                        Mat diffMat=new Mat(height, width, CV_8UC3, new Scalar(0, 0, 0));


//detect logo
//                    Mat blurredImage = new Mat();
//                    Mat blurredImage_inv = new Mat();
//                    Mat hsvImage = new Mat();
//                    Mat mask = new Mat();
//                    Mat morphOutput = new Mat();
//                    ArrayList<MatOfPoint> contours = new ArrayList<>();
//                    Mat hierarchy = new Mat();
//                    Imgproc.blur(mat, blurredImage, new Size(7, 7));
////                Core.bitwise_not(blurredImage, blurredImage_inv);  // ~blurredImage to avoid hue wrap around
//                    Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
//                    for (Map.Entry<String, ArrayList<Scalar>> brand : brands.entrySet()) {
//                        Boolean masked = false;
//                        for (Scalar brand_color : brand.getValue()) {
//                            Mat mask_color = new Mat();
//                            Scalar min_values = new Scalar(brand_color.val[0] - 15, brand_color.val[1] - 50, brand_color.val[2] - 40);
//                            Scalar max_values = new Scalar(brand_color.val[0] + 15, brand_color.val[1] + 50, brand_color.val[2] + 40);
//                            Core.inRange(hsvImage, min_values, max_values, mask_color);
//                            if (!masked) {
//                                mask = mask_color;
//                                masked = true;
//                            }
//                            Core.bitwise_or(mask, mask_color, mask);  // merge all masks
//                        }
//                        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
//                        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6, 6));
//                        Imgproc.erode(mask, morphOutput, erodeElement);
////                    Imgproc.erode(morphOutput, morphOutput, erodeElement);
//                        Imgproc.dilate(morphOutput, morphOutput, dilateElement);
//                        Imgproc.dilate(morphOutput, morphOutput, dilateElement);
//                        Imgproc.findContours(morphOutput, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//                        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
//                            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
//                                Imgproc.drawContours(mat, contours, idx, new Scalar(250, 255, 255));
//                            }
//                        }
//                    }
//
//
//