package swt2.virtualcollege.receive.mensa;

import org.springframework.stereotype.Component;

/*
 * SCHRITT 2 (MQTT) – ein einfacher Zwischenspeicher.
 *
 * Der MQTT-Empfaenger legt hier den zuletzt empfangenen Speiseplan ab,
 * und der MensaController liest ihn wieder heraus. So sehen wir im Browser,
 * ob die Daten WIRKLICH ueber MQTT angekommen sind.
 *
 * @Component = Spring verwaltet genau EIN Exemplar (Singleton-Bean), das
 * beide Seiten (Empfaenger + Controller) gemeinsam benutzen.
 * "volatile" sorgt dafuer, dass der Wert ueber Thread-Grenzen hinweg
 * sofort sichtbar ist (der Empfaenger laeuft in einem anderen Thread).
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
