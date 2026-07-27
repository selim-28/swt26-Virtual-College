package swt2.virtualcollege.receive.persistenz;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Ein ueber MQTT empfangenes Gericht, dauerhaft in der Datenbank abgelegt. */
@Entity
public class GespeichertesGericht {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double preis;
    private Instant empfangenAm;

    // Von JPA benoetigt.
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
