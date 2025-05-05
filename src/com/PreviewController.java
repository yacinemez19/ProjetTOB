package com;

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

public class PreviewController {

    // Label pour afficher le temps écoulé
    @FXML
    private Label timerLabel;

    // Animation Timeline pour gérer le timer en JavaFX
    private Timeline timerTimeline;

    // Composant JavaFX pour afficher la vidéo
    @FXML
    private ImageView videoView;

    // Pipeline GStreamer et son sink vidéo pour l’intégration JavaFX
    private Pipeline pipeline;
    private FXImageSink fxSink;

    // Lance la lecture de la vidéo
    @FXML
    private void handlePlay() {
        pipeline.play();
    }

    // Lance la lecture en reverse à vitesse normale
    @FXML
    private void handleReverse() {
        pipeline.play();
        boolean result = pipeline.seek(
                -1.0, // Lecture en arrière
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, pipeline.queryPosition(Format.TIME),
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("Seek reverse failed");
        }
    }

    // Met la vidéo en pause
    @FXML
    private void handlePause() {
        pipeline.setState(State.PAUSED);
    }

    // Affiche l’image précédente en mettant la vidéo en pause
    @FXML
    public void handleLastFrame() {
        pipeline.pause();
        long currentPosition = pipeline.queryPosition(Format.TIME);
        long frameDuration = (long) (1_000_000_000 / 25.0); // Durée d'une frame à 25 fps
        long newPosition = Math.max(0, currentPosition - frameDuration);

        boolean result = pipeline.seek(
                1.0, // Lecture normale
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, newPosition,
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("Previous frame seek failed");
        }
    }

    // Affiche l’image suivante en mettant la vidéo en pause
    @FXML
    public void handleNextFrame() {
        pipeline.pause();
        long currentPosition = pipeline.queryPosition(Format.TIME);
        long frameDuration = (long) (1_000_000_000 / 25.0); // Durée d'une frame à 25 fps
        long newPosition = currentPosition + frameDuration;

        boolean result = pipeline.seek(
                1.0,
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, newPosition,
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("Next frame seek failed");
        }
    }

    public void togglePlayPause(){
        System.out.println("togglePlayPause");
        State currentState = pipeline.getState();
        if (currentState == State.PAUSED) {
            pipeline.setState(State.PLAYING);
        } else {
            pipeline.setState(State.PAUSED);
        }
    }

    // Compteur en secondes depuis le début de la vidéo
    private int secondsElapsed = 0;

    // Méthode appelée automatiquement par JavaFX après le chargement du FXML
    @FXML
    public void initialize() {
        // Création d'une com.timeline pour l'animation du timer
        timerTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            long[] position = new long[1];
            position[0] = pipeline.queryPosition(Format.TIME);
            if(position[0] != -1) {
                timerLabel.setText(String.format(ClockTime.toString(position[0])));
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE); // on lance la com.timeline que quand la vidéo est prête

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
                            System.out.println("Audio Sink Pad not linked");
                            pad.link(audioSinkPad);
                        }
                        // On link la vidéo
                        if (!videoSinkPad.isLinked() && type.equals("video/x-raw")) {
                            System.out.println("Video Sink Pad not linked");
                            pad.link(videoSinkPad);
                        }
                    }
                }
        );
        pipeline.setState(State.PAUSED);
        // attendre que les pads soient liés
        source.connect((Element.NO_MORE_PADS) (elem) -> {
            System.out.println("Plus de pads à ajouter.");
            //pipeline.setState(State.PLAYING);
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
                        timerTimeline.play();
                    } else {
                        timerTimeline.stop();
                    }
            }
        });
    }
}
