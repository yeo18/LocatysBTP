package com.cms.tache.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.dto.CreateValidationTacheRequest;
import com.cms.tache.dto.ValidationTacheResponse;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.ValidationTache;
import com.cms.tache.entity.enums.ValidationTacheStatut;
import com.cms.tache.mapper.ValidationTacheMapper;
import com.cms.tache.mapper.ValidationTacheMapperImpl;
import com.cms.tache.repository.TacheRepository;
import com.cms.tache.repository.ValidationTacheRepository;
import com.cms.utilisateur.entity.Utilisateur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service de validation de taches (LOOP 3.15).
 *
 * <p>Valide que l'historique est conserve (chaque decision = nouvelle ligne,
 * jamais un simple champ dans {@code Tache}).
 */
@ExtendWith(MockitoExtension.class)
class ValidationTacheServiceImplTest {

    @Mock
    private ValidationTacheRepository validationTacheRepository;
    @Mock
    private TacheRepository tacheRepository;
    @Mock
    private CurrentUserService currentUserService;

    private ValidationTacheMapper mapper = new ValidationTacheMapperImpl();
    private ValidationTacheServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ValidationTacheServiceImpl(validationTacheRepository, tacheRepository,
                currentUserService, mapper);
    }

    private Tache tache() {
        Tache tache = new Tache();
        tache.setId(1L);
        tache.setTitre("Fondations");
        return tache;
    }

    private CreateValidationTacheRequest request(ValidationTacheStatut statut) {
        CreateValidationTacheRequest request = new CreateValidationTacheRequest();
        request.setTacheId(1L);
        request.setStatut(statut);
        request.setCommentaire(statut == ValidationTacheStatut.VALIDE ? "Conforme" : "Reprendre");
        request.setDateValidation(LocalDate.of(2026, 8, 25));
        return request;
    }

    @Test
    void validateTache_enregistreValidationAvecValidateur() {
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache()));
        Utilisateur validateur = new Utilisateur();
        validateur.setId(10L);
        when(currentUserService.getCurrentUtilisateur()).thenReturn(validateur);
        when(validationTacheRepository.save(any(ValidationTache.class)))
                .thenAnswer(invocation -> {
                    ValidationTache v = invocation.getArgument(0);
                    v.setId(1L);
                    return v;
                });

        ValidationTacheResponse response = service.validateTache(request(ValidationTacheStatut.VALIDE));

        assertThat(response.getStatut()).isEqualTo(ValidationTacheStatut.VALIDE);
        assertThat(response.getTacheId()).isEqualTo(1L);
        verify(validationTacheRepository).save(any(ValidationTache.class));
    }

    @Test
    void validateTache_tacheInexistante_leveException() {
        when(tacheRepository.findById(999L)).thenReturn(Optional.empty());
        CreateValidationTacheRequest request = request(ValidationTacheStatut.VALIDE);
        request.setTacheId(999L);

        assertThatThrownBy(() -> service.validateTache(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getValidationHistory_historiqueConcerne() {
        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache()));

        ValidationTache v1 = new ValidationTache();
        v1.setId(1L);
        v1.setTache(tache());
        v1.setStatut(ValidationTacheStatut.VALIDE);
        v1.setDateValidation(LocalDate.of(2026, 8, 25));

        ValidationTache v2 = new ValidationTache();
        v2.setId(2L);
        v2.setTache(tache());
        v2.setStatut(ValidationTacheStatut.REFUSE);
        v2.setDateValidation(LocalDate.of(2026, 8, 26));

        when(validationTacheRepository.findByTacheId(1L)).thenReturn(List.of(v1, v2));

        List<ValidationTacheResponse> historique = service.getValidationHistory(1L);

        assertThat(historique).hasSize(2);
        assertThat(historique.get(0).getStatut()).isEqualTo(ValidationTacheStatut.VALIDE);
        assertThat(historique.get(1).getStatut()).isEqualTo(ValidationTacheStatut.REFUSE);
    }

    @Test
    void getValidationHistory_tacheInexistante_leveException() {
        when(tacheRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getValidationHistory(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
