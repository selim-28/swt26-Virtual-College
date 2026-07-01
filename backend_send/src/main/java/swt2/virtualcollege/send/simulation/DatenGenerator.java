package swt2.virtualcollege.send.simulation;

/*
 * Muster "Factory Method" – die abstrakte FABRIK (Creator).
 *
 * "abstract" = man kann KEINEN DatenGenerator direkt erzeugen, nur eine
 * konkrete Unterklasse (SpeiseplanGenerator, AuslastungsGenerator,
 * AbfahrtsGenerator). Jede Unterklasse fuellt die "Fabrikmethode"
 * erzeugeDaten() mit ihrem eigenen Inhalt.
 *
 * Vorteil (OCP): Eine neue Datenart = eine neue Unterklasse. Der Code, der
 * den Generator benutzt (SimulationService), muss dafuer NICHT geaendert
 * werden – er kennt nur die gemeinsame Methode erzeugeDaten().
 *
 * Im Diagramm:  DatenGenerator <|-- SpeiseplanGenerator (usw.)
 *               DatenGenerator ..> Fremddatensatz
 */
public abstract class DatenGenerator {

    // Die FABRIKMETHODE: erzeugt ein Produkt (Fremddatensatz).
    // Jede konkrete Fabrik entscheidet selbst, WELCHES Produkt.
    public abstract Fremddatensatz erzeugeDaten();
}
