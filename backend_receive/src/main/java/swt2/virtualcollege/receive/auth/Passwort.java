package swt2.virtualcollege.receive.auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Passwort-Hashing mit PBKDF2 und zufaelligem Salt pro Konto. Gespeichert wird
 * nur "salt:hash"; beim Login wird mit demselben Salt neu gehasht und verglichen.
 * Verwendet bewusst nur die Java-Standardbibliothek.
 */
public final class Passwort {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONEN = 120_000;
    private static final int SCHLUESSEL_BITS = 256;
    private static final int SALT_BYTES = 16;

    private Passwort() {
    }

    /** Erzeugt aus einem Klartext-Passwort den Speicherwert "salt:hash". */
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
        // Zeitkonstanter Vergleich als Schutz gegen Timing-Angriffe.
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
