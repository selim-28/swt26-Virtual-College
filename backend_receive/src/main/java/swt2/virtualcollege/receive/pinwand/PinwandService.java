package swt2.virtualcollege.receive.pinwand;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import swt2.virtualcollege.receive.persistenz.GespeicherterPinwandPost;
import swt2.virtualcollege.receive.persistenz.PinwandPostRepository;

/*
 * Fachlogik der Pinwand.
 *
 * Der Service kapselt alles, was mit Pinwand-Posts passiert - und hier ist
 * jetzt das OBSERVER-MUSTER LIVE angeschlossen (US 4.3): Die Domaenen-Klasse
 * Pinwand ist das Subjekt; der BenachrichtigungsSpeicher ist als Beobachter
 * registriert. Jeder neue Beitrag loest automatisch eine Benachrichtigung aus,
 * die das Frontend abrufen und anzeigen kann.
 */
@Service
public class PinwandService {

    // Repository = Datenbank-Zugang.
    private final PinwandPostRepository repository;

    // Das SUBJEKT aus dem Observer-Muster (Kapitel 13) - jetzt im Live-Pfad.
    private final Pinwand pinwand = new Pinwand();

    // Der registrierte Beobachter, dessen Benachrichtigungen das Frontend abruft.
    private final BenachrichtigungsSpeicher benachrichtigungen;

    public PinwandService(PinwandPostRepository repository,
                          BenachrichtigungsSpeicher benachrichtigungen) {
        this.repository = repository;
        this.benachrichtigungen = benachrichtigungen;
        // Beobachter beim Subjekt ANMELDEN (abonnieren). Nur registrierte
        // Beobachter werden informiert - genau das Akzeptanzkriterium von US 4.3.
        pinwand.abonniere(benachrichtigungen);
    }

    // Alle Benachrichtigungen ueber neue Posts (fuer den REST-Endpunkt).
    public List<BenachrichtigungsSpeicher.Eintrag> alleBenachrichtigungen() {
        return benachrichtigungen.alle();
    }

    /*
     * Demo-Daten beim ersten Start.
     *
     * Wenn die Datenbank leer ist, legen wir sinnvolle Startposts an.
     * Wenn schon Daten vorhanden sind, lassen wir sie unveraendert.
     */
    @PostConstruct
    public void initialisiereWennLeer() {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new GespeicherterPinwandPost(
                "Verwaltung",
                "Verwaltungsangestellter",
                "Wartung",
                "Gebaeude 42 teilweise geschlossen",
                "Am Freitag sind einzelne Raeume wegen Wartungsarbeiten nicht verfuegbar.",
                Instant.now()
        ));
        repository.save(new GespeicherterPinwandPost(
                "Prof. Mueller",
                "Betreuer",
                "Betreuung",
                "Mathematik II Nachholtermin",
                "Die Vorlesung wird naechste Woche nachgeholt. Details folgen in ILIAS.",
                Instant.now()
        ));
    }

    // Alle Posts, neueste zuerst.
    public List<GespeicherterPinwandPost> allePosts() {
        return repository.findAllByOrderByIdDesc();
    }

    // Neuen Post validieren, speichern UND die Beobachter benachrichtigen.
    public GespeicherterPinwandPost erstellePost(PinwandController.NeuerPost anfrage) {
        String autor = textOderStandard(anfrage.autor(), "Student");
        String rolle = textOderStandard(anfrage.rolle(), "Student");
        String kategorie = textOderStandard(anfrage.kategorie(), "Campus");
        String titel = textOderStandard(anfrage.titel(), "Neuer Beitrag");
        String inhalt = textOderStandard(anfrage.inhalt(), "Ohne Inhalt");

        GespeicherterPinwandPost gespeichert = repository.save(new GespeicherterPinwandPost(
                autor, rolle, kategorie, titel, inhalt, Instant.now()
        ));

        // OBSERVER-PUSH (US 4.3): Das Subjekt informiert automatisch alle
        // registrierten Beobachter ueber den neuen Beitrag. Titel, Autor und
        // Kategorie stecken in der Meldung ("Informationen zum Beitrag").
        pinwand.neuerPost(new Post(titel + " (von " + autor + ", " + kategorie + ")", inhalt));

        return gespeichert;
    }

    // Like-Zaehler erhoehen und wieder speichern.
    public GespeicherterPinwandPost like(Long id) {
        GespeicherterPinwandPost post = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post nicht gefunden: " + id));
        post.like();
        return repository.save(post);
    }

    /*
     * Post loeschen (nur Admin - das erzwingt die SecurityConfig, die
     * DELETE /api/** ausschliesslich der Rolle ADMIN erlaubt). So kann die
     * Verwaltung unpassende Beitraege von der Pinwand entfernen.
     */
    public void loeschePost(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Post nicht gefunden: " + id);
        }
        repository.deleteById(id);
    }

    // Kleine Hilfsmethode: leere Eingaben bekommen einen sinnvollen Standard.
    private String textOderStandard(String wert, String standard) {
        if (wert == null || wert.isBlank()) {
            return standard;
        }
        return wert.trim();
    }
}
