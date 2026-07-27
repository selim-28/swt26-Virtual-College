package swt2.virtualcollege.receive.campus;

/**
 * Ein Raum ist in einem Zeitraum belegt. Oberklasse von Reservierung und
 * Veranstaltung.
 */
public class Belegung {

    private final String von;
    private final String bis;

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
