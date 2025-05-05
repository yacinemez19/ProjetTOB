package com.timeline;

import java.util.HashMap;

public class TimelineObject {
    /* Information sur l'objet */
    private String name;
    private int clipId;
    private String mediaType; // Deux options video ou audio
    private int srcId; // Quand il y a plusieurs sources videos/audios
    /* Information sur sa positon dans la com.timeline */
    private long[] offset;
    private long[] start;
    private long[] duration;

    TimelineObject(String name, int clipId, int srcId, long[] offset, long[] start, long[] duration) {
        this.name = name;
        this.clipId = clipId;
        this.mediaType = "video"; // en vrai on appellera la classe clip pour avoir l'info
        this.srcId = srcId;
        this.offset = offset;
        this.start = start;
        this.duration = duration;
    }

    TimelineObject(String name, int clipId, long[] offset, long[] start, long[] duration) {
        this(name, clipId, 1, offset, start, duration);
    }

    public void setClipId(int clipId) {
        this.clipId = clipId;
    }

    public void setSrcId(int srcId) {
        this.srcId = srcId;
    }

    public void setOffset(long[] offset) {
        this.offset = offset;
    }
    public void setStart(long[] start) {
        this.start = start;
    }
    public void setDuration(long[] duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getClipId() {
        return clipId;
    }

    public String getMediaType() {
        return mediaType;
    }

    public int getSrcId() {
        return srcId;
    }

    public long[] getOffset() {
        return offset;
    }

    public long[] getStart() {
        return start;
    }

    public long[] getDuration() {
        return duration;
    }

    public String toString(){
        HashMap<String,Object> data = new HashMap<>();
        data.put("name", name);
        data.put("clipId", clipId);
        data.put("mediaType", mediaType);
        data.put("srcId", srcId);
        data.put("offset", offset);
        data.put("start", start);
        return data.toString();
    }


}
