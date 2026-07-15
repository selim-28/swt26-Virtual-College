package swt2.virtualcollege.receive.pinwand;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Component;

/*
 * Muster "Observer" – ein KONKRETER BEOBACHTER, der im LIVE-System haengt
 * (EPIC 4, US 4.3 "Ueber neue Pinwand-Beitraege benachrichtigt werden").
 *
 * Der PinwandService meldet dieses Objekt bei der Pinwand (dem Subjekt) an.
 * Bei jedem neuen Beitrag ruft die Pinwand aktualisiere() auf, und wir merken
 * uns die Benachrichtigung. Das Frontend kann sie ueber
 * GET /api/pinwand/benachrichtigungen abrufen - so ist der Observer nicht nur
 * ein Diagramm-Beispiel, sondern eine sichtbare Funktion.
 *
 * Es werden nur die letzten MAX_EINTRAEGE behalten (aeltere fliegen raus),
 * damit der Speicher nicht unbegrenzt waechst.
 */
@Component
public class BenachrichtigungsSpeicher implements Beobachter {

    static final int MAX_EINTRAEGE = 20;

    /** Eine empfangene Benachrichtigung mit Zeitstempel. */
    public record Eintrag(String text, Instant zeitpunkt) {
    }

    // Deque = Liste mit schnellem Einfuegen vorn; neueste zuerst.
    private final Deque<Eintrag> eintraege = new ArrayDeque<>();

    /** Wird von der Pinwand (Subjekt) bei jedem neuen Post aufgerufen. */
    @Override
    public synchronized void aktualisiere(Benachrichtigung b) {
        eintraege.addFirst(new Eintrag(b.text(), Instant.now()));
        while (eintraege.size() > MAX_EINTRAEGE) {
            eintraege.removeLast();
        }
    }

    /** Alle gemerkten Benachrichtigungen, neueste zuerst (Kopie). */
    public synchronized List<Eintrag> alle() {
        return new ArrayList<>(eintraege);
    }
}
