package swt2.virtualcollege.send.simulation;

/*
 * Muster "Factory Method" – ein KONKRETES PRODUKT (ConcreteProduct).
 * Beschreibt die Auslastung eines Raumes (wie voll ist er gerade?).
 *
 * Im Diagramm:  Fremddatensatz <|.. AuslastungsDaten
 */
public class AuslastungsDaten implements Fremddatensatz {

    private final String raum;       // z. B. "B.1.23"
    private final int prozent;       // wie voll, 0..100

    public AuslastungsDaten(String raum, int prozent) {
        this.raum = raum;
        this.prozent = prozent;
    }

    @Override
    public String alsJson() {
        // Beispiel: {"typ":"auslastung","raum":"B.1.23","prozent":75}
        return "{\"typ\":\"auslastung\",\"raum\":\"" + raum
                + "\",\"prozent\":" + prozent + "}";
    }
}
