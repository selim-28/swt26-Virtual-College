package swt2.virtualcollege.receive.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Passwort-Hashing ueber die Spring-Security-Schnittstelle (PBKDF2 mit Salt). */
class PbkdfPasswortEncoderTest {

    private final PasswordEncoder encoder = new PbkdfPasswortEncoder();

    @Test
    void richtigesPasswortPasstAufSeinenHash() {
        String hash = encoder.encode("geheim123");
        assertThat(encoder.matches("geheim123", hash)).isTrue();
    }

    @Test
    void falschesPasswortPasstNicht() {
        String hash = encoder.encode("geheim123");
        assertThat(encoder.matches("falsch", hash)).isFalse();
    }

    @Test
    void hashEnthaeltNiemalsDasKlartextPasswort() {
        String hash = encoder.encode("geheim123");
        assertThat(hash).doesNotContain("geheim123");
        assertThat(hash).contains(":");
    }

    @Test
    void gleichesPasswortErgibtDurchSaltVerschiedeneHashes() {
        assertThat(encoder.encode("geheim123")).isNotEqualTo(encoder.encode("geheim123"));
    }
}
