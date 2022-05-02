package Video;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import static org.opencv.core.CvType.CV_8UC3;

public class Player {
    public static Boolean play;
    public static Boolean stop;
    public static final int EXTERNAL_BUFFER_SIZE= 4800;
    public static final int audioFrameCount=5999;
    public static byte[][] audioData=new byte[audioFrameCount][EXTERNAL_BUFFER_SIZE];//0.05s
    public static int videoFrameIndex=0;
    private static String audioFilePath="C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Videos\\data_test1.wav";

    public static void init(){
        try {
            nu.pattern.OpenCV.loadLocally();
            File audioFile = new File(audioFilePath);
            InputStream audioIS = new FileInputStream(audioFile);
            InputStream bufferedIn = new BufferedInputStream(audioIS);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
            AudioFormat audioFormat = audioInputStream.getFormat();
            Info info = new Info(SourceDataLine.class, audioFormat);
            SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, Player.EXTERNAL_BUFFER_SIZE);
            dataLine.start();
            int readBytes = 0;
            int second=0;
            while(readBytes!=-1){
                byte[] audioBuffer = new byte[Player.EXTERNAL_BUFFER_SIZE];
                readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
                audioData[second]= Arrays.copyOf(audioBuffer, audioBuffer.length);
                second++;
            }
            Player.play=false;
            Player.stop=false;
            Thread audioThread=new Thread(new AudioPlay(dataLine));
            VideoPlay videoPlay=new VideoPlay(480, 270,"Synchronized Video");
            Thread videoThread = new Thread(videoPlay);
            audioThread.start();
            videoThread.start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        init();
    }

}
class AudioPlay implements Runnable{
    private SourceDataLine dataLine;
    private int audioFrame;
    public AudioPlay(SourceDataLine dataLine){
        this.dataLine=dataLine;
        this.audioFrame=0;
    }
    public void run() {
        try {
            while(audioFrame<Player.audioFrameCount){
                TimeUnit inputDetect=TimeUnit.MILLISECONDS;
                while(Player.play==false){
                    inputDetect.sleep(10);
                }
                if(Player.videoFrameIndex%3==0 && audioFrame!=Player.videoFrameIndex%3*2){
                    audioFrame=Player.videoFrameIndex/3*2;
                }
                dataLine.write(Player.audioData[audioFrame], 0,Player.EXTERNAL_BUFFER_SIZE);
                audioFrame++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally{
            dataLine.drain();
            dataLine.close();
        }
    }
}
class VideoPlay implements Runnable {
    private JFrame frame;
    private JLabel lbIm1;
    private JButton playBtn, stopBtn;
    private JPanel videoPanel,controlPanel,parentPanel;
    private File videoFile;
    private InputStream videoIS;
    private BufferedInputStream videoInputStream;
    private byte[] videoBuffer;
    private final String firstFramePath="C:\\Users\\larry\\Documents\\VideoGenerator\\firstframe.png";
    private final String videoFilePath="C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Videos\\data_test1.rgb";
    private int width;
    private int height;

    public VideoPlay(int width, int height, String windowName) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        this.width=width;
        this.height =height;
        frame=new JFrame(windowName);
        parentPanel=new JPanel();
        lbIm1=new JLabel(new ImageIcon(firstFramePath));
        videoPanel=new JPanel();
        videoPanel.add(lbIm1);
        playBtn = new JButton("Play");
        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(playBtn.getText()=="Play"){
                    playBtn.setText("Pause");
                    Player.play=true;
                }else{
                    playBtn.setText("Play");
                    Player.play=false;

                }
            }
        });
        stopBtn = new JButton("Stop");
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Player.stop=true;
                    videoIS = new FileInputStream(videoFile);
                    videoInputStream = new BufferedInputStream(videoIS);
                    lbIm1.setIcon(new ImageIcon(firstFramePath));
                    videoBuffer=new byte[width * height * 3];
                    TimeUnit inputDetect=TimeUnit.MILLISECONDS;
                    inputDetect.sleep(10);
                    frame.repaint();
                    playBtn.setText("Play");
                    Player.videoFrameIndex=0;
                    Player.play=false;
                    Player.stop=false;
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            }
        });
        controlPanel = new JPanel();
        controlPanel.add(playBtn);
        controlPanel.add(stopBtn);
        controlPanel.setLayout(new FlowLayout());
        parentPanel.add(videoPanel);
        parentPanel.add(controlPanel);
        parentPanel.setLayout(new BoxLayout(parentPanel, BoxLayout.Y_AXIS));
        frame.getContentPane().add(parentPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public void run() {
        try {
            videoFile = new File(videoFilePath);
            videoIS = new FileInputStream(videoFile);
            videoBuffer = new byte[width * height * 3];
            videoInputStream = new BufferedInputStream(videoIS);
            Mat mat = new Mat(height, width, CV_8UC3, new Scalar(0, 0, 0));
            int r, g, b;
            TimeUnit inputDetect=TimeUnit.MILLISECONDS;
            long start= System.currentTimeMillis();
            double fps = (1.0 / 30) * 1000-4;
            int readBytes = 0;
            double deltaNanos=0.0;
            long timeSpent=0;
            long millis_prev = System.currentTimeMillis();
            while (readBytes!= -1) {
                readBytes=videoInputStream.read(videoBuffer, 0, videoBuffer.length);
                // read current frame
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        r = videoBuffer[x + y * width];
                        g = videoBuffer[x + y * width + height * width];
                        b = videoBuffer[x + y * width + height * width * 2];
                        if (r < 0) r += 256;
                        if (g < 0) g += 256;
                        if (b < 0) b += 256;
                        double[] pixel = {b, g, r};
                        mat.put(y, x, pixel);
                    }
                }
                Player.videoFrameIndex++;
                //opencv image to swing
                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".jpg", mat, matOfByte);
                byte[] byteArray = matOfByte.toArray();
                InputStream in = new ByteArrayInputStream(byteArray);
                BufferedImage bufImage = ImageIO.read(in);
                lbIm1.setIcon(new ImageIcon(bufImage));
                frame.repaint();
                long millis_spent = System.currentTimeMillis() - millis_prev;

                // produce constant fps
                if(millis_spent<=34){
                    TimeUnit mills=TimeUnit.MILLISECONDS;
                    TimeUnit nanos=TimeUnit.NANOSECONDS;
                    int waitMills= (int) (fps-millis_spent);
                    int waitNanos= (int)(1000*(fps-(int)fps));
                    deltaNanos+=fps-(int)fps-(double)waitNanos/1000;
                    if (deltaNanos>0.001){
                        waitNanos+=1;
                        deltaNanos-=0.001;
                    }
                    mills.sleep(waitMills);
                    nanos.sleep(waitNanos);
                }
                timeSpent+=System.currentTimeMillis()- millis_prev;
                while(Player.play==false){
                    inputDetect.sleep(10);
                }
                millis_prev = System.currentTimeMillis();
            }
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("timeElapsed:" + timeElapsed);
            System.out.println(timeSpent);
            CleanUp();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void CleanUp(){
        try {
            videoIS.close();
            Player.play=false;
            playBtn.setText("Play");
            lbIm1=new JLabel(new ImageIcon(firstFramePath));
            frame.repaint();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
