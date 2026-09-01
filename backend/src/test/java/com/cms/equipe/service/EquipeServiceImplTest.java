package com.cms.equipe.service;

import java.util.Optional;

import com.cms.equipe.dto.CreateEquipeRequest;
import com.cms.equipe.dto.EquipeResponse;
import com.cms.equipe.dto.UpdateEquipeRequest;
import com.cms.equipe.entity.Equipe;
import com.cms.equipe.mapper.EquipeMapper;
import com.cms.equipe.mapper.EquipeMapperImpl;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service equipe (repository mocke, aucun acces DB).
 */
@ExtendWith(MockitoExtension.class)
class EquipeServiceImplTest {

    @Mock
    private EquipeRepository equipeRepository;
    @Mock
    private DataAccessService dataAccessService;

    private EquipeMapper equipeMapper = new EquipeMapperImpl();
    private EquipeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EquipeServiceImpl(equipeRepository, equipeMapper, dataAccessService);
    }

    private CreateEquipeRequest creerRequest() {
        CreateEquipeRequest request = new CreateEquipeRequest();
        request.setNom("Equipe Maçonnerie");
        request.setDescription("Travaux de maçonnerie");
        return request;
    }

    @Test
    void creer_fonctionne_avecActifEtDates() {
        when(equipeRepository.save(any(Equipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EquipeResponse response = service.creer(creerRequest());

        assertThat(response.getNom()).isEqualTo("Equipe Maçonnerie");
        assertThat(response.getDescription()).isEqualTo("Travaux de maçonnerie");

        ArgumentCaptor<Equipe> captor = ArgumentCaptor.forClass(Equipe.class);
        verify(equipeRepository).save(captor.capture());
        Equipe enregistree = captor.getValue();
        assertThat(enregistree.getDateCreation()).isNotNull();
        assertThat(enregistree.getDateModification()).isNotNull();
    }

    @Test
    void modifier_fonctionne() {
        Equipe equipe = new Equipe();
        equipe.setId(1L);
        equipe.setNom("Equipe Maçonnerie");

        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(equipeRepository.save(any(Equipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateEquipeRequest request = new UpdateEquipeRequest();
        request.setNom("Equipe Coffrage");
        request.setDescription("Nouvelle description");

        EquipeResponse response = service.modifier(1L, request);

        assertThat(response.getNom()).isEqualTo("Equipe Coffrage");
        assertThat(response.getDescription()).isEqualTo("Nouvelle description");
    }

    @Test
    void trouverParId_inexistant_leveException() {
        when(equipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void trouverParId_existant_retourneReponse() {
        Equipe equipe = new Equipe();
        equipe.setId(1L);
        equipe.setNom("Equipe Maçonnerie");

        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));

        EquipeResponse response = service.trouverParId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNom()).isEqualTo("Equipe Maçonnerie");
    }

    @Test
    void lister_retourneEquipes() {
        when(equipeRepository.findAll()).thenReturn(java.util.List.of());

        assertThat(service.lister()).isEmpty();
    }

}
