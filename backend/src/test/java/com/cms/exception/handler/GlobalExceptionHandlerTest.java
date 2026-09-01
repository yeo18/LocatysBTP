package com.cms.exception.handler;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cms.auth.dto.LoginRequest;
import com.cms.auth.service.AuthenticationService;
import com.cms.common.constants.ApiRoutes;
import com.cms.exception.custom.EmailDejaUtiliseException;
import com.cms.exception.custom.UnauthorizedException;
import com.cms.exception.custom.UtilisateurNonTrouveException;
import com.cms.permission.service.PermissionService;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du GlobalExceptionHandler : format d'erreur uniforme
 * (success, message, code, timestamp, errors) pour les erreurs metier,
 * de validation, de securite et inattendues.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private PermissionService permissionService;
    @MockBean
    private DroitsService droitsService;

    private CreateUtilisateurRequest requeteValide() {
        CreateUtilisateurRequest request = new CreateUtilisateurRequest();
        request.setNom("Kouassi");
        request.setPrenom("Mariam");
        request.setEmail("nouveau@example.com");
        request.setPassword("secret123");
        request.setProfilId(1L);
        return request;
    }

    @Test
    void emailDejaUtilise_retourne409() throws Exception {
        when(authenticationService.inscrire(any(CreateUtilisateurRequest.class)))
                .thenThrow(EmailDejaUtiliseException.pourEmail("nouveau@example.com"));

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requeteValide())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("EMAIL_DEJA_UTILISE"))
                .andExpect(jsonPath("$.message").value("L'email est deja utilise : nouveau@example.com"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void utilisateurInexistant_retourne404() throws Exception {
        when(authenticationService.inscrire(any(CreateUtilisateurRequest.class)))
                .thenThrow(UtilisateurNonTrouveException.pourId(999L));

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requeteValide())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UTILISATEUR_NON_TROUVE"));
    }

    @Test
    void validationDtoInvalide_retourne400AvecDetailsParChamp() throws Exception {
        CreateUtilisateurRequest invalide = requeteValide();
        invalide.setNom("");
        invalide.setEmail("pas-un-email");

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalide)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.nom").exists())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @WithUtilisateur
    void accesRefuse_sansPermission_retourne403() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of());

        mockMvc.perform(post(ApiRoutes.PERMISSIONS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Lire les taches","nomPermission":"TACHE_LIRE","module":"TACHE"}"""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void erreurInattendue_retourne500SansDetailInterne() throws Exception {
        when(authenticationService.inscrire(any(CreateUtilisateurRequest.class)))
                .thenThrow(new IllegalStateException("detail technique interne"));

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requeteValide())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ERREUR_INTERNE"))
                .andExpect(jsonPath("$.message").value("Une erreur est survenue"));
    }

    @Test
    void identifiantsIncorrects_retourne401() throws Exception {
        when(authenticationService.connexion(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Email ou mot de passe incorrect"));

        mockMvc.perform(post(ApiRoutes.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("a@a.com", "mauvais"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

}
