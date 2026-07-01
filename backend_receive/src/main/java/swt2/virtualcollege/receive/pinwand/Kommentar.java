package swt2.virtualcollege.receive.pinwand;

/*
 * "Kommentar" (Kapitel 13): gehoert zu einem Post (Post *-- Kommentar).
 * Einfaches Datenobjekt: wer hat was geschrieben.
 */
public record Kommentar(String autor, String text) {
}
