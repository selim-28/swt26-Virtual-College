package swt2.virtualcollege.receive.campus;

/** Eine Belegung durch eine einzelne Person. */
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
