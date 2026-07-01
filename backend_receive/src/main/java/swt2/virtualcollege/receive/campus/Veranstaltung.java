package swt2.virtualcollege.receive.campus;

import java.util.ArrayList;
import java.util.List;

/*
 * "Veranstaltung" (Kapitel 13): eine Belegung durch eine Lehrveranstaltung,
 * an der sich Teilnehmer anmelden koennen. "ist eine" Belegung.
 * Im Diagramm:  Belegung <|-- Veranstaltung
 */
public class Veranstaltung extends Belegung {

    private final String titel;
    private final List<String> teilnehmer = new ArrayList<>();

    public Veranstaltung(String von, String bis, String titel) {
        super(von, bis);
        this.titel = titel;
    }

    public String getTitel() {
        return titel;
    }

    public void anmelden(String teilnehmerName) {
        teilnehmer.add(teilnehmerName);
    }

    public List<String> getTeilnehmer() {
        return teilnehmer;
    }
}
