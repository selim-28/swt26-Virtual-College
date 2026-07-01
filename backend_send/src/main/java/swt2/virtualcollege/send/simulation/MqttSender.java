package swt2.virtualcollege.send.simulation;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/*
 * SCHRITT 2 (MQTT) – der SENDER in backend_send.
 *
 * Stellt mit der Paho-Bibliothek eine echte Verbindung zum Mosquitto-Broker
 * her und VEROEFFENTLICHT (publish) Nachrichten in ein Topic. Genau das ist
 * im Verteilungsdiagramm der Pfeil  "External System --MQTT publish--> Broker".
 *
 * Die Verbindungsdaten kommen aus application.properties (mqtt.broker.url,
 * mqtt.client.id, mqtt.topic). Im Docker-Setup wird mqtt.broker.url per
 * Umgebungsvariable auf tcp://mosquitto:1883 gesetzt.
 */
@Component
public class MqttSender {

    private static final Logger log = LoggerFactory.getLogger(MqttSender.class);

    // @Value holt einen Wert aus der application.properties (bzw. Umgebung).
    @Value("${mqtt.broker.url}")
    private String brokerUrl;
    @Value("${mqtt.client.id}")
    private String clientId;
    @Value("${mqtt.topic}")
    private String topic;

    // Der eigentliche MQTT-Client (Verbindung zum Broker).
    private MqttClient client;

    /*
     * Wird nach dem Start automatisch aufgerufen. Wir verbinden in einem
     * Hintergrund-Thread MIT WIEDERHOLUNG, damit der Start nicht haengt,
     * falls der Broker noch ein paar Sekunden braucht.
     */
    @PostConstruct
    public void starteVerbindung() {
        Thread t = new Thread(this::verbindeMitWiederholung, "mqtt-sender-connect");
        t.setDaemon(true);
        t.start();
    }

    private void verbindeMitWiederholung() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // MemoryPersistence = der Client merkt sich nichts auf der Platte
                // (gut fuer Container).
                client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
                MqttConnectOptions opts = new MqttConnectOptions();
                opts.setAutomaticReconnect(true);  // bei Abbruch automatisch neu verbinden
                opts.setCleanSession(true);
                client.connect(opts);
                log.info("[MQTT-Sender] verbunden mit {} (Topic '{}')", brokerUrl, topic);
                return;
            } catch (Exception e) {
                log.warn("[MQTT-Sender] Broker noch nicht erreichbar ({}). Neuer Versuch in 3s...",
                        e.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /*
     * Veroeffentlicht einen JSON-Text im Topic. "retained = true" sorgt dafuer,
     * dass der Broker die LETZTE Nachricht aufhebt – so bekommt auch ein
     * Empfaenger, der sich spaeter verbindet, sofort den aktuellen Stand.
     */
    public void publish(String json) {
        try {
            if (client == null || !client.isConnected()) {
                log.warn("[MQTT-Sender] noch nicht verbunden – Nachricht verworfen: {}", json);
                return;
            }
            MqttMessage nachricht = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
            nachricht.setQos(1);
            nachricht.setRetained(true);
            client.publish(topic, nachricht);
            log.info("[MQTT-Sender] veroeffentlicht auf '{}': {}", topic, json);
        } catch (Exception e) {
            log.error("[MQTT-Sender] Senden fehlgeschlagen", e);
        }
    }

    @PreDestroy
    public void trenne() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception ignored) {
            // Beim Herunterfahren ignorieren wir Fehler bewusst.
        }
    }
}
