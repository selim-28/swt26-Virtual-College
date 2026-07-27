package swt2.virtualcollege.send.mensa;

import java.util.List;

/**
 * Fachklasse "Mensa" aus dem Klassendiagramm von backend_send.
 * Liefert den Speiseplan, den der SpeiseplanGenerator spaeter per MQTT verschickt.
 */
public class Mensa {

    /**
     * Aktueller Speiseplan. Die Daten sind fest hinterlegt; produktiv kaemen sie
     * aus einer Schnittstelle des Studierendenwerks.
     */
    public Speiseplan getSpeiseplan() {
        return new Speiseplan(List.of(
                new Gericht("Spaghetti Bolognese", 3.50),
                new Gericht("Gemuesecurry", 2.90)
        ));
    }
}
