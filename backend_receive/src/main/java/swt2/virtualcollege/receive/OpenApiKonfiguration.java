package swt2.virtualcollege.receive;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ergaenzt die von springdoc erzeugte Schnittstellendokumentation um Titel,
 * Beschreibung und das JWT-Bearer-Schema, damit geschuetzte Endpunkte direkt
 * ueber die Oberflaeche ausprobiert werden koennen.
 * Erreichbar unter /swagger-ui.html und /v3/api-docs.
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
