package swt2.virtualcollege.send.simulation;

/**
 * Factory Method: der abstrakte Creator. Jede konkrete Unterklasse erzeugt eine
 * eigene Art von Fremddatensatz, ohne dass der aufrufende SimulationService sie kennt.
 */
public abstract class DatenGenerator {

    /** Die Fabrikmethode. Die konkrete Fabrik entscheidet, welches Produkt entsteht. */
    public abstract Fremddatensatz erzeugeDaten();
}
