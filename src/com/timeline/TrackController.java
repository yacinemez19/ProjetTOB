package com.timeline;

import com.ImportedClip;
import com.VideoProject;
import com.timeline.Track;
import com.Clip;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import javax.xml.datatype.Duration;

import static java.lang.Math.*;

public class TrackController {

    @FXML
    private Label trackLabel;

    @FXML
    private Pane mediaPane;

    private int px_s = 5; // nombre de pixels par seconde

    private long SECOND = 1_000_000_000L;

    private Track model;

    private VideoProject project;

    public void initialize() {
        model = new Track();
    }

    public void setTrackName(String name) {
        trackLabel.setText(name);
    }

    /**
     * Ajoute un clip à la track sous forme TimelineObject
     *
     * @param clip l'objet à ajouter
     * @param zoom le niveau de zoom
     */
    public void addMediaBlock(Clip clip, double zoom, long timing) {
        TimelineObject timelineObject = model.addTimelineObject(clip, timing);

        // Obtenir un nombre aléatoire entre 0 et 255
        int randomColor = (int) (Math.random() * 99);
        timelineObject.setName("Clip " + randomColor);

        TimelineObjectVue mediaBlock = new TimelineObjectVue(timelineObject,
                zoom,
                px_s,
                mediaPane.getHeight());

        mediaPane.getChildren().add(mediaBlock);

        makeDraggable(mediaBlock, timelineObject);
    }

    public void addMediaButton(ActionEvent actionEvent) {
        Clip media = ImportedClip.test();
        addMediaBlock(media, 1.0, 0);

        for (Node timelinePane : mediaPane.getChildren()) {
            if (timelinePane instanceof TimelineObjectVue timelineObjectVue) {
                timelineObjectVue.updatePosition(px_s, 1.0);
            }
        }
    }

    /**
     * Met à jour la position de tous les objets de la timeline.
     */
    private void updatePosition() {
        for (Node timelinePane : mediaPane.getChildren()) {
            if (timelinePane instanceof TimelineObjectVue timelineObjectVue) {
                timelineObjectVue.updatePosition(px_s, 1.0);
            }
        }
    }

    private void makeDraggable(TimelineObjectVue TOview, TimelineObject TOmodel) {
        final Delta drag = new Delta();

        // TODO : améliorer le code

        TOview.setOnMouseEntered(e -> TOview.setCursor(Cursor.HAND));
        TOview.setOnMousePressed(e -> {
            drag.startX = e.getSceneX();
            drag.origLayoutX = TOview.getLayoutX();
            // on remonte cet objet au-dessus des autres
            TOview.toFront();
            e.consume();
        });

        TOview.setOnMouseDragged(e -> {
            double deltaPx   = e.getSceneX() - drag.startX;
            double proposedX = max(drag.origLayoutX + deltaPx, 0);

            // Pour chaque autre bloc, vérifier l’intersection et ajuster
            for (Node other : mediaPane.getChildren()) {
                if (other == TOview) continue;
                Bounds bOther = other.getBoundsInParent();
                Bounds bSelf  = new BoundingBox(proposedX, TOview.getLayoutY(),
                        TOview.getBoundsInParent().getWidth(),
                        TOview.getBoundsInParent().getHeight());
                if (bSelf.intersects(bOther)) {
                    // si on glisse vers la droite, on bute sur le bord gauche de l'autre
                    if (deltaPx > 0) {
                        proposedX = bOther.getMinX() - bSelf.getWidth();
                    }
                    // si on glisse vers la gauche, on bute sur le bord droit de l'autre
                    else {
                        proposedX = bOther.getMaxX();
                    }
                    break;
                }
            }

            System.out.println(proposedX);
            TOview.setLayoutX(proposedX);
            long newStartNs = Math.round(proposedX * SECOND / px_s);
            TOmodel.setStart(newStartNs);

            updatePosition();
            e.consume();
        });
    }

    /** Petit objet pour mémoriser l’état entre press/drag/release */
    private static class Delta {
        double startX, origLayoutX;
    }

    /** Largeur totale des contenus, pour éviter de sortir de l’écran */
    private double computeMaxContentWidth() {
        return mediaPane.getChildren().stream()
                .mapToDouble(n -> n.getLayoutX() + n.getBoundsInParent().getWidth())
                .max().orElse(mediaPane.getWidth());
    }
}