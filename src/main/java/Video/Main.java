package Video;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

//        Preprocess process=new Preprocess(args[0],args[1],args[2],args[3],args[4],args[5]);
//        process.process();
//        Player.videoFrameIndex=0;
        Thread playerThread=new Thread(new Player("test.rgb","replaceAds.wav"));
        playerThread.start();


    }
}
