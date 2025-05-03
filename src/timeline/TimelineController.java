package timeline;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.util.Duration;
import org.freedesktop.gstreamer.ClockTime;
import org.freedesktop.gstreamer.Format;

import java.io.IOException;

public class TimelineController {

    @FXML
    private SplitPane timelineSplitPane;

    @FXML
    public void initialize() {
        try {
            addTrack("Track 1");
            addTrack("Track 2");
            addTrack("Audio 1");
            initializeTimer();

            // Répartir équitablement les tracks dans l'espace alloué
            int trackCount = timelineSplitPane.getItems().size();
            double[] positions = new double[trackCount - 1];

            for (int i = 1; i < trackCount; i++) {
                positions[i - 1] = (1.0 / trackCount) * i;
            }
            timelineSplitPane.setDividerPositions(positions);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTrack(String name) throws IOException {
        //System.out.println("======TRACK FXML : " + getClass().getResource("../ressources/views/timeline/Track.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../ressources/views/timeline/Track.fxml"));
        Node trackNode = loader.load();
        TrackController controller = loader.getController();
        controller.setTrackName(name);
        timelineSplitPane.getItems().add(trackNode);
    }

    @FXML
    private void addTrack() {

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
