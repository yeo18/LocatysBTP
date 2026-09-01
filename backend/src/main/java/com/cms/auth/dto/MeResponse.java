package com.cms.auth.dto;

import java.util.Map;
import java.util.Set;

import com.cms.utilisateur.dto.UtilisateurResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Reponse du profil courant ({@code /auth/me}) : utilisateur authentifie +
 * codes de permissions effectifs (RBAC dynamique replique de DroitsService).
 *
 * <p>{@link #getPermissions()} contient les droits globaux (profil + exceptions
 * sans chantier). {@link #getPermissionsParChantier()} contient, pour chaque
 * chantier, les droits effectifs calculés AVEC les exceptions scopées à CE
 * chantier (chef du chantier A ≠ chef du chantier B).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {

    private UtilisateurResponse utilisateur;
    private Set<String> permissions;
    private Map<Long, Set<String>> permissionsParChantier;
}