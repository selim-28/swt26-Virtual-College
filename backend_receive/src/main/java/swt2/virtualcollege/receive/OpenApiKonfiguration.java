package swt2.virtualcollege.receive;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Beschreibt die automatisch erzeugte Schnittstellen-Doku (OpenAPI/Swagger).
 *
 * springdoc liest alle REST-Controller aus und baut daraus die komplette
 * API-Uebersicht. Diese Klasse ergaenzt nur zwei Dinge:
 *   1. Titel/Beschreibung, damit die Doku nach unserem Projekt aussieht.
 *   2. Das JWT-Bearer-Schema, damit der "Authorize"-Knopf in der Oberflaeche
 *      unser Login-Token annimmt und man geschuetzte Endpunkte direkt
 *      ausprobieren kann (Token vorher ueber POST /api/auth/login holen).
 *
 * Aufrufbar unter:  /swagger-ui.html  (Oberflaeche)  und  /v3/api-docs (JSON).
 */
@Configuration
public class OpenApiKonfiguration {

    @Bean
    public OpenAPI virtualCollegeApi() {
        String schemaName = "JWT-Login";
        return new OpenAPI()
                .info(new Info()
                        .title("Virtual College – backend_receive API")
                        .description("Alle synchronen REST-Schnittstellen des Application Servers: "
                                + "Auth (Login/Registrierung), Campus (Karte, Reservierungen), "
                                + "Pinwand (Posts, Likes, Benachrichtigungen), Mensa und OePNV. "
                                + "Geschuetzte Endpunkte erwarten ein JWT aus POST /api/auth/login.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(schemaName))
                .components(new Components().addSecuritySchemes(schemaName,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
