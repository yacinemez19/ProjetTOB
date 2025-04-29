package timeline;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Track {
    private String name;
    private List<TimelineObject> elements;
    int currentIndex;

    public Track(String name) {
        this.elements = new ArrayList<>();
        this.name = name;
    }
    public Track() {
        this.elements = new ArrayList<>();
        this.name = "New Track";
        this.currentIndex = 0;
    }

    public void addTimelineObject(TimelineObject obj) {
       // TO DO : il faut que les objets de la timelines soient dans l'ordre (obj.start)
    }

    public TimelineObject getObjectAtTime(long[] timing) {
        // TO DO : Retourne l'objet à la position timing
        return null;
    }
    public boolean modifyTimelineObject(long[] timing, String propertyName, Object newValue) {
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

    public void removeTimelineObject(long[] timing) {
        // TO DO : Enlever l'objet à cette date
    }

    /*FONCTIONS POUR LE NAME*/
    public String getName() {
        return name;
    }
    public void changeTimelineName(String newName) {
        // TO DO : Changer le nom de la Timeline
    }
}
