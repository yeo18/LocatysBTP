# Architecture — CMS (Chantier Management System)

Version : 0.1
Statut : Documentation (aucun code implémenté)

---

# 1. Vision générale

## Objectif

CMS est une application professionnelle de gestion de chantier pour les entreprises de BTP. Elle permet de gérer les chantiers, les équipes, les employés, les tâches, les validations, les historiques et les droits d'accès.

## Utilisateurs concernés

* Administrateur (gestion globale, permissions, utilisateurs)
* Chef d'équipe (gestion des chantiers et des équipes)
* Ouvrier (exécution et suivi de ses tâches)

## Fonctionnalités principales

* Gestion des chantiers (création, planification, localisation, suivi)
* Gestion des équipes et des affectations
* Gestion des tâches (titres, descriptions, priorités, statuts, validation)
* Historique des actions (audit)
* Système d'authentification sécurisé (JWT)
* Contrôle d'accès dynamique (RBAC)

---

# 2. Architecture globale

```
┌───────────────────────┐
│   Frontend (React)    │
│  - UI / composants    │
│  - état client        │
│  - appels API (JWT)   │
└───────────┬───────────┘
            │  HTTPS / JSON
            │
┌───────────▼───────────┐
│   Backend (Spring)    │
│  - REST API (/api/v1) │
│  - Spring Security    │
│  - RBAC dynamique     │
│  - règles métier      │
└───────────┬───────────┘
            │  JDBC / JPA
            │
┌───────────▼───────────┐
│   PostgreSQL          │
│  - données métier     │
│  - RBAC (tables)      │
│  - historique         │
└───────────────────────┘
```

Flux : le frontend n'accède jamais directement à la base. Toutes les données passent par l'API backend, qui applique l'authentification, l'autorisation et les règles métier.

---

# 3. Principes

## Séparation frontend / backend

* Deux applications indépendantes et déployables séparément.
* Le frontend consomme uniquement l'API REST.
* Aucune logique métier ni sécurité dans le frontend.

## Sécurité côté serveur

* Toute vérification se fait dans le backend.
* Le frontend ne doit jamais être considéré comme une sécurité.
* Principe : « Un utilisateur voit uniquement les ressources auxquelles il a droit. »

## RBAC dynamique

* Les droits sont stockés en base de données (profils + permissions).
* Modifiables sans redéploiement de code.
* Un utilisateur hérite des permissions de son profil, avec possibilité de permissions directes supplémentaires.

## Modularité

* Organisation par module métier (utilisateur, chantier, équipe, tâche, rbac, historique).
* Chaque module est autonome (entity, repository, service, controller, dto, mapper).
* Favorise la maintenabilité et l'évolutivité.

---

# 4. Décisions d'architecture

| Sujet | Choix |
|-------|-------|
| Frontend | React |
| Backend | Spring Boot (Java) |
| Base de données | PostgreSQL |
| Authentification | Spring Security + JWT |
| Autorisation | RBAC dynamique (BDD) |
| Migrations | Flyway (prévu) |
| Documentation API | Swagger (prévu) |
| Tests | JUnit + TestContainers (prévu) |

## Infrastructure commune

* **Réponses API** : toutes les réponses utilisent `ApiResponse<T>` (`{ success, message, data, timestamp }`).
* **Erreurs** : `CmsException` (base) + `GlobalExceptionHandler` (`@RestControllerAdvice`).
* **Constantes** : centralisées dans `com.cms.common.constants` (messages, routes, rôles, profils, dates, techniques).
* **Enums** : techniques dans `com.cms.common.enums` ; les enums métier seront définis par module.
* **Configuration** : centralisée dans `ApplicationProperties` (prefix `app`), secrets via variables d'environnement.
* **Mapping** : MapStruct (Spring Component Model) pour Entity ↔ DTO.
* **Validation** : Bean Validation sur les DTO, jamais dans les controllers.

---

FIN DU DOCUMENT
