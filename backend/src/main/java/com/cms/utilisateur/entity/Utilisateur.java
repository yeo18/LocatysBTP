package com.cms.utilisateur.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cms.chantier.entity.Chantier;
import com.cms.equipe.entity.MembreEquipe;
import com.cms.profil.entity.Profil;
import com.cms.tache.entity.AffectationTache;
import com.cms.tache.entity.Tache;
import com.cms.tache.entity.ValidationTache;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Entité centrale du CMS : compte de connexion (base de l'authentification).
 *
 * <p>Table : {@code utilisateur} (MLD). Un utilisateur possède un profil
 * (RBAC) et peut être responsable de chantiers, membre d'équipes, etc.
 */
@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 20)
    private String telephone;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profil_id", nullable = false)
    private Profil profil;

    @OneToMany(mappedBy = "utilisateur")
    private List<UtilisateurPermission> utilisateurPermissions = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    private List<MembreEquipe> membreEquipes = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    private List<AffectationTache> affectationTaches = new ArrayList<>();

    @OneToMany(mappedBy = "validateur")
    private List<ValidationTache> validations = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy")
    private List<Tache> tachesCrees = new ArrayList<>();

    @OneToMany(mappedBy = "responsable")
    private List<Chantier> chantiersResponsables = new ArrayList<>();
}
