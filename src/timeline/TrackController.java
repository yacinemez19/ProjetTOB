package timeline;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TrackController {

    @FXML
    private Label trackLabel;

    public void setTrackName(String name) {
        trackLabel.setText(name);
    }
}