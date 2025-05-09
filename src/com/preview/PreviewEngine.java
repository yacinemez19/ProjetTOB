package com.preview;

import com.Utils;
import com.timeline.TimelineObject;
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
import java.util.EnumSet;

public class PreviewEngine {

    // Attributs
    private boolean isStarted = false;
    private Pipeline pipeline;
    private FXImageSink fxSink;
    private  PadManager padManager = new PadManager();
    private Element audioSelector;
    private  Element videoSelector;

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
        // Les linker dynmaiques
        videoSelector = ElementFactory.make("input-selector", "videoSelector");
        audioSelector = ElementFactory.make("input-selector", "audioSelector");
        // Audio Sink
        Element converter = ElementFactory.make("audioconvert", "audioconverter");
        Element resample = ElementFactory.make("audioresample", "audiosample");
        Element audioSink = ElementFactory.make("autoaudiosink", "audiosink");
        // Video Sink
        Element videoConverter = ElementFactory.make("videoconvert", "videoconverter");
        fxSink = new FXImageSink();
        // On créé une pipelin vide et on lui ajoute tout les éléments
        pipeline = new Pipeline("testPipeline");

        pipeline.add(videoSelector);
        pipeline.add(audioSelector);
        pipeline.add(converter);
        pipeline.add(resample);
        pipeline.add(audioSink);
        pipeline.add(videoConverter);
        pipeline.add(fxSink.getSinkElement());
        // On link la fin de la pipeline audio input-selector->converter->resample->audiosink
        audioSelector.link(converter);
        converter.link(resample);
        resample.link(audioSink);
        // On link la vidéo input-selector->videoConverter->fxSink
        videoSelector.link(videoConverter);
        videoConverter.link(fxSink.getSinkElement());
        System.out.println("videoView: " + videoView);
        videoView.imageProperty().bind(fxSink.imageProperty());

        pipeline.setState(State.PAUSED);

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
    public void preloadClip(TimelineObject newClip) {
        Element source = ElementFactory.make("uridecodebin", "source");
        pipeline.add(source);
        source.set("uri", newClip.getSource().getSource().toString());
        source.connect(
                new Element.PAD_ADDED() {
                    @Override
                    public void padAdded(Element element, Pad pad) {
                        padManager.padLinker(element, pad, audioSelector, videoSelector, newClip);
                    }
                }
        );
        // attendre que les pads soient liés
        source.connect((Element.NO_MORE_PADS) (elem) -> {
            System.out.println("Plus de pads à ajouter.");
            //pipeline.setState(State.PLAYING);
        });

        newClip.setGstreamerSource(source);
    }

    /**
     * Permet de mettre en pause le preview peut importe la source
     */
    public void enginePause() {
        if (isStarted) {
            pipeline.setState(State.PAUSED);
        }
    }
    /**
     * Permet de mettre en pause le preview en play si il avait été mis en pause
     */
    public void engineResume() {
        if (isStarted) {
            pipeline.setState(State.PLAYING);
        }
    }
    /**
     * Permet de passer de play à pause ou inversement sans se soucier de l'état actuel
     */
    public void engineTogglePlayPause() {
        if (isStarted) {
            State currentState = pipeline.getState();
            if (currentState == State.PAUSED) {
                pipeline.setState(State.PLAYING);
            } else {
                pipeline.setState(State.PAUSED);
            }
        }
    }
    /**
     * Avance d'une frame dans la timeline et la met en pause si elle l'était pas
     */
    public void engineNextFrame() {
        // On vérifie que le preview a bien démarré avant de manipuler la pipeline
        if (!isStarted) {
            System.out.println("[Preview Engine] engine not ready");
            return;
        }
        // Pour éviter les soucis on s'assure qu'elle est en pause
        pipeline.setState(State.PAUSED);

        // On cherche la frame actuelle
        long currentPosition = pipeline.queryPosition(Format.TIME);
        long frameDuration = (long) (1_000_000_000 / 25.0); // Durée d'une frame à 25 fps

        // On avance d'une frame
        long newPosition = currentPosition + frameDuration;
        boolean result = pipeline.seek(
                1.0,
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, newPosition,
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("[Preview Engine] Next frame seek failed");
        }
    }

    /**
     * Recule d'une frame dans la timeline et la met en pause si elle l'était pas
     */
    public void engineLastFrame() {
        // On vérifie que le preview a bien démarré avant de manipuler la pipeline
        if (!isStarted) {
            System.out.println("[Preview Engine] engine not ready");
            return;
        }
        // Pour éviter les soucis on s'assure qu'elle est en pause
        pipeline.setState(State.PAUSED);

        // On cherche la frame actuelle
        long currentPosition = pipeline.queryPosition(Format.TIME);
        long frameDuration = (long) (1_000_000_000 / 25.0); // Durée d'une frame à 25 fps

        // On recule d'une frame
        long newPosition = Math.max(0, currentPosition - frameDuration);
        boolean result = pipeline.seek(
                1.0,
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, newPosition,
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("[Preview Engine] Next frame seek failed");
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
    /**
     * Permet de récupérer la position temporel actuelle
     */
    public long engineGetCurrentPosition() {
        if (isStarted) {
            return pipeline.queryPosition(Format.TIME);
        } else {
            return 0;
        }
    }
}
