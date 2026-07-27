package swt2.virtualcollege.receive.campus;

/** Ein Ort auf der Karte in Breite und Laenge. */
public record Koordinaten(double breite, double laenge) {

    /** Vereinfachte Luftlinie zwischen zwei Punkten. */
    public double distanzZu(Koordinaten andere) {
        double dB = this.breite - andere.breite;
        double dL = this.laenge - andere.laenge;
        return Math.sqrt(dB * dB + dL * dL);
    }
}
