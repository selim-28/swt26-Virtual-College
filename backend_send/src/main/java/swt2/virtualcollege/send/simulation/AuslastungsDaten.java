package swt2.virtualcollege.send.simulation;

/** Konkretes Produkt: die Auslastung eines Raumes in Prozent. */
public class AuslastungsDaten implements Fremddatensatz {

    private final String raum;
    private final int prozent;

    public AuslastungsDaten(String raum, int prozent) {
        this.raum = raum;
        this.prozent = prozent;
    }

    /** Erzeugt das Nachrichtenformat {"typ":"auslastung","raum":"B.1.23","prozent":75}. */
    @Override
    public String alsJson() {
        return "{\"typ\":\"auslastung\",\"raum\":\"" + raum
                + "\",\"prozent\":" + prozent + "}";
    }
}
