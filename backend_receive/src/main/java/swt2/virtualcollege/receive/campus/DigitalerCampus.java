package swt2.virtualcollege.receive.campus;

import swt2.virtualcollege.receive.nutzer.Nutzerverwaltung;
import swt2.virtualcollege.receive.pinwand.Pinwand;

/**
 * Fassade des Systems: buendelt die Subsysteme Karte, Pinwand und
 * Nutzerverwaltung hinter einem gemeinsamen Einstiegspunkt.
 */
public class DigitalerCampus {

    private final LiveCampusKarte karte = new LiveCampusKarte();
    private final Pinwand pinwand = new Pinwand();
    private final Nutzerverwaltung nutzerverwaltung = new Nutzerverwaltung();

    public LiveCampusKarte getKarte() {
        return karte;
    }

    public Pinwand getPinwand() {
        return pinwand;
    }

    public Nutzerverwaltung getNutzerverwaltung() {
        return nutzerverwaltung;
    }
}
