package swt2.virtualcollege.receive.auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/*
 * AUTHENTIFIZIERUNG – das sichere Passwort-Hashing.
 *
 * Wir speichern Passwoerter NIE im Klartext. Stattdessen:
 *  1. wir wuerfeln ein zufaelliges "Salt" (Salz) pro Konto,
 *  2. wir rechnen das Passwort + Salt 120.000-mal durch PBKDF2 (ein
 *     absichtlich langsames Verfahren, das Knacken teuer macht),
 *  3. wir speichern nur "salt:hash" – nie das echte Passwort.
 *
 * Beim Login wird das eingegebene Passwort mit DEMSELBEN Salt erneut
 * gehasht und mit dem gespeicherten Hash verglichen.
 *
 * Wir benutzen bewusst nur die Java-Standardbibliothek (javax.crypto),
 * damit keine zusaetzliche Bibliothek noetig ist.
 */
public final class Passwort {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONEN = 120_000;   // mehr = sicherer, aber langsamer
    private static final int SCHLUESSEL_BITS = 256;
    private static final int SALT_BYTES = 16;

    private Passwort() {
        // Hilfsklasse: keine Objekte noetig.
    }

    /** Erzeugt aus einem Klartext-Passwort den Speicher-Wert "salt:hash". */
    public static String hash(String klartext) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(klartext.toCharArray(), salt);
        return base64(salt) + ":" + base64(hash);
    }

    /** Prueft ein eingegebenes Passwort gegen den gespeicherten "salt:hash". */
    public static boolean pruefe(String klartext, String gespeichert) {
        if (klartext == null || gespeichert == null) {
            return false;
        }
        String[] teile = gespeichert.split(":");
        if (teile.length != 2) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(teile[0]);
        byte[] erwartet = Base64.getDecoder().decode(teile[1]);
        byte[] tatsaechlich = pbkdf2(klartext.toCharArray(), salt);
        // Zeit-konstanter Vergleich (schuetzt gegen "Timing-Angriffe").
        return MessageDigest.isEqual(erwartet, tatsaechlich);
    }

    private static byte[] pbkdf2(char[] passwort, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(passwort, salt, ITERATIONEN, SCHLUESSEL_BITS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Passwort-Hashing fehlgeschlagen", e);
        }
    }

    private static String base64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
