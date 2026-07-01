package swt2.virtualcollege.send.simulation;

/*
 * Muster "Factory Method" – eine KONKRETE FABRIK (ConcreteCreator).
 * Erzeugt AbfahrtsDaten (Beispiel-Abfahrt des OEPNV).
 *
 * Im Diagramm:  DatenGenerator <|-- AbfahrtsGenerator
 */
public class AbfahrtsGenerator extends DatenGenerator {

    private final VrrAbfahrten vrr = new VrrAbfahrten();

    @Override
    public Fremddatensatz erzeugeDaten() {
        // Echte Echtzeitdaten des VRR; bei fehlendem Internet liefert der
        // Dienst bewusst gekennzeichnete Demodaten, damit die Praesentation
        // weiterhin funktioniert.
        return vrr.holeAbfahrten();
    }
}
