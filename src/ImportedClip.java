import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Implémentation de l'interface Clip pour représenter un clip importé.
 */
public class ImportedClip implements Clip {

    private final URI source;
    private final Duration duration;
    private final int width;
    private final int height;
    private final BufferedImage thumbnail;
    private final long sizeBytes;
    private final String type;
    private final LocalDate dateCreated;

    /**
     * Construit un ImportedClip.
     *
     * @param source       URI du fichier source
     * @param duration     Durée de la vidéo
     * @param width        Largeur en pixels
     * @param height       Hauteur en pixels
     * @param thumbnail    Miniature générée
     * @param sizeBytes    Taille du fichier en octets
     * @param type         Type / extension ou codec (ex. "MP4")
     * @param dateCreated  Date d'import ou de création du clip
     */
    public ImportedClip(URI source,
                        Duration duration,
                        int width,
                        int height,
                        BufferedImage thumbnail,
                        long sizeBytes,
                        String type,
                        LocalDate dateCreated) {
        this.source      = Objects.requireNonNull(source, "source ne peut être null");
        this.duration    = Objects.requireNonNull(duration, "duration ne peut être null");
        this.width       = width;
        this.height      = height;
        this.thumbnail   = Objects.requireNonNull(thumbnail, "thumbnail ne peut être null");
        this.sizeBytes   = sizeBytes;
        this.type        = Objects.requireNonNull(type, "type ne peut être null");
        this.dateCreated = Objects.requireNonNull(dateCreated, "dateCreated ne peut être null");
    }

    @Override
    public URI getSource() {
        return source;
    }

    @Override
    public String getName() {
        String path = source.getPath();
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public BufferedImage getThumbnail() {
        return thumbnail;
    }

    @Override
    public long getSizeBytes() {
        return sizeBytes;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public LocalDate getDateCreated() {
        return dateCreated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImportedClip)) return false;
        ImportedClip that = (ImportedClip) o;
        return width == that.width &&
                height == that.height &&
                sizeBytes == that.sizeBytes &&
                source.equals(that.source) &&
                duration.equals(that.duration) &&
                thumbnail.equals(that.thumbnail) &&
                type.equals(that.type) &&
                dateCreated.equals(that.dateCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, duration, width, height, thumbnail, sizeBytes, type, dateCreated);
    }

    @Override
    public String toString() {
        return "ImportedClip{" +
                "name=" + getName() +
                ", duration=" + duration +
                ", resolution=" + width + "x" + height +
                ", size=" + getSizeLabel() +
                ", type=" + type +
                ", date=" + dateCreated +
                '}';
    }
}
