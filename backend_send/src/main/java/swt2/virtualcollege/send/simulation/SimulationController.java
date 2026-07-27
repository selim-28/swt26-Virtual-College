package swt2.virtualcollege.send.simulation;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Schnittstelle des Fremdsystems, um das Erzeugen und Versenden von
 * Datensaetzen anzustossen. Die fachliche Arbeit liegt im SimulationService.
 */
@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /** POST /api/simulation/start - loest einen Simulationslauf aus. */
    @PostMapping("/start")
    public Status start() {
        simulationService.starteSimulation();
        List<String> erzeugt = simulationService.getZuletztVeroeffentlicht();
        return new Status(
                "Simulation gestartet - " + erzeugt.size() + " Datensaetze erzeugt.",
                erzeugt
        );
    }

    /** Antwortobjekt: Meldung und die erzeugten JSON-Texte. */
    public record Status(String message, List<String> datensaetze) {
    }
}
