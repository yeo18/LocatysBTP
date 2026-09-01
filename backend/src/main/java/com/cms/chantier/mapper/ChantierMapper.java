package com.cms.chantier.mapper;

import com.cms.chantier.dto.ChantierResponse;
import com.cms.chantier.dto.ChantierResumeResponse;
import com.cms.chantier.dto.CreateChantierRequest;
import com.cms.chantier.dto.UpdateChantierRequest;
import com.cms.chantier.entity.Chantier;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ChantierMapper {

    @Mapping(source = "responsable.id", target = "responsableId")
    @Mapping(source = "responsable.nom", target = "responsableNom")
    ChantierResponse toResponse(Chantier entity);

    ChantierResumeResponse toResumeResponse(Chantier entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "taches", ignore = true)
    @Mapping(target = "affectationEquipeChantiers", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "adresseGeocodee", ignore = true)
    @Mapping(target = "origineCoordonnees", ignore = true)
    @Mapping(target = "fiabiliteCoordonnees", ignore = true)
    Chantier toEntity(CreateChantierRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "responsable", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "taches", ignore = true)
    @Mapping(target = "affectationEquipeChantiers", ignore = true)
    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "adresseGeocodee", ignore = true)
    @Mapping(target = "origineCoordonnees", ignore = true)
    @Mapping(target = "fiabiliteCoordonnees", ignore = true)
    void update(@MappingTarget Chantier entity, UpdateChantierRequest request);
}
