# MLD — MODULE TEMPLATE CHANTIER / TEMPLATE TÂCHE

Version : 1.1
Projet : CMS (Chantier Management System)
Statut : Conception uniquement — aucun code/table créé
Contexte : LOOP 5.4 — MLD relationnel (de LOOP 5.2 MCD + LOOP 5.3 dictionnaire + CORE backend)

---

# 1. OBJECTIF

Construire le MLD relationnel du module Template à partir :

- du MCD validé au LOOP 5.2 ;
- du dictionnaire des données validé au LOOP 5.3 ;
- du CORE backend existant.

Cette étape prépare directement l'implémentation JPA et SQL.

---

# 2. RÈGLE ABSOLUE

**NE PAS modifier le CORE existant.**

Les 19 entités existantes restent strictement inchangées. Ne sont modifiés :

❌ aucune Entity existante
❌ aucun attribut existant
❌ aucune relation existante
❌ aucun Repository / Service / Controller / DTO / Mapper
❌ aucun fichier Java
❌ aucune migration Flyway existante
❌ aucune permission RBAC existante

**Aucune nouvelle permission RBAC n'est créée à ce LOOP.**

---

# 3. PÉRIMÈTRE DU MODULE

Le module représente exactement deux concepts métier :

## 3.1 TemplateChantier

Modèle **COMPLET** de construction d'un bâtiment.
Exemples : Maison R+1, Immeuble R+5, Magasin, Villa.
Sert à préparer rapidement un nouveau chantier.

## 3.2 TemplateTache

**GROUPE RÉUTILISABLE DE TÂCHES**.
Exemples : Dalle béton, Toiture tôle, Toiture dalle, Installation électrique, Peinture.
Un TemplateTache **n'est PAS un chantier**.

---

# 4. IMPORTATION (FONCTIONNEMENT FUTUR)

Le système devra permettre ultérieurement :

| Cas | Flux | Règle |
|---|---|---|
| CAS 1 | TemplateChantier → IMPORT → Chantier → Tache | L'import crée des données **indépendantes** (snapshot) |
| CAS 2 | TemplateTache → IMPORT → Chantier existant → Tache | Import de tâches types dans un chantier existant |
| CAS 3 | TemplateChantier contient des TemplateTache | Relation structurelle (association N,N) |

La copie = **snapshot** : aucune donnée importée ne reste liée au template
de manière maintenue.

---

# 5. CONVENTIONS (ALIGNÉES CORE — `V1__init.sql`)

- Tables/colonnes en `snake_case`.
- Énumérés en `VARCHAR` + contrainte `CHECK`.
- Contraintes nommées : `pk_*`, `fk_*`, `uk_*`, `ck_*`, `ix_*`.
- PK `BIGSERIAL`, FK `BIGINT`.
- Champs d'audit `date_creation` / `date_modification` en `TIMESTAMP`.
- ddl-auto Hibernate = none (aucune création automatique).

Enums alignés strictement sur le CORE :

| Enum | Valeurs |
|---|---|
| statut (template) | ACTIF, INACTIF |
| priorite | HAUTE, MOYENNE, BASSE |
| type_construction | MAISON_R1, IMMEUBLE_R5, MAGASIN, VILLA, ENTREPOT |

---

# 6. TABLES LOGIQUES (3 NOUVELLES — AUCUNE TABLE CORE MODIFIÉE)

## 6.1 TABLE `template_chantier`

Modèle complet de construction (concept 1).

| Colonne | Type | Contrainte |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `nom` | VARCHAR(255) | NOT NULL, UNIQUE (`uk_template_chantier_nom`) |
| `description` | VARCHAR(1000) | NULL |
| `type_construction` | VARCHAR(20) | NOT NULL, CHECK `ck_template_chantier_type_construction` IN ('MAISON_R1','IMMEUBLE_R5','MAGASIN','VILLA','ENTREPOT') |
| `budget_estime` | NUMERIC(15,2) | NULL |
| `duree_estimee_jours` | INTEGER | NULL, CHECK `ck_template_chantier_duree` (duree > 0) |
| `statut` | VARCHAR(10) | NOT NULL DEFAULT 'ACTIF', CHECK `ck_template_chantier_statut` IN ('ACTIF','INACTIF') |
| `date_creation` | TIMESTAMP | NOT NULL |
| `date_modification` | TIMESTAMP | NOT NULL |
| `created_by` | BIGINT | NOT NULL, FK → `utilisateur.id` |

Index : `ix_template_chantier_created_by` (created_by)

## 6.2 TABLE `template_tache`

Groupe réutilisable de tâches (concept 2).

| Colonne | Type | Contrainte |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `titre` | VARCHAR(255) | NOT NULL |
| `description` | VARCHAR(1000) | NULL |
| `priorite` | VARCHAR(10) | NOT NULL DEFAULT 'MOYENNE', CHECK `ck_template_tache_priorite` IN ('HAUTE','MOYENNE','BASSE') |
| `duree_estimee_jours` | INTEGER | NULL, CHECK `ck_template_tache_duree` (duree > 0) — **non copiée** vers la tâche réelle |
| `date_creation` | TIMESTAMP | NOT NULL |
| `date_modification` | TIMESTAMP | NOT NULL |
| `created_by` | BIGINT | NOT NULL, FK → `utilisateur.id` |

Index : `ix_template_tache_created_by` (created_by)

## 6.3 TABLE `template_chantier_tache`

Association N:N — porte l'ordre d'exécution (CAS 3).

| Colonne | Type | Contrainte |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `template_chantier_id` | BIGINT | NOT NULL, FK → `template_chantier.id` |
| `template_tache_id` | BIGINT | NOT NULL, FK → `template_tache.id` |
| `ordre` | INTEGER | NOT NULL, CHECK `ck_template_chantier_tache_ordre` (ordre >= 1) |

Contraintes :

- `uk_template_chantier_tache_ordre` UNIQUE (`template_chantier_id`, `ordre`) — **ordre unique par template** ;
- `uk_template_chantier_tache_tache` UNIQUE (`template_chantier_id`, `template_tache_id`) — une même tâche type une seule fois par template ;
- `fk_template_chantier_tache_chantier` → `template_chantier.id` (ON DELETE CASCADE : supprimer un TemplateChantier supprime ses liens) ;
- `fk_template_chantier_tache_tache` → `template_tache.id` (les TemplateTache restent, réutilisables entre templates).

Index : `ix_template_chantier_tache_tache_id` (template_tache_id)

---

# 7. RELATIONS (MLD)

| Relation | Type | Description |
|---|---|---|
| `utilisateur` 1,N — `template_chantier` | 1,N (FK created_by) | Créateur du template |
| `utilisateur` 1,N — `template_tache` | 1,N (FK created_by) | Créateur du template de tâches |
| `template_chantier` N,N — `template_tache` | N,N via `template_chantier_tache` | CAS 3 : contient des tâches types, ordonnées |

Aucune relation avec les 19 tables CORE en écriture. Seules les FK de
traçabilité `created_by` pointent vers `utilisateur` (lecture).

---

# 8. RÈGLES DE GESTION

1. Suppression d'un `template_chantier` → cascade sur `template_chantier_tache`
   ; les `template_tache` associées restent (réutilisables).
2. Import CAS 1 : TemplateChantier + ses TemplateTache ordonnées → création
   d'un `chantier` + des `tache` correspondantes (données indépendantes).
3. Import CAS 2 : TemplateTache → `tache` d'un chantier existant.
4. `duree_estimee_jours` jamais recopiée lors des imports (le CORE n'a pas
   d'attribut durée sur `tache`).
5. Un TemplateTache n'est jamais un chantier (pas de FK vers chantier sur
   `template_tache`).

---

# 9. POINTS À VALIDER AVANT IMPLÉMENTATION

1. PK simple (`id` BIGSERIAL) sur l'association vs PK composite
   (`template_chantier_id`, `template_tache_id`). Recommandation : PK simple
   + `uk_..._ordre`.
2. `nom` UNIQUE sur `template_chantier` : à confirmer (deux templates au
   même nom ?).
3. `created_by` NOT NULL sur les 2 tables maîtres : à confirmer (cohérent
   avec `tache.created_by`).
4. Traceabilité "chantier issu d'un template" (CAS 1) : non portée par ce
   MLD — tout ajout sur `chantier` modifierait le CORE (interdit ici). À
   trancher au LOOP d'implémentation (mécanisme externe ou décision métier).
5. Migration Flyway : V2 dédiée aux 3 tables Template (aucune retouche V1).

---

# 10. RÉCAPITULATIF

- 3 tables créées, **0 table CORE modifiée**.
- 3 FK (2 traçabilité created_by + 1 association).
- 3 CHECK, 3 UNIQUE, 3 index.
- 0 nouvelle permission RBAC.

---

FIN DU MLD MODULE TEMPLATE
