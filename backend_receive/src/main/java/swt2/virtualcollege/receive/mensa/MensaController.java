// Eigener Unterordner (= eigenes Modul) "mensa".
// Beim "Vertical Slicing" bekommt jedes Thema seinen eigenen Ordner.
package swt2.virtualcollege.receive.mensa;

// "List" ist eine fertige Java-Klasse fuer eine Liste (mehrere Eintraege).
import java.util.List;

// Holt die Spring-Bausteine herein, mit denen wir REST-Endpunkte bauen:
// @GetMapping = reagiert auf eine "GET"-Anfrage (Daten abfragen/lesen).
import org.springframework.web.bind.annotation.GetMapping;
// @RequestMapping = legt die gemeinsame Basis-Adresse fuer diesen Controller fest.
import org.springframework.web.bind.annotation.RequestMapping;
// @RestController = macht aus dieser Klasse einen REST-Controller.
import org.springframework.web.bind.annotation.RestController;

/*
 * Modul "Mensa" (Daten vom Studierendenwerk).
 * Hier liegt die REST-Schnittstelle, ueber die das Frontend
 * den Speiseplan abfragen kann.
 */

// @RestController: sagt Spring "diese Klasse beantwortet Web-Anfragen
// und gibt die Antwort direkt als Daten (z.B. JSON) zurueck".
@RestController
// @RequestMapping("/api/mensa"): alle Adressen in dieser Klasse beginnen mit /api/mensa.
@RequestMapping("/api/mensa")
// Klassen-Definition. Name passt zur Datei MensaController.java.
public class MensaController {

    // @GetMapping("/speiseplan"): diese Methode antwortet auf
    // GET-Anfragen an die volle Adresse /api/mensa/speiseplan.
    @GetMapping("/speiseplan")
    // Die Methode "speiseplan" gibt eine Liste von "Gericht"-Objekten zurueck.
    public List<Gericht> speiseplan() {
        // "return" schickt die Antwort an den Aufrufer (das Frontend) zurueck.
        // List.of(...) baut eine feste Liste mit zwei Beispiel-Gerichten.
        // (Spaeter kommen hier die echten Daten ueber MQTT herein.)
        return List.of(
                // new Gericht(...) erzeugt ein Gericht mit Name und Preis.
                new Gericht("Spaghetti Bolognese", 3.50),
                new Gericht("Gemuesecurry", 2.90)
        );
    }

    // "record" ist eine kurze Schreibweise fuer ein einfaches Datenobjekt.
    // Ein Gericht hat hier einen Namen (Text) und einen Preis (Zahl mit Komma).
    public record Gericht(String name, double preis) {
    }
}
