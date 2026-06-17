// Eigener Unterordner (= eigenes Modul) "simulation" im Fremdsystem-Backend.
package swt2.virtualcollege.send.simulation;

// @PostMapping = reagiert auf eine "POST"-Anfrage (etwas ausloesen/anlegen).
import org.springframework.web.bind.annotation.PostMapping;
// @RequestMapping = legt die gemeinsame Basis-Adresse fuer diesen Controller fest.
import org.springframework.web.bind.annotation.RequestMapping;
// @RestController = macht aus dieser Klasse einen REST-Controller.
import org.springframework.web.bind.annotation.RestController;

/*
 * Modul "Simulation" des Fremdsystems (ÖPNV / Studierendenwerk).
 * REST-Schnittstelle, um das Erzeugen und (spaetere) Versenden von
 * Testdaten ueber MQTT anzustossen.
 */

// @RestController: diese Klasse beantwortet Web-Anfragen und gibt Daten zurueck.
@RestController
// @RequestMapping("/api/simulation"): alle Adressen hier beginnen mit /api/simulation.
@RequestMapping("/api/simulation")
// Klassen-Definition. Name passt zur Datei SimulationController.java.
public class SimulationController {

    // @PostMapping("/start"): antwortet auf POST /api/simulation/start.
    // POST nimmt man, wenn man eine Aktion ausloest (nicht nur Daten liest).
    @PostMapping("/start")
    // Methode "start" gibt ein "Status"-Objekt mit einer Meldung zurueck.
    public Status start() {
        // Schickt eine Bestaetigungs-Meldung zurueck.
        // (Das echte Versenden ueber MQTT kommt in einem spaeteren Praktikum.)
        return new Status("Simulation gestartet - Daten werden (spaeter) per MQTT veroeffentlicht.");
    }

    // Einfaches Datenobjekt fuer die Antwort: nur ein Text "message".
    public record Status(String message) {
    }
}
