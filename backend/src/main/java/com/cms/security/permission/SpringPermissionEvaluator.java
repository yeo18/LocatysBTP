package com.cms.security.permission;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.cms.security.service.CurrentUserService;
import com.cms.security.service.DroitsService;

/**
 * Adaptateur exposant le controle RBAC du CMS a Spring Security, afin de
 * rendre utilisable l'expression :
 *
 * <pre>
 * &#64;PreAuthorize("hasPermission('TACHE','MODIFIER')")
 * &#64;PreAuthorize("hasPermission(#id, 'CHANTIER', 'MODIFIER')")  // scope chantier
 * </pre>
 *
 * <p>L'expression a 2 arguments {@code hasPermission(module, action)} est
 * traduite par Spring en {@code hasPermission(authentication, 'TACHE',
 * 'MODIFIER')} : le premier argument est traite comme le module, le second
 * comme l'action. Les droits effectifs sont calcules en base a chaque
 * verification.
 *
 * <p>L'expression a 3 arguments {@code hasPermission(#id, 'CHANTIER',
 * 'MODIFIER')} est traduite en {@code hasPermission(authentication, #id,
 * 'CHANTIER', 'MODIFIER')} : elle verifie la permission dans le perimetre
 * (scope) du chantier identifie par {@code #id}, via
 * {@link DroitsService#calculerDroitsSurChantier}. C'est le mecanisme qui
 * permet d'etre « chef du chantier A mais pas du B ».
 */
@Component
public class SpringPermissionEvaluator implements PermissionEvaluator {

    private final com.cms.security.permission.PermissionEvaluator permissionEvaluator;
    private final CurrentUserService currentUserService;
    private final DroitsService droitsService;

    public SpringPermissionEvaluator(com.cms.security.permission.PermissionEvaluator permissionEvaluator,
                                     CurrentUserService currentUserService,
                                     DroitsService droitsService) {
        this.permissionEvaluator = permissionEvaluator;
        this.currentUserService = currentUserService;
        this.droitsService = droitsService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return permissionEvaluator.hasPermission(String.valueOf(targetDomainObject), String.valueOf(permission));
    }

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        // Permission scopée à un chantier : hasPermission(#id, 'CHANTIER', 'MODIFIER').
        if (targetId == null || targetType == null || permission == null) {
            return false;
        }
        Long chantierId;
        try {
            chantierId = Long.valueOf(String.valueOf(targetId));
        } catch (NumberFormatException ex) {
            return false;
        }
        String nomPermission = (targetType.trim() + "_" + permission).toUpperCase();
        Long userId = currentUserService.getCurrentUserId();
        return droitsService.calculerDroitsSurChantier(userId, chantierId).contains(nomPermission);
    }

}