package com.cms.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controle de permission fonctionnelle (RBAC Niveau 1) sur une methode de
 * Controller.
 *
 * <p>LOOP 3.8 : annotation preparee uniquement. La liaison effective avec
 * {@code PermissionEvaluator} (interception des methodes annotees) sera
 * activee au LOOP 3.9/3.10 avec le RBAC dynamique. Le token JWT ne porte
 * jamais les permissions : la verification se fait en base a chaque requete.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * Module concerne (ex : {@code TACHE}, {@code CHANTIER}).
     */
    String module();

    /**
     * Action a realiser (ex : {@code VIEW}, {@code CREATE}, {@code VALIDATE}).
     */
    String action();

}
