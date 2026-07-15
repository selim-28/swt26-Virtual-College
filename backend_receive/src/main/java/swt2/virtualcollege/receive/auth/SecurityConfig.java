package swt2.virtualcollege.receive.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * AUTHENTIFIZIERUNG (PRODUKTIONS-VARIANTE) – die zentrale Sicherheitsregel.
 *
 * Hier legen wir fest, WER WAS darf:
 *   - OEFFENTLICH (ohne Login): nur Login und Registrieren.
 *   - Alles andere unter /api/** verlangt ein gueltiges JWT-Token.
 *
 * Wichtige Einstellungen:
 *   - STATELESS: keine Server-Sitzung, der JWT-Ausweis genuegt (skaliert besser).
 *   - CSRF aus: fuer eine token-basierte REST-API nicht noetig (kein Cookie-Login).
 *   - Unser JwtAuthFilter laeuft VOR dem Standard-Login-Filter von Spring.
 *   - Eigene 401/403-Handler liefern saubere deutsche JSON-Meldungen.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;
    private final RestAuthEinstieg authEinstieg;
    private final RestZugriffVerweigert zugriffVerweigert;

    public SecurityConfig(JwtAuthFilter jwtFilter,
                          RestAuthEinstieg authEinstieg,
                          RestZugriffVerweigert zugriffVerweigert) {
        this.jwtFilter = jwtFilter;
        this.authEinstieg = authEinstieg;
        this.zugriffVerweigert = zugriffVerweigert;
    }

    @Bean
    public SecurityFilterChain sicherheitsKette(HttpSecurity http) throws Exception {
        http
                // REST-API mit Token: klassischer CSRF-Schutz (fuer Formular-Cookies) entfaellt.
                .csrf(csrf -> csrf.disable())
                // Keine Server-Sitzung: Jede Anfrage weist sich per JWT neu aus.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Wer darf wohin?
                .authorizeHttpRequests(regeln -> regeln
                        // Anmelden und Registrieren muessen ohne Token erreichbar sein.
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registrieren").permitAll()
                        // CORS-Vorabfragen (OPTIONS) nie blockieren.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // LOESCHEN (verwalten) darf nur der Admin. hasRole("ADMIN")
                        // prueft die Berechtigung "ROLE_ADMIN", die der JwtAuthFilter
                        // aus der Rolle "Admin" im Token baut.
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        // Alles Uebrige nur mit gueltigem Login.
                        .anyRequest().authenticated())
                // Saubere JSON-Fehler statt leerer 401/403-Seiten.
                .exceptionHandling(f -> f
                        .authenticationEntryPoint(authEinstieg)
                        .accessDeniedHandler(zugriffVerweigert))
                // Unseren Token-Filter vor den Standard-Login-Filter haengen.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Ungenutzte Standard-Logins abschalten (wir nutzen ausschliesslich JWT).
                .httpBasic(b -> b.disable())
                .formLogin(fl -> fl.disable());
        return http.build();
    }

    /*
     * Der PasswordEncoder, den Spring Security zum Pruefen/Erzeugen von
     * Passwort-Hashes benutzt. Wir binden unser bestehendes PBKDF2-Hashing an,
     * damit alte Hashes und das Demo-Konto weiter funktionieren.
     */
    @Bean
    public PasswordEncoder passwortEncoder() {
        return new PbkdfPasswortEncoder();
    }
}
