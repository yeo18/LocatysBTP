package com.cms.template.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.chantier.repository.ChantierRepository;
import com.cms.common.constants.SystemRoles;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.profil.entity.Profil;
import com.cms.security.service.CurrentUserService;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.repository.TacheRepository;
import com.cms.template.dto.CreateTemplateTacheRequest;
import com.cms.template.dto.CreateTemplateTacheTacheRequest;
import com.cms.template.dto.TemplateTacheResponse;
import com.cms.template.dto.TemplateTacheTacheResponse;
import com.cms.template.dto.TemplateTacheTacheResumeResponse;
import com.cms.template.dto.UpdateTemplateTacheRequest;
import com.cms.template.dto.UpdateTemplateTacheTacheRequest;
import com.cms.template.entity.TemplateTache;
import com.cms.template.entity.TemplateTacheTache;
import com.cms.template.mapper.TemplateTacheMapper;
import com.cms.template.mapper.TemplateTacheTacheMapper;
import com.cms.template.repository.TemplateTacheRepository;
import com.cms.template.repository.TemplateTacheTacheRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation du service metier TemplateTache (LOOP 5.8).
 */
@Service
public class TemplateTacheServiceImpl implements TemplateTacheService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateTacheServiceImpl.class);

    private final TemplateTacheRepository templateTacheRepository;
    private final TemplateTacheTacheRepository templateTacheTacheRepository;
    private final ChantierRepository chantierRepository;
    private final TacheRepository tacheRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MembreEquipeRepository membreEquipeRepository;
    private final AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    private final CurrentUserService currentUserService;
    private final TemplateTacheMapper templateTacheMapper;
    private final TemplateTacheTacheMapper templateTacheTacheMapper;

    public TemplateTacheServiceImpl(TemplateTacheRepository templateTacheRepository,
                                    TemplateTacheTacheRepository templateTacheTacheRepository,
                                    ChantierRepository chantierRepository,
                                    TacheRepository tacheRepository,
                                    UtilisateurRepository utilisateurRepository,
                                    MembreEquipeRepository membreEquipeRepository,
                                    AffectationEquipeChantierRepository affectationEquipeChantierRepository,
                                    CurrentUserService currentUserService,
                                    TemplateTacheMapper templateTacheMapper,
                                    TemplateTacheTacheMapper templateTacheTacheMapper) {
        this.templateTacheRepository = templateTacheRepository;
        this.templateTacheTacheRepository = templateTacheTacheRepository;
        this.chantierRepository = chantierRepository;
        this.tacheRepository = tacheRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.membreEquipeRepository = membreEquipeRepository;
        this.affectationEquipeChantierRepository = affectationEquipeChantierRepository;
        this.currentUserService = currentUserService;
        this.templateTacheMapper = templateTacheMapper;
        this.templateTacheTacheMapper = templateTacheTacheMapper;
    }

    @Override
    @Transactional
    public TemplateTacheResponse creer(CreateTemplateTacheRequest request) {
        TemplateTache templateTache = new TemplateTache();
        templateTache.setTitre(request.getTitre());
        templateTache.setDescription(request.getDescription());
        templateTache.setPriorite(request.getPriorite() != null ? request.getPriorite() : com.cms.tache.entity.enums.Priorite.MOYENNE);
        templateTache.setDureeEstimeeJours(request.getDureeEstimeeJours());
        templateTache.setCreatedBy(currentUserService.getCurrentUtilisateur());
        templateTache.setDateCreation(LocalDateTime.now());
        templateTache.setDateModification(LocalDateTime.now());

        templateTache = templateTacheRepository.save(templateTache);

        logger.info("Creation TemplateTache : id={}, titre={}", templateTache.getId(), templateTache.getTitre());
        return toResponse(templateTache);
    }

    @Override
    @Transactional
    public TemplateTacheResponse modifier(Long id, UpdateTemplateTacheRequest request) {
        TemplateTache templateTache = trouverEntityParId(id);
        templateTache.setTitre(request.getTitre());
        templateTache.setDescription(request.getDescription());
        templateTache.setPriorite(request.getPriorite() != null ? request.getPriorite() : com.cms.tache.entity.enums.Priorite.MOYENNE);
        templateTache.setDureeEstimeeJours(request.getDureeEstimeeJours());
        templateTache.setDateModification(LocalDateTime.now());

        templateTache = templateTacheRepository.save(templateTache);

        logger.info("Modification TemplateTache : id={}", templateTache.getId());
        return toResponse(templateTache);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateTacheResponse trouverParId(Long id) {
        return toResponse(trouverEntityParId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateTacheResponse> lister() {
        return templateTacheRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateTacheResponse> listerParNom(String nom) {
        if (nom == null || nom.isBlank()) {
            return lister();
        }
        return templateTacheRepository.findByTitreContainingIgnoreCase(nom.trim()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TemplateTacheTacheResponse ajouterTache(Long templateTacheId, CreateTemplateTacheTacheRequest request) {
        TemplateTache templateTache = trouverEntityParId(templateTacheId);

        String titre = request.getTitre().trim();
        verifierTitreUnique(templateTacheId, titre);

        TemplateTacheTache item = new TemplateTacheTache();
        item.setTemplateTache(templateTache);
        item.setTitre(titre);
        item.setDescription(request.getDescription());
        item.setPriorite(request.getPriorite() != null ? request.getPriorite() : com.cms.tache.entity.enums.Priorite.MOYENNE);
        item.setDureeEstimeeJours(request.getDureeEstimeeJours());
        item.setCreatedBy(currentUserService.getCurrentUtilisateur());
        item.setDateCreation(LocalDateTime.now());
        item.setDateModification(LocalDateTime.now());

        item = templateTacheTacheRepository.save(item);
        logger.info("Ajout tache structuree au TemplateTache id={} : itemId={}", templateTacheId, item.getId());
        return toItemResponse(item);
    }

    @Override
    @Transactional
    public TemplateTacheTacheResponse importerTache(Long templateTacheId, Long tacheId) {
        trouverEntityParId(templateTacheId);
        Tache tacheSource = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tache introuvable avec l'identifiant : " + tacheId));
        verifierTitreUnique(templateTacheId, tacheSource.getTitre());

        TemplateTacheTache item = new TemplateTacheTache();
        item.setTemplateTache(templateTacheRepository.getReferenceById(templateTacheId));
        item.setTitre(tacheSource.getTitre());
        item.setDescription(tacheSource.getDescription());
        item.setPriorite(tacheSource.getPriorite() != null ? tacheSource.getPriorite() : com.cms.tache.entity.enums.Priorite.MOYENNE);
        item.setDureeEstimeeJours(null);
        item.setCreatedBy(currentUserService.getCurrentUtilisateur());
        item.setDateCreation(LocalDateTime.now());
        item.setDateModification(LocalDateTime.now());

        item = templateTacheTacheRepository.save(item);
        logger.info("Import tache id={} dans TemplateTache id={} : itemId={}", tacheId, templateTacheId, item.getId());
        return toItemResponse(item);
    }

    @Override
    @Transactional
    public TemplateTacheTacheResponse modifierTache(Long templateTacheId, Long itemId,
                                                    UpdateTemplateTacheTacheRequest request) {
        TemplateTacheTache item = trouverItemParId(templateTacheId, itemId);
        String titre = request.getTitre().trim();
        if (!item.getTitre().equalsIgnoreCase(titre)) {
            verifierTitreUnique(templateTacheId, titre);
        }
        item.setTitre(titre);
        item.setDescription(request.getDescription());
        item.setPriorite(request.getPriorite() != null ? request.getPriorite() : com.cms.tache.entity.enums.Priorite.MOYENNE);
        item.setDureeEstimeeJours(request.getDureeEstimeeJours());
        item.setDateModification(LocalDateTime.now());

        templateTacheTacheRepository.save(item);

        logger.info("Modification tache structuree itemId={} (TemplateTache id={})", itemId, templateTacheId);
        return toItemResponse(item);
    }

    @Override
    @Transactional
    public void retirerTache(Long templateTacheId, Long itemId) {
        TemplateTacheTache item = trouverItemParId(templateTacheId, itemId);
        templateTacheTacheRepository.delete(item);
        logger.info("Retrait tache structuree itemId={} (TemplateTache id={})", itemId, templateTacheId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateTacheTacheResumeResponse> listerTaches(Long templateTacheId) {
        trouverEntityParId(templateTacheId);
        return templateTacheTacheRepository.findByTemplateTacheIdOrderByIdAsc(templateTacheId).stream()
                .map(templateTacheTacheMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    private void verifierTitreUnique(Long templateTacheId, String titre) {
        if (templateTacheTacheRepository.existsByTemplateTacheIdAndTitreIgnoreCase(templateTacheId, titre)) {
            throw new com.cms.exception.custom.BadRequestException(
                    "Une tache portant ce titre existe deja dans ce template.");
        }
    }

    private TemplateTacheTache trouverItemParId(Long templateTacheId, Long itemId) {
        TemplateTacheTache item = templateTacheTacheRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tache structuree introuvable avec l'identifiant : " + itemId));
        if (!item.getTemplateTache().getId().equals(templateTacheId)) {
            throw new ResourceNotFoundException(
                    "Tache structuree introuvable pour le template : " + templateTacheId);
        }
        return item;
    }

    private TemplateTacheTacheResponse toItemResponse(TemplateTacheTache entity) {
        return templateTacheTacheMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public int importerDansChantier(Long templateTacheId, Long chantierId) {
        TemplateTache templateTache = trouverEntityParId(templateTacheId);
        Chantier chantier = chantierRepository.findById(chantierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chantier introuvable avec l'identifiant : " + chantierId));
        verifierAccesChantier(chantier);

        Utilisateur utilisateur = currentUserService.getCurrentUtilisateur();
        List<TemplateTacheTache> sousTaches =
                templateTacheTacheRepository.findByTemplateTacheIdOrderByIdAsc(templateTacheId);

        int creees;
        if (!sousTaches.isEmpty()) {
            creees = 0;
            for (TemplateTacheTache sousTache : sousTaches) {
                Tache tache = copierVersTache(sousTache.getTitre(), sousTache.getDescription(),
                        sousTache.getPriorite(), chantier, utilisateur);
                tacheRepository.save(tache);
                creees++;
            }
        } else {
            // Aucune tache structuree : repli sur l'en-tete (valeur historique).
            Tache tache = copierVersTache(templateTache.getTitre(), templateTache.getDescription(),
                    templateTache.getPriorite(), chantier, utilisateur);
            tacheRepository.save(tache);
            creees = 1;
        }

        logger.info("Import TemplateTache id={} dans chantier id={} : {} tache(s) creee(s)",
                templateTacheId, chantierId, creees);
        return creees;
    }

    private Tache copierVersTache(String titre, String description,
                                  com.cms.tache.entity.enums.Priorite priorite, Chantier chantier,
                                  Utilisateur utilisateur) {
        Tache tache = new Tache();
        tache.setTitre(titre);
        tache.setDescription(description);
        tache.setPriorite(priorite != null ? priorite : com.cms.tache.entity.enums.Priorite.MOYENNE);
        tache.setStatus(TacheStatus.A_FAIRE);
        tache.setProgression(0);
        tache.setChantier(chantier);
        tache.setCreatedBy(utilisateur);
        tache.setDateCreation(LocalDateTime.now());
        tache.setDateModification(LocalDateTime.now());
        return tache;
    }

    private TemplateTache trouverEntityParId(Long id) {
        return templateTacheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TemplateTache introuvable avec l'identifiant : " + id));
    }

    // ------------------------------------------------------------------
    // Securite par donnees (Niveau 2)
    // ------------------------------------------------------------------

    private void verifierAccesChantier(Chantier chantier) {
        if (!accesChantierAutorise(chantier)) {
            throw ForbiddenException.generic();
        }
    }

    private boolean accesChantierAutorise(Chantier chantier) {
        Long utilisateurId = currentUserService.getCurrentUserId();
        if (estAdministrateur(utilisateurId)) {
            return true;
        }
        Set<Long> equipeIds = equipesDeUtilisateur(utilisateurId);
        if (equipeIds.isEmpty()) {
            return false;
        }
        return affectationEquipeChantierRepository.findByChantierId(chantier.getId()).stream()
                .filter(a -> a.getEquipe() != null)
                .anyMatch(a -> equipeIds.contains(a.getEquipe().getId()));
    }

    private Set<Long> equipesDeUtilisateur(Long utilisateurId) {
        return membreEquipeRepository.findByUtilisateurId(utilisateurId).stream()
                .map(MembreEquipe::getEquipe)
                .filter(e -> e != null)
                .map(e -> e.getId())
                .collect(Collectors.toSet());
    }

    private boolean estAdministrateur(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> ResourceNotFoundException.generic());
        Profil profil = utilisateur.getProfil();
        return profil != null && SystemRoles.ADMINISTRATEUR.equals(profil.getNom());
    }

    private TemplateTacheResponse toResponse(TemplateTache entity) {
        return templateTacheMapper.toResponse(entity);
    }

}
