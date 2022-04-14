package Video;

import java.io.*;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import static org.opencv.core.CvType.*;

public class PlayVideo {
    private int width;
    private int height;
    public PlayVideo(int width,int height){
        this.width=width;
        this.height =height;
    }

    public void Play(){
        try {
            File videoFile = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Videos\\data_test1.rgb");
            InputStream videoIS = new FileInputStream(videoFile);
            //BGR
            byte[] videoBuffer = new byte[width * height * 3];
            BufferedInputStream videoInputStream = new BufferedInputStream(videoIS);
            Mat mat=new Mat(height,width, CV_8UC3, new Scalar(0,0,0));
            int frameIndex=0;
            int r,g,b;
            long start = System.currentTimeMillis();
            while ((videoInputStream.read(videoBuffer, 0, videoBuffer.length)) != -1) {
//                System.out.println(frameIndex);
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
//                frameIndex++;
                HighGui.imshow("Video", mat);
                HighGui.waitKey(8);
            }
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("timeElapsed:"+timeElapsed);

            videoIS.close();
            HighGui.destroyWindow("Video");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
