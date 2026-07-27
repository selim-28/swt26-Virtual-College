package swt2.virtualcollege.receive.campus;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import swt2.virtualcollege.receive.auth.AngemeldeterNutzer;
import swt2.virtualcollege.receive.persistenz.GespeicherteReservierung;

/**
 * REST-Schnittstelle des Campus-Moduls: Karte mit Raeumen und Veranstaltungen
 * sowie Anlegen und Loeschen von Reservierungen.
 */
@RestController
@RequestMapping("/api/campus")
public class CampusController {

    private final CampusService service;

    public CampusController(CampusService service) {
        this.service = service;
    }

    @GetMapping("/karte")
    public CampusAntwort karte() {
        return new CampusAntwort(
                service.raeume(),
                service.veranstaltungen(),
                service.reservierungen().stream().map(this::alsAntwort).toList()
        );
    }

    /**
     * Reserviert einen Raum. Name und Rolle stammen aus dem geprueften Token und
     * nicht aus dem Request, damit niemand im Namen anderer reserviert (US 3.2).
     */
    @PostMapping("/reservierungen")
    public ReservierungAntwort reservieren(@RequestBody NeueReservierung anfrage,
                                           Authentication auth) {
        AngemeldeterNutzer nutzer = (AngemeldeterNutzer) auth.getPrincipal();
        NeueReservierung zugeordnet = new NeueReservierung(
                anfrage.raumId(), nutzer.name(), anfrage.von(), anfrage.bis());
        return alsAntwort(service.reserviere(zugeordnet, nutzer.rolle()));
    }

    /** Loeschen ist ueber die SecurityConfig auf die Admin-Rolle beschraenkt. */
    @DeleteMapping("/reservierungen/{id}")
    public void loeschen(@PathVariable Long id) {
        service.loescheReservierung(id);
    }

    /** Wandelt die Entity in die JSON-Antwort um. */
    private ReservierungAntwort alsAntwort(GespeicherteReservierung reservierung) {
        return new ReservierungAntwort(
                reservierung.getId(),
                reservierung.getRaumId(),
                reservierung.getRaumName(),
                reservierung.getReserviertVon(),
                reservierung.getVon(),
                reservierung.getBis(),
                reservierung.getStatus(),
                reservierung.getErstelltAm().toString()
        );
    }

    /** Antwort mit allen Daten der Campus-Karte. */
    public record CampusAntwort(List<RaumInfo> raeume,
                                List<VeranstaltungInfo> veranstaltungen,
                                List<ReservierungAntwort> reservierungen) {
    }

    /**
     * Raumdaten fuer das Frontend. x/y ist die Position auf der gezeichneten
     * Ersatzkarte in Prozent, lat/lng die Koordinate fuer die Leaflet-Karte.
     */
    public record RaumInfo(String id, String name, String gebaeude, String typ,
                           String status, boolean reservierbar,
                           int kapazitaet, int personen,
                           int x, int y, double lat, double lng,
                           List<String> ausstattung,
                           String beschreibung) {
    }

    /** Veranstaltungsdaten fuer Marker und Detailanzeige. */
    public record VeranstaltungInfo(String id, String titel, String raumId,
                                    String von, String bis, String dozent,
                                    String status) {
    }

    /** Anfrage des Frontends beim Reservieren. */
    public record NeueReservierung(String raumId, String reserviertVon,
                                   String von, String bis) {
    }

    /** Antwort nach erfolgreicher Reservierung. */
    public record ReservierungAntwort(Long id, String raumId, String raumName,
                                      String reserviertVon, String von, String bis,
                                      String status, String erstelltAm) {
    }
}
