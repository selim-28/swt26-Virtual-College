package swt2.virtualcollege.receive.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Schnittstelle der Authentifizierung unter /api/auth:
 * registrieren, login, ich und logout. Login und Registrierung liefern ein JWT,
 * das das Frontend als "Authorization: Bearer &lt;token&gt;" mitschickt.
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

    /**
     * Liefert den angemeldeten Nutzer. Der JwtAuthFilter hat das Token bereits
     * geprueft; es wird unveraendert zurueckgegeben, damit das Frontend es behaelt.
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

    public record RegistrierAnfrage(String email, String name, String rolle, String passwort) {
    }

    public record LoginAnfrage(String email, String passwort) {
    }

    /** Antwort an das Frontend; enthaelt bewusst weder Passwort noch Hash. */
    public record NutzerAntwort(String token, String email, String name, String rolle) {
    }
}
