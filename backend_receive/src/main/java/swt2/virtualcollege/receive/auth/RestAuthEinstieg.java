package swt2.virtualcollege.receive.auth;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * AUTHENTIFIZIERUNG (PRODUKTIONS-VARIANTE) – Antwort bei FEHLENDEM Login (401).
 *
 * Spring Security bricht Anfragen ohne gueltiges Token schon VOR den Controllern
 * ab. Damit das Frontend trotzdem eine saubere deutsche JSON-Meldung sieht
 * (statt einer leeren 401-Seite), schreiben wir hier { "status": 401, ... }.
 * Das Format ist identisch zur bestehenden FehlerBehandlung.
 */
@Component
public class RestAuthEinstieg implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest anfrage,
                         HttpServletResponse antwort,
                         AuthenticationException ausnahme) throws IOException {
        JsonFehler.schreibe(antwort, HttpStatus.UNAUTHORIZED,
                "Bitte melde dich an, um diese Funktion zu nutzen.");
    }
}
