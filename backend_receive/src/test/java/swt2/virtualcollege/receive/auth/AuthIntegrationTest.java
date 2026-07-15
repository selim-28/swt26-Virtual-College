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
 * Integrationstest fuer die komplette Authentifizierung ueber den ECHTEN
 * HTTP-Stack (echter Server auf zufaelligem Port + Spring Security +
 * JwtAuthFilter + Controller + H2-Datenbank), aufgerufen mit dem JDK-HttpClient.
 *
 * Deckt die Akzeptanzkriterien von EPIC 1 (US 1.1 - 1.3) ab:
 *  - Registrieren (Passwort gehasht, danach anmeldbar, Dubletten, kurzes Passwort)
 *  - Login (gueltig / ungueltig, Name+Rolle in der Antwort)
 *  - geschuetzte Funktionen erst nach Anmeldung
 *  - Logout macht das Token sofort ungueltig
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    // Spring Boot 4 stellt keinen ObjectMapper-Bean automatisch bereit,
    // fuer das Auslesen der JSON-Antworten genuegt aber ein eigener.
    private final ObjectMapper om = new ObjectMapper();

    private final HttpClient http = HttpClient.newHttpClient();

    private static final String DEMO_LOGIN =
            "{\"email\":\"student@fh-dortmund.de\",\"passwort\":\"passwort123\"}";

    // ---- kleine HTTP-Helfer ----

    private HttpResponse<String> post(String pfad, String jsonBody, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + pfad))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(jsonBody == null ? "" : jsonBody));
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return http.send(b.build(), BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String pfad, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + pfad)).GET();
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        return http.send(b.build(), BodyHandlers.ofString());
    }

    private String feld(String body, String name) throws Exception {
        JsonNode n = om.readTree(body).get(name);
        return n == null || n.isNull() ? null : n.asText();
    }

    private String tokenAusLogin(String jsonBody) throws Exception {
        HttpResponse<String> r = post("/api/auth/login", jsonBody, null);
        assertThat(r.statusCode()).isEqualTo(200);
        return feld(r.body(), "token");
    }

    // ---- Tests ----

    @Test
    void loginMitDemoKontoLiefertTokenUndRolle() throws Exception {
        HttpResponse<String> r = post("/api/auth/login", DEMO_LOGIN, null);

        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(feld(r.body(), "token")).isNotBlank();
        assertThat(feld(r.body(), "name")).isEqualTo("Demo Student");
        assertThat(feld(r.body(), "rolle")).isEqualTo("Student");
    }

    @Test
    void tokenSiehtWieEinJwtAus() throws Exception {
        String token = tokenAusLogin(DEMO_LOGIN);
        assertThat(token.split("\\.")).hasSize(3);   // Kopf.Inhalt.Signatur
    }

    @Test
    void falschesPasswortGibt401MitKlartext() throws Exception {
        HttpResponse<String> r = post("/api/auth/login",
                "{\"email\":\"student@fh-dortmund.de\",\"passwort\":\"falsch\"}", null);

        assertThat(r.statusCode()).isEqualTo(401);
        assertThat(feld(r.body(), "message")).isEqualTo("E-Mail oder Passwort ist falsch.");
    }

    @Test
    void geschuetzterEndpunktOhneTokenGibt401() throws Exception {
        HttpResponse<String> r = get("/api/auth/ich", null);

        assertThat(r.statusCode()).isEqualTo(401);
        assertThat(feld(r.body(), "message")).isNotBlank();
    }

    @Test
    void geschuetzterEndpunktMitTokenGibt200() throws Exception {
        String token = tokenAusLogin(DEMO_LOGIN);

        HttpResponse<String> r = get("/api/auth/ich", token);

        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(feld(r.body(), "email")).isEqualTo("student@fh-dortmund.de");
        assertThat(feld(r.body(), "name")).isEqualTo("Demo Student");
    }

    @Test
    void geschuetzteFachfunktionBrauchtLogin() throws Exception {
        assertThat(get("/api/campus/karte", null).statusCode()).isEqualTo(401);

        String token = tokenAusLogin(DEMO_LOGIN);
        assertThat(get("/api/campus/karte", token).statusCode()).isEqualTo(200);
    }

    @Test
    void registrierenLegtKontoAnUndLoggtDirektEin() throws Exception {
        String body = "{\"email\":\"neu.person@fh-dortmund.de\",\"name\":\"Neue Person\","
                + "\"rolle\":\"Betreuer\",\"passwort\":\"geheim123\"}";

        HttpResponse<String> r = post("/api/auth/registrieren", body, null);
        assertThat(r.statusCode()).isEqualTo(200);
        assertThat(feld(r.body(), "rolle")).isEqualTo("Betreuer");

        // Mit dem Token ist man sofort angemeldet.
        String token = feld(r.body(), "token");
        HttpResponse<String> ich = get("/api/auth/ich", token);
        assertThat(ich.statusCode()).isEqualTo(200);
        assertThat(feld(ich.body(), "email")).isEqualTo("neu.person@fh-dortmund.de");
    }

    @Test
    void bereitsVerwendeteEmailWirdAbgelehnt() throws Exception {
        String body = "{\"email\":\"doppelt@fh-dortmund.de\",\"name\":\"A\","
                + "\"rolle\":\"Student\",\"passwort\":\"geheim123\"}";
        assertThat(post("/api/auth/registrieren", body, null).statusCode()).isEqualTo(200);

        HttpResponse<String> zweite = post("/api/auth/registrieren", body, null);
        assertThat(zweite.statusCode()).isEqualTo(400);
        assertThat(feld(zweite.body(), "message")).isEqualTo("Diese E-Mail ist bereits registriert.");
    }

    @Test
    void zuKurzesPasswortWirdAbgelehnt() throws Exception {
        String body = "{\"email\":\"kurz@fh-dortmund.de\",\"name\":\"K\","
                + "\"rolle\":\"Student\",\"passwort\":\"123\"}";
        assertThat(post("/api/auth/registrieren", body, null).statusCode()).isEqualTo(400);
    }

    @Test
    void nachLogoutIstDasTokenSofortUngueltig() throws Exception {
        String token = tokenAusLogin(DEMO_LOGIN);

        // Vor dem Logout gilt das Token.
        assertThat(get("/api/auth/ich", token).statusCode()).isEqualTo(200);

        // Logout.
        assertThat(post("/api/auth/logout", "", token).statusCode()).isEqualTo(200);

        // Danach ist genau dieses Token gesperrt -> 401.
        assertThat(get("/api/auth/ich", token).statusCode()).isEqualTo(401);
    }
}
