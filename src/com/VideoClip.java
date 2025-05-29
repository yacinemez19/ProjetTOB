package com;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import java.nio.file.attribute.FileTime;
import java.time.ZoneId;



/**
 * Implémentation de l'interface com.Clip pour représenter un clip importé.
 */
public class VideoClip implements Clip {

    private final URI source;
    private final Duration duration;
    private final int width;
    private final int height;
    private final BufferedImage thumbnail;
    private final Path path;
    private final File file;

    /**
     * Construit un com.ImportedClip.
     *
     * @param source       URI du fichier source
     * @param duration     Durée de la vidéo
     * @param width        Largeur en pixels
     * @param height       Hauteur en pixels
     * @param thumbnail    Miniature générée
     */
    public VideoClip(URI source,
                     Duration duration,
                     int width,
                     int height,
                     BufferedImage thumbnail) {
        this.source      = Objects.requireNonNull(source, "source ne peut être null");
        this.duration    = Objects.requireNonNull(duration, "duration ne peut être null");
        this.width       = width;
        this.height      = height;
        this.thumbnail   = Objects.requireNonNull(thumbnail, "thumbnail ne peut être null");
        this.path        = Paths.get(this.source);
        this.file        = path.toFile();
    }

    // TODO : remove this test method
    public static VideoClip test() {
        return new VideoClip(URI.create("file:///home/adam/Downloads/test.mp4"),
                Duration.ofSeconds(10),
                1920,
                1080,
                new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB));
    }

    @Override
    public URI getSource() {
        return source;
    }

    @Override
    public String getName() {
        /**
        String path = source.getPath();
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
         */

        return file.getName();
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public BufferedImage getThumbnail() {
        return thumbnail;
    }

    @Override
    public long getSizeBytes() {
        return file.length();
    }

    @Override
    public String getType() {
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            throw new RuntimeException("IOException lors de la recherche du type de " + this.getName(), e);
        }
    }

    @Override
    public LocalDate getDateCreated() {
        try{
            FileTime creationTime = Files.readAttributes(path, BasicFileAttributes.class).creationTime();
            return creationTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch(IOException e){
            throw new RuntimeException("IOException lors de la recherche du type de " + this.getName(), e);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof VideoClip)) return false;
        VideoClip that = (VideoClip) object;
        return width == that.width &&
                height == that.height &&
                this.getSizeBytes() == that.getSizeBytes() &&
                source.equals(that.source) &&
                duration.equals(that.duration) &&
                thumbnail.equals(that.thumbnail) &&
                this.getType().equals(that.getType()) &&
                this.getDateCreated().equals(that.getDateCreated());
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, duration, width, height, thumbnail, this.getSizeBytes(), this.getType(), this.getDateCreated());
    }

    @Override
    public String toString() {
        return "com.ImportedClip{" +
                "name=" + getName() +
                ", duration=" + duration +
                ", resolution=" + width + "x" + height +
                ", size=" + getSizeLabel() +
                ", type=" + this.getType() +
                ", date=" + this.getDateCreated() +
                '}';
    }
}
