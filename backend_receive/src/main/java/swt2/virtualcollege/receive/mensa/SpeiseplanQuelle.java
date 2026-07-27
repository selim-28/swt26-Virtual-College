package swt2.virtualcollege.receive.mensa;

/**
 * Adapter-Muster: das Target-Interface. Die Anwendung fordert einen Speiseplan an,
 * ohne die konkrete Datenquelle zu kennen.
 */
public interface SpeiseplanQuelle {

    Speiseplan holeSpeiseplan();
}
