package uz.consortgroup.support_service.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Webinar Service API")
                        .description("API документация для Webinar Service")
                        .version("v1.0")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("GitHub репозиторий")
                        .url("https://github.com/Consort-Group-Corp/support_service"));
    }
}