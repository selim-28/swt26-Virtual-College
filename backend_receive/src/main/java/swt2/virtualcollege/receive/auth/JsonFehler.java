package swt2.virtualcollege.receive.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;

/*
 * Kleine Hilfe: schreibt einen Fehler als JSON direkt in die HTTP-Antwort –
 * im selben Format wie die bestehende FehlerBehandlung:
 *   { "status": 401, "message": "..." }
 *
 * Wird von den Security-Handlern (401/403) benutzt, die AUSSERHALB der normalen
 * Controller laufen und deshalb den @RestControllerAdvice nicht erreichen.
 */
final class JsonFehler {

    private JsonFehler() {
    }

    static void schreibe(HttpServletResponse antwort, HttpStatus status, String meldung) throws IOException {
        antwort.setStatus(status.value());
        antwort.setContentType(MediaType.APPLICATION_JSON_VALUE);
        antwort.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Anfuehrungszeichen in der Meldung entschaerfen, damit gueltiges JSON entsteht.
        String sicher = meldung == null ? "" : meldung.replace("\"", "'");
        antwort.getWriter().write(
                "{\"status\":" + status.value() + ",\"message\":\"" + sicher + "\"}");
    }
}
