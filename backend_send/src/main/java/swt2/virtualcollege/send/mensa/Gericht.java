// Unterordner (= Modul) "mensa" im Fremdsystem-Backend.
package swt2.virtualcollege.send.mensa;

/*
 * Fachklasse "Gericht" aus dem OOA/OOD-Klassendiagramm (Kapitel 13).
 * Ein Gericht hat einen Namen und einen Preis.
 *
 * "record" ist die kurze Java-Schreibweise fuer ein einfaches Datenobjekt:
 * Java erzeugt Konstruktor, getName()/getPreis() (hier name()/preis())
 * und equals/hashCode automatisch.
 */
public record Gericht(String name, double preis) {
}
