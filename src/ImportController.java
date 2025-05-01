import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.image.WritableImage;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ImportController implements Initializable {

    FileChooser fileChooser;

    ArrayList<String> words = new ArrayList<>(
            Arrays.asList("test", "dog","Human", "Days of our life", "The best day",
                    "Friends", "Animal", "Human", "Humans", "Bear", "Life",
                    "This is some text", "Words", "222", "Bird", "Dog", "A few words",
                    "Subscribe!", "SoftwareEngineeringStudent", "You got this!!",
                    "Super Human", "Super", "Like")
    );

    @FXML
    private TextField searchBar;

    @FXML
    private ListView<String> listView;

    @FXML
    void search(ActionEvent event) {
        listView.getItems().clear();
        listView.getItems().addAll(searchList(searchBar.getText(),words));
    }

    @FXML
    void addFile(ActionEvent event) {
        Window window = listView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            // traiter le fichier…
        }

    }

    @FXML
    void deleteFile(ActionEvent event) {
        System.out.println("Supprimer le fichier");
    }

    /**
     * Initialise le contrôleur. Cette méthode est appelée après que le fichier
     * FXML a été chargé.
     *
     * @param url            URL de la ressource
     * @param resourceBundle Bundle de ressources
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listView.getItems().addAll(words);
        configureListViewForDragAndDrop();
        ConfigureFileChooser();
    }

    private void ConfigureFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        // Uniquement les vidéos pour l'instant, audio à ajouter plus tard
        fileChooser.getExtensionFilters().add(
                     new FileChooser.ExtensionFilter("Vidéos", "*.asf", "*.avi",
                             "*.flv", "*.mkv", "*.mov", "*.mp4", "*.mpeg", "*.mpg",
                             "*.wmv", "*.wma", "*.webm", "*.ogg", "*.ogv", "*.3gp", "*.3g2",
                             "*.flv", "*.ts", "*.mxf", "*.oga", "*.wav" )
                 );
        fileChooser.setTitle("Importer un fichier vidéo");
    }

    /**
     * Configure le ListView pour permettre le drag and drop.
     */
    private void configureListViewForDragAndDrop() {
        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };

            // Détection du début du drag
            cell.setOnDragDetected(evt -> {
                if (cell.getItem() == null) {
                    return;
                }
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(cell.getItem());
                db.setContent(content);

                // Snapshot pour l’animation du curseur
                SnapshotParameters params = new SnapshotParameters();
                WritableImage snapshot = cell.snapshot(params, null);
                db.setDragView(snapshot, snapshot.getWidth() / 2, snapshot.getHeight() / 2);

                evt.consume();
            });

            // On peut intercepter la fin du drag si besoin (ici on ignore)
            cell.setOnDragDone(DragEvent::consume);

            return cell;
        });
    }

    /**
     * Filtre la liste de chaînes de caractères en fonction des mots-clés fournis.
     *
     * @param searchWords   Mots-clés à rechercher
     * @param listOfStrings Liste de chaînes de caractères à filtrer
     * @return Liste filtrée contenant uniquement les éléments qui contiennent tous les mots-clés
     */
    private List<String> searchList(String searchWords, List<String> listOfStrings) {

        List<String> searchWordsArray = Arrays.asList(searchWords.trim().split(" "));

        return listOfStrings.stream().filter(input -> {
            return searchWordsArray.stream().allMatch(word ->
                    input.toLowerCase().contains(word.toLowerCase()));
        }).collect(Collectors.toList());
    }
}