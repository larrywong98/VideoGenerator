package Video;

public class Main {

    /**
     * args2 and args3 order matters
     * args4 and args5 order matters
     *
     * args0: dataset\\data_test1.rgb
     * args1: dataset\\data_test1.wav
     * args2: dataset\\Starbucks_Ad_15s.wav
     * args3: dataset\\Subway_Ad_15s.wav
     * args4: dataset\\Starbucks_Ad_15s.rgb
     * args5: dataset\\Subway_Ad_15s.rgb
     *
     */
    public static void main(String[] args) {
//        Thread preProcess=new Thread(new Preprocess(args[0],args[1],args[2],args[3],args[4],args[5]));
//        preProcess.start();


        Preprocess process=new Preprocess(args[0],args[1],args[2],args[3],args[4],args[5]);
        process.process();
        Thread playerThread=new Thread(new Player("test.rgb","replaceAds.wav"));
        playerThread.start();
//        Thread playerThread=new Thread(new Player(args[0],args[1]));
//        playerThread.start();

    }
}
