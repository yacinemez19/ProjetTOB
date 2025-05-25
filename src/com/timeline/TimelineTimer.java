package com.timeline;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimelineTimer {
    private LongProperty currentTimeMs = new SimpleLongProperty(0);
    private boolean isPlaying = false;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LongProperty currentTimeMsProperty() {
        return currentTimeMs;
    }

    public long getCurrentTimeMs() {
        return currentTimeMs.get();
    }

    public void setCurrentTimeMs(long timeMs) {
        this.currentTimeMs.set(timeMs);
    }

    public void play() {
        if (!isPlaying) {
            isPlaying = true;
            scheduler.scheduleAtFixedRate(this::tick, 0, 30, TimeUnit.MILLISECONDS);
        }
    }

    public void pause() {
        isPlaying = false;
        scheduler.shutdown();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private void tick() {
        if (isPlaying) {
            currentTimeMs.set(currentTimeMs.get() + 30);
        }
    }
}