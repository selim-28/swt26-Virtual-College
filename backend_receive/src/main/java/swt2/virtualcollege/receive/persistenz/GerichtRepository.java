package swt2.virtualcollege.receive.persistenz;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * SCHRITT 3 (Datenbank) – ein REPOSITORY.
 *
 * Das ist das Wunder von Spring Data JPA: Wir schreiben nur dieses
 * Interface, und Spring baut daraus AUTOMATISCH die ganze Datenbank-Logik
 * (speichern, suchen, zaehlen ...). JpaRepository<Entity, Typ-des-Id>
 * bringt z. B. save(), findAll(), count() schon mit.
 */
public interface GerichtRepository extends JpaRepository<GespeichertesGericht, Long> {

    // Eigene Abfrage nur durch den Methoden-NAMEN: "alle, neueste zuerst".
    // Spring versteht den Namen und baut das passende SQL daraus.
    List<GespeichertesGericht> findAllByOrderByIdDesc();
}
