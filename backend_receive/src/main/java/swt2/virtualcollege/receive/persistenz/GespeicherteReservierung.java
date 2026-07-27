package swt2.virtualcollege.receive.persistenz;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Persistente Raumreservierung eines Nutzers. */
@Entity
public class GespeicherteReservierung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String raumId;
    private String raumName;

    private String reserviertVon;
    private String von;
    private String bis;
    private String status;
    private Instant erstelltAm;

    // Von JPA benoetigt.
    protected GespeicherteReservierung() {
    }

    public GespeicherteReservierung(String raumId, String raumName, String reserviertVon,
                                    String von, String bis, String status, Instant erstelltAm) {
        this.raumId = raumId;
        this.raumName = raumName;
        this.reserviertVon = reserviertVon;
        this.von = von;
        this.bis = bis;
        this.status = status;
        this.erstelltAm = erstelltAm;
    }

    public Long getId() {
        return id;
    }

    public String getRaumId() {
        return raumId;
    }

    public String getRaumName() {
        return raumName;
    }

    public String getReserviertVon() {
        return reserviertVon;
    }

    public String getVon() {
        return von;
    }

    public String getBis() {
        return bis;
    }

    public String getStatus() {
        return status;
    }

    public Instant getErstelltAm() {
        return erstelltAm;
    }
}
