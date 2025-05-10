package com.timeline;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class TrackController {

    @FXML
    private Label trackLabel;
    @FXML
    private Pane mediaPane;

    public void initialize() {
/*        mediaPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            redrawTimeline();
        });*/
    }

    public void setTrackName(String name) {
        trackLabel.setText(name);
    }

    public void addMediaBlock(double startTime, double duration, double totalDuration) {
        double totalWidth = mediaPane.getWidth(); // largeur totale affichée
        double scale = totalWidth / totalDuration;

        Region mediaBlock = new Region();
        mediaBlock.setStyle("-fx-background-color: cornflowerblue; -fx-border-color: black;");
        mediaBlock.setPrefHeight(mediaPane.getHeight()); // toute la hauteur
        mediaBlock.setPrefWidth(duration * scale);
        mediaBlock.setLayoutX(startTime * scale);

        mediaPane.getChildren().add(mediaBlock);
    }

    public Pane getMediaPane() {
        return mediaPane;
    }

}