package swt2.virtualcollege.receive.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Datenbankzugriff auf Benutzerkonten. Spring Data JPA leitet die Abfragen aus den Methodennamen ab. */
public interface BenutzerkontoRepository extends JpaRepository<Benutzerkonto, Long> {

    Optional<Benutzerkonto> findByEmail(String email);

    boolean existsByEmail(String email);
}
