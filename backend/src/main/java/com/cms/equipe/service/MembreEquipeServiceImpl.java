package com.cms.equipe.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.equipe.dto.MembreEquipeResponse;
import com.cms.equipe.dto.MembreEquipeResumeResponse;
import com.cms.equipe.entity.Equipe;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.equipe.entity.enums.RoleDansEquipe;
import com.cms.equipe.mapper.MembreEquipeMapper;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.exception.custom.BadRequestException;
import com.cms.exception.custom.DuplicateResourceException;
import com.cms.exception.custom.ResourceNotFoundException;
import com.cms.security.service.DataAccessService;
import com.cms.utilisateur.entity.Utilisateur;
import com.cms.utilisateur.repository.UtilisateurRepository;

/**
 * Implementation du service metier des appartenances utilisateur - equipe.
 */
@Service
public class MembreEquipeServiceImpl implements MembreEquipeService {

    private static final Logger logger = LoggerFactory.getLogger(MembreEquipeServiceImpl.class);

    private final MembreEquipeRepository membreEquipeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EquipeRepository equipeRepository;
    private final MembreEquipeMapper membreEquipeMapper;
    private final DataAccessService dataAccessService;

    public MembreEquipeServiceImpl(MembreEquipeRepository membreEquipeRepository,
                                   UtilisateurRepository utilisateurRepository,
                                   EquipeRepository equipeRepository,
                                   MembreEquipeMapper membreEquipeMapper,
                                   DataAccessService dataAccessService) {
        this.membreEquipeRepository = membreEquipeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.equipeRepository = equipeRepository;
        this.membreEquipeMapper = membreEquipeMapper;
        this.dataAccessService = dataAccessService;
    }

    @Override
    @Transactional
    public MembreEquipeResponse integrer(Long equipeId, Long utilisateurId, RoleDansEquipe role, LocalDate dateIntegration) {
        if (role == null) {
            throw new BadRequestException("Le role dans l'equipe est obligatoire (CHEF ou OUVRIER)");
        }
        if (dateIntegration == null) {
            throw new BadRequestException("La date d'integration est obligatoire");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'identifiant : " + utilisateurId));
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe introuvable avec l'identifiant : " + equipeId));

        boolean dejaMembre = membreEquipeRepository.findByEquipeId(equipeId).stream()
                .anyMatch(m -> m.getUtilisateur().getId().equals(utilisateurId));
        if (dejaMembre) {
            throw DuplicateResourceException.pourConflit(
                    "appartenance (utilisateur, equipe)", utilisateurId + ", " + equipeId);
        }

        MembreEquipe membre = new MembreEquipe();
        membre.setUtilisateur(utilisateur);
        membre.setEquipe(equipe);
        membre.setRoleDansEquipe(role);
        membre.setDateIntegration(dateIntegration);

        membre = membreEquipeRepository.save(membre);

        logger.info("Ajout membre : equipeId={}, utilisateurId={}, role={}",
                equipeId, utilisateurId, role);
        return membreEquipeMapper.toResponse(membre);
    }

    @Override
    @Transactional
    public MembreEquipeResponse changerRole(Long membreEquipeId, RoleDansEquipe role) {
        if (role == null) {
            throw new BadRequestException("Le role dans l'equipe est obligatoire (CHEF ou OUVRIER)");
        }
        MembreEquipe membre = trouverEntityParId(membreEquipeId);
        membre.setRoleDansEquipe(role);
        membre = membreEquipeRepository.save(membre);

        logger.info("Changement role membre : id={}, role={}", membre.getId(), role);
        return membreEquipeMapper.toResponse(membre);
    }

    @Override
    @Transactional
    public void retirer(Long membreEquipeId) {
        MembreEquipe membre = trouverEntityParId(membreEquipeId);
        membreEquipeRepository.delete(membre);

        logger.info("Retrait membre : id={}, equipeId={}, utilisateurId={}",
                membre.getId(), membre.getEquipe().getId(), membre.getUtilisateur().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembreEquipeResponse> listerMembres(Long equipeId) {
        equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe introuvable avec l'identifiant : " + equipeId));
        dataAccessService.verifierAccesEquipe(equipeId);
        return membreEquipeRepository.findByEquipeId(equipeId).stream()
                .map(membreEquipeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembreEquipeResumeResponse> listerEquipesDeUtilisateur(Long utilisateurId) {
        return membreEquipeRepository.findByUtilisateurId(utilisateurId).stream()
                .map(membreEquipeMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    private MembreEquipe trouverEntityParId(Long id) {
        return membreEquipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appartenance a une equipe introuvable avec l'identifiant : " + id));
    }

}
