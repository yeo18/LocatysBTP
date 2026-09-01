# Conception — Inventaire complet des entités métier du CMS

Version : 0.2
Statut : Conceptuel (aucun code, aucune table créée)
Base : LOOP 2.1 — inventaire officiel des entités

> ⚠️ DOCUMENT HISTORIQUE (LOOP 2.1 / 2.2). Certaines entités listées ici
> (Client, Notification, Materiel, tacheParente) ont été **retirées** au
> LOOP 2.2.1. La **référence officielle et unique** est désormais :
> `docs/dictionnaire-donnees-valide.md` (version 1.0).

Ce document est la référence officielle de la conception des données.
Il alimente le futur MCC, MCD, MLD et MPD.

---

# 1. Modèle RBAC officiel validé

* Utilisateur possède un Profil.
* Profil possède des Permissions par défaut.
* Utilisateur peut recevoir des permissions individuelles (ajout).
* Utilisateur peut avoir des permissions refusées individuellement (refus).

Ce modèle à 4 niveaux (Profil → Permission, User → ajout, User → refus)
permet une sécurité fine et dynamique, sans re-développement.

---

# 2. Inventaire complet des entités retenues

| # | Entité | Domaine |
|---|--------|---------|
| 1 | Utilisateur | Sécurité et utilisateurs |
| 2 | Profil | Sécurité et utilisateurs |
| 3 | Permission | Sécurité et utilisateurs |
| 4 | ProfilPermission | Sécurité et utilisateurs |
| 5 | UtilisateurPermission | Sécurité et utilisateurs |
| — | UtilisateurPermissionRefusee | Fusionnée dans UtilisateurPermission (champ type, LOOP 2.2) |
| 7 | Client | Organisation du chantier |
| 8 | Chantier | Organisation du chantier |
| 9 | Équipe | Organisation du chantier |
| 10 | MembreEquipe | Organisation du chantier |
| 11 | AffectationEquipeChantier | Organisation du chantier |
| 12 | Tâche | Gestion du travail |
| 13 | AffectationTâche | Gestion du travail |
| 14 | Planning | Gestion du travail |
| 15 | ValidationTâche | Gestion du travail |
| 16 | Commentaire | Gestion du travail |
| 17 | HistoriqueAction | Suivi et traçabilité |
| 18 | JournalConnexion | Suivi et traçabilité |
| 19 | Notification | Suivi et traçabilité |
| 20 | Document | Gestion documentaire |
| 21 | PièceJointe | Gestion documentaire |
| 22 | PhotoChantier | Gestion documentaire |
| 23 | Matériel | Ressources chantier |

---

# 3. Détail de chaque entité

## 3.1 Sécurité et utilisateurs

### Utilisateur

**Description métier** : personne habilitée à se connecter à l'application
(administrateur, chef d'équipe, ouvrier, client).

**Pourquoi** : c'est l'identité de connexion et la base de toute la sécurité
(authentification, autorisation, traçabilité).

**Informations principales** : id, nom, prenom, email (unique), mot_de_passe
(hash bcrypt), telephone, actif, date_creation, #profil_id.

**Relations prévues** : Profil (possède 1) ; Équipe (via MembreEquipe) ;
Tâche (via AffectationTâche) ; Permission (via UtilisateurPermission /
UtilisateurPermissionRefusee) ; HistoriqueAction, JournalConnexion,
Notification, Document, PhotoChantier, Commentaire, ValidationTâche.

---

### Profil

**Description métier** : rôle métier regroupant des permissions par défaut
(ADMINISTRATEUR, CHEF_EQUIPE, OUVRIER...).

**Pourquoi** : regroupe les droits communs à une fonction pour éviter de les
répéter sur chaque utilisateur (base du RBAC).

**Informations principales** : id, code (unique), libelle, description, actif.

**Relations prévues** : Utilisateur (1,n) ; Permission (via ProfilPermission).

---

### Permission

**Description métier** : action autorisée et nommée (USER_CREATE,
CHANTIER_VIEW, TACHE_VALIDATE...).

**Pourquoi** : unité élémentaire de droit, contrôlée par l'API à chaque appel.

**Informations principales** : id, code (unique), description, module.

**Relations prévues** : Profil (via ProfilPermission) ; Utilisateur (via
UtilisateurPermission et UtilisateurPermissionRefusee).

---

### ProfilPermission

**Description métier** : liaison Profil ↔ Permission (permissions par défaut
du profil).

**Pourquoi** : gère le RBAC de façon dynamique en base, sans modification de code.

**Informations principales** : id, #profil_id, #permission_id.

**Relations prévues** : Profil, Permission.

---

### UtilisateurPermission

**Description métier** : exception individuelle RBAC accordée **ou refusée** à un
utilisateur, en plus ou en dessous de celles de son profil.

**Pourquoi** : permet une exception à la hausse (droit supplémentaire) ou à la
baisse (refus, prioritaire sur le profil) sans modifier le profil.

**Informations principales** : id, #utilisateur_id, #permission_id,
type (ACCORDER/REFUSER), dateCreation, #createdBy.

**Relations prévues** : Utilisateur, Permission.

> Note LOOP 2.2 : l'entité `UtilisateurPermissionRefusee` (LOOP 2.1) a été
> **fusionnée** dans `UtilisateurPermission` via le champ `type`
> (voir `dictionnaire-donnees.md` §1.5).

---

## 3.2 Organisation du chantier

### Client

**Description métier** : propriétaire ou donneur d'ordre du chantier
(particulier ou entreprise).

**Pourquoi** : relie le chantier à son commanditaire, nécessaire à la gestion
commerciale et au suivi des contrats.

**Informations principales** : id, nom, prenom, telephone, email, adresse,
type (PARTICULIER/ENTREPRISE), actif.

**Relations prévues** : Chantier (1,n).

---

### Chantier

**Description métier** : projet de construction suivi dans l'application.

**Pourquoi** : entité centrale — tout (équipes, tâches, documents, photos,
matériel) gravite autour d'un chantier.

**Informations principales** : id, nom, description, localisation, type,
statut (PREVU/EN_COURS/TERMINE/ANNULE), date_debut, date_fin,
budget_previsionnel, #client_id, #responsable_id (utilisateur, optionnel),
date_creation.

**Relations prévues** : Client ; Équipe (via AffectationEquipeChantier) ;
Tâche (1,n) ; Planning (0,n) ; Document (0,n) ; PhotoChantier (0,n) ;
Matériel (0,n) ; Commentaire (0,n).

---

### Équipe

**Description métier** : groupe de travail (maçons, électriciens...).

**Pourquoi** : regroupe des utilisateurs qui interviennent ensemble ; une même
équipe peut être affectée à plusieurs chantiers (gestion multi-chantiers).

**Informations principales** : id, nom, description, actif, date_creation.

**Relations prévues** : Utilisateur (via MembreEquipe) ; Chantier (via
AffectationEquipeChantier).

---

### MembreEquipe

**Description métier** : appartenance d'un utilisateur à une équipe.

**Pourquoi** : relie Utilisateur et Équipe en conservant le rôle dans l'équipe
et la date d'ajout.

**Informations principales** : id, #equipe_id, #utilisateur_id,
role_dans_equipe (CHEF/OUVRIER), date_ajout.

**Relations prévues** : Équipe, Utilisateur.

---

### AffectationEquipeChantier

**Description métier** : affectation d'une équipe à un chantier sur une période.

**Pourquoi** : assure la sécurité par chantier (l'utilisateur ne voit que les
chantiers auxquels son équipe est affectée) et la flexibilité multi-chantiers
d'une même équipe.

**Informations principales** : id, #equipe_id, #chantier_id, date_debut,
date_fin, statut (ACTIVE/TERMINEE).

**Relations prévues** : Équipe, Chantier.

---

## 3.3 Gestion du travail

### Tâche

**Description métier** : unité de travail à réaliser sur un chantier.

**Pourquoi** : cœur opérationnel — suivi de l'avancement, priorisation,
validation.

**Informations principales** : id, titre, description, priorite (HAUTE/
MOYENNE/BASSE), statut (A_FAIRE/EN_COURS/TERMINE/VALIDE/REFUSE), ordre,
date_debut, date_fin, date_creation, #chantier_id, #planning_id (optionnel),
#tache_parente_id (sous-tâche, optionnel), #createur_id.

**Relations prévues** : Chantier ; Planning (0,1) ; Tâche (auto-référence,
sous-tâches) ; AffectationTâche (1,n) ; ValidationTâche (0,n) ;
Commentaire (0,n) ; PièceJointe (0,n).

---

### AffectationTâche

**Description métier** : utilisateur affecté à l'exécution d'une tâche.

**Pourquoi** : distribue le travail entre les membres et permet le filtrage
"mes tâches".

**Informations principales** : id, #tache_id, #utilisateur_id,
role (REALISATEUR/CONTROLEUR), date_affectation.

**Relations prévues** : Tâche, Utilisateur.

---

### Planning

**Description métier** : période de planification d'un chantier (global,
hebdomadaire, mensuel).

**Pourquoi** : organise les tâches dans le temps et structure l'avancement du chantier.

**Informations principales** : id, #chantier_id, libelle, type, date_debut,
date_fin, statut (PREVU/ACTIF/TERMINE).

**Relations prévues** : Chantier ; Tâche (1,n).

---

### ValidationTâche

**Description métier** : décision de validation ou de refus d'une tâche terminée.

**Pourquoi** : assure un contrôle qualité : une tâche doit être validée par une
personne autorisée (permission TACHE_VALIDATE).

**Informations principales** : id, #tache_id, #validateur_id, statut
(VALIDE/REFUSE), commentaire, date_validation.

**Relations prévues** : Tâche, Utilisateur (validateur).

---

### Commentaire

**Description métier** : message texte attaché à une tâche ou à un chantier.

**Pourquoi** : communication et traçabilité des échanges entre membres.

**Informations principales** : id, #auteur_id, #tache_id (optionnel),
#chantier_id (optionnel), contenu, date.

**Relations prévues** : Utilisateur (auteur) ; Tâche (0,1) ; Chantier (0,1) ;
PièceJointe (0,n).

---

## 3.4 Suivi et traçabilité

### HistoriqueAction

**Description métier** : journal d'audit de toutes les actions importantes.

**Pourquoi** : traçabilité complète (qui a fait quoi, quand, sur quoi) — exigence
de la Phase 9.

**Informations principales** : id, #utilisateur_id, action, type_entite,
entite_id, details, ip_adresse, date_action.

**Relations prévues** : Utilisateur (1,1).

---

### JournalConnexion

**Description métier** : traces de connexion et de déconnexion des utilisateurs.

**Pourquoi** : sécurité et audit (détection d'accès anormaux, horodatage).

**Informations principales** : id, #utilisateur_id, type (LOGIN/LOGOUT),
statut (SUCCES/ECHEC), ip_adresse, user_agent, date.

**Relations prévues** : Utilisateur (1,1).

---

### Notification

**Description métier** : message notifié à un utilisateur (tâche affectée,
validation, alerte).

**Pourquoi** : informer les acteurs sans relire manuellement le chantier.

**Informations principales** : id, #utilisateur_id, type, titre, contenu,
lien, lu (bool), date_creation.

**Relations prévues** : Utilisateur (1,1).

---

## 3.5 Gestion documentaire

### Document

**Description métier** : fichier métier rattaché à un chantier (contrat, plan,
devis...).

**Pourquoi** : centraliser les documents utiles au suivi du chantier.

**Informations principales** : id, titre, type, #chantier_id (optionnel),
#uploader_id, chemin_fichier, extension, taille, date_upload, statut.

**Relations prévues** : Chantier (0,1) ; Utilisateur (uploader).

---

### PièceJointe

**Description métier** : fichier attaché à une tâche, un commentaire ou une
validation (photo de constat, rapport...).

**Pourquoi** : enrichir les échanges et preuves sans multiplier les tables
(délégation via entite_type + entite_id).

**Informations principales** : id, entite_type (TACHE/COMMENTAIRE/VALIDATION),
entite_id, chemin_fichier, nom_original, extension, taille, date_ajout.

**Relations prévues** : polymorphique (Tâche, Commentaire, ValidationTâche).

---

### PhotoChantier

**Description métier** : photo d'avancement d'un chantier (avant/pendant/après).

**Pourquoi** : suivi visuel de l'avancement et preuve de l'état du chantier.

**Informations principales** : id, #chantier_id, #utilisateur_id, chemin,
description, type (AVANT/PENDANT/APRES), date_prise.

**Relations prévues** : Chantier, Utilisateur.

---

## 3.6 Ressources chantier

### Matériel

**Description métier** : matériel affecté à un chantier (bétonnière, échafaudage...).

**Pourquoi** : suivi de l'état et de l'affectation du matériel utilisé sur les
chantiers. Entité volontairement minimale.

**Informations principales** : id, nom, reference, type, etat (DISPONIBLE/
EN_UTILISATION/EN_PANNE), #chantier_id (optionnel), date_achat.

**Relations prévues** : Chantier (0,1).

---

# 4. Entités analysées et NON retenues

| Entité | Motif d'exclusion |
|--------|-------------------|
| Stock | Gestion d'inventaire complexe (mouvements, fournisseurs) hors périmètre des phases validées (MASTER_PLAN Phases 6-8). Réintroduisible si besoin futur. |
| Fournisseur | Nécessaire uniquement avec Stock/Dépenses ; hors périmètre actuel. |
| Dépense | Suivi financier détaillé hors périmètre ; le budget prévisionnel est déjà porté par Chantier.budget_previsionnel. |
| Budget | Remplacé par l'attribut budget_previsionnel du Chantier (pas d'entité dédiée à ce stade). |

---

# 5. Schéma global des entités (vue conceptuelle)

```
                         SÉCURITÉ / UTILISATEURS
PROFIL 1,n ----< UTILISATEUR >---- 0,n
  │                                 │
  1,n                               │ via UTILISATEUR_PERMISSION
  │ via PROFIL_PERMISSION           │ (type ACCORDER / REFUSER)
  ▼                                 │
PERMISSION <----- PROFIL_PERMISSION │

                        ORGANISATION
CLIENT 1,n ----< CHANTIER
                   │
   1,n      ┌──────┴──────┐
   via      │             │
AFFECTATION│           0,n
_EQUIPE    │         PLANNING
_CHANTIER  │
   │       ▼
EQUIPE 1,n ----< TACHE ----< (sous-tâches : auto-référence)
   │               │
   │ via           │ via AFFECTATION_TACHE / VALIDATION_TACHE / COMMENTAIRE
   ▼               ▼
MEMBRE_EQUIPE   UTILISATEUR
   │
   ▼
UTILISATEUR

                   DOCUMENTAIRE / SUIVI
CHANTIER 1,n ----< DOCUMENT / PHOTO_CHANTIER / MATERIEL
TACHE / COMMENTAIRE / VALIDATION ---< PIECE_JOINTE (polymorphique)
UTILISATEUR 1,n ----< HISTORIQUE_ACTION / JOURNAL_CONNEXION / NOTIFICATION
```

---

# 6. Impact sur la suite de la conception (MCD → MLD → MPD)

* Le MCD (LOOP 2.3) formalisera les associations et cardinalités exactes de
  chaque entité ci-dessus.
* Le MLD en découlera (tables, clés primaires/étrangères, tables de liaison).
* Le MPD précisera les types PostgreSQL, index et contraintes (LOOP 2.4+).
* Le module `audit` (dossier existant) couvrira HistoriqueAction et
  JournalConnexion.
* Les modules `materiel`, `document`, `notification`, `planning`, `stock`
  (dossiers existants) trouveront ici leurs entités (stock reste vide pour
  l'instant).

---

FIN DU DOCUMENT
