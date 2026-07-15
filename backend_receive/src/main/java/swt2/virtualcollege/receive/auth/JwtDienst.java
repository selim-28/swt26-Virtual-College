package swt2.virtualcollege.receive.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/*
 * AUTHENTIFIZIERUNG (PRODUKTIONS-VARIANTE) – erzeugt und prueft JWT-Token.
 *
 * Ein JWT ("JSON Web Token") ist ein digital SIGNIERTER Ausweis. Er besteht
 * aus drei Teilen (Header.Payload.Signatur) und enthaelt offen lesbare Daten
 * (E-Mail, Name, Rolle, Ablaufzeit) sowie eine Signatur. Die Signatur wird mit
 * unserem GEHEIMEN Schluessel erzeugt – ohne diesen Schluessel kann niemand ein
 * gueltiges Token faelschen oder den Inhalt unbemerkt aendern.
 *
 * Vorteil gegenueber der alten In-Memory-Token-Map: Das Backend muss sich keine
 * Tokens mehr merken. Es kann allein anhand der Signatur pruefen, ob ein Token
 * echt und noch gueltig ist ("stateless"). Das skaliert und uebersteht Neustarts.
 *
 * "jti" (JWT-ID) = eine eindeutige Nummer pro Token. Wir brauchen sie, damit ein
 * Token beim Logout gezielt gesperrt werden kann (siehe TokenSperrliste).
 */
@Component
public class JwtDienst {

    private final SecretKey schluessel;
    private final long gueltigkeitMs;

    public JwtDienst(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes:1440}") long minuten) {
        // HS256 verlangt einen Schluessel von mindestens 256 Bit (32 Zeichen).
        this.schluessel = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.gueltigkeitMs = minuten * 60_000L;
    }

    /** Stellt fuer ein Konto einen frischen, signierten Ausweis (JWT) aus. */
    public String erzeuge(Benutzerkonto konto) {
        Instant jetzt = Instant.now();
        return Jwts.builder()
                .subject(konto.getEmail())            // "wem gehoert das Token?"
                .claim("name", konto.getName())
                .claim("rolle", konto.getRolle())
                .id(UUID.randomUUID().toString())     // jti fuer die Logout-Sperrliste
                .issuedAt(Date.from(jetzt))
                .expiration(Date.from(jetzt.plusMillis(gueltigkeitMs)))
                .signWith(schluessel)
                .compact();
    }

    /**
     * Prueft Signatur und Ablaufdatum und gibt den Inhalt (Claims) zurueck.
     * Wirft eine JwtException, wenn das Token gefaelscht oder abgelaufen ist.
     */
    public Claims pruefe(String token) {
        return Jwts.parser()
                .verifyWith(schluessel)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
