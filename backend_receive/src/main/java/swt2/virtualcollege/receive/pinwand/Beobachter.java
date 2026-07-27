package swt2.virtualcollege.receive.pinwand;

/**
 * Observer-Muster: das Beobachter-Interface. Die Pinwand kennt ihre Empfaenger
 * nur ueber diesen Vertrag.
 */
public interface Beobachter {

    void aktualisiere(Benachrichtigung b);
}
