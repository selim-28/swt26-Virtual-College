package swt2.virtualcollege.receive.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Ein Benutzerkonto als Zeile der Tabelle "benutzerkonto". Gespeichert wird
 * ausschliesslich ein gesalzener Hash, nie das Klartext-Passwort.
 */
@Entity
public class Benutzerkonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Die E-Mail ist der Login-Name und darf nur einmal vorkommen.
    @Column(unique = true)
    private String email;

    private String name;
    private String rolle;

    // Format "salt:hash", beides Base64.
    private String passwortHash;

    private Instant erstelltAm;

    // Von JPA benoetigt.
    protected Benutzerkonto() {
    }

    public Benutzerkonto(String email, String name, String rolle,
                         String passwortHash, Instant erstelltAm) {
        this.email = email;
        this.name = name;
        this.rolle = rolle;
        this.passwortHash = passwortHash;
        this.erstelltAm = erstelltAm;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRolle() {
        return rolle;
    }

    public String getPasswortHash() {
        return passwortHash;
    }

    public Instant getErstelltAm() {
        return erstelltAm;
    }
}
