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
    private PreviewEngine previewEngine;

    // Lance la lecture de la vidéo
    @FXML
    private void handlePlay() {
        previewEngine.engineResume();
    }

    // Lance la lecture en reverse à vitesse normale
    @FXML
    private void handleReverse() {
        previewEngine.enginePause();
/*        pipeline.play();
        boolean result = pipeline.seek(
                -1.0, // Lecture en arrière
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, pipeline.queryPosition(Format.TIME),
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("Seek reverse failed");
        }*/
    }

    // Met la vidéo en pause
    @FXML
    private void handlePause() {
        previewEngine.enginePause();
    }

    // Affiche l’image précédente en mettant la vidéo en pause
    @FXML
    public void handleLastFrame() {
        previewEngine.engineLastFrame();
    }

    // Affiche l’image suivante en mettant la vidéo en pause
    @FXML
    public void handleNextFrame() {
        previewEngine.engineNextFrame();
    }

    public void togglePlayPause(){
        previewEngine.engineTogglePlayPause();
    }

    // Compteur en secondes depuis le début de la vidéo
    private int secondsElapsed = 0;

    // Méthode appelée automatiquement par JavaFX après le chargement du FXML
    @FXML
    public void initialize() {
        // On attend de recevoir le previewEngine du videoProject
    }

    public PreviewEngine getPreviewEngine() {
        return previewEngine;
    }

    public void setPreviewEngine(PreviewEngine previewEngine) {
        // On récupère le previewEngine et on le lance
        this.previewEngine = previewEngine;
        // Création d'une com.timeline pour l'animation du timer
        timerTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            long[] position = new long[1];
            position[0] = previewEngine.engineGetCurrentPosition();
            if(position[0] != -1) {
                timerLabel.setText(String.format(ClockTime.toString(position[0])));
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE); // on lance la com.timeline que quand la vidéo est prête

        // Pour gérer pause/play
        PreviewListener myPreviewListener = new PreviewListener() {
            @Override
            public void onPlaying() {timerTimeline.play();}
            @Override
            public void onPaused() {
                timerTimeline.play();
            }
        };
        previewEngine.engineStart(videoView, myPreviewListener);
    }
}
