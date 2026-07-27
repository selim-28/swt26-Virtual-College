package swt2.virtualcollege.receive.mensa;

import java.util.List;

/** Fachklasse "Speiseplan": das Ergebnis, das der Adapter liefert. */
public record Speiseplan(List<Gericht> gerichte) {
}
