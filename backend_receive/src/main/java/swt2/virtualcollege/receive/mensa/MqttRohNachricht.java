package swt2.virtualcollege.receive.mensa;

/**
 * Adapter-Muster: das Adaptee. Die rohe MQTT-Nachricht, so wie sie hereinkommt,
 * bevor der MqttDatenAdapter sie in Fachobjekte uebersetzt.
 */
public record MqttRohNachricht(String payloadJson) {
}
