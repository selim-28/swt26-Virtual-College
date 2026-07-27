package swt2.virtualcollege.send.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * US 7.1 (Issue #32) und US 7.2 (Issue #33): Der SimulationService laesst alle
 * Fabriken je einen Datensatz erzeugen und veroeffentlicht ihn.
 * Der MqttSender ist durch ein Test-Doppel ersetzt, damit kein Broker noetig ist.
 */
class SimulationServiceTest {

    /** Test-Doppel: merkt sich die Nachricht, statt sie zu senden. */
    private static final class ErfassenderSender extends MqttSender {
        final List<String> gesendet = new ArrayList<>();

        @Override
        public void publish(String json) {
            gesendet.add(json);
        }
    }

    @Test
    void starteSimulationErzeugtDreiDatensaetzeUndVeroeffentlichtSie() {
        ErfassenderSender sender = new ErfassenderSender();
        SimulationService service = new SimulationService(sender);

        service.starteSimulation();

        assertThat(sender.gesendet).hasSize(3);
        assertThat(sender.gesendet).anyMatch(j -> j.contains("\"typ\":\"speiseplan\""));
        assertThat(sender.gesendet).anyMatch(j -> j.contains("\"typ\":\"auslastung\""));
        assertThat(sender.gesendet).anyMatch(j -> j.contains("\"typ\":\"abfahrt\""));
    }

    @Test
    void erneuterStartLiefertWiederGenauDreiDatensaetze() {
        ErfassenderSender sender = new ErfassenderSender();
        SimulationService service = new SimulationService(sender);

        service.starteSimulation();
        service.starteSimulation();

        // Die Liste wird pro Lauf geleert, insgesamt gesendet wurden aber sechs.
        assertThat(service.getZuletztVeroeffentlicht()).hasSize(3);
        assertThat(sender.gesendet).hasSize(6);
    }

    @Test
    void veroeffentlichePublishtUndMerktSichDenText() {
        ErfassenderSender sender = new ErfassenderSender();
        SimulationService service = new SimulationService(sender);

        Fremddatensatz auslastung = new AuslastungsDaten("B.1.23", 42);
        service.veroeffentliche(auslastung);

        assertThat(sender.gesendet).containsExactly(auslastung.alsJson());
        assertThat(service.getZuletztVeroeffentlicht()).containsExactly(auslastung.alsJson());
    }
}
