package swt2.virtualcollege.send.mensa;

// "List" ist eine fertige Java-Klasse fuer eine Liste (mehrere Eintraege).
import java.util.List;

/*
 * Fachklasse "Speiseplan" aus dem Klassendiagramm (Kapitel 13).
 * Ein Speiseplan ist eine Liste von Gerichten fuer einen Tag.
 */
public record Speiseplan(List<Gericht> gerichte) {
}
