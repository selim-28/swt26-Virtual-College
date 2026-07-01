package swt2.virtualcollege.receive.persistenz;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Datenbank-Zugang fuer Pinwand-Posts.
 *
 * Spring Data JPA erzeugt die Implementierung automatisch. Wir brauchen
 * nur die fachlich lesbare Methode "neueste zuerst".
 */
public interface PinwandPostRepository extends JpaRepository<GespeicherterPinwandPost, Long> {

    // Liefert Posts so, wie Nutzer eine Pinwand erwarten: neueste oben.
    List<GespeicherterPinwandPost> findAllByOrderByIdDesc();
}
