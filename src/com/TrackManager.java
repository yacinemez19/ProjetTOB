package com;

import com.timeline.Track;

import java.time.Duration;
import java.util.ArrayList;

public class TrackManager {

    private ArrayList<Track> tracks;

    /**
     * Constructeur de la classe TrackManager
     */
    public TrackManager() {
        this.tracks = new ArrayList<>();
    }

    /**
     * getter liste des tracks
     * @return ArrayList<Track> tracks
     */
    public ArrayList<Track> getTracks() {

        return this.tracks;
    }

    /**
     * Ajoute une track
     * @param track
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
    public Duration getMaxDuration() {

        Duration maxDuration = Duration.ZERO;

        for (Track track : this.tracks) {

            Duration trackTotalDuration = track.getTotalDuration();

            if (trackTotalDuration.compareTo(maxDuration) > 0) {

                maxDuration = trackTotalDuration;
            }
        }
        return maxDuration;
    }

    public String toString() {
        return this.getTracks().toString();
    }
}
