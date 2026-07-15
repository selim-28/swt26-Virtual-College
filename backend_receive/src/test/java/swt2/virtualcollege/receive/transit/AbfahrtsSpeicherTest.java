package swt2.virtualcollege.receive.transit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/*
 * EPIC 7 – US 7.3 "Empfangene Fremddaten verarbeiten und speichern" (Issue #34),
 * hier fuer die OEPNV-Abfahrten (das zweite ueber MQTT empfangene Datenformat).
 *
 * Der AbfahrtsSpeicher liest die rohe JSON-Nachricht und haelt den aktuellen
 * Abfahrts-Snapshot fuer die REST-Schnittstelle bereit. Getestet werden:
 *   - "Rohdaten werden geprueft und in interne Objekte umgewandelt."
 *   - "Ungueltige Daten fuehren nicht zum Absturz des Systems."
 */
class AbfahrtsSpeicherTest {

    // US 6.2 (Issue #30): VOR dem ersten MQTT-Empfang liefert der Speicher
    // gekennzeichnete Platzhalter-Daten - die Anwendung bleibt nutzbar und
    // sagt ehrlich, dass noch keine Live-Daten da sind.
    @Test
    void startzustandLiefertGekennzeichneteErsatzdaten() {
        AbfahrtsSpeicher speicher = new AbfahrtsSpeicher();

        TransitController.TransitAntwort antwort = speicher.aktuelleAbfahrten();

        assertThat(antwort.quelle()).contains("Warte auf VRR-Daten");
        assertThat(antwort.abfahrten()).isNotEmpty();          // etwas Anzeigbares
        assertThat(antwort.abfahrten().get(0).zeit()).isEqualTo("--:--"); // klar als Platzhalter erkennbar
    }

    @Test
    void uebernimmtAbfahrtenUndQuelleAusJson() {
        AbfahrtsSpeicher speicher = new AbfahrtsSpeicher();

        String json = "{\"typ\":\"abfahrt\",\"quelle\":\"VRR EFA live\",\"abfahrten\":["
                + "{\"linie\":\"S-Bahn S1\",\"ziel\":\"Dortmund Hbf\",\"zeit\":\"10:05\"},"
                + "{\"linie\":\"Bus 447\",\"ziel\":\"Hacheney\",\"zeit\":\"10:12\"}]}";

        speicher.aktualisiereAusJson(json);

        TransitController.TransitAntwort antwort = speicher.aktuelleAbfahrten();
        assertThat(antwort.quelle()).isEqualTo("VRR EFA live");
        assertThat(antwort.haltestelle()).isEqualTo("Dortmund Universität S");
        assertThat(antwort.abfahrten()).hasSize(2);
        assertThat(antwort.abfahrten().get(0).linie()).isEqualTo("S-Bahn S1");
        assertThat(antwort.abfahrten().get(0).ziel()).isEqualTo("Dortmund Hbf");
    }

    // Robustheit: Kaputte Daten melden einen klaren Fehler UND lassen die zuletzt
    // gueltigen Daten unveraendert – das System liefert weiter etwas Sinnvolles.
    @Test
    void ungueltigesJsonLaesstVorherigeGueltigeDatenUnveraendert() {
        AbfahrtsSpeicher speicher = new AbfahrtsSpeicher();

        // Zuerst ein gueltiger Snapshot ...
        speicher.aktualisiereAusJson(
                "{\"typ\":\"abfahrt\",\"quelle\":\"VRR EFA live\",\"abfahrten\":["
                + "{\"linie\":\"U42\",\"ziel\":\"Hombruch\",\"zeit\":\"08:00\"}]}");

        // ... dann kommt kaputter Müll herein.
        assertThatThrownBy(() -> speicher.aktualisiereAusJson("kein json"))
                .isInstanceOf(IllegalArgumentException.class);

        // Der gute Snapshot ist NICHT beschaedigt worden.
        TransitController.TransitAntwort antwort = speicher.aktuelleAbfahrten();
        assertThat(antwort.quelle()).isEqualTo("VRR EFA live");
        assertThat(antwort.abfahrten()).hasSize(1);
        assertThat(antwort.abfahrten().get(0).linie()).isEqualTo("U42");
    }
}
