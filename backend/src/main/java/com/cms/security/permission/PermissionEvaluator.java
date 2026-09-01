package com.cms.security.permission;

/**
 * Contrat de verification des permissions fonctionnelles (RBAC Niveau 1).
 *
 * <p>LOOP 3.8 : contrat prepare uniquement. L'implementation complete (droits
 * effectifs = permissions du profil + ACCORDER - REFUSER, REFUSER prioritaire,
 * relus en base a chaque requete) sera fournie au LOOP 3.9 avec les
 * repositories {@code ProfilPermission} et {@code UtilisateurPermission}.
 *
 * <p>Le controle de perimetre (Niveau 2, restriction des donnees) est execute
 * dans les Services via {@code CurrentUserService}, jamais via annotations.
 */
public interface PermissionEvaluator {

    /**
     * Verifie le droit d'action de l'utilisateur courant sur un module.
     *
     * @param module module concerne (ex : {@code TACHE})
     * @param action action a realiser (ex : {@code VIEW}, {@code CREATE})
     * @return {@code true} si l'utilisateur courant possede le droit
     */
    boolean hasPermission(String module, String action);

    /**
     * Verifie le droit d'action via son code technique complet.
     *
     * @param nomPermission code technique (ex : {@code TACHE_VIEW})
     * @return {@code true} si l'utilisateur courant possede le droit
     */
    boolean hasPermission(String nomPermission);

}
