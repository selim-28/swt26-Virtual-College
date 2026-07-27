package swt2.virtualcollege.receive.pinwand;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import swt2.virtualcollege.receive.persistenz.GespeicherterPinwandPost;
import swt2.virtualcollege.receive.persistenz.PinwandPostRepository;

/**
 * Fachlogik der Pinwand. Hier haengt das Observer-Muster im Live-Pfad (US 4.3):
 * die Domaenenklasse Pinwand ist das Subjekt, der BenachrichtigungsSpeicher der
 * registrierte Beobachter.
 */
@Service
public class PinwandService {

    private final PinwandPostRepository repository;

    private final Pinwand pinwand = new Pinwand();

    private final BenachrichtigungsSpeicher benachrichtigungen;

    public PinwandService(PinwandPostRepository repository,
                          BenachrichtigungsSpeicher benachrichtigungen) {
        this.repository = repository;
        this.benachrichtigungen = benachrichtigungen;
        pinwand.abonniere(benachrichtigungen);
    }

    public List<BenachrichtigungsSpeicher.Eintrag> alleBenachrichtigungen() {
        return benachrichtigungen.alle();
    }

    /** Legt Beispielbeitraege an, solange die Datenbank leer ist. */
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

    /** Alle Beitraege, neueste zuerst. */
    public List<GespeicherterPinwandPost> allePosts() {
        return repository.findAllByOrderByIdDesc();
    }

    /** Prueft die Eingaben, speichert den Beitrag und benachrichtigt die Beobachter. */
    public GespeicherterPinwandPost erstellePost(PinwandController.NeuerPost anfrage) {
        String autor = textOderStandard(anfrage.autor(), "Student");
        String rolle = textOderStandard(anfrage.rolle(), "Student");
        String kategorie = textOderStandard(anfrage.kategorie(), "Campus");
        String titel = textOderStandard(anfrage.titel(), "Neuer Beitrag");
        String inhalt = textOderStandard(anfrage.inhalt(), "Ohne Inhalt");

        GespeicherterPinwandPost gespeichert = repository.save(new GespeicherterPinwandPost(
                autor, rolle, kategorie, titel, inhalt, Instant.now()
        ));

        // Das Subjekt informiert alle registrierten Beobachter.
        pinwand.neuerPost(new Post(titel + " (von " + autor + ", " + kategorie + ")", inhalt));

        return gespeichert;
    }

    /** Erhoeht den Like-Zaehler eines Beitrags. */
    public GespeicherterPinwandPost like(Long id) {
        GespeicherterPinwandPost post = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post nicht gefunden: " + id));
        post.like();
        return repository.save(post);
    }

    /**
     * Entfernt einen Beitrag. Die Beschraenkung auf die Admin-Rolle erzwingt die
     * SecurityConfig ueber die DELETE-Regel.
     */
    public void loeschePost(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Post nicht gefunden: " + id);
        }
        repository.deleteById(id);
    }

    /** Ersetzt leere Eingaben durch einen Standardwert. */
    private String textOderStandard(String wert, String standard) {
        if (wert == null || wert.isBlank()) {
            return standard;
        }
        return wert.trim();
    }
}
