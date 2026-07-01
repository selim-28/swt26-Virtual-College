package swt2.virtualcollege.receive.mensa;

import java.util.List;

/*
 * Fachklasse "Speiseplan" (Kapitel 13): die Liste der Gerichte.
 * Das ist das SAUBERE Ergebnis, das der Adapter am Ende liefert.
 */
public record Speiseplan(List<Gericht> gerichte) {
}
