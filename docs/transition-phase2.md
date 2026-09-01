# Transition Phase 2 — Conception métier et base de données

Version : 0.1
Statut : Note de transition (aucune table ni entité créée)
Auteur : LOOP 1.8

---

# 1. Objectif de la Phase 2

Produire la **conception métier complète** du CMS selon l'approche Merise :

```
MCC → MCD → MLD → MPD
```

Puis implémenter le modèle JPA (Phase 2) et les premiers modules techniques
(RBAC : profil, permission, utilisateur).

La Phase 2 suivra la stratégie Flyway définie dans `flyway-strategy.md` :
toute évolution de schéma passe par `db/migration/`, jamais par `ddl-auto`.

---

# 2. MCC — Modèle Conceptuel de Communication

Acteurs du système et flux d'information.

| Acteur | Flux (acteur → système) | Flux (système → acteur) |
|--------|------------------------|------------------------|
| Administrateur | Gérer utilisateurs, profils, permissions, chantiers | Confirmation, erreurs, rapports |
| Chef d'équipe | Créer/gérer équipes, tâches, matériel | Confirmation, notifications |
| Ouvrier | Consulter tâches, signaler avancement | Tâches assignées, notifications |
| Système | Écrire l'historique (audit) de chaque action | Journal d'audit |

Le MCC complet et détaillé (avec points d'échange et supports) sera établi
au **LOOP 2.1** (inventaire complet des entités).

---

# 3. Inventaire prévisionnel des entités

Base : `database-design.md` (ébauche Phase 0, 9 entités).

## Socle RBAC / sécurité

| Entité | Rôle |
|--------|------|
| utilisateur | Compte de connexion |
| profil | Rôle métier (ADMINISTRATEUR, CHEF_EQUIPE, OUVRIER) |
| permission | Action autorisée (USER_CREATE, CHANTIER_VIEW, TACHE_VALIDATE...) |
| profil_permission | Liaison profil ↔ permission (RBAC) |
| utilisateur_permission | Permissions directes supplémentaires |

## Métier

| Entité | Rôle |
|--------|------|
| chantier | Chantier de construction |
| equipe | Équipe de travail rattachée à un chantier |
| tache | Tâche d'un chantier |

## Technique

| Entité | Rôle |
|--------|------|
| historique_action | Journal d'audit des actions |

## À étudier au LOOP 2.1

Entités des modules complémentaires (selon les besoins métier) :

* materiel / stock (gestion du matériel et des stocks)
* document (pièces jointes, plans, rapports)
* planning (plannings de chantier)
* notification (notifications utilisateurs)
* dashboard (tableaux de bord)

**L'inventaire complet et définitif des entités sera établi au LOOP 2.1.**

---

# 4. Attributs, relations et cardinalités (état d'ébauche)

## Cardinalités Merise (issue de database-design.md)

```
UTILISATEUR 0,n ──── 1,1 PROFIL
PROFIL      0,n ──── 0,n PERMISSION   (via PROFIL_PERMISSION)
UTILISATEUR 0,n ──── 0,n PERMISSION   (via UTILISATEUR_PERMISSION)
CHANTIER    1,1 ──── 0,n EQUIPE
CHANTIER    1,1 ──── 0,n TACHE
EQUIPE      1,1 ──── 0,n UTILISATEUR
UTILISATEUR 1,1 ──── 0,n HISTORIQUE_ACTION
```

## MLD prévisionnel

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

# 5. Déroulement de la conception (LOOPs Phase 2)

| Étape | Livrable |
|-------|----------|
| LOOP 2.1 | Inventaire complet des entités + MCC détaillé |
| MCD | Modèle Conceptuel de Données (entités, associations, cardinalités) |
| MLD | Modèle Logique de Données (tables, clés, FKs) |
| MPD | Modèle Physique de Données (types PostgreSQL, index, contraintes) |
| Implémentation | Migrations Flyway + Entités JPA (LOOPs 2.1 → 2.6) |

---

# 6. Contraintes

* Nommage `snake_case` pour tables et colonnes.
* Clés primaires `bigint` auto-incrémentées (sauf tables de liaison en PK composite).
* Dates/timestamps cohérents entre PostgreSQL et Java (voir `DateFormats`).
* Les enums métier (priorite, statut, type) seront des `varchar` avec contraintes de validation.
* Toute migration est atomique et numérotée (`V<num>__<description>.sql`).
* Aucune table métier n'est créée avant la fin de la conception (LOOP 2.x).

---

FIN DU DOCUMENT
