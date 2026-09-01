package com.cms.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.cms.profil.entity.Profil;
import com.cms.security.service.PrincipalUtilisateur;
import com.cms.utilisateur.entity.Utilisateur;

/**
 * Fabrique du contexte de securite {@code @WithUtilisateur}.
 */
public class WithUtilisateurSecurityContextFactory implements WithSecurityContextFactory<WithUtilisateur> {

    @Override
    public SecurityContext createSecurityContext(WithUtilisateur annotation) {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom("ADMINISTRATEUR");

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setNom("Konate");
        utilisateur.setPrenom("Awa");
        utilisateur.setEmail("awa.konate@example.com");
        utilisateur.setProfil(profil);

        PrincipalUtilisateur principal = new PrincipalUtilisateur(utilisateur);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
