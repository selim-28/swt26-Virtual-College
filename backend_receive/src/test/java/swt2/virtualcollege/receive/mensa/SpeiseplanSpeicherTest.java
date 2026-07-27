package swt2.virtualcollege.receive.mensa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * US 5.1 (Issue #5): Der Speicher haelt den zuletzt empfangenen Speiseplan bereit
 * und meldet kontrolliert, wenn noch keine Daten eingegangen sind.
 */
class SpeiseplanSpeicherTest {

    @Test
    void ohneEmpfangeneDatenMeldetErKeineDaten() {
        SpeiseplanSpeicher speicher = new SpeiseplanSpeicher();

        assertThat(speicher.hatDaten()).isFalse();
        assertThat(speicher.getLetzterSpeiseplan()).isNull();
    }

    // US 5.1: Der zuletzt empfangene Plan ersetzt den vorherigen.
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
