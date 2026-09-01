package com.cms.chantier.entity;

import java.time.LocalDateTime;

import com.cms.profil.entity.Profil;
import com.cms.utilisateur.entity.Utilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Relation ternaire {@code Utilisateur x Chantier x Profil} : affectation
 * directe d'un utilisateur a un chantier, avec un profil unique pour la
 * periode consideree.
 *
 * <p>Un utilisateur peut avoir plusieurs profils dans l'application, mais
 * sur un meme chantier il dispose d'un seul et unique profil pendant une
 * periode donnee ({@code date_debut} / {@code date_fin exclue}). Le chevauchement
 * des periodes pour un meme couple (utilisateur, chantier) est refuse cote
 * service.
 *
 * <p>Table : {@code affectation_utilisateur_chantier} (V9 + V22).
 */
@Entity
@Table(name = "affectation_utilisateur_chantier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AffectationUtilisateurChantier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chantier_id", nullable = false)
    private Chantier chantier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profil_id", nullable = false)
    private Profil profil;

    @Column(name = "date_affectation", nullable = false)
    private LocalDateTime dateAffectation;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;
}