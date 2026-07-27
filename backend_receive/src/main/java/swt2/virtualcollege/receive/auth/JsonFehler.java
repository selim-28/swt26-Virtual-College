package swt2.virtualcollege.receive.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Schreibt einen Fehler im Format { "status": ..., "message": ... } direkt in die
 * HTTP-Antwort. Noetig fuer die Security-Handler, die ausserhalb der Controller
 * laufen und den @RestControllerAdvice nicht erreichen.
 */
final class JsonFehler {

    private JsonFehler() {
    }

    static void schreibe(HttpServletResponse antwort, HttpStatus status, String meldung) throws IOException {
        antwort.setStatus(status.value());
        antwort.setContentType(MediaType.APPLICATION_JSON_VALUE);
        antwort.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Anfuehrungszeichen ersetzen, damit gueltiges JSON entsteht.
        String sicher = meldung == null ? "" : meldung.replace("\"", "'");
        antwort.getWriter().write(
                "{\"status\":" + status.value() + ",\"message\":\"" + sicher + "\"}");
    }
}
