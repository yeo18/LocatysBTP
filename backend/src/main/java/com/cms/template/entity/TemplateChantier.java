package com.cms.template.entity;
import com.cms.template.entity.enums.TypeConstruction;
import com.cms.template.entity.enums.TemplateChantierStatut;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ModÃ¨le complet de construction d'un bÃ¢timent (table {@code template_chantier}).
 *
 * <p>Un template de chantier sert Ã  prÃ©parer rapidement un nouveau chantier
 * (ex. Maison R+1, Immeuble R+5). Il regroupe des {@link TemplateTache} via
 * l'association {@link TemplateChantierTache}. L'import produit des donnÃ©es
 * indÃ©pendantes : aucune relation permanente avec {@code chantier}.
 */
@Entity
@Table(name = "template_chantier", uniqueConstraints = @UniqueConstraint(
        name = "uk_template_chantier_nom", columnNames = "nom"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateChantier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_construction", nullable = false, length = 20)
    private TypeConstruction typeConstruction;

    @Column(name = "duree_estimee_jours")
    private Integer dureeEstimeeJours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TemplateChantierStatut statut = TemplateChantierStatut.ACTIF;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Utilisateur createdBy;

    @OneToMany(mappedBy = "templateChantier")
    private List<TemplateChantierTache> taches = new ArrayList<>();
}
