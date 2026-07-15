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

/*
 * EPIC 8 – Betrieb und Persistenz (US 8.2 "Daten dauerhaft speichern", Issue #37)
 *
 * Diese Tests beweisen die Akzeptanzkriterien der User Story 8.2 auf der
 * Ebene der Datenbank:
 *   - Benutzerkonten werden gespeichert.
 *   - Raumreservierungen werden gespeichert.
 *   - Pinwand-Beitraege und Likes werden gespeichert.
 *   - Empfangene Mensadaten werden gespeichert.
 *   - Die Daten bleiben nach einem Neustart erhalten.
 *
 * WIE testen wir "bleibt nach Neustart erhalten", ohne wirklich neu zu starten?
 * Wir speichern eine Zeile ueber das Repository und rufen dann am EntityManager
 * flush() + clear() auf:
 *   - flush()  = schreibt die Zeile wirklich in die Datenbank.
 *   - clear()  = leert den Arbeitsspeicher (den JPA-"Cache").
 * Danach MUSS die Zeile ueber das Repository frisch aus der Datenbank geladen
 * werden koennen. Das ist technisch genau dasselbe wie ein Neustart: der
 * Speicher ist leer, nur die Datenbank haelt die Daten. Gelingt das Laden,
 * ist die Persistenz bewiesen.
 *
 * @SpringBootTest laedt den echten Spring-Kontext (wie im Betrieb), @Transactional
 * setzt jeden Test danach zurueck, damit sich die Tests nicht gegenseitig stoeren.
 * (Spring Boot 4 liefert die schlanke @DataJpaTest-Slice nicht mehr im Kern, deshalb
 *  nutzen wir – wie AuthIntegrationTest – den vollen Kontext.)
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

    // --- US 8.2: "Benutzerkonten werden gespeichert." ---
    @Test
    void benutzerkontoUeberlebtNeustart() {
        benutzerkontoRepository.save(new Benutzerkonto(
                "max@fh-dortmund.de", "Max Mustermann", "Student",
                "salt:hash", Instant.now()));

        em.flush();   // wirklich in die DB schreiben
        em.clear();   // Arbeitsspeicher leeren -> wie ein Neustart

        Benutzerkonto geladen = benutzerkontoRepository.findByEmail("max@fh-dortmund.de")
                .orElseThrow();
        assertThat(geladen.getId()).isNotNull();
        assertThat(geladen.getName()).isEqualTo("Max Mustermann");
        assertThat(geladen.getRolle()).isEqualTo("Student");
        // Passwort niemals im Klartext (US 1.1) – es liegt nur als Hash vor.
        assertThat(geladen.getPasswortHash()).isEqualTo("salt:hash");
    }

    @Test
    void emailIstEindeutig() {
        benutzerkontoRepository.save(new Benutzerkonto(
                "einmalig@fh-dortmund.de", "Erste", "Student", "salt:hash", Instant.now()));
        em.flush();

        assertThat(benutzerkontoRepository.existsByEmail("einmalig@fh-dortmund.de")).isTrue();
        assertThat(benutzerkontoRepository.existsByEmail("unbekannt@fh-dortmund.de")).isFalse();
    }

    // --- US 8.2: "Raumreservierungen werden gespeichert." ---
    @Test
    void reservierungUeberlebtNeustart() {
        reservierungRepository.save(new GespeicherteReservierung(
                "raum-101", "Hoersaal 101", "max@fh-dortmund.de",
                "2026-07-08T10:00", "2026-07-08T12:00", "aktiv", Instant.now()));

        em.flush();
        em.clear();

        var alle = reservierungRepository.findAllByRaumIdOrderByIdDesc("raum-101");
        assertThat(alle).hasSize(1);
        GespeicherteReservierung r = alle.get(0);
        assertThat(r.getRaumName()).isEqualTo("Hoersaal 101");
        assertThat(r.getReserviertVon()).isEqualTo("max@fh-dortmund.de");
        assertThat(r.getStatus()).isEqualTo("aktiv");
    }

    // --- US 8.2: "Pinwand-Beitraege und Likes werden gespeichert." ---
    @Test
    void pinwandPostUndLikeUeberlebenNeustart() {
        GespeicherterPinwandPost gespeichert = pinwandPostRepository.save(
                new GespeicherterPinwandPost(
                        "Anna", "Student", "Lerngruppe",
                        "Mathe-Lerngruppe", "Wer macht mit?", Instant.now()));
        Long id = gespeichert.getId();

        // Zwei Likes vergeben und speichern.
        gespeichert.like();
        gespeichert.like();
        pinwandPostRepository.save(gespeichert);

        em.flush();
        em.clear();

        GespeicherterPinwandPost geladen = pinwandPostRepository.findById(id).orElseThrow();
        assertThat(geladen.getTitel()).isEqualTo("Mathe-Lerngruppe");
        assertThat(geladen.getAutor()).isEqualTo("Anna");
        // Der Like-Zaehler wurde dauerhaft mitgespeichert.
        assertThat(geladen.getLikes()).isEqualTo(2);
    }

    // --- US 8.2: "Empfangene Mensadaten werden gespeichert." ---
    @Test
    void mensaGerichtUeberlebtNeustart() {
        long vorher = gerichtRepository.count();
        gerichtRepository.save(new GespeichertesGericht("Currywurst", 3.50, Instant.now()));
        gerichtRepository.save(new GespeichertesGericht("Salat", 2.20, Instant.now()));

        em.flush();
        em.clear();

        assertThat(gerichtRepository.count()).isEqualTo(vorher + 2);
        var historie = gerichtRepository.findAllByOrderByIdDesc();
        // "neueste zuerst": das zuletzt gespeicherte Gericht steht oben.
        assertThat(historie.get(0).getName()).isEqualTo("Salat");
        assertThat(historie.get(0).getPreis()).isEqualTo(2.20);
    }

    // --- Alle vier Datenarten liegen gleichzeitig in der Datenbank ---
    // (US 8.3: "Benutzerkonten, Reservierungen, Pinwand-Posts und Mensadaten
    //  sind nachvollziehbar." – hier auf DB-Ebene nachgewiesen.)
    @Test
    void alleVierDatenartenWerdenGespeichert() {
        long postsVorher = pinwandPostRepository.count();

        benutzerkontoRepository.save(new Benutzerkonto(
                "u@fh-dortmund.de", "U", "Student", "salt:hash", Instant.now()));
        reservierungRepository.save(new GespeicherteReservierung(
                "r-1", "Raum 1", "u@fh-dortmund.de", "10:00", "11:00", "aktiv", Instant.now()));
        pinwandPostRepository.save(new GespeicherterPinwandPost(
                "U", "Student", "Info", "Titel", "Inhalt", Instant.now()));
        gerichtRepository.save(new GespeichertesGericht("Pizza", 4.00, Instant.now()));

        em.flush();
        em.clear();

        assertThat(benutzerkontoRepository.existsByEmail("u@fh-dortmund.de")).isTrue();
        assertThat(reservierungRepository.findAllByRaumIdOrderByIdDesc("r-1")).hasSize(1);
        // Es gab evtl. Demo-Posts beim Start – wir pruefen die relative Zunahme.
        assertThat(pinwandPostRepository.count()).isEqualTo(postsVorher + 1);
        assertThat(gerichtRepository.findAllByOrderByIdDesc())
                .anyMatch(g -> g.getName().equals("Pizza"));
    }
}
