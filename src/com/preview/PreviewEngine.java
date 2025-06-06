package com.preview;

import com.Clip;
import com.VideoClip;
import com.Utils;
import com.timeline.TimelineObject;
import com.timeline.TimelineTimer;
import com.timeline.Track;
import javafx.scene.image.ImageView;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.event.SeekType;
import org.freedesktop.gstreamer.fx.FXImageSink;
import org.freedesktop.gstreamer.message.StateChangedMessage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

public class PreviewEngine {

    // Attributs
    private boolean isStarted = false;
    private Pipeline pipeline;
    private FXImageSink fxSink;
    private PadManager padManager = new PadManager();
    private Element audioSelector;
    private Element videoSelector;
    private TimelineObject blackObject;
    private Track currentPlayingTrack;
    private PreviewController controller;

    /**
     * Permet de lancer le rendu dans le Preview
     *
     * @param videoView       Le sink ou on affichera la vidéo
     * @param previewListener Permet de définir des comportements spécifiques pour quand le preview est mis en pause/play
     */
    public void engineStart(ImageView videoView, PreviewListener previewListener) {
        /*
         * Set up paths to native GStreamer libraries - see adjacent file.
         */
        Utils.configurePaths();
        /*
         * Initialize GStreamer. Always pass the lowest version you require -
         * Version.BASELINE is GStreamer 1.8. Use Version.of() for higher.
         * Features requiring later versions of GStreamer than passed here will
         * throw an exception in the bindings even if the actual native library
         * is a higher version.
         */
        Gst.init(Version.BASELINE, "PreviewPipeline");
        /**
         * ON cook ici
         * Création des éléments GStreamer (source, convertisseurs, sinks audio et vidéo)
         * audioconvert et audioresample permette d'avoir un son correct en sortie dans le audiosink
         */
        // Les linker dynmaiques
        videoSelector = ElementFactory.make("input-selector", "videoSelector");
        audioSelector = ElementFactory.make("input-selector", "audioSelector");
        // Audio Sink
        Element converter = ElementFactory.make("audioconvert", "audioconverter");
        Element resample = ElementFactory.make("audioresample", "audiosample");
        Element audioSink = ElementFactory.make("autoaudiosink", "audiosink");
        // Video Sink
        Element videoConverter = ElementFactory.make("videoconvert", "videoconverter");
        fxSink = new FXImageSink();
        // On créé une pipelin vide et on lui ajoute tout les éléments
        pipeline = new Pipeline("testPipeline");

        pipeline.add(videoSelector);
        pipeline.add(audioSelector);
        pipeline.add(converter);
        pipeline.add(resample);
        pipeline.add(audioSink);
        pipeline.add(videoConverter);
        pipeline.add(fxSink.getSinkElement());
        // On link la fin de la pipeline audio input-selector->converter->resample->audiosink
        audioSelector.link(converter);
        converter.link(resample);
        resample.link(audioSink);
        // On link la vidéo input-selector->videoConverter->fxSink
        videoSelector.link(videoConverter);
        videoConverter.link(fxSink.getSinkElement());
        System.out.println("videoView: " + videoView);
        videoView.imageProperty().bind(fxSink.imageProperty());

        // loop on EOS if button selected
        pipeline.getBus().connect((Bus.MESSAGE) (bus, message) -> {
            switch (message.getType()) {
                case ASYNC_START:
                    System.out.println("Async start sur : " + message.getSource());
                    break;
                case ASYNC_DONE:
                    System.out.println("Async done sur : " + message.getSource());
                    break;
                case STATE_CHANGED:
                    StateChangedMessage stateChangedMsg = (StateChangedMessage) message;
                    //State oldState = stateChangedMsg.getOldState();
                    State newState = stateChangedMsg.getNewState();
                    System.out.println("New state : " + newState);
                    if (newState == State.PLAYING) {
                        previewListener.onPlaying();
                    } else {
                        previewListener.onPaused();
                    }
            }
        });

        // on indique que la pipeline tourne correctement
        isStarted = true;

    }

    public void preloadClip(TimelineObject newClip, CountDownLatch latch) {

        Element source = ElementFactory.make("uridecodebin", "source"+newClip.getName());
        pipeline.add(source);
        source.set("uri", newClip.getSource().getSource().toString());
        source.connect(
                new Element.PAD_ADDED() {
                    @Override
                    public void padAdded(Element element, Pad pad) {
                        padManager.padLinker(element, pad, audioSelector, videoSelector, newClip);
                    }
                }
        );
       //pipeline.setState(State.PAUSED);
        // attendre que les pads soient liés
        source.connect((Element.NO_MORE_PADS) (elem) -> {
            System.out.println("Plus de pads à ajouter pour " + newClip.getName());
            latch.countDown();
        });

        newClip.setGstreamerSource(source);
    }
    public void preloadClip(TimelineObject newClip) {

        Element source = ElementFactory.make("uridecodebin", "source"+newClip.getName());
        pipeline.add(source);
        source.set("uri", newClip.getSource().getSource().toString());
        source.connect(
                new Element.PAD_ADDED() {
                    @Override
                    public void padAdded(Element element, Pad pad) {
                        padManager.padLinker(element, pad, audioSelector, videoSelector, newClip);
                    }
                }
        );
        //pipeline.setState(State.PAUSED);
        // attendre que les pads soient liés
        source.connect((Element.NO_MORE_PADS) (elem) -> {
            System.out.println("Plus de pads à ajouter pour " + newClip.getName());
        });

        newClip.setGstreamerSource(source);
    }

    private void loadBlack () {
        File videoFile = new File("ressources/assets/blackSource.mp4");

                Clip blackVideo = new VideoClip(videoFile.toURI(),
                        Duration.ofSeconds(1),
                                3840,
                                2160,
                                new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB));

        blackObject = new TimelineObject(blackVideo, "video", 0, 0);
        Element blackSource = ElementFactory.make("uridecodebin", "_blackSource");

        pipeline.add(blackSource);
        blackSource.set("uri", blackObject.getSource().getSource().toString());
        blackSource.connect(
                                new Element.PAD_ADDED() {
                    @Override
                    public void padAdded(Element element, Pad pad) {
                        padManager.padLinker(element, pad, audioSelector, videoSelector, blackObject);
                    }
                }
        );
        //pipeline.setState(State.PAUSED);
        // attendre que les pads soient liés
        blackSource.connect((Element.NO_MORE_PADS) (elem) -> {
            System.out.println("Plus de pads à ajouter pour le noir");
        });

        blackObject.setGstreamerSource(blackSource);
    }

    /**
     * Permet de mettre en pause le preview peut importe la source
     */
    public void enginePause() {
        if (isStarted) {
            pipeline.setState(State.PAUSED);
            currentPlayingTrack.getTimer().pause();
        }
    }

    /**
     * Permet de mettre en pause le preview en play si il avait été mis en pause
     */
    public void engineResume() {
        if (isStarted) {
            playTrack(currentPlayingTrack);
            pipeline.setState(State.PLAYING);
            getAndSeekCurrent(currentPlayingTrack.getTimer().getCurrentTimeMs());
            currentPlayingTrack.getTimer().play();
        }
    }

    public void engineReset() {
        currentPlayingTrack.getTimer().pause();
        currentPlayingTrack.getTimer().setCurrentTimeMs(0);
        pipeline.setState(State.PAUSED);
        getAndSeekCurrent(currentPlayingTrack.getTimer().getCurrentTimeMs());
    }

    /**
     * Permet de passer de play à pause ou inversement sans se soucier de l'état actuel
     */
    public void engineTogglePlayPause() {
        if (isStarted) {
            State currentState = pipeline.getState();
            if (currentState == State.PAUSED) {
                pipeline.setState(State.PLAYING);
            } else {
                pipeline.setState(State.PAUSED);
            }
        }
    }

    /**
     * Avance d'une frame dans la timeline et la met en pause si elle l'était pas
     */
    public void engineNextFrame() {
        // On vérifie que le preview a bien démarré avant de manipuler la pipeline
        if (!isStarted) {
            System.out.println("[Preview Engine] engine not ready");
            return;
        }
        // Pour éviter les soucis on s'assure qu'elle est en pause
        pipeline.setState(State.PAUSED);

        // On cherche la frame actuelle
        long currentPosition = pipeline.queryPosition(Format.TIME);
        long frameDuration = (long) (1_000_000_000 / 25.0); // Durée d'une frame à 25 fps

        // On avance d'une frame
        long newPosition = currentPosition + frameDuration;
        boolean result = pipeline.seek(
                1.0,
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, newPosition,
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("[Preview Engine] Next frame seek failed");
        }
    }

    /**
     * Recule d'une frame dans la timeline et la met en pause si elle l'était pas
     */
    public void engineLastFrame() {
        // On vérifie que le preview a bien démarré avant de manipuler la pipeline
        if (!isStarted) {
            System.out.println("[Preview Engine] engine not ready");
            return;
        }
        // Pour éviter les soucis on s'assure qu'elle est en pause
        pipeline.setState(State.PAUSED);

        // On cherche la frame actuelle
        long currentPosition = pipeline.queryPosition(Format.TIME);
        long frameDuration = (long) (1_000_000_000 / 25.0); // Durée d'une frame à 25 fps

        // On recule d'une frame
        long newPosition = Math.max(0, currentPosition - frameDuration);
        boolean result = pipeline.seek(
                1.0,
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, newPosition,
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("[Preview Engine] Next frame seek failed");
        }
    }

    /**
     * Permet de fermer proprement le Preview en désallouant la mémoire utilisée
     */
    public void engineStop() {
        if (pipeline != null) {
            pipeline.setState(State.NULL);
            pipeline = null;
            isStarted = false;
        }
    }

    /**
     * Permet de récupérer la position temporel actuelle
     */
    public long engineGetCurrentPosition() {
        if (isStarted) {
            return pipeline.queryPosition(Format.TIME);
        } else {
            return 0;
        }
    }
    /**
     * Permet de récupérer la position temporel actuelle
     */
    public void playTrack(Track track) {
        currentPlayingTrack = track;
        controller.bindTimerLabel();
        CountDownLatch latch = new CountDownLatch(track.getElementCount());
        System.out.println("[Preview Engine] playing track with " + track.getElementCount() + " elements");
        track.mapElements((e) -> preloadClip(e, latch));
        loadBlack();
        pipeline.setState(State.PAUSED);
        new Thread(() -> {
            try {
                latch.await();
                TimelineTimer timer = track.getTimer();
                timer.setCurrentTimeMs(0);
                updatePreview(0);
                timer.play();
                enginePause();
                timer.currentTimeMsProperty().addListener((observable, oldValue, newValue) -> {
                    updatePreview(newValue.longValue());
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void updatePreview(long newPosition) {
        //System.out.println("[Preview Engine] updatePreview position : " + newPosition);
        if (currentPlayingTrack.newClipToRender(newPosition)) {
            System.out.println("[Preview Engine] updatePreview ON UPDATE LE CLIP EN COURS ");
            getAndSeekCurrent(newPosition);
        }
    }

    public void setPreviewController(PreviewController previewController) {
        controller = previewController;
    }

    public void setCurrentTrack(Track currentTrack) {
        currentPlayingTrack = currentTrack;
        controller.setCurrentTrack(currentTrack);
    }

    private void getAndSeekCurrent(long newPosition) {
        TimelineObject curr = currentPlayingTrack.getObjectAtTime(newPosition);
        long offset = 0;
        if (curr != null) {
            padManager.padSwapper(audioSelector, videoSelector, curr);
            offset = curr.getOffset();
        } else {
            padManager.padSwapper(audioSelector, videoSelector, blackObject);
        }
        boolean result = pipeline.seek(
                1.0,
                Format.TIME,
                EnumSet.of(SeekFlags.FLUSH, SeekFlags.ACCURATE),
                SeekType.SET, offset,
                SeekType.NONE, -1
        );
        if (!result) {
            System.out.println("[Preview Engine] Next frame seek failed");
        }
    }
}
