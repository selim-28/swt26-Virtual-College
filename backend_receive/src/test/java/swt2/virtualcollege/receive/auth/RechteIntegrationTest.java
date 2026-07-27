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

/**
 * Rollen und Rechte ueber den echten HTTP-Stack: Studierende reservieren
 * Lernraeume, Professor:innen zusaetzlich Hoersaele, Loeschen bleibt der
 * Admin-Rolle vorbehalten. Geprueft wird ausserdem, dass die Zuordnung aus dem
 * Token stammt und nicht aus dem Request.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RechteIntegrationTest {

    @LocalServerPort
    private int port;

    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

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

    // Der Request behauptet "Boesewicht", gespeichert wird der Name aus dem Token.
    @Test
    void professorDarfHoersaalReservierenZuordnungAusDemToken() throws Exception {
        String token = login("prof@fh-dortmund.de");

        HttpResponse<String> antwort = reserviere(token, "ef42-hoersaal1", "18:00", "19:00");

        assertThat(antwort.statusCode()).isEqualTo(200);
        JsonNode json = om.readTree(antwort.body());
        assertThat(json.get("raumName").asText()).isEqualTo("Hoersaal 1");
        assertThat(json.get("reserviertVon").asText()).isEqualTo("Prof. Demo");
    }

    @Test
    void studentDarfHoersaalNichtReservieren() throws Exception {
        String token = login("student@fh-dortmund.de");

        HttpResponse<String> antwort = reserviere(token, "ef42-hoersaal1", "19:00", "20:00");

        assertThat(antwort.statusCode()).isEqualTo(400);
        assertThat(antwort.body()).contains("Hoersaal");
    }

    @Test
    void adminDarfReservierungenLoeschen() throws Exception {
        String studentToken = login("student@fh-dortmund.de");
        HttpResponse<String> angelegt = reserviere(studentToken, "ef42-innovationlab", "06:00", "07:00");
        assertThat(angelegt.statusCode()).isEqualTo(200);
        long id = om.readTree(angelegt.body()).get("id").asLong();

        String adminToken = login("admin@fh-dortmund.de");
        assertThat(loesche(adminToken, "/api/campus/reservierungen/" + id).statusCode())
                .isEqualTo(200);
        // Ein zweiter Loeschversuch meldet 404.
        assertThat(loesche(adminToken, "/api/campus/reservierungen/" + id).statusCode())
                .isEqualTo(404);
    }

    // Spring Security blockt das Loeschen fuer Studierende mit 403.
    @Test
    void studentDarfNichtLoeschen() throws Exception {
        String studentToken = login("student@fh-dortmund.de");
        HttpResponse<String> angelegt = reserviere(studentToken, "ef42-hardwarelabor", "05:00", "06:00");
        assertThat(angelegt.statusCode()).isEqualTo(200);
        long id = om.readTree(angelegt.body()).get("id").asLong();

        HttpResponse<String> verweigert = loesche(studentToken, "/api/campus/reservierungen/" + id);
        assertThat(verweigert.statusCode()).isEqualTo(403);
    }

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
        // Autor und Rolle stammen aus dem Token, nicht aus dem Request.
        assertThat(json.get("autor").asText()).isEqualTo("Demo Student");
        assertThat(json.get("rolle").asText()).isEqualTo("Student");

        String adminToken = login("admin@fh-dortmund.de");
        assertThat(loesche(adminToken, "/api/pinwand/posts/" + json.get("id").asLong()).statusCode())
                .isEqualTo(200);
    }
}
