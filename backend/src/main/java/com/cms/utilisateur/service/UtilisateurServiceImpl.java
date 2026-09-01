package com.cms.utilisateur.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.common.constants.AuditEvents;
import com.cms.common.constants.SystemRoles;
import com.cms.common.dto.SearchRequest;
import com.cms.exception.custom.EmailDejaUtiliseException;
import com.cms.exception.custom.ForbiddenException;
import com.cms.exception.custom.PermissionDejaAttribueeException;
import com.cms.exception.custom.PermissionIntrouvableException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.exception.custom.UtilisateurNonTrouveException;
import com.cms.permission.entity.Permission;
import com.cms.permission.repository.PermissionRepository;
import com.cms.profil.entity.Profil;
import com.cms.profil.repository.ProfilRepository;
import com.cms.security.service.CurrentUserService;
import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UpdateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurPermissionResponse;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.cms.utilisateur.dto.UtilisateurResumeResponse;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.entity.UtilisateurPermission;
import com.cms.utilisateur.entity.enums.UtilisateurPermissionType;
import com.cms.utilisateur.mapper.UtilisateurMapper;
import com.cms.utilisateur.mapper.UtilisateurPermissionMapper;
import com.cms.utilisateur.repository.UtilisateurPermissionRepository;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Implementation du service metier du module utilisateur.
 */
@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurServiceImpl.class);

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final UtilisateurPermissionRepository utilisateurPermissionRepository;
    private final UtilisateurPermissionMapper utilisateurPermissionMapper;
    private final CurrentUserService currentUserService;

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository,
                                  ProfilRepository profilRepository,
                                  UtilisateurMapper utilisateurMapper,
                                  PasswordEncoder passwordEncoder,
                                  PermissionRepository permissionRepository,
                                  UtilisateurPermissionRepository utilisateurPermissionRepository,
                                  UtilisateurPermissionMapper utilisateurPermissionMapper,
                                  CurrentUserService currentUserService) {
        this.utilisateurRepository = utilisateurRepository;
        this.profilRepository = profilRepository;
        this.utilisateurMapper = utilisateurMapper;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.utilisateurPermissionRepository = utilisateurPermissionRepository;
        this.utilisateurPermissionMapper = utilisateurPermissionMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public UtilisateurResponse creerUtilisateur(CreateUtilisateurRequest request) {
        verifierEmailDisponible(request.getEmail());

        Profil profil = resoudreProfil(request.getProfilId());

        Utilisateur utilisateur = utilisateurMapper.toEntity(request);
        utilisateur.setProfil(profil);
        utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        utilisateur.setDateCreation(LocalDateTime.now());
        utilisateur.setDateModification(LocalDateTime.now());

        utilisateur = utilisateurRepository.save(utilisateur);

        logger.info("Creation utilisateur : id={}, email={}, profil={}",
                utilisateur.getId(), utilisateur.getEmail(), profil.getNom());
        return utilisateurMapper.toResponse(utilisateur);
    }

    @Override
    @Transactional
    public UtilisateurResponse modifierUtilisateur(Long id, UpdateUtilisateurRequest request) {
        // RÃ¨gle de confidentialitÃ© : seul l'utilisateur lui-mÃªme peut modifier
        // son propre compte (un admin ne modifie pas les identifiants d'autrui).
        if (!id.equals(currentUserService.getCurrentUserId())) {
            throw ForbiddenException.generic();
        }
        Utilisateur utilisateur = trouverEntityParId(id);

        if (!utilisateur.getEmail().equals(request.getEmail())) {
            verifierEmailDisponible(request.getEmail());
        }

        utilisateurMapper.update(utilisateur, request);
        // Le changement de role/profil n'est pas permis par ce cas d'utilisation.
        utilisateur.setDateModification(LocalDateTime.now());

        utilisateur = utilisateurRepository.save(utilisateur);

        logger.info("Modification utilisateur : id={}, email={}", utilisateur.getId(), utilisateur.getEmail());
        return utilisateurMapper.toResponse(utilisateur);
    }

    @Override
    @Transactional
    public UtilisateurResponse modifierProfil(Long id, Long profilId) {
        if (profilId == null) {
            throw new ResourceNotFoundException("Le profil est obligatoire pour changer de role.");
        }
        Utilisateur utilisateur = trouverEntityParId(id);
        Profil profil = resoudreProfil(profilId);
        utilisateur.setProfil(profil);
        utilisateur.setDateModification(LocalDateTime.now());

        utilisateur = utilisateurRepository.save(utilisateur);

        logger.info("Changement de profil utilisateur : id={}, nouveauProfil={}", utilisateur.getId(), profil.getNom());
        return utilisateurMapper.toResponse(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse trouverParId(Long id) {
        return utilisateurMapper.toResponse(trouverEntityParId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse trouverParEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> UtilisateurNonTrouveException.pourEmail(email));
        return utilisateurMapper.toResponse(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResumeResponse> trouverTous() {
        return utilisateurRepository.findAll().stream()
                .map(utilisateurMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UtilisateurResumeResponse> rechercher(SearchRequest search) {
        Pageable pageable = construirePageable(search);

        Page<Utilisateur> page;
        if (search.getMotCle() == null || search.getMotCle().isBlank()) {
            page = utilisateurRepository.findAll(pageable);
        } else {
            page = rechercherParMotCle(search, pageable);
        }

        return page.map(utilisateurMapper::toResumeResponse);
    }

    @Override
    @Transactional
    public UtilisateurPermissionResponse accorderPermission(Long utilisateurId, Long permissionId) {
        return ajouterExceptionIndividuelle(utilisateurId, permissionId, UtilisateurPermissionType.ACCORDER);
    }

    @Override
    @Transactional
    public UtilisateurPermissionResponse refuserPermission(Long utilisateurId, Long permissionId) {
        return ajouterExceptionIndividuelle(utilisateurId, permissionId, UtilisateurPermissionType.REFUSER);
    }

    @Override
    @Transactional
    public void retirerPermissionIndividuelle(Long id) {
        UtilisateurPermission exception = utilisateurPermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exception de permission introuvable avec l'identifiant : " + id));
        utilisateurPermissionRepository.delete(exception);
        logger.info("AUDIT|{}|Retrait exception RBAC individuelle : id={}",
                AuditEvents.RETRAIT_PERMISSION_INDIVIDUELLE, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurPermissionResponse> listerPermissionsIndividuelles(Long utilisateurId) {
        trouverEntityParId(utilisateurId);
        return utilisateurPermissionRepository.findByUtilisateurId(utilisateurId).stream()
                .map(utilisateurPermissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------------
    // Methodes privees
    // ---------------------------------------------------------------------

    private Utilisateur trouverEntityParId(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> UtilisateurNonTrouveException.pourId(id));
    }

    private void verifierEmailDisponible(String email) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw EmailDejaUtiliseException.pourEmail(email);
        }
    }

    /**
     * Resout le profil a attribuer. Si aucun profil n'est fourni, attribue le
     * profil systeme par defaut {@code UTILISATEUR_STANDARD}.
     */
    private Profil resoudreProfil(Long profilId) {
        Profil profil;
        if (profilId != null) {
            profil = profilRepository.findById(profilId)
                    .orElseThrow(() -> new ResourceNotFoundException("Profil introuvable avec l'identifiant : " + profilId));
        } else {
            profil = profilRepository.findByNom(SystemRoles.UTILISATEUR_STANDARD)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Profil systeme introuvable : " + SystemRoles.UTILISATEUR_STANDARD));
        }
        return profil;
    }

    /**
     * Cree une exception RBAC individuelle (ACCORDER ou REFUSER) pour un
     * utilisateur, avec tracage de l'auteur de la decision.
     */
    private UtilisateurPermissionResponse ajouterExceptionIndividuelle(Long utilisateurId, Long permissionId,
                                                                       UtilisateurPermissionType type) {
        Utilisateur utilisateur = trouverEntityParId(utilisateurId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> PermissionIntrouvableException.pourId(permissionId));

        if (utilisateurPermissionRepository
                .findByUtilisateurIdAndPermissionIdAndType(utilisateurId, permissionId, type).isPresent()) {
            throw PermissionDejaAttribueeException.pourUtilisateur(utilisateurId, permissionId, type.name());
        }

        UtilisateurPermission exception = new UtilisateurPermission();
        exception.setUtilisateur(utilisateur);
        exception.setPermission(permission);
        exception.setType(type);
        exception.setDateCreation(LocalDateTime.now());
        exception.setDateModification(LocalDateTime.now());
        exception.setCreatedBy(currentUserService.getCurrentUtilisateur());
        exception = utilisateurPermissionRepository.save(exception);

        logger.info("AUDIT|{}|Exception RBAC individuelle : utilisateurId={}, permissionId={}, type={}",
                type == UtilisateurPermissionType.ACCORDER ? AuditEvents.ACCORD_PERMISSION : AuditEvents.REFUS_PERMISSION,
                utilisateurId, permissionId, type);
        return utilisateurPermissionMapper.toResponse(exception);
    }

    private Page<Utilisateur> rechercherParMotCle(SearchRequest search, Pageable pageable) {
        String motCle = search.getMotCle().toLowerCase().trim();
        List<Utilisateur> filtrer = utilisateurRepository.findAll().stream()
                .filter(u -> u.getNom().toLowerCase().contains(motCle)
                        || u.getPrenom().toLowerCase().contains(motCle)
                        || u.getEmail().toLowerCase().contains(motCle))
                .collect(Collectors.toList());

        int debut = Math.min((int) pageable.getOffset(), filtrer.size());
        int fin = Math.min(debut + pageable.getPageSize(), filtrer.size());
        List<Utilisateur> contenu = filtrer.subList(debut, fin);

        return new PageImpl<>(contenu, pageable, filtrer.size());
    }

    private Pageable construirePageable(SearchRequest search) {
        if (search.getSort() == null || search.getSort().isBlank()) {
            return PageRequest.of(search.getPage(), search.getSize());
        }
        Sort.Direction direction = "DESC".equalsIgnoreCase(search.getDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(search.getPage(), search.getSize(), Sort.by(direction, search.getSort()));
    }

}
