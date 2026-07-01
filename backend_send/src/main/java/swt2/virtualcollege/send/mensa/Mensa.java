package swt2.virtualcollege.send.mensa;

import java.util.List;

/*
 * Fachklasse "Mensa" aus dem OOD-Klassendiagramm von backend_send (Kapitel 13).
 * Sie kennt den "echten" Speiseplan des Studierendenwerks.
 *
 * Im Diagramm:  Mensa --> Speiseplan   (die Mensa liefert einen Speiseplan)
 * Spaeter liest der SpeiseplanGenerator diesen Speiseplan aus und verschickt
 * ihn per MQTT (Factory-Muster).
 */
public class Mensa {

    /*
     * Liefert den aktuellen Speiseplan.
     * (Hier mit Beispiel-Daten. In einer echten App kaeme er aus einer
     *  Datenbank oder einer echten Schnittstelle des Studierendenwerks.)
     */
    public Speiseplan getSpeiseplan() {
        return new Speiseplan(List.of(
                new Gericht("Spaghetti Bolognese", 3.50),
                new Gericht("Gemuesecurry", 2.90)
        ));
    }
}
