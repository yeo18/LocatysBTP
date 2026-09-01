package com.cms.config.properties;

import com.cms.common.enums.ApplicationMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Proprietes applicatives centralisees.
 * Regroupees par domaine : application, securite, api.
 * Les secrets ne sont jamais stockes ici (variables d'environnement).
 *
 * Mapping yml :
 *   app.name, app.version, app.mode
 *   app.security.jwt.secret, app.security.jwt.expiration-ms
 *   app.api.prefix
 */
@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private Application app = new Application();
    private Security security = new Security();
    private Api api = new Api();

    @Data
    public static class Application {
        private String name;
        private String version;
        private ApplicationMode mode = ApplicationMode.STANDALONE;
    }

    @Data
    public static class Security {
        private Jwt jwt = new Jwt();

        @Data
        public static class Jwt {
            private String secret;
            private long expirationMs;
        }
    }

    @Data
    public static class Api {
        private String prefix = "/api/v1";
    }

}
