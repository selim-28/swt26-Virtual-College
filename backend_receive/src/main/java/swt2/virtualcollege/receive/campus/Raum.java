package swt2.virtualcollege.receive.campus;

import java.util.ArrayList;
import java.util.List;

/*
 * "Raum" (Kapitel 13): ein Raum hat Koordinaten und kann Belegungen haben.
 * Im Diagramm:  Raum --> Koordinaten,  Raum *-- Belegung
 */
public class Raum {

    private final String name;            // z. B. "B.1.23"
    private final Koordinaten koordinaten;
    private final List<Belegung> belegungen = new ArrayList<>();

    public Raum(String name, Koordinaten koordinaten) {
        this.name = name;
        this.koordinaten = koordinaten;
    }

    public String getName() {
        return name;
    }

    public Koordinaten getKoordinaten() {
        return koordinaten;
    }

    public void addBelegung(Belegung b) {
        belegungen.add(b);
    }

    public List<Belegung> getBelegungen() {
        return belegungen;
    }
}
