import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;

import java.net.URI;
import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.time.Duration;
import javax.imageio.ImageIO;



public class GStreamerVideoImporter implements VideoImporter {

    @Override
    public Clip importVideo(URI source) {

        // Paramètres d'un ImportedClip
        Duration duration;
        int width;
        int height;
        BufferedImage thumbnail;

        // Initialisation GStreamer
        Gst.init("VideoMetadataExample");

        // Pipeline avec format libre (pas de caps)
        String launchDesc = String.format(
                "uridecodebin uri=%s ! videoconvert ! videoscale ! appsink name=sink",
                source.getPath()
        );
        Pipeline pipeline = (Pipeline) Gst.parseLaunch(launchDesc);
        AppSink sink = (AppSink) pipeline.getElementByName("sink");

        // Pré-roll (pause pour décoder sans réellement jouer le fichier)
        StateChangeReturn ret = pipeline.setState(State.PAUSED);
        if (ret == StateChangeReturn.FAILURE) {
            throw new RuntimeException("Impossible de preroller");
        }


        // Lire une frame
        Sample sample = sink.pullSample();
        if (sample == null) {
            throw new RuntimeException("Impossible de récupérer une frame de la vidéo");
        }

        Structure capsStruct = sample.getCaps().getStructure(0);
        width = capsStruct.getInteger("width");
        height = capsStruct.getInteger("height");

        // Durée de la vidéo
        long[] durationArr = new long[1];
        long durationNs = pipeline.queryDuration(Format.TIME);
        duration = Duration.ofNanos(durationNs);

        // Miniature
        Buffer buffer = sample.getBuffer();
        ByteBuffer data = buffer.map(false);
        thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * 3;
                int r = data.get(i)   & 0xFF;
                int g = data.get(i+1) & 0xFF;
                int b = data.get(i+2) & 0xFF;
                int rgb = (r << 16) | (g << 8) | b;
                thumbnail.setRGB(x, y, rgb);
            }
        }
        buffer.unmap();
        try {
            ImageIO.write(thumbnail, "png", new File("snapshot.png"));      // sauvegarde de la miniature
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Nettoyage
        pipeline.setState(State.NULL);
        sample.dispose();  // Libérer la mémoire native


        return new ImportedClip(source, duration, width, height, thumbnail);
    }
}
