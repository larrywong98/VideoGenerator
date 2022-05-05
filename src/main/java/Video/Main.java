package Video;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
//        try {
//            Runtime rt = Runtime.getRuntime();
//            rt.exec("cmd.exe /c conda activate video && python DetectLogo.py");
//
////        Thread preProcess=new Thread(new Preprocess(args[0],args[1],args[2],args[3],args[4],args[5]));
////        preProcess.start();
//
        Preprocess process=new Preprocess(args[0],args[1],args[2],args[3],args[4],args[5]);
        process.process();
        Player.videoFrameIndex=0;
        Thread playerThread=new Thread(new Player("test.rgb","replaceAds.wav"));
//        Thread playerThread=new Thread(new Player("test.rgb","replaceAds.wav"));
//        playerThread.start();
//            Thread playerThread=new Thread(new Player(args[0],args[1]));
        playerThread.start();

//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Preprocess process=new Preprocess(args[0],args[1],args[2],args[3],args[4],args[5]);
//        process.process();
    }
}
