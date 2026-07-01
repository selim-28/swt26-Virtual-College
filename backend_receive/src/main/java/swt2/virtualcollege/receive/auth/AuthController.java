package swt2.virtualcollege.receive.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * AUTHENTIFIZIERUNG – die REST-Schnittstelle (die "Tuer" fuer das Frontend).
 *
 * Endpunkte (alle unter /api/auth):
 *   POST /registrieren  -> neues Konto anlegen (und direkt einloggen)
 *   POST /login         -> einloggen, Token erhalten
 *   GET  /ich           -> "wer bin ich?" (mit Token im Header X-Auth-Token)
 *   POST /logout        -> Token ungueltig machen
 *
 * Das Frontend speichert das Token nach dem Login und schickt es bei
 * geschuetzten Aufrufen im Header "X-Auth-Token" mit.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/registrieren")
    public NutzerAntwort registrieren(@RequestBody RegistrierAnfrage anfrage) {
        Benutzerkonto konto = service.registriere(
                anfrage.email(), anfrage.name(), anfrage.rolle(), anfrage.passwort());
        // Nach dem Registrieren gleich automatisch einloggen.
        String token = service.login(anfrage.email(), anfrage.passwort());
        return new NutzerAntwort(token, konto.getEmail(), konto.getName(), konto.getRolle());
    }

    @PostMapping("/login")
    public NutzerAntwort login(@RequestBody LoginAnfrage anfrage) {
        String token = service.login(anfrage.email(), anfrage.passwort());
        Benutzerkonto konto = service.ausToken(token);
        return new NutzerAntwort(token, konto.getEmail(), konto.getName(), konto.getRolle());
    }

    @GetMapping("/ich")
    public NutzerAntwort ich(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        Benutzerkonto konto = service.ausToken(token);
        return new NutzerAntwort(token, konto.getEmail(), konto.getName(), konto.getRolle());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        service.logout(token);
    }

    // ---- Anfrage-/Antwort-Formate (JSON) ----

    public record RegistrierAnfrage(String email, String name, String rolle, String passwort) {
    }

    public record LoginAnfrage(String email, String passwort) {
    }

    // Antwort enthaelt NIE das Passwort/den Hash – nur, was das Frontend braucht.
    public record NutzerAntwort(String token, String email, String name, String rolle) {
    }
}
