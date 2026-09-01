package com.cms.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.auth.dto.ChangerMotDePasseRequest;
import com.cms.auth.dto.LoginRequest;
import com.cms.auth.dto.MeResponse;
import com.cms.auth.dto.TokenResponse;
import com.cms.config.properties.ApplicationProperties;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.UnauthorizedException;
import com.cms.security.jwt.JwtService;
import com.cms.security.service.CurrentUserService;
import com.cms.security.service.DroitsService;
import com.cms.security.service.PrincipalUtilisateur;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.mapper.UtilisateurMapper;
import com.cms.utilisateur.repository.UtilisateurRepository;
import com.cms.utilisateur.service.UtilisateurService;

/**
 * Implementation du service d'authentification (LOOP 3.10).
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final ApplicationProperties properties;
    private final CurrentUserService currentUserService;
    private final DroitsService droitsService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     JwtService jwtService,
                                     UtilisateurService utilisateurService,
                                     UtilisateurRepository utilisateurRepository,
                                     UtilisateurMapper utilisateurMapper,
                                     ApplicationProperties properties,
                                     CurrentUserService currentUserService,
                                     DroitsService droitsService,
                                     PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.utilisateurService = utilisateurService;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMapper = utilisateurMapper;
        this.properties = properties;
        this.currentUserService = currentUserService;
        this.droitsService = droitsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UtilisateurResponse inscrire(CreateUtilisateurRequest request) {
        // L'inscription publique ignore toujours le profilId fourni par le client :
        // le profil systeme UTILISATEUR_STANDARD est determine cote serveur.
        request.setProfilId(null);
        return utilisateurService.creerUtilisateur(request);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse connexion(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException ex) {
            throw new UnauthorizedException("Compte desactive");
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }
        Utilisateur utilisateur = ((PrincipalUtilisateur) authentication.getPrincipal()).getUtilisateur();
        String token = jwtService.genererToken(utilisateur.getId());
        long expiresIn = properties.getSecurity().getJwt().getExpirationMs() / 1000;
        return new TokenResponse(token, "Bearer", expiresIn, utilisateurMapper.toResponse(utilisateur));
    }

    @Override
    @Transactional(readOnly = true)
    public MeResponse me() {
        Utilisateur utilisateur = currentUserService.getCurrentUtilisateur();
        Long utilisateurId = utilisateur.getId();
        MeResponse response = new MeResponse();
        response.setUtilisateur(utilisateurService.trouverParId(utilisateurId));
        response.setPermissions(droitsService.calculerDroits(utilisateurId));
        response.setPermissionsParChantier(droitsService.calculerDroitsParChantier(utilisateurId));
        return response;
    }

    @Override
    @Transactional
    public void changerMotDePasse(ChangerMotDePasseRequest request) {
        Utilisateur utilisateur = currentUserService.getCurrentUtilisateur();

        if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getPassword())) {
            throw new BadRequestException("L'ancien mot de passe est incorrect");
        }
        if (!request.getNouveauMotDePasse().equals(request.getConfirmation())) {
            throw new BadRequestException("La confirmation ne correspond pas au nouveau mot de passe");
        }

        utilisateur.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateur.setDateModification(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);
    }

}
