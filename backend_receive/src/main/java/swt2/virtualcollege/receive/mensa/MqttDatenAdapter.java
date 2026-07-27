package swt2.virtualcollege.receive.mensa;

import java.util.ArrayList;
import java.util.List;

// Spring Boot 4 nutzt Jackson 3, daher das Paket "tools.jackson".
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adapter-Muster: der Adapter. Erfuellt das Target-Interface SpeiseplanQuelle
 * und uebersetzt intern eine rohe MQTT-Nachricht in Speiseplan- und Gericht-Objekte.
 */
public class MqttDatenAdapter implements SpeiseplanQuelle {

    private final MqttRohNachricht roh;

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    public MqttDatenAdapter(MqttRohNachricht roh) {
        this.roh = roh;
    }

    /**
     * Liest das vereinbarte Nachrichtenformat
     * {"typ":"speiseplan","gerichte":[{"name":"...","preis":3.5}, ...]}
     * und baut daraus die Fachobjekte.
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
            throw new RuntimeException("Konnte MQTT-Speiseplan nicht lesen: "
                    + roh.payloadJson(), e);
        }
        return new Speiseplan(gerichte);
    }
}
