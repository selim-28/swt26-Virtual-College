package swt2.virtualcollege.receive.pinwand;

import java.util.ArrayList;
import java.util.List;

/*
 * Muster "Observer" – das SUBJEKT (Kapitel 13, Aufgabe 2).
 *
 * Die Pinwand fuehrt eine Liste von Abonnenten (Beobachtern). Kommt ein
 * neuer Post, ruft sie benachrichtige() und JEDER Abonnent bekommt
 * automatisch aktualisiere() – ohne dass die Pinwand die Nutzer einzeln
 * kennen muss (Entkopplung).
 *
 * Im Diagramm:  Pinwand o-- Beobachter,  Pinwand *-- Post
 */
public class Pinwand {

    // Die Liste der Abonnenten (Beobachter). Typ ist das Interface –
    // dadurch ist es egal, WELCHE Klasse dahinter steckt.
    private final List<Beobachter> abonnenten = new ArrayList<>();

    // Die Beitraege an der Pinwand.
    private final List<Post> posts = new ArrayList<>();

    // Beobachter anmelden (abonnieren).
    public void abonniere(Beobachter b) {
        abonnenten.add(b);
    }

    // Beobachter abmelden – WICHTIG laut Folie, sonst "Lapsed Listener".
    public void meldeAb(Beobachter b) {
        abonnenten.remove(b);
    }

    // Alle Abonnenten benachrichtigen.
    public void benachrichtige(Benachrichtigung b) {
        for (Beobachter beobachter : abonnenten) {
            beobachter.aktualisiere(b);
        }
    }

    /*
     * Neuen Post anpinnen: speichern UND alle Abonnenten benachrichtigen.
     * Das ist der typische "Push"-Moment des Observer-Musters.
     */
    public void neuerPost(Post post) {
        posts.add(post);
        benachrichtige(new Benachrichtigung("Neuer Pinwand-Post: " + post.titel()));
    }

    public List<Post> getPosts() {
        return posts;
    }
}
