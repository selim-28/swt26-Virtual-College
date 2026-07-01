package swt2.virtualcollege.receive.mensa;

import java.util.ArrayList;
import java.util.List;

// Jackson ist eine fertige Bibliothek zum Lesen/Schreiben von JSON.
// Sie ist schon ueber spring-boot-starter-web im Projekt vorhanden.
// (Spring Boot 4 nutzt Jackson 3 – daher das Paket "tools.jackson".)
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/*
 * Muster "Adapter" – der ADAPTER selbst (Kapitel 13, Aufgabe 2).
 *
 * Er erfuellt das Target-Interface SpeiseplanQuelle ("ich kann einen
 * Speiseplan liefern") und uebersetzt im Inneren eine rohe MQTT-Nachricht
 * (Adaptee) in saubere Speiseplan-/Gericht-Objekte.
 *
 * Im Diagramm:  SpeiseplanQuelle <|.. MqttDatenAdapter
 *               MqttDatenAdapter --> MqttRohNachricht
 *               MqttDatenAdapter --> Speiseplan : liefert
 */
public class MqttDatenAdapter implements SpeiseplanQuelle {

    // Die rohe Nachricht, die uebersetzt werden soll (Adaptee).
    private final MqttRohNachricht roh;

    // Werkzeug zum Lesen von JSON-Text (Jackson 3: JsonMapper).
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public MqttDatenAdapter(MqttRohNachricht roh) {
        this.roh = roh;
    }

    /*
     * Die Uebersetzung: rohen JSON-Text einlesen und daraus eine Liste
     * von Gericht-Objekten bauen. Erwartetes Format (vom Fremdsystem):
     * {"typ":"speiseplan","gerichte":[{"name":"...","preis":3.5}, ...]}
     */
    @Override
    public Speiseplan holeSpeiseplan() {
        List<Gericht> gerichte = new ArrayList<>();
        try {
            JsonNode wurzel = MAPPER.readTree(roh.payloadJson());
            JsonNode liste = wurzel.get("gerichte");
            if (liste != null) {
                for (int i = 0; i < liste.size(); i++) {
                    JsonNode g = liste.get(i);
                    gerichte.add(new Gericht(
                            g.get("name").asString(),
                            g.get("preis").asDouble()
                    ));
                }
            }
        } catch (Exception e) {
            // Falls die Nachricht kaputt ist: klar verstaendlich abbrechen.
            throw new RuntimeException("Konnte MQTT-Speiseplan nicht lesen: "
                    + roh.payloadJson(), e);
        }
        return new Speiseplan(gerichte);
    }
}
