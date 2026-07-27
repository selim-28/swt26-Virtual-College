package swt2.virtualcollege.send.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

/**
 * US 6.2 (Issue #30): Faellt der VRR-Dienst aus, liefert das Backend statt eines
 * Fehlers klar gekennzeichnete Ersatzdaten. Der Ausfall wird ueber eine
 * unerreichbare Adresse erzwungen.
 */
class VrrAbfahrtenTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    void beiAusfallKommenGekennzeichneteErsatzdatenStattAbsturz() {
        VrrAbfahrten ausgefallen = new VrrAbfahrten("http://127.0.0.1:9/kein-dienst");

        AbfahrtsDaten daten = ausgefallen.holeAbfahrten();

        String json = daten.alsJson();
        assertThat(json).contains("Offline-Fallback");
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
