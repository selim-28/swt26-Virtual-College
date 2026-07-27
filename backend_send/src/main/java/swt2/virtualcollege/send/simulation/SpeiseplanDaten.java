package swt2.virtualcollege.send.simulation;

import swt2.virtualcollege.send.mensa.Gericht;
import swt2.virtualcollege.send.mensa.Speiseplan;

/** Konkretes Produkt: ein Speiseplan, der sich selbst als JSON darstellen kann. */
public class SpeiseplanDaten implements Fremddatensatz {

    private final Speiseplan speiseplan;

    public SpeiseplanDaten(Speiseplan speiseplan) {
        this.speiseplan = speiseplan;
    }

    /**
     * Erzeugt das vereinbarte Nachrichtenformat:
     * {"typ":"speiseplan","gerichte":[{"name":"...","preis":3.5}, ...]}
     */
    @Override
    public String alsJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"typ\":\"speiseplan\",\"gerichte\":[");
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
