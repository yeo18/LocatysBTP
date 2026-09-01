package com.cms.tache.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.tache.entity.AffectationTacheUtilisateur;

public interface AffectationTacheUtilisateurRepository extends JpaRepository<AffectationTacheUtilisateur, Long> {

    List<AffectationTacheUtilisateur> findByTacheId(Long tacheId);

    void deleteByTacheId(Long tacheId);

    List<AffectationTacheUtilisateur> findByUtilisateurId(Long utilisateurId);
}
