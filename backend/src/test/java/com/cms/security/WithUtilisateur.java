package com.cms.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * Place un utilisateur authentifie (PrincipalUtilisateur reel) dans le
 * contexte de securite pour les tests d'integration des Controllers.
 *
 * <p>Contrairement a {@code @WithMockUser}, le principal est bien un
 * {@code PrincipalUtilisateur} : le {@code CurrentUserService} et le
 * {@code PermissionEvaluatorImpl} (RBAC) peuvent fonctionner normalement.
 * Les droits effectifs sont controles en mockant {@code DroitsService}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WithSecurityContext(factory = WithUtilisateurSecurityContextFactory.class)
public @interface WithUtilisateur {
}
