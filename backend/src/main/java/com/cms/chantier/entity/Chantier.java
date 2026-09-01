package com.cms.chantier.entity;
import com.cms.chantier.entity.enums.OrigineCoordonnees;
import com.cms.chantier.entity.enums.FiabiliteCoordonnees;
import com.cms.chantier.entity.enums.ChantierStatut;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cms.equipe.entity.AffectationEquipeChantier;
import com.cms.tache.entity.Tache;
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
 * EntitÃ© centrale : projet de construction.
 *
 * <p>Table : {@code chantier} (MLD). Responsable optionnel (0..1) ; un
 * chantier actif doit avoir un responsable (rÃ¨gle mÃ©tier).
 */
@Entity
@Table(name = "chantier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chantier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(name = "adresse_saisie", length = 255)
    private String adresseSaisie;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "adresse_geocodee", length = 300)
    private String adresseGeocodee;

    @Enumerated(EnumType.STRING)
    @Column(name = "origine_coordonnees", length = 20)
    private OrigineCoordonnees origineCoordonnees;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiabilite_coordonnees", length = 20)
    private FiabiliteCoordonnees fiabiliteCoordonnees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChantierStatut statut = ChantierStatut.PREVU;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(nullable = false)
    private int progression;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Utilisateur responsable;

    @OneToMany(mappedBy = "chantier")
    private List<Tache> taches = new ArrayList<>();

    @OneToMany(mappedBy = "chantier")
    private List<AffectationEquipeChantier> affectationEquipeChantiers = new ArrayList<>();

}
