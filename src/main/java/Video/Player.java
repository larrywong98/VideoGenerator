package Video;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static org.opencv.core.CvType.CV_8UC3;

public class Player implements Runnable{
    public static Boolean play;
    public static Boolean stop;
    // 4800 5999
    public static final int EXTERNAL_BUFFER_SIZE= 4800;
    public static final int audioFrameCount=6035;
    public static byte[][] audioData=new byte[audioFrameCount][EXTERNAL_BUFFER_SIZE];//0.05s
    public static int videoFrameIndex=0;
    private SourceDataLine dataLine;
    private String videoFile;

    public Player(String videoFile,String audioFilePath) {
        try {
            nu.pattern.OpenCV.loadLocally();
            this.videoFile=videoFile;
            File audioFile = new File(audioFilePath);
            InputStream audioIS = new FileInputStream(audioFile);
            InputStream bufferedIn = new BufferedInputStream(audioIS);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
            AudioFormat audioFormat = audioInputStream.getFormat();
            Info info = new Info(SourceDataLine.class, audioFormat);
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, Player.EXTERNAL_BUFFER_SIZE);
            dataLine.start();
            int readBytes = 0;
            int second=0;
            //read audio to memory
            while(readBytes!=-1){
                byte[] audioBuffer = new byte[Player.EXTERNAL_BUFFER_SIZE];
                readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
                audioData[second]= Arrays.copyOf(audioBuffer, audioBuffer.length);
                second++;
            }
            Player.play=false;
            Player.stop=false;
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

    @Override
    public void run() {
        try {
            Thread audioThread=new Thread(new AudioPlay(dataLine));
            VideoPlay videoPlay = new VideoPlay(480, 270,"Synchronized Video",videoFile);
            Thread videoThread = new Thread(videoPlay);
            audioThread.start();
            videoThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

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
                while(Player.play==false){
                    TimeUnit.MILLISECONDS.sleep(10);
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
    private final String initFramePath="initframe.png";
    private final double fps = (1.0 / 32.4) * 1000;
//    private String videoFilePath;
    private int width;
    private int height;
    private HashMap<String, ArrayList<Scalar>> brands;

    /**
     * User Interface
     */
    public VideoPlay(int width, int height, String windowName,String videoFilePath) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        this.width=width;
        this.height =height;
//        this.videoFilePath=videoFilePath;
        videoFile = new File(videoFilePath);
        frame=new JFrame(windowName);
        parentPanel=new JPanel();
        lbIm1=new JLabel(new ImageIcon(initFramePath));
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
                    lbIm1.setIcon(new ImageIcon(initFramePath));
                    videoBuffer=new byte[width * height * 3];
                    TimeUnit.MILLISECONDS.sleep(100);
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
    private HashMap<String, ArrayList<Scalar>> readBrands() {
        HashMap<String, ArrayList<Scalar>> res = new HashMap<>();
        try
        {
            File dir = new File("dataset");
            File[] files= dir.listFiles((d, name) -> name.endsWith("_logo.rgb"));
            for (File file : files) {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(0);
                Mat mat = new Mat(height, width, CV_8UC3, new Scalar(0,0,0));
                byte[] bytes = new byte[width * height * 3];
                raf.read(bytes);
                int gap = 256 / 16;
                ArrayList<Integer> quant_vals = new ArrayList<Integer>();
                for (int i = gap - 1; i < 256; i += gap) {
                    quant_vals.add(i - gap / 2);
                }
                int r, g, b;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        r = bytes[x + y * width];
                        g = bytes[x + y * width + height * width];
                        b = bytes[x + y * width + height * width * 2];
                        if (r < 0) r += 256;
                        if (g < 0) g += 256;
                        if (b < 0) b += 256;
                        // map to corresponding range
                        r = quant_vals.get(r / gap);
                        g = quant_vals.get(g / gap);
                        b = quant_vals.get(b / gap);
                        double[] pixel = {b, g, r};
                        mat.put(y, x, pixel);
                    }
                }

                Mat hsvImage = new Mat();
                Imgproc.cvtColor(mat, hsvImage, Imgproc.COLOR_BGR2HSV);
                Imgcodecs imageCodecs = new Imgcodecs();
                imageCodecs.imwrite("output.jpg", hsvImage);
                // get all color
                HashMap<Integer, int[]> colors = new HashMap<Integer, int[]>();  // map<hue, [count, s, v]>
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        double[] hsv_double = hsvImage.get(y, x);
                        int[] hsv = new int[]{(int)hsv_double[0], (int)hsv_double[1], (int)hsv_double[2]};

                        if (!colors.containsKey(hsv[0])) {
                            colors.put(hsv[0], new int[]{1, hsv[1], hsv[2]});
                        } else {
                            colors.put(hsv[0], new int[]{colors.get(hsv[0])[0] + 1, hsv[1], hsv[2]});
                        }
                    }
                }
                // get top n dominant colors -> at least 70% of all pixels
                Map<Integer, int[]> sorted_colors = colors.entrySet().stream()
                        .sorted((e1, e2) -> Integer.compare(e2.getValue()[0], e1.getValue()[0]))
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));

                ArrayList<Scalar> brand_values = new ArrayList<>();
                int pix_count = 0, pix_threshold = (int)(0.9 * width * height);
                for (Map.Entry<Integer, int[]> e : sorted_colors.entrySet()) {
                    if (pix_count > pix_threshold) break;
                    // skip white, avoid detecting sky
                    if (e.getKey() != 0) brand_values.add(new Scalar(e.getKey(), e.getValue()[1], e.getValue()[2]));
                    pix_count += e.getValue()[0];
//                    System.out.println(e.getKey() + ": " + e.getValue()[0] + ", " + e.getValue()[1] + ", " + e.getValue()[2]);
                }
                res.put(file.getName(), brand_values);
                System.out.println("start parsing next");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Video Play with 30 fps
     */
    public void run() {
        try {
            while(true){
                videoIS = new FileInputStream(videoFile);
                videoBuffer = new byte[width * height * 3];
                videoInputStream = new BufferedInputStream(videoIS);
                Mat mat = new Mat(height, width, CV_8UC3, new Scalar(0, 0, 0));
                int r, g, b;
                long start= System.currentTimeMillis();
                brands = readBrands();
                int readBytes = 0;
//                Player.play=true;
                while(Player.play==false){
                    TimeUnit.MILLISECONDS.sleep(10);
                }

                // 2585 3035 7006 7455
//                while(Player.videoFrameIndex<4100){
//                    videoInputStream.read(videoBuffer, 0, videoBuffer.length);
//                    Player.videoFrameIndex++;
//                }

                long timeSpent=0;
                long millis_prev = System.currentTimeMillis();
                double deltaTime=0.0;
                int preFrameMillsDiff=0;
                int preFrameNanosDiff=0;
                int waitMills=0;
                int waitNanos=0;
//                double maxEntropy=0;
//                double prevSumDiff=0;
                ArrayList<Integer> rawTimeStamps = new ArrayList<>();
                ArrayList<Integer> timeStamps = new ArrayList<>();
//                int[] previousR=new int[width * height];


                while(readBytes!=-1){
//                while (Player.videoFrameIndex<900) {
                    readBytes=videoInputStream.read(videoBuffer, 0, videoBuffer.length);
                    double sumdiff=0;
                    double[] pixel={0,0,0};
                    // read current frame
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            r = videoBuffer[x + y * width];
                            g = videoBuffer[x + y * width + height * width];
                            b = videoBuffer[x + y * width + height * width * 2];
                            if (r < 0) r += 256;
                            if (g < 0) g += 256;
                            if (b < 0) b += 256;
                            pixel = new double[]{b, g, r};
//                            sumdiff += Math.abs(r - previousR[y * width + x]);
//                            previousR[y * width + x]=r;
                            mat.put(y, x, pixel);
                        }
                    }
//                    if (sumdiff > 7900000) {
//                        System.out.println(sumdiff + " " + (sumdiff - prevSumDiff) + " " + Player.videoFrameIndex);
//                    }
//                    prevSumDiff = sumdiff;
//
//                    if(Player.videoFrameIndex>6200 && Player.videoFrameIndex<7000){
//                        Imgcodecs.imwrite("dataset\\ad6_hrc\\hrc"+Player.videoFrameIndex+".png",mat);
//                    }
//                    Imgcodecs.imwrite("dataset\\Allframes\\"+Player.videoFrameIndex+".png",mat);
//

//                    //opencv image to swing
                    MatOfByte matOfByte = new MatOfByte();
                    Imgcodecs.imencode(".jpg", mat, matOfByte);
                    lbIm1.setIcon(new ImageIcon(ImageIO.read(new ByteArrayInputStream(matOfByte.toArray()))));
                    frame.repaint();
                    double millis_spent = System.currentTimeMillis() - millis_prev;
                    // produce constant fps
                    deltaTime=fps-millis_spent;
                    if (deltaTime > 0) {
                        waitMills = (int) deltaTime;
                        waitNanos = (int) (1000 * (fps - (int) fps));
                        if(waitMills-preFrameMillsDiff>0){
                            TimeUnit.MILLISECONDS.sleep(waitMills-preFrameMillsDiff);
                            TimeUnit.NANOSECONDS.sleep(waitNanos-preFrameNanosDiff);
                            preFrameMillsDiff = 0 ;
                            preFrameNanosDiff = 0 ;
                        }else{
                            preFrameMillsDiff = preFrameMillsDiff-waitMills ;
                            preFrameNanosDiff = preFrameNanosDiff-waitNanos ;
                        }
                    } else {
                        deltaTime = -deltaTime;
                        preFrameMillsDiff += (int) deltaTime;
                        preFrameNanosDiff += (int) (1000 * (deltaTime - (int) deltaTime));
                    }
                    timeSpent+=System.currentTimeMillis()- millis_prev;
                    while(Player.play==false){
                        TimeUnit.MILLISECONDS.sleep(10);
                    }
                    Player.videoFrameIndex++;
                    millis_prev = System.currentTimeMillis();
                }
                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                System.out.println("timeElapsed:" + timeElapsed);
                System.out.println(timeSpent);
                ToInitState();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * Back to Initial State
     */
    public void ToInitState() {
        try {
            Player.play = false;
            Player.videoFrameIndex=0;
            playBtn.setText("Play");
            lbIm1.setIcon(new ImageIcon(initFramePath));
            TimeUnit.MILLISECONDS.sleep(100);
            frame.repaint();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
