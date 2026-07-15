package swt2.virtualcollege.receive.mensa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/*
 * EPIC 7 – US 7.3 "Empfangene Fremddaten verarbeiten und speichern" (Issue #34).
 *
 * Der MqttDatenAdapter (Muster "Adapter") ist der Kern der Verarbeitung: Er
 * nimmt die rohe MQTT-Nachricht (JSON-Text) und uebersetzt sie in saubere
 * interne Objekte (Speiseplan/Gericht). Diese Tests decken die Akzeptanz-
 * kriterien ab:
 *   - "MQTT-Nachrichten werden als JSON empfangen."
 *   - "Rohdaten werden geprueft und in interne Objekte umgewandelt."
 *   - "Ungueltige Daten fuehren nicht zum Absturz des Systems."
 */
class MqttDatenAdapterTest {

    // Rohdaten -> interne Objekte: gueltiger Speiseplan wird korrekt uebersetzt.
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

    // Fehlt die Gerichte-Liste, ist das Ergebnis ein leerer Plan (kein Absturz).
    @Test
    void fehlendeGerichteListeErgibtLeerenSpeiseplan() {
        String json = "{\"typ\":\"speiseplan\"}";

        MqttDatenAdapter adapter = new MqttDatenAdapter(new MqttRohNachricht(json));
        Speiseplan plan = adapter.holeSpeiseplan();

        assertThat(plan.gerichte()).isEmpty();
    }

    // Kaputter JSON-Text: verstaendlich abbrechen (klare Meldung mit der Ursache),
    // statt falsche Daten weiterzureichen. Das System stuerzt dadurch nicht ab –
    // der Aufrufer bekommt eine eindeutige Fehlermeldung.
    @Test
    void kaputterJsonWirdVerstaendlichGemeldet() {
        String kaputt = "das ist kein JSON";

        MqttDatenAdapter adapter = new MqttDatenAdapter(new MqttRohNachricht(kaputt));

        assertThatThrownBy(adapter::holeSpeiseplan)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Speiseplan");
    }
}
