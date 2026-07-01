package swt2.virtualcollege.receive.auth;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;

/*
 * AUTHENTIFIZIERUNG – die Fachlogik (Registrieren, Login, Logout).
 *
 * Ablauf:
 *  - registriere(): legt ein neues Benutzerkonto an (Passwort wird gehasht).
 *  - login(): prueft E-Mail + Passwort und gibt ein zufaelliges "Token" zurueck.
 *  - ausToken(): findet zu einem Token das eingeloggte Konto.
 *  - logout(): macht ein Token ungueltig.
 *
 * Das Token ist wie ein "Garderoben-Zettel": Nach dem Login bekommt der
 * Browser ein Token und zeigt es bei jeder Anfrage vor. Wir merken uns die
 * gueltigen Tokens hier im Arbeitsspeicher (Map token -> email).
 *
 * Hinweis (ehrlich): Das ist eine schlanke Demo-Authentifizierung fuer das
 * Praktikum. Passwoerter sind sicher gehasht, aber Tokens liegen nur im
 * Speicher und laufen nicht ab. Fuer den Produktivbetrieb wuerde man z. B.
 * Spring Security mit JWT/Sessions nehmen.
 */
@Service
public class AuthService {

    private final BenutzerkontoRepository repository;

    // Gueltige Tokens:  token -> email.  Beim Neustart leer (man muss sich neu einloggen).
    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public AuthService(BenutzerkontoRepository repository) {
        this.repository = repository;
    }

    /*
     * Legt beim ersten Start ein Demo-Konto an, damit man sich sofort
     * einloggen kann (praktisch fuer die Praesentation).
     * Login:  student@fh-dortmund.de  /  passwort123
     */
    @PostConstruct
    public void demoKontoAnlegen() {
        if (!repository.existsByEmail("student@fh-dortmund.de")) {
            repository.save(new Benutzerkonto(
                    "student@fh-dortmund.de",
                    "Demo Student",
                    "Student",
                    Passwort.hash("passwort123"),
                    Instant.now()
            ));
        }
    }

    public Benutzerkonto registriere(String email, String name, String rolle, String passwort) {
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
        return repository.save(new Benutzerkonto(e, n, r, Passwort.hash(passwort), Instant.now()));
    }

    /** Prueft Login-Daten und gibt bei Erfolg ein neues Token zurueck. */
    public String login(String email, String passwort) {
        Benutzerkonto konto = repository.findByEmail(normalisiere(email))
                .orElseThrow(this::loginFehler);
        if (!Passwort.pruefe(passwort, konto.getPasswortHash())) {
            throw loginFehler();
        }
        String token = UUID.randomUUID().toString();
        tokens.put(token, konto.getEmail());
        return token;
    }

    /** Findet das eingeloggte Konto zu einem Token (oder 401, wenn ungueltig). */
    public Benutzerkonto ausToken(String token) {
        if (token == null || token.isBlank()) {
            throw loginFehler();
        }
        String email = tokens.get(token);
        if (email == null) {
            throw loginFehler();
        }
        return repository.findByEmail(email).orElseThrow(this::loginFehler);
    }

    public void logout(String token) {
        if (token != null) {
            tokens.remove(token);
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
