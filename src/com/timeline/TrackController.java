package com.timeline;

import com.VideoClip;
import com.VideoProject;
import com.Clip;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

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

        model.getElements().addListener((ListChangeListener<TimelineObject>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (TimelineObject obj : change.getAddedSubList()) {
                        // afficher dans la piste sélectionnée
                        addTimelineObjectVue(obj, 1.0);
                    }
                }
            }
        });
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
        TimelineObject timelineObject = model.addTimelineObject(clip, timing, clip.getDuration().toMillis());

        // Obtenir un nombre aléatoire entre 0 et 255
        // TODO : a remplacer par un vrai nom
        int randomColor = (int) (Math.random() * 99);
        timelineObject.setName("Clip " + randomColor);

        addTimelineObjectVue(timelineObject, zoom);

        System.out.println(model);
    }

    /**
     * Ajout un TimelineObjectVue à la track
     * @param obj le modèle de l'object à ajouter
     */
    public void addTimelineObjectVue(TimelineObject obj, double zoom) {
        TimelineObjectVue mediaBlock = new TimelineObjectVue(obj,
                zoom,
                px_s,
                mediaPane.getHeight());

        mediaPane.getChildren().add(mediaBlock);

        makeDraggable(mediaBlock, obj);
    }


    public void addMediaButton(ActionEvent actionEvent) {
        Clip media = VideoClip.test();
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

    public Track getTrack() {
        return model;
    }

    /** Petit objet pour mémoriser l’état entre press/drag/release */
    private static class Delta {
        double startX, origLayoutX;
    }
}