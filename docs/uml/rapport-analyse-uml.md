# Rapport d'analyse — Diagramme de classes UML du projet CMS

- **Projet** : CMS (Chantier Management System) — « BatiFlow »
- **Outil** : PlantUML (rendu PNG + SVG)
- **Date** : 28/08/2026
- **Méthode** : Reverse engineering complet (code Java/JPA → migrations SQL/Flyway → PostgreSQL → services → DTO → frontend) puis reconstruction du diagramme de classes conforme au cours « INTRODUCTION A UML-1.pdf » (NIANGORAN A. K.).
- **Livrables** :
  - `docs/uml/diagramme-classes.mdj` — **projet StarUML 7** (fichier natif, ouvert et vérifié dans StarUML)
  - `docs/uml/diagramme-classes-complet.mdj` — version **avec le domaine Templates** (de secours ; remisé par choix utilisateur)
  - `docs/uml/diagramme-classes.puml` (source PlantUML)
  - `docs/uml/diagramme-classes.png` (rendu PNG, 4096 × 1470 px)
  - `docs/uml/diagramme-classes.svg` (rendu vectoriel)
  - `docs/uml/rapport-analyse-uml.md` (ce rapport)
- **Outil principal** : **StarUML 7.0.0** (`C:\Program Files\StarUML\StarUML.exe`). Le fichier `.mdj` est le format natif de StarUML (JSON) : classes, attributs, opérations, énumérations (compartiment des littéraux), associations avec multiplicités aux deux extrémités et losange de composition pour les 2 relations à cycle de vie lié. Pour exporter l'image : dans StarUML, ouvrir le diagramme puis *File → Export Diagram as Image*.
- **Améliorations v2 du `.mdj`** (retour utilisateur) : (1) cases de classe agrandies → **toutes les méthodes visibles** ; (2) **routage orthogonal** des associations (style rectiligne + voies distinctes par paire) → **plus d'enchevêtrement** ; (3) **domaine Templates retiré** du diagramme principal (version complète conservée séparément).
- **Améliorations v3 du `.mdj`** (retour utilisateur) : **toutes les méthodes de la couche Service** présentes avec **signatures complètes** — paramètres typés (`id : Long`, `request : CreateChantierRequest`, …) et **type de retour** (` : ChantierResponse`). 73 méthodes dans le diagramme principal (15 classes) ; 100 dans la version complète (19 classes). Les entités ne portant que des accesseurs Lombok (getters/setters techniques) restent hors diagramme.

---

## 1. Résumé du projet

Le CMS est un **système de gestion de chantiers BTP** (entreprise de construction). Il permet de gérer des **chantiers**, des **équipes de travail** mobiles, des **tâches** avec **validations** réalisées sur le terrain, des **templates réutilisables** (modèles de chantier et de tâches), le tout protégé par un **RBAC dynamique** (profils + permissions + exceptions individuelles, y compris **scopées à un chantier**). Un module **« Analyse du site »** agrège des données externes (météo, géocodage, environnement) pour aider à la préparation d'un chantier.

Le backend est un **monolithe modulaire Spring Boot** ; le frontend est une SPA **React/TypeScript** ; les données sont persistées dans **PostgreSQL** dont le schéma est entièrement piloté par **Flyway** (`ddl-auto: none`).

Évolution notable (traçable dans les migrations `V1 → V21`) : le MCD initial prévoyait 19 entités dont `Planning`, `Commentaire`, `Document`, `PieceJointe`, `PhotoChantier`, `HistoriqueAction`, `JournalConnexion`. Ces modules ont été **volontairement retirés** (`V6`, `V7`) avant mise en production, et remplacés/étendus par le module **Templates** (`V2`, `V3`, `V5`), l'**affectation directe utilisateur↔chantier** (`V9`), les **permissions scopées par chantier** (`V11`) et le **fractionnement des affectations de tâche** (`V18`).

## 2. Technologies détectées

| Couche | Technologies |
|--------|--------------|
| **Backend** | Java 25, Spring Boot 3.5.3, Spring Web, Spring Data JPA (Hibernate), Spring Security + **JWT (jjwt 0.12.6)**, Validation, Lombok, MapStruct 1.6.3, springdoc-openapi 2.8.5, Testcontainers 1.20.4 |
| **Base de données** | **PostgreSQL** (dev : `gestion_de_chantier` sur `localhost:5432`, user `admin` ; test : `gestion_de_chantier_test`) ; **Flyway** (`db/migration/V1..V21`) |
| **Docker** | Conteneurs PostgreSQL `chantier-db` / `batiflow-db`, `pgadmin4` (port 5050) lancés via `start.ps1` |
| **Frontend** | React 19, TypeScript, Vite, Tailwind, TanStack React Query, axios, React Router, Leaflet |
| **Documentation projet** | `docs/` : MCD, MLD relationnel, dictionnaire, RBAC dynamique, architecture, API, etc. |

## 3. Architecture détectée

Monolithe modulaire : chaque domaine est un **package autonome** `controller → service → repository → entity → dto → mapper`.

```
com.cms
├── auth            (authentification : inscription, connexion, JWT, me)
├── utilisateur     (compte + exceptions de permission individuelles)
├── profil          (rôles + profil_permission)
├── permission      (référentiel de permissions)
├── chantier        (chantier + affectation utilisateur + permission scopée)
├── equipe          (équipe, membre_equipe, affectation_equipe_chantier)
├── tache           (tâche, affectations, validations)
├── template        (templates de chantier et de tâches)
├── analyse         (clients externes : géocodage, météo, environnement)
├── security        (filtre JWT, RBAC Niveau 1, périmètre Niveau 2, droits)
├── common / config / exception
```

**Sécurité (2 niveaux)** :
- **Niveau 1 — RBAC fonctionnel** : `PermissionEvaluator` → `DroitsService.calculerDroits()`.
  Formule réelle : `droits = permissions(profil) + ACCORDER − REFUSER` (REFUSER prioritaire), relus **en base à chaque requête** (aucun cache, aucun droit dans le JWT). Le profil système `ADMINISTRATEUR` possède automatiquement *toutes* les permissions du référentiel.
- **Niveau 2 — périmètre des données** : `DataAccessService` restreint la visibilité (chantiers d'une équipe active de l'utilisateur + affectations directes + exceptions ACCORDER scopées ; équipes dont il est membre).

## 4. Classes retenues

La **source de vérité** est le schéma Flyway final (`ddl-auto: none`, donc les tables sont uniquement créées par les migrations). 19 classes métier retenues (toutes vérifiées : annotation `@Entity` ✓, table SQL ✓, DTO ✓, frontend ✓) :

### RBAC (6 classes)
| Classe UML | Table | Rôle |
|------------|-------|------|
| `Utilisateur` | `utilisateur` | Compte de connexion (auth), porteur d'un profil obligatoire |
| `Profil` | `profil` | Rôle métier portant des droits par défaut |
| `Permission` | `permission` | Unité de droit (`nomPermission` = `MODULE_ACTION`) |
| `ProfilPermission` | `profil_permission` | Association Profil ↔ Permission (droits par défaut) |
| `UtilisateurPermission` | `utilisateur_permission` | Exception individuelle **ACCORDER / REFUSER** (hors chantier) |
| `UtilisateurPermissionChantier` | `utilisateur_permission_chantier` | Exception RBAC **scopée à un chantier** |

### Organisation (5 classes)
| Classe UML | Table | Rôle |
|------------|-------|------|
| `Chantier` | `chantier` | Projet de construction central |
| `Equipe` | `equipe` | Groupe de travail mobile |
| `MembreEquipe` | `membre_equipe` | Appartenance Utilisateur ↔ Équipe (CHEF/OUVRIER) |
| `AffectationEquipeChantier` | `affectation_equipe_chantier` | Équipe affectée à un chantier par période (statut ACTIVE/TERMINEE) |
| `AffectationUtilisateurChantier` | `affectation_utilisateur_chantier` | Affectation **directe** d'un utilisateur à un chantier |

### Travail (5 classes)
| Classe UML | Table | Rôle |
|------------|-------|------|
| `Tache` | `tache` | Unité de travail d'un chantier |
| `AffectationTacheUtilisateur` | `affectation_tache_utilisateur` | Tâche affectée à un utilisateur (REALISATEUR/CONTROLEUR) |
| `AffectationTacheEquipe` | `affectation_tache_equipe` | Tâche affectée à une équipe (REALISATEUR/CONTROLEUR) |
| `ValidationTache` | `validation_tache` | Décision VALIDE/REFUSE (historique conservé) |

### Templates (4 classes)
| Classe UML | Table | Rôle |
|------------|-------|------|
| `TemplateChantier` | `template_chantier` | Modèle complet de construction (import = copie métier) |
| `TemplateTache` | `template_tache` | Groupe réutilisable de tâches modèles |
| `TemplateChantierTache` | `template_chantier_tache` | Association N:N TemplateChantier ↔ TemplateTache |
| `TemplateTacheTache` | `template_tache_tache` | Tâche structurée d'un TemplateTache (snapshot indépendant) |

**Exclues à dessein** : les entités des modules supprimés (`Planning`, `Commentaire`, `Document`, `PieceJointe`, `PhotoChantier`, `HistoriqueAction`, `JournalConnexion` — tables DROPPED en V6/V7 et packages retirés) ; l'entité legacy `AffectationTache` (voir §13) ; les classes purement techniques du module `analyse` (clients HTTP sans persistance) et les couches `security`, `config`, `common`, DTO et mappers.

## 5. Relations

| Association | Cardinalité | Rôle / Pivot | Preuve |
|-------------|-------------|--------------|--------|
| Utilisateur **–** Profil | `0..*` – `1` | possède | `@JoinColumn(profil_id, nullable=false)`, FK NOT NULL |
| Profil **–** ProfilPermission | `1` – `0..*` | porte | `@OneToMany(mappedBy="profil")` ; profil_id + permission_id, UNIQUE(P,perm) |
| Permission **–** ProfilPermission | `1` – `0..*` | attribuée à | idem |
| Utilisateur **–** UtilisateurPermission | `1` – `0..*` | reçoit | `@JoinColumn(utilisateur_id)`, UK(utilisateur, permission, type) |
| Permission **–** UtilisateurPermission | `1` – `0..*` | concerne | `@JoinColumn(permission_id)` |
| Utilisateur **–** UtilisateurPermission | `1` – `0..*` | aAutorisé (createdBy) | `@JoinColumn(created_by, nullable=false)` |
| Utilisateur **–** UtilisateurPermissionChantier | `1` – `0..*` | reçoit (scopée) | `@JoinColumn(utilisateur_id)` |
| Chantier **–** UtilisateurPermissionChantier | `1` – `0..*` | périmètre | `@JoinColumn(chantier_id)` |
| Permission **–** UtilisateurPermissionChantier | `1` – `0..*` | concerne | `@JoinColumn(permission_id)` |
| Utilisateur **–** UtilisateurPermissionChantier | `1` – `0..*` | aAutorisé (createdBy) | `@JoinColumn(created_by)` |
| Utilisateur **–** Chantier | `0..1` – `0..*` | responsable | `@ManyToOne(fetch=LAZY)` **optionnel** (`responsable_id` nullable) |
| Chantier **–** Tache | `1` – `0..*` | contient | `@JoinColumn(chantier_id, nullable=false)` |
| Utilisateur **–** Tache | `1` – `0..*` | aCréé (createdBy) | `@JoinColumn(created_by, nullable=false)` |
| Utilisateur **–** Equipe | via MembreEquipe | appartient | deux `@ManyToOne` NOT NULL |
| Equipe **–** MembreEquipe | `1` – `0..*` | composée de | `@OneToMany` ; UK(utilisateur, equipe) |
| Equipe **–** AffectationEquipeChantier | `1` – `0..*` | travaille sur | `@ManyToOne(equipe_id)` NOT NULL |
| Chantier **–** AffectationEquipeChantier | `1` – `0..*` | reçoit | `@ManyToOne(chantier_id)` NOT NULL |
| Utilisateur **–** AffectationUtilisateurChantier | `1` – `0..*` | affecté à | `@ManyToOne` NOT NULL, UK(user, chantier) |
| Chantier **–** AffectationUtilisateurChantier | `1` – `0..*` | autorise | idem |
| Tache **–** AffectationTacheUtilisateur | `1` – `0..*` | ciblée par | `@ManyToOne(tache_id)` NOT NULL |
| Utilisateur **–** AffectationTacheUtilisateur | `1` – `0..*` | exécute | `@ManyToOne(utilisateur_id)` NOT NULL |
| Tache **–** AffectationTacheEquipe | `1` – `0..*` | ciblée par | `@ManyToOne(tache_id)` NOT NULL |
| Equipe **–** AffectationTacheEquipe | `1` – `0..*` | exécute | `@ManyToOne(equipe_id)` NOT NULL |
| Tache **–** ValidationTache | `1` – `0..*` | subit | `@ManyToOne(tache_id)` NOT NULL |
| Utilisateur **–** ValidationTache | `1` – `0..*` | valide (validateur) | `@ManyToOne(validateur_id)` NOT NULL |
| TemplateChantier **–** TemplateChantierTache | `1` – `0..*` (◆ composition) | référence | `ON DELETE CASCADE` (V2) |
| TemplateTache **–** TemplateChantierTache | `1` – `0..*` | réutilisé dans | `@ManyToOne(template_tache_id)` NOT NULL |
| TemplateTache **–** TemplateTacheTache | `1` – `0..*` (◆ composition) | structure | `ON DELETE CASCADE` (V5) |
| Utilisateur **–** TemplateChantier/TemplateTache/TemplateTacheTache | `1` – `0..*` | aCréé (createdBy) | `@ManyToOne(created_by)` NOT NULL |

**Trames d'audit (dates) systématiques** : `dateCreation` / `dateModification` présents sur les entités manipulables ; `createdBy` tracé sur les décisions RBAC et les créations de tâches/templates.

## 6. Cardinalités — justification

1. **`profil_id NOT NULL`** (FK) ⇒ un Utilisateur a **exactement 1** Profil ; un Profil est partagé par `0..*` utilisateurs.
2. **`affectation_tache_*`.tache_id NOT NULL** ⇒ toute affectation cible **une et une seule** Tache ; une Tache a `0..*` affectations.
3. **Chantier.responsable optionnel** ⇒ `0..1` côté responsable (`@ManyToOne` sans `optional=false`), `0..*` côté chantiers par utilisateur (règle métier : un chantier **actif** doit avoir un responsable — contrôlé en service, pas en base).
4. Les **tables de liaison** ont été volontairement éclatées (V18) : `affectation_tache` → `affectation_tache_utilisateur` **+** `affectation_tache_equipe` ⇒ deux associations binaires **indépendantes** (aucune contrainte de type XOR résiduelle en base).
5. **Templates** : `template_chantier_tache` et `template_tache_tache` portent des `ON DELETE CASCADE` ⇒ **composition** (cycle de vie lié au conteneur). Toutes les autres relations sont des **associations simples** (aucun cascade de suppression en base : la suppression d'un chantier/équipe/profil est protégée ou interdite par le code).

## 7. Héritages

**Aucun héritage métier** dans le modèle implémenté :
- aucun `@MappedSuperclass`, aucun `@Inheritance`, aucun `extends` entre entités ;
- `AffectationTacheUtilisateur` et `AffectationTacheEquipe` **ne partagent pas** de super-classe : ce sont deux classes distinctes correspondant aux deux tables issues de V18 (le regroupement hérité aurait été possible mais **n'existe pas** dans le code) ;
- les seuls `implements` rencontrés sont techniques (`UserDetails`, interfaces de service `*ServiceImpl implements *Service`), hors du périmètre métier du diagramme.

## 8. Associations importantes

- **Classe d'association RBAC** : `ProfilPermission` porte les droits par défaut ; `UtilisateurPermission` et `UtilisateurPermissionChantier` portent les exceptions (`type : ACCORDER/REFUSER`, audit `createdBy`). Le cours prévoit qu'une association porteuse d'attributs peut être transformée en **ensemble d'associations binaires** (page « classe association », §10 du cours) — c'est l'option retenue, en accord avec le modèle JPA réel.
- **Cœur organisationnel** : `AffectationEquipeChantier` est la base de la **sécurité par périmètre** (visibilité des chantiers selon les équipes actives). `AffectationUtilisateurChantier` (V9) étend cette vision aux utilisateurs affectés directement.
- **Cycle de validation** : `ValidationTache` est un **historique** immuable de décisions `VALIDE/REFUSE` liées à un `validateur` et une date ; le statut de la `Tache` (`A_FAIRE/EN_COURS/VALIDE/REFUSE`) et sa `progression` sont dérivés par la couche service.
- **Templates** : les imports produisent des **copies métier indépendantes** (aucune FK `template_*` dans `chantier`/`tache` — principe documenté V2/V5).

## 9. Composition / Agrégation

| Type | Relation | Justification |
|------|----------|---------------|
| **Composition** (◆ plein) | `TemplateChantier` ◆— `* TemplateChantierTache` | `ON DELETE CASCADE` sur `template_chantier_id` : les lignes d'association appartiennent au template et disparaissent avec lui |
| **Composition** (◆ plein) | `TemplateTache` ◆— `* TemplateTacheTache` | `ON DELETE CASCADE` sur `template_tache_id` : suppression du template ⇒ suppression de ses sous-tâches |
| Association simple | toutes les autres | Aucun cascade en base ; cycles de vie indépendants (le cours définit la composition forte comme « destruction du conteneur ⇒ destruction des composants ») |

Aucune **agrégation** (losange vide) n'est retenue : aucune relation « contenant/partagé » remplissant la sémantique du cours n'existe dans le schéma réel.

## 10. Correspondance avec la base de données

Table de correspondance complète (source : `db/migration/V1..V21`, schéma final) :

| Classe UML | Table | PK | Contraintes clés |
|------------|-------|----|------------------|
| Utilisateur | `utilisateur` | id | UK email ; FK profil_id NOT NULL |
| Profil | `profil` | id | UK nom |
| Permission | `permission` | id | UK nom_permission |
| ProfilPermission | `profil_permission` | id | UK(profil_id, permission_id) |
| UtilisateurPermission | `utilisateur_permission` | id | UK(utilisateur_id, permission_id, type) ; CHECK type |
| UtilisateurPermissionChantier | `utilisateur_permission_chantier` | id | UK(utilisateur_id, chantier_id, permission_id, type) ; CHECK type |
| Chantier | `chantier` | id | CHECK statut, progression 0-100, dates ; FK responsable_id |
| Equipe | `equipe` | id | — |
| MembreEquipe | `membre_equipe` | id | UK(utilisateur_id, equipe_id) ; CHECK rôle |
| AffectationEquipeChantier | `affectation_equipe_chantier` | id | CHECK statut, dates ; FK equipe_id, chantier_id |
| AffectationUtilisateurChantier | `affectation_utilisateur_chantier` | id | UK(utilisateur_id, chantier_id) |
| Tache | `tache` | id | CHECK priorite, status (sans TERMINE, V17), progression, dates ; FK chantier_id, created_by |
| AffectationTacheUtilisateur | `affectation_tache_utilisateur` | id | CHECK rôle ; FK tache_id, utilisateur_id |
| AffectationTacheEquipe | `affectation_tache_equipe` | id | CHECK rôle ; FK tache_id, equipe_id |
| ValidationTache | `validation_tache` | id | CHECK statut ; FK tache_id, validateur_id |
| TemplateChantier | `template_chantier` | id | UK nom ; CHECK type_construction, statut, durée |
| TemplateTache | `template_tache` | id | CHECK priorite, durée |
| TemplateChantierTache | `template_chantier_tache` | id | UK(template_chantier_id, template_tache_id) ; FK CASCADE |
| TemplateTacheTache | `template_tache_tache` | id | UK(template_tache_id, titre) ; FK CASCADE |

Les **énumérés** sont stockés en `VARCHAR` + contrainte `CHECK` (convention MLD §5.4) : cohérent avec le stéréotype UML « enumeration ».

## 11. Correspondance avec le code Java

- Toutes les classes du diagramme sont des **entités JPA** annotées `@Entity` dans `com.cms.*.entity` (21 `@Entity` au total, `AffectationTache` restant du code mort).
- Relations portées uniquement par **`@ManyToOne` / `@OneToMany(mappedBy=…)`** : aucun `@ManyToMany`, aucun `@JoinTable`, aucun `@EmbeddedId`, aucun `@Inheritance`.
- Toutes les FK sont déclarées côté enfant (`@JoinColumn`), les listes du parent étant en `mappedBy`.
- Services réels utilisés pour les opérations réintégrées dans le compartiment « opérations » du diagramme : `ChantierService`, `EquipeService`/`MembreEquipeService`/`AffectationEquipeChantierService`, `TacheService`/`AffectationTacheService`/`ValidationTacheService`, `TemplateChantierService`/`TemplateTacheService`, `UtilisateurService`/`ProfilService`/`PermissionService`, `UtilisateurPermissionChantierService`, `DroitsService`.
- Les entités ne portent que des accesseurs générés par Lombok (`getX`/`setX`) : omises conformément à la pédagogie du diagramme (le cours autorise à ne pas représenter les méthodes purement techniques).

## 12. Règles UML du cours appliquées

Règles du document « INTRODUCTION A UML-1.pdf » respectées :

1. **Rectangles à 3 compartiments** (nom gras centré / attributs / opérations) — pages 71-74.
2. **Visibilité** : `-` attribut privé, `+` opération publique — page 29.
3. **Attributs typés** avec **multiplicité entre crochets** `[1]` / `[0..1]` après le type — page 82.
4. **Contraintes** entre accolades : `{unique}` sur les attributs uniques — pages 78-80.
5. **Associations** : traits simples, **nom de l'association** (verbe), **rôles** et **cardinalités explicites** aux deux extrémités ; cardinalité par défaut = 1 sinon précisé — pages 87-91.
6. **Classe d'association** : les associations porteuses d'attributs (`MembreEquipe`, `AffectationEquipeChantier`, `UtilisateurPermission`, etc.) sont converties en **associations binaires** conformément à la remarque du cours (page 104) et au modèle relationnel réel.
7. **Composition** : losange plein côté conteneur, exclusivement pour les relations à cycle de vie lié (`Template*`) — pages 105-106.
8. **Énumérations** : classeurs stéréotypés `« enumeration »` listant les littéraux — page 84.
9. **Aucun héritage fabriqué** : pas de « EST-UN » dans le modèle ⇒ aucune généralisation dessinée (pages 30-34, 108).
10. **Généralisation/réalisation/dépendance** : non utilisées car non pertinentes entre classes métier (les interfaces Spring sont techniques).

## 13. Incohérences détectées

1. **`AffectationTache` = code mort (le plus important)** : l'entité JPA `com.cms.tache.entity.AffectationTache` mappe `@Table(name="affectation_tache")`, or cette table est **supprimée** par la migration `V18`. Son repository (`AffectationTacheRepository`) n'est **jamais appelé** ; `Utilisateur.affectationTaches`, `Equipe.affectationTaches` et `Tache.affectations` référencent cette entité. Les services opérationnels (`AffectationTacheServiceImpl`, `TacheServiceImpl`) n'utilisent que `AffectationTacheUtilisateur` / `AffectationTacheEquipe`. → **Le diagramme représente la réalité en base** (les 2 tables éclatées), l'entité legacy est exclue.
2. **Écart MCD ↔ implémentation** : `docs/MCD-final.md` liste 19 entités dont `Planning`, `Commentaire`, `Document`, `PieceJointe`, `PhotoChantier`, `HistoriqueAction`, `JournalConnexion`. Toutes ont été **supprimées en base (V6/V7)** et retirées du backend. Le schéma final contient 19 tables que l'on retrouve dans les 19 classes du diagramme : les tables conservées de V1, les tables Templates (V2/V5), l'`affectation_utilisateur_chantier` (V9) et l'`utilisateur_permission_chantier` (V11). → Entités des modules supprimés non représentées (non implémentées).
3. **`AffectationTacheEquipe` sans dates d'audit** : `V21` ajoute `date_creation`/`date_modification` uniquement sur `affectation_tache_utilisateur` ; `affectation_tache_equipe` n'a que `date_affectation`. Le diagramme reflète rigoureusement cette asymétrie.
4. **Emplacement du repository** : `AffectationEquipeChantierRepository` vit dans `chantier.repository` alors que l'entité est dans `equipe.entity` (détail d'organisation, sans impact sur le modèle).
5. **Frontend partiellement obsolète** : `frontend/src/core/lib/types.ts` (« BatiFlow ERP ») contient des champs jamais fournis par l'API (`Tache.cout`, `ChecklistItem`, `User.role`, `User.avatar`…) ; les types **alignés** sur le backend sont dans `core/api/types.ts`. Seuls ces derniers ont été utilisés en source complémentaire (aucun impact sur le diagramme).
6. **Règle de validation de tâche** : la contrainte « au moins une cible (utilisateur OU équipe) » était un `CHECK` dans V1 (`ck_affectation_tache_cible`) ; elle a disparu avec le fractionnement V18. L'invariant est désormais **appliqué uniquement en service** (`AffectationTacheServiceImpl`) — signalé pour information.

## 14. Décisions prises

1. **Source de vérité = schéma Flyway final** (base réelle), complété par les entités JPA et les services. `ddl-auto: none` ⇒ la base ne peut être altérée par Hibernate.
2. Représenter les **19 classes persistées opérationnelles** ; sont volontairement hors diagramme : le module `analyse` (clients externes sans persistance), la couche technique `security/config/common`, les DTO et mappers.
3. Exclure l'entité legacy `AffectationTache` (table inexistante) — voir §13.1.
4. Convertir les **classes d'association** en entités à part entière reliées par des **associations binaires** (conformité cours + modèle JPA).
5. Utiliser la **composition** (losange plein) uniquement pour `TemplateChantier`→`TemplateChantierTache` et `TemplateTache`→`TemplateTacheTache`, seules relations à `ON DELETE CASCADE`.
6. Rejeter une **agrégation** et un **héritage** non justifiés (le cours interdit de créer un héritage sur la seule ressemblance d'attributs).
7. Afficher dans le compartiment opérations un **sous-ensemble fidèle des opérations réelles** des services (comportement métier), sans les accesseurs Lombok ni les DTO.
8. Cardinalités justifiées systématiquement par (annotation JPA + FK SQL + logique service + documentation), jamais « par défaut ».

## 15. Vérification finale

**Contrôle du diagramme :**
- Fichiers générés et **vérifiés** : `.puml` compile sans erreur, `.png` valide (4096×1470 px, signature PNG contrôlée), `.svg` généré avec tous les libellés/cardinalités présents.
- 19 classes, 12 énumérations, ~30 associations — toutes issues du code et des migrations ; aucune classe inventée.
- Cardinalités présentes sur **chaque** association ; rôles (`responsable`, `validateur`, `createdBy`, …) nommés.

**Test de cohérence inversé** (du diagramme vers la base) : à partir du diagramme on reconstitue exactement le MLD —
chaque classe = table (PK auto `id`), chaque association simple = FK côté enfant (`1` → `NOT NULL`, `0..1` → nullable), chaque composition = `ON DELETE CASCADE`, chaque énumération = VARCHAR + CHECK. Aucune information ne manque pour reconstruire le modèle relationnel final (V1..V21).

**Pour mémoire universitaire** : le diagramme représente le **modèle réellement implémenté** du système (priorité : exactitude > cohérence > respect du cours > lisibilité > esthétique). Les divergences entre la documentation conceptuelle (MCD/MLD initiaux) et le système en production sont explicitement documentées (§13), ce qui valorise la démarche de reverse engineering.

---

*Fin du rapport.*