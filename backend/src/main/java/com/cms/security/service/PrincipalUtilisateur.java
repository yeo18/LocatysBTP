package com.cms.security.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.cms.utilisateur.entity.Utilisateur;

/**
 * Principal de securite : enveloppe l'entite {@link Utilisateur} pour Spring
 * Security.
 *
 * <p>Les autorites sont volontairement vides au LOOP 3.8 : les droits
 * effectifs (RBAC dynamique) sont charges depuis la base a chaque requete au
 * LOOP 3.9 et ne transitent jamais par le JWT.
 */
public class PrincipalUtilisateur implements UserDetails {

    private final Utilisateur utilisateur;

    public PrincipalUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    /**
     * Retourne l'entite utilisateur courante (usage interne : services,
     * perimetre chantier Niveau 2).
     */
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return utilisateur.getPassword();
    }

    @Override
    public String getUsername() {
        return utilisateur.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
