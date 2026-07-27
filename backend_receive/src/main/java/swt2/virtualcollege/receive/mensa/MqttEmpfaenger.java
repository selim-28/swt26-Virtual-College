package swt2.virtualcollege.receive.mensa;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
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
 * Abonniert das MQTT-Topic und bildet damit im Verteilungsdiagramm den Pfeil
 * "Broker --MQTT subscribe--> App" ab. Eingehende Speiseplaene laufen ueber den
 * MqttDatenAdapter, Abfahrten ueber den AbfahrtsSpeicher.
 */
@Component
public class MqttEmpfaenger implements MqttCallback {

    private static final Logger log = LoggerFactory.getLogger(MqttEmpfaenger.class);

    @Value("${mqtt.broker.url}")
    private String brokerUrl;
    @Value("${mqtt.client.id}")
    private String clientId;
    @Value("${mqtt.topic}")
    private String topic;

    private final SpeiseplanSpeicher speicher;

    private final swt2.virtualcollege.receive.persistenz.SpeisearchivService archiv;
    private final swt2.virtualcollege.receive.transit.AbfahrtsSpeicher abfahrtsSpeicher;

    private MqttClient client;

    public MqttEmpfaenger(SpeiseplanSpeicher speicher,
                          swt2.virtualcollege.receive.persistenz.SpeisearchivService archiv,
                          swt2.virtualcollege.receive.transit.AbfahrtsSpeicher abfahrtsSpeicher) {
        this.speicher = speicher;
        this.archiv = archiv;
        this.abfahrtsSpeicher = abfahrtsSpeicher;
    }

    /**
     * Verbindet nach dem Start in einem Hintergrund-Thread mit Wiederholung,
     * damit der Start nicht blockiert, wenn der Broker noch nicht bereit ist.
     */
    @PostConstruct
    public void starteVerbindung() {
        Thread t = new Thread(this::verbindeUndAbonniere, "mqtt-empfaenger-connect");
        t.setDaemon(true);
        t.start();
    }

    private void verbindeUndAbonniere() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
                client.setCallback(this);
                MqttConnectOptions opts = new MqttConnectOptions();
                opts.setAutomaticReconnect(true);
                opts.setCleanSession(true);
                client.connect(opts);
                client.subscribe(topic);
                log.info("[MQTT-Empfaenger] verbunden mit {} und Topic '{}' abonniert",
                        brokerUrl, topic);
                return;
            } catch (Exception e) {
                log.warn("[MQTT-Empfaenger] Broker noch nicht erreichbar ({}). Neuer Versuch in 3s...",
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
     * Callback des Paho-Clients: erkennt den Nachrichtentyp und delegiert an das
     * zustaendige Fachmodul.
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        log.info("[MQTT-Empfaenger] Nachricht erhalten: {}", payload);
        if (payload.contains("\"typ\":\"speiseplan\"")) {
            MqttDatenAdapter adapter = new MqttDatenAdapter(new MqttRohNachricht(payload));
            Speiseplan speiseplan = adapter.holeSpeiseplan();
            speicher.speichere(speiseplan);
            archiv.speichere(speiseplan);
            log.info("[MQTT-Empfaenger] Speiseplan aktualisiert ({} Gerichte)",
                    speiseplan.gerichte().size());
        } else if (payload.contains("\"typ\":\"abfahrt\"")) {
            abfahrtsSpeicher.aktualisiereAusJson(payload);
            log.info("[MQTT-Empfaenger] OEPNV-Abfahrten aktualisiert");
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("[MQTT-Empfaenger] Verbindung verloren: {}", cause == null ? "?" : cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Nur fuer Sender relevant.
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
