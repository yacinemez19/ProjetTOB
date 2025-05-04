package examples;/*
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
public class ComplexPipeline {

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
        Gst.init(Version.BASELINE, "examples.BasicPipeline", args);

        /**
         * Use Gst.parseLaunch() to create a pipeline from a GStreamer string
         * definition. This method returns Pipeline when more than one element
         * is specified.
         */
        //pipeline = (Pipeline) Gst.parseLaunch("videotestsrc ! autovideosink");

        Element source = ElementFactory.make("uridecodebin", "source");
        // Audio Sink
        Element converter = ElementFactory.make("audioconvert", "audioconverter");
        if (converter == null) {
            System.err.println("Erreur : impossible de créer audioconvert");
        }
        Element resample = ElementFactory.make("audioresample", "audiosample");
        if (resample == null) {
            System.err.println("Erreur : impossible de créer audioconvert");
        }
        Element audioSink = ElementFactory.make("autoaudiosink", "audiosink");
        if (audioSink == null) {
            System.err.println("Erreur : impossible de créer audioconvert");
        }
        // Video Sink
        Element videoConverter = ElementFactory.make("videoconvert", "videoconverter");
        Element fxSink = ElementFactory.make("autovideosink", "videosink");
        // On créé une pipelin vide et on lui ajoute tout les éléments
        pipeline = new Pipeline("testPipeline");
        pipeline.add(source);
        pipeline.add(converter);
        pipeline.add(resample);
        pipeline.add(audioSink);
        pipeline.add(videoConverter);
        pipeline.add(fxSink);
        // On link l'audio
        converter.link(resample);
        resample.link(audioSink);
        // On link la vidéo
        videoConverter.link(fxSink);
        // On set la source et on ajoute le pad-added signal
        source.set("uri", "https://gstreamer.freedesktop.org/data/media/sintel_trailer-480p.webm");
        source.connect(
                new Element.PAD_ADDED() {
                    @Override
                    public void padAdded(Element element, Pad pad) {
                        Pad audioSinkPad = converter.getStaticPad("sink");
                        Pad videoSinkPad = videoConverter.getStaticPad("sink");
                        if (audioSinkPad == null) {
                            System.err.println("audioSinkPad est null !");
                        }
                        if (videoSinkPad == null) {
                            System.err.println("videoSinkPad est null !");
                        }

                        System.out.println("New pad received " + pad.getName() + " from " + element.getName());

                        // On récupère le type de fichier
                        Caps caps = pad.getCurrentCaps();
                        String type = caps.getStructure(0).getName();
                        System.out.println("Le type : " + type);

                        if (!audioSinkPad.isLinked() && type.equals("audio/x-raw")) {
                            pad.link(audioSinkPad);
                            System.out.println("Audio Sink Pad linked");
                        }
                        if (!videoSinkPad.isLinked() && type.equals("video/x-raw")) {
                            pad.link(videoSinkPad);
                            System.out.println("Video Sink Pad linked");
                        }
                    }
                }
        );

        Bus bus = pipeline.getBus();
        bus.connect((Bus.MESSAGE) (Bus bus1, Message message) -> {
            switch (message.getType()) {
                case ERROR:
                    // Parse l'erreur
                    System.err.println("Erreur : ");
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