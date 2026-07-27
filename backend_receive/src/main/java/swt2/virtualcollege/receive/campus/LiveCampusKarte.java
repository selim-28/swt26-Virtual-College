package swt2.virtualcollege.receive.campus;

import java.util.ArrayList;
import java.util.List;

import swt2.virtualcollege.receive.mensa.Speiseplan;
import swt2.virtualcollege.receive.mensa.SpeiseplanQuelle;

/**
 * Die interaktive Campus-Karte mit Gebaeuden und Live-Daten. Sie kennt nur das
 * Interface SpeiseplanQuelle, nicht die konkrete Herkunft der Mensa-Daten.
 */
public class LiveCampusKarte {

    private final List<Gebaeude> gebaeude = new ArrayList<>();

    private SpeiseplanQuelle speiseplanQuelle;

    public void addGebaeude(Gebaeude g) {
        gebaeude.add(g);
    }

    public List<Gebaeude> getGebaeude() {
        return gebaeude;
    }

    public void setSpeiseplanQuelle(SpeiseplanQuelle quelle) {
        this.speiseplanQuelle = quelle;
    }

    public Speiseplan zeigeSpeiseplan() {
        return speiseplanQuelle.holeSpeiseplan();
    }
}
