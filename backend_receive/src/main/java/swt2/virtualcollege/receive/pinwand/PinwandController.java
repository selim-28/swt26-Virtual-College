package swt2.virtualcollege.receive.pinwand;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import swt2.virtualcollege.receive.auth.AngemeldeterNutzer;
import swt2.virtualcollege.receive.persistenz.GespeicherterPinwandPost;

/**
 * REST-Schnittstelle der Pinwand: Beitraege lesen und anlegen, Likes setzen,
 * Beitraege loeschen und die Observer-Benachrichtigungen abrufen.
 */
@RestController
@RequestMapping("/api/pinwand")
public class PinwandController {

    private final PinwandService service;

    public PinwandController(PinwandService service) {
        this.service = service;
    }

    @GetMapping("/posts")
    public List<PostAntwort> posts() {
        return service.allePosts().stream()
                .map(this::alsAntwort)
                .toList();
    }

    /**
     * Legt einen Beitrag an. Autor und Rolle stammen aus dem geprueften Token und
     * nicht aus dem Request, damit niemand unter fremdem Namen posten kann (US 4.1).
     */
    @PostMapping("/posts")
    public PostAntwort erstellen(@RequestBody NeuerPost anfrage, Authentication auth) {
        AngemeldeterNutzer nutzer = (AngemeldeterNutzer) auth.getPrincipal();
        NeuerPost zugeordnet = new NeuerPost(
                nutzer.name(), nutzer.rolle(),
                anfrage.kategorie(), anfrage.titel(), anfrage.inhalt());
        return alsAntwort(service.erstellePost(zugeordnet));
    }

    /** Loeschen ist ueber die SecurityConfig auf die Admin-Rolle beschraenkt. */
    @DeleteMapping("/posts/{id}")
    public void loeschen(@PathVariable Long id) {
        service.loeschePost(id);
    }

    /** Die vom Beobachter gesammelten Benachrichtigungen (US 4.3). */
    @GetMapping("/benachrichtigungen")
    public List<BenachrichtigungsSpeicher.Eintrag> benachrichtigungen() {
        return service.alleBenachrichtigungen();
    }

    @PostMapping("/posts/{id}/like")
    public PostAntwort liken(@PathVariable Long id) {
        return alsAntwort(service.like(id));
    }

    /** Wandelt die Entity in die JSON-Antwort um. */
    private PostAntwort alsAntwort(GespeicherterPinwandPost post) {
        return new PostAntwort(
                post.getId(),
                post.getAutor(),
                post.getRolle(),
                post.getKategorie(),
                post.getTitel(),
                post.getInhalt(),
                post.getLikes(),
                post.getErstelltAm().toString()
        );
    }

    /** JSON-Koerper, den das Frontend beim Erstellen sendet. */
    public record NeuerPost(String autor, String rolle, String kategorie,
                            String titel, String inhalt) {
    }

    /** JSON-Antwort fuer die Anzeige im Frontend. */
    public record PostAntwort(Long id, String autor, String rolle, String kategorie,
                              String titel, String inhalt, int likes, String erstelltAm) {
    }
}
