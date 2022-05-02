package Video;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.*;

public class PlayVideo {
    private int width;
    private int height;
    private HashMap<String, ArrayList<Scalar>> brands;

    public PlayVideo(int width,int height){
        this.width=width;
        this.height =height;
    }

    private HashMap<String, ArrayList<Scalar>> readBrands() {
        HashMap<String, ArrayList<Scalar>> res = new HashMap<>();
        try
        {
            File dir = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset2\\Videos\\Brand_Images");
            File[] files = dir.listFiles((d, name) -> name.endsWith(".rgb"));

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
                //Writing the image
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


    public void Play() {
        try {
            File videoFile = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset2\\Videos\\data_test1.rgb");
            InputStream videoIS = new FileInputStream(videoFile);
            //BGR
            byte[] videoBuffer = new byte[width * height * 3];
            BufferedInputStream videoInputStream = new BufferedInputStream(videoIS);
            Mat mat = new Mat(height, width, CV_8UC3, new Scalar(0,0,0));
            brands = readBrands();
            int frameIndex=0;
            int r,g,b;
            long start = System.currentTimeMillis();

            double fps = (1.0 / 30) * 1000;
            long millis_prev = System.currentTimeMillis();
            while ((videoInputStream.read(videoBuffer, 0, videoBuffer.length)) != -1) {
//                System.out.println(frameIndex);

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
//                frameIndex++;


                Mat blurredImage = new Mat();
                Mat blurredImage_inv = new Mat();
                Mat hsvImage = new Mat();
                Mat mask = new Mat();
                Mat morphOutput = new Mat();
                ArrayList<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();

                // remove noise and convert to HSV
                Imgproc.blur(mat, blurredImage, new Size(7, 7));
//                Core.bitwise_not(blurredImage, blurredImage_inv);  // ~blurredImage to avoid hue wrap around
                Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

                // threshold HSV image to select brands
                for (Map.Entry<String, ArrayList<Scalar>> brand : brands.entrySet()) {
                    Boolean masked = false;
                    for (Scalar brand_color : brand.getValue()) {
                        Mat mask_color = new Mat();
                        Scalar min_values = new Scalar(brand_color.val[0] - 15, brand_color.val[1] - 50, brand_color.val[2] - 40);
                        Scalar max_values = new Scalar(brand_color.val[0] + 15, brand_color.val[1] + 50, brand_color.val[2] + 40);
                        Core.inRange(hsvImage, min_values, max_values, mask_color);
                        if (!masked) {
                            mask = mask_color;
                            masked = true;
                        }
                        Core.bitwise_or(mask, mask_color, mask);  // merge all masks
                    }

                    // morphological operators
                    // dilate with large element, erode with small ones
                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6, 6));
                    Imgproc.erode(mask, morphOutput, erodeElement);
//                    Imgproc.erode(morphOutput, morphOutput, erodeElement);
                    Imgproc.dilate(morphOutput, morphOutput, dilateElement);
                    Imgproc.dilate(morphOutput, morphOutput, dilateElement);

                    // find contours
                    Imgproc.findContours(morphOutput, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

                    // if any contour exist
                    if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
//                        System.out.println("Detect LOGO: " + brand.getKey() + "!!!");
                        // for each contour, display it in white
                        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                            Imgproc.drawContours(mat, contours, idx, new Scalar(250, 255, 255));
                        }
                    }
                }

                HighGui.imshow("Video", mat);
//                HighGui.imshow("mask", mask);
                long millis_spent = System.currentTimeMillis() - millis_prev;
                HighGui.waitKey((int)(fps - (int)millis_spent));  // for 30 fps
                millis_prev = System.currentTimeMillis();
            }

            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("timeElapsed:" + timeElapsed);

            videoIS.close();
            HighGui.destroyWindow("Video");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
