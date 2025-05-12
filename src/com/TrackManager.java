package com;

import com.timeline.Track;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TrackManager {

    private ObservableList<Track> tracks;


    TrackManager() {
        tracks = new ArrayList<>();
    }

    /**
     * Constructeur de la classe TrackManager
     */
    public TrackManager() {
        this.tracks = FXCollections.observableArrayList();
    }

    /**
     * Getter liste des tracks
     * @return tracks
     */
    public ObservableList<Track> getTracks() {
        return this.tracks;
    }

    /**
     * Ajoute une track
     * @param track à ajouter
     */
    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track ne peut pas être nulle");
        }
        this.tracks.add(track);
    }

    /**
     * supprime la track de la liste
     * @param track
     */
    public void removeTrack(Track track) {
        if (this.tracks.contains(track)) {
            this.tracks.remove(track);
        }else {
            System.out.println("La track n'existe pas");
        }

    }

    /**
     * Fonction retournant la track à l'index donné
     *
     * @param index
     */
    public Track getTrack(int index) {
        if (index >= 0 && index < this.tracks.size()) {
            return this.tracks.get(index);
        } else {
            throw new IndexOutOfBoundsException("Index de la track hors limites : " + index);
        }
    }

    public void moveTrack(Track track) {
        // à voir si on garde ou pas
        System.out.println("move track");
    }

    /**
     * Fonction retournant la taille de la liste des tracks
     * @return size
     */
    public int size(){

        return this.tracks.size();
    }

    /**
     * Fonction retournant la durée max des tracks
     * @return maxDuration
     */
    public long getMaxDuration() {
        long maxDuration = 0;

        for (Track track : this.tracks) {
            long trackTotalDuration = track.getTotalDuration();

            if (trackTotalDuration > maxDuration) {
                maxDuration = trackTotalDuration;
            }
        }
        return maxDuration;
    }

    public String toString() {
        return this.getTracks().toString();
    }
}
