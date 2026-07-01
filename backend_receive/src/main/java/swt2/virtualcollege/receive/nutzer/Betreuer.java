package swt2.virtualcollege.receive.nutzer;

/*
 * "Betreuer" (Kapitel 13): eine Rolle des Nutzers. "ist ein" Nutzer.
 * Im Diagramm:  Nutzer <|-- Betreuer
 */
public class Betreuer extends Nutzer {

    public Betreuer(String name) {
        super(name);
    }
}
