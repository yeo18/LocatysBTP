package com.cms.tache.entity;
import com.cms.tache.entity.enums.AffectationTacheRole;

import java.time.LocalDate;

import com.cms.equipe.entity.Equipe;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EntitÃ© associative de l'affectation d'une tÃ¢che.
 *
 * <p>Table : {@code affectation_tache} (MLD). RÃ¨gle mÃ©tier : au moins une
 * cible obligatoire (utilisateur OU Ã©quipe) â€” Ã  exprimer en contrainte CHECK
 * au MPD. Le contrÃ´le applicatif sera fait dans le service.
 */
@Entity
@Table(name = "affectation_tache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationTache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AffectationTacheRole role;

    @Column(name = "date_affectation", nullable = false)
    private LocalDate dateAffectation;
}
