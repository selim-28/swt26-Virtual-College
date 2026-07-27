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

/**
 * Fachlogik des Campus-Moduls. Raeume und Veranstaltungen sind fest hinterlegt,
 * die Reservierungen liegen dagegen in der Datenbank.
 */
@Service
public class CampusService {

    private final ReservierungRepository repository;

    /*
     * Raeume der FH Dortmund am Campus Emil-Figge-Strasse (Haus 40, 42 und 44).
     * Die lat/lng sind Gebaeudezentren aus OpenStreetMap, damit die Marker auf
     * den richtigen Haeusern sitzen.
     */
    private final List<CampusController.RaumInfo> raeume = List.of(
            new CampusController.RaumInfo(
                    "ef42-hoersaal1",
                    "Hoersaal 1",
                    "Haus 42 (Informatik)",
                    "Vorlesung",
                    "belegt",
                    false,   // Hoersaal: nur Professor:innen und Admin
                    250,
                    180,
                    52,
                    40,
                    51.49398, 7.42012,
                    List.of("Beamer", "Mikrofon", "Dozentenkamera"),
                    "Grosser Hoersaal im Informatik-Gebaeude der FH (Emil-Figge-Str. 42)."
            ),
            new CampusController.RaumInfo(
                    "ef40-kostbar",
                    "Mensa kostBar",
                    "Haus 40 (Hoersaalgebaeude)",
                    "Mensa",
                    "geoeffnet",
                    false,   // offener Ort, nicht reservierbar
                    180,
                    95,
                    78,
                    28,
                    51.49430, 7.42072,
                    List.of("Speiseplan live", "Snacks", "Mittagsmenues"),
                    "Mensa des Studierendenwerks im Hoersaalgebaeude - der Speiseplan kommt live aus den Fremddaten."
            ),
            new CampusController.RaumInfo(
                    "ef42-innovationlab",
                    "Innovation Lab",
                    "Haus 42 (Informatik)",
                    "Labor",
                    "frei",
                    true,
                    20,
                    4,
                    50,
                    44,
                    51.49392, 7.42022,
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
                    51.49380, 7.42048,
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
                    51.49386, 7.42035,
                    List.of("Whiteboard", "Beamer", "WLAN"),
                    "Uebungen und Lerngruppen des FB Informatik."
            ),
            new CampusController.RaumInfo(
                    "ef44-bibliothek",
                    "FH-Bibliothek",
                    "Haus 44",
                    "Lernort",
                    "geoeffnet",
                    false,   // offener Lernort, nicht reservierbar
                    120,
                    64,
                    32,
                    58,
                    51.49388, 7.41895,
                    List.of("Stillarbeit", "Gruppenraeume", "Ausleihe"),
                    "Bibliothek der FH Dortmund am Standort Emil-Figge-Str. 44 - offener Lernort."
            )
    );

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

    public List<CampusController.RaumInfo> raeume() {
        return raeume;
    }

    public List<CampusController.VeranstaltungInfo> veranstaltungen() {
        return veranstaltungen;
    }

    public List<GespeicherteReservierung> reservierungen() {
        return repository.findAllByOrderByIdDesc();
    }

    /**
     * Legt eine Reservierung an (US 3.2). Es gelten drei Regeln: der Raum muss
     * fuer die Rolle reservierbar sein, das Ende muss nach dem Beginn liegen und
     * der Zeitraum darf sich nicht mit einer vorhandenen Reservierung ueberschneiden.
     * Verstoesse werden als ResponseStatusException gemeldet und von der
     * FehlerBehandlung in JSON mit Klartextmeldung uebersetzt.
     */
    public GespeicherteReservierung reserviere(CampusController.NeueReservierung anfrage,
                                               String rolle) {
        CampusController.RaumInfo raum = findeRaum(anfrage.raumId());

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

        LocalTime beginn = alsUhrzeit(von);
        LocalTime ende = alsUhrzeit(bis);
        if (!ende.isAfter(beginn)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Das Ende (" + bis + ") muss nach dem Beginn (" + von + ") liegen.");
        }

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

    /**
     * Rollenregel: Labore und Seminarraeume duerfen alle Angemeldeten reservieren,
     * Hoersaele nur Professor:innen und Admins, Mensa und Bibliothek niemand.
     */
    private boolean darfReservieren(CampusController.RaumInfo raum, String rolle) {
        if (raum.reservierbar()) {
            return true;
        }
        boolean privilegiert = "Professor".equalsIgnoreCase(rolle)
                || "Admin".equalsIgnoreCase(rolle);
        return privilegiert && "Vorlesung".equals(raum.typ());
    }

    /**
     * Entfernt eine Reservierung. Die Beschraenkung auf die Admin-Rolle erzwingt
     * die SecurityConfig ueber die DELETE-Regel.
     */
    public void loescheReservierung(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Reservierung nicht gefunden: " + id);
        }
        repository.deleteById(id);
    }

    private CampusController.RaumInfo findeRaum(String raumId) {
        return raeume.stream()
                .filter(raum -> raum.id().equals(raumId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Raum nicht gefunden: " + raumId));
    }

    /** Wandelt "HH:mm" in eine Uhrzeit um; unlesbare Eingaben werden abgelehnt. */
    private LocalTime alsUhrzeit(String text) {
        try {
            return LocalTime.parse(text);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ungueltige Uhrzeit: '" + text + "' (erwartet HH:mm).");
        }
    }

    /**
     * Zwei Zeitraeume ueberschneiden sich, wenn jeder vor dem Ende des anderen
     * beginnt. Direktes Angrenzen ist erlaubt.
     */
    private boolean ueberschneidet(LocalTime beginn, LocalTime ende,
                                   GespeicherteReservierung vorhanden) {
        try {
            LocalTime vorhandenBeginn = LocalTime.parse(vorhanden.getVon());
            LocalTime vorhandenEnde = LocalTime.parse(vorhanden.getBis());
            return beginn.isBefore(vorhandenEnde) && vorhandenBeginn.isBefore(ende);
        } catch (DateTimeParseException alteDaten) {
            // Altdaten mit unlesbarer Zeit blockieren keine neuen Buchungen.
            return false;
        }
    }

    /** Ersetzt leere Eingaben durch einen Standardwert. */
    private String textOderStandard(String wert, String standard) {
        if (wert == null || wert.isBlank()) {
            return standard;
        }
        return wert.trim();
    }
}
