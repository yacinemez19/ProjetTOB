import Track;
import TimelineObject;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VideoProject est la source de vérité pour votre projet de montage :
 * il gère la collection de tracks et l’ajout de clips sur ces pistes.
 */
public class VideoProject {

    // Singleton si vous voulez un accès global
    private static final VideoProject INSTANCE = new VideoProject();
    public static VideoProject getInstance() {
        return INSTANCE;
    }

    private final TrackManager trackManager = new TrackManager();

    private VideoProject() {
        // Ajoutez par défaut une piste vidéo
        trackManager.addTrack(new Track("Vidéos"));
    }

    /**
     * Ajoute une nouvelle piste au projet.
     */
    public void addTrack(Track track) {
        trackManager.addTrack(track);
    }

    /**
     * Renvoie la liste des pistes.
     */
    public List<Track> getTracks() {
        return trackManager.getTracks();
    }

    /**
     * Ajoute un clip à la piste d’index donné.
     * Crée un TimelineObject avec l'intégralité du clip (inPoint = 0, outPoint = duration).
     */
    public void addClip(Clip clip, int trackIndex) {
        if (trackIndex < 0 || trackIndex >= trackManager.size()) {
            throw new IndexOutOfBoundsException("Track index hors limites : " + trackIndex);
        }
        TimelineObject obj = new TimelineObject(
                clip,
                Duration.ZERO,
                Duration.ZERO,
                clip.getDuration()
        );
        trackManager.getTracks().get(trackIndex).addItem(obj);
    }

    /**
     * Renvoie tous les clips (dans l'ordre des pistes puis des objets de timeline).
     */
    public List<Clip> getAllClips() {
        return trackManager.getTracks().stream()
                .flatMap(track -> track.getItems().stream())
                .map(TimelineObject::getClip)
                .collect(Collectors.toList());
    }
}
