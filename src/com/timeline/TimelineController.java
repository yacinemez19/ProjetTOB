package com.timeline;

import com.VideoProject;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.Node;
import javafx.scene.control.Slider;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TimelineController implements Initializable {

    private VideoProject videoProject;

    @FXML
    private SplitPane timelineSplitPane;

    @FXML
    private Slider timeSlider;

    private Curseur curseur;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

            initializeTimer();

            // Initialisation correcte avec le slider FXML
            curseur = new Curseur(timeSlider, videoProject);
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

    /*
     * Setter pour le projet vidéo
     * @param videoProject le projet vidéo à définir
     */
    public void setVideoProject(VideoProject videoProjectToSet) {
        this.videoProject = videoProjectToSet;
        for (Track track : videoProject.getTracks()) {
            try {
                addTrackVue(track);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        videoProject.getTracks().addListener((ListChangeListener<Track>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Track track : change.getAddedSubList()) {
                        try {
                            addTrackVue(track);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void addTrackVue(Track trackModel) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../ressources/views/timeline/Track.fxml"));
        Node trackNode = loader.load();
        TrackController controller = loader.getController();

        controller.setModel(trackModel);
        controller.setTrackName(trackModel.getName());
        timelineSplitPane.getItems().add(trackNode);
    }

    @FXML
    private void addTrackButton() throws IOException {
        String name = "Track " + (timelineSplitPane.getItems().size() + 1);
        Track model = new Track(name);
        videoProject.addTrack(model);
        addTrackVue(model);
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
