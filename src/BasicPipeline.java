/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2021 Neil C Smith - Codelerity Ltd.
 *
 * Copying and distribution of this file, with or without modification,
 * are permitted in any medium without royalty provided the copyright
 * notice and this notice are preserved. This file is offered as-is,
 * without any warranty.
 *
 */

import java.util.concurrent.TimeUnit;

import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.message.Message;

/**
 * Simply launches a test GStreamer pipeline using the Java bindings.
 *
 * @author Neil C Smith ( https://www.codelerity.com )
 */
public class BasicPipeline {

    /**
     * Always store the top-level pipeline reference to stop it being garbage
     * collected.
     */
    private static Pipeline pipeline;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        /**
         * Set up paths to native GStreamer libraries - see adjacent file.
         */
        Utils.configurePaths();

        /**
         * Initialize GStreamer. Always pass the lowest version you require -
         * Version.BASELINE is GStreamer 1.8. Use Version.of() for higher.
         * Features requiring later versions of GStreamer than passed here will
         * throw an exception in the bindings even if the actual native library
         * is a higher version.
         */
        Gst.init(Version.BASELINE, "BasicPipeline", args);

        /**
         * Use Gst.parseLaunch() to create a pipeline from a GStreamer string
         * definition. This method returns Pipeline when more than one element
         * is specified.
         */
        //pipeline = (Pipeline) Gst.parseLaunch("videotestsrc ! autovideosink");

        Element source = ElementFactory.make("videotestsrc", "source");
        // Video Sink
        Element videoConverter = ElementFactory.make("videoconvert", "videoconverter");

        Element fxSink = ElementFactory.make("autovideosink", "videosink");
        // On crée un pipeline vide et on lui ajoute tous les éléments
        pipeline = new Pipeline("testPipeline");
        pipeline.add(source);
        pipeline.add(videoConverter);
        pipeline.add(fxSink);
        // On link la vidéo
        source.link(videoConverter);
        videoConverter.link(fxSink);

        Bus bus = pipeline.getBus();
        bus.connect((Bus.MESSAGE) (Bus bus1, Message message) -> {
            switch (message.getType()) {
                case ERROR:
                    // Parse l'erreur
                    Structure err = message.getStructure();
                    if (err.hasField("details")) {
                        System.out.println("On a les details");
                        Structure details = (Structure) err.getValue("details");
                        System.err.println(details.toString());
                    }
                    System.err.println("Erreur : " + err.toString());
                    break;
                case WARNING:
                    System.out.println("Warning : ");
                    break;
                case INFO:
                    Structure info = message.getStructure();
                    System.out.println("Info : " + info.toString());
                    break;
                case EOS:
                    System.out.println("Fin du stream !");
                    Gst.quit();
                    break;
                default:
                    break;
            }
        });
        pipeline.setState(State.PAUSED);
        // attendre que les pads soient liés
        try {Thread.sleep(1500);}
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("on lance la pipeline");
        pipeline.setState(State.PLAYING);
        /**
         * Start the pipeline.
         */
        // ON CHECK LES PADS
        Pad videoConvertSinkPad = videoConverter.getStaticPad("sink");
        Pad videoConvertSrcPad = videoConverter.getStaticPad("src");
        Pad sourceSrxPad = source.getStaticPad("src");

        // Regarder ce que le pad accepte et propose :
        System.out.println("videoconvert sink caps: " + videoConvertSinkPad.getCurrentCaps());
        System.out.println("videoconvert src caps: " + videoConvertSrcPad.getCurrentCaps());
        System.out.println("videotestsrc src caps: " + sourceSrxPad.getCurrentCaps());
        pipeline.play();

        /**
         * GStreamer native threads will not be taken into account by the JVM
         * when deciding whether to shutdown, so we have to keep the main thread
         * alive. Gst.main() will keep the calling thread alive until Gst.quit()
         * is called. Here we use the built-in executor to schedule a quit after
         * 10 seconds.
         */
        Gst.getExecutor().schedule(Gst::quit, 10, TimeUnit.SECONDS);
        Gst.main();

    }



}