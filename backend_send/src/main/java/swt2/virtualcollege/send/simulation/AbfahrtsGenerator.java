package swt2.virtualcollege.send.simulation;

/** Konkrete Fabrik: liefert die OePNV-Abfahrten des VRR als Fremddatensatz. */
public class AbfahrtsGenerator extends DatenGenerator {

    private final VrrAbfahrten vrr = new VrrAbfahrten();

    @Override
    public Fremddatensatz erzeugeDaten() {
        // Ohne Internet liefert der Dienst gekennzeichnete Demodaten.
        return vrr.holeAbfahrten();
    }
}
