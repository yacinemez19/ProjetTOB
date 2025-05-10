package com.timeline;

import javafx.scene.control.Slider;

public class Curseur {

    private Slider timeSlider;
    private double position;

    public Curseur(Slider timeSlider) {
        this.timeSlider = timeSlider;

        // Écoute les changements manuels du curseur par l'utilisateur
        this.timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                // L'utilisateur vient de finir de déplacer le curseur
                this.position = timeSlider.getValue();
            }
        });
    }

    public void setPosition(double position) {
        this.position = position;
        timeSlider.setValue(position);
    }

    public double getPosition() {
        return position;
    }

}
