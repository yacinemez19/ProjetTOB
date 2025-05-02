import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.Format;

/**
 * Classe d'importation de clips utilisant GStreamer.
 * Cette classe extrait une image de la vidéo et la sauvegarde en tant que fichier PNG
 * (uniquement pour les tests sera enlevé plus tard), permet aussi de récupérer toutes les
 * informations nécessaires à l'instanciation d'un clip à partir d'un URI.
 */
public class GStreamerVideoImporter implements VideoImporter {

    private static Pipeline pipeline;

    @Override
    public Clip importVideo(URI source) throws IllegalArgumentException {
        String uri = source.toString();

        // Configure native paths (cf. BasicPipeline)
        Utils.configurePaths();

        // Initialise GStreamer
        Gst.init(Version.BASELINE, "SnapshotPipeline", source.toString());

        // Construire le pipeline : uridecodebin → videoconvert → videoscale → appsink
        String caps = "video/x-raw,format=RGB,width=160,pixel-aspect-ratio=1/1";
        String launch = String.format(
                "uridecodebin uri=%s ! videoconvert ! videoscale ! appsink name=sink caps=\"%s\"",
                uri, caps
        );
        pipeline = (Pipeline) Gst.parseLaunch(launch);

        // Récupérer l'AppSink
        AppSink sink = (AppSink) pipeline.getElementByName("sink");

        // Gérer les messages d'erreur / EOS
        Bus bus = pipeline.getBus();
        bus.connect((Bus.ERROR) (sources, b,  m) -> {
            System.err.println("Erreur GStreamer : " + m);
            Gst.quit();
        });
        bus.connect((Bus.EOS) (sources) -> {
            System.out.println("Fin du flux vidéo.");
            Gst.quit();
        });

        // Mettre en PAUSED pour obtenir le preroll
        pipeline.setState(State.PAUSED);
        // Attendre la mise en preroll (optionnellement, on pourrait mieux faire avec un MainLoop)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Obtenir la durée et chercher à 5% (ou à 1 s si durée inconnue)
        long duration = pipeline.queryDuration(Format.TIME);
        long position = (duration > 0)
                ? duration * 5 / 100
                : TimeUnit.SECONDS.toNanos(1);;
        pipeline.seekSimple(Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.KEY_UNIT),
                position
        );

        // Extraire le sample (bloquant jusqu'au preroll)
        Sample sample = sink.pullPreroll();
        if (sample == null) {
            System.err.println("Impossible de récupérer un échantillon vidéo.");
            cleanup();
            throw new RuntimeException("Échantillon nul");
        }

        // Lire caps pour connaître largeur/hauteur
        Structure s = sample.getCaps().getStructure(0);
        int width  = s.getInteger("width");
        int height = s.getInteger("height");

        // Accéder au buffer et mapper les données
        Buffer buffer = sample.getBuffer();

        ByteBuffer data = buffer.map(false);
        if (data == null) {
            System.err.println("Impossible de récupérer la data.");
            cleanup();
            throw new RuntimeException("data nul");
        }

        // Décomposer en pixels RGB et construire un BufferedImage
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int r = data.get() & 0xff;
            int g = data.get() & 0xff;
            int b = data.get() & 0xff;
            pixels[i] = (0xff << 24) | (r << 16) | (g << 8) | b;
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, width, height, pixels, 0, width);

        // Sauver en PNG uniquement pour les tests à enlever plus tard
        try {
            ImageIO.write(img, "png", new File("snapshot.png"));
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture de la miniature : " + e.getMessage());
            throw new RuntimeException(e);
        }
        System.out.println("snapshot.png généré.");

        // Libération et sortie
        buffer.unmap();
        sample.dispose();
        cleanup();

        // Créer un clip avec les données extraites
        // TODO : récupérer la taille du fichier source
        // TODO : récupérer le type du fichier source
        // TODO : récupérer la date de création du fichier source
        // TODO : récupérer la longueur et la largeur du fichier source (ici c'est celui
        //  de la thumbnail qui est utilisé)
        // TODO : rendre le code plus propre en séparant en plusieurs méthodes
        return new ImportedClip(source, Duration.ofNanos(duration), width, height, img,
                1_000, "MP4", LocalDate.now());
    }

    private static void cleanup() {
        pipeline.setState(State.NULL);
        pipeline.dispose();
        Gst.quit();
    }
}