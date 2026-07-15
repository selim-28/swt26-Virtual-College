package swt2.virtualcollege.send.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/*
 * EPIC 7 – US 7.1 "Fremddatensimulation starten" (Issue #32)
 *          US 7.2 "Daten asynchron ueber MQTT uebertragen" (Issue #33)
 *
 * Der SimulationService ist der Ausloeser: Er laesst die drei Fabriken je einen
 * Fremddatensatz erzeugen und veroeffentlicht ihn ueber den MqttSender (publish).
 *
 * Damit der Test OHNE echten Broker und OHNE Netz laeuft, ersetzen wir den
 * MqttSender durch einen "erfassenden" Sender: Er merkt sich nur, was
 * veroeffentlicht wurde, statt es wirklich zu verschicken. So pruefen wir die
 * Logik des Service unabhaengig von der Infrastruktur.
 */
class SimulationServiceTest {

    /* Test-Doppel: statt zu senden, wird die Nachricht nur gemerkt. */
    private static final class ErfassenderSender extends MqttSender {
        final List<String> gesendet = new ArrayList<>();

        @Override
        public void publish(String json) {
            gesendet.add(json);
        }
    }

    // US 7.1: "Mensa-, OEPNV- und Auslastungsdaten werden erzeugt."
    // US 7.2: "Das sendende Backend veroeffentlicht die Daten ueber MQTT."
    @Test
    void starteSimulationErzeugtDreiDatensaetzeUndVeroeffentlichtSie() {
        ErfassenderSender sender = new ErfassenderSender();
        SimulationService service = new SimulationService(sender);

        service.starteSimulation();

        // Genau drei Datenarten wurden veroeffentlicht ...
        assertThat(sender.gesendet).hasSize(3);
        // ... und jede Art ist dabei (am "typ" im JSON erkennbar).
        assertThat(sender.gesendet).anyMatch(j -> j.contains("\"typ\":\"speiseplan\""));
        assertThat(sender.gesendet).anyMatch(j -> j.contains("\"typ\":\"auslastung\""));
        assertThat(sender.gesendet).anyMatch(j -> j.contains("\"typ\":\"abfahrt\""));
    }

    // US 7.1: "Ein erneuter Start ist moeglich, ohne die Anwendung neu zu starten."
    @Test
    void erneuterStartLiefertWiederGenauDreiDatensaetze() {
        ErfassenderSender sender = new ErfassenderSender();
        SimulationService service = new SimulationService(sender);

        service.starteSimulation();
        service.starteSimulation();   // zweiter Lauf

        // Die "zuletzt veroeffentlicht"-Liste wird bei jedem Lauf geleert:
        // nach dem zweiten Start sind es wieder genau drei (nicht sechs).
        assertThat(service.getZuletztVeroeffentlicht()).hasSize(3);
        // Insgesamt wurden ueber beide Laeufe sechs Nachrichten gesendet.
        assertThat(sender.gesendet).hasSize(6);
    }

    // US 7.2 (Detail): veroeffentliche() sendet ueber MQTT UND merkt sich den Text
    // (fuer die Rueckmeldung an den Nutzer, US 7.1 "verstaendliche Rueckmeldung").
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
