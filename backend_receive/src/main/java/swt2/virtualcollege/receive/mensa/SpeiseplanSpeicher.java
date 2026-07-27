package swt2.virtualcollege.receive.mensa;

import org.springframework.stereotype.Component;

/**
 * Zwischenspeicher fuer den zuletzt ueber MQTT empfangenen Speiseplan.
 * Empfaenger und Controller teilen sich dieselbe Bean; "volatile" macht die
 * Aenderung ueber Thread-Grenzen hinweg sofort sichtbar.
 */
@Component
public class SpeiseplanSpeicher {

    private volatile Speiseplan letzterSpeiseplan;

    public void speichere(Speiseplan speiseplan) {
        this.letzterSpeiseplan = speiseplan;
    }

    public Speiseplan getLetzterSpeiseplan() {
        return letzterSpeiseplan;
    }

    public boolean hatDaten() {
        return letzterSpeiseplan != null;
    }
}
