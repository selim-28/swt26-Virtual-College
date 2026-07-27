package swt2.virtualcollege.send.simulation;

/**
 * Factory Method: das Produkt-Interface. Jedes Datenpaket des Fremdsystems
 * (Speiseplan, Auslastung, Abfahrten) kann sich als JSON darstellen.
 */
public interface Fremddatensatz {

    /** Der Datensatz als JSON-Text, so wie er ueber MQTT verschickt wird. */
    String alsJson();
}
