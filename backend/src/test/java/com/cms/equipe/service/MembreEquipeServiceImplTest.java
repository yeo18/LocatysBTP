package com.cms.equipe.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.cms.equipe.entity.Equipe;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.equipe.entity.enums.RoleDansEquipe;
import com.cms.equipe.mapper.MembreEquipeMapper;
import com.cms.equipe.mapper.MembreEquipeMapperImpl;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.security.service.DataAccessService;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

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
 * Tests unitaires du service membre-equipe (repositories mocks).
 */
@ExtendWith(MockitoExtension.class)
class MembreEquipeServiceImplTest {

    private static final LocalDate DATE_INTEGRATION = LocalDate.of(2026, 8, 6);

    @Mock
    private MembreEquipeRepository membreEquipeRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private EquipeRepository equipeRepository;
    @Mock
    private DataAccessService dataAccessService;

    private MembreEquipeMapper membreEquipeMapper = new MembreEquipeMapperImpl();
    private MembreEquipeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MembreEquipeServiceImpl(
                membreEquipeRepository, utilisateurRepository, equipeRepository,
                membreEquipeMapper, dataAccessService);
    }

    private Utilisateur creerUtilisateur() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(5L);
        utilisateur.setNom("Konate");
        utilisateur.setPrenom("Awa");
        return utilisateur;
    }

    private Equipe creerEquipe() {
        Equipe equipe = new Equipe();
        equipe.setId(1L);
        equipe.setNom("Equipe Maçonnerie");
        return equipe;
    }

    @Test
    void integrer_fonctionne() {
        when(utilisateurRepository.findById(5L)).thenReturn(Optional.of(creerUtilisateur()));
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(creerEquipe()));
        when(membreEquipeRepository.findByEquipeId(1L)).thenReturn(List.of());
        when(membreEquipeRepository.save(any(MembreEquipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.integrer(1L, 5L, RoleDansEquipe.OUVRIER, DATE_INTEGRATION);

        assertThat(response.getEquipeId()).isEqualTo(1L);
        assertThat(response.getUtilisateurId()).isEqualTo(5L);
        assertThat(response.getRoleDansEquipe()).isEqualTo(RoleDansEquipe.OUVRIER);
        assertThat(response.getDateIntegration()).isEqualTo(DATE_INTEGRATION);
    }

    @Test
    void integrer_roleNul_refuse() {
        assertThatThrownBy(() -> service.integrer(1L, 5L, null, DATE_INTEGRATION))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void integrer_utilisateurDejaMembre_refuse() {
        when(utilisateurRepository.findById(5L)).thenReturn(Optional.of(creerUtilisateur()));
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(creerEquipe()));

        MembreEquipe existant = new MembreEquipe();
        existant.setId(1L);
        existant.setUtilisateur(creerUtilisateur());
        existant.setEquipe(creerEquipe());
        when(membreEquipeRepository.findByEquipeId(1L)).thenReturn(List.of(existant));

        assertThatThrownBy(() -> service.integrer(1L, 5L, RoleDansEquipe.OUVRIER, DATE_INTEGRATION))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void integrer_equipeInexistante_leveException() {
        when(utilisateurRepository.findById(5L)).thenReturn(Optional.of(creerUtilisateur()));
        when(equipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.integrer(1L, 5L, RoleDansEquipe.OUVRIER, DATE_INTEGRATION))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changerRole_metAJourLeRole() {
        MembreEquipe membre = new MembreEquipe();
        membre.setId(10L);
        membre.setUtilisateur(creerUtilisateur());
        membre.setEquipe(creerEquipe());
        membre.setRoleDansEquipe(RoleDansEquipe.OUVRIER);

        when(membreEquipeRepository.findById(10L)).thenReturn(Optional.of(membre));
        when(membreEquipeRepository.save(any(MembreEquipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.changerRole(10L, RoleDansEquipe.CHEF);

        assertThat(response.getRoleDansEquipe()).isEqualTo(RoleDansEquipe.CHEF);
    }

    @Test
    void retirer_supprimeLeMembre() {
        MembreEquipe membre = new MembreEquipe();
        membre.setId(10L);
        membre.setUtilisateur(creerUtilisateur());
        membre.setEquipe(creerEquipe());

        when(membreEquipeRepository.findById(10L)).thenReturn(Optional.of(membre));

        service.retirer(10L);

        verify(membreEquipeRepository).delete(membre);
    }

    @Test
    void listerMembres_retourneListe() {
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(creerEquipe()));
        when(membreEquipeRepository.findByEquipeId(1L)).thenReturn(List.of());

        assertThat(service.listerMembres(1L)).isEmpty();
    }

    @Test
    void listerEquipesDeUtilisateur_retourneListe() {
        when(membreEquipeRepository.findByUtilisateurId(5L)).thenReturn(List.of());

        assertThat(service.listerEquipesDeUtilisateur(5L)).isEmpty();
    }

}
