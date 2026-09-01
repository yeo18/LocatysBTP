package com.cms.equipe.entity;
import com.cms.equipe.entity.enums.RoleDansEquipe;

import java.time.LocalDate;

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
 * EntitÃ© associative de l'appartenance d'un utilisateur Ã  une Ã©quipe.
 *
 * <p>Table : {@code membre_equipe} (MLD). RÃ´le dans l'Ã©quipe (CHEF/OUVRIER).
 */
@Entity
@Table(name = "membre_equipe",
        uniqueConstraints = @UniqueConstraint(name = "uk_membre_equipe", columnNames = {"utilisateur_id", "equipe_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MembreEquipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_dans_equipe", nullable = false, length = 10)
    private RoleDansEquipe roleDansEquipe;

    @Column(name = "date_integration", nullable = false)
    private LocalDate dateIntegration;
}
