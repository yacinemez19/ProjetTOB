package examples;

import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

public class VideoMetadataExample {
    private static final String CAPS = "video/x-raw,format=RGB,width=160,pixel-aspect-ratio=1/1";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java examples.VideoMetadataExample <uri or filepath>");
            System.exit(-1);
        }
        String uri = args[0];
        Path filepath = Path.of(new java.net.URI(uri)); // ou Paths.get(uri) si chemin local
        File file = filepath.toFile();

        // --- 1. Initialisation GStreamer ---
        Gst.init("examples.VideoMetadataExample", args);

        // --- 2. Construction du pipeline ---
        String launchDesc = String.format(
                "uridecodebin uri=%s ! videoconvert ! videoscale ! appsink name=sink caps=\"%s\"",
                uri, CAPS
        );
        Pipeline pipeline = (Pipeline) Gst.parseLaunch(launchDesc);
        AppSink sink = (AppSink) pipeline.getElementByName("sink");

        // --- 3. Lecture prérégler (preroll) ---
        pipeline.setState(State.PAUSED);


        // --- 4. Durée de la vidéo ---
        long duration = pipeline.queryDuration(Format.TIME);
        double durationSec = duration / 1e9;                          // en secondes

        // --- 5. Dimensions (width/height) et miniature (thumbnail) ---
        Sample sample = sink.pullSample();
        Structure capsStruct = sample.getCaps().getStructure(0);
        int width  = capsStruct.getInteger("width");
        int height = capsStruct.getInteger("height");

        Buffer buffer = sample.getBuffer();
        ByteBuffer data = buffer.map(false);
        BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
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
        ImageIO.write(thumbnail, "png", new File("snapshot.png"));      // sauvegarde de la miniature

        // --- 6. Taille du fichier en octets ---
        long sizeBytes = file.length();

        // --- 7. Type / extension ou codec ---
        //  • Pour l’extension :
        String extension = "";
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) extension = name.substring(dot + 1).toUpperCase();
        //  • Pour le codec conteneur, on peut interroger le caps de uridecodebin :
        //    ici on se contente de l’extension, mais on pourrait creuser via decodebin.getSinkCaps()

        // --- 8. Date de création (import) du fichier local ---
        BasicFileAttributes attrs = Files.readAttributes(filepath, BasicFileAttributes.class);
        Instant dateCreated = attrs.creationTime().toInstant();

        // --- 9. Affichage des résultats ---
        System.out.println("Duration        : " + String.format("%.2f s", durationSec));
        System.out.println("Width x Height  : " + width + " x " + height + " px");
        System.out.println("Thumbnail       : snapshot.png");
        System.out.println("Size (bytes)    : " + sizeBytes);
        System.out.println("Extension/Type  : " + extension);
        System.out.println("Date Created    : " + dateCreated);

        // --- 10. Cleanup ---
        pipeline.setState(State.NULL);
        Gst.deinit();
    }
}
