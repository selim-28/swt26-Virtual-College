package swt2.virtualcollege.receive.persistenz;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/*
 * SCHRITT 3 (Datenbank) – eine ENTITY (= eine Tabellenzeile).
 *
 * @Entity sagt Spring/Hibernate: "fuer diese Klasse gibt es eine Tabelle".
 * Jedes Objekt dieser Klasse ist eine Zeile. So speichern wir die ueber
 * MQTT empfangenen Gerichte dauerhaft in der Datenbank.
 *
 * Eine Entity braucht (anders als ein record) einen leeren Konstruktor
 * und veraenderbare Felder mit get/set – das verlangt JPA.
 */
@Entity
public class GespeichertesGericht {

    // @Id = der Primaerschluessel. @GeneratedValue = die Datenbank vergibt
    // die Nummer automatisch (1, 2, 3, ...).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double preis;
    private Instant empfangenAm;

    // Leerer Konstruktor (von JPA benoetigt).
    protected GespeichertesGericht() {
    }

    public GespeichertesGericht(String name, double preis, Instant empfangenAm) {
        this.name = name;
        this.preis = preis;
        this.empfangenAm = empfangenAm;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPreis() {
        return preis;
    }

    public Instant getEmpfangenAm() {
        return empfangenAm;
    }
}
