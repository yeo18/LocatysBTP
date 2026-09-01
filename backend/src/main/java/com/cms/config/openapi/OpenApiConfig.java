package com.cms.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI cmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CMS - Chantier Management System API")
                        .description("API de gestion de chantiers : chantiers, equipes, employes, "
                                + "taches, validations, historiques et droits d'acces (RBAC dynamique).")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("Equipe CMS")
                                .email("contact@cms.example.com"))
                        .license(new License()
                                .name("Proprietaire")
                                .url("https://cms.example.com/licence")))
                .addServersItem(new Server()
                        .url("http://localhost:8091")
                        .description("Serveur de developpement local"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Authentification par token JWT (Bearer). "
                                        + "Le token est obtenu via POST /api/v1/auth/login.")));
    }

}
