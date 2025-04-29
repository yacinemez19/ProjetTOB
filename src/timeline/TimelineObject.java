package timeline;

public class TimelineObject {
    /* Information sur l'objet */
    String name;
    int clipId;
    String mediaType; // Deux options video ou audio
    int srcId; // Quand il y a plusieurs sources videos/audios
    /* Information sur sa positon dans la timeline */
    long[] offset;
    long[] start;
    long[] duration;

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
}
