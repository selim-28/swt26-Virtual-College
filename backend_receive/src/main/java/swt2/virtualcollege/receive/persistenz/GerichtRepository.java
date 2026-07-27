package swt2.virtualcollege.receive.persistenz;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Datenbankzugriff auf die empfangenen Gerichte. */
public interface GerichtRepository extends JpaRepository<GespeichertesGericht, Long> {

    /** Alle Eintraege, neueste zuerst. */
    List<GespeichertesGericht> findAllByOrderByIdDesc();
}
