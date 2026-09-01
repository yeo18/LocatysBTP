package com.cms.equipe.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.equipe.dto.AffectationEquipeChantierResponse;
import com.cms.equipe.dto.AffectationEquipeChantierResumeResponse;
import com.cms.equipe.entity.AffectationEquipeChantier;
import com.cms.equipe.entity.enums.AffectationEquipeChantierStatut;
import com.cms.equipe.entity.Equipe;
import com.cms.equipe.mapper.AffectationEquipeChantierMapper;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;

/**
 * Implementation du service metier des affectations equipe - chantier.
 */
@Service
public class AffectationEquipeChantierServiceImpl implements AffectationEquipeChantierService {

    private static final Logger logger = LoggerFactory.getLogger(AffectationEquipeChantierServiceImpl.class);

    private final AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    private final EquipeRepository equipeRepository;
    private final ChantierRepository chantierRepository;
    private final AffectationEquipeChantierMapper affectationEquipeChantierMapper;
    private final DataAccessService dataAccessService;

    public AffectationEquipeChantierServiceImpl(
            AffectationEquipeChantierRepository affectationEquipeChantierRepository,
            EquipeRepository equipeRepository,
            ChantierRepository chantierRepository,
            AffectationEquipeChantierMapper affectationEquipeChantierMapper,
            DataAccessService dataAccessService) {
        this.affectationEquipeChantierRepository = affectationEquipeChantierRepository;
        this.equipeRepository = equipeRepository;
        this.chantierRepository = chantierRepository;
        this.affectationEquipeChantierMapper = affectationEquipeChantierMapper;
        this.dataAccessService = dataAccessService;
    }

    @Override
    @Transactional
    public AffectationEquipeChantierResponse affecter(Long equipeId, Long chantierId,
                                                      LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null) {
            throw new BadRequestException("La date de debut d'affectation est obligatoire");
        }
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new BadRequestException("La date de fin doit etre posterieure ou egale a la date de debut");
        }

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe introuvable avec l'identifiant : " + equipeId));
        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new ResourceNotFoundException("Chantier introuvable avec l'identifiant : " + chantierId));

        boolean affectationActiveExistante = affectationEquipeChantierRepository.findByEquipeId(equipeId).stream()
                .anyMatch(a -> a.getChantier().getId().equals(chantierId)
                        && a.getStatut() == AffectationEquipeChantierStatut.ACTIVE);
        if (affectationActiveExistante) {
            throw DuplicateResourceException.pourConflit(
                    "affectation active (equipe, chantier)", equipeId + ", " + chantierId);
        }

        AffectationEquipeChantier affectation = new AffectationEquipeChantier();
        affectation.setEquipe(equipe);
        affectation.setChantier(chantier);
        affectation.setDateDebut(dateDebut);
        affectation.setDateFin(dateFin);
        affectation.setStatut(AffectationEquipeChantierStatut.ACTIVE);

        affectation = affectationEquipeChantierRepository.save(affectation);

        logger.info("Affectation equipe-chantier : equipeId={}, chantierId={}, dateDebut={}",
                equipeId, chantierId, dateDebut);
        return affectationEquipeChantierMapper.toResponse(affectation);
    }

    @Override
    @Transactional
    public AffectationEquipeChantierResponse terminer(Long affectationId) {
        AffectationEquipeChantier affectation = trouverEntityParId(affectationId);
        if (affectation.getStatut() == AffectationEquipeChantierStatut.TERMINEE) {
            throw new ResourceNotFoundException("Affectation deja terminee : " + affectationId);
        }
        affectation.setStatut(AffectationEquipeChantierStatut.TERMINEE);
        affectation = affectationEquipeChantierRepository.save(affectation);

        logger.info("Terminaison affectation : id={}", affectation.getId());
        return affectationEquipeChantierMapper.toResponse(affectation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationEquipeChantierResumeResponse> listerEquipesDuChantier(Long chantierId) {
        chantierRepository.findById(chantierId)
                .orElseThrow(() -> new ResourceNotFoundException("Chantier introuvable avec l'identifiant : " + chantierId));
        return affectationEquipeChantierRepository.findByChantierId(chantierId).stream()
                .filter(a -> dataAccessService.accesEquipe(a.getEquipe().getId()))
                .map(affectationEquipeChantierMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationEquipeChantierResponse> listerChantiersDeEquipe(Long equipeId) {
        equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe introuvable avec l'identifiant : " + equipeId));
        dataAccessService.verifierAccesEquipe(equipeId);
        return affectationEquipeChantierRepository.findByEquipeId(equipeId).stream()
                .map(affectationEquipeChantierMapper::toResponse)
                .collect(Collectors.toList());
    }

    private AffectationEquipeChantier trouverEntityParId(Long id) {
        return affectationEquipeChantierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation equipe-chantier introuvable avec l'identifiant : " + id));
    }

}
