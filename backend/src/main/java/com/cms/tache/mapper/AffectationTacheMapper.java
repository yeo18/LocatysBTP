package com.cms.tache.mapper;

import com.cms.tache.dto.AffectationTacheResponse;
import com.cms.tache.entity.AffectationTacheEquipe;
import com.cms.tache.entity.AffectationTacheUtilisateur;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AffectationTacheMapper {

    @Mapping(source = "tache.id", target = "tacheId")
    @Mapping(source = "tache.titre", target = "tacheTitre")
    @Mapping(source = "utilisateur.id", target = "utilisateurId")
    @Mapping(source = "utilisateur.nom", target = "utilisateurNom")
    @Mapping(target = "equipeId", ignore = true)
    @Mapping(target = "equipeNom", ignore = true)
    AffectationTacheResponse toResponse(AffectationTacheUtilisateur entity);

    @Mapping(source = "tache.id", target = "tacheId")
    @Mapping(source = "tache.titre", target = "tacheTitre")
    @Mapping(source = "equipe.id", target = "equipeId")
    @Mapping(source = "equipe.nom", target = "equipeNom")
    @Mapping(target = "utilisateurId", ignore = true)
    @Mapping(target = "utilisateurNom", ignore = true)
    AffectationTacheResponse toResponse(AffectationTacheEquipe entity);
}
