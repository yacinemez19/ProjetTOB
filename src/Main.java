import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
                new TestVideoImporter()
        );

        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                getClass().getResource("ressources/views/Main.fxml")));
        Parent root = loader.load();

        // Injecter le modèle dans le contrôleur
        MainController mainController = loader.getController();
        mainController.setVideoProject(videoProject);
        System.out.println("On a set VideoProject dans le MainController");

        // Créer la scène avec le root obtenu
        Scene scene = new Scene(root);

        // Configurer et afficher le stage
        primaryStage.setTitle("Logiciel de montage vidéo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
