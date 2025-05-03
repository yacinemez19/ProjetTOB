package timeline;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
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
       // TODO : il faut que les objets de la timelines soient dans l'ordre (obj.start)
        int j=0;
        while (j<elements.size() && elements.get(j).getStart()[0] < obj.getStart()[0]) j += 1;
        elements.add(j, obj);
    }

    public TimelineObject getObjectAtTime(long[] timing) {
        // TODO : Retourne l'objet à la position timing
        if (timing==null) return null;
        for (TimelineObject object : elements) {
            long start = object.getStart()[0];
            long end = object.getDuration()[0] + start ;
            if (timing[0] < end && timing[0]>=start ) {
                return object;
            }
        }
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
     * @return Duration totalDuration
     */
    public Duration getTotalDuration(){
        // TODO : Implémenter la fonction
        return null;
    }

    public List<TimelineObject> getItems() {
        return elements;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public String toString() {
        HashMap<String,Object> data = new HashMap<>();
        data.put("name",this.getName());
        data.put("elements",this.getItems());
        data.put("currentIndex",this.getCurrentIndex());
        return data.toString();
    }
}
