import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;

/**
 * Représente un projet vidéo, source de vérité sur les clips importés et les pistes de la
 * timeline.
 */
public class VideoProject {
    private ClipRegistry clipRegistry;
    private TrackManager trackManager;
    private VideoImporter videoImporter;

    private String projectName;

    /**
     * Constructeur de la classe VideoProject.
     *
     * @param clipRegistry   Le registre des clips.
     * @param trackManager   Le gestionnaire de pistes.
     * @param videoImporter  L'importateur vidéo.
     */
    public VideoProject(ClipRegistry clipRegistry, TrackManager trackManager,
                        VideoImporter videoImporter) {
        this.clipRegistry = clipRegistry;
        this.trackManager = trackManager;
        this.videoImporter = videoImporter;

        this.projectName = "Video Project";
    }

    /**
     * Constructeur de la classe VideoProject.
     *
     * @param clipRegistry   Le registre des clips.
     * @param trackManager   Le gestionnaire de pistes.
     * @param videoImporter  L'importateur vidéo.
     */
    public VideoProject(ClipRegistry clipRegistry, TrackManager trackManager,
                        VideoImporter videoImporter, String projectName) {
        this.clipRegistry = clipRegistry;
        this.trackManager = trackManager;
        this.videoImporter = videoImporter;

        this.projectName = projectName;
    }

    /**
     * Importe une vidéo à partir d'une URI et l'ajoute au registre des clips.
     *
     * @param source L'URI de la vidéo à importer.
     */
    public void importVideo(URI source) {
        Clip clip = videoImporter.importVideo(source);
        clipRegistry.register(clip);
    }

    /**
     * Supprime une vidéo du registre des clips.
     *
     * @param clip Le clip à supprimer.
     */
    public void deleteVideo(Clip clip) {
        clipRegistry.unregister(clip);
    }

    /**
     * Obtenir un clip à partir de son URI.
     *
     * @param uri URI de la clip recherché.
     * @return Le clip correspondant à l'URI.
     */
    public Clip getClip(URI uri) {
        return clipRegistry.getClip(uri);
    }

    /**
     * Obtenir tous les clips du registre.
     *
     * @return Une collection de tous les clips.
     */
    public Collection<Clip> getAllClips() {
        return clipRegistry.getAllClips();
    }

    /**
     * Obtenir le gestionnaire de pistes.
     *
     * @return Le gestionnaire de pistes.
     */
    public TrackManager getTrackManager() {
        return trackManager;
    }

    /**
     * Obtenir le registre des clips.
     *
     * @return Le registre des clips.
     */
    public ClipRegistry getClipRegistry() {
        return clipRegistry;
    }

    /**
     * Obtenir l'importateur vidéo.
     *
     * @return L'importateur vidéo.
     */
    public VideoImporter getVideoImporter() {
        return videoImporter;
    }

    /**
     * Obtenir la durée maximale de la timeline.
     *
     * @return La durée maximale de la timeline.
     */
    public Duration getMaxDuration() {
        return trackManager.getMaxDuration();
    }

    /**
     * Construit une Map avec toutes les infos serialisables du projet vidéo.
     */
    private HashMap<String, Object> toMap() {
        HashMap<String, Object> data = new HashMap<>();

        data.put("name", projectName);

        // Ajouter les clips importés
        Collection<Clip> clips = getAllClips();


        return null;
    }


    /**
     * Sauvegarder le projet vidéo sous forme d'un fichier.
     */
    public void saveProject() {

    }

}
