package swt2.virtualcollege.receive.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * ROLLEN & RECHTE ueber den ECHTEN HTTP-Stack (Spring Security + JWT).
 *
 * Beweist die drei Demo-Rollen:
 *   - Student:   darf Lernraeume reservieren, aber KEINE Hoersaele, NICHT loeschen.
 *   - Professor: darf zusaetzlich Hoersaele reservieren.
 *   - Admin:     darf verwalten (Reservierungen und Pinwand-Posts LOESCHEN).
 * Ausserdem: Die Zuordnung (reserviertVon/autor) kommt aus dem GEPRUEFTEN JWT,
 * nicht aus dem Request - man kann nicht unter fremdem Namen buchen/posten.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RechteIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    // --- kleine Helfer -----------------------------------------------------

    private String url(String pfad) {
        return "http://localhost:" + port + pfad;
    }

    private String login(String email) throws Exception {
        HttpResponse<String> antwort = http.send(HttpRequest.newBuilder(URI.create(url("/api/auth/login")))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(
                        "{\"email\":\"" + email + "\",\"passwort\":\"passwort123\"}"))
                .build(), BodyHandlers.ofString());
        assertThat(antwort.statusCode()).isEqualTo(200);
        return om.readTree(antwort.body()).get("token").asText();
    }

    private HttpResponse<String> reserviere(String token, String raumId,
                                            String von, String bis) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url("/api/campus/reservierungen")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(BodyPublishers.ofString("{\"raumId\":\"" + raumId
                        + "\",\"reserviertVon\":\"Boesewicht\",\"von\":\"" + von
                        + "\",\"bis\":\"" + bis + "\"}"))
                .build(), BodyHandlers.ofString());
    }

    private HttpResponse<String> loesche(String token, String pfad) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url(pfad)))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build(), BodyHandlers.ofString());
    }

    // --- die eigentlichen Rechte-Tests --------------------------------------

    // Professor darf den Hoersaal reservieren - und die Zuordnung kommt aus dem
    // Token: Obwohl der Request "Boesewicht" behauptet, steht "Prof. Demo" drin.
    @Test
    void professorDarfHoersaalReservierenZuordnungAusDemToken() throws Exception {
        String token = login("prof@fh-dortmund.de");

        HttpResponse<String> antwort = reserviere(token, "ef42-hoersaal1", "18:00", "19:00");

        assertThat(antwort.statusCode()).isEqualTo(200);
        JsonNode json = om.readTree(antwort.body());
        assertThat(json.get("raumName").asText()).isEqualTo("Hoersaal 1");
        assertThat(json.get("reserviertVon").asText()).isEqualTo("Prof. Demo"); // NICHT "Boesewicht"!
    }

    // Student darf den Hoersaal NICHT reservieren (Rollenregel, 400 + Klartext).
    @Test
    void studentDarfHoersaalNichtReservieren() throws Exception {
        String token = login("student@fh-dortmund.de");

        HttpResponse<String> antwort = reserviere(token, "ef42-hoersaal1", "19:00", "20:00");

        assertThat(antwort.statusCode()).isEqualTo(400);
        assertThat(antwort.body()).contains("Hoersaal");
    }

    // Admin darf Reservierungen loeschen; eine zweite Loeschung meldet 404.
    @Test
    void adminDarfReservierungenLoeschen() throws Exception {
        // Student legt eine Reservierung an ...
        String studentToken = login("student@fh-dortmund.de");
        HttpResponse<String> angelegt = reserviere(studentToken, "ef42-innovationlab", "06:00", "07:00");
        assertThat(angelegt.statusCode()).isEqualTo(200);
        long id = om.readTree(angelegt.body()).get("id").asLong();

        // ... und der Admin raeumt sie weg.
        String adminToken = login("admin@fh-dortmund.de");
        assertThat(loesche(adminToken, "/api/campus/reservierungen/" + id).statusCode())
                .isEqualTo(200);
        // Noch einmal loeschen -> gibt es nicht mehr.
        assertThat(loesche(adminToken, "/api/campus/reservierungen/" + id).statusCode())
                .isEqualTo(404);
    }

    // Student darf NICHT loeschen: Spring Security blockt mit 403 (Zugriff verweigert).
    @Test
    void studentDarfNichtLoeschen() throws Exception {
        String studentToken = login("student@fh-dortmund.de");
        HttpResponse<String> angelegt = reserviere(studentToken, "ef42-hardwarelabor", "05:00", "06:00");
        assertThat(angelegt.statusCode()).isEqualTo(200);
        long id = om.readTree(angelegt.body()).get("id").asLong();

        HttpResponse<String> verweigert = loesche(studentToken, "/api/campus/reservierungen/" + id);
        assertThat(verweigert.statusCode()).isEqualTo(403);
    }

    // Admin darf Pinwand-Posts loeschen; Autor/Rolle kommen beim Erstellen aus dem Token.
    @Test
    void adminDarfPinwandPostsLoeschen() throws Exception {
        String studentToken = login("student@fh-dortmund.de");
        HttpResponse<String> post = http.send(HttpRequest.newBuilder(URI.create(url("/api/pinwand/posts")))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + studentToken)
                .POST(BodyPublishers.ofString(
                        "{\"autor\":\"Boesewicht\",\"rolle\":\"Admin\","
                        + "\"kategorie\":\"Campus\",\"titel\":\"Testpost\",\"inhalt\":\"weg damit\"}"))
                .build(), BodyHandlers.ofString());
        assertThat(post.statusCode()).isEqualTo(200);
        JsonNode json = om.readTree(post.body());
        // Autor + Rolle stammen aus dem Token, nicht aus dem Request:
        assertThat(json.get("autor").asText()).isEqualTo("Demo Student");
        assertThat(json.get("rolle").asText()).isEqualTo("Student");

        String adminToken = login("admin@fh-dortmund.de");
        assertThat(loesche(adminToken, "/api/pinwand/posts/" + json.get("id").asLong()).statusCode())
                .isEqualTo(200);
    }
}
