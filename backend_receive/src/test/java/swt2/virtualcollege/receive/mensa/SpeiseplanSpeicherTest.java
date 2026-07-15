package swt2.virtualcollege.receive.mensa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/*
 * EPIC 5 – Mensa-Informationen.
 *   US 5.1 "Aktuellen Mensa-Speiseplan anzeigen" (Issue #5)
 *
 * Der SpeiseplanSpeicher haelt den zuletzt ueber MQTT empfangenen Speiseplan
 * fuer die REST-Schnittstelle bereit. Wichtig fuer das Akzeptanzkriterium
 * "Ein Ausfall externer Daten bringt die Anwendung nicht zum Absturz":
 * Auch OHNE empfangene Daten antwortet der Speicher kontrolliert (hatDaten()
 * = false, der Controller liefert dann gekennzeichnete Demo-Daten).
 */
class SpeiseplanSpeicherTest {

    // Vor dem ersten MQTT-Empfang: kontrolliert "keine Daten" melden.
    @Test
    void ohneEmpfangeneDatenMeldetErKeineDaten() {
        SpeiseplanSpeicher speicher = new SpeiseplanSpeicher();

        assertThat(speicher.hatDaten()).isFalse();
        assertThat(speicher.getLetzterSpeiseplan()).isNull();
    }

    // US 5.1: "Neu empfangene Fremddaten werden automatisch verarbeitet" -
    // der zuletzt empfangene Plan ersetzt den alten.
    @Test
    void neuEmpfangenerSpeiseplanErsetztDenAlten() {
        SpeiseplanSpeicher speicher = new SpeiseplanSpeicher();

        speicher.speichere(new Speiseplan(List.of(new Gericht("Currywurst", 3.5))));
        speicher.speichere(new Speiseplan(List.of(
                new Gericht("Salat", 2.2), new Gericht("Pasta", 4.0))));

        assertThat(speicher.hatDaten()).isTrue();
        assertThat(speicher.getLetzterSpeiseplan().gerichte()).hasSize(2);
        assertThat(speicher.getLetzterSpeiseplan().gerichte().get(0).name()).isEqualTo("Salat");
    }
}
