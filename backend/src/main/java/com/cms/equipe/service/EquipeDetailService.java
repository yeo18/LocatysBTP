package com.cms.equipe.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cms.chantier.repository.AffectationEquipeChantierRepository;
import com.cms.equipe.dto.AffectationEquipeChantierResponse;
import com.cms.equipe.dto.EquipeDetailResponse;
import com.cms.equipe.dto.MembreEquipeResponse;
import com.cms.equipe.entity.AffectationEquipeChantier;
import com.cms.equipe.entity.Equipe;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.equipe.mapper.AffectationEquipeChantierMapper;
import com.cms.equipe.mapper.MembreEquipeMapper;
import com.cms.equipe.repository.EquipeRepository;
import com.cms.equipe.repository.MembreEquipeRepository;
import com.cms.security.service.DataAccessService;

/**
 * Fournit la liste des equipes enrichie (membres + chantier actif) en un
 * nombre reduit de requetes (1 equipe + 2 groupes IN) au lieu d'un N+1.
 */
@Service
public class EquipeDetailService {

    private final EquipeRepository equipeRepository;
    private final MembreEquipeRepository membreEquipeRepository;
    private final AffectationEquipeChantierRepository affectationEquipeChantierRepository;
    private final MembreEquipeMapper membreEquipeMapper;
    private final AffectationEquipeChantierMapper affectationEquipeChantierMapper;
    private final DataAccessService dataAccessService;

    public EquipeDetailService(EquipeRepository equipeRepository,
                               MembreEquipeRepository membreEquipeRepository,
                               AffectationEquipeChantierRepository affectationEquipeChantierRepository,
                               MembreEquipeMapper membreEquipeMapper,
                               AffectationEquipeChantierMapper affectationEquipeChantierMapper,
                               DataAccessService dataAccessService) {
        this.equipeRepository = equipeRepository;
        this.membreEquipeRepository = membreEquipeRepository;
        this.affectationEquipeChantierRepository = affectationEquipeChantierRepository;
        this.membreEquipeMapper = membreEquipeMapper;
        this.affectationEquipeChantierMapper = affectationEquipeChantierMapper;
        this.dataAccessService = dataAccessService;
    }

    @Transactional(readOnly = true)
    public List<EquipeDetailResponse> listerDetaillees() {
        List<Equipe> equipes = equipeRepository.findAll();
        if (!dataAccessService.estAdministrateur()) {
            Set<Long> ids = dataAccessService.equipesDeUtilisateur();
            equipes = equipes.stream()
                    .filter(e -> ids.contains(e.getId()))
                    .collect(Collectors.toList());
        }
        if (equipes.isEmpty()) {
            return List.of();
        }
        List<Long> ids = equipes.stream().map(Equipe::getId).collect(Collectors.toList());

        Map<Long, List<MembreEquipe>> membresParEquipe = membreEquipeRepository.findByEquipeIdIn(ids).stream()
                .collect(Collectors.groupingBy(m -> m.getEquipe().getId()));
        Map<Long, List<AffectationEquipeChantier>> affectationsParEquipe =
                affectationEquipeChantierRepository.findByEquipeIdIn(ids).stream()
                        .collect(Collectors.groupingBy(a -> a.getEquipe().getId()));

        return equipes.stream().map(e -> {
            EquipeDetailResponse response = new EquipeDetailResponse();
            response.setId(e.getId());
            response.setNom(e.getNom());
            response.setDescription(e.getDescription());
            response.setMembres(membresParEquipe.getOrDefault(e.getId(), List.of()).stream()
                    .map(membreEquipeMapper::toResponse)
                    .collect(Collectors.toList()));
            response.setAffectations(affectationsParEquipe.getOrDefault(e.getId(), List.of()).stream()
                    .map(affectationEquipeChantierMapper::toResponse)
                    .collect(Collectors.toList()));
            return response;
        }).collect(Collectors.toList());
    }
}