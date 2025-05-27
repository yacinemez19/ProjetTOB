package com;

import com.preview.PreviewController;
import com.preview.PreviewEngine;
import com.timeline.TimelineObject;
import com.timeline.Track;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.time.Duration;
import java.util.Objects;

/**
 * Class principale de l'application.
 * Utilise les fichiers FXML (dans src/ressources/views) pour créer l'interface
 * utilisateur.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws java.io.IOException {
        // Instancier le modèle
        VideoProject videoProject = new VideoProject(
                new ClipRegistry(),
                new TrackManager(),
                new GStreamerVideoImporter(),
                new PreviewEngine()

        );

        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                getClass().getResource("../ressources/views/Main.fxml")));
        Parent root = loader.load();


        // Injecter le modèle dans le contrôleur
        MainController mainController = loader.getController();
        mainController.setVideoProject(videoProject);
        System.out.println("On a set com.VideoProject dans le com.MainController");

        // Créer la scène avec le root obtenu
        Scene scene = new Scene(root);

        // Configurer et afficher le stage
        primaryStage.setTitle("Logiciel de montage vidéo");
        primaryStage.setScene(scene);
        primaryStage.show();

        /// /////////////////////
/*        // Petit scénario de test
        Track track = new Track();
        // on importe un fichier pour test
        File videoFile = new File("videos/nuuuuuul.MTS");
        // on ajoute dans le video project
        videoProject.importVideo(videoFile.toURI());
        videoProject.addTrack(track);
        Clip testClip = videoProject.getClip(videoFile.toURI());
        track.addTimelineObject(testClip, 0, 3000);

        File videoFile2 = new File("videos/ptitsdej.MXF");
        System.out.println("Path: " + videoFile2.getAbsolutePath());
        System.out.println("Exists: " + videoFile2.exists());
        videoProject.importVideo(videoFile2.toURI());
        Clip newTestClip = videoProject.getClip(videoFile2.toURI());
        track.addTimelineObject(newTestClip, 4000, 6000);

        PreviewEngine previewEngine = mainController.getPreviewEngine();
        previewEngine.playTrack(track);*/
        /// ////////////////////
    }
}
