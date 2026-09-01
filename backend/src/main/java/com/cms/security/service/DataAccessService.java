package com.cms.security.service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cms.chantier.entity.AffectationUtilisateurChantier;
import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.chantier.repository.AffectationUtilisateurChantierRepository;
import com.cms.chantier.repository.UtilisateurPermissionChantierRepository;
import com.cms.common.constants.SystemRoles;
import com.cms.equipe.entity.AffectationEquipeChantier;
import com.cms.equipe.entity.enums.AffectationEquipeChantierStatut;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.exception.custom.ForbiddenException;
import com.cms.profil.entity.Profil;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * RÃ¨gle gÃ©nÃ©rale d'accÃ¨s aux donnÃ©es : un utilisateur ne voit que ce qui
 * le concerne, rien de plus. L'administrateur voit tout.
 *
 * <p>Centralise un niveau de sÃ©curitÃ© par donnÃ©es (Niveau 2) utilisÃ© par
 * toutes les couches Service : Ã©quipes visibles = Ã©quipes dont l'utilisateur
 * est membre ; tÃ¢ches visibles = tÃ¢ches affectÃ©es Ã  l'utilisateur ou Ã  l'une
 * de ses Ã©quipes.
 */
@Service
public class DataAccessService {

    private final CurrentUserService currentUserService;
    private final UtilisateurRepository utilisateurRepository;
    private final MembreEquipeRepository membreEquipeRepository;
    private final AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    private final AffectationUtilisateurChantierRepository affectationUtilisateurChantierRepository;
    private final UtilisateurPermissionChantierRepository utilisateurPermissionChantierRepository;

    public DataAccessService(CurrentUserService currentUserService,
                             UtilisateurRepository utilisateurRepository,
                             MembreEquipeRepository membreEquipeRepository,
                             AffectationEquipeChantierRepository affectationEquipeChantierRepository,
                             AffectationUtilisateurChantierRepository affectationUtilisateurChantierRepository,
                             UtilisateurPermissionChantierRepository utilisateurPermissionChantierRepository) {
        this.currentUserService = currentUserService;
        this.utilisateurRepository = utilisateurRepository;
        this.membreEquipeRepository = membreEquipeRepository;
        this.affectationEquipeChantierRepository = affectationEquipeChantierRepository;
        this.affectationUtilisateurChantierRepository = affectationUtilisateurChantierRepository;
        this.utilisateurPermissionChantierRepository = utilisateurPermissionChantierRepository;
    }

    /** L'utilisateur courant est-il administrateur (visibilitÃ© totale) ? */
    public boolean estAdministrateur() {
        Long utilisateurId = currentUserService.getCurrentUserId();
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(ForbiddenException::generic);
        Profil profil = utilisateur.getProfil();
        return profil != null && SystemRoles.ADMINISTRATEUR.equals(profil.getNom());
    }

    /** Ensemble des id d'Ã©quipes dont l'utilisateur courant est membre. */
    public Set<Long> equipesDeUtilisateur() {
        Long utilisateurId = currentUserService.getCurrentUserId();
        return membreEquipeRepository.findByUtilisateurId(utilisateurId).stream()
                .map(MembreEquipe::getEquipe)
                .filter(e -> e != null)
                .map(e -> e.getId())
                .collect(Collectors.toSet());
    }

    /** L'utilisateur courant a-t-il accÃ¨s Ã  l'Ã©quipe donnÃ©e ? */
    public boolean accesEquipe(Long equipeId) {
        return estAdministrateur() || equipesDeUtilisateur().contains(equipeId);
    }

    /** VÃ©rifie l'accÃ¨s Ã  l'Ã©quipe et lÃ¨ve un 403 sinon. */
    public void verifierAccesEquipe(Long equipeId) {
        if (!accesEquipe(equipeId)) {
            throw ForbiddenException.generic();
        }
    }

    /** Ensemble des id de chantiers accessibles : chantiers oÃ¹ une Ã©quipe de
     * l'utilisateur est affectÃ©e (affectation ACTIVE) OU chantiers oÃ¹
     * l'utilisateur est affectÃ© directement. */
    public Set<Long> chantiersDeUtilisateur() {
        Long utilisateurId = currentUserService.getCurrentUserId();
        Set<Long> result = new HashSet<>();

        Set<Long> equipeIds = equipesDeUtilisateur();
        if (!equipeIds.isEmpty()) {
            affectationEquipeChantierRepository.findByEquipeIdIn(equipeIds).stream()
                    .filter(a -> a.getStatut() == AffectationEquipeChantierStatut.ACTIVE)
                    .map(AffectationEquipeChantier::getChantier)
                    .filter(c -> c != null)
                    .map(c -> c.getId())
                    .forEach(result::add);
        }

        affectationUtilisateurChantierRepository.findByUtilisateurId(utilisateurId).stream()
                .map(AffectationUtilisateurChantier::getChantier)
                .filter(c -> c != null)
                .map(c -> c.getId())
                .forEach(result::add);

        // Chantiers oÃ¹ l'utilisateur dÃ©tient une permission scopÃ©e ACCORDER
        // (ex : chef de ce chantier prÃ©cis) => il les voit.
        utilisateurPermissionChantierRepository
                .findByUtilisateurIdAndType(utilisateurId, UtilisateurPermissionType.ACCORDER).stream()
                .map(e -> e.getChantier())
                .filter(c -> c != null)
                .map(c -> c.getId())
                .forEach(result::add);

        return result;
    }

    /** L'utilisateur courant a-t-il accÃ¨s au chantier donnÃ© ? */
    public boolean accesChantier(Long chantierId) {
        return estAdministrateur() || chantiersDeUtilisateur().contains(chantierId);
    }

    /** VÃ©rifie l'accÃ¨s au chantier et lÃ¨ve un 403 sinon. */
    public void verifierAccesChantier(Long chantierId) {
        if (!accesChantier(chantierId)) {
            throw ForbiddenException.generic();
        }
    }
}