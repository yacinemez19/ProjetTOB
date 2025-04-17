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

    private Pipeline pipeline;
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
         * ON cook ici
        Element source = ElementFactory.make("videotestsrc", "source");
        source.set("pattern", 18);
         */
        Element source = ElementFactory.make("uridecodebin", "source");
        // Audio Sink
        Element converter = ElementFactory.make("audioconvert", "audioconverter");
        Element resample = ElementFactory.make("audioresample", "audiosample");
        Element audioSink = ElementFactory.make("autoaudiosink", "audiosink");
        // Video Sink
        Element videoConverter = ElementFactory.make("videoconvert", "videoconverter");
        fxSink = new FXImageSink();
        // On créé une pipelin vide et on lui ajoute tout les éléments
        pipeline = new Pipeline("testPipeline");
        pipeline.add(source);
        pipeline.add(converter);
        pipeline.add(resample);
        pipeline.add(audioSink);
        pipeline.add(videoConverter);
        pipeline.add(fxSink.getSinkElement());
        // On link l'audio
        converter.link(resample);
        resample.link(audioSink);
        // On link la vidéo
        videoConverter.link(fxSink.getSinkElement());
        videoView.imageProperty().bind(fxSink.imageProperty());
        // On set la source et on ajoute le pad-added signal
        source.set("uri", "https://gstreamer.freedesktop.org/data/media/sintel_trailer-480p.webm");
        source.connect(
                new Element.PAD_ADDED() {
                    @Override
                    public void padAdded(Element element, Pad pad) {
                        Pad audioSinkPad = converter.getSinkPads().getFirst();
                        Pad videoSinkPad = videoConverter.getSinkPads().getFirst();

                        System.out.println("New pad received " + pad.getName() + " from " + element.getName());

                        // On récupère le type de fichier
                        Caps caps = pad.getCurrentCaps();
                        String type = caps.getStructure(0).getName();
                        System.out.println(type);

                        if (!audioSinkPad.isLinked() && type.equals("audio/x-raw")) {
                            System.out.println("Audio Sink Pad not linked");
                            pad.link(audioSinkPad);
                        }
                        if (!videoSinkPad.isLinked() && type.equals("video/x-raw")) {
                            System.out.println("Video Sink Pad not linked");
                            pad.link(videoSinkPad);
                        }
                    }
                }
        );
        pipeline.setState(State.PAUSED);
        // attendre que les pads soient liés
        try {Thread.sleep(1500);}
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("on lance la pipeline");
        //pipeline.setState(State.PLAYING);
/*        pipeline.getBus().connect((Bus.ASYNC_DONE) bus -> {
            System.out.println("Pipeline ready — now playing.");
            pipeline.play();
        });*/

/*        // loop on EOS if button selected
        pipeline.getBus().connect((Bus.EOS) source1 -> {
            // handle on event thread!
                    pipeline.stop();
        });*/

        /**
         * GStreamer native threads will not be taken into account by the JVM
         * when deciding whether to shutdown, so we have to keep the main thread
         * alive. Gst.main() will keep the calling thread alive until Gst.quit()
         * is called. Here we use the built-in executor to schedule a quit after
         * 10 seconds.
         */
        //Gst.getExecutor().schedule(Gst::quit, 10, TimeUnit.SECONDS);
        //Gst.main();
    }
}
