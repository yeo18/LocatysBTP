package com.cms.equipe.controller;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cms.common.constants.ApiRoutes;
import com.cms.equipe.dto.CreateEquipeRequest;
import com.cms.equipe.dto.CreateMembreEquipeRequest;
import com.cms.equipe.dto.EquipeResponse;
import com.cms.equipe.dto.MembreEquipeResponse;
import com.cms.equipe.dto.UpdateMembreEquipeRequest;
import com.cms.equipe.entity.enums.RoleDansEquipe;
import com.cms.equipe.service.AffectationEquipeChantierService;
import com.cms.equipe.service.EquipeService;
import com.cms.equipe.service.MembreEquipeService;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du Controller Equipe : RBAC (autorise / refuse) et
 * fonctionnel (creation equipe, ajout membre, modification du role).
 */
@SpringBootTest
@AutoConfigureMockMvc
class EquipeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EquipeService equipeService;
    @MockBean
    private MembreEquipeService membreEquipeService;
    @MockBean
    private AffectationEquipeChantierService affectationEquipeChantierService;
    @MockBean
    private DroitsService droitsService;

    // ------------------------------------------------------------------
    // Cas 1 : creation d'une equipe par un utilisateur autorise (ADMINISTRATEUR)
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationEquipeAutorisee_retourne201() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("EQUIPE_CREER"));

        EquipeResponse response = new EquipeResponse();
        response.setId(1L);
        response.setNom("Equipe Maçonnerie");
        when(equipeService.creer(any(CreateEquipeRequest.class))).thenReturn(response);

        CreateEquipeRequest request = new CreateEquipeRequest();
        request.setNom("Equipe Maçonnerie");
        request.setDescription("Equipe travaux");

        mockMvc.perform(post(ApiRoutes.EQUIPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.nom").value("Equipe Maçonnerie"));
    }

    // ------------------------------------------------------------------
    // Cas 2 : ajout d'un membre dans une equipe
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void ajoutMembreAutorise_retourne201() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("EQUIPE_MODIFIER"));

        MembreEquipeResponse response = new MembreEquipeResponse();
        response.setId(10L);
        response.setUtilisateurId(5L);
        response.setEquipeId(1L);
        response.setRoleDansEquipe(RoleDansEquipe.OUVRIER);
        response.setDateIntegration(LocalDate.of(2026, 8, 6));
        when(membreEquipeService.integrer(eq(1L), eq(5L), eq(RoleDansEquipe.OUVRIER),
                any(LocalDate.class))).thenReturn(response);

        CreateMembreEquipeRequest request = new CreateMembreEquipeRequest();
        request.setEquipeId(1L);
        request.setUtilisateurId(5L);
        request.setRoleDansEquipe(RoleDansEquipe.OUVRIER);
        request.setDateIntegration(LocalDate.of(2026, 8, 6));

        mockMvc.perform(post(ApiRoutes.EQUIPES + "/membres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.roleDansEquipe").value("OUVRIER"));
    }

    // ------------------------------------------------------------------
    // Cas 3 : modification du role CHEF/OUVRIER
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void modificationRoleAutorisee_retourne200() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("EQUIPE_MODIFIER"));

        MembreEquipeResponse response = new MembreEquipeResponse();
        response.setId(10L);
        response.setUtilisateurId(5L);
        response.setEquipeId(1L);
        response.setRoleDansEquipe(RoleDansEquipe.CHEF);
        when(membreEquipeService.changerRole(eq(10L), eq(RoleDansEquipe.CHEF))).thenReturn(response);

        UpdateMembreEquipeRequest request = new UpdateMembreEquipeRequest();
        request.setRoleDansEquipe(RoleDansEquipe.CHEF);
        request.setDateIntegration(LocalDate.of(2026, 8, 6));

        mockMvc.perform(put(ApiRoutes.EQUIPES + "/membres/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleDansEquipe").value("CHEF"));
    }

    // ------------------------------------------------------------------
    // Cas 4 : utilisateur sans permission -> 403
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationEquipeRefusee_sansPermission_retourne403() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of());

        CreateEquipeRequest request = new CreateEquipeRequest();
        request.setNom("Equipe Maçonnerie");

        mockMvc.perform(post(ApiRoutes.EQUIPES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // Securite de base : sans token -> 401
    // ------------------------------------------------------------------

    @Test
    void liste_sansToken_retourne401() throws Exception {
        mockMvc.perform(get(ApiRoutes.EQUIPES))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUtilisateur
    void retraitMembreAutorise_retourne200() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(Set.of("EQUIPE_SUPPRIMER"));

        mockMvc.perform(delete(ApiRoutes.EQUIPES + "/membres/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

}
