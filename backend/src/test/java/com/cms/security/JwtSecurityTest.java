package com.cms.security;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cms.config.properties.ApplicationProperties;
import com.cms.profil.entity.Profil;
import com.cms.security.filter.JwtAuthenticationFilter;
import com.cms.security.jwt.JwtService;
import com.cms.security.service.CustomUserDetailsService;
import com.cms.security.service.PrincipalUtilisateur;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Tests du socle d'authentification JWT (LOOP 3.8) sans Controller :
 * <ol>
 *   <li>succes : token emis et valide, requete suivante authentifiee ;</li>
 *   <li>mauvais mot de passe : authentification refusee (BadCredentials) ;</li>
 *   <li>token invalide : aucune authentification positionnee (rejet 401).
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
class JwtSecurityTest {

    private static final String SECRET = "cle-test-cms-2026-0123456789abcdefghijklmnopqrstu";
    private static final String EMAIL = "chef.chantier@example.com";
    private static final String MOT_DE_PASSE = "secret123";

    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private Utilisateur utilisateurActif;

    @BeforeEach
    void setUp() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getSecurity().getJwt().setSecret(SECRET);
        properties.getSecurity().getJwt().setExpirationMs(3600000L);
        jwtService = new JwtService(properties);
        passwordEncoder = new BCryptPasswordEncoder();
        userDetailsService = new CustomUserDetailsService(utilisateurRepository);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        utilisateurActif = new Utilisateur();
        utilisateurActif.setId(7L);
        utilisateurActif.setNom("Konate");
        utilisateurActif.setPrenom("Awa");
        utilisateurActif.setEmail(EMAIL);
        utilisateurActif.setPassword(passwordEncoder.encode(MOT_DE_PASSE));
        Profil profil = new Profil();
        profil.setId(1L);
        profil.setNom("ADMINISTRATEUR");
        utilisateurActif.setProfil(profil);
    }

    private DaoAuthenticationProvider creerProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Test
    void succesConnexion_tokenValide_puisRequeteAuthentifiee() throws Exception {
        when(utilisateurRepository.findById(7L)).thenReturn(Optional.of(utilisateurActif));

        // Etape 1 : emission du token (etape "login").
        String token = jwtService.genererToken(utilisateurActif.getId());
        assertThat(jwtService.estValide(token)).isTrue();
        assertThat(jwtService.extraireUserId(token)).isEqualTo(7L);

        // Etape 2 : requete suivante avec le Bearer token -> contexte authentifie.
        MockHttpServletRequest requete = new MockHttpServletRequest(HttpMethod.GET.name(), "/api/v1/chantiers");
        requete.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse reponse = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(requete, reponse, new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getPrincipal()).isInstanceOf(PrincipalUtilisateur.class);
        assertThat(((PrincipalUtilisateur) auth.getPrincipal()).getUtilisateur().getId()).isEqualTo(7L);
        SecurityContextHolder.clearContext();
    }

    @Test
    void connexion_mauvaisMotDePasse_authentificationRefusee() {
        when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateurActif));

        assertThatThrownBy(() -> creerProvider().authenticate(
                new UsernamePasswordAuthenticationToken(EMAIL, "mauvais-mdp")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void requete_tokenInvalide_aucuneAuthentificationPositionnee() throws Exception {
        MockHttpServletRequest requete = new MockHttpServletRequest(HttpMethod.GET.name(), "/api/v1/chantiers");
        requete.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token.invalide.xxx");
        MockHttpServletResponse reponse = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(requete, reponse, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        SecurityContextHolder.clearContext();
    }

    @Test
    void requete_sansHeaderAuthorization_aucuneAuthentification() throws Exception {
        MockHttpServletRequest requete = new MockHttpServletRequest(HttpMethod.GET.name(), "/api/v1/chantiers");
        MockHttpServletResponse reponse = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(requete, reponse, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        SecurityContextHolder.clearContext();
    }

}
