package com.cms.utilisateur.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cms.common.constants.ApiRoutes;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.cms.utilisateur.dto.UpdateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.cms.utilisateur.service.UtilisateurService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du Controller Utilisateur : modification de son propre
 * compte (authentifié) et authentification (token absent -> 401).
 *
 * <p>La création et la modification des identifiants d'autrui ont été retirées :
 * chaque utilisateur ne gère que son propre compte.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UtilisateurService utilisateurService;
    @MockBean
    private DroitsService droitsService;

    private UpdateUtilisateurRequest creerRequeteModification() {
        UpdateUtilisateurRequest request = new UpdateUtilisateurRequest();
        request.setNom("Konate");
        request.setPrenom("Awa");
        request.setEmail("awa@example.com");
        request.setProfilId(1L);
        return request;
    }

    @Test
    @WithUtilisateur
    void modificationPropreCompte_retourne200() throws Exception {
        UtilisateurResponse response = new UtilisateurResponse();
        response.setId(5L);
        response.setEmail("awa@example.com");
        when(utilisateurService.modifierUtilisateur(eq(5L), any(UpdateUtilisateurRequest.class))).thenReturn(response);

        mockMvc.perform(put(ApiRoutes.USERS + "/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequeteModification())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5L));
    }

    @Test
    void liste_sansToken_retourne401() throws Exception {
        mockMvc.perform(get(ApiRoutes.USERS))
                .andExpect(status().isUnauthorized());
    }

}
