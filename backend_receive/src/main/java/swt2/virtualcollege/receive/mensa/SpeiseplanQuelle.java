package swt2.virtualcollege.receive.mensa;

/*
 * Muster "Adapter" – das TARGET-Interface (Kapitel 13, Aufgabe 2).
 *
 * Das ist die Schnittstelle, die unsere App benutzen WILL: "gib mir einen
 * Speiseplan". Woher die Daten kommen (MQTT, Datenbank, Testdaten), ist
 * der App egal – sie kennt nur diesen Vertrag.
 */
public interface SpeiseplanQuelle {

    // Liefert einen fertigen, sauberen Speiseplan.
    Speiseplan holeSpeiseplan();
}
