package swt2.virtualcollege.send.simulation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Client der Factory Method: kennt die konkreten Generatoren, laesst sie je einen
 * Fremddatensatz erzeugen und veroeffentlicht diesen ueber MQTT.
 */
@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    // Eine neue Datenart erfordert nur einen weiteren Eintrag hier (OCP).
    private final List<DatenGenerator> generatoren = List.of(
            new SpeiseplanGenerator(),
            new AuslastungsGenerator(),
            new AbfahrtsGenerator()
    );

    private final List<String> zuletztVeroeffentlicht = new ArrayList<>();

    private final MqttSender mqttSender;

    public SimulationService(MqttSender mqttSender) {
        this.mqttSender = mqttSender;
    }

    /**
     * Laesst jeden Generator einen Datensatz erzeugen und veroeffentlicht ihn.
     * Die Methode unterscheidet dabei bewusst nicht nach Datentyp.
     */
    public void starteSimulation() {
        zuletztVeroeffentlicht.clear();
        for (DatenGenerator generator : generatoren) {
            Fremddatensatz datensatz = generator.erzeugeDaten();
            veroeffentliche(datensatz);
        }
    }

    /** Sendet einen einzelnen Datensatz an den Broker und merkt ihn sich. */
    public void veroeffentliche(Fremddatensatz datensatz) {
        String json = datensatz.alsJson();
        zuletztVeroeffentlicht.add(json);
        mqttSender.publish(json);
        log.info("[Simulation] Datensatz per MQTT veroeffentlicht: {}", json);
    }

    public List<String> getZuletztVeroeffentlicht() {
        return zuletztVeroeffentlicht;
    }
}
