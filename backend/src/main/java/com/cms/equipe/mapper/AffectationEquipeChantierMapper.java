package com.cms.equipe.mapper;

import com.cms.equipe.dto.AffectationEquipeChantierResponse;
import com.cms.equipe.dto.AffectationEquipeChantierResumeResponse;
import com.cms.equipe.dto.CreateAffectationEquipeChantierRequest;
import com.cms.equipe.dto.UpdateAffectationEquipeChantierRequest;
import com.cms.equipe.entity.AffectationEquipeChantier;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AffectationEquipeChantierMapper {

    @Mapping(source = "equipe.id", target = "equipeId")
    @Mapping(source = "equipe.nom", target = "equipeNom")
    @Mapping(source = "chantier.id", target = "chantierId")
    @Mapping(source = "chantier.nom", target = "chantierNom")
    AffectationEquipeChantierResponse toResponse(AffectationEquipeChantier entity);

    @Mapping(source = "equipe.nom", target = "equipeNom")
    @Mapping(source = "chantier.nom", target = "chantierNom")
    AffectationEquipeChantierResumeResponse toResumeResponse(AffectationEquipeChantier entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipe", ignore = true)
    @Mapping(target = "chantier", ignore = true)
    AffectationEquipeChantier toEntity(CreateAffectationEquipeChantierRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipe", ignore = true)
    @Mapping(target = "chantier", ignore = true)
    void update(@MappingTarget AffectationEquipeChantier entity, UpdateAffectationEquipeChantierRequest request);
}
