package com.cms.chantier.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cms.chantier.dto.ChantierResponse;
import com.cms.chantier.dto.CreateChantierRequest;
import com.cms.chantier.dto.UpdateChantierRequest;
import com.cms.chantier.entity.Chantier;
import com.cms.chantier.entity.enums.ChantierStatut;
import com.cms.chantier.mapper.ChantierMapper;
import com.cms.chantier.mapper.ChantierMapperImpl;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.common.dto.SearchRequest;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service chantier (repository mocke, aucun acces DB).
 */
@ExtendWith(MockitoExtension.class)
class ChantierServiceImplTest {

    @Mock
    private ChantierRepository chantierRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private DataAccessService dataAccessService;

    private ChantierMapper chantierMapper = new ChantierMapperImpl();
    private ChantierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChantierServiceImpl(chantierRepository, chantierMapper, utilisateurRepository, dataAccessService);
        lenient().when(dataAccessService.estAdministrateur()).thenReturn(true);
    }

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

    private Chantier chantierExistant(Long id) {
        Chantier chantier = new Chantier();
        chantier.setId(id);
        chantier.setNom("Residence Les Alizes");
        chantier.setAdresseSaisie("Dakar, Ouakam");
        chantier.setStatut(ChantierStatut.EN_COURS);
        chantier.setProgression(0);
        return chantier;
    }

    @Test
    void creer_fonctionne_avecDatesEtProgressionZero() {
        when(chantierRepository.save(any(Chantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChantierResponse response = service.creer(creerRequest());

        assertThat(response.getNom()).isEqualTo("Residence Les Alizes");
        assertThat(response.getStatut()).isEqualTo(ChantierStatut.PREVU);
        assertThat(response.getProgression()).isZero();

        org.mockito.ArgumentCaptor<Chantier> captor =
                org.mockito.ArgumentCaptor.forClass(Chantier.class);
        verify(chantierRepository).save(captor.capture());
        Chantier enregistre = captor.getValue();
        assertThat(enregistre.getDateCreation()).isNotNull();
        assertThat(enregistre.getDateModification()).isNotNull();
    }

    @Test
    void creer_avecResponsable_valide_attacheLeResponsable() {
        Utilisateur responsable = new Utilisateur();
        responsable.setId(3L);
        when(utilisateurRepository.findById(3L))
                .thenReturn(Optional.of(responsable));
        when(chantierRepository.save(any(Chantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateChantierRequest request = creerRequest();
        request.setResponsableId(3L);

        ChantierResponse response = service.creer(request);

        assertThat(response.getResponsableId()).isEqualTo(3L);
    }

    @Test
    void creer_responsableInexistant_leveException() {
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        CreateChantierRequest request = creerRequest();
        request.setResponsableId(999L);

        assertThatThrownBy(() -> service.creer(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void modifier_fonctionne() {
        Chantier chantier = chantierExistant(1L);
        when(chantierRepository.findById(1L)).thenReturn(Optional.of(chantier));
        when(chantierRepository.save(any(Chantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateChantierRequest request = new UpdateChantierRequest();
        request.setNom("Residence Les Alizes 2");
        request.setStatut(ChantierStatut.TERMINE);

        ChantierResponse response = service.modifier(1L, request);

        assertThat(response.getNom()).isEqualTo("Residence Les Alizes 2");
        assertThat(response.getStatut()).isEqualTo(ChantierStatut.TERMINE);
    }

    @Test
    void modifier_inexistant_leveException() {
        when(chantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modifier(999L, new UpdateChantierRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void annuler_passeAuStatutANNULE() {
        Chantier chantier = chantierExistant(1L);
        when(chantierRepository.findById(1L)).thenReturn(Optional.of(chantier));
        when(chantierRepository.save(any(Chantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ChantierResponse response = service.annuler(1L);

        assertThat(response.getStatut()).isEqualTo(ChantierStatut.ANNULE);
    }

    @Test
    void annuler_dejaAnnule_leveException() {
        Chantier chantier = chantierExistant(1L);
        chantier.setStatut(ChantierStatut.ANNULE);
        when(chantierRepository.findById(1L)).thenReturn(Optional.of(chantier));

        assertThatThrownBy(() -> service.annuler(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void trouverParId_inexistant_leveException() {
        when(chantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void trouverParId_calculeProgressionDepuisTaches() {
        Chantier chantier = chantierExistant(1L);
        chantier.getTaches().add(tache(TacheStatus.VALIDE));
        chantier.getTaches().add(tache(TacheStatus.VALIDE));
        chantier.getTaches().add(tache(TacheStatus.A_FAIRE));
        when(chantierRepository.findById(1L)).thenReturn(Optional.of(chantier));

        ChantierResponse response = service.trouverParId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProgression()).isEqualTo(67);
    }

    @Test
    void rechercher_sansFiltre_retournePage() {
        Chantier c1 = chantierExistant(1L);
        Chantier c2 = chantierExistant(2L);
        when(chantierRepository.findAll()).thenReturn(List.of(c1, c2));

        SearchRequest search = new SearchRequest();
        search.setPage(0);
        search.setSize(10);

        var page = service.rechercher(search, null);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void rechercher_motCle_filtreSurNom() {
        Chantier c1 = chantierExistant(1L);
        c1.setNom("Residence Les Alizes");
        Chantier c2 = chantierExistant(2L);
        c2.setNom("Ecole de Thies");
        when(chantierRepository.findAll()).thenReturn(List.of(c1, c2));

        SearchRequest search = new SearchRequest();
        search.setPage(0);
        search.setSize(10);
        search.setMotCle("alizes");

        var page = service.rechercher(search, null);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getNom()).isEqualTo("Residence Les Alizes");
    }

    private Tache tache(TacheStatus status) {
        Tache tache = new Tache();
        tache.setStatus(status);
        return tache;
    }

}
