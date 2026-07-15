package swt2.virtualcollege.receive.auth;

import org.springframework.security.crypto.password.PasswordEncoder;

/*
 * AUTHENTIFIZIERUNG (PRODUKTIONS-VARIANTE) – Bruecke zu Spring Security.
 *
 * Spring Security kennt keine konkrete Hash-Methode, sondern nur die Schnittstelle
 * PasswordEncoder (encode + matches). Wir binden hier unser bereits vorhandenes,
 * sicheres PBKDF2-Hashing (Klasse Passwort) an diese Schnittstelle an.
 *
 * Vorteil: Bereits gespeicherte Passwort-Hashes (Format "salt:hash") und das
 * Demo-Konto bleiben GUELTIG – wir muessen die Datenbank nicht neu befuellen.
 * (In einem Neuprojekt wuerde man hier direkt BCryptPasswordEncoder nehmen.)
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
