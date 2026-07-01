package swt2.virtualcollege.receive.nutzer;

/*
 * "Student" (Kapitel 13): eine Rolle des Nutzers. "ist ein" Nutzer.
 * Im Diagramm:  Nutzer <|-- Student
 */
public class Student extends Nutzer {

    public Student(String name) {
        super(name);
    }
}
