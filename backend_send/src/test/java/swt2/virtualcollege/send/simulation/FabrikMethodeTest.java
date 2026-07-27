package swt2.virtualcollege.send.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

/**
 * US 7.1 (Issue #32): Jede Fabrik liefert das passende Produkt, und jedes Produkt
 * beschreibt sich als gueltiges JSON mit dem Typ, nach dem der Empfaenger sortiert.
 */
class FabrikMethodeTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    @Test
    void speiseplanGeneratorErzeugtSpeiseplanDaten() {
        Fremddatensatz produkt = new SpeiseplanGenerator().erzeugeDaten();

        assertThat(produkt).isInstanceOf(SpeiseplanDaten.class);
        String json = produkt.alsJson();
        assertThat(json).contains("\"typ\":\"speiseplan\"");
        assertThat(json).contains("\"gerichte\":");
        assertGueltigesJson(json);
    }

    @Test
    void auslastungsGeneratorErzeugtAuslastungsDaten() {
        Fremddatensatz produkt = new AuslastungsGenerator().erzeugeDaten();

        assertThat(produkt).isInstanceOf(AuslastungsDaten.class);
        String json = produkt.alsJson();
        assertThat(json).contains("\"typ\":\"auslastung\"");
        assertThat(json).contains("\"raum\":");
        assertThat(json).contains("\"prozent\":");
        assertGueltigesJson(json);
    }

    // Das Produkt wird direkt gebaut, damit der Test ohne Netz laeuft.
    @Test
    void abfahrtsDatenBeschreibenSichAlsGueltigesJson() {
        AbfahrtsDaten produkt = new AbfahrtsDaten(List.of(
                new AbfahrtsDaten.Abfahrt("S-Bahn S1", "Dortmund Hbf", "10:05"),
                new AbfahrtsDaten.Abfahrt("Bus 447", "Dortmund Hacheney", "10:12")
        ), "VRR EFA live");

        String json = produkt.alsJson();
        assertThat(json).contains("\"typ\":\"abfahrt\"");
        assertThat(json).contains("\"quelle\":\"VRR EFA live\"");
        assertThat(json).contains("S-Bahn S1");
        assertGueltigesJson(json);
    }

    @Test
    void abfahrtsDatenMaskierenAnfuehrungszeichen() {
        AbfahrtsDaten produkt = new AbfahrtsDaten(List.of(
                new AbfahrtsDaten.Abfahrt("Bus \"X\"", "Ziel \"A\"", "09:00")
        ), "quelle mit \" Zeichen");

        assertGueltigesJson(produkt.alsJson());
    }

    private void assertGueltigesJson(String json) {
        assertThatCode(() -> MAPPER.readTree(json))
                .as("Produkt muss gueltiges JSON liefern: %s", json)
                .doesNotThrowAnyException();
    }
}
