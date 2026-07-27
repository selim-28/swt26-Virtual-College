package swt2.virtualcollege.receive.pinwand;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Konkreter Beobachter (US 4.3): merkt sich die Benachrichtigungen der Pinwand,
 * damit das Frontend sie ueber GET /api/pinwand/benachrichtigungen abrufen kann.
 * Es werden nur die letzten MAX_EINTRAEGE gehalten.
 */
@Component
public class BenachrichtigungsSpeicher implements Beobachter {

    static final int MAX_EINTRAEGE = 20;

    /** Eine empfangene Benachrichtigung mit Zeitstempel. */
    public record Eintrag(String text, Instant zeitpunkt) {
    }

    // Neueste Eintraege stehen vorn.
    private final Deque<Eintrag> eintraege = new ArrayDeque<>();

    /** Wird von der Pinwand bei jedem neuen Beitrag aufgerufen. */
    @Override
    public synchronized void aktualisiere(Benachrichtigung b) {
        eintraege.addFirst(new Eintrag(b.text(), Instant.now()));
        while (eintraege.size() > MAX_EINTRAEGE) {
            eintraege.removeLast();
        }
    }

    /** Kopie aller gemerkten Benachrichtigungen, neueste zuerst. */
    public synchronized List<Eintrag> alle() {
        return new ArrayList<>(eintraege);
    }
}
