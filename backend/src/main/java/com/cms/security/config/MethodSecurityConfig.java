package com.cms.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

import com.cms.security.permission.SpringPermissionEvaluator;

/**
 * Configuration du moteur d'expressions de securite des methodes.
 *
 * <p>Enregistre l'adaptateur {@link SpringPermissionEvaluator} afin de rendre
 * utilisable l'expression :
 * <pre>
 * &#64;PreAuthorize("hasPermission('MODULE','ACTION')")
 * </pre>
 *
 * <p>{@code @EnableMethodSecurity} est deja active sur {@code SecurityConfig} ;
 * ce bean est automatiquement utilise par Spring Security.
 */
@Configuration
public class MethodSecurityConfig {

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            SpringPermissionEvaluator springPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(springPermissionEvaluator);
        return handler;
    }

}
