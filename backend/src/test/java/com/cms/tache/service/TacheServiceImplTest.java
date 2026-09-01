package com.cms.tache.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.common.response.PageResponse;
import com.cms.equipe.entity.AffectationEquipeChantier;
import com.cms.equipe.entity.Equipe;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.profil.entity.Profil;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.dto.CreateTacheRequest;
import com.cms.tache.dto.TacheResponse;
import com.cms.tache.dto.TacheResumeResponse;
import com.cms.tache.entity.AffectationTacheEquipe;
import com.cms.tache.entity.AffectationTacheUtilisateur;
import com.cms.tache.entity.enums.AffectationTacheRole;
import com.cms.tache.entity.enums.Priorite;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.mapper.TacheMapper;
import com.cms.tache.mapper.TacheMapperImpl;
import com.cms.tache.repository.AffectationTacheEquipeRepository;
import com.cms.tache.repository.AffectationTacheUtilisateurRepository;
import com.cms.tache.repository.TacheRepository;
import com.cms.tache.repository.ValidationTacheRepository;
import com.cms.security.service.DataAccessService;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service tache (repository mocke, aucun acces DB).
 */
@ExtendWith(MockitoExtension.class)
class TacheServiceImplTest {

    @Mock
    private TacheRepository tacheRepository;
    @Mock
    private ChantierRepository chantierRepository;
    @Mock
    private AffectationTacheUtilisateurRepository affectationUtilisateurRepository;
    @Mock
    private AffectationTacheEquipeRepository affectationEquipeRepository;
    @Mock
    private ValidationTacheRepository validationTacheRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private DataAccessService dataAccessService;

    private TacheMapper tacheMapper = new TacheMapperImpl();
    private TacheServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TacheServiceImpl(tacheRepository, chantierRepository,
                affectationUtilisateurRepository, affectationEquipeRepository,
                validationTacheRepository, currentUserService, dataAccessService, tacheMapper);
    }

    private Chantier chantier(Long id) {
        Chantier chantier = new Chantier();
        chantier.setId(id);
        chantier.setNom("Chantier Test");
        return chantier;
    }

    private Tache tache(Long id, Long chantierId) {
        Tache tache = new Tache();
        tache.setId(id);
        tache.setTitre("Fondations");
        tache.setPriorite(Priorite.HAUTE);
        tache.setStatus(TacheStatus.A_FAIRE);
        tache.setProgression(0);
        tache.setChantier(chantier(chantierId));
        return tache;
    }

    private CreateTacheRequest creerRequest() {
        CreateTacheRequest request = new CreateTacheRequest();
        request.setTitre("Fondations");
        request.setPriorite(Priorite.HAUTE);
        request.setDateDebut(LocalDate.of(2026, 8, 10));
        request.setDateFin(LocalDate.of(2026, 8, 20));
        request.setChantierId(1L);
        return request;
    }

    private void mockAdmin() {
        when(dataAccessService.estAdministrateur()).thenReturn(true);
    }

    // ------------------------------------------------------------------
    // createTache
    // ------------------------------------------------------------------

    @Test
    void createTache_fonctionne_avecStatutEtProgressionInitiaux() {
        when(chantierRepository.findById(1L)).thenReturn(Optional.of(chantier(1L)));
        when(tacheRepository.save(any(Tache.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(currentUserService.getCurrentUtilisateur()).thenReturn(new Utilisateur());

        TacheResponse response = service.createTache(creerRequest());

        assertThat(response.getTitre()).isEqualTo("Fondations");
        assertThat(response.getChantierId()).isEqualTo(1L);
        verify(tacheRepository).save(any(Tache.class));
    }

    @Test
    void createTache_chantierInexistant_leveException() {
        when(chantierRepository.findById(999L)).thenReturn(Optional.empty());
        CreateTacheRequest request = creerRequest();
        request.setChantierId(999L);

        assertThatThrownBy(() -> service.createTache(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createTache_dateFinAvantDateDebut_leveException() {
        when(chantierRepository.findById(1L)).thenReturn(Optional.of(chantier(1L)));
        CreateTacheRequest request = creerRequest();
        request.setDateDebut(LocalDate.of(2026, 8, 20));
        request.setDateFin(LocalDate.of(2026, 8, 10));

        assertThatThrownBy(() -> service.createTache(request))
                .isInstanceOf(BadRequestException.class);
    }

    // ------------------------------------------------------------------
    // updateTache
    // ------------------------------------------------------------------

    @Test
    void updateTache_fonctionne() {
        Tache tache = tache(1L, 1L);
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        mockAdmin();
        when(tacheRepository.save(any(Tache.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new com.cms.tache.dto.UpdateTacheRequest();
        request.setTitre("Fondations renforcees");
        request.setStatus(TacheStatus.EN_COURS);
        request.setProgression(50);

        TacheResponse response = service.updateTache(1L, request);

        assertThat(response.getTitre()).isEqualTo("Fondations renforcees");
        assertThat(response.getStatus()).isEqualTo(TacheStatus.EN_COURS);
        assertThat(response.getProgression()).isEqualTo(50);
    }

    @Test
    void updateTache_inexistante_leveException() {
        when(tacheRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTache(999L, new com.cms.tache.dto.UpdateTacheRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // findById + securite par donnees
    // ------------------------------------------------------------------

    @Test
    void findById_inexistante_leveException() {
        when(tacheRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_administrateur_accede() {
        Tache tache = tache(1L, 1L);
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        mockAdmin();

        TacheResponse response = service.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void findById_utilisateurAffecteDirectement_accede() {
        Tache tache = tache(1L, 1L);
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(currentUserService.getCurrentUserId()).thenReturn(10L);

        AffectationTacheUtilisateur affectation = new AffectationTacheUtilisateur();
        affectation.setId(1L);
        affectation.setTache(tache);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(10L);
        affectation.setUtilisateur(utilisateur);
        when(affectationUtilisateurRepository.findByTacheId(1L)).thenReturn(List.of(affectation));

        TacheResponse response = service.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void findById_equipeDeLUtilisateur_accede() {
        Tache tache = tache(1L, 5L);
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(dataAccessService.equipesDeUtilisateur()).thenReturn(Set.of(2L));

        Equipe equipe = new Equipe();
        equipe.setId(2L);
        AffectationTacheEquipe affectation = new AffectationTacheEquipe();
        affectation.setTache(tache);
        affectation.setEquipe(equipe);
        when(affectationEquipeRepository.findByTacheId(1L)).thenReturn(List.of(affectation));

        TacheResponse response = service.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void findById_utilisateurHorsPerimetre_refuse() {
        Tache tache = tache(1L, 5L);
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(affectationUtilisateurRepository.findByTacheId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ForbiddenException.class);
    }

    // ------------------------------------------------------------------
    // findAll
    // ------------------------------------------------------------------

    @Test
    void findAll_retournePage() {
        Page<Tache> page = new PageImpl<>(List.of(tache(1L, 1L)), PageRequest.of(0, 10), 1);
        when(tacheRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        mockAdmin();

        PageResponse<TacheResumeResponse> result = service.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    // ------------------------------------------------------------------
    // deleteTache
    // ------------------------------------------------------------------

    @Test
    void deleteTache_fonctionne() {
        Tache tache = tache(1L, 1L);
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        mockAdmin();

        service.deleteTache(1L);

        verify(affectationUtilisateurRepository).deleteByTacheId(1L);
        verify(affectationEquipeRepository).deleteByTacheId(1L);
        verify(validationTacheRepository).deleteByTacheId(1L);
        verify(tacheRepository).delete(tache);
    }

    @Test
    void deleteTache_inexistante_leveException() {
        when(tacheRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteTache(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTache_horsPerimetre_refuse() {
        Tache tache = tache(1L, 5L);
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(dataAccessService.estAdministrateur()).thenReturn(false);
        when(currentUserService.getCurrentUserId()).thenReturn(10L);
        when(dataAccessService.equipesDeUtilisateur()).thenReturn(Set.of());
        when(affectationUtilisateurRepository.findByTacheId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.deleteTache(1L))
                .isInstanceOf(ForbiddenException.class);
    }

}
