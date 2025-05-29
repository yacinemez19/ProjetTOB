package com.importation;

import com.Clip;
import com.VideoClip;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.time.Duration;

public class TestVideoImporter implements VideoImporter {
    @Override
    public Clip importVideo(URI source) throws IllegalArgumentException {
        return new VideoClip(source,
                Duration.ofSeconds(120),
                1920,
                1080,
                new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB));
    }
}
