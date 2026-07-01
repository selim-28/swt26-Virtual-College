// Eigener Unterordner (= eigenes Modul) "transit" (ÖPNV).
package swt2.virtualcollege.receive.transit;

// "List" ist eine fertige Java-Klasse fuer eine Liste (mehrere Eintraege).
import java.util.List;

// @GetMapping = reagiert auf eine "GET"-Anfrage (Daten abfragen/lesen).
import org.springframework.web.bind.annotation.GetMapping;
// @RequestMapping = legt die gemeinsame Basis-Adresse fuer diesen Controller fest.
import org.springframework.web.bind.annotation.RequestMapping;
// @RestController = macht aus dieser Klasse einen REST-Controller.
import org.springframework.web.bind.annotation.RestController;

/*
 * Modul "Transit" (Daten vom ÖPNV).
 * REST-Schnittstelle, ueber die das Frontend die naechsten Abfahrten abfragt.
 */

// @RestController: diese Klasse beantwortet Web-Anfragen und gibt Daten zurueck.
@RestController
// @RequestMapping("/api/transit"): alle Adressen hier beginnen mit /api/transit.
@RequestMapping("/api/transit")
// Klassen-Definition. Name passt zur Datei TransitController.java.
public class TransitController {

    private final AbfahrtsSpeicher speicher;

    public TransitController(AbfahrtsSpeicher speicher) {
        this.speicher = speicher;
    }

    // @GetMapping("/abfahrten"): antwortet auf GET /api/transit/abfahrten.
    @GetMapping("/abfahrten")
    // Methode "abfahrten" gibt eine Liste von "Abfahrt"-Objekten zurueck.
    public TransitAntwort abfahrten() {
        return speicher.aktuelleAbfahrten();
    }

    // Einfaches Datenobjekt fuer eine Abfahrt: Linie, Ziel, Zeit (alles Text).
    public record Abfahrt(String linie, String ziel, String zeit) {
    }

    public record TransitAntwort(String quelle, String haltestelle,
                                 String aktualisiertAm, List<Abfahrt> abfahrten) {
    }
}
