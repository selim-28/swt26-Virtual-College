package swt2.virtualcollege.receive.auth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Token Revocation List: merkt sich die IDs abgemeldeter Token bis zu deren
 * Ablauf. Ohne sie waere ein JWT nach dem Logout weiterhin gueltig. Da nur bis
 * zum Ablauf gespeichert wird, raeumt sich die Liste selbst auf.
 */
@Component
public class TokenSperrliste {

    // jti -> Ablaufzeitpunkt des Tokens.
    private final Map<String, Instant> gesperrt = new ConcurrentHashMap<>();

    /** Sperrt ein Token bis zu seinem regulaeren Ablauf. */
    public void sperre(String jti, Instant ablauf) {
        if (jti != null && ablauf != null) {
            gesperrt.put(jti, ablauf);
        }
        aufraeumen();
    }

    /** true, wenn das Token gesperrt und noch nicht abgelaufen ist. */
    public boolean istGesperrt(String jti) {
        if (jti == null) {
            return false;
        }
        Instant ablauf = gesperrt.get(jti);
        if (ablauf == null) {
            return false;
        }
        if (ablauf.isBefore(Instant.now())) {
            gesperrt.remove(jti);
            return false;
        }
        return true;
    }

    /** Entfernt abgelaufene Eintraege, damit die Liste nicht unbegrenzt waechst. */
    private void aufraeumen() {
        Instant jetzt = Instant.now();
        gesperrt.entrySet().removeIf(eintrag -> eintrag.getValue().isBefore(jetzt));
    }
}
