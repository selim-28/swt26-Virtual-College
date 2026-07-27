package swt2.virtualcollege.receive.pinwand;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer-Muster: das Subjekt. Die Pinwand fuehrt eine Liste von Abonnenten und
 * benachrichtigt sie bei jedem neuen Beitrag, ohne die Empfaenger einzeln zu kennen.
 */
public class Pinwand {

    private final List<Beobachter> abonnenten = new ArrayList<>();

    private final List<Post> posts = new ArrayList<>();

    public void abonniere(Beobachter b) {
        abonnenten.add(b);
    }

    /** Abmelden verhindert, dass ausgeschiedene Beobachter weiter benachrichtigt werden. */
    public void meldeAb(Beobachter b) {
        abonnenten.remove(b);
    }

    public void benachrichtige(Benachrichtigung b) {
        for (Beobachter beobachter : abonnenten) {
            beobachter.aktualisiere(b);
        }
    }

    /** Speichert einen neuen Beitrag und benachrichtigt alle Abonnenten. */
    public void neuerPost(Post post) {
        posts.add(post);
        benachrichtige(new Benachrichtigung("Neuer Pinwand-Post: " + post.titel()));
    }

    public List<Post> getPosts() {
        return posts;
    }
}
