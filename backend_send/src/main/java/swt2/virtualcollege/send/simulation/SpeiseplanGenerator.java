package swt2.virtualcollege.send.simulation;

import swt2.virtualcollege.send.mensa.Mensa;

/*
 * Muster "Factory Method" – eine KONKRETE FABRIK (ConcreteCreator).
 * Erzeugt SpeiseplanDaten: liest den echten Speiseplan aus der Mensa
 * und verpackt ihn in ein Produkt.
 *
 * Im Diagramm:  DatenGenerator <|-- SpeiseplanGenerator
 *               SpeiseplanGenerator ..> Mensa : liest Speiseplan
 *               SpeiseplanGenerator ..> SpeiseplanDaten
 */
public class SpeiseplanGenerator extends DatenGenerator {

    // Die Fachklasse, aus der wir den echten Speiseplan holen.
    private final Mensa mensa = new Mensa();

    @Override
    public Fremddatensatz erzeugeDaten() {
        // Speiseplan aus der Mensa holen und als Datenpaket verpacken.
        return new SpeiseplanDaten(mensa.getSpeiseplan());
    }
}
