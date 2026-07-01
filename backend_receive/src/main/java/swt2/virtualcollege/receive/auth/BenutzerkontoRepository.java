package swt2.virtualcollege.receive.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * AUTHENTIFIZIERUNG – der Datenbank-Zugang fuer Benutzerkonten.
 *
 * Wir schreiben nur die Methoden-Namen hin – den eigentlichen SQL-Code
 * baut Spring Data JPA daraus automatisch. "findByEmail" sucht z. B. das
 * Konto mit dieser E-Mail.
 */
public interface BenutzerkontoRepository extends JpaRepository<Benutzerkonto, Long> {

    Optional<Benutzerkonto> findByEmail(String email);

    boolean existsByEmail(String email);
}
