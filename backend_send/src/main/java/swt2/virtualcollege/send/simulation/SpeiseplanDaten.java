package swt2.virtualcollege.send.simulation;

import swt2.virtualcollege.send.mensa.Gericht;
import swt2.virtualcollege.send.mensa.Speiseplan;

/*
 * Muster "Factory Method" – ein KONKRETES PRODUKT (ConcreteProduct).
 * Verpackt einen Speiseplan und kann sich selbst als JSON beschreiben.
 *
 * Im Diagramm:  Fremddatensatz <|.. SpeiseplanDaten   und
 *               SpeiseplanDaten --> Speiseplan : verpackt
 */
public class SpeiseplanDaten implements Fremddatensatz {

    // Der eingepackte Speiseplan (kommt aus der Mensa-Fachklasse).
    private final Speiseplan speiseplan;

    public SpeiseplanDaten(Speiseplan speiseplan) {
        this.speiseplan = speiseplan;
    }

    /*
     * Baut den JSON-Text von Hand zusammen, damit man das Format
     * gut sehen kann. Ergebnis-Beispiel:
     * {"typ":"speiseplan","gerichte":[{"name":"...","preis":3.5}, ...]}
     */
    @Override
    public String alsJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"typ\":\"speiseplan\",\"gerichte\":[");
        // Schleife ueber alle Gerichte und je ein kleines JSON-Objekt anhaengen.
        for (int i = 0; i < speiseplan.gerichte().size(); i++) {
            Gericht g = speiseplan.gerichte().get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"name\":\"").append(g.name())
              .append("\",\"preis\":").append(g.preis()).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }
}
