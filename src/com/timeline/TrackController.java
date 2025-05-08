package com.timeline;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class TrackController {

    @FXML
    private Label trackLabel;

    @FXML
    private Pane mediaPane;

    private int px_s = 5; // nombre de pixels par seconde

    private long SECOND = 1_000_000_000L;

    public void initialize() {
/*        mediaPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            redrawTimeline();
        });*/
    }

    public void setTrackName(String name) {
        trackLabel.setText(name);
    }

    /**
     * Dessine un mediaBlock sur la timeline
     *
     * @param startTime le temps de début du mediaBlock
     * @param duration la durée du mediaBlock
     * @param zoom le niveau de zoom
     */
    public void addMediaBlock(TimelineObject timelineObject, float zoom) {
        Region mediaBlock = new TimelineObjectVue("MediaBlock",
                startTime,
                duration,
                zoom,
                px_s,
                mediaPane.getHeight());

        mediaPane.getChildren().add(mediaBlock);
    }

    public void addMediaButton(ActionEvent actionEvent) {
        addMediaBlock(0, 100 * SECOND, 1);
        addMediaBlock(100 * SECOND, 20*SECOND, 1);
    }
}