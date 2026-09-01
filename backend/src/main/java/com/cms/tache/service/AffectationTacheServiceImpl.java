package com.cms.tache.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.equipe.entity.Equipe;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.tache.dto.AffectationTacheResponse;
import com.cms.tache.dto.CreateAffectationTacheRequest;
import com.cms.tache.dto.UpdateAffectationTacheRequest;
import com.cms.tache.entity.AffectationTacheEquipe;
import com.cms.tache.entity.AffectationTacheUtilisateur;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.enums.AffectationTacheRole;
import com.cms.tache.mapper.AffectationTacheMapper;
import com.cms.tache.repository.AffectationTacheEquipeRepository;
import com.cms.tache.repository.AffectationTacheUtilisateurRepository;
import com.cms.tache.repository.TacheRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Implementation du service des affectations de taches.
 *
 * <p>Une tache peut etre affectee a un utilisateur (table
 * {@code affectation_tache_utilisateur}) OU a une equipe (table
 * {@code affectation_tache_equipe}) : deux relations binaires independantes.
 */
@Service
public class AffectationTacheServiceImpl implements AffectationTacheService {

    private static final Logger logger = LoggerFactory.getLogger(AffectationTacheServiceImpl.class);

    private final AffectationTacheUtilisateurRepository affectationUtilisateurRepository;
    private final AffectationTacheEquipeRepository affectationEquipeRepository;
    private final TacheRepository tacheRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EquipeRepository equipeRepository;
    private final AffectationTacheMapper affectationTacheMapper;

    public AffectationTacheServiceImpl(AffectationTacheUtilisateurRepository affectationUtilisateurRepository,
                                       AffectationTacheEquipeRepository affectationEquipeRepository,
                                       TacheRepository tacheRepository,
                                       UtilisateurRepository utilisateurRepository,
                                       EquipeRepository equipeRepository,
                                       AffectationTacheMapper affectationTacheMapper) {
        this.affectationUtilisateurRepository = affectationUtilisateurRepository;
        this.affectationEquipeRepository = affectationEquipeRepository;
        this.tacheRepository = tacheRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.equipeRepository = equipeRepository;
        this.affectationTacheMapper = affectationTacheMapper;
    }

    @Override
    @Transactional
    public AffectationTacheResponse assignTache(CreateAffectationTacheRequest request) {
        Tache tache = tacheRepository.findById(request.getTacheId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tache introuvable avec l'identifiant : " + request.getTacheId()));

        AffectationTacheRole role = request.getRole();
        LocalDate date = request.getDateAffectation();

        if (request.getUtilisateurId() != null) {
            verifierDoublonUtilisateur(request.getTacheId(), request.getUtilisateurId(), role, null);
            Utilisateur utilisateur = chargerUtilisateur(request.getUtilisateurId());
            AffectationTacheUtilisateur at = new AffectationTacheUtilisateur();
            at.setTache(tache);
            at.setUtilisateur(utilisateur);
            at.setRole(role);
            at.setDateAffectation(date);
            at.setDateCreation(LocalDateTime.now());
            at.setDateModification(LocalDateTime.now());
            at = affectationUtilisateurRepository.save(at);
            logger.info("Affectation tache (utilisateur) : tacheId={}, utilisateurId={}, role={}",
                    tache.getId(), utilisateur.getId(), role);
            return affectationTacheMapper.toResponse(at);
        }

        if (request.getEquipeId() != null) {
            verifierDoublonEquipe(request.getTacheId(), request.getEquipeId(), role, null);
            Equipe equipe = chargerEquipe(request.getEquipeId());
            AffectationTacheEquipe at = new AffectationTacheEquipe();
            at.setTache(tache);
            at.setEquipe(equipe);
            at.setRole(role);
            at.setDateAffectation(date);
            at = affectationEquipeRepository.save(at);
            logger.info("Affectation tache (equipe) : tacheId={}, equipeId={}, role={}",
                    tache.getId(), equipe.getId(), role);
            return affectationTacheMapper.toResponse(at);
        }

        throw new BadRequestException("Une affectation doit cibler un utilisateur ou une equipe");
    }

    @Override
    @Transactional
    public AffectationTacheResponse updateAffectation(Long id, UpdateAffectationTacheRequest request) {
        if (request.getUtilisateurId() != null) {
            AffectationTacheUtilisateur at = affectationUtilisateurRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Affectation introuvable avec l'identifiant : " + id));
            verifierDoublonUtilisateur(at.getTache().getId(), request.getUtilisateurId(), request.getRole(), id);
            at.setUtilisateur(chargerUtilisateur(request.getUtilisateurId()));
            at.setRole(request.getRole());
            at.setDateAffectation(request.getDateAffectation());
            at.setDateModification(LocalDateTime.now());
            at = affectationUtilisateurRepository.save(at);
            return affectationTacheMapper.toResponse(at);
        }

        if (request.getEquipeId() != null) {
            AffectationTacheEquipe at = affectationEquipeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Affectation introuvable avec l'identifiant : " + id));
            verifierDoublonEquipe(at.getTache().getId(), request.getEquipeId(), request.getRole(), id);
            at.setEquipe(chargerEquipe(request.getEquipeId()));
            at.setRole(request.getRole());
            at.setDateAffectation(request.getDateAffectation());
            at = affectationEquipeRepository.save(at);
            return affectationTacheMapper.toResponse(at);
        }

        throw new BadRequestException("Une affectation doit cibler un utilisateur ou une equipe");
    }

    @Override
    @Transactional
    public void removeAffectation(Long id) {
        affectationUtilisateurRepository.deleteById(id);
        affectationEquipeRepository.deleteById(id);
        logger.info("Suppression affectation : id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AffectationTacheResponse> findByTacheId(Long tacheId) {
        List<AffectationTacheResponse> result = new ArrayList<>();
        affectationUtilisateurRepository.findByTacheId(tacheId).stream()
                .map(affectationTacheMapper::toResponse)
                .forEach(result::add);
        affectationEquipeRepository.findByTacheId(tacheId).stream()
                .map(affectationTacheMapper::toResponse)
                .forEach(result::add);
        return result;
    }

    private Utilisateur chargerUtilisateur(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur introuvable avec l'identifiant : " + utilisateurId));
    }

    private Equipe chargerEquipe(Long equipeId) {
        return equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Equipe introuvable avec l'identifiant : " + equipeId));
    }

    private void verifierDoublonUtilisateur(Long tacheId, Long utilisateurId, AffectationTacheRole role, Long excluId) {
        boolean doublon = affectationUtilisateurRepository.findByTacheId(tacheId).stream()
                .filter(a -> excluId == null || !excluId.equals(a.getId()))
                .anyMatch(a -> a.getRole() == role
                        && a.getUtilisateur() != null
                        && utilisateurId.equals(a.getUtilisateur().getId()));
        if (doublon) {
            throw DuplicateResourceException.pourConflit("affectation",
                    "tache " + tacheId + " / role " + role);
        }
    }

    private void verifierDoublonEquipe(Long tacheId, Long equipeId, AffectationTacheRole role, Long excluId) {
        boolean doublon = affectationEquipeRepository.findByTacheId(tacheId).stream()
                .filter(a -> excluId == null || !excluId.equals(a.getId()))
                .anyMatch(a -> a.getRole() == role
                        && a.getEquipe() != null
                        && equipeId.equals(a.getEquipe().getId()));
        if (doublon) {
            throw DuplicateResourceException.pourConflit("affectation",
                    "tache " + tacheId + " / role " + role);
        }
    }
}
