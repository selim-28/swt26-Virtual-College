package swt2.virtualcollege.send.simulation;

/*
 * Muster "Factory Method" – eine KONKRETE FABRIK (ConcreteCreator).
 * Erzeugt AuslastungsDaten (Beispiel-Werte fuer die Raum-Auslastung).
 *
 * Im Diagramm:  DatenGenerator <|-- AuslastungsGenerator
 */
public class AuslastungsGenerator extends DatenGenerator {

    @Override
    public Fremddatensatz erzeugeDaten() {
        // Beispiel-Auslastung (in einer echten App: aus Sensoren/Buchungen).
        return new AuslastungsDaten("B.1.23", 75);
    }
}
