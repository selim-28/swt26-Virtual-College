// Eigener Unterordner (= eigenes Modul) "mensa".
// Beim "Vertical Slicing" bekommt jedes Thema seinen eigenen Ordner.
package swt2.virtualcollege.receive.mensa;

// "List" ist eine fertige Java-Klasse fuer eine Liste (mehrere Eintraege).
import java.util.List;

// @GetMapping = reagiert auf eine "GET"-Anfrage (Daten abfragen/lesen).
import org.springframework.web.bind.annotation.GetMapping;
// @RequestMapping = legt die gemeinsame Basis-Adresse fuer diesen Controller fest.
import org.springframework.web.bind.annotation.RequestMapping;
// @RestController = macht aus dieser Klasse einen REST-Controller.
import org.springframework.web.bind.annotation.RestController;

/*
 * Modul "Mensa" (Daten vom Studierendenwerk).
 * REST-Schnittstelle, ueber die das Frontend den Speiseplan abfragt.
 *
 * SCHRITT 2: Der Controller liefert jetzt – wenn vorhanden – die ECHTEN,
 * ueber MQTT empfangenen Daten (aus dem SpeiseplanSpeicher). Solange noch
 * keine MQTT-Nachricht kam, liefert er Beispiel-Daten. Am Feld "quelle"
 * sieht man sofort, woher die Daten stammen.
 */

@RestController
@RequestMapping("/api/mensa")
public class MensaController {

    // Der gemeinsame Speicher, in den der MQTT-Empfaenger schreibt.
    private final SpeiseplanSpeicher speicher;

    // Schritt 3: liest die dauerhaft gespeicherte Historie aus der Datenbank.
    private final swt2.virtualcollege.receive.persistenz.SpeisearchivService archiv;

    public MensaController(SpeiseplanSpeicher speicher,
                           swt2.virtualcollege.receive.persistenz.SpeisearchivService archiv) {
        this.speicher = speicher;
        this.archiv = archiv;
    }

    @GetMapping("/speiseplan")
    public MensaAntwort speiseplan() {
        // Gibt es schon einen ueber MQTT empfangenen Speiseplan?
        if (speicher.hatDaten()) {
            return new MensaAntwort(
                    "MQTT (live vom Fremdsystem)",
                    speicher.getLetzterSpeiseplan().gerichte()
            );
        }
        // Noch nichts empfangen -> Beispiel-Daten + Hinweis.
        return new MensaAntwort(
                "Beispiel (noch keine MQTT-Daten - erst POST /api/simulation/start am Backend 8084)",
                List.of(
                        new Gericht("Spaghetti Bolognese", 3.50),
                        new Gericht("Gemuesecurry", 2.90)
                )
        );
    }

    /*
     * SCHRITT 3: zeigt die dauerhaft in der Datenbank gespeicherte Historie.
     * Jeder POST /api/simulation/start fuegt neue Zeilen hinzu – die Anzahl
     * waechst also bei jedem Aufruf. Das beweist die echte Speicherung.
     */
    @GetMapping("/historie")
    public HistorieAntwort historie() {
        var eintraege = archiv.historie().stream()
                .map(g -> new HistorieEintrag(g.getId(), g.getName(), g.getPreis(),
                        g.getEmpfangenAm().toString()))
                .toList();
        return new HistorieAntwort(archiv.anzahl(), eintraege);
    }

    // Antwort-Objekt: zeigt die Datenquelle UND die Gerichte.
    public record MensaAntwort(String quelle, List<Gericht> gerichte) {
    }

    // Antwort-Objekt fuer die Historie: Gesamtzahl + die einzelnen Zeilen.
    public record HistorieAntwort(long anzahlGesamt, List<HistorieEintrag> eintraege) {
    }

    public record HistorieEintrag(Long id, String name, double preis, String empfangenAm) {
    }
}

