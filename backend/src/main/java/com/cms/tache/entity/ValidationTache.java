package com.cms.tache.entity;
import com.cms.tache.entity.enums.ValidationTacheStatut;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
 * EntitÃ© reprÃ©sentant une dÃ©cision de validation d'une tÃ¢che.
 *
 * <p>Table : {@code validation_tache} (MLD). Historique des dÃ©cisions
 * VALIDE/REFUSE conservÃ©.
 */
@Entity
@Table(name = "validation_tache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationTache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validateur_id", nullable = false)
    private Utilisateur validateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ValidationTacheStatut statut;

    @Column(length = 500)
    private String commentaire;

    @Column(name = "date_validation", nullable = false)
    private LocalDate dateValidation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;
}
