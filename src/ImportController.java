import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class ImportController implements Initializable {

    FileChooser fileChooser;

    // Le projet vidéo associé
    VideoProject videoProject;

    @FXML
    private TextField searchBar;

    @FXML private TableView<Clip>      clipTable;
    @FXML private TableColumn<Clip, Image>  colThumbnail;
    @FXML private TableColumn<Clip, String> colName;
    @FXML private TableColumn<Clip, String> colDuration;
    @FXML private TableColumn<Clip, String> colResolution;
    @FXML private TableColumn<Clip, String> colSize;
    @FXML private TableColumn<Clip, String> colDate;

    @FXML private ListView<String>      listView;

    @FXML
    void search(ActionEvent event) {
        // TODO : implémenter la recherche
        System.out.println("Recherche...");
    }

    @FXML
    void addFile(ActionEvent event) {
        Window window = clipTable.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            videoProject.importVideo(file.toURI());
        }
        updateClipList();
    }

    @FXML
    void deleteFile(ActionEvent event) {
        // TODO : implémenter la suppression
        System.out.println("Supprimer le fichier");
        updateClipList();
    }

    /**
     * Setter pour le projet vidéo.
     *
     * @param videoProject Le projet vidéo à définir
     */
    public void setVideoProject(VideoProject videoProject) {
        this.videoProject = videoProject;

        // Mettre à jour la liste des clips affichée dans le tableau dès qu'on a un projet
        // vidéo pour éviter d'avoir un problème de null pointer.
        updateClipList();
    }

    /**
     * Met à jour la liste des clips affichée dans le tableau.
     */
    public void updateClipList() {
        clipTable.getItems().clear();
        clipTable.getItems().addAll(videoProject.getAllClips());
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
        ConfigureClipTable();

        ConfigureFileChooser();

        ClipTableDragAndDrop clipTableDragAndDrop = new ClipTableDragAndDrop();

        clipTableDragAndDrop.enableDrag(clipTable);
    }

    private void ConfigureFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        // Uniquement les vidéos pour l'instant, audio à ajouter plus tard
        /*fileChooser.getExtensionFilters().add(
                     new FileChooser.ExtensionFilter("Vidéos", "*.asf", "*.avi",
                             "*.flv", "*.mkv", "*.mov", "*.mp4", "*.mpeg", "*.mpg",
                             "*.wmv", "*.wma", "*.webm", "*.ogg", "*.ogv", "*.3gp", "*.3g2",
                             "*.flv", "*.ts", "*.mxf", "*.oga", "*.wav" )
                 );*/
        fileChooser.setTitle("Importer un fichier vidéo");
    }

    /**
     * Configure le tableClip pour afficher convenablement les données.
     */
    private void ConfigureClipTable(){
        colName.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getName())
        );
        colDuration.setCellValueFactory(cell -> {
            Duration d = cell.getValue().getDuration();
            long h = d.toHours();
            long m = d.minusHours(h).toMinutes();
            long s = d.minusHours(h).minusMinutes(m).getSeconds();
            String formatted = String.format("%02d:%02d:%02d", h, m, s);
            return new ReadOnlyStringWrapper(formatted);
        });

        // 3. Résolution
        colResolution.setCellValueFactory(cell -> {
            Clip clip = cell.getValue();
            String res = clip.getWidth() + "×" + clip.getHeight();
            return new ReadOnlyStringWrapper(res);
        });

        // 4. Taille formatée
        colSize.setCellValueFactory(cell -> {
            long bytes = cell.getValue().getSizeBytes();
            String human;
            DecimalFormat df = new DecimalFormat("#.#");
            if (bytes < 1024) {
                human = bytes + " o";
            } else if (bytes < 1024*1024) {
                human = df.format(bytes/1024.0) + " Ko";
            } else {
                human = df.format(bytes/(1024.0*1024)) + " Mo";
            }
            return new ReadOnlyStringWrapper(human);
        });

        // 6. Date (format court local)
        colDate.setCellValueFactory(cell -> {
            String d = cell.getValue()
                    .getDateCreated()
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
            return new ReadOnlyStringWrapper(d);
        });

        // 7. Miniature : conversion BufferedImage → Image & inline cellFactory
        colThumbnail.setCellValueFactory(cell -> {
            Image fx = SwingFXUtils.toFXImage(cell.getValue().getThumbnail(), null);
            return new ReadOnlyObjectWrapper<>(fx);
        });
        colThumbnail.setCellFactory(col -> new TableCell<>() {
                    private final ImageView iv = new ImageView();
                    {
                        iv.setFitWidth(100);
                        iv.setFitHeight(56);
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