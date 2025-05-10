package com.preview;

import com.timeline.TimelineObject;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.Pad;

public class PadManager {

    public void padLinker(Element element, Pad pad, Element audioSelector, Element videoSelector, TimelineObject timelineObject) {
        Pad audioSinkPad = audioSelector.getRequestPad("sink_%u");
        Pad videoSinkPad = videoSelector.getRequestPad("sink_%u");

        System.out.println("New pad received " + pad.getName() + " from " + element.getName());

        // On récupère le type de fichier
        Caps caps = pad.getCurrentCaps();
        String type = caps.getStructure(0).getName();
        System.out.println(type);
        // On link l'audio
        if (!audioSinkPad.isLinked() && type.equals("audio/x-raw")) {
            System.out.println("Audio Sink Pad linked");
            audioSelector.set("active-pad", audioSinkPad);
            pad.link(audioSinkPad);
            timelineObject.setAudioPad(audioSinkPad);
        }
        // On link la vidéo
        if (!videoSinkPad.isLinked() && type.equals("video/x-raw")) {
            System.out.println("Video Sink Pad linked");
            videoSelector.set("active-pad", videoSinkPad);
            pad.link(videoSinkPad);
            timelineObject.setvideoPad(videoSinkPad);
        }
    }
}
