package swt2.virtualcollege.receive.campus;

import java.util.ArrayList;
import java.util.List;

import swt2.virtualcollege.receive.mensa.Speiseplan;
import swt2.virtualcollege.receive.mensa.SpeiseplanQuelle;

/*
 * "LiveCampusKarte" (Kapitel 13): die interaktive Karte mit Gebaeuden und
 * Live-Daten. Sie BENUTZT eine SpeiseplanQuelle, um das Mensa-Essen zu
 * zeigen – woher die Daten kommen (MQTT-Adapter, Test, ...), weiss sie nicht.
 * Genau das ist der Sinn des Adapter-Musters (geringe Kopplung).
 *
 * Im Diagramm:  LiveCampusKarte o-- Gebaeude
 *               LiveCampusKarte ..> SpeiseplanQuelle : holt Live-Daten
 */
public class LiveCampusKarte {

    private final List<Gebaeude> gebaeude = new ArrayList<>();

    // Die Quelle der Live-Mensa-Daten (Interface, nicht die konkrete Klasse!).
    private SpeiseplanQuelle speiseplanQuelle;

    public void addGebaeude(Gebaeude g) {
        gebaeude.add(g);
    }

    public List<Gebaeude> getGebaeude() {
        return gebaeude;
    }

    // Quelle setzen (z. B. den MqttDatenAdapter).
    public void setSpeiseplanQuelle(SpeiseplanQuelle quelle) {
        this.speiseplanQuelle = quelle;
    }

    // Live-Speiseplan anzeigen: einfach die Quelle fragen.
    public Speiseplan zeigeSpeiseplan() {
        return speiseplanQuelle.holeSpeiseplan();
    }
}
