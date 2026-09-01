package com.cms.tache.entity;

import java.time.LocalDate;

import com.cms.equipe.entity.Equipe;
import com.cms.tache.entity.enums.AffectationTacheRole;

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
 * Entite associative d'affectation d'une tache a une equipe.
 *
 * <p>Table : {@code affectation_tache_equipe} (relation binaire TACHE - EQUIPE).
 */
@Entity
@Table(name = "affectation_tache_equipe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationTacheEquipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AffectationTacheRole role;

    @Column(name = "date_affectation", nullable = false)
    private LocalDate dateAffectation;
}
