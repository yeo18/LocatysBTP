package com.cms.equipe.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.equipe.dto.CreateEquipeRequest;
import com.cms.equipe.dto.EquipeResponse;
import com.cms.equipe.dto.EquipeResumeResponse;
import com.cms.equipe.dto.UpdateEquipeRequest;
import com.cms.equipe.entity.Equipe;
import com.cms.equipe.mapper.EquipeMapper;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;

/**
 * Implementation du service metier du module equipe.
 *
 * <p>Règle générale (Niveau 2) : un utilisateur ne voit que les équipes
 * dont il est membre. L'administrateur voit toutes les équipes.
 */
@Service
public class EquipeServiceImpl implements EquipeService {

    private static final Logger logger = LoggerFactory.getLogger(EquipeServiceImpl.class);

    private final EquipeRepository equipeRepository;
    private final EquipeMapper equipeMapper;
    private final DataAccessService dataAccessService;

    public EquipeServiceImpl(EquipeRepository equipeRepository, EquipeMapper equipeMapper,
                             DataAccessService dataAccessService) {
        this.equipeRepository = equipeRepository;
        this.equipeMapper = equipeMapper;
        this.dataAccessService = dataAccessService;
    }

    @Override
    @Transactional
    public EquipeResponse creer(CreateEquipeRequest request) {
        Equipe equipe = equipeMapper.toEntity(request);
        equipe.setDateCreation(LocalDateTime.now());
        equipe.setDateModification(LocalDateTime.now());

        equipe = equipeRepository.save(equipe);

        logger.info("Creation equipe : id={}, nom={}", equipe.getId(), equipe.getNom());
        return equipeMapper.toResponse(equipe);
    }

    @Override
    @Transactional
    public EquipeResponse modifier(Long id, UpdateEquipeRequest request) {
        Equipe equipe = trouverEntityParId(id);

        equipeMapper.update(equipe, request);
        equipe.setDateModification(LocalDateTime.now());

        equipe = equipeRepository.save(equipe);

        logger.info("Modification equipe : id={}, nom={}", equipe.getId(), equipe.getNom());
        return equipeMapper.toResponse(equipe);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipeResponse trouverParId(Long id) {
        Equipe equipe = trouverEntityParId(id);
        dataAccessService.verifierAccesEquipe(id);
        return equipeMapper.toResponse(equipe);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipeResumeResponse> lister() {
        List<Equipe> equipes = equipeRepository.findAll();
        if (!dataAccessService.estAdministrateur()) {
            equipes = equipes.stream()
                    .filter(e -> dataAccessService.accesEquipe(e.getId()))
                    .collect(Collectors.toList());
        }
        return equipes.stream()
                .map(equipeMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    private Equipe trouverEntityParId(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe introuvable avec l'identifiant : " + id));
    }

}
