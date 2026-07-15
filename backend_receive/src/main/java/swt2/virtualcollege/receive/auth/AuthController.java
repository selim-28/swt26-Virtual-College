package swt2.virtualcollege.receive.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 *   POST /login         -> einloggen, JWT-Token erhalten
 *   GET  /ich           -> "wer bin ich?" (Token im Header Authorization: Bearer)
 *   POST /logout        -> aktuelles Token sperren
 *
 * PRODUKTIONS-VARIANTE: Login/Registrieren geben einen signierten JWT-Ausweis
 * zurueck. Das Frontend speichert ihn und schickt ihn bei jeder geschuetzten
 * Anfrage im Header "Authorization: Bearer <token>" mit. Ob eine Anfrage
 * durchgelassen wird, entscheidet Spring Security (siehe SecurityConfig).
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
        AuthService.Sitzung sitzung = service.registriere(
                anfrage.email(), anfrage.name(), anfrage.rolle(), anfrage.passwort());
        return antwort(sitzung);
    }

    @PostMapping("/login")
    public NutzerAntwort login(@RequestBody LoginAnfrage anfrage) {
        AuthService.Sitzung sitzung = service.login(anfrage.email(), anfrage.passwort());
        return antwort(sitzung);
    }

    /*
     * "Wer bin ich?" – Der JwtAuthFilter hat das Token bereits geprueft und den
     * angemeldeten Nutzer bereitgestellt. Wir geben das mitgeschickte Token
     * unveraendert zurueck, damit das Frontend seinen gespeicherten Ausweis behaelt.
     */
    @GetMapping("/ich")
    public NutzerAntwort ich(@AuthenticationPrincipal AngemeldeterNutzer nutzer,
                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return new NutzerAntwort(bearerToken(authHeader), nutzer.email(), nutzer.name(), nutzer.rolle());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        service.abmelden(bearerToken(authHeader));
    }

    // ---- Hilfsfunktionen ----

    private NutzerAntwort antwort(AuthService.Sitzung sitzung) {
        Benutzerkonto k = sitzung.konto();
        return new NutzerAntwort(sitzung.token(), k.getEmail(), k.getName(), k.getRolle());
    }

    /** Holt das reine Token aus dem Header "Authorization: Bearer <token>". */
    private String bearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
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
