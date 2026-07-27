package swt2.virtualcollege.receive.persistenz;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

/**
 * Persistenter Pinwand-Beitrag. Dadurch ueberstehen Beitraege einen Neustart
 * der Anwendung.
 */
@Entity
public class GespeicherterPinwandPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String autor;
    private String rolle;
    private String kategorie;
    private String titel;

    // @Lob erlaubt laengeren Text als eine VARCHAR-Spalte.
    @Lob
    private String inhalt;

    private int likes;
    private Instant erstelltAm;

    // Von JPA benoetigt.
    protected GespeicherterPinwandPost() {
    }

    public GespeicherterPinwandPost(String autor, String rolle, String kategorie,
                                    String titel, String inhalt, Instant erstelltAm) {
        this.autor = autor;
        this.rolle = rolle;
        this.kategorie = kategorie;
        this.titel = titel;
        this.inhalt = inhalt;
        this.erstelltAm = erstelltAm;
        this.likes = 0;
    }

    public Long getId() {
        return id;
    }

    public String getAutor() {
        return autor;
    }

    public String getRolle() {
        return rolle;
    }

    public String getKategorie() {
        return kategorie;
    }

    public String getTitel() {
        return titel;
    }

    public String getInhalt() {
        return inhalt;
    }

    public int getLikes() {
        return likes;
    }

    public Instant getErstelltAm() {
        return erstelltAm;
    }

    public void like() {
        this.likes++;
    }
}
