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

/**
 * Publiziert Nachrichten im MQTT-Topic und bildet damit im Verteilungsdiagramm
 * den Pfeil "External System --MQTT publish--> Broker" ab.
 * Die Verbindungsdaten stammen aus application.properties.
 */
@Component
public class MqttSender {

    private static final Logger log = LoggerFactory.getLogger(MqttSender.class);

    @Value("${mqtt.broker.url}")
    private String brokerUrl;
    @Value("${mqtt.client.id}")
    private String clientId;
    @Value("${mqtt.topic}")
    private String topic;

    private MqttClient client;

    /**
     * Verbindet nach dem Start in einem Hintergrund-Thread mit Wiederholung,
     * damit der Start nicht blockiert, wenn der Broker noch nicht bereit ist.
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
                // MemoryPersistence: keine Zwischenspeicherung auf der Platte (Container).
                client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
                MqttConnectOptions opts = new MqttConnectOptions();
                opts.setAutomaticReconnect(true);
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

    /**
     * Veroeffentlicht einen JSON-Text im Topic. Die Nachricht wird "retained"
     * gesendet, damit auch spaeter verbundene Empfaenger sofort den aktuellen
     * Stand erhalten.
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
            // Fehler beim Herunterfahren sind unerheblich.
        }
    }
}
