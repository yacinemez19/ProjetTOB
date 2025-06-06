package com.importation;

import com.Clip;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import java.awt.image.BufferedImage;

/**
 * Configure une TableView<com.Clip> pour qu’elle soit
 * source de drag & drop (export) vers un autre composant (com.timeline, etc.).
 */
public class ClipTableDragAndDrop {

    /** Format de données utilisé pour transporter l’URI du com.Clip. */
    private static final DataFormat CLIP_URI_FORMAT = DataFormat.PLAIN_TEXT;

    /**
     * Applique la configuration de drag & drop sur la TableView.
     * @param tableView la TableView à rendre draggable
     */
    public void enableDrag(TableView<Clip> tableView) {
        tableView.setRowFactory(this::createDraggableRow);
    }

    /**
     * Crée une TableRow configurée pour démarrer le drag.
     */
    private TableRow<Clip> createDraggableRow(TableView<Clip> tableView) {
        TableRow<Clip> row = new TableRow<>();
        row.setOnDragDetected(event -> startRowDrag(row, event));
        row.setOnDragDone(Event::consume);
        return row;
    }

    /**
     * Démarre le drag sur la ligne : on emballe l’URI et la miniature dans le Dragboard.
     */
    private void startRowDrag(TableRow<Clip> row, MouseEvent event) {
        Clip clip = row.getItem();
        if (clip == null) {
            return;
        }

        Dragboard dragboard = row.startDragAndDrop(TransferMode.COPY);
        dragboard.setContent(buildClipboardContent(clip));
        dragboard.setDragView(buildDragView(clip));
        event.consume();
    }

    /**
     * Prépare le contenu du clipboard (ici, l’URI du clip).
     */
    private ClipboardContent buildClipboardContent(Clip clip) {
        ClipboardContent content = new ClipboardContent();
        content.put(CLIP_URI_FORMAT, clip.getSource().toString());
        return content;
    }

    /**
     * Génère l’image à afficher sous le curseur (la miniature du clip).
     */
    private Image buildDragView(Clip clip) {
        BufferedImage thumb = clip.getThumbnail();
        return SwingFXUtils.toFXImage(thumb, null);
    }
}
