package swt2.virtualcollege.send.simulation;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

/*
 * EPIC 7 – US 7.2 "Daten asynchron ueber MQTT uebertragen" (Issue #33).
 *
 * Akzeptanzkriterium: "Ein voruebergehend nicht erreichbarer Empfaenger fuehrt
 * NICHT zum Absturz des Senders." Wir bauen genau diese Situation nach: ein
 * frischer MqttSender ohne verbundenen Broker (client == null). Ein publish()
 * darf dann keine Ausnahme werfen, sondern die Nachricht nur verwerfen und den
 * Fehler protokollieren.
 */
class MqttSenderTest {

    @Test
    void publishOhneVerbindungStuerztNichtAb() {
        // Kein Spring, kein Broker: der Client ist nicht verbunden.
        MqttSender sender = new MqttSender();

        assertThatCode(() -> sender.publish("{\"typ\":\"speiseplan\"}"))
                .doesNotThrowAnyException();
    }
}
