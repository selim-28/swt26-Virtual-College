package swt2.virtualcollege.receive.pinwand;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import swt2.virtualcollege.receive.persistenz.GespeicherterPinwandPost;
import swt2.virtualcollege.receive.persistenz.PinwandPostRepository;

/*
 * Fachlogik der Pinwand.
 *
 * Der Service kapselt alles, was mit Pinwand-Posts passiert. Der
 * Controller bleibt dadurch klein, und spaeter koennte man hier das
 * Observer-Muster noch enger anschliessen (z. B. echte Benachrichtigungen).
 */
@Service
public class PinwandService {

    // Repository = Datenbank-Zugang.
    private final PinwandPostRepository repository;

    public PinwandService(PinwandPostRepository repository) {
        this.repository = repository;
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

    // Neuen Post validieren und speichern.
    public GespeicherterPinwandPost erstellePost(PinwandController.NeuerPost anfrage) {
        String autor = textOderStandard(anfrage.autor(), "Student");
        String rolle = textOderStandard(anfrage.rolle(), "Student");
        String kategorie = textOderStandard(anfrage.kategorie(), "Campus");
        String titel = textOderStandard(anfrage.titel(), "Neuer Beitrag");
        String inhalt = textOderStandard(anfrage.inhalt(), "Ohne Inhalt");

        return repository.save(new GespeicherterPinwandPost(
                autor, rolle, kategorie, titel, inhalt, Instant.now()
        ));
    }

    // Like-Zaehler erhoehen und wieder speichern.
    public GespeicherterPinwandPost like(Long id) {
        GespeicherterPinwandPost post = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post nicht gefunden: " + id));
        post.like();
        return repository.save(post);
    }

    // Kleine Hilfsmethode: leere Eingaben bekommen einen sinnvollen Standard.
    private String textOderStandard(String wert, String standard) {
        if (wert == null || wert.isBlank()) {
            return standard;
        }
        return wert.trim();
    }
}
