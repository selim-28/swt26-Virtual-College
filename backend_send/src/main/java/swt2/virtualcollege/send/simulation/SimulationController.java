// Eigener Unterordner (= eigenes Modul) "simulation" im Fremdsystem-Backend.
package swt2.virtualcollege.send.simulation;

import java.util.List;

// @PostMapping = reagiert auf eine "POST"-Anfrage (etwas ausloesen/anlegen).
import org.springframework.web.bind.annotation.PostMapping;
// @RequestMapping = legt die gemeinsame Basis-Adresse fuer diesen Controller fest.
import org.springframework.web.bind.annotation.RequestMapping;
// @RestController = macht aus dieser Klasse einen REST-Controller.
import org.springframework.web.bind.annotation.RestController;

/*
 * Modul "Simulation" des Fremdsystems (OEPNV / Studierendenwerk).
 * REST-Schnittstelle, um das Erzeugen und Versenden von Testdaten
 * anzustossen. Der Controller macht die Arbeit NICHT selbst, sondern
 * gibt sie an den SimulationService weiter (Trennung der Aufgaben).
 */

// @RestController: diese Klasse beantwortet Web-Anfragen und gibt Daten zurueck.
@RestController
// @RequestMapping("/api/simulation"): alle Adressen hier beginnen mit /api/simulation.
@RequestMapping("/api/simulation")
// Klassen-Definition. Name passt zur Datei SimulationController.java.
public class SimulationController {

    // Der Service, der die Factory-Klassen kennt. Spring "reicht" ihn
    // ueber den Konstruktor herein (Dependency Injection).
    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    // @PostMapping("/start"): antwortet auf POST /api/simulation/start.
    // POST nimmt man, wenn man eine Aktion ausloest (nicht nur Daten liest).
    @PostMapping("/start")
    public Status start() {
        // Service starten: er laesst die drei Fabriken Datensaetze erzeugen.
        simulationService.starteSimulation();
        // Zur Rueckmeldung zeigen wir, WAS erzeugt wurde.
        List<String> erzeugt = simulationService.getZuletztVeroeffentlicht();
        return new Status(
                "Simulation gestartet - " + erzeugt.size() + " Datensaetze erzeugt.",
                erzeugt
        );
    }

    // Datenobjekt fuer die Antwort: eine Meldung + die erzeugten JSON-Texte.
    public record Status(String message, List<String> datensaetze) {
    }
}
