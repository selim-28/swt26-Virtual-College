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

/**
 * US 3.1 (Issue #6) und US 3.2 (Issue #24): die Reservierungsregeln des
 * CampusService, isoliert gegen ein Mock-Repository geprueft.
 */
class CampusServiceTest {

    private final ReservierungRepository repository = mock(ReservierungRepository.class);
    private final CampusService service = new CampusService(repository);

    // US 3.2: Die Reservierung wird dem angemeldeten Nutzer zugeordnet.
    @Test
    void reservierungWirdDemNutzerUndRaumZugeordnet() {
        when(repository.save(any(GespeicherteReservierung.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        var anfrage = new CampusController.NeueReservierung(
                "ef42-innovationlab", "Max Mustermann", "14:00", "16:00");
        GespeicherteReservierung ergebnis = service.reserviere(anfrage, "Student");

        assertThat(ergebnis.getRaumId()).isEqualTo("ef42-innovationlab");
        assertThat(ergebnis.getRaumName()).isEqualTo("Innovation Lab");
        assertThat(ergebnis.getReserviertVon()).isEqualTo("Max Mustermann");
        assertThat(ergebnis.getVon()).isEqualTo("14:00");
        assertThat(ergebnis.getBis()).isEqualTo("16:00");
        assertThat(ergebnis.getStatus()).isEqualTo("bestaetigt");
        verify(repository).save(any(GespeicherteReservierung.class));
    }

    // US 3.2: Vor der Reservierung muss ein gueltiger Raum ausgewaehlt sein.
    @Test
    void unbekannterRaumWirdAbgelehnt() {
        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("gibt-es-nicht", "Max", "10:00", "11:00"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Raum nicht gefunden");

        verify(repository, never()).save(any());
    }

    // Rollenregel: Mensa und Bibliothek sind offene Orte und fuer niemanden buchbar.
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

    // Rollenregel: Hoersaal ist fuer Studierende gesperrt ...
    @Test
    void studentDarfHoersaalNichtReservieren() {
        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("ef42-hoersaal1", "Max", "08:00", "09:00"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Hoersaal");

        verify(repository, never()).save(any());
    }

    // ... aber Professor:innen und Admins duerfen ihn reservieren.
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

    // US 3.2: Das Ende muss nach dem Beginn liegen.
    @Test
    void endeVorDemBeginnWirdAbgelehnt() {
        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("ef42-innovationlab", "Max", "14:00", "13:00"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nach dem Beginn");

        verify(repository, never()).save(any());
    }

    // US 3.2: Keine Ueberschneidung mit einer vorhandenen Reservierung.
    @Test
    void ueberschneidungMitVorhandenerReservierungWirdAbgelehnt() {
        when(repository.findAllByRaumIdOrderByIdDesc("ef42-innovationlab")).thenReturn(List.of(
                new GespeicherteReservierung("ef42-innovationlab", "Innovation Lab", "Anna",
                        "12:00", "13:00", "bestaetigt", Instant.now())));

        assertThatThrownBy(() -> service.reserviere(
                new CampusController.NeueReservierung("ef42-innovationlab", "Max", "12:30", "13:30"),
                "Student"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("bereits reserviert");

        verify(repository, never()).save(any());
    }

    // Grenzfall: direktes Angrenzen ist erlaubt.
    @Test
    void angrenzendeReservierungIstErlaubt() {
        when(repository.findAllByRaumIdOrderByIdDesc("ef42-innovationlab")).thenReturn(List.of(
                new GespeicherteReservierung("ef42-innovationlab", "Innovation Lab", "Anna",
                        "12:00", "13:00", "bestaetigt", Instant.now())));
        when(repository.save(any(GespeicherteReservierung.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        GespeicherteReservierung ergebnis = service.reserviere(
                new CampusController.NeueReservierung("ef42-innovationlab", "Max", "13:00", "14:00"),
                "Student");

        assertThat(ergebnis.getVon()).isEqualTo("13:00");
        assertThat(ergebnis.getStatus()).isEqualTo("bestaetigt");
        verify(repository).save(any(GespeicherteReservierung.class));
    }

    // US 3.2: Leere Eingaben bekommen sinnvolle Standardwerte.
    @Test
    void leereZeitenBekommenStandardwerte() {
        when(repository.save(any(GespeicherteReservierung.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        GespeicherteReservierung ergebnis = service.reserviere(
                new CampusController.NeueReservierung("ef42-innovationlab", "  ", "", "  "),
                "Student");

        assertThat(ergebnis.getReserviertVon()).isEqualTo("Student");
        assertThat(ergebnis.getVon()).isEqualTo("12:00");
        assertThat(ergebnis.getBis()).isEqualTo("13:00");
    }

    // Loeschen durch die Verwaltung.
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

    // US 3.1: Veranstaltungen werden mit ihrer Uhrzeit angezeigt.
    @Test
    void veranstaltungenWerdenMitUhrzeitGeliefert() {
        var veranstaltungen = service.veranstaltungen();

        assertThat(veranstaltungen).isNotEmpty();
        assertThat(veranstaltungen).allSatisfy(v -> {
            assertThat(v.von()).isNotBlank();
            assertThat(v.bis()).isNotBlank();
            assertThat(v.raumId()).isNotBlank();
        });
    }

    // US 3.1: Vorhandene Reservierungen werden angezeigt.
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

    // US 3.1: Es werden genau die sechs Raeume der FH am Campus geliefert.
    // Der Audimax gehoert zur TU Dortmund und darf nicht enthalten sein.
    @Test
    void raeumeSindDieSechsFhRaeumeOhneAudimax() {
        var raeume = service.raeume();

        assertThat(raeume).hasSize(6);
        assertThat(raeume).noneMatch(r -> r.id().equals("ef44-computerraum"));
        assertThat(raeume).noneMatch(r -> r.id().equals("ef44-seminarraum"));
        assertThat(raeume).anyMatch(r -> r.id().equals("ef42-hoersaal1")
                && r.gebaeude().contains("Informatik"));
        assertThat(raeume).noneMatch(r -> r.name().toLowerCase().contains("audimax"));
        assertThat(raeume).anyMatch(r -> r.id().equals("ef40-kostbar"));
        assertThat(raeume).anyMatch(r -> r.id().equals("ef42-innovationlab"));
        assertThat(raeume).anyMatch(r -> r.id().equals("ef44-bibliothek"));
        assertThat(raeume).allSatisfy(r -> assertThat(r.name()).isNotBlank());
    }
}
