package swt2.virtualcollege.send.simulation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
 * "SimulationService" – der AUSLOESER (Client der Factory) aus dem
 * OOD-Klassendiagramm von backend_send (Kapitel 13).
 *
 * Er kennt die drei Fabriken (DatenGenerator), laesst sie je einen
 * Fremddatensatz erzeugen und "veroeffentlicht" diesen. In Schritt 1
 * heisst "veroeffentlichen" erst einmal nur: JSON bauen + protokollieren.
 * In Schritt 2 (MQTT) wird daraus echtes Senden ueber den Mosquitto-Broker.
 *
 * @Service = Spring verwaltet dieses Objekt und kann es in den
 * SimulationController "hineinreichen" (Dependency Injection).
 */
@Service
public class SimulationService {

    // Logger = Werkzeug, um Meldungen in die Konsole zu schreiben.
    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    // Die drei konkreten Fabriken (Factory Method). Eine neue Datenart
    // waere einfach eine weitere Zeile hier – sonst nichts (OCP).
    private final List<DatenGenerator> generatoren = List.of(
            new SpeiseplanGenerator(),
            new AuslastungsGenerator(),
            new AbfahrtsGenerator()
    );

    // Merkt sich die zuletzt veroeffentlichten JSON-Texte (fuer Anzeige/Test).
    private final List<String> zuletztVeroeffentlicht = new ArrayList<>();

    // Der MQTT-Sender (Schritt 2). Spring reicht ihn ueber den Konstruktor herein.
    private final MqttSender mqttSender;

    public SimulationService(MqttSender mqttSender) {
        this.mqttSender = mqttSender;
    }

    /*
     * Startet die Simulation: laesst JEDE Fabrik einen Datensatz erzeugen
     * und veroeffentlicht ihn. Beachte: Diese Methode kennt KEIN "if typ==...",
     * sie ruft nur generator.erzeugeDaten() – das ist der Witz des Musters.
     */
    public void starteSimulation() {
        zuletztVeroeffentlicht.clear();
        for (DatenGenerator generator : generatoren) {
            Fremddatensatz datensatz = generator.erzeugeDaten();
            veroeffentliche(datensatz);
        }
    }

    /*
     * Veroeffentlicht EINEN Datensatz. In Schritt 1 nur als Log-Ausgabe.
     * (Schritt 2 ersetzt den Log-Aufruf durch echtes MQTT-Publish.)
     */
    public void veroeffentliche(Fremddatensatz datensatz) {
        String json = datensatz.alsJson();
        zuletztVeroeffentlicht.add(json);
        // Schritt 2: WIRKLICH ueber MQTT an den Broker senden (publish).
        mqttSender.publish(json);
        log.info("[Simulation] Datensatz per MQTT veroeffentlicht: {}", json);
    }

    // Erlaubt dem Controller, die zuletzt veroeffentlichten Texte anzuzeigen.
    public List<String> getZuletztVeroeffentlicht() {
        return zuletztVeroeffentlicht;
    }
}
