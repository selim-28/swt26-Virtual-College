package swt2.virtualcollege.receive.nutzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swt2.virtualcollege.receive.pinwand.Benachrichtigung;
import swt2.virtualcollege.receive.pinwand.Beobachter;

/*
 * "Nutzer" (Kapitel 13). WICHTIG fuer das Observer-Muster:
 * Nutzer "implements Beobachter" – also ist jeder Nutzer ein moeglicher
 * Empfaenger von Pinwand-Benachrichtigungen.
 *
 * Im Diagramm:  Beobachter <|.. Nutzer
 *               Nutzer <|-- Student / Betreuer / Verwaltungsangestellter
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

    /*
     * Wird von der Pinwand (Subjekt) aufgerufen. In einer echten App wuerde
     * der Nutzer hier z. B. eine Push-Nachricht aufs Handy bekommen.
     */
    @Override
    public void aktualisiere(Benachrichtigung b) {
        log.info("[Nutzer {}] Benachrichtigung erhalten: {}", name, b.text());
    }
}
