package swt2.virtualcollege.receive.campus;

/*
 * "Reservierung" (Kapitel 13): eine Belegung durch eine Person.
 * "ist eine" Belegung.  Im Diagramm:  Belegung <|-- Reservierung
 */
public class Reservierung extends Belegung {

    private final String reserviertVon;

    public Reservierung(String von, String bis, String reserviertVon) {
        super(von, bis);
        this.reserviertVon = reserviertVon;
    }

    public String getReserviertVon() {
        return reserviertVon;
    }
}
