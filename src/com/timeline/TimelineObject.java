package com.timeline;

import java.util.HashMap;
import org.freedesktop.gstreamer.*;
import com.Clip;

public class TimelineObject {
    /* Information sur l'objet */
    private Clip source;
    private Element gstreamerSource;
    private Pad videoPad;
    private Pad audioPad;
    private String name;
    private String mediaType; // Deux options video ou audio
    /* Information sur sa positon dans la com.timeline */
    private long offset;
    private long start;
    private long duration;

    public TimelineObject(Clip source, String mediaType, long offset, long start) {
        this.source = source;
        this.name = source.getName();
        this.mediaType = "video"; // en vrai on appellera la classe clip pour avoir l'info
        this.offset = offset; // décalage par rapport au t0 de la vidéo
        this.start = start;
        this.duration = source.getDuration().toNanos();
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
    public void setStart(long start) {
        this.start = start;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setGstreamerSource(Element gstreamerSource) {
        this.gstreamerSource = gstreamerSource;
    }

    public void setvideoPad(Pad videoPad) {
        this.videoPad = videoPad;
    }

    public void setAudioPad(Pad audioPad) {
        this.audioPad = audioPad;
    }

    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
    }

    public long getOffset() {
        return offset;
    }

    public long getStart() {
        return start;
    }

    public long getDuration() {
        return duration;
    }

    public Clip getSource() {
        return source;
    }

    public String toString(){
        HashMap<String,Object> data = new HashMap<>();
        data.put("clip", source);
        data.put("mediaType", mediaType);
        data.put("offset", offset);
        data.put("start", start);
        return data.toString();
    }
}
