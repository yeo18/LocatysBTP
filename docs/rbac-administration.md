# Administration RBAC — Gestion des profils, permissions et droits effectifs

Version : 1.0
LOOP : 3.12
Date : 06/08/2026

---

## 1. Vue d'ensemble

Le RBAC est **100 % dynamique** : tous les rôles (profils), toutes les
permissions et toutes les associations sont stockés en base (PostgreSQL) et
administrés via l'API REST. Aucun rôle n'est codé en dur dans le code Java,
aucune permission n'est embarquée dans le JWT.

```
UTILISATEUR ──profil──> PROFIL ──<profil_permission>──> PERMISSION
   │
   └──<utilisateur_permission (ACCORDER / REFUSER)>──> PERMISSION
```

---

## 2. Gestion des profils

Service : `com.cms.profil.service.ProfilService(Impl)`

| Opération | Méthode | Règle |
|-----------|---------|-------|
| Créer | `creerProfil` | nom obligatoire et unique (409 sinon) |
| Modifier | `modifierProfil` | nom unique ; profils système non renommables |
| Rechercher | `trouverParId` / `rechercher` / `listerActifs` | 404 si absent |
| Activer | `activerProfil` | déjà actif → 400 |
| Désactiver | `desactiverProfil` | profils système interdits (409) |
| Supprimer | `supprimerProfil` | profils système / encore attribués interdits |

Profils système protégés (`SystemRoles`) : `ADMINISTRATEUR` et
`UTILISATEUR_STANDARD` — non supprimables, non renommables, non désactivables.

Endpoints (`ProfilController`, `/api/v1/profils`) :

| Méthode | Route | Permission requise |
|---------|-------|--------------------|
| POST | `/api/v1/profils` | `PROFIL_CREER` |
| GET | `/api/v1/profils` | `PROFIL_LIRE` |
| GET | `/api/v1/profils/{id}` | `PROFIL_LIRE` |
| PUT | `/api/v1/profils/{id}` | `PROFIL_MODIFIER` |
| DELETE | `/api/v1/profils/{id}` | `PROFIL_SUPPRIMER` |
| PATCH | `/api/v1/profils/{id}/activer` | `PROFIL_MODIFIER` |
| PATCH | `/api/v1/profils/{id}/desactiver` | `PROFIL_MODIFIER` |
| POST | `/api/v1/profils/{id}/permissions/{permissionId}` | `PROFIL_MODIFIER` |
| DELETE | `/api/v1/profils/{id}/permissions/{permissionId}` | `PROFIL_MODIFIER` |
| GET | `/api/v1/profils/{id}/permissions` | `PROFIL_LIRE` |

---

## 3. Gestion des permissions (référentiel)

Service : `com.cms.permission.service.PermissionService(Impl)`

| Opération | Méthode | Règle |
|-----------|---------|-------|
| Créer | `creerPermission` | codePermission obligatoire + unique ; module/action obligatoires ; convention `MODULE_ACTION` (MAJUSCULES) |
| Modifier | `modifierPermission` | code unique si changement ; module/action normalisés |
| Rechercher | `trouverParId` / `trouverParCode` / `rechercher` / `listerParModule` | 404 si absent |
| Activer | `activerPermission` | référentiel permanent : ré-attribution possible |
| Désactiver | `desactiverPermission` | retire tous les octrois (profils + utilisateurs), conserve le référentiel |
| Supprimer | `supprimerPermission` | supprime la permission et ses octrois |

Le référentiel est quasi-statique : il est maintenu uniquement par
l'administration. Une permission désactivée conserve son enregistrement
(référentiel) ; seuls ses octrois sont purgés. Sa réactivation n'octroie rien
automatiquement : il faut ré-attribuer (cas par cas) via
`ProfilService.ajouterPermission` ou `UtilisateurService.accorderPermission`.

Endpoints (`PermissionController`, `/api/v1/permissions`) :

| Méthode | Route | Permission requise |
|---------|-------|--------------------|
| POST | `/api/v1/permissions` | `PERMISSION_CREER` |
| GET | `/api/v1/permissions` | `PERMISSION_LIRE` |
| GET | `/api/v1/permissions/{id}` | `PERMISSION_LIRE` |
| GET | `/api/v1/permissions/code/{code}` | `PERMISSION_LIRE` |
| GET | `/api/v1/permissions/modules/{module}` | `PERMISSION_LIRE` |
| PUT | `/api/v1/permissions/{id}` | `PERMISSION_MODIFIER` |
| DELETE | `/api/v1/permissions/{id}` | `PERMISSION_SUPPRIMER` |
| PATCH | `/api/v1/permissions/{id}/activer` | `PERMISSION_MODIFIER` |
| PATCH | `/api/v1/permissions/{id}/desactiver` | `PERMISSION_MODIFIER` |

---

## 4. Association profil — permission

Service : `ProfilService` (N-N via `ProfilPermission`).

| Opération | Méthode | Règle |
|-----------|---------|-------|
| Attribuer | `ajouterPermission(profilId, permissionId)` | association unique (409 si déjà attribuée) |
| Retirer | `retirerPermission(profilId, permissionId)` | 400 si non attribuée |
| Lister | `listerPermissions(profilId)` | associations du profil |

Un profil peut posséder plusieurs permissions ; une permission peut appartenir
à plusieurs profils.

---

## 5. Exceptions individuelles utilisateur — ACCORDER / REFUSER

Service : `com.cms.utilisateur.service.UtilisateurService(Impl)`.

| Opération | Méthode | Endpoint |
|-----------|---------|----------|
| ACCORDER | `accorderPermission` | `POST /api/v1/users/{id}/permissions/{permissionId}/accorder` |
| REFUSER | `refuserPermission` | `POST /api/v1/users/{id}/permissions/{permissionId}/refuser` |
| Retirer une exception | `retirerPermissionIndividuelle` | `DELETE /api/v1/users/permissions/{exceptionId}` |
| Lister | `listerPermissionsIndividuelles` | `GET /api/v1/users/{id}/permissions` |

Règles :

- une exception ACCORDER ajoute une permission individuelle ;
- une exception REFUSER retire une permission, **même si son profil la possède** ;
- l'exception est unique par `(utilisateur, permission, type)` (409 sinon) ;
- l'auteur de la décision est tracé (`createdBy` via `CurrentUserService`).

---

## 6. Calcul des droits effectifs

Moteur : `com.cms.security.service.DroitsService.calculerDroits(utilisateurId)`.

```
DROITS EFFECTIFS = (Permissions du Profil) + ACCORDER − REFUSER

Priorité : REFUSER > ACCORDER
```

- Le calcul est effectué **à chaque requête**, directement depuis PostgreSQL
  (aucun cache, aucune permission dans le JWT) → toute modification est
  effective immédiatement, sans reconnexion ;
- le profil système `ADMINISTRATEUR` possède l'ensemble des permissions du
  référentiel (codes toujours relus en base, jamais en dur) ;
- un `REFUSER` individuel reste prioritaire, même pour `ADMINISTRATEUR` ;
- consommation : `@PreAuthorize("hasPermission('MODULE','ACTION')")` →
  `SpringPermissionEvaluator` → `PermissionEvaluatorImpl` → `DroitsService`.

---

## 7. Sécurité des endpoints

Aucun `if (admin)` : chaque endpoint protégé est contrôlé par
`@PreAuthorize("hasPermission('MODULE','ACTION')")` avec les codes
`PROFIL_*` / `PERMISSION_*` / `UTILISATEUR_*`. Un utilisateur sans permission
reçoit un 403 (`ACCES_REFUSE`, format `ErrorResponse`).

---

## 8. Validations

- **Profil** : `nom` obligatoire (`@NotBlank`), nom unique
  (`ProfilRepository.existsByNom` → `NomProfilDejaExistantException` 409) ;
- **Permission** : `codePermission` obligatoire + unique
  (`existsByCodePermission` → `CodePermissionDejaExistantException` 409) et
  conforme `^[A-Z_]+$` ; `module` et `action` obligatoires ; normalisation
  MAJUSCULES côté service.

Toutes les erreurs sont converties par `GlobalExceptionHandler` au format
`ErrorResponse` (voir `docs/gestion-erreurs-api.md`).

---

## 9. Préparation de l'audit

Constantes : `com.cms.common.constants.AuditEvents`. Chaque opération loggue
déjà son événement (`AUDIT|<code>|...`) :

| Événement | Opération |
|-----------|-----------|
| `CREATION_PROFIL` / `MODIFICATION_PROFIL` / `SUPPRESSION_PROFIL` / `ACTIVATION_PROFIL` / `DESACTIVATION_PROFIL` | cycle de vie d'un profil |
| `CREATION_PERMISSION` / `MODIFICATION_PERMISSION` / `SUPPRESSION_PERMISSION` / `ACTIVATION_PERMISSION` / `DESACTIVATION_PERMISSION` | cycle de vie d'une permission |
| `ATTRIBUTION_PERMISSION` / `RETRAIT_PERMISSION` | association profil-permission |
| `ACCORD_PERMISSION` / `REFUS_PERMISSION` / `RETRAIT_PERMISSION_INDIVIDUELLE` | exceptions individuelles utilisateur |

La journalisation complète (enregistrement `HistoriqueAction`) sera finalisée
avec le module Audit (LOOP dédié).

---

## 10. Tests

- `RbacAdministrationTest` (5 cas obligatoires) :
  - Cas 1 — créer un profil → profil créé ;
  - Cas 2 — associer une permission → permission disponible pour le profil ;
  - Cas 3 — utilisateur avec profil → hérite des permissions du profil ;
  - Cas 4 — utilisateur ACCORDER → permission supplémentaire ;
  - Cas 5 — utilisateur REFUSER → permission retirée même si profil autorisé.
- `DroitsServiceTest` (5) : droits effectifs (dont REFUSER prioritaire sur
  ADMINISTRATEUR).
- `ProfilServiceImplTest` (13) + `PermissionServiceImplTest` (10, dont
  `activerPermission`).

Résultat global : **91/91 tests OK, BUILD SUCCESS**.
