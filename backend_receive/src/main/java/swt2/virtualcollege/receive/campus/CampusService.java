package swt2.virtualcollege.receive.campus;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import swt2.virtualcollege.receive.persistenz.GespeicherteReservierung;
import swt2.virtualcollege.receive.persistenz.ReservierungRepository;

/*
 * Fachlogik fuer den Live-Campus.
 *
 * Die Raeume und Veranstaltungen sind fuer die erste Version bewusst
 * kuratiert/statisch, weil sie euer Kanban-Konzept abbilden. Die
 * Reservierungen sind dagegen echt persistent und landen in der Datenbank.
 */
@Service
public class CampusService {

    // Repository = Datenbank-Zugang fuer Reservierungen.
    private final ReservierungRepository repository;

    // Statische Raumliste: diese Daten bilden unsere Campus-Karte.
    private final List<CampusController.RaumInfo> raeume = List.of(
            new CampusController.RaumInfo(
                    "g42-audimax",
                    "Audimax",
                    "Gebaeude 42",
                    "Vorlesung",
                    "belegt",
                    200,
                    156,
                    58,
                    24,
                    51.49386, 7.42009,
                    List.of("Beamer", "Mikrofon", "WLAN"),
                    "Grosser Hoersaal fuer Vorlesungen und Infoveranstaltungen."
            ),
            new CampusController.RaumInfo(
                    "g42-mensa",
                    "Mensa Campus Nord",
                    "Gebaeude 42",
                    "Mensa",
                    "geoeffnet",
                    120,
                    83,
                    70,
                    34,
                    51.49432, 7.42072,
                    List.of("Speiseplan live", "Sitzplaetze", "Kaffee"),
                    "Mensa-Informationen kommen aus dem Live-Daten-Bereich."
            ),
            new CampusController.RaumInfo(
                    "g40-labor105",
                    "Labor 105",
                    "Gebaeude 40",
                    "Freier Raum",
                    "frei",
                    24,
                    3,
                    34,
                    58,
                    51.49412, 7.42139,
                    List.of("Computer-Pool", "Drucker", "WLAN"),
                    "Freier Lern- und Arbeitsraum fuer Studierende."
            ),
            new CampusController.RaumInfo(
                    "g40-seminar303",
                    "Seminarraum 303",
                    "Gebaeude 40",
                    "Uebung",
                    "beginnt bald",
                    36,
                    18,
                    47,
                    67,
                    51.49402, 7.42128,
                    List.of("Whiteboard", "Steckdosen", "WLAN"),
                    "Seminarraum fuer Uebungen, Lerngruppen und Workshops."
            )
    );

    // Statische Veranstaltungen: sichtbar in der Karte und den Details.
    private final List<CampusController.VeranstaltungInfo> veranstaltungen = List.of(
            new CampusController.VeranstaltungInfo(
                    "swt2",
                    "Softwaretechnik 2",
                    "g42-audimax",
                    "14:15",
                    "17:35",
                    "Prof. Sachweh / Team",
                    "heute"
            ),
            new CampusController.VeranstaltungInfo(
                    "java-uebung",
                    "Programmierung Uebung",
                    "g40-seminar303",
                    "10:00",
                    "11:30",
                    "Tutorium",
                    "beginnt bald"
            )
    );

    public CampusService(ReservierungRepository repository) {
        this.repository = repository;
    }

    // Alle Raeume fuer die Karte.
    public List<CampusController.RaumInfo> raeume() {
        return raeume;
    }

    // Alle Veranstaltungen fuer die Karte.
    public List<CampusController.VeranstaltungInfo> veranstaltungen() {
        return veranstaltungen;
    }

    // Alle gespeicherten Reservierungen.
    public List<GespeicherteReservierung> reservierungen() {
        return repository.findAllByOrderByIdDesc();
    }

    // Neue Reservierung anlegen und speichern.
    public GespeicherteReservierung reserviere(CampusController.NeueReservierung anfrage) {
        CampusController.RaumInfo raum = findeRaum(anfrage.raumId());
        String person = textOderStandard(anfrage.reserviertVon(), "Student");
        String von = textOderStandard(anfrage.von(), "12:00");
        String bis = textOderStandard(anfrage.bis(), "13:00");

        return repository.save(new GespeicherteReservierung(
                raum.id(),
                raum.name(),
                person,
                von,
                bis,
                "bestaetigt",
                Instant.now()
        ));
    }

    // Sucht einen Raum anhand seiner ID.
    private CampusController.RaumInfo findeRaum(String raumId) {
        return raeume.stream()
                .filter(raum -> raum.id().equals(raumId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Raum nicht gefunden: " + raumId));
    }

    // Kleine Hilfsmethode fuer robuste Nutzereingaben.
    private String textOderStandard(String wert, String standard) {
        if (wert == null || wert.isBlank()) {
            return standard;
        }
        return wert.trim();
    }
}
