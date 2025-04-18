import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.event.SeekType;
import org.freedesktop.gstreamer.fx.FXImageSink;
import org.freedesktop.gstreamer.message.Message;

import java.util.EnumSet;

public class Preview {

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
        pipeline.pause();
    }

    // Affiche l’image précédente en mettant la vidéo en pause
    @FXML
    private void handleLastFrame() {
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
    private void handleNextFrame() {
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

    // Compteur en secondes depuis le début de la vidéo
    private int secondsElapsed = 0;

    // Méthode appelée automatiquement par JavaFX après le chargement du FXML
    @FXML
    public void initialize() {
        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsElapsed++;
            int minutes = secondsElapsed / 60;
            int seconds = secondsElapsed % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();

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
        Gst.init(Version.BASELINE, "BasicPipeline");

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
        source.set("uri", "https://gstreamer.freedesktop.org/data/media/sintel_trailer-480p.webm");
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

                        if (!audioSinkPad.isLinked() && type.equals("audio/x-raw")) {
                            System.out.println("Audio Sink Pad not linked");
                            pad.link(audioSinkPad);
                        }
                        if (!videoSinkPad.isLinked() && type.equals("video/x-raw")) {
                            System.out.println("Video Sink Pad not linked");
                            pad.link(videoSinkPad);
                        }
                    }
                }
        );
        pipeline.setState(State.PAUSED);
        // attendre que les pads soient liés
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("on lance la pipeline");
        pipeline.setState(State.PLAYING);
/*        pipeline.getBus().connect((Bus.ASYNC_DONE) bus -> {
            System.out.println("Pipeline ready — now playing.");
            pipeline.play();
        });*/

/*        // loop on EOS if button selected
        pipeline.getBus().connect((Bus.EOS) source1 -> {
            // handle on event thread!
                    pipeline.stop();
        });*/
    }
}
