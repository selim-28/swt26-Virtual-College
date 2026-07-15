package swt2.virtualcollege.receive.campus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import swt2.virtualcollege.receive.persistenz.GespeicherteReservierung;
import swt2.virtualcollege.receive.persistenz.ReservierungRepository;

/*
 * EPIC 3 – Raumverwaltung und Reservierung.
 *   US 3.1 "Raumbelegung einsehen" (Issue #6)
 *   US 3.2 "Raum reservieren" (Issue #24)
 *
 * Getestet wird die Fachlogik im CampusService ISOLIERT: Das Repository
 * (Datenbank-Zugang) wird durch ein Mockito-Mock ersetzt. So laufen die Tests
 * schnell und ohne echte Datenbank – wir pruefen nur die Regeln des Service.
 *
 * Die Raeume sind die 6 kuratierten FH-Dortmund-Raeume am Campus
 * Emil-Figge-Strasse (Haus 40/42/44). ROLLENREGEL: Hoersaele reservieren nur
 * Professor:innen/Admins; Mensa (kostBar) und Bibliothek niemand.
 */
class CampusServiceTest {

    private final ReservierungRepository repository = mock(ReservierungRepository.class);
    private final CampusService service = new CampusService(repository);

    // --- US 3.2: "Die Reservierung wird dem angemeldeten Nutzer zugeordnet." ---
    @Test
    void reservierungWirdDemNutzerUndRaumZugeordnet() {
        // save gibt die uebergebene Reservierung einfach zurueck (wie die echte DB).
        when(repository.save(any(GespeicherteReservierung.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        var anfrage = new CampusController.NeueReservierung(
                "ef42-innovationlab", "Max Mustermann", "14:00", "16:00");
        GespeicherteReservierung ergebnis = service.reserviere(anfrage, "Student");

        assertThat(ergebnis.getRaumId()).isEqualTo("ef42-innovationlab");
        assertThat(ergebnis.getRaumName()).isEqualTo("Innovation Lab");   // Name aus der Raumliste
        assertThat(ergebnis.getReserviertVon()).isEqualTo("Max Mustermann");
        assertThat(ergebnis.getVon()).isEqualTo("14:00");
        assertThat(ergebnis.getBis()).isEqualTo("16:00");
        // US 3.2: nach erfolgreicher Reservierung ist sie bestaetigt ...
        assertThat(ergebnis.getStatus()).isEqualTo("bestaetigt");
        // ... und wurde wirklich gespeichert (dauerhaft in der DB).
        verify(repository).save(any(GespeicherteReservierung.class));
    }

    // --- US 3.2: "Vor der Reservierung muss ein Raum ausgewaehlt sein." ---
    @Test
    void unbekannterRaumWirdAbgelehnt() {
        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("gibt-es-nicht", "Max", "10:00", "11:00"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Raum nicht gefunden");

        verify(repository, never()).save(any());
    }

    // --- ROLLENREGEL: Mensa (kostBar) und Bibliothek kann NIEMAND reservieren,
    //     auch der Admin nicht (offene Orte). ---
    @Test
    void mensaUndBibliothekKannNiemandReservieren() {
        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("ef40-kostbar", "Max", "12:00", "13:00"),
                "Admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("kann nicht reserviert werden");

        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("ef44-bibliothek", "Max", "12:00", "13:00"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("kann nicht reserviert werden");

        verify(repository, never()).save(any());
    }

    // --- ROLLENREGEL: Hoersaal ist fuer Studierende gesperrt ... ---
    @Test
    void studentDarfHoersaalNichtReservieren() {
        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("ef42-hoersaal1", "Max", "08:00", "09:00"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Hoersaal");

        verify(repository, never()).save(any());
    }

    // --- ... aber Professor:innen und Admins duerfen ihn reservieren. ---
    @Test
    void professorUndAdminDuerfenHoersaalReservieren() {
        when(repository.save(any(GespeicherteReservierung.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        GespeicherteReservierung profBuchung = service.reserviere(
                new CampusController.NeueReservierung("ef42-hoersaal1", "Prof. Demo", "08:00", "09:00"),
                "Professor");
        assertThat(profBuchung.getRaumName()).isEqualTo("Hoersaal 1");
        assertThat(profBuchung.getStatus()).isEqualTo("bestaetigt");

        GespeicherteReservierung adminBuchung = service.reserviere(
                new CampusController.NeueReservierung("ef42-hoersaal1", "Demo Admin", "09:00", "10:00"),
                "Admin");
        assertThat(adminBuchung.getReserviertVon()).isEqualTo("Demo Admin");
    }

    // --- US 3.2 (Regel 2): Das Ende muss nach dem Beginn liegen. ---
    @Test
    void endeVorDemBeginnWirdAbgelehnt() {
        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("ef42-innovationlab", "Max", "14:00", "13:00"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nach dem Beginn");

        verify(repository, never()).save(any());
    }

    // --- US 3.2 (Regel 3): Keine Ueberschneidung mit vorhandener Reservierung. ---
    @Test
    void ueberschneidungMitVorhandenerReservierungWirdAbgelehnt() {
        when(repository.findAllByRaumIdOrderByIdDesc("ef42-innovationlab")).thenReturn(List.of(
                new GespeicherteReservierung("ef42-innovationlab", "Innovation Lab", "Anna",
                        "12:00", "13:00", "bestaetigt", Instant.now())));

        // 12:30-13:30 ueberlappt die vorhandene 12:00-13:00 -> Konflikt.
        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("ef42-innovationlab", "Max", "12:30", "13:30"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bereits reserviert");

        verify(repository, never()).save(any());
    }

    // --- US 3.2 (Regel 3, Grenzfall): direkt ANGRENZEN ist erlaubt. ---
    @Test
    void angrenzendeReservierungIstErlaubt() {
        when(repository.findAllByRaumIdOrderByIdDesc("ef42-innovationlab")).thenReturn(List.of(
                new GespeicherteReservierung("ef42-innovationlab", "Innovation Lab", "Anna",
                        "12:00", "13:00", "bestaetigt", Instant.now())));
        when(repository.save(any(GespeicherteReservierung.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        // 13:00-14:00 beginnt exakt, wenn die andere endet -> kein Konflikt.
        GespeicherteReservierung ergebnis = service.reserviere(
                new CampusController.NeueReservierung("ef42-innovationlab", "Max", "13:00", "14:00"),
                "Student");

        assertThat(ergebnis.getVon()).isEqualTo("13:00");
        assertThat(ergebnis.getStatus()).isEqualTo("bestaetigt");
        verify(repository).save(any(GespeicherteReservierung.class));
    }

    // --- US 3.2: robuste Eingaben – leere Zeiten bekommen sinnvolle Standardwerte. ---
    @Test
    void leereZeitenBekommenStandardwerte() {
        when(repository.save(any(GespeicherteReservierung.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        GespeicherteReservierung ergebnis = service.reserviere(
                new CampusController.NeueReservierung("ef42-innovationlab", "  ", "", "  "),
                "Student");

        assertThat(ergebnis.getReserviertVon()).isEqualTo("Student");   // Standard
        assertThat(ergebnis.getVon()).isEqualTo("12:00");
        assertThat(ergebnis.getBis()).isEqualTo("13:00");
    }

    // --- Admin-Verwaltung: Reservierung loeschen (US 3 + Admin-Rolle) ---
    @Test
    void loeschenEntferntVorhandeneReservierungUndMeldetUnbekannteIds() {
        when(repository.existsById(7L)).thenReturn(true);
        service.loescheReservierung(7L);
        verify(repository).deleteById(7L);

        when(repository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.loescheReservierung(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nicht gefunden");
    }

    // --- US 3.1: "Veranstaltungen werden mit ihrer Uhrzeit angezeigt." ---
    @Test
    void veranstaltungenWerdenMitUhrzeitGeliefert() {
        var veranstaltungen = service.veranstaltungen();

        assertThat(veranstaltungen).isNotEmpty();
        assertThat(veranstaltungen).allSatisfy(v -> {
            assertThat(v.von()).isNotBlank();   // Beginn-Uhrzeit vorhanden
            assertThat(v.bis()).isNotBlank();   // Ende-Uhrzeit vorhanden
            assertThat(v.raumId()).isNotBlank(); // gehoert zu einem Raum
        });
    }

    // --- US 3.1: "Vorhandene Reservierungen werden angezeigt." (aus der DB) ---
    @Test
    void gespeicherteReservierungenWerdenGeliefert() {
        var gespeichert = new GespeicherteReservierung(
                "ef42-seminarraum", "Seminarraum 128", "Anna",
                "09:00", "10:00", "bestaetigt", Instant.now());
        when(repository.findAllByOrderByIdDesc()).thenReturn(List.of(gespeichert));

        var reservierungen = service.reservierungen();

        assertThat(reservierungen).hasSize(1);
        assertThat(reservierungen.get(0).getRaumName()).isEqualTo("Seminarraum 128");
        assertThat(reservierungen.get(0).getReserviertVon()).isEqualTo("Anna");
    }

    // --- US 3.1: die 6 FH-Raeume (Emil-Figge-Str.) sind da - OHNE TU-Audimax. ---
    @Test
    void raeumeSindDieSechsFhRaeumeOhneAudimax() {
        var raeume = service.raeume();

        assertThat(raeume).hasSize(6);
        // Entfernt (Nutzerwunsch): Computerraum und Seminarraum 210 in Haus 44.
        assertThat(raeume).noneMatch(r -> r.id().equals("ef44-computerraum"));
        assertThat(raeume).noneMatch(r -> r.id().equals("ef44-seminarraum"));
        // Der Hoersaal liegt jetzt im Informatik-Gebaeude (Haus 42).
        assertThat(raeume).anyMatch(r -> r.id().equals("ef42-hoersaal1")
                && r.gebaeude().contains("Informatik"));
        // Der Audimax gehoert zur TU Dortmund - er darf NICHT auftauchen.
        assertThat(raeume).noneMatch(r -> r.name().toLowerCase().contains("audimax"));
        // Stichproben der echten FH-Orte (Haus 40/42/44).
        assertThat(raeume).anyMatch(r -> r.id().equals("ef40-kostbar"));       // Mensa kostBar
        assertThat(raeume).anyMatch(r -> r.id().equals("ef42-innovationlab")); // FB Informatik
        assertThat(raeume).anyMatch(r -> r.id().equals("ef44-bibliothek"));    // FH-Bibliothek
        assertThat(raeume).allSatisfy(r -> assertThat(r.name()).isNotBlank());
    }
}
