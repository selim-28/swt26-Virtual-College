package swt2.virtualcollege.send.simulation;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

/**
 * US 7.2 (Issue #33): Ein nicht erreichbarer Empfaenger darf den Sender nicht
 * zum Absturz bringen - publish() verwirft die Nachricht und protokolliert sie.
 */
class MqttSenderTest {

    @Test
    void publishOhneVerbindungStuerztNichtAb() {
        MqttSender sender = new MqttSender();

        assertThatCode(() -> sender.publish("{\"typ\":\"speiseplan\"}"))
                .doesNotThrowAnyException();
    }
}
