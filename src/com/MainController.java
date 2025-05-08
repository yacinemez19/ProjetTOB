package com;

import com.timeline.TimelineController;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class MainController {


    @FXML
    private AnchorPane previewPanel;

    @FXML
    private ImportController importPaneController;

    @FXML
    private MenuBarController menuBarController;

    @FXML
    private TimelineController timelineController;

    private VideoProject videoProject;

    private PreviewController previewController;

    public void setVideoProject(VideoProject videoProject) {
        this.videoProject = videoProject;
        importPaneController.setVideoProject(videoProject);
        menuBarController.setVideoProject(videoProject);
        timelineController.setVideoProject(videoProject);
    }

    public void initialize() {
        try {
            // Charger Preview.fxml dynamiquement
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../ressources/views/Preview.fxml"));
            AnchorPane previewContent = loader.load();
            previewController = loader.getController();
            previewPanel.getChildren().add(previewContent);

            // Configurer les raccourcis une fois que la scène est prête
            Scene scene = previewPanel.getScene();
            if (scene != null) {
                setupKeyboardShortcuts(scene);
            } else {
                // Si la scène n'est pas encore disponible, attendre un peu
                previewPanel.sceneProperty().addListener((obs, oldScene, newScene) -> {
                        setupKeyboardShortcuts(newScene);
                });
            }
            Platform.runLater(() -> {
                previewPanel.requestFocus(); // ou un parent direct plus global
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (!(scene.getFocusOwner() instanceof TextField)) { // évite que le raccourci se lance dans la barre de recherche
                switch (event.getCode()) {
                    case SPACE:
                        previewController.togglePlayPause();
                        event.consume(); // évite que le bouton soit activé par espace
                        break;
                    case LEFT:
                        previewController.handleLastFrame();
                        event.consume();
                        break;
                    case RIGHT:
                        previewController.handleNextFrame();
                        event.consume();
                        break;
                }
            }
        });
    }

}

