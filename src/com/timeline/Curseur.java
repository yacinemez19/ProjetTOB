package com.timeline;

import com.VideoProject;
import javafx.scene.control.Slider;

public class Curseur {

    private Slider timeSlider;
    private double position;

    private VideoProject videoProject;

    /**
     * Constructeur de la classe Curseur.
     * @param timeSlider Slider pour le curseur de temps
     * @param videoProject Projet vidéo associé
     */
    public Curseur(Slider timeSlider, VideoProject videoProject) {
        this.timeSlider = timeSlider;
        this.videoProject = videoProject;
        initListeners();
    }

    /**
     * Initialiser des listeners
     */
    private void initListeners() {
        timeSlider.setOnMouseReleased(e -> {
            this.position = timeSlider.getValue();
        });
    }

    /**
     * Mettre à jour la position du curseur.
     * @param position position du curseur
     */
    public void setPosition(double position) {
        this.position = position;
        timeSlider.setValue(position);
    }

    /**
     * Obtenir la position du curseur
     * @return position
     */
    public double getPosition() {
        return position;
    }
}
