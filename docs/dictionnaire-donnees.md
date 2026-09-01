# Dictionnaire complet des données — CMS

Version : 0.3
Statut : Conceptuel (aucun code, aucune table créée)
Base : LOOP 2.2 — définition précise des attributs
Convention : les attributs sont nommés en `camelCase` (modèle conceptuel).
La conversion en `snake_case` se fera au MPD (Phase 2, LOOP 2.5).

> ⚠️ DOCUMENT HISTORIQUE (LOOP 2.2). Certaines entités (Client, Notification,
> Materiel) et attributs (tacheParente, budget) ont été **retirés** au
> LOOP 2.2.1. La **référence officielle et unique** est désormais :
> `docs/dictionnaire-donnees-valide.md` (version 1.0).

**Légende** : O = obligatoire, O* = obligatoire avec défaut, N = optionnel.
Type : type conceptuel indicatif (précisé au MPD).

---

# 1. Entités de sécurité (RBAC)

## 1.1 UTILISATEUR

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| nom | Nom de famille | chaîne | O | @NotBlank, max 100 |
| prenom | Prénom | chaîne | O | @NotBlank, max 100 |
| email | Adresse de connexion | chaîne | O | @Email, @NotBlank, unique, max 255 |
| password | Mot de passe chiffré (bcrypt) | chaîne | O | min 8, max 255 |
| telephone | Contact téléphonique | chaîne | N | regex téléphone |
| actif | Compte actif/désactivé | booléen | O* | défaut true |
| dateCreation | Date de création du compte | date | O* | auto (prePersist) |
| dateModification | Date de dernière modification | date | O* | auto (preUpdate) |

**Attributs analysés et refusés** :
* `photoProfil` — non nécessaire : les photos concernent les chantiers (PhotoChantier), aucun besoin de photo d'identité.
* `derniereConnexion` — redondant : la traçabilité des connexions est portée par l'entité `JournalConnexion`.
* `compteBloque` — différé : la gestion du blocage (tentatives échouées) relève de la Phase 4 (JWT / authentification) ; `actif` suffit à la Phase 2.

---

## 1.2 PROFIL

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| nom | Nom du profil (rôle) | chaîne | O | @NotBlank, unique, max 100 |
| description | Rôle détaillé | chaîne | N | max 255 |
| actif | Profil activé/désactivé | booléen | O* | défaut true |
| dateCreation | Date de création | date | O* | auto (prePersist) |
| dateModification | Date de modification | date | O* | auto (preUpdate) |

**Justifications** : `description` clarifie le rôle ; `actif` permet de désactiver
un rôle sans supprimer son historique ; dates techniques pour l'audit.

---

## 1.3 PERMISSION

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| nom | Libellé affichable | chaîne | O | @NotBlank, max 100 |
| codePermission | Code technique unique (RBAC) | chaîne | O | @NotBlank, unique, pattern (ex : CHANTIER_CREATE) |
| description | Description de l'action | chaîne | N | max 255 |
| module | Module concerné (chantier, tache, utilisateur...) | chaîne | O | @NotBlank, max 50 |
| action | Action CRUD (CREATE, UPDATE, DELETE, VIEW...) | chaîne | O | @NotBlank, max 50 |

**Justifications** : `codePermission` est la clé lisible utilisée par le RBAC
(contrôles en API) ; `module` + `action` structurent et filtrent l'administration
des droits ; `nom` reste le libellé affichable.

Exemple de codes : CHANTIER_CREATE, CHANTIER_UPDATE, CHANTIER_DELETE,
CHANTIER_VIEW, TACHE_VALIDATE, USER_CREATE.

---

## 1.4 PROFIL_PERMISSION (associative)

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| profil | Profil concerné | référence | O | FK → Profil |
| permission | Permission accordée au profil | référence | O | FK → Permission |

Aucun attribut supplémentaire nécessaire : la liaison exprime uniquement les
permissions par défaut d'un profil.

---

## 1.5 UTILISATEUR_PERMISSION (associative — exceptions RBAC)

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| utilisateur | Utilisateur concerné | référence | O | FK → Utilisateur |
| permission | Permission concernée | référence | O | FK → Permission |
| type | Sens de l'exception | énuméré | O | ACCORDER / REFUSER |
| dateCreation | Date de la décision | date | O* | auto (prePersist) |
| createdBy | Auteur de la décision | référence | O | FK → Utilisateur |

**Fusion décidée (LOOP 2.2)** : l'entité `UtilisateurPermissionRefusee`
(LOOP 2.1) est **fusionnée** ici. Le champ `type` (ACCORDER/REFUSER) distingue
l'ajout du refus. Une seule table, contrainte d'unicité
(utilisateur, permission, type) — un même couple ne peut être présent qu'une fois
par type. Un refus est prioritaire sur les permissions du profil.

---

# 2. Entités d'organisation du chantier

## 2.1 CLIENT

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| nom | Nom (entreprise) / nom de famille | chaîne | O | @NotBlank, max 100 |
| prenom | Prénom (particulier) | chaîne | N | max 100 |
| email | Email de contact | chaîne | N | @Email, max 255 |
| telephone | Téléphone de contact | chaîne | N | regex téléphone |
| adresse | Adresse du client | chaîne | N | max 255 |
| type | Type de client | énuméré | O* | PARTICULIER / ENTREPRISE |
| utilisateur | Compte de connexion associé (optionnel) | référence | N | FK → Utilisateur |
| actif | Client actif/désactivé | booléen | O* | défaut true |

**Décision Client (utilisateur ou entité indépendante ?)** :
Client = **entité métier indépendante**, avec **lien optionnel vers un compte
Utilisateur**.
* La plupart des clients (donneur d'ordre) sont externes et ne se connectent pas.
* S'il doit se connecter (consulter l'avancement), on crée un compte Utilisateur
  (profil CLIENT) et on le relie via `utilisateur`.
* Les deux ne sont PAS dupliqués : le compte reste dans `Utilisateur`, les
  coordonnées métier restent dans `Client`.

---

## 2.2 CHANTIER (entité centrale)

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| nom | Nom du chantier | chaîne | O | @NotBlank, max 255 |
| description | Description du projet | chaîne | N | max 1000 |
| adresse | Localisation précise | chaîne | N | max 255 |
| budget | Budget prévisionnel | nombre | N | @DecimalMin(0) |
| statut | État du chantier | énuméré | O* | PREVU / EN_COURS / TERMINE / ANNULE |
| dateDebut | Date de début | date | N | @PastOrPresent |
| dateFin | Date de fin prévue | date | N | @Future (et ≥ dateDebut) |
| progression | Avancement global | nombre | O* | @Min(0) @Max(100), défaut 0 |
| client | Client donneur d'ordre | référence | N | FK → Client |
| responsable | Responsable du chantier | référence | N | FK → Utilisateur |
| dateCreation | Date de création | date | O* | auto (prePersist) |
| dateModification | Date de modification | date | O* | auto (preUpdate) |

**Justifications** : `adresse` localise précisément (remplace `localisation`) ;
`budget` simple (pas d'entité Dépense, décision LOOP 2.1) ; `client` et
`responsable` relient aux acteurs ; `progression` donne l'avancement visible sur
le tableau de bord ; dates techniques pour l'audit.

---

## 2.3 EQUIPE

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| nom | Nom de l'équipe | chaîne | O | @NotBlank, max 100 |
| description | Description de l'équipe | chaîne | N | max 255 |
| actif | Équipe active/désactivée | booléen | O* | défaut true |
| dateCreation | Date de création | date | O* | auto (prePersist) |
| dateModification | Date de modification | date | O* | auto (preUpdate) |

**Attributs analysés et refusés** :
* `responsableEquipe` — refusé : le responsable est déjà identifiable via
  `MembreEquipe.role_dans_equipe` (CHEF). Un attribut dédié créerait une source
  de vérité dupliquée et incohérente.

---

## 2.4 MEMBRE_EQUIPE (associative)

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| utilisateur | Membre de l'équipe | référence | O | FK → Utilisateur |
| equipe | Équipe d'appartenance | référence | O | FK → Equipe |
| roleDansEquipe | Rôle au sein de l'équipe | énuméré | O* | CHEF / OUVRIER |
| dateIntegration | Date d'intégration | date | O* | auto |

`roleDansEquipe` permet de désigner le chef d'équipe (voir Equipe).

---

## 2.5 AFFECTATION_EQUIPE_CHANTIER (associative)

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| equipe | Équipe affectée | référence | O | FK → Equipe |
| chantier | Chantier concerné | référence | O | FK → Chantier |
| dateDebut | Début d'affectation | date | O | requis |
| dateFin | Fin d'affectation | date | N | ≥ dateDebut |
| statut | État de l'affectation | énuméré | O* | ACTIVE / TERMINEE |

Cette entité porte la **sécurité par chantier** : l'utilisateur ne voit que les
chantiers auxquels ses équipes sont affectées.

---

# 3. Entités de gestion du travail

## 3.1 TACHE

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| titre | Nom de la tâche | chaîne | O | @NotBlank, max 255 |
| description | Description détaillée | chaîne | N | max 1000 |
| priorite | Niveau de priorité | énuméré | O* | HAUTE / MOYENNE / BASSE |
| status | État de la tâche | énuméré | O* | A_FAIRE / EN_COURS / TERMINE / VALIDE / REFUSE |
| ordre | Ordre d'affichage | nombre | N | ≥ 0 |
| progression | Avancement de la tâche | nombre | O* | @Min(0) @Max(100), défaut 0 |
| dateDebut | Début prévu | date | N | requis |
| dateFin | Fin prévue | date | N | ≥ dateDebut |
| dateRealisation | Date réelle de réalisation | date | N | @PastOrPresent |
| chantier | Chantier concerné | référence | O | FK → Chantier |
| planning | Planning de rattachement | référence | N | FK → Planning |
| tacheParente | Sous-tâche parente | référence | N | FK → Tache (auto) |
| createdBy | Créateur de la tâche | référence | O | FK → Utilisateur |
| dateCreation | Date de création | date | O* | auto (prePersist) |
| dateModification | Date de modification | date | O* | auto (preUpdate) |

**Attributs analysés et refusés** :
* `commentaireValidation` — refusé : redondant avec `ValidationTache` qui porte
  déjà le commentaire, le validateur, le statut et la date de validation.

**Ajouts** : `progression` (suivi visuel), `dateRealisation` (suivi réel),
`createdBy` (traçabilité).

---

## 3.2 AFFECTATION_TACHE (associative)

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| tache | Tâche concernée | référence | O | FK → Tache |
| utilisateur | Exécutant | référence | O | FK → Utilisateur |
| role | Rôle sur la tâche | énuméré | O* | REALISATEUR / CONTROLEUR |
| dateAffectation | Date d'affectation | date | O* | auto |

**Décision utilisateur ou équipe ?** : **utilisateur**. Le travail est réalisé
par des personnes ; l'appartenance à une équipe est portée par `MembreEquipe`.
Cela permet le filtre « mes tâches » et l'assignation précise d'un réalisateur
et/ou d'un contrôleur.

---

## 3.3 PLANNING

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| chantier | Chantier concerné | référence | O | FK → Chantier |
| libelle | Nom du planning | chaîne | O | @NotBlank, max 100 |
| type | Type de planning | énuméré | O* | GLOBAL / HEBDOMADAIRE / MENSUEL |
| dateDebut | Début de la période | date | O | requis |
| dateFin | Fin de la période | date | N | ≥ dateDebut |
| statut | État du planning | énuméré | O* | PREVU / ACTIF / TERMINE |
| dateCreation | Date de création | date | O* | auto |

---

## 3.4 VALIDATION_TACHE

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| tache | Tâche validée | référence | O | FK → Tache |
| validateur | Utilisateur validateur | référence | O | FK → Utilisateur |
| statut | Décision | énuméré | O | VALIDE / REFUSE |
| commentaire | Motif de la décision | chaîne | N | max 500 |
| dateValidation | Date de la décision | date | O* | auto |

---

## 3.5 COMMENTAIRE

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| auteur | Auteur du commentaire | référence | O | FK → Utilisateur |
| tache | Tâche commentée | référence | N | FK → Tache |
| chantier | Chantier commenté | référence | N | FK → Chantier |
| contenu | Texte du commentaire | chaîne | O | @NotBlank, max 1000 |
| dateCreation | Date du commentaire | date | O* | auto |

Contrainte : un commentaire porte sur **une tâche OU un chantier** (au moins une
des deux références renseignée).

---

# 4. Entités de suivi et traçabilité

## 4.1 HISTORIQUE_ACTION

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| utilisateur | Auteur de l'action | référence | O | FK → Utilisateur |
| action | Libellé de l'action | chaîne | O | @NotBlank, max 100 |
| typeEntite | Type d'objet concerné | chaîne | O | max 100 |
| entiteId | Identifiant de l'objet concerné | nombre | O | requis |
| details | Détails JSON de l'action | texte | N | — |
| ipAdresse | IP d'origine | chaîne | N | max 45 |
| dateAction | Date de l'action | date | O* | auto |

## 4.2 JOURNAL_CONNEXION

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| utilisateur | Compte concerné | référence | O | FK → Utilisateur |
| type | Type d'événement | énuméré | O | LOGIN / LOGOUT |
| statut | Résultat | énuméré | O | SUCCES / ECHEC |
| ipAdresse | IP d'origine | chaîne | N | max 45 |
| userAgent | Navigateur/agent | chaîne | N | max 255 |
| date | Date de l'événement | date | O* | auto |

## 4.3 NOTIFICATION

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| utilisateur | Destinataire | référence | O | FK → Utilisateur |
| type | Type de notification | énuméré | O | TACHE / VALIDATION / ALERTE / INFO |
| titre | Titre | chaîne | O | @NotBlank, max 100 |
| contenu | Message | chaîne | N | max 500 |
| lien | Cible dans l'application | chaîne | N | max 255 |
| lu | Notification lue | booléen | O* | défaut false |
| dateCreation | Date d'envoi | date | O* | auto |

---

# 5. Entités de gestion documentaire

## 5.1 DOCUMENT

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| titre | Titre du document | chaîne | O | @NotBlank, max 255 |
| type | Type (CONTRAT, PLAN, DEVIS...) | énuméré | O | liste définie |
| chantier | Chantier lié | référence | N | FK → Chantier |
| uploader | Utilisateur qui a déposé | référence | O | FK → Utilisateur |
| cheminFichier | Emplacement du fichier | chaîne | O | max 500 |
| extension | Extension du fichier | chaîne | N | max 10 |
| taille | Taille en octets | nombre | N | ≥ 0 |
| statut | État du document | énuméré | O* | BROUILLON / FINAL |
| dateUpload | Date de dépôt | date | O* | auto |

## 5.2 PIECE_JOINTE (polymorphique)

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| entiteType | Type d'entité hôte | énuméré | O | TACHE / COMMENTAIRE / VALIDATION |
| entiteId | Identifiant de l'entité hôte | nombre | O | requis |
| cheminFichier | Emplacement du fichier | chaîne | O | max 500 |
| nomOriginal | Nom d'origine | chaîne | O | max 255 |
| extension | Extension | chaîne | N | max 10 |
| taille | Taille en octets | nombre | N | ≥ 0 |
| dateAjout | Date d'ajout | date | O* | auto |

## 5.3 PHOTO_CHANTIER

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| chantier | Chantier concerné | référence | O | FK → Chantier |
| utilisateur | Auteur de la photo | référence | O | FK → Utilisateur |
| chemin | Emplacement du fichier | chaîne | O | max 500 |
| description | Légende | chaîne | N | max 255 |
| type | Moment de prise | énuméré | O* | AVANT / PENDANT / APRES |
| datePrise | Date de prise | date | O* | auto |

---

# 6. Ressources chantier

## 6.1 MATERIEL

| Attribut | Rôle | Type | O/N | Validation |
|----------|------|------|-----|------------|
| id | Identifiant technique | nombre | O | auto-généré |
| nom | Nom du matériel | chaîne | O | @NotBlank, max 100 |
| reference | Référence interne | chaîne | N | unique, max 100 |
| type | Catégorie | chaîne | N | max 100 |
| etat | État courant | énuméré | O* | DISPONIBLE / EN_UTILISATION / EN_PANNE |
| chantier | Chantier d'affectation | référence | N | FK → Chantier |
| dateAchat | Date d'achat | date | N | @PastOrPresent |

---

# 7. Récapitulatif — attributs proposés par l'agent (hors base concepteur)

| Attribut | Entité | Pourquoi nécessaire |
|----------|--------|---------------------|
| telephone | Utilisateur | Contact direct sur chantier (métier BTP) |
| actif | Utilisateur, Profil, Equipe, Client | Désactivation sans suppression (sécurité, historique) |
| dateCreation / dateModification | Toutes les tables principales | Audit et traçabilité (cohérence transversale) |
| description | Profil, Equipe, Chantier | Clarification métier |
| codePermission, module, action | Permission | Clé RBAC lisible + structuration/filtrage des droits |
| type (ACCORDER/REFUSER) | UtilisateurPermission | Fusion des exceptions RBAC (décision LOOP 2.2) |
| createdBy, dateCreation | UtilisateurPermission | Traçabilité de la décision d'exception |
| adresse | Chantier | Localisation précise |
| budget | Chantier | Suivi budgétaire simple (remplace entité Budget) |
| client, responsable | Chantier | Rattachement aux acteurs |
| progression | Chantier, Tache | Avancement visible (tableau de bord) |
| roleDansEquipe | MembreEquipe | Désigner le chef d'équipe |
| role | AffectationTache | Distinguer réalisateur/contrôleur |
| tacheParente, ordre | Tache | Sous-tâches et ordonnancement |

# 8. Récapitulatif — attributs refusés

| Attribut | Entité | Motif |
|----------|--------|-------|
| photoProfil | Utilisateur | Non nécessaire ; photos réservées aux chantiers |
| derniereConnexion | Utilisateur | Redondant avec JournalConnexion |
| compteBloque | Utilisateur | Différé à la Phase 4 (JWT / blocage) |
| commentaireValidation | Tache | Redondant avec ValidationTache |
| responsableEquipe | Equipe | Redondant avec MembreEquipe.roleDansEquipe (CHEF) |

---

FIN DU DOCUMENT
