package com.cms.template.entity;

import java.time.LocalDateTime;

import com.cms.tache.entity.enums.Priorite;
import com.cms.utilisateur.entity.Utilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tache structuree d'un {@link TemplateTache} (table
 * {@code template_tache_tache}).
 *
 * <p>Un TemplateTache represente un ensemble structure de taches modele
 * (ex. "PrÃ©paration dalle" contenant preparer-coffrage, poser-ferraillage,
 * couler-beton...). Chaque ligne porte une copie de structure (titre,
 * description, priorite, duree) : independante de la table
 * operationnelle {@code tache} (aucune FK). L'import dans un chantier
 * produit des copies (snapshot) non liees a ce modele.
 */
@Entity
@Table(name = "template_tache_tache", uniqueConstraints = {
        @UniqueConstraint(name = "uk_template_tache_tache_titre",
                columnNames = {"template_tache_id", "titre"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTacheTache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_tache_id", nullable = false)
    private TemplateTache templateTache;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priorite priorite = Priorite.MOYENNE;

    @Column(name = "duree_estimee_jours")
    private Integer dureeEstimeeJours;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Utilisateur createdBy;
}