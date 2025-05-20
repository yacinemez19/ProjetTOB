package com.timeline;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class TimelineObjectVue extends StackPane {

    private long SECOND = 1_000_000_000L; // nanosecondes
    private double dragAnchorX;
    private double initialLayoutX;

    private TimelineObject model;

    public TimelineObjectVue(TimelineObject model,
                             double zoom,
                             int px_s,
                             double height) {

        this.setStyle("-fx-background-color: cornflowerblue; -fx-border-color: black;");
        this.setPrefHeight(height); // toute la hauteur

        double inv_scale = SECOND / ( px_s * zoom);

        this.setPrefWidth(model.getDuration()/inv_scale);
        this.setLayoutX(model.getStart() / inv_scale);

        // ** nouvelle partie : ajouter un label **
        Label nameLabel = new Label(model.getName());
        // optionnel : couleur de texte, marge interne, police…
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setPadding(new Insets(2, 4, 2, 4));
        // on aligne à gauche-centre
        StackPane.setAlignment(nameLabel, Pos.CENTER_LEFT);
        this.getChildren().add(nameLabel);

        this.model = model;

        //enableDrag(px_s);
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

    public void updatePosition(int px_s, double zoom) {
        double inv_scale = SECOND / ( px_s * zoom);
        this.setLayoutX(model.getStart() / inv_scale);
    }
}
