package swt2.virtualcollege.send.simulation;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/*
 * Gateway zum oeffentlichen VRR-EFA-Abfahrtsmonitor. Diese technische
 * Integrationsklasse gehoert zum simulierten Fremdsystem (backend_send):
 * Sie holt reale Daten, die danach wie alle anderen Fremddatensaetze durch
 * Factory Method -> MQTT -> Empfaenger laufen.
 */
public class VrrAbfahrten {

    private static final Logger log = LoggerFactory.getLogger(VrrAbfahrten.class);
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final String HALTESTELLE = "Dortmund Universität";
    private static final String ENDPOINT = "https://efa.vrr.de/vrr/XSLT_DM_REQUEST";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    public AbfahrtsDaten holeAbfahrten() {
        try {
            String query = "?language=de&outputFormat=JSON&type_dm=stop&mode=direct"
                    + "&useRealtime=1&limit=8&name_dm="
                    + URLEncoder.encode(HALTESTELLE, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT + query))
                    .timeout(Duration.ofSeconds(7))
                    .header("User-Agent", "VirtualCollege-SWT2/1.0 (student project)")
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("VRR antwortet mit HTTP " + response.statusCode());
            }

            JsonNode wurzel = MAPPER.readTree(response.body());
            JsonNode liste = wurzel.get("departureList");
            List<AbfahrtsDaten.Abfahrt> abfahrten = new ArrayList<>();
            if (liste != null) {
                for (int i = 0; i < liste.size() && abfahrten.size() < 8; i++) {
                    JsonNode eintrag = liste.get(i);
                    JsonNode linie = eintrag.get("servingLine");
                    if (linie == null || linie.get("number") == null) continue;

                    JsonNode zeit = eintrag.get("realDateTime") != null
                            ? eintrag.get("realDateTime") : eintrag.get("dateTime");
                    String uhrzeit = zweistellig(zeit, "hour") + ":" + zweistellig(zeit, "minute");
                    String nummer = linie.get("number").asString();
                    String produkt = linie.get("name") == null ? "" : linie.get("name").asString();
                    String linienname = produkt.isBlank() || nummer.startsWith(produkt)
                            ? nummer : produkt + " " + nummer;
                    String ziel = linie.get("direction") == null
                            ? "Ziel unbekannt" : linie.get("direction").asString();
                    abfahrten.add(new AbfahrtsDaten.Abfahrt(linienname, ziel, uhrzeit));
                }
            }
            if (abfahrten.isEmpty()) throw new IllegalStateException("VRR lieferte keine Abfahrten");
            return new AbfahrtsDaten(abfahrten, "VRR EFA live · Dortmund Universität S");
        } catch (Exception e) {
            log.warn("VRR-Live-Daten nicht erreichbar, benutze gekennzeichneten Fallback: {}", e.getMessage());
            LocalTime jetzt = LocalTime.now();
            DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm");
            return new AbfahrtsDaten(List.of(
                    new AbfahrtsDaten.Abfahrt("S-Bahn S1", "Dortmund Hbf", jetzt.plusMinutes(8).format(hhmm)),
                    new AbfahrtsDaten.Abfahrt("Bus 447", "Dortmund Hacheney", jetzt.plusMinutes(13).format(hhmm)),
                    new AbfahrtsDaten.Abfahrt("Bus 462", "Dortmund Huckarde Bushof", jetzt.plusMinutes(18).format(hhmm))
            ), "Offline-Fallback · zuletzt keine VRR-Verbindung");
        }
    }

    private String zweistellig(JsonNode node, String feld) {
        if (node == null || node.get(feld) == null) return "00";
        return String.format("%02d", node.get(feld).asInt());
    }
}
