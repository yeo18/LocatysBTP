package com.cms.tache.entity;
import com.cms.tache.entity.enums.TacheStatus;
import com.cms.tache.entity.enums.Priorite;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cms.chantier.entity.Chantier;
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
 * EntitÃ© reprÃ©sentant une unitÃ© de travail d'un chantier.
 *
 * <p>Table : {@code tache} (MLD). Toute tÃ¢che appartient Ã  un chantier
 * (chantier_id obligatoire).
 */
@Entity
@Table(name = "tache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tache {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TacheStatus status = TacheStatus.A_FAIRE;

    @Column(nullable = false)
    private int progression;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_realisation")
    private LocalDate dateRealisation;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chantier_id", nullable = false)
    private Chantier chantier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Utilisateur createdBy;

    @OneToMany(mappedBy = "tache")
    private List<AffectationTache> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "tache")
    private List<ValidationTache> validations = new ArrayList<>();
}
