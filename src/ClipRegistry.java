import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

public class ClipRegistry {
    private HashMap<URI, Clip> clipsByURI;

    public ClipRegistry() {
        clipsByURI = new HashMap<>();
    }

    public void register(Clip clip) {
        if (clip == null) {
            throw new IllegalArgumentException("Clip ne peut pas être null");
        }
        URI uri = clip.getSource();
        if (clipsByURI.containsKey(uri)) {
            throw new IllegalArgumentException("Clip déjà enregistré avec cette URI");
        }
        clipsByURI.put(uri, clip);

        // Debugging output
        System.out.println(clipsByURI);
    }

    public void unregister(Clip clip) {
        if (clip == null) {
            throw new IllegalArgumentException("Clip ne peut pas être null");
        }
        URI uri = clip.getSource();
        if (clipsByURI.containsKey(uri)) {
            clipsByURI.remove(uri);
        } else {
            throw new IllegalArgumentException("Clip n'est pas enregistré avec cette URI");
        }

    }

    public Clip getClip(URI uri) {
        return clipsByURI.get(uri);
    }

    public Collection<Clip> getAllClips() {
        return clipsByURI.values();
    }
}
