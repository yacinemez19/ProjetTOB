import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

public class MainController {

    @FXML
    private AnchorPane previewPanel;


    private PreviewController previewController;

    public void initialize() {
        try {
            // Charger Preview.fxml dynamiquement
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ressources/views/Preview.fxml"));
            AnchorPane previewContent = loader.load();
            previewController = loader.getController();
            previewPanel.getChildren().add(previewContent);

            // Configurer les raccourcis une fois que la scène est prête
            Scene scene = previewPanel.getScene();
            if (scene != null) {
                setupKeyboardShortcuts(scene);
            } else {
                // Si la scène n'est pas encore disponible, attendre un peu
                previewPanel.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        setupKeyboardShortcuts(newScene);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.SPACE),
                () -> previewController.togglePlayPause()
        );
    }
}
