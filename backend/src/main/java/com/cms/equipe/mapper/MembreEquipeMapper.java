package com.cms.equipe.mapper;

import com.cms.equipe.dto.CreateMembreEquipeRequest;
import com.cms.equipe.dto.MembreEquipeResponse;
import com.cms.equipe.dto.MembreEquipeResumeResponse;
import com.cms.equipe.dto.UpdateMembreEquipeRequest;
import com.cms.equipe.entity.MembreEquipe;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MembreEquipeMapper {

    @Mapping(source = "utilisateur.id", target = "utilisateurId")
    @Mapping(source = "utilisateur.nom", target = "utilisateurNom")
    @Mapping(source = "utilisateur.prenom", target = "utilisateurPrenom")
    @Mapping(source = "equipe.id", target = "equipeId")
    @Mapping(source = "equipe.nom", target = "equipeNom")
    MembreEquipeResponse toResponse(MembreEquipe entity);

    @Mapping(source = "utilisateur.id", target = "utilisateurId")
    @Mapping(source = "utilisateur.nom", target = "utilisateurNom")
    @Mapping(source = "equipe.id", target = "equipeId")
    @Mapping(source = "equipe.nom", target = "equipeNom")
    MembreEquipeResumeResponse toResumeResponse(MembreEquipe entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "equipe", ignore = true)
    MembreEquipe toEntity(CreateMembreEquipeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "equipe", ignore = true)
    void update(@MappingTarget MembreEquipe entity, UpdateMembreEquipeRequest request);
}
