package com.cms.tache.mapper;

import com.cms.tache.dto.CreateTacheRequest;
import com.cms.tache.dto.TacheResponse;
import com.cms.tache.dto.TacheResumeResponse;
import com.cms.tache.dto.UpdateTacheRequest;
import com.cms.tache.entity.Tache;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TacheMapper {

    @Mapping(source = "chantier.id", target = "chantierId")
    @Mapping(source = "chantier.nom", target = "chantierNom")
    TacheResponse toResponse(Tache entity);

    @Mapping(source = "chantier.id", target = "chantierId")
    TacheResumeResponse toResumeResponse(Tache entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chantier", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "progression", ignore = true)
    @Mapping(target = "dateRealisation", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "affectations", ignore = true)
    @Mapping(target = "validations", ignore = true)
    Tache toEntity(CreateTacheRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chantier", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "dateRealisation", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "affectations", ignore = true)
    @Mapping(target = "validations", ignore = true)
    void update(@MappingTarget Tache entity, UpdateTacheRequest request);
}
