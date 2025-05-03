import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Cellule personnalisée pour afficher une image dans une TableView.
 * @param <S> le type de l'objet contenu dans la TableView
 */
public class ImageTableCell<S> extends TableCell<S, Image> {

    private final ImageView iv = new ImageView();

    public ImageTableCell(double maxWidth, double maxHeight) {
        iv.setFitWidth(maxWidth);
        iv.setFitHeight(maxHeight);
        iv.setPreserveRatio(true);
    }

    @Override
    protected void updateItem(Image img, boolean empty) {
        super.updateItem(img, empty);
        if (empty || img == null) {
            setGraphic(null);
        } else {
            iv.setImage(img);
            setGraphic(iv);
        }
    }
}
