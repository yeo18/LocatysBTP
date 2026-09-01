package com.cms.equipe.entity;
import com.cms.equipe.entity.enums.AffectationEquipeChantierStatut;

import java.time.LocalDate;

import com.cms.chantier.entity.Chantier;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EntitÃ© associative de l'affectation d'une Ã©quipe Ã  un chantier (pÃ©riode).
 *
 * <p>Table : {@code affectation_equipe_chantier} (MLD). Base de la
 * sÃ©curitÃ© des donnÃ©es : visibilitÃ© des chantiers par Ã©quipe.
 */
@Entity
@Table(name = "affectation_equipe_chantier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationEquipeChantier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chantier_id", nullable = false)
    private Chantier chantier;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AffectationEquipeChantierStatut statut = AffectationEquipeChantierStatut.ACTIVE;
}
