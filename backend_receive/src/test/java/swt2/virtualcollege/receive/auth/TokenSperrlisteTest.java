package swt2.virtualcollege.receive.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/*
 * Unit-Tests fuer die Logout-Sperrliste.
 */
class TokenSperrlisteTest {

    @Test
    void unbekanntesTokenIstNichtGesperrt() {
        TokenSperrliste liste = new TokenSperrliste();
        assertThat(liste.istGesperrt("egal")).isFalse();
        assertThat(liste.istGesperrt(null)).isFalse();
    }

    @Test
    void gesperrtesTokenWirdErkannt() {
        TokenSperrliste liste = new TokenSperrliste();
        liste.sperre("jti-1", Instant.now().plusSeconds(60));
        assertThat(liste.istGesperrt("jti-1")).isTrue();
    }

    @Test
    void abgelaufeneSperreZaehltNichtMehr() {
        TokenSperrliste liste = new TokenSperrliste();
        // Ablauf liegt in der Vergangenheit -> Eintrag ist bedeutungslos.
        liste.sperre("jti-alt", Instant.now().minusSeconds(1));
        assertThat(liste.istGesperrt("jti-alt")).isFalse();
    }
}
