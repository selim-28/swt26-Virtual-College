package swt2.virtualcollege.receive.campus;

/*
 * "Koordinaten" (Kapitel 13): ein Ort auf der Karte (Breite/Laenge).
 * Im Diagramm:  Raum --> Koordinaten
 */
public record Koordinaten(double breite, double laenge) {

    // Sehr einfache "Luftlinie" zwischen zwei Punkten (nur zur Veranschaulichung).
    public double distanzZu(Koordinaten andere) {
        double dB = this.breite - andere.breite;
        double dL = this.laenge - andere.laenge;
        return Math.sqrt(dB * dB + dL * dL);
    }
}
