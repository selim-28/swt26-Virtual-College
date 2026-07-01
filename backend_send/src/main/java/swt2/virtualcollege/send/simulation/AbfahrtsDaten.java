package swt2.virtualcollege.send.simulation;

import java.util.List;

/*
 * Muster "Factory Method" – ein KONKRETES PRODUKT (ConcreteProduct).
 * Beschreibt mehrere OEPNV-Abfahrten plus die Datenquelle.
 *
 * NEU: Statt einer einzelnen Beispiel-Abfahrt traegt dieses Produkt jetzt
 * eine LISTE echter Abfahrten (vom VRR, siehe VrrAbfahrten) und ein
 * "quelle"-Feld, damit das Frontend zeigen kann, ob die Daten live sind.
 *
 * Im Diagramm:  Fremddatensatz <|.. AbfahrtsDaten
 */
public class AbfahrtsDaten implements Fremddatensatz {

    // Eine einzelne Abfahrt: Linie, Ziel, Uhrzeit (HH:mm).
    public record Abfahrt(String linie, String ziel, String zeit) {
    }

    private final List<Abfahrt> abfahrten;
    private final String quelle;

    public AbfahrtsDaten(List<Abfahrt> abfahrten, String quelle) {
        this.abfahrten = abfahrten;
        this.quelle = quelle;
    }

    @Override
    public String alsJson() {
        // {"typ":"abfahrt","quelle":"...","abfahrten":[{"linie":"..","ziel":"..","zeit":".."}, ...]}
        StringBuilder sb = new StringBuilder();
        sb.append("{\"typ\":\"abfahrt\",\"quelle\":\"").append(esc(quelle)).append("\",\"abfahrten\":[");
        for (int i = 0; i < abfahrten.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            Abfahrt a = abfahrten.get(i);
            sb.append("{\"linie\":\"").append(esc(a.linie()))
              .append("\",\"ziel\":\"").append(esc(a.ziel()))
              .append("\",\"zeit\":\"").append(esc(a.zeit())).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    // Macht Anfuehrungszeichen/Backslashes im Text JSON-sicher.
    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
