package com.cms.tache.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.dto.CreateValidationTacheRequest;
import com.cms.tache.dto.ValidationTacheResponse;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.entity.ValidationTache;
import com.cms.tache.entity.enums.ValidationTacheStatut;
import com.cms.tache.mapper.ValidationTacheMapper;
import com.cms.tache.repository.TacheRepository;
import com.cms.tache.repository.ValidationTacheRepository;

/**
 * Implementation du service des validations de taches (LOOP 3.15).
 *
 * <p>L'historique est conserve : chaque appel a {@code validateTache} insere
 * une nouvelle ligne {@code ValidationTache}. Le validateur est l'utilisateur
 * connecte.
 */
@Service
public class ValidationTacheServiceImpl implements ValidationTacheService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationTacheServiceImpl.class);

    private final ValidationTacheRepository validationTacheRepository;
    private final TacheRepository tacheRepository;
    private final CurrentUserService currentUserService;
    private final ValidationTacheMapper validationTacheMapper;

    public ValidationTacheServiceImpl(ValidationTacheRepository validationTacheRepository,
                                      TacheRepository tacheRepository,
                                      CurrentUserService currentUserService,
                                      ValidationTacheMapper validationTacheMapper) {
        this.validationTacheRepository = validationTacheRepository;
        this.tacheRepository = tacheRepository;
        this.currentUserService = currentUserService;
        this.validationTacheMapper = validationTacheMapper;
    }

    @Override
    @Transactional
    public ValidationTacheResponse validateTache(CreateValidationTacheRequest request) {
        Tache tache = tacheRepository.findById(request.getTacheId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tache introuvable avec l'identifiant : " + request.getTacheId()));

        ValidationTache validation = validationTacheMapper.toEntity(request);
        validation.setTache(tache);
        validation.setValidateur(currentUserService.getCurrentUtilisateur());
        validation.setDateModification(LocalDateTime.now());

        if (request.getStatut() == ValidationTacheStatut.VALIDE) {
            tache.setProgression(100);
            tache.setStatus(TacheStatus.VALIDE);
        } else if (request.getStatut() == ValidationTacheStatut.REFUSE) {
            tache.setProgression(0);
            tache.setStatus(TacheStatus.REFUSE);
        }
        tacheRepository.save(tache);

        validation = validationTacheRepository.save(validation);

        logger.info("Validation tache : tacheId={}, statut={}, validateurId={}",
                tache.getId(), request.getStatut(),
                validation.getValidateur() != null ? validation.getValidateur().getId() : null);
        return validationTacheMapper.toResponse(validation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValidationTacheResponse> getValidationHistory(Long tacheId) {
        tacheRepository.findById(tacheId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tache introuvable avec l'identifiant : " + tacheId));
        return validationTacheRepository.findByTacheId(tacheId).stream()
                .map(validationTacheMapper::toResponse)
                .collect(Collectors.toList());
    }

}
