package swt2.virtualcollege.receive.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/*
 * Unit-Tests fuer den JwtDienst (kein Spring noetig): Erzeugen/Pruefen,
 * abgelaufene und verfaelschte Tokens.
 */
class JwtDienstTest {

    // Fuer HS256 muss der Schluessel mindestens 32 Zeichen (256 Bit) lang sein.
    private static final String SECRET = "test-geheimer-schluessel-mindestens-32z!";

    private Benutzerkonto konto() {
        return new Benutzerkonto("a@b.de", "Alice", "Student", "salt:hash", Instant.now());
    }

    @Test
    void erzeugtesTokenLaesstSichPruefenUndEnthaeltDieAngaben() {
        JwtDienst dienst = new JwtDienst(SECRET, 60);

        String token = dienst.erzeuge(konto());
        Claims inhalt = dienst.pruefe(token);

        assertThat(token.split("\\.")).hasSize(3);          // Kopf.Inhalt.Signatur
        assertThat(inhalt.getSubject()).isEqualTo("a@b.de");
        assertThat(inhalt.get("name", String.class)).isEqualTo("Alice");
        assertThat(inhalt.get("rolle", String.class)).isEqualTo("Student");
        assertThat(inhalt.getId()).isNotBlank();            // jti fuer die Sperrliste
        assertThat(inhalt.getExpiration()).isAfter(inhalt.getIssuedAt());
    }

    @Test
    void jedesTokenBekommtEineEigeneNummer() {
        JwtDienst dienst = new JwtDienst(SECRET, 60);

        String jti1 = dienst.pruefe(dienst.erzeuge(konto())).getId();
        String jti2 = dienst.pruefe(dienst.erzeuge(konto())).getId();

        assertThat(jti1).isNotEqualTo(jti2);
    }

    @Test
    void abgelaufenesTokenWirdAbgelehnt() {
        // Gueltigkeit -1 Minute -> das Token ist sofort abgelaufen.
        JwtDienst dienst = new JwtDienst(SECRET, -1);
        String token = dienst.erzeuge(konto());

        assertThatThrownBy(() -> dienst.pruefe(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void verfaelschtesTokenWirdAbgelehnt() {
        JwtDienst dienst = new JwtDienst(SECRET, 60);
        String[] teile = dienst.erzeuge(konto()).split("\\.");   // Kopf.Inhalt.Signatur
        // Erstes Zeichen des Inhalts aendern -> die Signatur passt nicht mehr.
        char erstes = teile[1].charAt(0);
        char neu = (erstes == 'A') ? 'B' : 'A';
        String verfaelscht = teile[0] + "." + neu + teile[1].substring(1) + "." + teile[2];

        assertThatThrownBy(() -> dienst.pruefe(verfaelscht)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenMitAnderemSchluesselWirdAbgelehnt() {
        String token = new JwtDienst(SECRET, 60).erzeuge(konto());
        JwtDienst andererServer = new JwtDienst("ein-voellig-anderer-geheimer-schluessel-32z", 60);

        assertThatThrownBy(() -> andererServer.pruefe(token)).isInstanceOf(JwtException.class);
    }
}
