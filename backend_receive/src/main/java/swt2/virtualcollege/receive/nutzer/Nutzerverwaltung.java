package swt2.virtualcollege.receive.nutzer;

import java.util.ArrayList;
import java.util.List;

/*
 * "Nutzerverwaltung" (Kapitel 13): verwaltet alle Nutzer.
 * Im Diagramm:  Nutzerverwaltung *-- Nutzer
 */
public class Nutzerverwaltung {

    private final List<Nutzer> nutzer = new ArrayList<>();

    public void registriere(Nutzer n) {
        nutzer.add(n);
    }

    public List<Nutzer> getNutzer() {
        return nutzer;
    }
}
