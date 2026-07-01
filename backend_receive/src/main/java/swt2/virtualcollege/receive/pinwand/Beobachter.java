package swt2.virtualcollege.receive.pinwand;

/*
 * Muster "Observer" – das BEOBACHTER-Interface (Kapitel 13, Aufgabe 2).
 *
 * Jeder, der von der Pinwand benachrichtigt werden will, muss "aktualisiere"
 * anbieten. So muss die Pinwand ihre Empfaenger nicht einzeln kennen –
 * sie kennt nur "etwas, das benachrichtigt werden kann" (geringe Kopplung).
 */
public interface Beobachter {

    // Wird vom Subjekt (Pinwand) aufgerufen, wenn etwas Neues passiert.
    void aktualisiere(Benachrichtigung b);
}
