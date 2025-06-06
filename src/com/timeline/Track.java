package com.timeline;

import com.Clip;
import com.preview.PreviewEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Track {
    private String name;
    private ObservableList<TimelineObject> elements;
    private TimelineTimer timer;
    private int currentIndex;
    private long endCurrentObject;

    public Track(String name) {
        this.elements = FXCollections.observableArrayList();
        this.name = name;
        timer = new TimelineTimer();
        endCurrentObject = 0;
    }

    public Track() {
        this("Timeline");
        this.currentIndex = 0;
    }

    /**
     * Fonction ajoutant un objet au bout de la piste.
     *
     * @param obj l'objet à ajouter
     */
    public void addTimelineObjectAtEnd(Clip obj, PreviewEngine engine) {
        TimelineObject newTimelineObject = new TimelineObject(obj,
                "video",
                0,
                getTotalDuration());
        elements.add(newTimelineObject);
        //engine.preloadClip(newTimelineObject);
    }

    /**
     * Fonction ajoutant un objet à la piste à un moment donné.
     *
     * @param obj    l'objet à ajouter
     * @param timing le moment donné
     * @return TimelineObject
     * @throws IllegalArgumentException si le timing est négatif ou si l'espace n'est pas libre
     */
    public TimelineObject addTimelineObject(Clip obj, long timing, long duration) {
        if (timing < 0) {
            throw new IllegalArgumentException("Le timing ne peut pas être négatif");
        }

        // Trouver l'index où insérer l'objet dans le tableau d'éléments
        int index = elements.indexOf(getFirstAfter(timing));
        if (index == -1) {
            index = elements.size();
        }

        TimelineObject timelineObject = new TimelineObject(obj, "video", 0, timing, duration);

        // Décaler tous les objets après le timing si l'espace n'est pas suffisant
        if (!isPlaceEnough(timing, timelineObject)) {
            shiftAfter(timing, obj.getDuration().toNanos());
        }

        elements.add(index, timelineObject);

        // TODO : Supprimer l'affichage de test
        System.out.println("Début des clips dans ma timeline : ");
        for (TimelineObject element : elements) {
            System.out.println(element.getStart());
        }

        return timelineObject;
    }

    public void shiftAfter(long timing, long delta) {
        System.out.println("Shift après " + timing + " de " + delta);
        for (TimelineObject object : elements) {
            long start = object.getStart();
            if (timing <= start) {
                System.out.println("Je deplace l'objet " + object.getName() + " de " + delta);
                object.shift(delta);
            }
        }
    }

    public boolean isPlaceEnough(long timing, TimelineObject timelineObject) {
        TimelineObject obj = getFirstAfter(timing);

        if (obj == null) {
            return true;
        }

        return (timing + timelineObject.getDuration() < obj.getStart());
    }

    public boolean isShiftPossible(TimelineObject objToShift, long delta) {
        long start = objToShift.getStart();
        long end = objToShift.getDuration() + start;

        for (TimelineObject object : elements) {
            if (object == objToShift) {
                continue;
            }

            long start2 = object.getStart();
            long end2 = object.getDuration() + start2;
            if (start2 < end && end2 > start) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fonction retournant le premier objet après un moment donné.
     * Retourne null si aucun objet n'est trouvé.
     *
     * @param timing le moment donné
     * @return TimelineObject
     */
    private TimelineObject getFirstAfter(long timing) {
        for (TimelineObject object : elements) {
            long start = object.getStart();
            if (timing <= start) {
                return object;
            }
        }
        return null;
    }

    /*
     * Fonction retournant l'objet à un moment donné.
     * @param timing le moment donné
     * @return TimelineObject l'objet à ce moment
     */
    public TimelineObject getObjectAtTime(long timing) {

        if (elements.isEmpty()) {
            endCurrentObject = 0;
            currentIndex = -1; // Indiquer qu'aucun objet n'est courant
            return null;
        }


        for (int i = 0; i < elements.size(); i++) {
            TimelineObject object = elements.get(i);
            long start = object.getStart();
            long end = object.getDuration() + start;
            if (timing >= start && timing < end) {
                currentIndex = elements.indexOf(object);
                endCurrentObject = end;
                return object;
            } else if (timing < start) {
                // Si le timing est avant le début du premier objet trouvé
                endCurrentObject = start;
                return null;
            } else if (timing >= end && i == elements.size() - 1) {
                // Si le timing est après la fin du dernier objet
                endCurrentObject = Long.MAX_VALUE;
                return null;
            } else if (timing >= end) {
                // Vérifier l'espace entre cet objet et le suivant
                if (i + 1 < elements.size()) {
                    long nextStart = elements.get(i + 1).getStart();
                    if (timing >= end && timing < nextStart) {
                        endCurrentObject = nextStart;
                        return null;
                    }
                } else {
                    // Si c'est le dernier élément, la période de "noir" dure indéfiniment après
                    endCurrentObject = Long.MAX_VALUE;
                    return null;
                }
            }
        }
        return null;
    }

    /*
     * Fonction retournant l'objet actuellement sous le curseur.
     * @return TimelineObject l'objet à ce moment
     */
    public TimelineObject getCurrentObject() {
        return getObjectAtTime(timer.getCurrentTimeMs());
    }

    /**
     * Fonction retournant si on a changé d'objet ou pas par rapport au temps dans la timeline
     *
     * @return boolean Vrai si et seulement si on a changé d'objet
     */
    public boolean newClipToRender(long timing) {
        System.out.println("New clip to render end : " + endCurrentObject);
        return (timing > endCurrentObject);
    }

    public boolean modifyTimelineObject(long timing, String propertyName, Object newValue) {
        TimelineObject obj = getObjectAtTime(timing);
        if (obj == null) return false;

        // Construire le nom du setter
        String methodName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

        try {
            // Chercher le setter correspondant
            Method[] methods = obj.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                    method.invoke(obj, newValue);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeTimelineObject(long timing) {
        TimelineObject object = getObjectAtTime(timing);
        if (object == null) return;
        else {
            elements.remove(object);
        }
    }

    /*FONCTIONS POUR LE NAME*/
    public String getName() {
        return name;
    }

    public void changeTimelineName(String newName) {
        // TODO : Changer le nom de la Timeline
        this.name = newName;
    }

    /**
     * Fonction retournant la durée totale de la track
     *
     * @return long totalDuration
     */
    public long getTotalDuration() {
        long totalDuration = 0;

        for (TimelineObject object : elements) {
            totalDuration += object.getDuration() + object.getStart();
        }
        return totalDuration;
    }

    public List<TimelineObject> getItems() {
        return elements;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public ObservableList<TimelineObject> getElements() {
        return elements;
    }

    public void mapElements(Consumer<TimelineObject> action) {
        for (TimelineObject object : elements) {
            action.accept(object);
        }
    }

    public int getElementCount() {
        return elements.size();
    }

    public TimelineTimer getTimer() {
        return timer;
    }

    public String toString() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", this.getName());
        data.put("elements", this.getItems());
        data.put("currentIndex", this.getCurrentIndex());
        return data.toString();
    }
}
