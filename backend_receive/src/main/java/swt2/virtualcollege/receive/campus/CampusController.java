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

/*
 * REST-Controller fuer den Live-Campus.
 *
 * Das Frontend bekommt hier die Karte, Raeume, Veranstaltungen und
 * Reservierungen. Dadurch wird aus dem Kanban-Screen "Live Campus" eine
 * echte Funktion: ansehen, Raum auswaehlen, reservieren, speichern.
 */
@RestController
@RequestMapping("/api/campus")
public class CampusController {

    // Service enthaelt die Fachlogik und Datenbank-Zugriffe.
    private final CampusService service;

    public CampusController(CampusService service) {
        this.service = service;
    }

    // GET /api/campus/karte: alle Daten fuer die Campus-Karte.
    @GetMapping("/karte")
    public CampusAntwort karte() {
        return new CampusAntwort(
                service.raeume(),
                service.veranstaltungen(),
                service.reservierungen().stream().map(this::alsAntwort).toList()
        );
    }

    /*
     * POST /api/campus/reservierungen: Raum reservieren.
     *
     * WICHTIG (US 3.2 "dem angemeldeten Nutzer zugeordnet"): Name und Rolle
     * kommen aus dem GEPRUEFTEN JWT (Authentication-Principal), nicht aus dem
     * Request - so kann niemand im Namen anderer reservieren.
     */
    @PostMapping("/reservierungen")
    public ReservierungAntwort reservieren(@RequestBody NeueReservierung anfrage,
                                           Authentication auth) {
        AngemeldeterNutzer nutzer = (AngemeldeterNutzer) auth.getPrincipal();
        NeueReservierung zugeordnet = new NeueReservierung(
                anfrage.raumId(), nutzer.name(), anfrage.von(), anfrage.bis());
        return alsAntwort(service.reserviere(zugeordnet, nutzer.rolle()));
    }

    // DELETE /api/campus/reservierungen/{id}: Reservierung entfernen.
    // Die SecurityConfig laesst DELETE nur fuer die Rolle ADMIN durch.
    @DeleteMapping("/reservierungen/{id}")
    public void loeschen(@PathVariable Long id) {
        service.loescheReservierung(id);
    }

    // Entity -> JSON-Antwort.
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

    // Antwort fuer die gesamte Karte.
    public record CampusAntwort(List<RaumInfo> raeume,
                                List<VeranstaltungInfo> veranstaltungen,
                                List<ReservierungAntwort> reservierungen) {
    }

    // Raumdaten, die das React-Frontend direkt zeichnen kann.
    // x/y = Position auf der gemalten Fallback-Karte (in Prozent).
    // lat/lng = echte GPS-Koordinaten fuer die Leaflet-Karte (Satellit/Strasse).
    // reservierbar = duerfen Studierende diesen Raum buchen? (US 3.2: nur
    // "freie Raeume" wie Labore/Seminarraeume - NICHT Mensa oder Vorlesungssaal.)
    public record RaumInfo(String id, String name, String gebaeude, String typ,
                           String status, boolean reservierbar,
                           int kapazitaet, int personen,
                           int x, int y, double lat, double lng,
                           List<String> ausstattung,
                           String beschreibung) {
    }

    // Kleine Veranstaltungsdaten fuer Marker und Detailanzeigen.
    public record VeranstaltungInfo(String id, String titel, String raumId,
                                    String von, String bis, String dozent,
                                    String status) {
    }

    // Anfrage aus dem Frontend.
    public record NeueReservierung(String raumId, String reserviertVon,
                                   String von, String bis) {
    }

    // Antwort nach erfolgreicher Reservierung.
    public record ReservierungAntwort(Long id, String raumId, String raumName,
                                      String reserviertVon, String von, String bis,
                                      String status, String erstelltAm) {
    }
}
