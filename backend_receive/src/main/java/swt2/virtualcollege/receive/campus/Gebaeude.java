package swt2.virtualcollege.receive.campus;

import java.util.ArrayList;
import java.util.List;

/** Ein Gebaeude und die Raeume, aus denen es besteht. */
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
