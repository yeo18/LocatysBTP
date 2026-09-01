package com.cms.equipe.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cms.tache.entity.AffectationTache;

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
 * Entité représentant un groupe de travail.
 *
 * <p>Table : {@code equipe} (MLD). Une équipe est mobile : elle est
 * affectée à des chantiers par période ({@code AffectationEquipeChantier}).
 */
@Entity
@Table(name = "equipe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 255)
    private String description;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "equipe")
    private List<MembreEquipe> membres = new ArrayList<>();

    @OneToMany(mappedBy = "equipe")
    private List<AffectationEquipeChantier> affectationsChantiers = new ArrayList<>();

    @OneToMany(mappedBy = "equipe")
    private List<AffectationTache> affectationTaches = new ArrayList<>();
}
