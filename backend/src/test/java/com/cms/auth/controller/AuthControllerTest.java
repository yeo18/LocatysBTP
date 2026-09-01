package com.cms.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cms.auth.dto.LoginRequest;
import com.cms.auth.dto.TokenResponse;
import com.cms.auth.service.AuthenticationService;
import com.cms.common.constants.ApiRoutes;
import com.cms.exception.custom.UnauthorizedException;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration des endpoints publics d'authentification.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void connexion_identifiantsCorrects_retourneToken200() throws Exception {
        UtilisateurResponse utilisateur = new UtilisateurResponse();
        utilisateur.setId(7L);
        utilisateur.setEmail("chef@example.com");
        when(authenticationService.connexion(any(LoginRequest.class)))
                .thenReturn(new TokenResponse("token.abc.123", "Bearer", 3600L, utilisateur));

        mockMvc.perform(post(ApiRoutes.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("chef@example.com", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token.abc.123"))
                .andExpect(jsonPath("$.data.type").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600L));
    }

    @Test
    void connexion_mauvaisMotDePasse_retourne401() throws Exception {
        when(authenticationService.connexion(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Email ou mot de passe incorrect"));

        mockMvc.perform(post(ApiRoutes.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("chef@example.com", "mauvais"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void inscription_retourne201() throws Exception {
        UtilisateurResponse utilisateur = new UtilisateurResponse();
        utilisateur.setId(8L);
        utilisateur.setEmail("nouveau@example.com");
        when(authenticationService.inscrire(any(CreateUtilisateurRequest.class)))
                .thenReturn(utilisateur);

        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Kouassi");
        request.setPrenom("Mariam");
        request.setEmail("nouveau@example.com");
        request.setPassword("secret123");
        request.setProfilId(1L);

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("nouveau@example.com"));
    }

    @Test
    void inscription_sansProfilId_accepteLaValidation() throws Exception {
        UtilisateurResponse utilisateur = new UtilisateurResponse();
        utilisateur.setId(8L);
        utilisateur.setEmail("nouveau@example.com");
        when(authenticationService.inscrire(any(CreateUtilisateurRequest.class)))
                .thenReturn(utilisateur);

        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Kouassi");
        request.setPrenom("Mariam");
        request.setEmail("nouveau@example.com");
        request.setPassword("secret123");

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("nouveau@example.com"));
    }

    @Test
    void inscription_avecProfilIdAdministrateur_accepteLaValidation() throws Exception {
        UtilisateurResponse utilisateur = new UtilisateurResponse();
        utilisateur.setId(8L);
        utilisateur.setEmail("nouveau@example.com");
        when(authenticationService.inscrire(any(CreateUtilisateurRequest.class)))
                .thenReturn(utilisateur);

        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Kouassi");
        request.setPrenom("Mariam");
        request.setEmail("nouveau@example.com");
        request.setPassword("secret123");
        request.setProfilId(2L);

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("nouveau@example.com"));
    }

}
