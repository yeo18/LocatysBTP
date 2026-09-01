# Controllers REST — Contrats API (LOOP 3.10)

Livrable : LOOP 3.10 (couche REST)
Projet : CMS (Chantier Management System)
Version : 1.0

---

# 1. Objectif

Exposer les fonctionnalités métier aux frontends (React) via une API REST
sécurisée, en respectant les principes de la couche Controller :

- recevoir les **Request DTO** (`@Valid`) ;
- appeler les **Services** ;
- retourner des **Response DTO** ;
- appliquer les **contrôles de permission** (`@PreAuthorize`) ;
- gérer les **codes HTTP** (201 / 200 / 401 / 403 / 404 / 409).

Interdits dans un Controller : accès Repository, logique métier, manipulation
d'Entity, calcul de permissions, gestion de transactions.

## Périmètre de ce LOOP

Les Controllers des modules dont les **Services existent** ont été créés :
**Auth, Utilisateur, Profil, Permission**. Les Controllers Chantier, Equipe,
Tache, Document, Audit seront créés dans un LOOP ultérieur, une fois leurs
Services implémentés (définis dans `architecture-services.md`).

## Base des routes

Convention projet (figée) : préfixe `/api/v1` (`ApiRoutes.BASE_API`), noms de
ressources au pluriel, en minuscules. Divergence notée avec les chemins
simplifiés du cahier des charges (`/api/utilisateurs`) — la convention `/api/v1`
a été retenue pour la cohérence avec `api-convention.md`, `ApiRoutes` et la
documentation existante.

---

# 2. Format des réponses

Toutes les API retournent `ApiResponse<T>` :

```json
{
  "success": true,
  "message": "Operation reussie",
  "data": {},
  "timestamp": "2026-08-06T00:00:00"
}
```

Les listes paginées retournent `PageResponse<T>` (dans `data`) :

```json
{
  "success": true,
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  }
}
```

`PageResponse.from(Page<T>)` (méthode statique ajoutée) convertit une
`org.springframework.data.domain.Page` en `PageResponse`.

## Paramètres de pagination

```
GET /api/v1/users?page=0&size=20&sort=nom&direction=ASC&motCle=awa
```

La requête est liée au DTO `SearchRequest` (`@ModelAttribute`) : `page`
(défaut 0), `size` (défaut 20), `sort`, `direction` (ASC/DESC), `motCle`.

---

# 3. Codes HTTP

| Code | Usage |
|------|-------|
| 200 | Succès (GET, PUT, PATCH, DELETE) |
| 201 | Ressource créée (POST) |
| 400 | Requête invalide / validation échouée |
| 401 | Non authentifié ou token expiré |
| 403 | Authentifié mais permission refusée |
| 404 | Ressource inexistante |
| 409 | Conflit (email/code/nom déjà utilisé, association déjà présente) |
| 500 | Erreur serveur (message générique) |

---

# 4. Authentification

- Route publique : `POST /api/v1/auth/login` → token JWT.
- Route publique : `POST /api/v1/auth/register` → création de compte.
- Toutes les autres routes exigent `Authorization: Bearer <token>`.
- Le token ne contient aucune permission (règle LOOP 3.8) : les droits
  effectifs sont relus en base à chaque requête (RBAC dynamique, LOOP 3.9).

---

# 5. Endpoints — Authentification

| Méthode | Route | Entrée | Sortie | Permission | HTTP |
|---------|-------|--------|--------|------------|------|
| POST | `/api/v1/auth/register` | `CreateUtilisateurRequest` | `ApiResponse<UtilisateurResponse>` | publique | 201 |
| POST | `/api/v1/auth/login` | `LoginRequest` | `ApiResponse<TokenResponse>` | publique | 200 |

`TokenResponse` : `token`, `type` (`Bearer`), `expiresIn` (secondes),
`utilisateur` (`UtilisateurResponse`). Mauvais identifiants ou compte
désactivé → 401 (`UnauthorizedException`).

---

# 6. Endpoints — Utilisateurs (`/api/v1/users`)

| Méthode | Route | Entrée | Sortie | Permission | HTTP |
|---------|-------|--------|--------|------------|------|
| POST | `/api/v1/users` | `CreateUtilisateurRequest` | `ApiResponse<UtilisateurResponse>` | UTILISATEUR_CREER | 201 |
| GET | `/api/v1/users` | `SearchRequest` (query) | `ApiResponse<PageResponse<UtilisateurResumeResponse>>` | UTILISATEUR_LIRE | 200 |
| GET | `/api/v1/users/{id}` | — | `ApiResponse<UtilisateurResponse>` | UTILISATEUR_LIRE | 200 |
| GET | `/api/v1/users/email/{email}` | — | `ApiResponse<UtilisateurResponse>` | UTILISATEUR_LIRE | 200 |
| PUT | `/api/v1/users/{id}` | `UpdateUtilisateurRequest` | `ApiResponse<UtilisateurResponse>` | UTILISATEUR_MODIFIER | 200 |
| DELETE | `/api/v1/users/{id}` | — | `ApiResponse<UtilisateurResponse>` (désactivation logique) | UTILISATEUR_SUPPRIMER | 200 |
| POST | `/api/v1/users/{id}/permissions/{permissionId}/accorder` | — | `ApiResponse<UtilisateurPermissionResponse>` | UTILISATEUR_MODIFIER | 200 |
| POST | `/api/v1/users/{id}/permissions/{permissionId}/refuser` | — | `ApiResponse<UtilisateurPermissionResponse>` | UTILISATEUR_MODIFIER | 200 |
| DELETE | `/api/v1/users/permissions/{exceptionId}` | — | `ApiResponse<Void>` | UTILISATEUR_MODIFIER | 200 |
| GET | `/api/v1/users/{id}/permissions` | — | `ApiResponse<List<UtilisateurPermissionResponse>>` | UTILISATEUR_LIRE | 200 |

---

# 7. Endpoints — Profils (`/api/v1/profils`)

| Méthode | Route | Entrée | Sortie | Permission | HTTP |
|---------|-------|--------|--------|------------|------|
| POST | `/api/v1/profils` | `CreateProfilRequest` | `ApiResponse<ProfilResponse>` | PROFIL_CREER | 201 |
| GET | `/api/v1/profils` | `SearchRequest` (query) | `ApiResponse<PageResponse<ProfilResumeResponse>>` | PROFIL_LIRE | 200 |
| GET | `/api/v1/profils/actifs` | — | `ApiResponse<List<ProfilResumeResponse>>` | PROFIL_LIRE | 200 |
| GET | `/api/v1/profils/{id}` | — | `ApiResponse<ProfilResponse>` | PROFIL_LIRE | 200 |
| PUT | `/api/v1/profils/{id}` | `UpdateProfilRequest` | `ApiResponse<ProfilResponse>` | PROFIL_MODIFIER | 200 |
| DELETE | `/api/v1/profils/{id}` | — | `ApiResponse<Void>` | PROFIL_SUPPRIMER | 200 |
| PATCH | `/api/v1/profils/{id}/desactiver` | — | `ApiResponse<ProfilResponse>` | PROFIL_MODIFIER | 200 |
| PATCH | `/api/v1/profils/{id}/activer` | — | `ApiResponse<ProfilResponse>` | PROFIL_MODIFIER | 200 |
| POST | `/api/v1/profils/{id}/permissions/{permissionId}` | — | `ApiResponse<ProfilPermissionResponse>` | PROFIL_MODIFIER | 200 |
| DELETE | `/api/v1/profils/{id}/permissions/{permissionId}` | — | `ApiResponse<Void>` | PROFIL_MODIFIER | 200 |
| GET | `/api/v1/profils/{id}/permissions` | — | `ApiResponse<List<ProfilPermissionResponse>>` | PROFIL_LIRE | 200 |

---

# 8. Endpoints — Permissions (`/api/v1/permissions`)

| Méthode | Route | Entrée | Sortie | Permission | HTTP |
|---------|-------|--------|--------|------------|------|
| POST | `/api/v1/permissions` | `CreatePermissionRequest` | `ApiResponse<PermissionResponse>` | PERMISSION_CREER | 201 |
| GET | `/api/v1/permissions` | `SearchRequest` (query) | `ApiResponse<PageResponse<PermissionResumeResponse>>` | PERMISSION_LIRE | 200 |
| GET | `/api/v1/permissions/{id}` | — | `ApiResponse<PermissionResponse>` | PERMISSION_LIRE | 200 |
| GET | `/api/v1/permissions/code/{codePermission}` | — | `ApiResponse<PermissionResponse>` | PERMISSION_LIRE | 200 |
| GET | `/api/v1/permissions/modules/{module}` | — | `ApiResponse<List<PermissionResumeResponse>>` | PERMISSION_LIRE | 200 |
| PUT | `/api/v1/permissions/{id}` | `UpdatePermissionRequest` | `ApiResponse<PermissionResponse>` | PERMISSION_MODIFIER | 200 |
| DELETE | `/api/v1/permissions/{id}` | — | `ApiResponse<Void>` | PERMISSION_SUPPRIMER | 200 |
| PATCH | `/api/v1/permissions/{id}/desactiver` | — | `ApiResponse<PermissionResponse>` | PERMISSION_MODIFIER | 200 |

---

# 9. Sécurité — RBAC dans les Controllers

Chaque endpoint protégé porte :

```java
@PreAuthorize("hasPermission('MODULE','ACTION')")
```

L'expression est résolue par la chaîne RBAC (LOOP 3.9) :
`@PreAuthorize` → `SpringPermissionEvaluator` → `PermissionEvaluatorImpl`
(code `MODULE_ACTION`) → `DroitsService` (droits effectifs en base).

Le Controller répond uniquement à : « cette action est-elle autorisée ? ».
Le contrôle de **périmètre des données** (Niveau 2, « cette donnée
appartient-elle à l'utilisateur ? ») sera fait dans les Services via
`CurrentUserService` (jamais par un paramètre frontend).

---

# 10. Structure des packages

```
com.cms
├── auth
│   ├── controller/AuthController.java
│   ├── dto/LoginRequest.java, TokenResponse.java
│   └── service/AuthenticationService(Impl).java
├── utilisateur/controller/UtilisateurController.java
├── profil/controller/ProfilController.java
└── permission/controller/PermissionController.java
```

Modifications annexes :

- `config/SecurityConfig.java` : `/api/v1/auth/**` ajouté en route publique.
- `common/constants/ApiRoutes.java` : `PROFILS`, `PERMISSIONS` ajoutés.
- `common/response/PageResponse.java` : méthode statique `from(Page<T>)`.
- `exception/handler/GlobalExceptionHandler.java` : handler
  `AccessDeniedException` → 403 (évite qu'un refus RBAC devienne un 500).

---

# 11. Documentation API (Swagger/OpenAPI)

- Config existante (LOOP 1.5) : schéma de sécurité `bearerAuth` (HTTP Bearer
  JWT), serveur dev `http://localhost:8091`.
- Les Controllers sont annotés : `@Tag` (groupe), `@Operation` (résumé +
  description), `@ApiResponses` (codes et descriptions).
- Interface interactive : `http://localhost:8091/swagger-ui.html`.
- NB : l'annotation Swagger `@ApiResponse` est utilisée en forme qualifiée
  (`@io.swagger.v3.oas.annotations.responses.ApiResponse`) pour éviter le
  conflit de nom avec la classe métier `ApiResponse`.

---

# 12. Tests effectués

| Test | Cas couverts | Résultat |
|------|--------------|----------|
| `AuthenticationServiceImplTest` (3) | login correct → token ; mauvais mot de passe → 401 ; inscription délègue à UtilisateurService | ✅ |
| `AuthControllerTest` (3) | login 200 + token ; mauvais identifiants → 401 ; register → 201 | ✅ |
| `UtilisateurControllerTest` (4) | création autorisée → 201 ; création refusée sans permission → 403 ; modification autorisée → 200 ; liste sans token → 401 | ✅ |
| `ProfilControllerTest` (2) | création autorisée → 201 ; création refusée sans permission → 403 | ✅ |
| `PermissionControllerTest` (2) | création autorisée → 201 ; création refusée sans permission → 403 | ✅ |
| Suite existante | 64 tests LOOP 3.8/3.9 | ✅ |

Total : **78/78 tests OK**, `mvn test` BUILD SUCCESS.

---

# 13. Ce qui reste à faire

- **LOOP 3.11** : gestion globale des exceptions, validation API
  (messages d'erreur détaillés `errors`), format d'erreur professionnel.
- Controllers des modules Chantier, Equipe, Tache, Document, Audit — une fois
  leurs Services implémentés.
- MPD + migrations Flyway (tables `profil_permission`,
  `utilisateur_permission`, seed des permissions et profils système).
- Périmètre Niveau 2 dans les Services (`CurrentUserService`).

---

# 14. Vérifications de conformité (LOOP 3.10)

- ✅ Controllers → Services uniquement (aucun accès Repository) ;
- ✅ aucune logique métier dans les Controllers ;
- ✅ aucune Entity exposée (DTO uniquement) ;
- ✅ permissions appliquées via `@PreAuthorize` (RBAC dynamique) ;
- ✅ codes HTTP gérés (201 / 200 / 401 / 403 / 404 / 409) ;
- ✅ validation `@Valid` sur les Request DTO ;
- ✅ pagination `PageResponse<T>` ;
- ✅ Swagger/OpenAPI documenté ;
- ✅ aucune migration Flyway créée ;
- ✅ routes conformes à la convention `/api/v1`.
