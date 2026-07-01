package swt2.virtualcollege.receive.auth;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/*
 * Sorgt dafuer, dass Fehler (z. B. falsches Passwort) als sauberes JSON mit
 * KLARTEXT-Meldung beim Frontend ankommen, z. B.:
 *   { "status": 401, "message": "E-Mail oder Passwort ist falsch." }
 *
 * Ohne das wuerde das Frontend nur einen nackten "401" sehen. So kann es die
 * verstaendliche deutsche Meldung direkt anzeigen.
 */
@RestControllerAdvice
public class FehlerBehandlung {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> behandle(ResponseStatusException ex) {
        Map<String, Object> koerper = new LinkedHashMap<>();
        koerper.put("status", ex.getStatusCode().value());
        koerper.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(koerper);
    }
}
