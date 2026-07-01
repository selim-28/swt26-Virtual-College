package swt2.virtualcollege.receive.persistenz;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/*
 * Persistente Raumreservierung.
 *
 * Das macht den Live-Campus praktisch nutzbar: Ein Student kann im
 * Frontend einen Raum reservieren, und die Reservierung wird in der
 * Datenbank gespeichert. Das ist bewusst minimal, aber schon ein echter
 * End-to-End-Use-Case.
 */
@Entity
public class GespeicherteReservierung {

    // Automatische Datenbank-ID.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Raum und Anzeigeinformationen.
    private String raumId;
    private String raumName;

    // Reservierungsdaten.
    private String reserviertVon;
    private String von;
    private String bis;
    private String status;
    private Instant erstelltAm;

    // Leerer Konstruktor fuer JPA.
    protected GespeicherteReservierung() {
    }

    // Fachlicher Konstruktor fuer neue Reservierungen.
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
