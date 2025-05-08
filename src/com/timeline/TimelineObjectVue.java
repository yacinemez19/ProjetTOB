package com.timeline;

import javafx.scene.layout.StackPane;

public class TimelineObjectVue extends StackPane {

    private long SECOND = 1_000_000_000L; // nanosecondes
    private double dragAnchorX;
    private double initialLayoutX;

    private TimelineObject model;

    public TimelineObjectVue(TimelineObject model,
                             float zoom,
                             int px_s,
                             double height) {
        this.setStyle("-fx-background-color: cornflowerblue; -fx-border-color: black;");
        this.setPrefHeight(height); // toute la hauteur

        double inv_scale = SECOND / ( px_s * zoom);

        this.setPrefWidth(model.getDuration()[0]/inv_scale);
        this.setLayoutX(model.getStart()[0] / inv_scale);

        enableDrag(px_s);
    }

    private void enableDrag(double pxPerSecond) {
        setOnMousePressed(evt -> {
            dragAnchorX    = evt.getSceneX();
            initialLayoutX = getLayoutX();
            evt.consume();
        });

        setOnMouseDragged(evt -> {
            double deltaX = evt.getSceneX() - dragAnchorX;
            double newLayoutX = initialLayoutX + deltaX;
            // bornes si besoin : newLayoutX = clamp(newLayoutX, 0, maxX);
            setLayoutX(newLayoutX);
            evt.consume();
        });
    }
}
