package swt2.virtualcollege.receive.mensa;

/*
 * Muster "Adapter" – das ADAPTEE (das "inkompatible" Objekt, Kapitel 13).
 *
 * So kommen die Daten ueber MQTT herein: ein roher JSON-Text. Damit kann
 * unsere App noch nichts anfangen – deshalb uebersetzt der MqttDatenAdapter
 * diesen Text in saubere Speiseplan-Objekte.
 */
public record MqttRohNachricht(String payloadJson) {
}
