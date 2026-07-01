package swt2.virtualcollege.send.simulation;

/*
 * Muster "Factory Method" – das PRODUKT-Interface (Kapitel 13, Aufgabe 2).
 *
 * Ein "Fremddatensatz" ist irgendein Datenpaket, das das Fremdsystem
 * erzeugt und spaeter per MQTT verschickt (Speiseplan, Auslastung,
 * Abfahrten ...). Alle Datenpakete koennen sich "alsJson()" beschreiben,
 * damit man sie als Text ueber MQTT versenden kann.
 *
 * "interface" = ein VERTRAG: jede Klasse, die "implements Fremddatensatz"
 * schreibt, MUSS die Methode alsJson() anbieten.
 */
public interface Fremddatensatz {

    // Liefert den Datensatz als JSON-Text (so wird er ueber MQTT verschickt).
    String alsJson();
}
