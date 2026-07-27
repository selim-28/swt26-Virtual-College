package swt2.virtualcollege.receive.mensa;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Schnittstelle des Moduls "Mensa". Liefert den zuletzt ueber MQTT
 * empfangenen Speiseplan und die dauerhaft gespeicherte Historie.
 */
@RestController
@RequestMapping("/api/mensa")
public class MensaController {

    private final SpeiseplanSpeicher speicher;

    private final swt2.virtualcollege.receive.persistenz.SpeisearchivService archiv;

    public MensaController(SpeiseplanSpeicher speicher,
                           swt2.virtualcollege.receive.persistenz.SpeisearchivService archiv) {
        this.speicher = speicher;
        this.archiv = archiv;
    }

    /**
     * Liefert den aktuellen Speiseplan. Solange keine MQTT-Nachricht eingegangen
     * ist, kommen Beispieldaten; das Feld "quelle" macht den Unterschied sichtbar.
     */
    @GetMapping("/speiseplan")
    public MensaAntwort speiseplan() {
        if (speicher.hatDaten()) {
            return new MensaAntwort(
                    "MQTT (live vom Fremdsystem)",
                    speicher.getLetzterSpeiseplan().gerichte()
            );
        }
        return new MensaAntwort(
                "Beispiel (noch keine MQTT-Daten - erst POST /api/simulation/start am Backend 8084)",
                List.of(
                        new Gericht("Spaghetti Bolognese", 3.50),
                        new Gericht("Gemuesecurry", 2.90)
                )
        );
    }

    /** Liefert die in der Datenbank gespeicherte Historie aller Empfaenge. */
    @GetMapping("/historie")
    public HistorieAntwort historie() {
        var eintraege = archiv.historie().stream()
                .map(g -> new HistorieEintrag(g.getId(), g.getName(), g.getPreis(),
                        g.getEmpfangenAm().toString()))
                .toList();
        return new HistorieAntwort(archiv.anzahl(), eintraege);
    }

    /** Antwort mit Datenquelle und Gerichten. */
    public record MensaAntwort(String quelle, List<Gericht> gerichte) {
    }

    /** Antwort der Historie: Gesamtzahl und die einzelnen Zeilen. */
    public record HistorieAntwort(long anzahlGesamt, List<HistorieEintrag> eintraege) {
    }

    public record HistorieEintrag(Long id, String name, double preis, String empfangenAm) {
    }
}
