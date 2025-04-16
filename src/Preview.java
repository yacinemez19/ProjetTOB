import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.event.SeekType;
import org.freedesktop.gstreamer.fx.FXImageSink;
import org.freedesktop.gstreamer.message.Message;

public class Preview {

    @FXML
    private ImageView videoView;

    private Pipeline pipeline;
    private FXImageSink fxSink;

    @FXML
    private void handlePlay() {
        pipeline.play();
    }

    @FXML
    private void handlePause() {
        pipeline.pause();
    }

    @FXML
    private void handleNextFrame() {
        pipeline.pause();
        //pipeline.seek(Format.TIME, SeekFlags.FLUSH, SeekType.SET, pipeline.queryPosition(Format.TIME) + 40000000); // 40ms avance 1 frame à 25fps
    }
}
