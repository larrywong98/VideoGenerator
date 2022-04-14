package Video;

import javax.sound.sampled.AudioInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * plays a wave file using PlaySound class
 *
 * @author Giulio
 */
public class PlayWaveFile {

    /**
     * <Replace this with one clearly defined responsibility this method does.>
     *
     * @param args
     *            the name of the wave file to play
     */
    public static void main(String[] args) {
        nu.pattern.OpenCV.loadLocally();
        try {
            int width = 480;
            int height = 270;
            PlayVideo playVideo = new PlayVideo(width, height);

            File audioFile = new File("C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\dataset1\\Videos\\data_test1.wav");
            InputStream audioIS = null;

            audioIS = new FileInputStream(audioFile);

            PlaySound playSound=new PlaySound(audioIS);


            playVideo.Play();
            playSound.play();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (PlayWaveException e) {
            e.printStackTrace();
        }


    }

}
