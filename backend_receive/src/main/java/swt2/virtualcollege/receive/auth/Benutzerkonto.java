package swt2.virtualcollege.receive.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/*
 * AUTHENTIFIZIERUNG – ein BENUTZERKONTO (= eine Tabellenzeile).
 *
 * @Entity sagt Spring/Hibernate: "fuer diese Klasse gibt es eine Tabelle".
 * Jedes Konto ist eine Zeile in der Tabelle "benutzerkonto".
 *
 * WICHTIG: Wir speichern NIE das Klartext-Passwort, sondern nur einen
 * gesalzenen Hash (siehe Klasse Passwort). Selbst wenn jemand die Datenbank
 * liest, kann er die Passwoerter nicht zurueckrechnen.
 */
@Entity
public class Benutzerkonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // E-Mail = der Login-Name. unique = es darf sie nur EINMAL geben.
    @Column(unique = true)
    private String email;

    private String name;
    private String rolle;          // Student / Betreuer / Verwaltungsangestellter

    // Format: "salt:hash" (beides Base64). Niemals das Klartext-Passwort!
    private String passwortHash;

    private Instant erstelltAm;

    // Leerer Konstruktor (von JPA benoetigt).
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
