package com.cms.security.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.entity.AffectationUtilisateurChantier;
import com.cms.chantier.entity.Chantier;
import com.cms.chantier.repository.AffectationUtilisateurChantierRepository;
import com.cms.chantier.repository.UtilisateurPermissionChantierRepository;
import com.cms.common.constants.SystemRoles;
import com.cms.exception.custom.UtilisateurNonTrouveException;
import com.cms.permission.entity.Permission;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.entity.Profil;
import com.cms.profil.entity.ProfilPermission;
import com.cms.profil.repository.ProfilPermissionRepository;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.entity.UtilisateurPermission;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;
import com.cms.utilisateur.repository.UtilisateurPermissionRepository;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Moteur de calcul des droits effectifs d'un utilisateur (RBAC dynamique).
 *
 * <p>Formule officielle :
 * <pre>
 * Droits effectifs =
 *   (Permissions du Profil)
 *   + (Permissions ACCORDER)
 *   - (Permissions REFUSER)     // REFUSER prioritaire
 * </pre>
 *
 * <p>Le calcul est effectue a chaque appel, directement depuis PostgreSQL
 * (aucun stockage en JWT, aucun cache). Une modification des octrois est donc
 * effective immediatement, sans reconnexion.
 *
 * <p>Regle documentee du modele de donnees : le profil systeme
 * {@code ADMINISTRATEUR} possede l'ensemble des permissions du referentiel
 * (les codes proviennent toujours de la base). Un REFUSER individuel reste
 * prioritaire, meme pour ce profil.
 */
@Service
public class DroitsService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilPermissionRepository profilPermissionRepository;
    private final UtilisateurPermissionRepository utilisateurPermissionRepository;
    private final UtilisateurPermissionChantierRepository utilisateurPermissionChantierRepository;
    private final AffectationUtilisateurChantierRepository affectationUtilisateurChantierRepository;
    private final PermissionRepository permissionRepository;

    public DroitsService(UtilisateurRepository utilisateurRepository,
                         ProfilPermissionRepository profilPermissionRepository,
                         UtilisateurPermissionRepository utilisateurPermissionRepository,
                         UtilisateurPermissionChantierRepository utilisateurPermissionChantierRepository,
                         AffectationUtilisateurChantierRepository affectationUtilisateurChantierRepository,
                         PermissionRepository permissionRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.profilPermissionRepository = profilPermissionRepository;
        this.utilisateurPermissionRepository = utilisateurPermissionRepository;
        this.utilisateurPermissionChantierRepository = utilisateurPermissionChantierRepository;
        this.affectationUtilisateurChantierRepository = affectationUtilisateurChantierRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Calcule l'ensemble des codes de permissions effectifs d'un utilisateur.
     *
     * @param utilisateurId identifiant de l'utilisateur
     * @return ensemble de codes (ex : TACHE_LIRE)
     */
    @Transactional(readOnly = true)
    public Set<String> calculerDroits(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> UtilisateurNonTrouveException.pourId(utilisateurId));
        Profil profil = utilisateur.getProfil();

        Set<String> droits = new HashSet<>(permissionsDuProfil(profil));

        for (UtilisateurPermission exception : utilisateurPermissionRepository.findByUtilisateurId(utilisateurId)) {
            String code = exception.getPermission().getNomPermission();
            if (exception.getType() == UtilisateurPermissionType.ACCORDER) {
                droits.add(code);
            } else {
                droits.remove(code);
            }
        }
        return droits;
    }

    private Set<String> permissionsDuProfil(Profil profil) {
        if (SystemRoles.ADMINISTRATEUR.equals(profil.getNom())) {
            return permissionRepository.findAll().stream()
                    .map(Permission::getNomPermission)
                    .collect(Collectors.toSet());
        }
        return profilPermissionRepository.findByProfilId(profil.getId()).stream()
                .map(ProfilPermission::getPermission)
                .map(Permission::getNomPermission)
                .collect(Collectors.toSet());
    }

    // ---------------------------------------------------------------------
    // Droits scopÃ©s par chantier
    // ---------------------------------------------------------------------

    /**
     * Calcule les droits effectifs d'un utilisateur dans le pÃ©rimÃ¨tre d'un
     * chantier prÃ©cis.
     *
     * <p>Formule :
     * <pre>
     * Droits sur le chantier =
     *   (Permissions du Profil)
     *   + (Perm. globales ACCORDER)
     *   + (Perm. scopÃ©es sur CE chantier => ACCORDER)
     *   - (Perm. globales REFUSER)
     *   - (Perm. scopÃ©es sur CE chantier => REFUSER)   // REFUSER prioritaire
     * </pre>
     *
     * @param utilisateurId identifiant de l'utilisateur
     * @param chantierId    identifiant du chantier
     * @return ensemble de codes (ex : CHANTIER_MODIFIER) valables pour ce chantier
     */
@Transactional(readOnly = true)
    public Set<String> calculerDroitsSurChantier(Long utilisateurId, Long chantierId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> UtilisateurNonTrouveException.pourId(utilisateurId));
        Profil profil = utilisateur.getProfil();

        Set<String> droits = new HashSet<>(permissionsDuProfil(profil));
        appliquerExceptions(droits, utilisateurPermissionRepository.findByUtilisateurId(utilisateurId));

        // Profil de la relation ternaire (affectation utilisateur <-> chantier) :
        // il s'applique uniquement si la periode courante est valide, et ses
        // permissions s'ajoutent a celles du profil global.
        Profil profilDuChantier = profilActifSurChantier(utilisateurId, chantierId);
        if (profilDuChantier != null) {
            droits.addAll(permissionsDuProfil(profilDuChantier));
        }

        // Exceptions scopées au chantier donné uniquement.
        utilisateurPermissionChantierRepository
                .findByChantierIdAndUtilisateurId(chantierId, utilisateurId)
                .forEach(e -> {
                    String code = e.getPermission().getNomPermission();
                    if (e.getType() == UtilisateurPermissionType.ACCORDER) {
                        droits.add(code);
                    } else {
                        droits.remove(code);
                    }
                });
        return droits;
    }

    private void appliquerExceptions(Set<String> droits,
                                     java.util.List<UtilisateurPermission> exceptions) {
        for (UtilisateurPermission exception : exceptions) {
            String code = exception.getPermission().getNomPermission();
            if (exception.getType() == UtilisateurPermissionType.ACCORDER) {
                droits.add(code);
            } else {
                droits.remove(code);
            }
        }
    }

    /**
     * Calcule les droits effectifs par chantier pour un utilisateur.
     *
     * <p>Renvoie une map {@code chantierId -> droits effectifs} qui ne contient
     * que les chantiers oÃ¹ l'utilisateur possÃ¨de au moins une exception scopÃ©e
     * (ACCORDER ou REFUSER). Pour chaque chantier, le calcul repart des droits
     * globaux (profil + exceptions globales) puis applique les exceptions
     * scopÃ©es Ã  CET chantier. Un chantier sans exception scopÃ©e n'apparaÃ®t pas
     * dans la map (droits globaux suffisent).
     *
     * @param utilisateurId identifiant de l'utilisateur
     * @return map chantierId -> ensemble de codes de permissions
     */
    @Transactional(readOnly = true)
    public java.util.Map<Long, Set<String>> calculerDroitsParChantier(Long utilisateurId) {
        var scopeeParChantier = utilisateurPermissionChantierRepository.findByUtilisateurId(utilisateurId);

        Set<Long> chantiers = new HashSet<>();
        for (var exception : scopeeParChantier) {
            if (exception.getChantier() != null) {
                chantiers.add(exception.getChantier().getId());
            }
        }
        // Chantiers où l'utilisateur détient un profil via la relation ternaire
        // (affectation utilisateur <-> chantier) pendant la période en cours.
        affectationUtilisateurChantierRepository.findByUtilisateurId(utilisateurId).stream()
                .filter(this::periodeActive)
                .map(AffectationUtilisateurChantier::getChantier)
                .filter(c -> c != null)
                .map(Chantier::getId)
                .forEach(chantiers::add);

        if (chantiers.isEmpty()) {
            return Map.of();
        }

        var resultat = new java.util.HashMap<Long, Set<String>>();
        for (Long chantierId : chantiers) {
            resultat.computeIfAbsent(chantierId, k -> calculerDroitsSurChantier(utilisateurId, chantierId));
        }
        for (var exception : scopeeParChantier) {
            Chantier chantier = exception.getChantier();
            if (chantier == null) {
                continue;
            }
            Long chantierId = chantier.getId();
            Set<String> droits = resultat.computeIfAbsent(chantierId,
                    k -> calculerDroitsSurChantier(utilisateurId, chantierId));
            String code = exception.getPermission().getNomPermission();
            if (exception.getType() == UtilisateurPermissionType.ACCORDER) {
                droits.add(code);
            } else {
                droits.remove(code);
            }
        }
        return resultat;
    }

    /**
     * Profil actif de l'utilisateur sur un chantier (relation ternaire), ou
     * {@code null} si aucune affectation n'est en cours de période.
     */
    private Profil profilActifSurChantier(Long utilisateurId, Long chantierId) {
        return affectationUtilisateurChantierRepository
                .findByUtilisateurIdAndChantierId(utilisateurId, chantierId).stream()
                .filter(this::periodeActive)
                .map(AffectationUtilisateurChantier::getProfil)
                .findFirst()
                .orElse(null);
    }

    private boolean periodeActive(AffectationUtilisateurChantier affectation) {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debut = affectation.getDateDebut();
        LocalDateTime fin = affectation.getDateFin();
        if (debut != null && debut.isAfter(maintenant)) {
            return false;
        }
        return fin == null || !fin.isBefore(maintenant);
    }
}
