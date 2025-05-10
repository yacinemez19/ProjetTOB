package com.timeline;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.Node;
import javafx.scene.control.Slider;

import java.io.IOException;

public class TimelineController {

    @FXML
    private SplitPane timelineSplitPane;

    @FXML
    private Slider timeSlider;

    private Curseur curseur;

    @FXML
    public void initialize() {

            initializeTimer();

            // Initialisation correcte avec le slider FXML
            curseur = new Curseur(timeSlider);
            curseur.setPosition(0);

            // Répartir équitablement les tracks dans l'espace alloué
            int trackCount = timelineSplitPane.getItems().size();
            double[] positions = new double[trackCount - 1];

            for (int i = 1; i < trackCount; i++) {
                positions[i - 1] = (1.0 / trackCount) * i;
            }
            timelineSplitPane.setDividerPositions(positions);

        // Lier le slider au curseur
        timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            curseur.setPosition(newVal.doubleValue()); // Position en secondes ou unités de temps
        });

    }

    private void addTrackButton(String name) throws IOException {
        //System.out.println("======TRACK FXML : " + getClass().getResource("../ressources/views/com.timeline/Track.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../ressources/views/timeline/Track.fxml"));
        Node trackNode = loader.load();
        TrackController controller = loader.getController();
        controller.setTrackName(name);
        timelineSplitPane.getItems().add(trackNode);
    }

    @FXML
    private void addTrackButton() {
        try{
            addTrackButton("Track");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerTrack() {

    }

    @FXML
    private void separer() {

    }

    // Label pour afficher le temps écoulé
    @FXML
    private Label timerLabel;

    @FXML
    public void initializeTimer() {
        timerLabel.setText("00:00:00");
    }

}
