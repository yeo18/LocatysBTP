# Dictionnaire des données — VERSION OFFICIELLE VALIDÉE

Version : 1.2 (officielle)
Statut : DÉFINITIF (conceptuel — aucun code, aucune table créée)
Loop de validation : LOOP 2.2.1, LOOP 2.2.2, LOOP 2.2.3 (précisions finales)
Référence unique pour : MCD, MLD, MPD, Entity Spring Boot, migrations Flyway.

---

# 1. Règles de gouvernance

À partir du LOOP 2.2.1, toute évolution du modèle passe obligatoirement par :

```
PROPOSITION DE MODIFICATION
        ↓
JUSTIFICATION
        ↓
VALIDATION DU CONCEPTEUR
        ↓
INTÉGRATION
```

L'agent ne peut PAS créer/supprimer d'entité, modifier un attribut, une règle
RBAC ou une relation métier en dehors de ce circuit.

---

# 2. Modèle RBAC officiel

```
UTILISATEUR
    │ possède
    ▼
  PROFIL
    │ définit
    ▼
PROFIL_PERMISSION
    │
    ▼
PERMISSION

UTILISATEUR
    │ exceptions individuelles
    ▼
UTILISATEUR_PERMISSION (type = ACCORDER / REFUSER)
```

Règles :
* Un Utilisateur possède un Profil.
* Un Profil définit des Permissions par défaut (ProfilPermission).
* Un Utilisateur peut recevoir des exceptions individuelles (UtilisateurPermission)
  de type **ACCORDER** (ajout) ou **REFUSER** (refus).

Droits effectifs d'un utilisateur :

```
Droits effectifs =
  Permissions du profil
  + ACCORDER utilisateur
  - REFUSER utilisateur
```

La règle **REFUSER est prioritaire** sur le profil et sur ACCORDER.

---

# 3. Profils système initiaux

Le système démarre avec deux profils obligatoires :

## 3.1 ADMINISTRATEUR

Rôle :
* gestion complète de l'application ;
* gestion des utilisateurs ;
* création/modification des profils ;
* attribution des permissions ;
* gestion des chantiers ;
* gestion des équipes ;
* administration générale.

## 3.2 UTILISATEUR_STANDARD

Rôle (accès limité selon ses permissions) :
* consulter son dashboard ;
* consulter ses chantiers autorisés ;
* consulter ses tâches ;
* consulter son historique ;
* effectuer uniquement les actions autorisées.

**Restriction** : il ne peut pas consulter les données globales de l'application.

Ces deux profils seront créés en données initiales (seed) lors de l'implémentation.

---

# 4. Sécurité par périmètre

Le modèle distingue deux niveaux de sécurité complémentaires :

## 4.1 Niveau 1 — Permission fonctionnelle (droit d'action)

Contrôlée par les Permissions (RBAC).

Exemples : `TACHE_VIEW`, `TACHE_VALIDATE`, `CHANTIER_CREATE`, `CHANTIER_UPDATE`.

## 4.2 Niveau 2 — Restriction des données (visibilité)

Un utilisateur peut uniquement voir :
* les **chantiers** auxquels ses équipes sont affectées
  (`AffectationEquipeChantier`) ;
* les **équipes** auxquelles il appartient (`MembreEquipe`) ;
* les **tâches** qui lui sont affectées (`AffectationTache`).

**Une permission ne donne jamais accès à toutes les données.**

Exemple : utilisateur A → Chantier A uniquement, Équipe B uniquement,
tâches affectées uniquement.

Les deux niveaux s'appliquent ensemble : une permission (Niveau 1) autorise
l'action, mais la visibilité (Niveau 2) limite toujours les données concernées.

---

# 5. Liste des entités validées (19)

## MODULE SÉCURITÉ
* Utilisateur
* Profil
* Permission
* ProfilPermission
* UtilisateurPermission

## MODULE ORGANISATION
* Chantier
* Equipe
* MembreEquipe
* AffectationEquipeChantier

## MODULE TRAVAIL
* Tache
* AffectationTache
* ValidationTache
* Planning
* Commentaire

## MODULE TRAÇABILITÉ
* HistoriqueAction
* JournalConnexion

## MODULE DOCUMENT
* Document
* PieceJointe
* PhotoChantier

---

# 6. Dictionnaire par entité (attributs définitifs)

**Légende** : O = obligatoire, O* = obligatoire avec défaut, N = optionnel.
Convention : `camelCase` conceptuel → `snake_case` au MPD.

## 6.1 UTILISATEUR

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| nom | Nom de famille | O | @NotBlank, max 100 |
| prenom | Prénom | O | @NotBlank, max 100 |
| email | Adresse de connexion | O | @Email, unique, max 255 |
| password | Mot de passe chiffré (bcrypt) | O | min 8, max 255 |
| telephone | Contact téléphonique | N | regex téléphone |
| actif | Compte actif/désactivé | O* | défaut true |
| dateCreation | Date de création | O* | auto |
| dateModification | Date de modification | O* | auto |

## 6.2 PROFIL

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| nom | Nom du profil | O | @NotBlank, unique, max 100 |
| description | Rôle détaillé | N | max 255 |
| actif | Profil activé/désactivé | O* | défaut true |
| dateCreation | Date de création | O* | auto |
| dateModification | Date de modification | O* | auto |

## 6.3 PERMISSION

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| nom | Libellé affichable | O | @NotBlank, max 100 |
| codePermission | Code technique unique (RBAC) | O | unique, pattern (ex : TACHE_VIEW) |
| module | Module concerné | O | max 50 |
| action | Action CRUD (CREATE/UPDATE/DELETE/VIEW...) | O | max 50 |
| description | Description de l'action | N | max 255 |

## 6.4 PROFIL_PERMISSION (associative)

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| profil | Profil concerné | O | FK → Profil |
| permission | Permission accordée | O | FK → Permission |

## 6.5 UTILISATEUR_PERMISSION (associative — exceptions RBAC)

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| utilisateur | Utilisateur concerné | O | FK → Utilisateur |
| permission | Permission concernée | O | FK → Permission |
| type | Sens de l'exception | O | ACCORDER / REFUSER |
| dateCreation | Date de la décision | O* | auto |
| createdBy | Auteur de la décision | O | FK → Utilisateur |

## 6.6 CHANTIER (entité centrale)

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| nom | Nom du chantier | O | @NotBlank, max 255 |
| description | Description du projet | N | max 1000 |
| adresse | Localisation | N | max 255 |
| statut | État du chantier | O* | PREVU / EN_COURS / TERMINE / ANNULE |
| budget | Estimation financière indicative | N | @DecimalMin(0) |
| dateDebut | Date de début | N | @PastOrPresent |
| dateFin | Date de fin prévue | N | ≥ dateDebut |
| progression | Avancement global | O* | @Min(0) @Max(100), défaut 0 |
| dateCreation | Date de création | O* | auto |
| dateModification | Date de modification | O* | auto |

**Remarques** :
* `progression` : **conservée**. Mode de calcul à définir dans les règles
  métier — possibilités : saisie manuelle ou calcul automatique depuis les tâches.
* `budget` : **conservé (correction LOOP 2.2.3)** —
  `budget = estimation financière indicative du chantier`. Ce champ ne
  représente **pas** un module financier complet.
* `responsable` : **retiré** (correction LOOP 2.2.2). La notion de responsable
  sera modélisée au LOOP 2.3 sous forme de **relation**
  (UTILISATEUR — responsable de — CHANTIER).

## 6.7 EQUIPE

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| nom | Nom de l'équipe | O | @NotBlank, max 100 |
| description | Description de l'équipe | N | max 255 |
| actif | Équipe active/désactivée | O* | défaut true |
| dateCreation | Date de création | O* | auto |
| dateModification | Date de modification | O* | auto |

## 6.8 MEMBRE_EQUIPE (associative)

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| utilisateur | Membre de l'équipe | O | FK → Utilisateur |
| equipe | Équipe d'appartenance | O | FK → Equipe |
| roleDansEquipe | Rôle dans l'équipe | O* | CHEF / OUVRIER |
| dateIntegration | Date d'intégration | O* | auto |

## 6.9 AFFECTATION_EQUIPE_CHANTIER (associative)

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| equipe | Équipe affectée | O | FK → Equipe |
| chantier | Chantier concerné | O | FK → Chantier |
| dateDebut | Début d'affectation | O | requis |
| dateFin | Fin d'affectation | N | ≥ dateDebut |
| statut | État de l'affectation | O* | ACTIVE / TERMINEE |

Porte la sécurité des données (visibilité des chantiers).

## 6.10 TACHE

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| titre | Nom de la tâche | O | @NotBlank, max 255 |
| description | Description détaillée | N | max 1000 |
| priorite | Niveau de priorité | O* | HAUTE / MOYENNE / BASSE |
| status | État de la tâche | O* | A_FAIRE / EN_COURS / TERMINE / VALIDE / REFUSE |
| ordre | Ordre d'affichage | N | ≥ 0 |
| progression | Avancement | O* | @Min(0) @Max(100), défaut 0 |
| dateDebut | Début prévu | N | requis |
| dateFin | Fin prévue | N | ≥ dateDebut |
| dateRealisation | Date réelle de réalisation | N | @PastOrPresent |
| chantier | Chantier concerné | O | FK → Chantier |
| planning | Planning de rattachement | N | FK → Planning |
| createdBy | Créateur | O | FK → Utilisateur |
| dateCreation | Date de création | O* | auto |
| dateModification | Date de modification | O* | auto |

**Remarque** : `progression` représente l'avancement de la tâche (**conservée**).
Son mode de calcul (saisie manuelle ou automatique) sera précisé dans les règles métier.

## 6.11 AFFECTATION_TACHE (associative)

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| tache | Tâche concernée | O | FK → Tache |
| utilisateur | Cible : exécutant | N | FK → Utilisateur |
| equipe | Cible : équipe | N | FK → Equipe |
| role | Rôle sur la tâche | O* | REALISATEUR / CONTROLEUR |
| dateAffectation | Date d'affectation | O* | auto |

**Règle (correction LOOP 2.2.2)** : au moins une cible obligatoire —
`utilisateur` OU `equipe` (ou les deux).

## 6.12 VALIDATION_TACHE

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| tache | Tâche validée | O | FK → Tache |
| validateur | Utilisateur validateur | O | FK → Utilisateur |
| statut | Décision | O | VALIDE / REFUSE |
| commentaire | Motif de la décision | N | max 500 |
| dateValidation | Date de la décision | O* | auto |

## 6.13 PLANNING

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| chantier | Chantier concerné | O | FK → Chantier |
| libelle | Nom du planning | O | @NotBlank, max 100 |
| type | Type de planning | O* | GLOBAL / HEBDOMADAIRE / MENSUEL |
| dateDebut | Début de la période | O | requis |
| dateFin | Fin de la période | N | ≥ dateDebut |
| statut | État du planning | O* | PREVU / ACTIF / TERMINE |
| dateCreation | Date de création | O* | auto |

## 6.14 COMMENTAIRE

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| auteur | Auteur | O | FK → Utilisateur |
| tache | Tâche commentée | N | FK → Tache |
| chantier | Chantier commenté | N | FK → Chantier |
| contenu | Texte | O | @NotBlank, max 1000 |
| dateCreation | Date | O* | auto |

Contrainte : au moins une référence (tache OU chantier) renseignée.

## 6.15 HISTORIQUE_ACTION

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| utilisateur | Auteur | O | FK → Utilisateur |
| action | Libellé de l'action | O | max 100 |
| typeEntite | Type d'objet | O | max 100 |
| entiteId | Identifiant d'objet | O | requis |
| details | Détails (JSON) | N | — |
| ipAdresse | IP d'origine | N | max 45 |
| dateAction | Date de l'action | O* | auto |

## 6.16 JOURNAL_CONNEXION

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| utilisateur | Compte concerné | O | FK → Utilisateur |
| type | Type d'événement | O | LOGIN / LOGOUT |
| statut | Résultat | O | SUCCES / ECHEC |
| ipAdresse | IP d'origine | N | max 45 |
| userAgent | Navigateur | N | max 255 |
| date | Date | O* | auto |

## 6.17 DOCUMENT

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| titre | Titre du document | O | @NotBlank, max 255 |
| type | Type (CONTRAT, PLAN, DEVIS...) | O | liste définie |
| chantier | Chantier lié | N | FK → Chantier |
| uploader | Utilisateur qui a déposé | O | FK → Utilisateur |
| cheminFichier | Emplacement du fichier | O | max 500 |
| extension | Extension | N | max 10 |
| taille | Taille (octets) | N | ≥ 0 |
| statut | État du document | O* | BROUILLON / FINAL |
| dateUpload | Date de dépôt | O* | auto |

## 6.18 PIECE_JOINTE (polymorphique)

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| entiteType | Type d'entité hôte | O | TACHE / COMMENTAIRE / VALIDATION |
| entiteId | Identifiant de l'entité hôte | O | requis |
| cheminFichier | Emplacement du fichier | O | max 500 |
| nomOriginal | Nom d'origine | O | max 255 |
| extension | Extension | N | max 10 |
| taille | Taille (octets) | N | ≥ 0 |
| dateAjout | Date d'ajout | O* | auto |

## 6.19 PHOTO_CHANTIER

| Attribut | Rôle | O/N | Validation |
|----------|------|-----|------------|
| id | Identifiant technique | O | auto |
| chantier | Chantier concerné | O | FK → Chantier |
| utilisateur | Auteur | O | FK → Utilisateur |
| chemin | Emplacement du fichier | O | max 500 |
| description | Légende | N | max 255 |
| type | Moment de prise | O* | AVANT / PENDANT / APRES |
| datePrise | Date de prise | O* | auto |

## 6.20 RÈGLE TRANSVERSALE — ATTRIBUTS TECHNIQUES

Règle générale (correction LOOP 2.2.2) :

* Toutes les entités principales peuvent posséder : `id`, `dateCreation`,
  `dateModification`.
* Les attributs `createdBy` / `modifiedBy` ne sont **pas** ajoutés
  automatiquement : ils ne sont intégrés que si une **justification métier**
  existe.

Application actuelle :
* `createdBy` présent sur **UtilisateurPermission** (traçabilité de la décision
  d'exception) et sur **Tache** (créateur de la tâche) — justifiés.
* `modifiedBy` : aucun, non requis pour l'instant.

---

# 7. Entités et attributs REJETÉS

| Élément | Type | Motif |
|---------|------|-------|
| Client | Entité | Hors périmètre CORE (outil interne). Tout besoin futur = nouveau module validé. |
| PhotoProfil | Attribut (Utilisateur) | Non nécessaire ; photos réservées aux chantiers. |
| DerniereConnexion | Attribut (Utilisateur) | Redondant avec JournalConnexion. |
| CompteBloque | Attribut (Utilisateur) | Différé à la Phase 4 (JWT / blocage). |
| CommentaireValidation | Attribut (Tache) | Redondant avec ValidationTache. |
| ResponsableEquipe | Attribut (Equipe) | Redondant avec MembreEquipe.roleDansEquipe. |

# 8. Entités et attributs EN ATTENTE (modules futurs — hors CORE)

| Élément | Note |
|---------|------|
| Client | Module futur NON VALIDÉ — hors CORE. |
| Materiel | Module ressources — futur. |
| Stock | Hors périmètre actuel. |
| Module financier complet | Dépense, facture, paiement, fournisseur, comptabilité — hors CORE (Chantier.budget reste une estimation indicative, pas un module). |
| Notification | Module notifications — futur. |
| tacheParente | Sous-tâches — module futur. |

Aucun de ces éléments n'apparaît dans le MCD actuel.

---

# 9. Corrections appliquées (delta LOOP 2.1 → 2.2.1)

* Suppression de l'entité Client (et de la référence chantier.client).
* Retrait de Notification (hors liste validée).
* Retrait de Materiel (en attente).
* Retrait de Tache.tacheParente (en attente).
* Retrait de Chantier.budget (en attente).
* RBAC officiel figé : ProfilPermission + UtilisateurPermission(type ACCORDER/REFUSER).
* Profils initiaux système documentés (ADMINISTRATEUR, UTILISATEUR_STANDARD).
* Sécurité par périmètre (fonctionnelle + données) documentée.

---

# 10. Historique des corrections (LOOP 2.2.2)

| # | Anomalie détectée | Correction appliquée | Justification |
|---|-------------------|----------------------|---------------|
| 1 | Chantier.responsable (attribut) | Attribut retiré ; relation prévue au LOOP 2.3 (UTILISATEUR — responsable de — CHANTIER) | Le responsable est un lien entre entités, pas une propriété simple du chantier |
| 2 | Client présent dans l'historique | Confirmé hors périmètre, module futur NON VALIDÉ | Outil interne de gestion de chantier |
| 3 | Formule des droits effectifs absente | Ajout : Profil + ACCORDER − REFUSER, REFUSER prioritaire | Clarifie la sémantique exacte du RBAC |
| 4 | Sécurité par périmètre non explicite | Deux niveaux explicités ; « une permission ne donne jamais accès à toutes les données » | Distinction droit d'action / visibilité des données |
| 5 | Mode de calcul de progression | Remarque ajoutée (saisie manuelle OU calcul depuis tâches) ; attributs conservés | À trancher dans les règles métier |
| 6 | AffectationTache limitée à l'utilisateur | `utilisateur` (N) + `equipe` (N) ajouté ; au moins une cible obligatoire | Une tâche peut être confiée à un utilisateur ou à une équipe |
| 7 | createdBy/modifiedBy non cadrés | Règle transversale : id/dateCreation/dateModification génériques ; createdBy/modifiedBy uniquement si justification | Évite le sur-équipement technique |
| 8 | Modules futurs non regroupés | Client, Materiel, Stock, Budget avancé, Depense, Fournisseur, Notification listés hors CORE | Ils n'entrent pas dans le MCD actuel |

---

# 11. Historique des corrections (LOOP 2.2.3 — v1.2)

| # | Anomalie / précision | Correction appliquée | Justification |
|---|----------------------|----------------------|---------------|
| 1 | Profils système insuffisamment détaillés | Section « Profils système initiaux » : ADMINISTRATEUR (gestion complète, profils, permissions, chantiers, équipes) et UTILISATEUR_STANDARD (accès limité, restriction sur les données globales) | Préciser le périmètre exact de chaque profil initial |
| 2 | Chantier.budget sans définition | `budget = estimation financière indicative du chantier` ; ce n'est pas un module financier complet | Éviter toute dérive vers un module comptable hors CORE |

---

FIN DU DOCUMENT — RÉFÉRENCE OFFICIELLE
