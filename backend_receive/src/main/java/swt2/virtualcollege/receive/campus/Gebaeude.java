package swt2.virtualcollege.receive.campus;

import java.util.ArrayList;
import java.util.List;

/*
 * "Gebaeude" (Kapitel 13): ein Gebaeude besteht aus Raeumen.
 * Im Diagramm:  Gebaeude *-- Raum
 */
public class Gebaeude {

    private final String name;
    private final List<Raum> raeume = new ArrayList<>();

    public Gebaeude(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addRaum(Raum r) {
        raeume.add(r);
    }

    public List<Raum> getRaeume() {
        return raeume;
    }
}
