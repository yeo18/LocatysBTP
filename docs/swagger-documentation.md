# Documentation API — CMS (Swagger / OpenAPI)

Version : 0.1
Statut : Active

---

# 1. Accès à la documentation

| URL | Description |
|-----|-------------|
| http://localhost:8091/swagger-ui.html | Interface Swagger UI (redirection) |
| http://localhost:8091/swagger-ui/index.html | Interface Swagger UI |
| http://localhost:8091/v3/api-docs | Schéma OpenAPI JSON brut |

Port de développement : **8090** (profil `dev`).

---

# 2. Utilisation de Swagger

1. Démarrer l'application (voir `start.ps1`).
2. Ouvrir http://localhost:8091/swagger-ui.html
3. Parcourir les endpoints par module (tags).
4. Pour les endpoints protégés : cliquer **Authorize**, coller le token JWT (`Bearer <token>`).

---

# 3. Où seront documentées les futures API

Toutes les futures API REST métier apparaîtront **automatiquement** dans Swagger :
- Chaque `@RestController` sera listé avec ses méthodes.
- Chaque DTO sera affiché avec ses champs.
- Les codes HTTP et réponses seront documentés via les annotations Springdoc/OpenAPI.

Conventions appliquées :
- Base URL : `/api/v1`
- Format de réponse commun : `{ success, message, data }`
- Authentification : `Authorization: Bearer <token>` (schéma `bearerAuth` préparé)

---

# 4. Configuration technique

| Paramètre | Valeur |
|-----------|--------|
| Dépendance | springdoc-openapi-starter-webmvc-ui 2.8.5 |
| Package de config | `com.cms.config.openapi` (OpenApiConfig) |
| Tri des tags | alphabétique |
| Tri des opérations | par méthode HTTP |
| Schéma de sécurité | HTTP Bearer JWT (préparé, non implémenté) |
| Serveur documenté | http://localhost:8091 (dev) |

---

# 5. Informations de l'API (OpenApiConfig)

- **Titre** : CMS - Chantier Management System API
- **Description** : gestion de chantiers, équipes, employés, tâches, validations, historiques, RBAC
- **Version** : 0.1.0
- **Contact** : Équipe CMS
- **Licence** : Propriétaire

---

FIN DU DOCUMENT
