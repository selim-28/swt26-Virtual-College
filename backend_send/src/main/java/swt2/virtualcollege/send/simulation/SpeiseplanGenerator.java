package swt2.virtualcollege.send.simulation;

import swt2.virtualcollege.send.mensa.Mensa;

/**
 * Konkrete Fabrik: liest den Speiseplan aus der Mensa und verpackt ihn
 * als Fremddatensatz.
 */
public class SpeiseplanGenerator extends DatenGenerator {

    private final Mensa mensa = new Mensa();

    @Override
    public Fremddatensatz erzeugeDaten() {
        return new SpeiseplanDaten(mensa.getSpeiseplan());
    }
}
