package com;

import com.timeline.Track;
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

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
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
    private MenuButton trackChooser;

    @FXML
    private TextField searchBar;

    @FXML private TableView<Clip>      clipTable;
    @FXML private TableColumn<Clip, Image>  colThumbnail;
    @FXML private TableColumn<Clip, String> colName;
    @FXML private TableColumn<Clip, String> colDuration;
    @FXML private TableColumn<Clip, String> colResolution;
    @FXML private TableColumn<Clip, String> colSize;
    @FXML private TableColumn<Clip, String> colDate;
    @FXML private Button btnDelete;

    @FXML
    void search(ActionEvent event) {
        clipTable.getItems().clear();
        List<Clip> clips = searchList(searchBar.getText(), videoProject.getAllClips());
        clipTable.getItems().addAll(clips);
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

    private void ConfigureDeleteButton(){
        btnDelete.setDisable(true);
        clipTable.getSelectionModel().selectedItemProperty().addListener((obs, oldClip, newClip) -> {
            btnDelete.setDisable(newClip == null);
        });
        btnDelete.setOnAction(event -> deleteFile());
    }

    @FXML
    void deleteFile() {
        Clip selected = clipTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            videoProject.deleteVideo(selected);
            System.out.println("Supprimer le fichier");
        }
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

        ConfigureDeleteButton();

        trackChooser.setDisable(true);
        clipTable.getSelectionModel().selectedItemProperty().addListener((obs, oldClip, newClip) -> {
            trackChooser.setDisable(newClip == null);
        });

        ClipTableDragAndDrop clipTableDragAndDrop = new ClipTableDragAndDrop();

        clipTableDragAndDrop.enableDrag(clipTable);
    }

    /**
     * Ajoute une piste au menu de sélection.
     *
     * @param track La piste à ajouter
     */
    private void addTrackToMenu(Track track) {
        // Ajouter un button pour chaque piste
        MenuItem item = new MenuItem(track.getName());
        trackChooser.getItems().add(item);

        // Ajouter un listener pour ajouter le clip à la piste
        item.setOnAction(event -> {
            Clip selected = clipTable.getSelectionModel().getSelectedItem();
            track.addTimelineObjectAtEnd(selected);
        });

        System.out.println(track);
    }

    @FXML
    public void configureTrackChooser() {
        trackChooser.getItems().clear();
        videoProject.getTracks().forEach(this::addTrackToMenu);
        System.out.println("Mes maxis clipos : " + videoProject.getTracks());
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

        setupColumn(colName, Clip::getName);

        setupColumn(colDuration, clip -> formatDuration(clip.getDuration()));

        setupColumn(colResolution, clip ->
                clip.getWidth() + "x" + clip.getHeight()
        );

        setupColumn(colSize, clip ->
                formatSize(clip.getSizeBytes())
        );

        setupColumn(colDate, clip ->
                clip.getDateCreated()
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        );

        // Setup la colonne pour afficher la miniature
        colThumbnail.setCellValueFactory(cell -> {
            Image fx = SwingFXUtils.toFXImage(cell.getValue().getThumbnail(), null);
            return new ReadOnlyObjectWrapper<>(fx);
        });
        colThumbnail.setCellFactory(col -> new ImageTableCell<Clip>(100, 56));
    }

    /**
     * Configure une colonne de la table pour afficher une valeur formatée.
     *
     * @param column Colonne à configurer
     * @param mapper Fonction de mappage pour obtenir la valeur à afficher
     */
    private void setupColumn(TableColumn<Clip, String> column,
                             Function <Clip, String> mapper) {
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(mapper.apply(cell.getValue()))
        );
    }

    /**
     * Formate la taille en octets, Ko, Mo ou Go.
     *
     * @param bytes Taille en octets
     * @return Taille formatée
     */
    private String formatSize(long bytes) {
        String human;
        DecimalFormat df = new DecimalFormat("#.#");
        if (bytes < 1024) {
            human = bytes + " o";
        } else if (bytes < 1024*1024) {
            human = df.format(bytes/1024.0) + " Ko";
        } else if (bytes < 1024*1024*1024) {
            human = df.format(bytes/(1024.0*1024)) + " Mo";
        } else {
            human = df.format(bytes/(1024.0*1024*1024)) + " Go";
        }
        return human;
    }

    /**
     * Formate la durée en heures, minutes et secondes.
     *
     * @param d Durée à formater
     * @return Durée formatée
     */
    private String formatDuration(Duration d) {
        long h = d.toHours();
        long m = d.minusHours(h).toMinutes();
        long s = d.minusHours(h).minusMinutes(m).getSeconds();
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /**
     * Filtre la liste de clips en fonction des mots-clés fournis et des noms des Clips.
     *
     * @param searchWords   Mots-clés à rechercher
     * @param clips Liste de clips à filtrer
     * @return Liste filtrée contenant uniquement les éléments qui contiennent tous les mots-clés
     */
    private List<Clip> searchList(String searchWords, Collection<Clip> clips) {

        List<String> searchWordsArray = Arrays.asList(searchWords.trim().split(" "));

        return clips.stream().filter(input -> {
            return searchWordsArray.stream().allMatch(word ->
                    input.getName().toLowerCase().contains(word.toLowerCase()));
        }).collect(Collectors.toList());
    }
}
