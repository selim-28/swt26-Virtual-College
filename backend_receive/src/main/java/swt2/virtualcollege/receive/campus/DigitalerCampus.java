package swt2.virtualcollege.receive.campus;

import swt2.virtualcollege.receive.nutzer.Nutzerverwaltung;
import swt2.virtualcollege.receive.pinwand.Pinwand;

/*
 * "DigitalerCampus" (Kapitel 13): die WURZEL/FASSADE des Systems.
 * Sie "besteht aus" den drei Subsystemen Karte, Pinwand und Nutzerverwaltung
 * und reicht Anfragen an sie weiter.
 *
 * Im Diagramm:  DigitalerCampus *-- LiveCampusKarte
 *               DigitalerCampus *-- Pinwand
 *               DigitalerCampus *-- Nutzerverwaltung
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
