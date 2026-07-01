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

/*
 * SCHRITT 2 (MQTT) – der EMPFAENGER in backend_receive.
 *
 * Verbindet sich mit dem Mosquitto-Broker und ABONNIERT (subscribe) das Topic.
 * Im Verteilungsdiagramm ist das der Pfeil "Broker --MQTT subscribe--> App".
 *
 * Wenn eine Speiseplan-Nachricht ankommt, benutzt er das ADAPTER-Muster:
 * MqttRohNachricht  ->  MqttDatenAdapter  ->  sauberer Speiseplan, der dann
 * im SpeiseplanSpeicher landet.
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

    // Hierhin legen wir den empfangenen Speiseplan (gemeinsame Bean).
    private final SpeiseplanSpeicher speicher;

    // Schritt 3: speichert den empfangenen Speiseplan zusaetzlich dauerhaft.
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
                client.setCallback(this);          // "ich" reagiere auf Nachrichten
                MqttConnectOptions opts = new MqttConnectOptions();
                opts.setAutomaticReconnect(true);
                opts.setCleanSession(true);
                client.connect(opts);
                client.subscribe(topic);            // Topic abonnieren
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

    /*
     * Wird vom Paho-Client aufgerufen, sobald eine Nachricht ankommt.
     * Speiseplan-Nachrichten laufen durch den Adapter; Abfahrts-Nachrichten
     * werden an den fachlich getrennten AbfahrtsSpeicher delegiert.
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        log.info("[MQTT-Empfaenger] Nachricht erhalten: {}", payload);
        // Nachrichtentyp erkennen und an das passende Vertical Slice delegieren.
        if (payload.contains("\"typ\":\"speiseplan\"")) {
            // ADAPTER-Muster: rohe Nachricht -> sauberer Speiseplan.
            MqttDatenAdapter adapter = new MqttDatenAdapter(new MqttRohNachricht(payload));
            Speiseplan speiseplan = adapter.holeSpeiseplan();
            speicher.speichere(speiseplan);
            // Schritt 3: zusaetzlich dauerhaft in der Datenbank ablegen.
            archiv.speichere(speiseplan);
            log.info("[MQTT-Empfaenger] Speiseplan aktualisiert ({} Gerichte)",
                    speiseplan.gerichte().size());
        } else if (payload.contains("\"typ\":\"abfahrt\"")) {
            abfahrtsSpeicher.aktualisiereAusJson(payload);
            log.info("[MQTT-Empfaenger] OEPNV-Abfahrten aktualisiert");
        }
    }

    // Muss vorhanden sein (Teil des MqttCallback-Vertrags), hier nichts zu tun.
    @Override
    public void connectionLost(Throwable cause) {
        log.warn("[MQTT-Empfaenger] Verbindung verloren: {}", cause == null ? "?" : cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Nur fuer Sender relevant – hier leer.
    }

    @PreDestroy
    public void trenne() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception ignored) {
            // Beim Herunterfahren bewusst ignorieren.
        }
    }
}
