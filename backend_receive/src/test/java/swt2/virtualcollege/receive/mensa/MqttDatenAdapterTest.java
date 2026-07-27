package swt2.virtualcollege.receive.mensa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * US 7.3 (Issue #34): Der Adapter uebersetzt die rohe MQTT-Nachricht in
 * Speiseplan- und Gericht-Objekte und meldet ungueltige Daten verstaendlich.
 */
class MqttDatenAdapterTest {

    @Test
    void uebersetztGueltigenSpeiseplanInGerichte() {
        String json = "{\"typ\":\"speiseplan\",\"gerichte\":["
                + "{\"name\":\"Currywurst\",\"preis\":3.5},"
                + "{\"name\":\"Salat\",\"preis\":2.2}]}";

        MqttDatenAdapter adapter = new MqttDatenAdapter(new MqttRohNachricht(json));
        Speiseplan plan = adapter.holeSpeiseplan();

        assertThat(plan.gerichte()).hasSize(2);
        assertThat(plan.gerichte().get(0).name()).isEqualTo("Currywurst");
        assertThat(plan.gerichte().get(0).preis()).isEqualTo(3.5);
        assertThat(plan.gerichte().get(1).name()).isEqualTo("Salat");
    }

    // Fehlt die Gerichte-Liste, entsteht ein leerer Plan statt eines Fehlers.
    @Test
    void fehlendeGerichteListeErgibtLeerenSpeiseplan() {
        String json = "{\"typ\":\"speiseplan\"}";

        MqttDatenAdapter adapter = new MqttDatenAdapter(new MqttRohNachricht(json));
        Speiseplan plan = adapter.holeSpeiseplan();

        assertThat(plan.gerichte()).isEmpty();
    }

    // Kaputter JSON-Text bricht mit klarer Meldung ab, statt falsche Daten
    // weiterzureichen.
    @Test
    void kaputterJsonWirdVerstaendlichGemeldet() {
        String kaputt = "das ist kein JSON";

        MqttDatenAdapter adapter = new MqttDatenAdapter(new MqttRohNachricht(kaputt));

        assertThatThrownBy(adapter::holeSpeiseplan)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Speiseplan");
    }
}
