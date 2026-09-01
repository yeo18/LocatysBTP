package com.cms.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cms.auth.dto.LoginRequest;
import com.cms.auth.dto.TokenResponse;
import com.cms.config.properties.ApplicationProperties;
import com.cms.exception.custom.UnauthorizedException;
import com.cms.profil.entity.Profil;
import com.cms.security.jwt.JwtService;
import com.cms.security.service.DroitsService;
import com.cms.security.service.CurrentUserService;
import com.cms.security.service.PrincipalUtilisateur;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.mapper.UtilisateurMapper;
import com.cms.utilisateur.repository.UtilisateurRepository;
import com.cms.utilisateur.service.UtilisateurService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service d'authentification (LOOP 3.10).
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UtilisateurService utilisateurService;
    @Mock
    private UtilisateurMapper utilisateurMapper;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private DroitsService droitsService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthenticationServiceImpl service;

    @BeforeEach
    void setUp() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getSecurity().getJwt().setSecret("cle-test-cms-2026-0123456789abcdefghijklmnopqrstu");
        properties.getSecurity().getJwt().setExpirationMs(3600000L);
        service = new AuthenticationServiceImpl(authenticationManager, jwtService,
                utilisateurService, utilisateurRepository, utilisateurMapper, properties,
                currentUserService, droitsService, passwordEncoder);
    }

    private Utilisateur utilisateurActif() {
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom("ADMINISTRATEUR");

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(7L);
        utilisateur.setNom("Konate");
        utilisateur.setPrenom("Awa");
        utilisateur.setEmail("chef@example.com");
        utilisateur.setProfil(profil);
        return utilisateur;
    }

    @Test
    void connexion_identifiantsCorrects_retourneToken() {
        Utilisateur utilisateur = utilisateurActif();
        PrincipalUtilisateur principal = new PrincipalUtilisateur(utilisateur);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.genererToken(7L)).thenReturn("token.abc.123");

        UtilisateurResponse reponse = new UtilisateurResponse();
        reponse.setId(7L);
        reponse.setEmail("chef@example.com");
        when(utilisateurMapper.toResponse(utilisateur)).thenReturn(reponse);

        TokenResponse resultat = service.connexion(new LoginRequest("chef@example.com", "secret123"));

        assertThat(resultat.getToken()).isEqualTo("token.abc.123");
        assertThat(resultat.getType()).isEqualTo("Bearer");
        assertThat(resultat.getExpiresIn()).isEqualTo(3600L);
        assertThat(resultat.getUtilisateur().getEmail()).isEqualTo("chef@example.com");
    }

    @Test
    void connexion_mauvaisMotDePasse_leveUnauthorized() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("mauvais mot de passe"));

        assertThatThrownBy(() -> service.connexion(new LoginRequest("chef@example.com", "mauvais")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void inscription_delegueAuServiceUtilisateur() {
        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Konate");
        request.setPrenom("Awa");
        request.setEmail("chef@example.com");
        request.setPassword("secret123");
        request.setProfilId(1L);

        UtilisateurResponse reponse = new UtilisateurResponse();
        reponse.setId(9L);
        reponse.setEmail("chef@example.com");
        when(utilisateurService.creerUtilisateur(any())).thenReturn(reponse);

        assertThat(service.inscrire(request)).isSameAs(reponse);
    }

    @Test
    void inscription_sansProfilId_ignoreEtForceProfilStandard() {
        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Konate");
        request.setPrenom("Awa");
        request.setEmail("chef@example.com");
        request.setPassword("secret123");

        UtilisateurResponse reponse = new UtilisateurResponse();
        reponse.setId(9L);
        when(utilisateurService.creerUtilisateur(any())).thenReturn(reponse);

        service.inscrire(request);

        ArgumentCaptor<CreateUtilisateurRequest> captor = ArgumentCaptor.forClass(CreateUtilisateurRequest.class);
        verify(utilisateurService).creerUtilisateur(captor.capture());
        assertThat(captor.getValue().getProfilId()).isNull();
    }

    @Test
    void inscription_avecProfilIdAdministrateur_ignoreEtForceProfilStandard() {
        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Konate");
        request.setPrenom("Awa");
        request.setEmail("chef@example.com");
        request.setPassword("secret123");
        request.setProfilId(2L);

        UtilisateurResponse reponse = new UtilisateurResponse();
        reponse.setId(9L);
        when(utilisateurService.creerUtilisateur(any())).thenReturn(reponse);

        service.inscrire(request);

        ArgumentCaptor<CreateUtilisateurRequest> captor = ArgumentCaptor.forClass(CreateUtilisateurRequest.class);
        verify(utilisateurService).creerUtilisateur(captor.capture());
        assertThat(captor.getValue().getProfilId()).isNull();
    }

    @Test
    void inscription_avecProfilIdAutre_ignoreEtForceProfilStandard() {
        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Konate");
        request.setPrenom("Awa");
        request.setEmail("chef@example.com");
        request.setPassword("secret123");
        request.setProfilId(7L);

        UtilisateurResponse reponse = new UtilisateurResponse();
        reponse.setId(9L);
        when(utilisateurService.creerUtilisateur(any())).thenReturn(reponse);

        service.inscrire(request);

        ArgumentCaptor<CreateUtilisateurRequest> captor = ArgumentCaptor.forClass(CreateUtilisateurRequest.class);
        verify(utilisateurService).creerUtilisateur(captor.capture());
        assertThat(captor.getValue().getProfilId()).isNull();
    }

    @Test
    void inscription_profilIdClientJamaisTransmisAuServiceUtilisateur() {
        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Konate");
        request.setPrenom("Awa");
        request.setEmail("chef@example.com");
        request.setPassword("secret123");
        request.setProfilId(999L);

        UtilisateurResponse reponse = new UtilisateurResponse();
        reponse.setId(9L);
        when(utilisateurService.creerUtilisateur(any())).thenReturn(reponse);

        service.inscrire(request);

        ArgumentCaptor<CreateUtilisateurRequest> captor = ArgumentCaptor.forClass(CreateUtilisateurRequest.class);
        verify(utilisateurService).creerUtilisateur(captor.capture());
        assertThat(captor.getValue().getProfilId()).isNull();
    }

}
