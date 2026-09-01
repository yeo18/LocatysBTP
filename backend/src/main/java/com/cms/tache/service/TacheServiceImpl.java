package com.cms.tache.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.common.response.PageResponse;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.CurrentUserService;
import com.cms.security.service.DataAccessService;
import com.cms.tache.dto.CreateTacheRequest;
import com.cms.tache.dto.TacheResponse;
import com.cms.tache.dto.TacheResumeResponse;
import com.cms.tache.dto.UpdateTacheRequest;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.mapper.TacheMapper;
import com.cms.tache.repository.AffectationTacheEquipeRepository;
import com.cms.tache.repository.AffectationTacheUtilisateurRepository;
import com.cms.tache.repository.TacheRepository;
import com.cms.tache.repository.ValidationTacheRepository;

/**
 * Implementation du service metier du module tache (LOOP 3.15).
 *
 * <p>Securite par donnees (Niveau 2, architecture-backend Â§197) : une
 * permission {@code TACHE_LIRE} ne donne pas acces a toutes les taches.
 * Une tache est visible si l'utilisateur est administrateur (RBAC), ou
 * si elle lui est affectee directement, ou si l'une de ses equipes y est
 * affectee ou est affectee au chantier de la tache. Sinon 403.
 */
@Service
public class TacheServiceImpl implements TacheService {

    private static final Logger logger = LoggerFactory.getLogger(TacheServiceImpl.class);

    private final TacheRepository tacheRepository;
    private final ChantierRepository chantierRepository;
    private final AffectationTacheUtilisateurRepository affectationUtilisateurRepository;
    private final AffectationTacheEquipeRepository affectationEquipeRepository;
    private final ValidationTacheRepository validationTacheRepository;
    private final CurrentUserService currentUserService;
    private final DataAccessService dataAccessService;
    private final TacheMapper tacheMapper;

    public TacheServiceImpl(TacheRepository tacheRepository,
                            ChantierRepository chantierRepository,
                            AffectationTacheUtilisateurRepository affectationUtilisateurRepository,
                            AffectationTacheEquipeRepository affectationEquipeRepository,
                            ValidationTacheRepository validationTacheRepository,
                            CurrentUserService currentUserService,
                            DataAccessService dataAccessService,
                            TacheMapper tacheMapper) {
        this.tacheRepository = tacheRepository;
        this.chantierRepository = chantierRepository;
        this.affectationUtilisateurRepository = affectationUtilisateurRepository;
        this.affectationEquipeRepository = affectationEquipeRepository;
        this.validationTacheRepository = validationTacheRepository;
        this.currentUserService = currentUserService;
        this.dataAccessService = dataAccessService;
        this.tacheMapper = tacheMapper;
    }

    @Override
    @Transactional
    public TacheResponse createTache(CreateTacheRequest request) {
        Chantier chantier = chantierRepository.findById(request.getChantierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chantier introuvable avec l'identifiant : " + request.getChantierId()));
        verifierDates(request.getDateDebut(), request.getDateFin());

        Tache tache = tacheMapper.toEntity(request);
        tache.setChantier(chantier);
        tache.setCreatedBy(currentUserService.getCurrentUtilisateur());
        tache.setStatus(TacheStatus.A_FAIRE);
        tache.setProgression(0);
        tache.setDateCreation(LocalDateTime.now());
        tache.setDateModification(LocalDateTime.now());

        tache = tacheRepository.save(tache);

        logger.info("Creation tache : id={}, titre={}, chantierId={}",
                tache.getId(), tache.getTitre(), chantier.getId());
        return tacheMapper.toResponse(tache);
    }

    @Override
    @Transactional
    public TacheResponse updateTache(Long id, UpdateTacheRequest request) {
        Tache tache = trouverEntityParId(id);
        verifierAcces(tache);
        verifierDates(request.getDateDebut(), request.getDateFin());

        tacheMapper.update(tache, request);
        tache.setDateModification(LocalDateTime.now());

        tache = tacheRepository.save(tache);

        logger.info("Modification tache : id={}, titre={}, status={}",
                tache.getId(), tache.getTitre(), tache.getStatus());
        return tacheMapper.toResponse(tache);
    }

    @Override
    @Transactional(readOnly = true)
    public TacheResponse findById(Long id) {
        Tache tache = trouverEntityParId(id);
        verifierAcces(tache);
        return tacheMapper.toResponse(tache);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TacheResumeResponse> findAll(Pageable pageable) {
        Page<Tache> page = tacheRepository.findAll(pageable);
        List<Tache> contenu = page.getContent().stream()
                .filter(this::accesAutorise)
                .collect(Collectors.toList());
        Page<Tache> pageFiltree = new PageImpl<>(contenu, pageable, contenu.size());
        return PageResponse.from(pageFiltree.map(tacheMapper::toResumeResponse));
    }

    @Override
    @Transactional
    public void deleteTache(Long id) {
        Tache tache = trouverEntityParId(id);
        verifierAcces(tache);
        affectationUtilisateurRepository.deleteByTacheId(id);
        affectationEquipeRepository.deleteByTacheId(id);
        validationTacheRepository.deleteByTacheId(id);
        tacheRepository.delete(tache);
        logger.info("Suppression tache : id={}", id);
    }

    private Tache trouverEntityParId(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tache introuvable avec l'identifiant : " + id));
    }

    private void verifierDates(java.time.LocalDate dateDebut, java.time.LocalDate dateFin) {
        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new BadRequestException("La date de fin doit etre posterieure ou egale a la date de debut");
        }
    }

    // ------------------------------------------------------------------
    // Securite par donnees (Niveau 2)
    // ------------------------------------------------------------------

    private void verifierAcces(Tache tache) {
        if (!accesAutorise(tache)) {
            throw ForbiddenException.generic();
        }
    }

    private boolean accesAutorise(Tache tache) {
        // RÃ¨gle gÃ©nÃ©rale : un utilisateur ne voit que ce qui le concerne.
        // Une tÃ¢che est visible si elle lui est affectÃ©e directement ou si
        // elle est affectÃ©e Ã  l'une de ses Ã©quipes. L'administrateur voit tout.
        if (dataAccessService.estAdministrateur()) {
            return true;
        }
        Long tacheId = tache.getId();
        Long utilisateurId = currentUserService.getCurrentUserId();
        Set<Long> equipeIds = dataAccessService.equipesDeUtilisateur();

        boolean affecteDirectement = affectationUtilisateurRepository.findByTacheId(tacheId).stream()
                .anyMatch(a -> a.getUtilisateur() != null
                        && utilisateurId.equals(a.getUtilisateur().getId()));
        if (affecteDirectement) {
            return true;
        }

        return !equipeIds.isEmpty()
                && affectationEquipeRepository.findByTacheId(tacheId).stream()
                        .anyMatch(a -> a.getEquipe() != null
                                && equipeIds.contains(a.getEquipe().getId()));
    }

}
