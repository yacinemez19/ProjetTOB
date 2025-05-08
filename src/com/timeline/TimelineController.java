package com.timeline;

import com.VideoProject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TimelineController implements Initializable {

    private VideoProject videoProject;

    @FXML
    private SplitPane timelineSplitPane;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {

            initializeTimer();

            // Répartir équitablement les tracks dans l'espace alloué
            int trackCount = timelineSplitPane.getItems().size();
            double[] positions = new double[trackCount - 1];

            for (int i = 1; i < trackCount; i++) {
                positions[i - 1] = (1.0 / trackCount) * i;
            }
            timelineSplitPane.setDividerPositions(positions);

    }

    /*
     * Setter pour le projet vidéo
     * @param videoProject le projet vidéo à définir
     */
    public void setVideoProject(VideoProject videoProject) {
        this.videoProject = videoProject;
    }

    private void addTrackButton(String name) throws IOException {
        //System.out.println("======TRACK FXML : " + getClass().getResource("../ressources/views/com.timeline/Track.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../ressources/views/timeline/Track.fxml"));
        Node trackNode = loader.load();
        TrackController controller = loader.getController();
        controller.setTrackName(name);
        timelineSplitPane.getItems().add(trackNode);
        videoProject.addTrack(new Track(name));
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
