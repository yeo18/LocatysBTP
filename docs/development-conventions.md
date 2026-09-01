# Conventions de développement — CMS

Version : 0.1
Statut : Guide obligatoire pour toutes les modifications futures

---

# 1. Convention générale du projet

## Principes

* **Clean Code** : code lisible, explicite, sans magie.
* **Séparation des responsabilités** : chaque classe a un seul rôle.
* **Maintenabilité prioritaire** : le code est écrit pour être relu et modifié.
* **Aucune duplication inutile** : factoriser via des composants communs.

## Règle d'architecture par fonctionnalité

Toute fonctionnalité respecte la chaîne suivante :

```
Controller
    |
Service
    |
Repository
    |
Database
```

* Controller → reçoit la requête HTTP, délègue, répond.
* Service → logique métier, transactions, règles de sécurité.
* Repository → accès aux données.
* Database → stockage PostgreSQL.

Aucun saut de couche : un controller n'appelle jamais directement un repository.

---

# 2. Convention des packages Spring Boot

Structure obligatoire :

```
com.cms

├── config
│
├── security
│
├── exception
│
├── common
│
├── utilisateur
│   ├── entity
│   ├── repository
│   ├── service
│   ├── controller
│   ├── dto
│   └── mapper
│
├── chantier
├── equipe
├── tache
├── rbac
└── historique
```

| Dossier | Rôle |
|---------|------|
| config | Beans Spring, CORS, Swagger, configuration globale |
| security | JWT, filtres, UserDetails, configuration Spring Security |
| exception | Exceptions métier + handler global (@ControllerAdvice) |
| common | Réponse standard, pagination, constantes, utilitaires transverses |
| utilisateur | Module métier utilisateur |
| chantier | Module métier chantier |
| equipe | Module métier équipe |
| tache | Module métier tâche |
| rbac | Module RBAC (profil, permission, droits dynamiques) |
| historique | Module audit (journal des actions) |

Chaque module métier contient obligatoirement les sous-dossiers : `entity`, `repository`, `service`, `controller`, `dto`, `mapper`.

---

# 3. Convention des classes Java

## Entity

Format : nom singulier, PascalCase.

```
Utilisateur.java
Chantier.java
Tache.java
```

## Repository

Toujours terminer par `Repository`.

```
UtilisateurRepository
ChantierRepository
```

## Service

Interface : nom métier + `Service`.

```
UtilisateurService
```

Implémentation : interface + `Impl`.

```
UtilisateurServiceImpl
```

## Controller

Format : nom métier + `Controller`.

```
UtilisateurController
ChantierController
```

## DTO

| Usage | Format |
|-------|--------|
| Création | `UtilisateurCreateDTO` |
| Modification | `UtilisateurUpdateDTO` |
| Réponse | `UtilisateurResponseDTO` |

Règle : les DTO sont indépendants des entities ; jamais d'entity exposée directement.

---

# 13. Stratégie DTO, Mapper et Validation

## Stratégie DTO

Chaque Entity possède obligatoirement :
* un **Request DTO** (entrée API : CreateDTO / UpdateDTO) ;
* un **Response DTO** (sortie API).

Un Controller ne retourne jamais directement une Entity : toujours un DTO.

## Stratégie Mapper (MapStruct)

* Tous les mappers utilisent **MapStruct** avec `componentModel = spring`.
* Un Mapper par module : `ChantierMapper` (interface dans `dto`/`mapper`).
* Mapping automatique Entity ↔ DTO via MapStruct.
* Les implémentations sont générées à la compilation (aucun code manuel).

## Stratégie Validation (Bean Validation)

* Toutes les validations se font **au niveau des DTO** avec les annotations Jakarta Validation :
  `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Positive`, `@Past`, `@Future`.
* **Aucune logique de validation dans les Controllers** (utilisation de `@Valid` sur les DTO).
* Les messages d'erreur sont **centralisés** (messages de validation uniformes).

## Outils de développement

| Outil | Usage |
|-------|-------|
| Lombok | Réduction du boilerplate (getters/setters, constructeurs) |
| MapStruct | Mapping Entity ↔ DTO automatique |
| Bean Validation | Validation uniforme des entrées API |

---

# 14. Infrastructure commune

## Constantes

- Package `com.cms.common.constants` : classes finales non instanciables (constructeur privé).
- Classes par domaine : `Messages`, `ApiRoutes`, `SystemRoles`, `SpringProfiles`, `DateFormats`, `TechnicalValues`.
- **Aucune constante métier** dans ce package (elles seront dans les modules concernés).

## Énumérations

- Package `com.cms.common.enums` : uniquement les enums techniques (`EnvironmentType`, `ApiVersion`, `ApplicationMode`).
- Les enums métier (priorité, statut de tâche...) seront définies dans les modules métier.

## Réponses API

- Toute réponse REST utilise `ApiResponse<T>` (`com.cms.common.response`).
- Format : `{ success, message, data, timestamp }`.
- Méthodes : `success(data)`, `success(message, data)`, `error(message)`.

## Gestion des erreurs

- Base : `CmsException` (classe abstraite, contient le code HTTP).
- Exceptions techniques : `BadRequestException`, `ResourceNotFoundException`, `UnauthorizedException`, `ForbiddenException`.
- Handler centralisé : `GlobalExceptionHandler` (`@RestControllerAdvice`) → convertit tout en `ApiResponse`.
- Les exceptions métier hériteront de `CmsException` dans les phases suivantes.

## Configuration centralisée

- `com.cms.config.properties.ApplicationProperties` (prefix `app`).
- Groupes par domaine : `app.application`, `app.security` (jwt), `app.api`.
- Les secrets viennent toujours de variables d'environnement.

## Journalisation

Niveaux : DEBUG < INFO < WARN < ERROR.

| Profil | root | com.cms | SQL |
|--------|------|---------|-----|
| dev | INFO | DEBUG | DEBUG |
| test | WARN | DEBUG | - |
| prod | WARN | INFO | - |

---

# 4. Convention des méthodes

Noms standards à respecter dans les services :

| Action | Nom |
|--------|-----|
| Création | `create()` |
| Recherche par id | `findById()` |
| Liste | `findAll()` |
| Modification | `update()` |
| Suppression | `delete()` |
| Authentification | `register()` / `login()` |
| Affectation | `assign()` |
| Validation | `validate()` |

---

# 5. Convention REST API

Base URL :

```
/api/v1
```

Ressources au pluriel, minuscules, sans accents.

## Utilisateur

```
GET    /api/v1/users
GET    /api/v1/users/{id}
POST   /api/v1/users
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}
```

## Chantier

```
GET    /api/v1/chantiers
GET    /api/v1/chantiers/{id}
POST   /api/v1/chantiers
PUT    /api/v1/chantiers/{id}
DELETE /api/v1/chantiers/{id}
```

## Tâche

```
GET    /api/v1/taches
GET    /api/v1/taches/{id}
POST   /api/v1/taches
PUT    /api/v1/taches/{id}
DELETE /api/v1/taches/{id}
```

## Authentification

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
```

---

# 6. Convention des réponses API

Toutes les réponses utilisent un format commun.

## Succès

```json
{
  "success": true,
  "message": "Opération réussie",
  "data": {}
}
```

## Erreur

```json
{
  "success": false,
  "message": "Erreur",
  "timestamp": ""
}
```

## Erreur de validation

```json
{
  "success": false,
  "message": "Erreur de validation",
  "timestamp": "",
  "errors": {
    "nom": "Le nom est obligatoire"
  }
}
```

La classe `ApiResponse<T>` dans `com.cms.common` est utilisée pour toutes les réponses.

---

# 7. Gestion des exceptions

Stratégie globale centralisée.

## Classes prévues

* `GlobalExceptionHandler` — intercepte toutes les exceptions (`@ControllerAdvice`)
* `ResourceNotFoundException` — ressource absente (404)
* `BadRequestException` — requête invalide (400)
* `UnauthorizedException` — non authentifié / token invalide (401)
* `ForbiddenException` — permission refusée (403)

## Règles

* Toute exception métier → réponse au format commun (section 6).
* Jamais de stack trace exposée au client.
* Les messages d'erreur sont cohérents et explicités.

---

# 8. Convention sécurité

* Authentification : **JWT** (Bearer token).
* Mot de passe : **BCrypt** (jamais en clair).
* Autorisation : **RBAC dynamique** (droits en base, vérifiés côté serveur).
* **Règle : aucun contrôle d'accès uniquement côté frontend.** La sécurité réelle se fait exclusivement dans le backend.

---

# 9. Convention base de données

* Tables : `snake_case`, au singulier.

```
utilisateur
profil_permission
historique_action
```

* Colonnes : `snake_case`.

```
date_creation
id_utilisateur
```

* Clés étrangères : `id_<table_source>`.

```
id_utilisateur
id_chantier
```

* Identifiants : `bigint` auto-générés (voir database-design.md).

---

# 10. Convention Git

## Branches

```
main
develop
feature/nom
bugfix/nom
hotfix/nom
```

## Messages de commit

| Type | Usage |
|------|-------|
| `feat:` | Ajout de fonctionnalité |
| `fix:` | Correction de bug |
| `docs:` | Documentation |
| `refactor:` | Refactorisation |
| `test:` | Tests |

Exemple :

```
feat: add JWT authentication
```

---

# 11. Convention de test

| Type de test | Outil |
|--------------|-------|
| Tests unitaires | JUnit |
| Tests d'intégration | Spring Boot Test |
| Tests base de données | TestContainers |

Règle : **chaque nouvelle fonctionnalité doit avoir des tests.**

---

# 12. Convention Loop Engineering

Chaque intervention respecte le cycle suivant.

## Avant

* analyser l'existant ;
* expliquer les fichiers impactés ;
* identifier le LOOP concerné.

## Pendant

* modifier ;
* tester.

## Après

Produire un rapport de fin de LOOP contenant :
* fichiers modifiés ;
* opérations réalisées ;
* tests effectués ;
* problèmes rencontrés ;
* ce qui reste à faire.

Le MASTER_PLAN.md est mis à jour à la fin de chaque LOOP.

---

FIN DU DOCUMENT
