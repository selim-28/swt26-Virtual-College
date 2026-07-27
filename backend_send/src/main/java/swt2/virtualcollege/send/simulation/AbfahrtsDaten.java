package swt2.virtualcollege.send.simulation;

import java.util.List;

/**
 * Konkretes Produkt: eine Liste von OePNV-Abfahrten samt Angabe der Datenquelle,
 * damit das Frontend Live- und Ersatzdaten unterscheiden kann.
 */
public class AbfahrtsDaten implements Fremddatensatz {

    /** Eine einzelne Abfahrt: Linie, Ziel, Uhrzeit (HH:mm). */
    public record Abfahrt(String linie, String ziel, String zeit) {
    }

    private final List<Abfahrt> abfahrten;
    private final String quelle;

    public AbfahrtsDaten(List<Abfahrt> abfahrten, String quelle) {
        this.abfahrten = abfahrten;
        this.quelle = quelle;
    }

    /**
     * Erzeugt das vereinbarte Nachrichtenformat:
     * {"typ":"abfahrt","quelle":"...","abfahrten":[{"linie":"..","ziel":"..","zeit":".."}, ...]}
     */
    @Override
    public String alsJson() {
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

    /** Maskiert Anfuehrungszeichen und Backslashes fuer die JSON-Ausgabe. */
    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
