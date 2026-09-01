package com.cms.profil.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cms.utilisateur.entity.Utilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant un profil (rôle métier portant des droits par défaut).
 *
 * <p>Table : {@code profil} (MLD). Chaque utilisateur possède un profil.
 */
@Entity
@Table(name = "profil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 255)
    private String description;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "profil")
    private List<ProfilPermission> profilPermissions = new ArrayList<>();

    @OneToMany(mappedBy = "profil")
    private List<Utilisateur> utilisateurs = new ArrayList<>();
}
