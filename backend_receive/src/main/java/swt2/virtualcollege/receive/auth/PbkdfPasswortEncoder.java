package swt2.virtualcollege.receive.auth;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Bindet das PBKDF2-Hashing der Klasse Passwort an die Spring-Security-Schnittstelle
 * PasswordEncoder an, damit bereits gespeicherte Hashes gueltig bleiben.
 */
public class PbkdfPasswortEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence klartext) {
        return Passwort.hash(klartext.toString());
    }

    @Override
    public boolean matches(CharSequence klartext, String gespeicherterHash) {
        return Passwort.pruefe(klartext.toString(), gespeicherterHash);
    }
}
