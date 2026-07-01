package swt2.virtualcollege.receive.nutzer;

/*
 * "Verwaltungsangestellter" (Kapitel 13): eine Rolle des Nutzers.
 * "ist ein" Nutzer. Im Diagramm:  Nutzer <|-- Verwaltungsangestellter
 */
public class Verwaltungsangestellter extends Nutzer {

    public Verwaltungsangestellter(String name) {
        super(name);
    }
}
