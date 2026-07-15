package swt2.virtualcollege.send.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

/*
 * EPIC 7 – Fremdsystem und asynchrone Datenuebertragung
 * US 7.1 "Fremddatensimulation starten" (Issue #32).
 *
 * Diese Tests pruefen das Muster "Factory Method" und damit das
 * Akzeptanzkriterium "Mensa-, OEPNV- und Auslastungsdaten werden erzeugt":
 * Jede konkrete Fabrik (DatenGenerator) liefert das richtige Produkt
 * (Fremddatensatz), und jedes Produkt beschreibt sich als GUELTIGES JSON mit
 * dem korrekten "typ" – genau das Format, das spaeter ueber MQTT verschickt und
 * vom Empfaenger anhand des Typs einsortiert wird (US 7.3).
 */
class FabrikMethodeTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    // --- Mensadaten ---
    @Test
    void speiseplanGeneratorErzeugtSpeiseplanDaten() {
        Fremddatensatz produkt = new SpeiseplanGenerator().erzeugeDaten();

        assertThat(produkt).isInstanceOf(SpeiseplanDaten.class);
        String json = produkt.alsJson();
        assertThat(json).contains("\"typ\":\"speiseplan\"");
        assertThat(json).contains("\"gerichte\":");
        assertGueltigesJson(json);
    }

    // --- Auslastungsdaten ---
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

    // --- OEPNV-Abfahrten (ohne Netz getestet: Produkt direkt gebaut) ---
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

    // Sonderzeichen (Anfuehrungszeichen) im Text duerfen das JSON nicht zerstoeren.
    @Test
    void abfahrtsDatenMaskierenAnfuehrungszeichen() {
        AbfahrtsDaten produkt = new AbfahrtsDaten(List.of(
                new AbfahrtsDaten.Abfahrt("Bus \"X\"", "Ziel \"A\"", "09:00")
        ), "quelle mit \" Zeichen");

        // Trotz Anfuehrungszeichen im Text bleibt das JSON les-/parsebar.
        assertGueltigesJson(produkt.alsJson());
    }

    // Kleine Hilfe: prueft, ob ein Text wirklich gueltiges JSON ist.
    private void assertGueltigesJson(String json) {
        assertThatCode(() -> MAPPER.readTree(json))
                .as("Produkt muss gueltiges JSON liefern: %s", json)
                .doesNotThrowAnyException();
    }
}
