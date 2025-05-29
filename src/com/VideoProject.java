package com;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.importation.ClipRegistry;
import com.importation.VideoImporter;
import com.preview.PreviewEngine;
import com.timeline.Track;
import com.timeline.TrackManager;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import java.nio.file.Path;

/**
 * Représente un projet vidéo, source de vérité sur les clips importés et les pistes de la
 * com.timeline.
 */
public class VideoProject {
    private ClipRegistry clipRegistry;
    private TrackManager trackManager;
    private VideoImporter videoImporter;
    private PreviewEngine previewEngine;

    private String projectName;

    /**
     * Constructeur de la classe com.VideoProject.
     *
     * @param clipRegistry   Le registre des clips.
     * @param trackManager   Le gestionnaire de pistes.
     * @param videoImporter  L'importateur vidéo.
     */
    public VideoProject(ClipRegistry clipRegistry, TrackManager trackManager,
                        VideoImporter videoImporter, PreviewEngine previewEngine) {
        this(clipRegistry, trackManager, videoImporter, previewEngine, "Video Project");
    }

    /**
     * Constructeur de la classe com.VideoProject.
     *
     * @param clipRegistry   Le registre des clips.
     * @param trackManager   Le gestionnaire de pistes.
     * @param videoImporter  L'importateur vidéo.
     */
    public VideoProject(ClipRegistry clipRegistry, TrackManager trackManager,
                        VideoImporter videoImporter, PreviewEngine previewEngine, String projectName) {
        this.clipRegistry = clipRegistry;
        this.trackManager = trackManager;
        this.videoImporter = videoImporter;
        this.previewEngine = previewEngine;
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
     * Obtenir le gestionnaire de pistes.
     *
     * @return Le gestionnaire de pistes.
     */
    public TrackManager getTrackManager() {

        return trackManager;
    }

    /**
     * Obtenir la liste des tracks du projet
     *
     * @return liste des tracks
     */
    public List<Track> getTracks(){

        return this.getTrackManager().getTracks();
    }

    /**
     * Ajouter une track au projet vidéo.
     *
     * @param track La track à ajouter.
     */
    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("La track ne peut pas être nulle.");
        }
        if (getTracks().isEmpty()) {
            System.out.println("[Video Project] Add default track " + track.getName());
            this.getTrackManager().addTrack(track);
            previewEngine.setCurrentTrack(track);
        } else {
            this.getTrackManager().addTrack(track);
        }
    }

    /**
     * Obtenir une track à partir de son index.
     *
     * @param index
     * @return
     */
    public Track getTrack(int index) {
        return this.getTrackManager().getTrack(index);
    }

    /**
     * Obtenir la durée maximale de la com.timeline.
     *
     * @return La durée maximale de la com.timeline.
     */
    public long getMaxDuration() {
        return trackManager.getMaxDuration();
    }
    /**
     *
     * Obtenir le previewEngine associé
     */
    public PreviewEngine getPreviewEngine() {
        return previewEngine;
    }

    /**
     * Obtenir le nom du projet vidéo.
     *
     * @return Le nom du projet vidéo.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Construit une Map avec toutes les infos serialisables du projet vidéo.
     */
    private HashMap<String, Object> toMap() {
        HashMap<String, Object> data = new HashMap<>();

        data.put("name", projectName);

        // Ajouter les clips importés
        Collection<URI> clipURIs = clipRegistry.getAllClipURIs();
        data.put("clips", clipURIs.toString());

        //Ajouter les tracks

        //data.put("tracks", trackManager.toString());
        return data;
    }

    /**
     * Sauvegarder le projet vidéo sous forme de fichier JSON.
     *
     * @param filePath Le chemin du fichier où sauvegarder le projet.
     * @param contentToSave Le contenu à sauvegarder dans le fichier JSON.
     * @throws IOException Si une erreur d'entrée/sortie se produit.
     */
    public void makeJson(Path filePath,
                         HashMap<String, Object> contentToSave)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);

        objectMapper.writeValue(filePath.toFile(), toMap());
    }

    /**
     * Sauvegarder le projet vidéo sous forme d'un fichier.
     *
     * @param saveFilePath Le chemin du fichier où sauvegarder le projet.
     */
    public void saveProject(Path saveFilePath) {
        // TODO : Enlever le log
        System.out.println(toMap());

        try {
            makeJson(saveFilePath, toMap());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
