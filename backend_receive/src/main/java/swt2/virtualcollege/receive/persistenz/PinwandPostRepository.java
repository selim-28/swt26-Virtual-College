package swt2.virtualcollege.receive.persistenz;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Datenbankzugriff auf Pinwand-Beitraege. */
public interface PinwandPostRepository extends JpaRepository<GespeicherterPinwandPost, Long> {

    /** Beitraege in Anzeigereihenfolge: neueste zuerst. */
    List<GespeicherterPinwandPost> findAllByOrderByIdDesc();
}
