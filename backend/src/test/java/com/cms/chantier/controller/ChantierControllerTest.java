package com.cms.chantier.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cms.chantier.dto.ChantierResponse;
import com.cms.chantier.dto.ChantierResumeResponse;
import com.cms.chantier.dto.CreateChantierRequest;
import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.chantier.service.ChantierService;
import com.cms.common.constants.ApiRoutes;
import com.cms.security.WithUtilisateur;
import com.cms.security.service.DroitsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration du Controller Chantier.
 *
 * <p>Cas obligatoires : creation par un utilisateur autorise (201), refus
 * sans permission (403), consultation (200), recherche paginee, annulation
 * (suppression logique), acces sans token (401).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ChantierControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChantierService chantierService;
    @MockBean
    private DroitsService droitsService;

    private CreateChantierRequest creerRequest() {
        CreateChantierRequest request = new CreateChantierRequest();
        request.setNom("Residence Les Alizes");
        request.setDescription("Construction de 12 villas");
        request.setAdresseSaisie("Dakar, Ouakam");
        request.setStatut(ChantierStatut.PREVU);
        request.setDateDebut(LocalDate.of(2026, 9, 1));
        request.setDateFin(LocalDate.of(2027, 3, 31));
        return request;
    }

    private ChantierResponse chantierResponse() {
        ChantierResponse response = new ChantierResponse();
        response.setId(1L);
        response.setNom("Residence Les Alizes");
        response.setStatut(ChantierStatut.PREVU);
        response.setProgression(0);
        return response;
    }

    // ------------------------------------------------------------------
    // Cas 1 : utilisateur autorise cree un chantier -> 201
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationChantierAutorisee_retourne201() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(java.util.Set.of("CHANTIER_CREER"));
        when(chantierService.creer(any(CreateChantierRequest.class)))
                .thenReturn(chantierResponse());

        mockMvc.perform(post(ApiRoutes.CHANTIERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.nom").value("Residence Les Alizes"));
    }

    // ------------------------------------------------------------------
    // Cas 2 : utilisateur sans permission cree un chantier -> 403
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void creationChantierRefusee_sansPermission_retourne403() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(java.util.Set.of());

        mockMvc.perform(post(ApiRoutes.CHANTIERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ------------------------------------------------------------------
    // Cas 3 : consultation d'un chantier -> 200
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void consultationChantier_retourne200() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(java.util.Set.of("CHANTIER_LIRE"));
        when(droitsService.calculerDroitsSurChantier(anyLong(), anyLong())).thenReturn(java.util.Set.of("CHANTIER_LIRE"));
        when(chantierService.trouverParId(1L)).thenReturn(chantierResponse());

        mockMvc.perform(get(ApiRoutes.CHANTIERS + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nom").value("Residence Les Alizes"));
    }

    // ------------------------------------------------------------------
    // Cas 4 : recherche paginee -> 200
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void rechercheChantiers_retournePage() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(java.util.Set.of("CHANTIER_LIRE"));

        ChantierResumeResponse resume = new ChantierResumeResponse();
        resume.setId(1L);
        resume.setNom("Residence Les Alizes");
        resume.setStatut(ChantierStatut.PREVU);
        resume.setProgression(0);
        when(chantierService.rechercher(any(), isNull()))
                .thenReturn(new PageImpl<>(List.of(resume)));

        mockMvc.perform(get(ApiRoutes.CHANTIERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    // ------------------------------------------------------------------
    // Cas 5 : modification d'un chantier -> 200
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void modificationChantier_retourne200() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(java.util.Set.of("CHANTIER_MODIFIER"));
        when(droitsService.calculerDroitsSurChantier(anyLong(), anyLong())).thenReturn(java.util.Set.of("CHANTIER_MODIFIER"));
        when(chantierService.modifier(anyLong(), any())).thenReturn(chantierResponse());

        mockMvc.perform(put(ApiRoutes.CHANTIERS + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creerRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    // ------------------------------------------------------------------
    // Cas 6 : annulation (suppression logique) -> 200
    // ------------------------------------------------------------------

    @Test
    @WithUtilisateur
    void annulationChantier_retourne200() throws Exception {
        when(droitsService.calculerDroits(anyLong())).thenReturn(java.util.Set.of("CHANTIER_SUPPRIMER"));
        when(droitsService.calculerDroitsSurChantier(anyLong(), anyLong())).thenReturn(java.util.Set.of("CHANTIER_SUPPRIMER"));
        ChantierResponse annule = chantierResponse();
        annule.setStatut(ChantierStatut.ANNULE);
        when(chantierService.annuler(1L)).thenReturn(annule);

        mockMvc.perform(delete(ApiRoutes.CHANTIERS + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statut").value("ANNULE"));
    }

    // ------------------------------------------------------------------
    // Securite de base : sans token -> 401
    // ------------------------------------------------------------------

    @Test
    void liste_sansToken_retourne401() throws Exception {
        mockMvc.perform(get(ApiRoutes.CHANTIERS))
                .andExpect(status().isUnauthorized());
    }

}
