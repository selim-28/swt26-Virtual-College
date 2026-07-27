package swt2.virtualcollege.send.simulation;

/** Konkrete Fabrik: erzeugt Beispielwerte fuer die Raumauslastung. */
public class AuslastungsGenerator extends DatenGenerator {

    @Override
    public Fremddatensatz erzeugeDaten() {
        // Produktiv kaemen die Werte aus Sensoren oder Buchungen.
        return new AuslastungsDaten("B.1.23", 75);
    }
}
