import javafx.application.Platform;
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

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class Preview {

    @FXML
    private ImageView videoView;

    private PlayBin pipeline;
    private FXImageSink fxSink;

    @FXML
    private void handlePlay() {
        pipeline.play();
    }

    @FXML
    private void handleReverse() {
        pipeline.play();
        // Seek avec une vitesse négative : -1.0 pour vitesse normale à l'envers
        boolean result = pipeline.seek(
                -1.0, // rate négatif = lecture en reverse
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, pipeline.queryPosition(Format.TIME),
                SeekType.NONE, -1
        );

        if (!result) {
            System.out.println("Seek reverse failed");
        }
    }
    @FXML
    private void handlePause() {
        pipeline.pause();
    }

    @FXML
    private void handleLastFrame() {
        pipeline.pause();
        long currentPosition = pipeline.queryPosition(Format.TIME);
        long frameDuration = (long)(1_000_000_000 / 25.0); // 25 fps → 40ms par frame
        long newPosition = Math.max(0, currentPosition - frameDuration);

        boolean result = pipeline.seek(
                1.0, // normal rate
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, newPosition,
                SeekType.NONE, -1
        );

        if (!result) {
            System.out.println("Previous frame seek failed");
        }
    }

    @FXML
    private void handleNextFrame() {
        pipeline.pause();
        long currentPosition = pipeline.queryPosition(Format.TIME);
        long frameDuration = (long)(1_000_000_000 / 25.0); // 40ms
        long newPosition = currentPosition + frameDuration;

        boolean result = pipeline.seek(
                1.0,
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, newPosition,
                SeekType.NONE, -1
        );

        if (!result) {
            System.out.println("Next frame seek failed");
        }

    }

    @FXML
    public void initialize() {
        /**
         * Set up paths to native GStreamer libraries - see adjacent file.
         */
        Utils.configurePaths();

        /**
         * Initialize GStreamer. Always pass the lowest version you require -
         * Version.BASELINE is GStreamer 1.8. Use Version.of() for higher.
         * Features requiring later versions of GStreamer than passed here will
         * throw an exception in the bindings even if the actual native library
         * is a higher version.
         */
        Gst.init(Version.BASELINE, "BasicPipeline");

        /**
         * Use Gst.parseLaunch() to create a pipeline from a GStreamer string
         * definition. This method returns Pipeline when more than one element
         * is specified.
         */
        //pipeline = new PlayBin("playbin", "https://gstreamer.freedesktop.org/data/media/sintel_trailer-480p.webm");
        pipeline = (PlayBin) Gst.parseLaunch("playbin uri=https://gstreamer.freedesktop.org/data/media/sintel_trailer-480p.webm");
        fxSink = new FXImageSink();
        pipeline.setVideoSink(fxSink.getSinkElement());
        videoView.imageProperty().bind(fxSink.imageProperty());

        // loop on EOS if button selected
        pipeline.getBus().connect((Bus.EOS) source -> {
            // handle on event thread!
                    pipeline.stop();
        });

        /**
         * GStreamer native threads will not be taken into account by the JVM
         * when deciding whether to shutdown, so we have to keep the main thread
         * alive. Gst.main() will keep the calling thread alive until Gst.quit()
         * is called. Here we use the built-in executor to schedule a quit after
         * 10 seconds.
         */
        Gst.getExecutor().schedule(Gst::quit, 10, TimeUnit.SECONDS);
        Gst.main();
    }
}
