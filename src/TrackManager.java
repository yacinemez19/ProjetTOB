import timeline.Track;

import java.time.Duration;
import java.util.ArrayList;

public class TrackManager {

    private ArrayList<Track> tracks;

    public ArrayList<Track> getTracks() {
        return this.tracks;
    }

    public void addTrack(Track track) {
        this.tracks.add(track);
    }

    public boolean removeTrack(Track track) {
        return this.tracks.remove(track);
    }

    public void moveTrack(Track track) {
        // à voir si on garde ou pas
        System.out.println("move track");
    }

    public int size(){
        return this.tracks.size();
    }

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


}
