package com.cms.tache.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cms.equipe.entity.Equipe;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.tache.dto.AffectationTacheResponse;
import com.cms.tache.dto.CreateAffectationTacheRequest;
import com.cms.tache.dto.UpdateAffectationTacheRequest;
import com.cms.tache.entity.AffectationTacheEquipe;
import com.cms.tache.entity.AffectationTacheUtilisateur;
import com.cms.tache.entity.enums.AffectationTacheRole;
import com.cms.tache.entity.Tache;
import com.cms.tache.mapper.AffectationTacheMapper;
import com.cms.tache.mapper.AffectationTacheMapperImpl;
import com.cms.tache.repository.AffectationTacheEquipeRepository;
import com.cms.tache.repository.AffectationTacheUtilisateurRepository;
import com.cms.tache.repository.TacheRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service d'affectation de taches (LOOP 3.15).
 *
 * <p>Depuis le découpage des relations binaires, une tâche est affectée à un
 * utilisateur ({@code affectation_tache_utilisateur}) OU à une équipe
 * ({@code affectation_tache_equipe}) : les deux repos sont mockés séparément.
 */
@ExtendWith(MockitoExtension.class)
class AffectationTacheServiceImplTest {

    @Mock
    private AffectationTacheUtilisateurRepository affectationUtilisateurRepository;
    @Mock
    private AffectationTacheEquipeRepository affectationEquipeRepository;
    @Mock
    private TacheRepository tacheRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private EquipeRepository equipeRepository;

    private AffectationTacheMapper mapper = new AffectationTacheMapperImpl();
    private AffectationTacheServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AffectationTacheServiceImpl(affectationUtilisateurRepository,
                affectationEquipeRepository, tacheRepository, utilisateurRepository,
                equipeRepository, mapper);
    }

    private Tache tache() {
        Tache tache = new Tache();
        tache.setId(1L);
        tache.setTitre("Fondations");
        return tache;
    }

    private Utilisateur utilisateur() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(10L);
        utilisateur.setNom("Duval");
        return utilisateur;
    }

    private CreateAffectationTacheRequest requestUtilisateur() {
        CreateAffectationTacheRequest request = new CreateAffectationTacheRequest();
        request.setTacheId(1L);
        request.setUtilisateurId(10L);
        request.setRole(AffectationTacheRole.REALISATEUR);
        request.setDateAffectation(LocalDate.of(2026, 8, 10));
        return request;
    }

    // ------------------------------------------------------------------
    // assignTache
    // ------------------------------------------------------------------

    @Test
    void assignTache_aUtilisateur_fonctionne() {
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache()));
        Utilisateur utilisateur = utilisateur();
        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(utilisateur));
        when(affectationUtilisateurRepository.findByTacheId(1L)).thenReturn(List.of());

        AffectationTacheUtilisateur sauv = new AffectationTacheUtilisateur();
        sauv.setTache(tache());
        sauv.setUtilisateur(utilisateur);
        sauv.setRole(AffectationTacheRole.REALISATEUR);
        when(affectationUtilisateurRepository.save(any(AffectationTacheUtilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AffectationTacheResponse response = service.assignTache(requestUtilisateur());

        assertThat(response.getRole()).isEqualTo(AffectationTacheRole.REALISATEUR);
        assertThat(response.getTacheId()).isEqualTo(1L);
        assertThat(response.getUtilisateurId()).isEqualTo(10L);
        assertThat(response.getUtilisateurNom()).isEqualTo("Duval");
    }

    @Test
    void assignTache_aEquipe_fonctionne() {
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache()));
        Equipe equipe = new Equipe();
        equipe.setId(2L);
        equipe.setNom("Equipe Gros Oeuvre");
        when(equipeRepository.findById(2L)).thenReturn(Optional.of(equipe));
        when(affectationEquipeRepository.findByTacheId(1L)).thenReturn(List.of());

        AffectationTacheEquipe sauv = new AffectationTacheEquipe();
        sauv.setTache(tache());
        sauv.setEquipe(equipe);
        sauv.setRole(AffectationTacheRole.REALISATEUR);
        when(affectationEquipeRepository.save(any(AffectationTacheEquipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateAffectationTacheRequest request = requestUtilisateur();
        request.setUtilisateurId(null);
        request.setEquipeId(2L);

        AffectationTacheResponse response = service.assignTache(request);

        assertThat(response.getEquipeId()).isEqualTo(2L);
        assertThat(response.getEquipeNom()).isEqualTo("Equipe Gros Oeuvre");
        assertThat(response.getUtilisateurId()).isNull();
    }

    @Test
    void assignTache_utilisateurPrioritaireSurEquipe() {
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache()));
        Utilisateur utilisateur = utilisateur();
        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(utilisateur));
        when(affectationUtilisateurRepository.findByTacheId(1L)).thenReturn(List.of());

        AffectationTacheUtilisateur sauv = new AffectationTacheUtilisateur();
        sauv.setTache(tache());
        sauv.setUtilisateur(utilisateur);
        sauv.setRole(AffectationTacheRole.REALISATEUR);
        when(affectationUtilisateurRepository.save(any(AffectationTacheUtilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateAffectationTacheRequest request = requestUtilisateur();
        request.setEquipeId(2L); // ignoré : la branche utilisateur prime

        AffectationTacheResponse response = service.assignTache(request);

        assertThat(response.getUtilisateurId()).isEqualTo(10L);
        assertThat(response.getEquipeId()).isNull();
    }

    @Test
    void assignTache_sansCible_leveException() {
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache()));
        CreateAffectationTacheRequest request = requestUtilisateur();
        request.setUtilisateurId(null);
        request.setEquipeId(null);

        assertThatThrownBy(() -> service.assignTache(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void assignTache_tacheInexistante_leveException() {
        when(tacheRepository.findById(999L)).thenReturn(Optional.empty());
        CreateAffectationTacheRequest request = requestUtilisateur();
        request.setTacheId(999L);

        assertThatThrownBy(() -> service.assignTache(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignTache_doublonUtilisateur_leveException() {
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache()));

        AffectationTacheUtilisateur existante = new AffectationTacheUtilisateur();
        existante.setTache(tache());
        existante.setUtilisateur(utilisateur());
        existante.setRole(AffectationTacheRole.REALISATEUR);
        when(affectationUtilisateurRepository.findByTacheId(1L)).thenReturn(List.of(existante));

        assertThatThrownBy(() -> service.assignTache(requestUtilisateur()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // ------------------------------------------------------------------
    // updateAffectation
    // ------------------------------------------------------------------

    @Test
    void updateAffectation_fonctionne() {
        AffectationTacheUtilisateur affectation = new AffectationTacheUtilisateur();
        affectation.setId(1L);
        affectation.setTache(tache());
        affectation.setRole(AffectationTacheRole.REALISATEUR);
        when(affectationUtilisateurRepository.findById(1L)).thenReturn(Optional.of(affectation));
        when(affectationUtilisateurRepository.findByTacheId(1L)).thenReturn(List.of(affectation));
        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(utilisateur()));
        when(affectationUtilisateurRepository.save(any(AffectationTacheUtilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAffectationTacheRequest request = new UpdateAffectationTacheRequest();
        request.setUtilisateurId(10L);
        request.setRole(AffectationTacheRole.CONTROLEUR);
        request.setDateAffectation(LocalDate.of(2026, 8, 11));

        AffectationTacheResponse response = service.updateAffectation(1L, request);

        assertThat(response.getRole()).isEqualTo(AffectationTacheRole.CONTROLEUR);
    }

    @Test
    void updateAffectation_inexistante_leveException() {
        UpdateAffectationTacheRequest request = new UpdateAffectationTacheRequest();
        request.setUtilisateurId(10L);
        when(affectationUtilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAffectation(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAffectation_sansCible_leveException() {
        assertThatThrownBy(() -> service.updateAffectation(1L, new UpdateAffectationTacheRequest()))
                .isInstanceOf(BadRequestException.class);
    }

    // ------------------------------------------------------------------
    // removeAffectation
    // ------------------------------------------------------------------

    @Test
    void removeAffectation_supprimeDesDeuxTables() {
        service.removeAffectation(1L);

        verify(affectationUtilisateurRepository).deleteById(1L);
        verify(affectationEquipeRepository).deleteById(1L);
    }

    // ------------------------------------------------------------------
    // findByTacheId
    // ------------------------------------------------------------------

    @Test
    void findByTacheId_combineUtilisateursEtEquipes() {
        AffectationTacheUtilisateur eu = new AffectationTacheUtilisateur();
        eu.setTache(tache());
        eu.setUtilisateur(utilisateur());
        eu.setRole(AffectationTacheRole.REALISATEUR);
        when(affectationUtilisateurRepository.findByTacheId(1L)).thenReturn(List.of(eu));

        AffectationTacheEquipe ee = new AffectationTacheEquipe();
        Equipe equipe = new Equipe();
        equipe.setId(2L);
        equipe.setNom("Equipe Finitions");
        ee.setTache(tache());
        ee.setEquipe(equipe);
        ee.setRole(AffectationTacheRole.CONTROLEUR);
        when(affectationEquipeRepository.findByTacheId(1L)).thenReturn(List.of(ee));

        List<AffectationTacheResponse> resultats = service.findByTacheId(1L);

        assertThat(resultats).hasSize(2);
        assertThat(resultats).extracting(r -> r.getEquipeId()).contains(2L);
        assertThat(resultats).extracting(r -> r.getUtilisateurId()).contains(10L);
    }

}