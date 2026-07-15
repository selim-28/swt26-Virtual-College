package swt2.virtualcollege.send.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

/*
 * EPIC 6 – Oeffentlicher Nahverkehr.
 *   US 6.2 "Ersatzdaten bei Ausfall des Verkehrsdienstes" (Issue #30)
 *
 * Wir lassen den VRR-Dienst GEZIELT ausfallen, indem wir eine absichtlich
 * unerreichbare Adresse einstellen (127.0.0.1:9 - dort antwortet nichts).
 * Erwartung: KEIN Absturz, sondern Ersatzdaten, die klar als Offline-Daten
 * gekennzeichnet sind.
 */
class VrrAbfahrtenTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    void beiAusfallKommenGekennzeichneteErsatzdatenStattAbsturz() {
        VrrAbfahrten ausgefallen = new VrrAbfahrten("http://127.0.0.1:9/kein-dienst");

        AbfahrtsDaten daten = ausgefallen.holeAbfahrten();   // darf NICHT werfen

        String json = daten.alsJson();
        // Eindeutig als Offline-Daten gekennzeichnet (Akzeptanzkriterium).
        assertThat(json).contains("Offline-Fallback");
        // Es gibt weiterhin anzeigbare Abfahrten (Linie, Ziel, Zeit).
        assertThat(json).contains("S-Bahn S1");
        assertThat(json).contains("\"typ\":\"abfahrt\"");
    }

    @Test
    void ersatzdatenSindGueltigesJsonFuerDieMqttKette() {
        VrrAbfahrten ausgefallen = new VrrAbfahrten("http://127.0.0.1:9/kein-dienst");

        String json = ausgefallen.holeAbfahrten().alsJson();

        // Der Fallback muss durch dieselbe MQTT-Kette passen wie Live-Daten.
        assertThatCode(() -> MAPPER.readTree(json)).doesNotThrowAnyException();
    }
}
