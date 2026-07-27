package swt2.virtualcollege.receive.nutzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swt2.virtualcollege.receive.pinwand.Benachrichtigung;
import swt2.virtualcollege.receive.pinwand.Beobachter;

/**
 * Basisklasse aller Nutzer. Als Beobachter kann jeder Nutzer Benachrichtigungen
 * der Pinwand empfangen; Student, Betreuer und Verwaltungsangestellter erben davon.
 */
public class Nutzer implements Beobachter {

    private static final Logger log = LoggerFactory.getLogger(Nutzer.class);

    private final String name;

    public Nutzer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Wird von der Pinwand aufgerufen. Produktiv stuende hier der Versand einer
     * Push-Nachricht.
     */
    @Override
    public void aktualisiere(Benachrichtigung b) {
        log.info("[Nutzer {}] Benachrichtigung erhalten: {}", name, b.text());
    }
}
