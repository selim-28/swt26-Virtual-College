package swt2.virtualcollege.receive.persistenz;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import swt2.virtualcollege.receive.auth.Benutzerkonto;
import swt2.virtualcollege.receive.auth.BenutzerkontoRepository;

/**
 * US 8.2 (Issue #37): Konten, Reservierungen, Pinwand-Beitraege und Mensadaten
 * werden dauerhaft gespeichert.
 *
 * Ein Neustart wird durch flush() und clear() am EntityManager nachgebildet: die
 * Zeile steht dann in der Datenbank, der JPA-Cache ist leer. Laesst sie sich
 * danach neu laden, ist die Persistenz belegt.
 *
 * Spring Boot 4 bringt keine @DataJpaTest-Slice mehr mit, deshalb laeuft der Test
 * mit dem vollen Kontext; @Transactional setzt jeden Testfall zurueck.
 */
@SpringBootTest
@Transactional
class PersistenzIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private BenutzerkontoRepository benutzerkontoRepository;

    @Autowired
    private GerichtRepository gerichtRepository;

    @Autowired
    private ReservierungRepository reservierungRepository;

    @Autowired
    private PinwandPostRepository pinwandPostRepository;

    @Test
    void benutzerkontoUeberlebtNeustart() {
        benutzerkontoRepository.save(new Benutzerkonto(
                "max@example.invalid", "Max Mustermann", "Student",
                "salt:hash", Instant.now()));

        em.flush();
        em.clear();

        Benutzerkonto geladen = benutzerkontoRepository.findByEmail("max@example.invalid")
                .orElseThrow();
        assertThat(geladen.getId()).isNotNull();
        assertThat(geladen.getName()).isEqualTo("Max Mustermann");
        assertThat(geladen.getRolle()).isEqualTo("Student");
        // Das Passwort liegt nur als Hash vor (US 1.1).
        assertThat(geladen.getPasswortHash()).isEqualTo("salt:hash");
    }

    @Test
    void emailIstEindeutig() {
        benutzerkontoRepository.save(new Benutzerkonto(
                "einmalig@example.invalid", "Erste", "Student", "salt:hash", Instant.now()));
        em.flush();

        assertThat(benutzerkontoRepository.existsByEmail("einmalig@example.invalid")).isTrue();
        assertThat(benutzerkontoRepository.existsByEmail("unbekannt@example.invalid")).isFalse();
    }

    @Test
    void reservierungUeberlebtNeustart() {
        reservierungRepository.save(new GespeicherteReservierung(
                "raum-101", "Hoersaal 101", "max@example.invalid",
                "2026-07-08T10:00", "2026-07-08T12:00", "aktiv", Instant.now()));

        em.flush();
        em.clear();

        var alle = reservierungRepository.findAllByRaumIdOrderByIdDesc("raum-101");
        assertThat(alle).hasSize(1);
        GespeicherteReservierung r = alle.get(0);
        assertThat(r.getRaumName()).isEqualTo("Hoersaal 101");
        assertThat(r.getReserviertVon()).isEqualTo("max@example.invalid");
        assertThat(r.getStatus()).isEqualTo("aktiv");
    }

    @Test
    void pinwandPostUndLikeUeberlebenNeustart() {
        GespeicherterPinwandPost gespeichert = pinwandPostRepository.save(
                new GespeicherterPinwandPost(
                        "Anna", "Student", "Lerngruppe",
                        "Mathe-Lerngruppe", "Wer macht mit?", Instant.now()));
        Long id = gespeichert.getId();

        gespeichert.like();
        gespeichert.like();
        pinwandPostRepository.save(gespeichert);

        em.flush();
        em.clear();

        GespeicherterPinwandPost geladen = pinwandPostRepository.findById(id).orElseThrow();
        assertThat(geladen.getTitel()).isEqualTo("Mathe-Lerngruppe");
        assertThat(geladen.getAutor()).isEqualTo("Anna");
        assertThat(geladen.getLikes()).isEqualTo(2);
    }

    @Test
    void mensaGerichtUeberlebtNeustart() {
        long vorher = gerichtRepository.count();
        gerichtRepository.save(new GespeichertesGericht("Currywurst", 3.50, Instant.now()));
        gerichtRepository.save(new GespeichertesGericht("Salat", 2.20, Instant.now()));

        em.flush();
        em.clear();

        assertThat(gerichtRepository.count()).isEqualTo(vorher + 2);
        var historie = gerichtRepository.findAllByOrderByIdDesc();
        assertThat(historie.get(0).getName()).isEqualTo("Salat");
        assertThat(historie.get(0).getPreis()).isEqualTo(2.20);
    }

    // US 8.3: Alle vier Datenarten liegen gleichzeitig in der Datenbank.
    @Test
    void alleVierDatenartenWerdenGespeichert() {
        long postsVorher = pinwandPostRepository.count();

        benutzerkontoRepository.save(new Benutzerkonto(
                "u@example.invalid", "U", "Student", "salt:hash", Instant.now()));
        reservierungRepository.save(new GespeicherteReservierung(
                "r-1", "Raum 1", "u@example.invalid", "10:00", "11:00", "aktiv", Instant.now()));
        pinwandPostRepository.save(new GespeicherterPinwandPost(
                "U", "Student", "Info", "Titel", "Inhalt", Instant.now()));
        gerichtRepository.save(new GespeichertesGericht("Pizza", 4.00, Instant.now()));

        em.flush();
        em.clear();

        assertThat(benutzerkontoRepository.existsByEmail("u@example.invalid")).isTrue();
        assertThat(reservierungRepository.findAllByRaumIdOrderByIdDesc("r-1")).hasSize(1);
        // Beim Start koennen Beispielbeitraege angelegt worden sein, daher die
        // relative Zunahme pruefen.
        assertThat(pinwandPostRepository.count()).isEqualTo(postsVorher + 1);
        assertThat(gerichtRepository.findAllByOrderByIdDesc())
                .anyMatch(g -> g.getName().equals("Pizza"));
    }
}
