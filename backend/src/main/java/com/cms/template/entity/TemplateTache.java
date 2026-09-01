package com.cms.template.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Groupe rÃ©utilisable de tÃ¢ches (table {@code template_tache}).
 *
 * <p>Un template de tÃ¢che (ex. Dalle bÃ©ton, Toiture tÃ´le) n'est PAS un
 * chantier. Il peut Ãªtre utilisÃ© dans plusieurs {@link TemplateChantier} et
 * importÃ© directement dans un chantier existant.
 */
@Entity
@Table(name = "template_tache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @OneToMany(mappedBy = "templateTache")
    private List<TemplateChantierTache> templates = new ArrayList<>();

    @OneToMany(mappedBy = "templateTache")
    private List<TemplateTacheTache> taches = new ArrayList<>();
}
