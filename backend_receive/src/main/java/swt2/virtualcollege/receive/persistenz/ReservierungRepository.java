package swt2.virtualcollege.receive.persistenz;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Datenbankzugriff auf Raumreservierungen. */
public interface ReservierungRepository extends JpaRepository<GespeicherteReservierung, Long> {

    /** Reservierungen eines Raumes, neueste zuerst. */
    List<GespeicherteReservierung> findAllByRaumIdOrderByIdDesc(String raumId);

    /** Alle Reservierungen, neueste zuerst. */
    List<GespeicherteReservierung> findAllByOrderByIdDesc();
}
