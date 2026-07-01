package swt2.virtualcollege.receive.persistenz;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import swt2.virtualcollege.receive.mensa.Gericht;
import swt2.virtualcollege.receive.mensa.Speiseplan;

/*
 * SCHRITT 3 (Datenbank) – der Service, der das Speichern kapselt.
 *
 * Wenn ueber MQTT ein Speiseplan ankommt, schreibt dieser Service jedes
 * Gericht als eigene Zeile in die Datenbank (mit Zeitstempel). Spaeter
 * kann man die ganze "Historie" wieder auslesen – das beweist, dass die
 * Daten DAUERHAFT gespeichert sind (im Verteilungsdiagramm: der JDBC-Pfeil
 * vom Application Server zur Datenbank).
 */
@Service
public class SpeisearchivService {

    private static final Logger log = LoggerFactory.getLogger(SpeisearchivService.class);

    private final GerichtRepository repository;

    public SpeisearchivService(GerichtRepository repository) {
        this.repository = repository;
    }

    // Speichert alle Gerichte eines Speiseplans in der Datenbank.
    public void speichere(Speiseplan speiseplan) {
        Instant jetzt = Instant.now();
        for (Gericht g : speiseplan.gerichte()) {
            repository.save(new GespeichertesGericht(g.name(), g.preis(), jetzt));
        }
        log.info("[Datenbank] {} Gerichte gespeichert (insgesamt jetzt {}).",
                speiseplan.gerichte().size(), repository.count());
    }

    // Liefert die gesamte Historie (neueste zuerst).
    public List<GespeichertesGericht> historie() {
        return repository.findAllByOrderByIdDesc();
    }

    // Wie viele Zeilen liegen insgesamt in der Datenbank?
    public long anzahl() {
        return repository.count();
    }
}
