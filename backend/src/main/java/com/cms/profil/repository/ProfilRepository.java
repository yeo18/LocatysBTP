package com.cms.profil.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.profil.entity.Profil;

/**
 * Accès aux données des profils.
 */
public interface ProfilRepository extends JpaRepository<Profil, Long> {

    Optional<Profil> findByNom(String nom);

    boolean existsByNom(String nom);
}
