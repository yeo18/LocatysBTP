package com.cms.utilisateur.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cms.utilisateur.entity.Utilisateur;

/**
 * Accès aux données des utilisateurs.
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);
}
