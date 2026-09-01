package com.cms.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Charge l'utilisateur depuis la base pour Spring Security.
 *
 * <p>Utilise par le {@code DaoAuthenticationProvider} (verification du mot de
 * passe lors de la connexion) et par le {@code JwtAuthenticationFilter}
 * (rechargement de l'utilisateur a chaque requete).
 *
 * <p>Le calcul des droits effectifs (profil + ACCORDER - REFUSER, REFUSER
 * prioritaire) sera ajoute au LOOP 3.9 (RBAC dynamique) dans
 * {@code getAuthorities()}.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable avec l'email : " + email));
        logger.debug("Chargement utilisateur pour l'authentification : email={}", email);
        return new PrincipalUtilisateur(utilisateur);
    }

    /**
     * Charge l'utilisateur par identifiant (utilise par le filtre JWT).
     *
     * @param id identifiant de l'utilisateur
     * @return principal de securite
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable avec l'identifiant : " + id));
        logger.debug("Chargement utilisateur via JWT : id={}", id);
        return new PrincipalUtilisateur(utilisateur);
    }

}
