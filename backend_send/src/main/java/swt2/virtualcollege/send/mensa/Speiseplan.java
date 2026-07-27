package swt2.virtualcollege.send.mensa;

import java.util.List;

/** Fachklasse "Speiseplan" aus dem Klassendiagramm: die Gerichte eines Tages. */
public record Speiseplan(List<Gericht> gerichte) {
}
