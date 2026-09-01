package com.cms.equipe.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.equipe.entity.AffectationEquipeChantier;
import com.cms.equipe.entity.enums.AffectationEquipeChantierStatut;
import com.cms.equipe.entity.Equipe;
import com.cms.equipe.mapper.AffectationEquipeChantierMapper;
import com.cms.equipe.mapper.AffectationEquipeChantierMapperImpl;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service affectation equipe - chantier (repositories mocks).
 */
@ExtendWith(MockitoExtension.class)
class AffectationEquipeChantierServiceImplTest {

    @Mock
    private AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    @Mock
    private EquipeRepository equipeRepository;
    @Mock
    private ChantierRepository chantierRepository;
    @Mock
    private DataAccessService dataAccessService;

    private AffectationEquipeChantierMapper affectationEquipeChantierMapper =
            new AffectationEquipeChantierMapperImpl();
    private AffectationEquipeChantierServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AffectationEquipeChantierServiceImpl(
                affectationEquipeChantierRepository, equipeRepository,
                chantierRepository, affectationEquipeChantierMapper, dataAccessService);
    }

    private Equipe creerEquipe() {
        Equipe equipe = new Equipe();
        equipe.setId(1L);
        equipe.setNom("Equipe Maçonnerie");
        return equipe;
    }

    private Chantier creerChantier() {
        Chantier chantier = new Chantier();
        chantier.setId(2L);
        chantier.setNom("Construction Villa");
        return chantier;
    }

    @Test
    void affecter_fonctionne() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(creerEquipe()));
        when(chantierRepository.findById(2L)).thenReturn(Optional.of(creerChantier()));
        when(affectationEquipeChantierRepository.findByEquipeId(1L)).thenReturn(List.of());
        when(affectationEquipeChantierRepository.save(any(AffectationEquipeChantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.affecter(1L, 2L,
                LocalDate.of(2026, 8, 6), LocalDate.of(2026, 12, 31));

        assertThat(response.getEquipeId()).isEqualTo(1L);
        assertThat(response.getChantierId()).isEqualTo(2L);
        assertThat(response.getStatut()).isEqualTo(AffectationEquipeChantierStatut.ACTIVE);
    }

    @Test
    void affecter_dateDebutNulle_refuse() {
        assertThatThrownBy(() -> service.affecter(1L, 2L, null, LocalDate.of(2026, 12, 31)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void affecter_dateFinAvantDateDebut_refuse() {
        assertThatThrownBy(() -> service.affecter(1L, 2L,
                LocalDate.of(2026, 12, 31), LocalDate.of(2026, 8, 6)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void affecter_affectationActiveExistante_refuse() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(creerEquipe()));
        when(chantierRepository.findById(2L)).thenReturn(Optional.of(creerChantier()));

        AffectationEquipeChantier existante = new AffectationEquipeChantier();
        existante.setId(1L);
        existante.setEquipe(creerEquipe());
        existante.setChantier(creerChantier());
        existante.setStatut(AffectationEquipeChantierStatut.ACTIVE);
        when(affectationEquipeChantierRepository.findByEquipeId(1L)).thenReturn(List.of(existante));

        assertThatThrownBy(() -> service.affecter(1L, 2L,
                LocalDate.of(2026, 8, 6), null))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void terminer_passeStatutTerminee() {
        AffectationEquipeChantier affectation = new AffectationEquipeChantier();
        affectation.setId(1L);
        affectation.setEquipe(creerEquipe());
        affectation.setChantier(creerChantier());
        affectation.setStatut(AffectationEquipeChantierStatut.ACTIVE);

        when(affectationEquipeChantierRepository.findById(1L)).thenReturn(Optional.of(affectation));
        when(affectationEquipeChantierRepository.save(any(AffectationEquipeChantier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.terminer(1L);

        assertThat(response.getStatut()).isEqualTo(AffectationEquipeChantierStatut.TERMINEE);
    }

    @Test
    void terminer_dejaTerminee_leveException() {
        AffectationEquipeChantier affectation = new AffectationEquipeChantier();
        affectation.setId(1L);
        affectation.setEquipe(creerEquipe());
        affectation.setChantier(creerChantier());
        affectation.setStatut(AffectationEquipeChantierStatut.TERMINEE);

        when(affectationEquipeChantierRepository.findById(1L)).thenReturn(Optional.of(affectation));

        assertThatThrownBy(() -> service.terminer(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listerEquipesDuChantier_retourneListe() {
        when(chantierRepository.findById(2L)).thenReturn(Optional.of(creerChantier()));
        when(affectationEquipeChantierRepository.findByChantierId(2L)).thenReturn(List.of());

        assertThat(service.listerEquipesDuChantier(2L)).isEmpty();
    }

    @Test
    void listerChantiersDeEquipe_retourneListe() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(creerEquipe()));
        when(affectationEquipeChantierRepository.findByEquipeId(1L)).thenReturn(List.of());

        assertThat(service.listerChantiersDeEquipe(1L)).isEmpty();
    }

    @Test
    void listerEquipesDuChantier_chantierInexistant_leveException() {
        when(chantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listerEquipesDuChantier(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void affecter_equipeInexistante_leveException() {
        when(equipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.affecter(999L, 2L, LocalDate.of(2026, 8, 6), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void affecter_chantierInexistant_leveException() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(creerEquipe()));
        when(chantierRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.affecter(1L, 999L, LocalDate.of(2026, 8, 6), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
