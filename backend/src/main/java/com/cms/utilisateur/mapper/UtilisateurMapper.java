package com.cms.utilisateur.mapper;

import com.cms.utilisateur.dto.CreateUtilisateurRequest;
import com.cms.utilisateur.dto.UpdateUtilisateurRequest;
import com.cms.utilisateur.dto.UtilisateurResponse;
import com.cms.utilisateur.dto.UtilisateurResumeResponse;
import com.cms.utilisateur.entity.Utilisateur;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UtilisateurMapper {

    @Mapping(source = "profil.id", target = "profilId")
    @Mapping(source = "profil.nom", target = "profilNom")
    UtilisateurResponse toResponse(Utilisateur entity);

    @Mapping(source = "profil.nom", target = "profilNom")
    UtilisateurResumeResponse toResumeResponse(Utilisateur entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "utilisateurPermissions", ignore = true)
    @Mapping(target = "membreEquipes", ignore = true)
    @Mapping(target = "affectationTaches", ignore = true)
    @Mapping(target = "validations", ignore = true)
    @Mapping(target = "tachesCrees", ignore = true)
    @Mapping(target = "chantiersResponsables", ignore = true)
    Utilisateur toEntity(CreateUtilisateurRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "profil", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "utilisateurPermissions", ignore = true)
    @Mapping(target = "membreEquipes", ignore = true)
    @Mapping(target = "affectationTaches", ignore = true)
    @Mapping(target = "validations", ignore = true)
    @Mapping(target = "tachesCrees", ignore = true)
    @Mapping(target = "chantiersResponsables", ignore = true)
    void update(@MappingTarget Utilisateur entity, UpdateUtilisateurRequest request);
}
