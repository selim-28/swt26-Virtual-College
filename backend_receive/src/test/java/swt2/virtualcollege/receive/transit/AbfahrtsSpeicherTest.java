package swt2.virtualcollege.receive.transit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * US 7.3 (Issue #34) fuer die OePNV-Abfahrten: Rohdaten werden geprueft und in
 * interne Objekte umgewandelt, ungueltige Daten fuehren nicht zum Absturz.
 */
class AbfahrtsSpeicherTest {

    // US 6.2 (Issue #30): Vor dem ersten Empfang liefert der Speicher
    // gekennzeichnete Platzhalter statt eines Fehlers.
    @Test
    void startzustandLiefertGekennzeichneteErsatzdaten() {
        AbfahrtsSpeicher speicher = new AbfahrtsSpeicher();

        TransitController.TransitAntwort antwort = speicher.aktuelleAbfahrten();

        assertThat(antwort.quelle()).contains("Warte auf VRR-Daten");
        assertThat(antwort.abfahrten()).isNotEmpty();
        assertThat(antwort.abfahrten().get(0).zeit()).isEqualTo("--:--");
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

    // Ungueltige Daten melden einen klaren Fehler und lassen den zuletzt
    // gueltigen Datensatz unveraendert.
    @Test
    void ungueltigesJsonLaesstVorherigeGueltigeDatenUnveraendert() {
        AbfahrtsSpeicher speicher = new AbfahrtsSpeicher();

        speicher.aktualisiereAusJson(
                "{\"typ\":\"abfahrt\",\"quelle\":\"VRR EFA live\",\"abfahrten\":["
                + "{\"linie\":\"U42\",\"ziel\":\"Hombruch\",\"zeit\":\"08:00\"}]}");

        assertThatThrownBy(() -> speicher.aktualisiereAusJson("kein json"))
                .isInstanceOf(IllegalArgumentException.class);

        TransitController.TransitAntwort antwort = speicher.aktuelleAbfahrten();
        assertThat(antwort.quelle()).isEqualTo("VRR EFA live");
        assertThat(antwort.abfahrten()).hasSize(1);
        assertThat(antwort.abfahrten().get(0).linie()).isEqualTo("U42");
    }
}
