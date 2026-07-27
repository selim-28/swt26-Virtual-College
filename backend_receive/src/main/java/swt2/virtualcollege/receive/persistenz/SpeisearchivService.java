package swt2.virtualcollege.receive.persistenz;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import swt2.virtualcollege.receive.mensa.Gericht;
import swt2.virtualcollege.receive.mensa.Speiseplan;

/**
 * Kapselt das dauerhafte Speichern der ueber MQTT empfangenen Speiseplaene und
 * bildet damit den JDBC-Pfeil des Verteilungsdiagramms ab.
 */
@Service
public class SpeisearchivService {

    private static final Logger log = LoggerFactory.getLogger(SpeisearchivService.class);

    private final GerichtRepository repository;

    public SpeisearchivService(GerichtRepository repository) {
        this.repository = repository;
    }

    /** Schreibt jedes Gericht eines Speiseplans als eigene Zeile mit Zeitstempel. */
    public void speichere(Speiseplan speiseplan) {
        Instant jetzt = Instant.now();
        for (Gericht g : speiseplan.gerichte()) {
            repository.save(new GespeichertesGericht(g.name(), g.preis(), jetzt));
        }
        log.info("[Datenbank] {} Gerichte gespeichert (insgesamt jetzt {}).",
                speiseplan.gerichte().size(), repository.count());
    }

    /** Die gesamte Historie, neueste zuerst. */
    public List<GespeichertesGericht> historie() {
        return repository.findAllByOrderByIdDesc();
    }

    /** Anzahl aller gespeicherten Zeilen. */
    public long anzahl() {
        return repository.count();
    }
}
