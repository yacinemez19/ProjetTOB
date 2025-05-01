import java.net.URI;
import java.util.Collection;

/**
 * Représente un projet vidéo, source de vérité sur les clips importés et les pistes de la
 * timeline.
 */
public class VideoProject {
    private ClipRegistry clipRegistry;
    private TrackManager trackManager;
    private VideoImporter videoImporter;

    public VideoProject(ClipRegistry clipRegistry, TrackManager trackManager,
                        VideoImporter videoImporter) {
        this.clipRegistry = clipRegistry;
        this.trackManager = trackManager;
        this.videoImporter = videoImporter;
    }

    public void importVideo(URI source) {
        Clip clip = videoImporter.importVideo(source);
        clipRegistry.register(clip);
    }

    public Clip getClip(URI uri) {
        return clipRegistry.getClip(uri);
    }

    public Collection<Clip> getAllClips() {
        return clipRegistry.getAllClips();
    }
}
