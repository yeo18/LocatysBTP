package com.cms.equipe.mapper;

import com.cms.equipe.dto.CreateEquipeRequest;
import com.cms.equipe.dto.EquipeResponse;
import com.cms.equipe.dto.EquipeResumeResponse;
import com.cms.equipe.dto.UpdateEquipeRequest;
import com.cms.equipe.entity.Equipe;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EquipeMapper {

    EquipeResponse toResponse(Equipe entity);

    EquipeResumeResponse toResumeResponse(Equipe entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "membres", ignore = true)
    @Mapping(target = "affectationsChantiers", ignore = true)
    @Mapping(target = "affectationTaches", ignore = true)
    Equipe toEntity(CreateEquipeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "membres", ignore = true)
    @Mapping(target = "affectationsChantiers", ignore = true)
    @Mapping(target = "affectationTaches", ignore = true)
    void update(@MappingTarget Equipe entity, UpdateEquipeRequest request);
}
