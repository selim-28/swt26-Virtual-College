package swt2.virtualcollege.receive.persistenz;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Datenbank-Zugang fuer Raumreservierungen.
 *
 * Auch hier schreibt Spring Data JPA die eigentliche Implementierung
 * automatisch. Wir formulieren nur, welche Abfragen wir brauchen.
 */
public interface ReservierungRepository extends JpaRepository<GespeicherteReservierung, Long> {

    // Alle Reservierungen fuer einen Raum, neueste zuerst.
    List<GespeicherteReservierung> findAllByRaumIdOrderByIdDesc(String raumId);

    // Alle Reservierungen im System, neueste zuerst.
    List<GespeicherteReservierung> findAllByOrderByIdDesc();
}
