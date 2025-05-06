package com.preview;

import com.Utils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.event.SeekType;
import org.freedesktop.gstreamer.fx.FXImageSink;
import org.freedesktop.gstreamer.message.StateChangedMessage;

import java.io.File;

public class PreviewEngine {

    // Attributs
    private boolean isStarted = false;
    private Pipeline pipeline;
    private FXImageSink fxSink;

    /**
     * Permet de lancer le rendu dans le Preview
     * @param videoView Le sink ou on affichera la vidéo
     * @param previewListener Permet de définir des comportements spécifiques pour quand le preview est mis en pause/play
     */
    public void engineStart(ImageView videoView, PreviewListener previewListener) {
        /*
         * Set up paths to native GStreamer libraries - see adjacent file.
         */
        Utils.configurePaths();
        /*
         * Initialize GStreamer. Always pass the lowest version you require -
         * Version.BASELINE is GStreamer 1.8. Use Version.of() for higher.
         * Features requiring later versions of GStreamer than passed here will
         * throw an exception in the bindings even if the actual native library
         * is a higher version.
         */
        Gst.init(Version.BASELINE, "PreviewPipeline");
        /**
         * ON cook ici
         * Création des éléments GStreamer (source, convertisseurs, sinks audio et vidéo)
         * audioconvert et audioresample permette d'avoir un son correct en sortie dans le audiosink
         */
        Element source = ElementFactory.make("uridecodebin", "source");
        // Audio Sink
        Element converter = ElementFactory.make("audioconvert", "audioconverter");
        Element resample = ElementFactory.make("audioresample", "audiosample");
        Element audioSink = ElementFactory.make("autoaudiosink", "audiosink");
        // Video Sink
        Element videoConverter = ElementFactory.make("videoconvert", "videoconverter");
        fxSink = new FXImageSink();
        // On créé une pipelin vide et on lui ajoute tout les éléments
        pipeline = new Pipeline("testPipeline");
        pipeline.add(source);
        pipeline.add(converter);
        pipeline.add(resample);
        pipeline.add(audioSink);
        pipeline.add(videoConverter);
        pipeline.add(fxSink.getSinkElement());
        // On link la fin de la pipeline audio converter->resample->audiosink
        converter.link(resample);
        resample.link(audioSink);
        // On link la vidéo videoConverter->fxSink
        videoConverter.link(fxSink.getSinkElement());
        System.out.println("videoView: " + videoView);
        videoView.imageProperty().bind(fxSink.imageProperty());
        // On set la source et on ajoute le pad-added signal qui permettera de connecter la source aux sorties son et audio
        File videoFile = new File("videos/nuuuuuul.MTS");
        System.out.println("Path: " + videoFile.getAbsolutePath());
        System.out.println("Exists: " + videoFile.exists());
        source.set("uri", videoFile.toURI().toString());
        //source.set("uri", "https://gstreamer.freedesktop.org/data/media/sintel_trailer-480p.webm");
        source.connect(
                new Element.PAD_ADDED() {
                    @Override
                    public void padAdded(Element element, Pad pad) {
                        Pad audioSinkPad = converter.getSinkPads().getFirst();
                        Pad videoSinkPad = videoConverter.getSinkPads().getFirst();

                        System.out.println("New pad received " + pad.getName() + " from " + element.getName());

                        // On récupère le type de fichier
                        Caps caps = pad.getCurrentCaps();
                        String type = caps.getStructure(0).getName();
                        System.out.println(type);
                        // On link l'audio
                        if (!audioSinkPad.isLinked() && type.equals("audio/x-raw")) {
                            System.out.println("Audio Sink Pad linked");
                            pad.link(audioSinkPad);
                        }
                        // On link la vidéo
                        if (!videoSinkPad.isLinked() && type.equals("video/x-raw")) {
                            System.out.println("Video Sink Pad linked");
                            pad.link(videoSinkPad);
                        }
                    }
                }
        );
        pipeline.setState(State.PAUSED);
        // attendre que les pads soient liés
        source.connect((Element.NO_MORE_PADS) (elem) -> {
            System.out.println("Plus de pads à ajouter.");
            pipeline.setState(State.PLAYING);
        });

        // loop on EOS if button selected
        pipeline.getBus().connect((Bus.MESSAGE) (bus, message) -> {
            switch (message.getType()) {
                case ASYNC_START:
                    System.out.println("Async start sur : " + message.getSource());
                    break;
                case ASYNC_DONE:
                    System.out.println("Async done sur : " + message.getSource());
                    break;
                case STATE_CHANGED:
                    StateChangedMessage stateChangedMsg = (StateChangedMessage) message;
                    //State oldState = stateChangedMsg.getOldState();
                    State newState = stateChangedMsg.getNewState();
                    if (newState == State.PLAYING) {
                        previewListener.onPlaying();
                    } else {
                        previewListener.onPaused();
                    }
            }
        });

        // on indique que la pipeline tourne correctement
        isStarted = true;

    }
    /**
     * Permet de mettre en pause le preview peut importe la source
     */
    public void enginePause() {
        if (isStarted) {
            pipeline.pause();
        }
    }
    /**
     * Permet de fermer proprement le Preview en désallouant la mémoire utilisée
     */
    public void engineStop() {
        if (pipeline != null) {
            pipeline.setState(State.NULL);
            pipeline = null;
            isStarted = false;
        }
    }
}
