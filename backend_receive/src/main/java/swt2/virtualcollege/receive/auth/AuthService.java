package swt2.virtualcollege.receive.auth;

import java.time.Instant;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.PostConstruct;

/**
 * Fachlogik der Authentifizierung: Registrieren, Login und Logout.
 * Passwoerter werden ueber den PasswordEncoder (PBKDF2) geprueft, die Sitzung
 * laeuft ueber ein signiertes JWT. Der Logout sperrt das Token in der
 * TokenSperrliste, damit es sofort ungueltig ist.
 */
@Service
public class AuthService {

    private final BenutzerkontoRepository repository;
    private final PasswordEncoder passwortEncoder;
    private final JwtDienst jwt;
    private final TokenSperrliste sperrliste;

    public AuthService(BenutzerkontoRepository repository,
                       PasswordEncoder passwortEncoder,
                       JwtDienst jwt,
                       TokenSperrliste sperrliste) {
        this.repository = repository;
        this.passwortEncoder = passwortEncoder;
        this.jwt = jwt;
        this.sperrliste = sperrliste;
    }

    /** Ergebnis einer Anmeldung: das JWT und das zugehoerige Konto. */
    public record Sitzung(String token, Benutzerkonto konto) {
    }

    /**
     * Legt beim ersten Start je ein Demo-Konto pro Rolle an, damit alle Sichten
     * vorfuehrbar sind. Passwort jeweils "passwort123".
     */
    @PostConstruct
    public void demoKontenAnlegen() {
        legeAnWennNeu("student@example.invalid", "Demo Student", "Student");
        legeAnWennNeu("prof@example.invalid", "Prof. Demo", "Professor");
        legeAnWennNeu("admin@example.invalid", "Demo Admin", "Admin");
    }

    private void legeAnWennNeu(String email, String name, String rolle) {
        if (!repository.existsByEmail(email)) {
            repository.save(new Benutzerkonto(
                    email, name, rolle,
                    passwortEncoder.encode("passwort123"),
                    Instant.now()
            ));
        }
    }

    /** Legt ein neues Konto an und meldet direkt an. */
    public Sitzung registriere(String email, String name, String rolle, String passwort) {
        String e = normalisiere(email);
        if (e.isBlank() || !e.contains("@")) {
            throw fehler("Bitte eine gueltige E-Mail angeben.");
        }
        if (passwort == null || passwort.length() < 6) {
            throw fehler("Das Passwort muss mindestens 6 Zeichen haben.");
        }
        if (repository.existsByEmail(e)) {
            throw fehler("Diese E-Mail ist bereits registriert.");
        }
        String r = (rolle == null || rolle.isBlank()) ? "Student" : rolle.trim();
        String n = (name == null || name.isBlank()) ? e : name.trim();
        Benutzerkonto konto = repository.save(
                new Benutzerkonto(e, n, r, passwortEncoder.encode(passwort), Instant.now()));
        return new Sitzung(jwt.erzeuge(konto), konto);
    }

    /** Prueft die Anmeldedaten und gibt bei Erfolg eine neue Sitzung zurueck. */
    public Sitzung login(String email, String passwort) {
        Benutzerkonto konto = repository.findByEmail(normalisiere(email))
                .orElseThrow(this::loginFehler);
        if (!passwortEncoder.matches(passwort == null ? "" : passwort, konto.getPasswortHash())) {
            throw loginFehler();
        }
        return new Sitzung(jwt.erzeuge(konto), konto);
    }

    /**
     * Meldet ab: sperrt das uebergebene Token bis zu seinem Ablauf, damit es
     * sofort nicht mehr gilt. Ein bereits ungueltiges Token wird still ignoriert.
     */
    public void abmelden(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Claims inhalt = jwt.pruefe(token);
            Date ablauf = inhalt.getExpiration();
            sperrliste.sperre(inhalt.getId(), ablauf == null ? Instant.now() : ablauf.toInstant());
        } catch (JwtException | IllegalArgumentException ungueltig) {
            // Token war ohnehin schon ungueltig.
        }
    }

    private String normalisiere(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private ResponseStatusException fehler(String text) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, text);
    }

    private ResponseStatusException loginFehler() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-Mail oder Passwort ist falsch.");
    }
}
