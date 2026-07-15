package swt2.virtualcollege.receive.auth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/*
 * AUTHENTIFIZIERUNG (PRODUKTIONS-VARIANTE) – die Logout-Sperrliste ("Denylist").
 *
 * JWT-Token sind "stateless": Sie gelten bis zu ihrem Ablaufdatum, egal was das
 * Backend tut. Beim Logout wollen wir ein Token aber SOFORT ungueltig machen –
 * sonst waere die Akzeptanz-Anforderung "geschuetzte Funktionen sind nach dem
 * Abmelden nicht mehr erreichbar" nicht erfuellt.
 *
 * Loesung: Wir merken uns die IDs (jti) abgemeldeter Tokens bis zu ihrem
 * ohnehin baldigen Ablauf. Der Sicherheitsfilter lehnt jedes gesperrte Token ab.
 * Weil wir nur bis zum Ablauf speichern, bleibt die Liste klein und raeumt sich
 * selbst auf. (Das ist das Standard-Muster "Token Revocation List".)
 */
@Component
public class TokenSperrliste {

    // jti -> Ablaufzeitpunkt des Tokens. Nach Ablauf darf der Eintrag verschwinden.
    private final Map<String, Instant> gesperrt = new ConcurrentHashMap<>();

    /** Sperrt ein Token bis zu seinem regulaeren Ablauf. */
    public void sperre(String jti, Instant ablauf) {
        if (jti != null && ablauf != null) {
            gesperrt.put(jti, ablauf);
        }
        aufraeumen();
    }

    /** true, wenn dieses Token abgemeldet (gesperrt) wurde und noch nicht abgelaufen ist. */
    public boolean istGesperrt(String jti) {
        if (jti == null) {
            return false;
        }
        Instant ablauf = gesperrt.get(jti);
        if (ablauf == null) {
            return false;
        }
        if (ablauf.isBefore(Instant.now())) {
            gesperrt.remove(jti);   // schon abgelaufen -> Eintrag nicht mehr noetig
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
