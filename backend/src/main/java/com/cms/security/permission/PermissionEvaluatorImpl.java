package com.cms.security.permission;

import org.springframework.stereotype.Component;

import com.cms.security.service.CurrentUserService;
import com.cms.security.service.DroitsService;

/**
 * Implementation du controleur de permissions fonctionnelles (RBAC Niveau 1).
 *
 * <p>Pour chaque verification :
 * <ol>
 *   <li>recupere l'utilisateur connecte (SecurityContext) ;</li>
 *   <li>calcule ses droits effectifs depuis PostgreSQL ({@link DroitsService}) ;</li>
 *   <li>autorise ou refuse selon la presence du code demande.</li>
 * </ol>
 *
 * <p>Aucune permission n'est stockee dans le JWT ni codee en dur : les droits
 * sont relus en base a chaque requete (modification effective immediatement).
 */
@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {

    private final CurrentUserService currentUserService;
    private final DroitsService droitsService;

    public PermissionEvaluatorImpl(CurrentUserService currentUserService, DroitsService droitsService) {
        this.currentUserService = currentUserService;
        this.droitsService = droitsService;
    }

    @Override
    public boolean hasPermission(String module, String action) {
        String nomPermission = module.toUpperCase() + "_" + action.toUpperCase();
        return hasPermission(nomPermission);
    }

    @Override
    public boolean hasPermission(String nomPermission) {
        Long userId = currentUserService.getCurrentUserId();
        return droitsService.calculerDroits(userId).contains(nomPermission);
    }

}
