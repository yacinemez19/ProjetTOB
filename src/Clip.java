import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;

/**
 * Représente un clip vidéo importé dans le projet.
 */
public interface Clip {

    /**
     * Chemin ou URI de la source vidéo.
     */
    URI getSource();

    /**
     * Nom du fichier (ex. "ma-sequence.mp4").
     */
    String getName();

    /**
     * Durée totale du clip.
     */
    Duration getDuration();

    /**
     * Largeur (en pixels) de la vidéo.
     */
    int getWidth();

    /**
     * Hauteur (en pixels) de la vidéo.
     */
    int getHeight();

    /**
     * Miniature (thumbnail) générée pour affichage.
     */
    BufferedImage getThumbnail();

    /**
     * Taille du fichier en octets.
     */
    long getSizeBytes();

    /**
     * Type / extension ou codec du fichier (ex. "MP4", "MOV").
     */
    String getType();

    /**
     * Date de création ou d'import du clip.
     */
    LocalDate getDateCreated();

    // Méthode utilitaire par défaut pour formater la taille en Mo/Ko
    default String getSizeLabel() {
        long bytes = getSizeBytes();
        if (bytes < 1_024) return bytes + " o";
        long kb = bytes / 1_024;
        if (kb < 1_024) return kb + " Ko";
        double mb = bytes / 1_024d / 1_024d;
        return String.format("%.1f Mo", mb);
    }
}
