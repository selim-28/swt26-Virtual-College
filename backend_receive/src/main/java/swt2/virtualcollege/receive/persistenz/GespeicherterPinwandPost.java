package swt2.virtualcollege.receive.persistenz;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

/*
 * Persistente Pinwand-Nachricht.
 *
 * Diese Entity macht die Pinwand wirklich nutzbar: Posts verschwinden
 * nicht mehr nach einem Neustart, sondern liegen als Tabellenzeilen in
 * H2 bzw. MariaDB. Damit wird aus dem Observer-Beispiel eine echte
 * Campus-Funktion fuer Studierende.
 */
@Entity
public class GespeicherterPinwandPost {

    // Primaerschluessel: die Datenbank vergibt die ID automatisch.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Autor, Rolle und Kategorie werden im Frontend angezeigt.
    private String autor;
    private String rolle;
    private String kategorie;
    private String titel;

    // @Lob erlaubt laengeren Text als eine normale VARCHAR-Spalte.
    @Lob
    private String inhalt;

    // Kleine Nutzungsdaten fuer die Pinwand.
    private int likes;
    private Instant erstelltAm;

    // Leerer Konstruktor: JPA braucht ihn, um Objekte aus DB-Zeilen zu bauen.
    protected GespeicherterPinwandPost() {
    }

    // Fachlicher Konstruktor: damit erzeugt unser Service neue Posts.
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

    // Ein Like ist eine echte Aenderung am gespeicherten Post.
    public void like() {
        this.likes++;
    }
}
