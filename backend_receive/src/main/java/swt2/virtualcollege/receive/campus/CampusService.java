package swt2.virtualcollege.receive.campus;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    /*
     * Kuratierte Raumliste: 6 Raeume der FH DORTMUND am Campus Emil-Figge-Strasse
     * (Quelle: fh-dortmund.de "Campus Emil-Figge-Strasse" + stwdo.de).
     *   Haus 40 = Hoersaalgebaeude mit Mensa "kostBar" des Studierendenwerks
     *   Haus 42 = FB Informatik (unser Hoersaal, Innovation Lab, Hardware-Labor, Seminarraum)
     *   Haus 44 = FH-Bibliothek
     * Der Audimax gehoert zur TU Dortmund und ist deshalb NICHT dabei.
     *
     * Die lat/lng sind ECHTE OpenStreetMap-Gebaeudezentren (per Overpass geholt),
     * damit die Marker auf den richtigen Haeusern sitzen und sichtbar verteilt sind:
     *   kostBar (Hoersaalgebaeude)   ~51.4943, 7.4207  (Nordosten)
     *   FB Informatik  (Haus 42)     ~51.4939, 7.4203  (Mitte - die meisten Raeume)
     *   FH-Bibliothek  (Haus 44)     ~51.4939, 7.4190  (Westen)
     * Raeume im selben Haus liegen bewusst nah beieinander (sie SIND im selben
     * Gebaeude) und trennen sich beim Hineinzoomen.
     */
    private final List<CampusController.RaumInfo> raeume = List.of(
            new CampusController.RaumInfo(
                    "ef42-hoersaal1",
                    "Hoersaal 1",
                    "Haus 42 (Informatik)",
                    "Vorlesung",
                    "belegt",
                    false,   // Hoersaele reservieren nur Professor:innen/Admin (Rollenregel)
                    250,
                    180,
                    52,
                    40,
                    51.49398, 7.42012,   // FB Informatik, Nordteil (Hoersaal)
                    List.of("Beamer", "Mikrofon", "Dozentenkamera"),
                    "Grosser Hoersaal im Informatik-Gebaeude der FH (Emil-Figge-Str. 42)."
            ),
            new CampusController.RaumInfo(
                    "ef40-kostbar",
                    "Mensa kostBar",
                    "Haus 40 (Hoersaalgebaeude)",
                    "Mensa",
                    "geoeffnet",
                    false,   // eine Mensa ist kein reservierbarer Raum
                    180,
                    95,
                    78,
                    28,
                    51.49430, 7.42072,   // echtes kostBar-Gebaeude (OSM)
                    List.of("Speiseplan live", "Snacks", "Mittagsmenues"),
                    "Mensa des Studierendenwerks im Hoersaalgebaeude - der Speiseplan kommt live aus den Fremddaten."
            ),
            new CampusController.RaumInfo(
                    "ef42-innovationlab",
                    "Innovation Lab",
                    "Haus 42 (Informatik)",
                    "Labor",
                    "frei",
                    true,    // Projekt-/Lernraum: fuer alle reservierbar
                    20,
                    4,
                    50,
                    44,
                    51.49392, 7.42022,   // FB Informatik, Nordwestteil
                    List.of("Prototyping", "3D-Drucker", "WLAN"),
                    "Kreativ- und Projektraum des FB Informatik (Haus 42)."
            ),
            new CampusController.RaumInfo(
                    "ef42-hardwarelabor",
                    "Hardware-Labor",
                    "Haus 42 (Informatik)",
                    "Labor",
                    "frei",
                    true,
                    24,
                    6,
                    56,
                    56,
                    51.49380, 7.42048,   // FB Informatik, Suedostteil
                    List.of("Messplaetze", "Loetstationen", "Oszilloskope"),
                    "Hardware-Praktika des FB Informatik (Haus 42)."
            ),
            new CampusController.RaumInfo(
                    "ef42-seminarraum",
                    "Seminarraum 128",
                    "Haus 42 (Informatik)",
                    "Uebung",
                    "beginnt bald",
                    true,
                    36,
                    18,
                    46,
                    52,
                    51.49386, 7.42035,   // FB Informatik, Mitte
                    List.of("Whiteboard", "Beamer", "WLAN"),
                    "Uebungen und Lerngruppen des FB Informatik."
            ),
            new CampusController.RaumInfo(
                    "ef44-bibliothek",
                    "FH-Bibliothek",
                    "Haus 44",
                    "Lernort",
                    "geoeffnet",
                    false,   // offener Lernort: einfach hingehen, keine Reservierung
                    120,
                    64,
                    32,
                    58,
                    51.49388, 7.41895,   // echte FH-Bibliothek (EFS), OSM
                    List.of("Stillarbeit", "Gruppenraeume", "Ausleihe"),
                    "Bibliothek der FH Dortmund am Standort Emil-Figge-Str. 44 - offener Lernort."
            )
    );

    // Statische Veranstaltungen: sichtbar in der Karte und den Details.
    private final List<CampusController.VeranstaltungInfo> veranstaltungen = List.of(
            new CampusController.VeranstaltungInfo(
                    "swt2",
                    "Softwaretechnik 2",
                    "ef42-hoersaal1",
                    "14:15",
                    "17:35",
                    "Prof. Sachweh / Team",
                    "heute"
            ),
            new CampusController.VeranstaltungInfo(
                    "java-uebung",
                    "Programmierung Uebung",
                    "ef42-seminarraum",
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

    /*
     * Neue Reservierung anlegen und speichern (US 3.2).
     *
     * Fachliche Regeln (vorher fehlten sie - man konnte sogar die Mensa
     * doppelt reservieren!):
     *   1. Nur reservierbare Raeume - MIT ROLLENREGEL: Hoersaele duerfen
     *      zusaetzlich Professor:innen und Admins reservieren; Mensa und
     *      Bibliothek darf NIEMAND reservieren (offene Orte).
     *   2. Das Ende muss NACH dem Beginn liegen.
     *   3. Keine Ueberschneidung mit einer vorhandenen Reservierung des Raums.
     * Verstoesse werden als ResponseStatusException gemeldet - die
     * FehlerBehandlung macht daraus sauberes JSON mit Klartext-Meldung,
     * das das Frontend direkt anzeigen kann.
     */
    public GespeicherteReservierung reserviere(CampusController.NeueReservierung anfrage,
                                               String rolle) {
        CampusController.RaumInfo raum = findeRaum(anfrage.raumId());

        // Regel 1 (mit Rolle): Wer darf DIESEN Raum reservieren?
        if (!darfReservieren(raum, rolle)) {
            String meldung = "Vorlesung".equals(raum.typ())
                    ? raum.name() + " ist ein Hoersaal - reservieren duerfen ihn nur "
                            + "Professor:innen oder die Verwaltung (Admin)."
                    : raum.name() + " kann nicht reserviert werden (" + raum.typ() + ").";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, meldung);
        }

        String person = textOderStandard(anfrage.reserviertVon(), "Student");
        String von = textOderStandard(anfrage.von(), "12:00");
        String bis = textOderStandard(anfrage.bis(), "13:00");

        // Regel 2: Beginn/Ende muessen echte Uhrzeiten sein, Ende nach Beginn.
        LocalTime beginn = alsUhrzeit(von);
        LocalTime ende = alsUhrzeit(bis);
        if (!ende.isAfter(beginn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Das Ende (" + bis + ") muss nach dem Beginn (" + von + ") liegen.");
        }

        // Regel 3: Der Zeitraum darf sich nicht mit einer vorhandenen
        // Reservierung DESSELBEN Raums ueberschneiden.
        for (GespeicherteReservierung vorhanden : repository.findAllByRaumIdOrderByIdDesc(raum.id())) {
            if (ueberschneidet(beginn, ende, vorhanden)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        raum.name() + " ist von " + vorhanden.getVon() + " bis "
                                + vorhanden.getBis() + " bereits reserviert.");
            }
        }

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

    /*
     * ROLLENREGEL: Wer darf welchen Raum reservieren?
     *   - reservierbare Raeume (Labore, Seminarraeume, PC-Pools): alle Angemeldeten
     *   - Hoersaele (typ "Vorlesung"): nur Professor:innen und Admins
     *   - Mensa/Bibliothek: niemand (offene Orte, keine Buchung noetig)
     */
    private boolean darfReservieren(CampusController.RaumInfo raum, String rolle) {
        if (raum.reservierbar()) {
            return true;
        }
        boolean privilegiert = "Professor".equalsIgnoreCase(rolle)
                || "Admin".equalsIgnoreCase(rolle);
        return privilegiert && "Vorlesung".equals(raum.typ());
    }

    /*
     * Reservierung loeschen (nur Admin - das erzwingt die SecurityConfig,
     * die DELETE /api/** ausschliesslich der Rolle ADMIN erlaubt).
     */
    public void loescheReservierung(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Reservierung nicht gefunden: " + id);
        }
        repository.deleteById(id);
    }

    // Sucht einen Raum anhand seiner ID.
    private CampusController.RaumInfo findeRaum(String raumId) {
        return raeume.stream()
                .filter(raum -> raum.id().equals(raumId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Raum nicht gefunden: " + raumId));
    }

    // Wandelt "HH:mm" in eine echte Uhrzeit um; unlesbare Eingaben werden abgelehnt.
    private LocalTime alsUhrzeit(String text) {
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ungueltige Uhrzeit: '" + text + "' (erwartet HH:mm).");
        }
    }

    /*
     * Zwei Zeitraeume ueberschneiden sich, wenn jeder VOR dem Ende des anderen
     * beginnt. Angrenzen (13:00-14:00 nach 12:00-13:00) ist ausdruecklich ok.
     */
    private boolean ueberschneidet(LocalTime beginn, LocalTime ende,
                                   GespeicherteReservierung vorhanden) {
        try {
            LocalTime vorhandenBeginn = LocalTime.parse(vorhanden.getVon());
            LocalTime vorhandenEnde = LocalTime.parse(vorhanden.getBis());
            return beginn.isBefore(vorhandenEnde) && vorhandenBeginn.isBefore(ende);
        } catch (DateTimeParseException alteDaten) {
            // Alte Eintraege mit unlesbarer Zeit blockieren neue Buchungen nicht.
            return false;
        }
    }

    // Kleine Hilfsmethode fuer robuste Nutzereingaben.
    private String textOderStandard(String wert, String standard) {
        if (wert == null || wert.isBlank()) {
            return standard;
        }
        return wert.trim();
    }
}
