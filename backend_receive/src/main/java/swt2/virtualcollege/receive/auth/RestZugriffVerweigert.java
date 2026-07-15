package swt2.virtualcollege.receive.auth;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * AUTHENTIFIZIERUNG (PRODUKTIONS-VARIANTE) – Antwort bei fehlender BERECHTIGUNG (403).
 *
 * Der Nutzer IST angemeldet, darf aber diese Funktion nicht (z. B. eine
 * rollengeschuetzte Aktion). Wir liefern eine saubere deutsche JSON-Meldung.
 */
@Component
public class RestZugriffVerweigert implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest anfrage,
                       HttpServletResponse antwort,
                       AccessDeniedException ausnahme) throws IOException {
        JsonFehler.schreibe(antwort, HttpStatus.FORBIDDEN,
                "Fuer diese Funktion fehlt dir die Berechtigung.");
    }
}
