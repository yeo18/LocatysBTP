package com.cms.profil.controller;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cms.common.constants.ApiRoutes;
import com.cms.profil.dto.CreateProfilRequest;
import com.cms.profil.dto.ProfilResponse;
import com.cms.profil.service.ProfilService;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du Controller Profil (RBAC).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProfilControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfilService profilService;
    @MockBean
    private DroitsService droitsService;

    @Test
    @WithUtilisateur
    void creationAutorisee_retourne201() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("PROFIL_CREER"));
        ProfilResponse response = new ProfilResponse();
        response.setId(3L);
        response.setNom("RESPONSABLE_CHANTIER");
        when(profilService.creerProfil(any(CreateProfilRequest.class))).thenReturn(response);

        CreateProfilRequest request = new CreateProfilRequest();
        request.setNom("RESPONSABLE_CHANTIER");
        request.setDescription("Responsable de chantier");

        mockMvc.perform(post(ApiRoutes.PROFILS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithUtilisateur
    void creationRefusee_sansPermission_retourne403() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of());

        CreateProfilRequest request = new CreateProfilRequest();
        request.setNom("RESPONSABLE_CHANTIER");

        mockMvc.perform(post(ApiRoutes.PROFILS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

}
