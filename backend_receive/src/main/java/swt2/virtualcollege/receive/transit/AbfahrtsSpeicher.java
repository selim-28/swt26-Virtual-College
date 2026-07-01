package swt2.virtualcollege.receive.transit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/* Hält den letzten über MQTT empfangenen ÖPNV-Snapshot im Arbeitsspeicher. */
@Component
public class AbfahrtsSpeicher {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private volatile List<TransitController.Abfahrt> abfahrten = List.of(
            new TransitController.Abfahrt("S-Bahn S1", "Dortmund Hbf", "--:--"),
            new TransitController.Abfahrt("Bus 447", "Dortmund Hacheney", "--:--")
    );
    private volatile String quelle = "Warte auf VRR-Daten via MQTT";
    private volatile String aktualisiertAm;

    public void aktualisiereAusJson(String payload) {
        try {
            JsonNode wurzel = MAPPER.readTree(payload);
            List<TransitController.Abfahrt> neu = new ArrayList<>();
            JsonNode liste = wurzel.get("abfahrten");
            if (liste != null) {
                for (int i = 0; i < liste.size(); i++) {
                    JsonNode a = liste.get(i);
                    neu.add(new TransitController.Abfahrt(
                            a.get("linie").asString(),
                            a.get("ziel").asString(),
                            a.get("zeit").asString()
                    ));
                }
            }
            if (!neu.isEmpty()) abfahrten = List.copyOf(neu);
            if (wurzel.get("quelle") != null) quelle = wurzel.get("quelle").asString();
            aktualisiertAm = Instant.now().toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Konnte MQTT-Abfahrten nicht lesen", e);
        }
    }

    public TransitController.TransitAntwort aktuelleAbfahrten() {
        return new TransitController.TransitAntwort(
                quelle, "Dortmund Universität S", aktualisiertAm, abfahrten
        );
    }
}
