package swt2.virtualcollege.receive.pinwand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import swt2.virtualcollege.receive.persistenz.GespeicherterPinwandPost;
import swt2.virtualcollege.receive.persistenz.PinwandPostRepository;

/**
 * US 4.1 (Issue #8), US 4.2 (Issue #25) und US 4.3 (Issue #26). Das Repository ist
 * gemockt, der BenachrichtigungsSpeicher dagegen echt, damit sich der Observer im
 * Live-Pfad nachweisen laesst.
 */
class PinwandServiceTest {

    private final PinwandPostRepository repository = mock(PinwandPostRepository.class);
    private final BenachrichtigungsSpeicher benachrichtigungen = new BenachrichtigungsSpeicher();
    private final PinwandService service = new PinwandService(repository, benachrichtigungen);

    // US 4.1 und 4.3: Erstellen speichert und benachrichtigt.
    @Test
    void erstellePostSpeichertUndBenachrichtigtBeobachter() {
        when(repository.save(any(GespeicherterPinwandPost.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        GespeicherterPinwandPost post = service.erstellePost(new PinwandController.NeuerPost(
                "Demo Student", "Student", "Campus", "ILIAS heute langsam", "Bitte Geduld."));

        assertThat(post.getAutor()).isEqualTo("Demo Student");
        assertThat(post.getTitel()).isEqualTo("ILIAS heute langsam");
        verify(repository).save(any(GespeicherterPinwandPost.class));

        assertThat(benachrichtigungen.alle()).hasSize(1);
        String text = benachrichtigungen.alle().get(0).text();
        assertThat(text).contains("ILIAS heute langsam");
        assertThat(text).contains("Demo Student");
        assertThat(text).contains("Campus");
    }

    // US 4.3: Nur registrierte Beobachter werden informiert.
    @Test
    void nurRegistrierteBeobachterWerdenInformiert() {
        final int[] zaehlerErster = {0};
        final int[] zaehlerZweiter = {0};
        Beobachter erster = b -> zaehlerErster[0]++;
        Beobachter zweiter = b -> zaehlerZweiter[0]++;

        Pinwand pinwand = new Pinwand();
        pinwand.abonniere(erster);
        pinwand.neuerPost(new Post("Testtitel", "Inhalt"));

        assertThat(zaehlerErster[0]).isEqualTo(1);
        assertThat(zaehlerZweiter[0]).isZero();

        pinwand.abonniere(zweiter);
        pinwand.neuerPost(new Post("Zweiter Beitrag", "Inhalt"));
        assertThat(zaehlerErster[0]).isEqualTo(2);
        assertThat(zaehlerZweiter[0]).isEqualTo(1);

        // Abmelden beendet die Benachrichtigungen.
        pinwand.meldeAb(erster);
        pinwand.neuerPost(new Post("Dritter Beitrag", "Inhalt"));
        assertThat(zaehlerErster[0]).isEqualTo(2);
        assertThat(zaehlerZweiter[0]).isEqualTo(2);
    }

    // US 4.3: Der Speicher behaelt nur die juengsten Eintraege.
    @Test
    void benachrichtigungsSpeicherBehaeltNurDieLetztenEintraege() {
        for (int i = 1; i <= BenachrichtigungsSpeicher.MAX_EINTRAEGE + 5; i++) {
            benachrichtigungen.aktualisiere(new Benachrichtigung("Meldung " + i));
        }

        assertThat(benachrichtigungen.alle()).hasSize(BenachrichtigungsSpeicher.MAX_EINTRAEGE);
        assertThat(benachrichtigungen.alle().get(0).text()).endsWith("Meldung 25");
    }

    // US 4.2: Like erhoeht den Zaehler und wird gespeichert.
    @Test
    void likeErhoehtZaehlerUndSpeichert() {
        GespeicherterPinwandPost post = new GespeicherterPinwandPost(
                "Anna", "Student", "Campus", "Titel", "Inhalt", java.time.Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(post));
        when(repository.save(any(GespeicherterPinwandPost.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        GespeicherterPinwandPost ergebnis = service.like(1L);

        assertThat(ergebnis.getLikes()).isEqualTo(1);
        verify(repository).save(post);
    }

    // US 4.1: Leere Pflichtfelder bekommen Standardwerte.
    @Test
    void leereFelderBekommenStandardwerte() {
        when(repository.save(any(GespeicherterPinwandPost.class)))
                .thenAnswer(aufruf -> aufruf.getArgument(0));

        GespeicherterPinwandPost post = service.erstellePost(
                new PinwandController.NeuerPost("", null, " ", "", null));

        assertThat(post.getAutor()).isEqualTo("Student");
        assertThat(post.getKategorie()).isEqualTo("Campus");
        assertThat(post.getTitel()).isEqualTo("Neuer Beitrag");
    }

    // Loeschen entfernt Beitraege; unbekannte IDs melden 404.
    @Test
    void loeschenEntferntPostsUndMeldetUnbekannteIds() {
        when(repository.existsById(3L)).thenReturn(true);
        service.loeschePost(3L);
        verify(repository).deleteById(3L);

        when(repository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.loeschePost(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nicht gefunden");
        verify(repository, never()).deleteById(99L);
    }
}
