package swt2.virtualcollege.receive.campus;

/*
 * "Belegung" (Kapitel 13): ein Raum ist zu einer Zeit belegt.
 * Oberklasse fuer die zwei Spezialfaelle Reservierung und Veranstaltung.
 * Im Diagramm:  Belegung <|-- Reservierung,  Belegung <|-- Veranstaltung
 */
public class Belegung {

    private final String von;   // z. B. "10:00"
    private final String bis;   // z. B. "12:00"

    public Belegung(String von, String bis) {
        this.von = von;
        this.bis = bis;
    }

    public String getVon() {
        return von;
    }

    public String getBis() {
        return bis;
    }
}
