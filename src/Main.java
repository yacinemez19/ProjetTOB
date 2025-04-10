import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.setTitle("Montage 2000");
        primaryStage.show();
    }
}
