package swt2.virtualcollege.receive.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * AUTHENTIFIZIERUNG (PRODUKTIONS-VARIANTE) – der Tuersteher pro Anfrage.
 *
 * Dieser Filter laeuft bei JEDER HTTP-Anfrage EINMAL (OncePerRequestFilter) und:
 *   1. liest den Header  "Authorization: Bearer <token>",
 *   2. prueft mit dem JwtDienst Signatur + Ablauf,
 *   3. prueft die Logout-Sperrliste,
 *   4. legt bei Erfolg den angemeldeten Nutzer in den Spring-Security-Kontext.
 *
 * Ist kein/ein ungueltiges Token dabei, passiert hier NICHTS Schlimmes: Die
 * Anfrage bleibt "anonym". Ob das erlaubt ist, entscheidet danach die
 * SecurityConfig (z. B. Login/Registrieren erlaubt, alles andere gesperrt).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtDienst jwt;
    private final TokenSperrliste sperrliste;

    public JwtAuthFilter(JwtDienst jwt, TokenSperrliste sperrliste) {
        this.jwt = jwt;
        this.sperrliste = sperrliste;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest anfrage,
                                    HttpServletResponse antwort,
                                    FilterChain kette) throws ServletException, IOException {
        String token = leseToken(anfrage);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims inhalt = jwt.pruefe(token);
                if (!sperrliste.istGesperrt(inhalt.getId())) {
                    setzeAngemeldet(inhalt);
                }
            } catch (JwtException | IllegalArgumentException ungueltig) {
                // Gefaelschtes/abgelaufenes Token -> Anfrage bleibt anonym.
                SecurityContextHolder.clearContext();
            }
        }
        kette.doFilter(anfrage, antwort);
    }

    /** Holt das Token aus dem Header "Authorization: Bearer <token>". */
    private String leseToken(HttpServletRequest anfrage) {
        String header = anfrage.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }

    /** Traegt den Nutzer (mit seiner Rolle) in den Sicherheitskontext ein. */
    private void setzeAngemeldet(Claims inhalt) {
        String rolle = inhalt.get("rolle", String.class);
        AngemeldeterNutzer nutzer = new AngemeldeterNutzer(
                inhalt.getSubject(),
                inhalt.get("name", String.class),
                rolle);
        // Aus der Rolle wird eine Spring-Berechtigung, z. B. "ROLE_STUDENT".
        var berechtigungen = List.of(
                new SimpleGrantedAuthority("ROLE_" + (rolle == null ? "STUDENT" : rolle.toUpperCase())));
        var auth = new UsernamePasswordAuthenticationToken(nutzer, null, berechtigungen);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
