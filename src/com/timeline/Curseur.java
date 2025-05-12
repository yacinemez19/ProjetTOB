package com.timeline;

import com.VideoProject;
import javafx.scene.control.Slider;

public class Curseur {

    private Slider timeSlider;
    private double position;

    private VideoProject videoProject;

    /**
     * Constructeur de la classe Curseur
     * @param timeSlider
     */
    public Curseur(Slider timeSlider, VideoProject videoProject) {
        this.timeSlider = timeSlider;
        this.videoProject = videoProject;
        initListeners();
    }

    /**
     * Fonction d initialisation des listeners
     */
    private void initListeners() {
        timeSlider.setOnMouseReleased(e -> {
            double value = timeSlider.getValue();
            this.position = value;
        });
    }

    /**
     * Fonction qui permet de mettre à jour la position du curseur
     * @param position
     */
    public void setPosition(double position) {
        this.position = position;
        timeSlider.setValue(position);
    }

    /**
     * Fonction qui retourne la position du curseur
     * @return position
     */
    public double getPosition() {
        return position;
    }
}
