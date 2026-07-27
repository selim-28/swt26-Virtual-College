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

/**
 * Zentrale Sicherheitsregeln: Login und Registrierung sind oeffentlich, alles
 * andere unter /api/** verlangt ein gueltiges JWT, Loeschen zusaetzlich die
 * Admin-Rolle. Die Anwendung arbeitet stateless, ohne Server-Sitzung.
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
                // Token-basierte REST-API: CSRF-Schutz fuer Formular-Cookies entfaellt.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regeln -> regeln
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registrieren").permitAll()
                        // Die Doku beschreibt die API nur; Ausprobieren geht weiterhin nur mit Token.
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // hasRole("ADMIN") prueft die Berechtigung ROLE_ADMIN,
                        // die der JwtAuthFilter aus der Rolle im Token bildet.
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Saubere JSON-Fehler statt leerer 401/403-Seiten.
                .exceptionHandling(f -> f
                        .authenticationEntryPoint(authEinstieg)
                        .accessDeniedHandler(zugriffVerweigert))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(b -> b.disable())
                .formLogin(fl -> fl.disable());
        return http.build();
    }

    /** Bindet das PBKDF2-Hashing an, mit dem bestehende Konten angelegt wurden. */
    @Bean
    public PasswordEncoder passwortEncoder() {
        return new PbkdfPasswortEncoder();
    }
}
