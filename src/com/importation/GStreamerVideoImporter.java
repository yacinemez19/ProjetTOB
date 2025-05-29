package com.importation;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import java.awt.image.BufferedImage;

import com.Clip;
import com.VideoClip;
import com.Utils;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.Format;

import java.awt.Graphics2D;

/**
 * Classe d'importation de clips utilisant GStreamer.
 *
 * Cette classe extrait une image de la vidéo.
 * Permet aussi de récupérer toutes les informations nécessaires
 * à l'instanciation d'un clip à partir d'un URI.
 */
public class GStreamerVideoImporter implements VideoImporter {

    private static Pipeline pipeline;

    /**
     * Importe une vidéo à partir d'un URI, extrait une miniature et retourne un clip.
     *
     * @param source URI de la vidéo à importer.
     * @return @return Un objet {@link Clip} représentant la vidéo importée.
     * @throws IllegalArgumentException si l'import échoue
     */
    @Override
    public Clip importVideo(URI source) throws IllegalArgumentException {

        // Configure native paths (cf. examples.BasicPipeline)
        Utils.configurePaths();

        // Initialiser GStreamer
        Gst.init(Version.BASELINE, "SnapshotPipeline", source.toString());

        // Construire le pipeline
        pipeline = buildPipeline(source.toString());

        // Mettre en place le bus
        setupBus(pipeline);

        // Mettre en pause la pipeline pour obtenir le preroll
        pausePipeline();

        // Obtenir la durée
        long duration = getDuration();

        // Déplacer le pipeline à une position représentative de 5% de la vidéo (ou 1s si durée inconnue)
        seekToPreviewFrame(duration);

        // Extraire l'échantillon à l'endroit où on s'est placé
        Sample sample = extractSample();

        // Lire caps pour connaître largeur/hauteur
        Structure s = sample.getCaps().getStructure(0);
        int width  = s.getInteger("width");
        int height = s.getInteger("height");

        // Transformer les données brutes du Sample en un BufferedImage
        BufferedImage thumbnail = convertSampleToImage(sample, width, height);

        // Redimensionner la miniature en fonction de la largeur pour qu'elle prenne moins de place
        thumbnail = resizeImage(thumbnail, 192);

        // Libération et sortie
        sample.getBuffer().unmap();
        sample.dispose();
        cleanup();

        // Créer un clip avec les données extraites
        return new VideoClip(source, Duration.ofNanos(duration), width, height, thumbnail);
    }

    /**
     * Construit le pipeline GStreamer pour lire la vidéo.
     * @param uri URI de la vidéo à importer.
     * @return Un objet {@link Pipeline} configuré pour lire la vidéo.
     */
    private static Pipeline buildPipeline(String uri) {

        // Construire le pipeline : uridecodebin → videoconvert → videoscale → appsink
        String caps = "video/x-raw,format=RGB,pixel-aspect-ratio=1/1";
        String launch = String.format(
                "uridecodebin uri=%s ! videoconvert ! videoscale ! appsink name=sink caps=\"%s\"",
                uri, caps
        );

        return (Pipeline) Gst.parseLaunch(launch);
    }

    /**
     * Configure le bus pour gérer les messages d'erreur et EOS.
     * @param pipeline Le pipeline GStreamer à configurer.
     */
    private static void setupBus(Pipeline pipeline) {
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
    }

    /**
     * Met le pipeline en pause pour obtenir le preroll.
     */
    private static void pausePipeline() {
        pipeline.setState(State.PAUSED);

        // Attendre la mise en preroll (optionnellement, on pourrait mieux faire avec un MainLoop)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Récupère la durée du pipeline.
     * @return La durée de la vidéo en nanosecondes.
     */
    private long getDuration() {
        return pipeline.queryDuration(Format.TIME);
    }

    /**
     * Déplace le pipeline à une position représentative de 5% de la vidéo ou 1 seconde si la durée est inconnue.
     * @param duration La durée de la vidéo en nanosecondes.
     */
    private void seekToPreviewFrame(long duration) {
        long position = (duration > 0)
                ? duration * 5 / 100
                : TimeUnit.SECONDS.toNanos(1);;
        pipeline.seekSimple(Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.KEY_UNIT),
                position
        );
    }

    /**
     * Récupère un échantillon vidéo (sample) à partir de l'élément AppSink.
     * @return Un objet {@link Sample} contenant l'échantillon vidéo.
     * @throws RuntimeException si l'échantillon est nul.
     */
    private Sample extractSample() {
        // Récupérer l'AppSink
        AppSink sink = (AppSink) pipeline.getElementByName("sink");

        Sample sample = sink.pullPreroll();

        if (sample == null) {
            System.err.println("Impossible de récupérer un échantillon vidéo.");
            cleanup();
            throw new RuntimeException("Échantillon nul");
        }
        else
            return sample;
    }

    /**
     * Convertit un échantillon vidéo en BufferedImage.
     * @param sample L'échantillon vidéo à convertir.
     * @param width  La largeur de l'image.
     * @param height La hauteur de l'image.
     * @return Un objet {@link BufferedImage} représentant l'échantillon vidéo.
     */
    private BufferedImage convertSampleToImage(Sample sample, int width, int height) {
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

        return img;
    }

    /**
     * Redimensionne une image à une largeur cible tout en conservant les proportions.
     * @param originalImage L'image d'origine à redimensionner.
     * @param targetWidth   La largeur cible de l'image redimensionnée.
     * @return Une nouvelle image redimensionnée.
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth) {
        // Calcul des proportions
        double ratio = (double) targetWidth / (double)originalImage.getWidth();
        int targetHeight = (int) Math.round(originalImage.getHeight() * ratio);

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }

    /**
     * Libère les ressources et quitte GStreamer.
     */
    private static void cleanup() {
        pipeline.setState(State.NULL);
        pipeline.dispose();
        Gst.quit();
    }

}