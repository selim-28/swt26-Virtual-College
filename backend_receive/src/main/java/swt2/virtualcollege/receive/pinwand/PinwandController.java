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

/*
 * REST-Controller fuer die virtuelle Pinwand.
 *
 * Das Frontend benutzt diese Endpunkte, um echte Posts aus der Datenbank
 * zu lesen, neue Posts zu speichern und Likes zu setzen. Damit wird das
 * Observer-/Pinwand-Konzept aus dem Diagramm fuer Nutzer sichtbar.
 */
@RestController
@RequestMapping("/api/pinwand")
public class PinwandController {

    // Die eigentliche Fachlogik liegt im Service, nicht im Controller.
    private final PinwandService service;

    public PinwandController(PinwandService service) {
        this.service = service;
    }

    // GET /api/pinwand/posts: alle Posts fuer die Pinwand.
    @GetMapping("/posts")
    public List<PostAntwort> posts() {
        return service.allePosts().stream()
                .map(this::alsAntwort)
                .toList();
    }

    /*
     * POST /api/pinwand/posts: neuen Post speichern.
     *
     * US 4.1 "Autor und Rolle werden aus dem angemeldeten Konto uebernommen":
     * Beides kommt aus dem GEPRUEFTEN JWT (Principal), nicht aus dem Request -
     * niemand kann unter fremdem Namen posten.
     */
    @PostMapping("/posts")
    public PostAntwort erstellen(@RequestBody NeuerPost anfrage, Authentication auth) {
        AngemeldeterNutzer nutzer = (AngemeldeterNutzer) auth.getPrincipal();
        NeuerPost zugeordnet = new NeuerPost(
                nutzer.name(), nutzer.rolle(),
                anfrage.kategorie(), anfrage.titel(), anfrage.inhalt());
        return alsAntwort(service.erstellePost(zugeordnet));
    }

    // DELETE /api/pinwand/posts/{id}: Post entfernen.
    // Die SecurityConfig laesst DELETE nur fuer die Rolle ADMIN durch.
    @DeleteMapping("/posts/{id}")
    public void loeschen(@PathVariable Long id) {
        service.loeschePost(id);
    }

    /*
     * GET /api/pinwand/benachrichtigungen: die Observer-Benachrichtigungen
     * (US 4.3). Jeder neue Beitrag erzeugt automatisch einen Eintrag -
     * erzeugt vom Subjekt (Pinwand) beim registrierten Beobachter
     * (BenachrichtigungsSpeicher).
     */
    @GetMapping("/benachrichtigungen")
    public List<BenachrichtigungsSpeicher.Eintrag> benachrichtigungen() {
        return service.alleBenachrichtigungen();
    }

    // POST /api/pinwand/posts/{id}/like: Like-Zaehler erhoehen.
    @PostMapping("/posts/{id}/like")
    public PostAntwort liken(@PathVariable Long id) {
        return alsAntwort(service.like(id));
    }

    // Kleine Umwandlung: Entity aus der Datenbank -> JSON-Antwort.
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

    // JSON-Koerper, den das Frontend beim Erstellen sendet.
    public record NeuerPost(String autor, String rolle, String kategorie,
                            String titel, String inhalt) {
    }

    // JSON-Antwort, die das Frontend anzeigen kann.
    public record PostAntwort(Long id, String autor, String rolle, String kategorie,
                              String titel, String inhalt, int likes, String erstelltAm) {
    }
}
