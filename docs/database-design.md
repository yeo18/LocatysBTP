# Base de données — CMS

Version : 0.1
Statut : Documentation (aucune table créée)

---

# 1. Généralités

* SGBD : PostgreSQL
* Base cible : `gestion_de_chantier` (créée, conteneur chantier-db, port 5432)
* Outil de migration prévu : Flyway
* Nommage : `snake_case` pour les tables et colonnes

---

# 2. Entités principales

* utilisateur
* profil
* permission
* profil_permission
* utilisateur_permission
* chantier
* equipe
* tache
* historique_action

---

# 3. Description des entités

## utilisateur

Représente un utilisateur de l'application.

| Champ | Type | Contrainte |
|-------|------|-----------|
| id | bigint | PK, auto |
| nom | varchar(100) | NOT NULL |
| prenom | varchar(100) | NOT NULL |
| email | varchar(255) | NOT NULL, UNIQUE |
| mot_de_passe | varchar(255) | NOT NULL (hash bcrypt) |
| actif | boolean | NOT NULL, défaut true |
| profil_id | bigint | FK → profil |

## profil

Représente un rôle métier (ADMINISTRATEUR, CHEF_EQUIPE, OUVRIER).

| Champ | Type | Contrainte |
|-------|------|-----------|
| id | bigint | PK, auto |
| code | varchar(50) | NOT NULL, UNIQUE |
| libelle | varchar(100) | NOT NULL |

## permission

Représente une action autorisée (USER_CREATE, CHANTIER_VIEW, TACHE_VALIDATE...).

| Champ | Type | Contrainte |
|-------|------|-----------|
| id | bigint | PK, auto |
| code | varchar(100) | NOT NULL, UNIQUE |
| description | varchar(255) | |

## profil_permission

Table de liaison profil ↔ permission.

| Champ | Type | Contrainte |
|-------|------|-----------|
| profil_id | bigint | FK → profil (PK composite) |
| permission_id | bigint | FK → permission (PK composite) |

## utilisateur_permission

Permissions directes supplémentaires sur un utilisateur (exception RBAC).

| Champ | Type | Contrainte |
|-------|------|-----------|
| utilisateur_id | bigint | FK → utilisateur (PK composite) |
| permission_id | bigint | FK → permission (PK composite) |

## chantier

Représente un chantier.

| Champ | Type | Contrainte |
|-------|------|-----------|
| id | bigint | PK, auto |
| nom | varchar(255) | NOT NULL |
| localisation | varchar(255) | |
| type | varchar(255) | |
| date_debut | date | |
| date_fin | date | |

## equipe

Représente une équipe de travail.

| Champ | Type | Contrainte |
|-------|------|-----------|
| id | bigint | PK, auto |
| nom | varchar(255) | NOT NULL |
| chantier_id | bigint | FK → chantier |

## tache

Représente une tâche d'un chantier.

| Champ | Type | Contrainte |
|-------|------|-----------|
| id | bigint | PK, auto |
| titre | varchar(255) | NOT NULL |
| description | varchar(255) | |
| priorite | varchar(50) | (HAUTE, MOYENNE, BASSE) |
| statut | varchar(50) | (A_FAIRE, EN_COURS, TERMINE, VALIDE, REFUSE) |
| ordre | integer | |
| date_debut | date | |
| date_fin | date | |
| chantier_id | bigint | FK → chantier |

## historique_action

Journal d'audit de toutes les actions importantes.

| Champ | Type | Contrainte |
|-------|------|-----------|
| id | bigint | PK, auto |
| utilisateur_id | bigint | FK → utilisateur |
| action | varchar(100) | NOT NULL |
| type_entite | varchar(100) | |
| entite_id | bigint | |
| details | text | |
| date_action | timestamp | NOT NULL |

---

# 4. Relations et cardinalités (Merise)

```
UTILISATEUR 0,n ──── 1,1 PROFIL
PROFIL      0,n ──── 0,n PERMISSION   (via PROFIL_PERMISSION)
UTILISATEUR 0,n ──── 0,n PERMISSION   (via UTILISATEUR_PERMISSION)
CHANTIER    1,1 ──── 0,n EQUIPE
CHANTIER    1,1 ──── 0,n TACHE
EQUIPE      1,1 ──── 0,n UTILISATEUR
UTILISATEUR 1,1 ──── 0,n HISTORIQUE_ACTION
```

---

# 5. MLD

```
UTILISATEUR (id, nom, prenom, email, mot_de_passe, actif, #profil_id)
PROFIL (id, code, libelle)
PERMISSION (id, code, description)
PROFIL_PERMISSION (#profil_id, #permission_id)
UTILISATEUR_PERMISSION (#utilisateur_id, #permission_id)
CHANTIER (id, nom, localisation, type, date_debut, date_fin)
EQUIPE (id, nom, #chantier_id)
TACHE (id, titre, description, priorite, statut, ordre, date_debut, date_fin, #chantier_id)
HISTORIQUE_ACTION (id, action, type_entite, entite_id, details, date_action, #utilisateur_id)
```

---

# 6. Schéma des relations

```
PROFIL ───────< PROFIL_PERMISSION >─────── PERMISSION
  │                                          ▲
  │                                          │
  │                                          │
  ▼                                          │
UTILISATEUR ───────< UTILISATEUR_PERMISSION >┘
  │
  │
  ▼
HISTORIQUE_ACTION

CHANTIER ───────< EQUIPE
  │
  └──────< TACHE
```

---

FIN DU DOCUMENT
