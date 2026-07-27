package swt2.virtualcollege.receive.transit;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Schnittstelle des Moduls "Transit". Liefert dem Frontend die zuletzt ueber
 * MQTT empfangenen OePNV-Abfahrten.
 */
@RestController
@RequestMapping("/api/transit")
public class TransitController {

    private final AbfahrtsSpeicher speicher;

    public TransitController(AbfahrtsSpeicher speicher) {
        this.speicher = speicher;
    }

    @GetMapping("/abfahrten")
    public TransitAntwort abfahrten() {
        return speicher.aktuelleAbfahrten();
    }

    /** Eine einzelne Abfahrt: Linie, Ziel, Zeit. */
    public record Abfahrt(String linie, String ziel, String zeit) {
    }

    public record TransitAntwort(String quelle, String haltestelle,
                                 String aktualisiertAm, List<Abfahrt> abfahrten) {
    }
}
