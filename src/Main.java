import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws java. io. IOException {
        // Charger le fichier FXML
        Parent root = null;
        root = FXMLLoader.load(getClass().getResource("ressources/views/Main.fxml"));

        // Créer la scène avec le root obtenu
        Scene scene = new Scene(root);

        // Configurer et afficher le stage
        primaryStage.setTitle("Exemple FXML");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
