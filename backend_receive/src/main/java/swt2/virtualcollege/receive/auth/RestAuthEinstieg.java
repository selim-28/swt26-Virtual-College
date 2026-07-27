package swt2.virtualcollege.receive.auth;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Antwort auf Anfragen ohne gueltiges Token (401). Liefert eine verstaendliche
 * JSON-Meldung im selben Format wie die FehlerBehandlung.
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
