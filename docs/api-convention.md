# Conventions API — CMS

Version : 0.1
Statut : Documentation

---

# 1. Format des URL

* Préfixe : `/api/v1`
* Nom des ressources : pluriel, en minuscules, sans accents.

Exemples :

```
/api/v1/auth/login
/api/v1/auth/refresh
/api/v1/users
/api/v1/users/{id}
/api/v1/chantiers
/api/v1/chantiers/{id}
/api/v1/taches
/api/v1/equipes
/api/v1/profils
/api/v1/permissions
/api/v1/historique
```

## Ressources imbriquées

```
GET  /api/v1/chantiers/{id}/taches       → tâches d'un chantier
GET  /api/v1/chantiers/{id}/equipes      → équipes d'un chantier
```

---

# 2. Format de réponse

## Succès

```json
{
  "success": true,
  "data": {}
}
```

## Erreur

```json
{
  "success": false,
  "message": ""
}
```

## Erreur avec détails (validation)

```json
{
  "success": false,
  "message": "Erreur de validation",
  "errors": {
    "nom": "Le nom est obligatoire",
    "email": "Format email invalide"
  }
}
```

---

# 3. Codes HTTP

| Code | Usage |
|------|-------|
| 200 OK | Succès (GET, PUT, PATCH, DELETE) |
| 201 Created | Ressource créée (POST) |
| 204 No Content | Suppression réussie (si applicable) |
| 400 Bad Request | Requête invalide / validation échouée |
| 401 Unauthorized | Non authentifié ou token expiré |
| 403 Forbidden | Authentifié mais permission refusée |
| 404 Not Found | Ressource inexistante |
| 409 Conflict | Conflit (ex. email déjà utilisé) |
| 422 Unprocessable Entity | Règle métier non respectée (si retenu) |
| 500 Internal Server Error | Erreur serveur |

---

# 4. Pagination

* Paramètres de requête : `page` (0-indexée) et `size`.

```
GET /api/v1/chantiers?page=0&size=20
```

* Réponse paginée :

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

---

# 5. Tri et filtres

* Tri : `sort=champ,asc|desc`

```
GET /api/v1/taches?sort=dateFin,desc
```

* Filtres : en paramètres de requête nommés selon les champs.

```
GET /api/v1/taches?statut=EN_COURS&chantierId=3
```

---

# 6. Validation

* La validation se fait avec Jakarta Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`, ...).
* Chaque champ invalide renvoie un message précis dans `errors`.
* Le backend valide toujours, même si le frontend valide déjà.

---

# 7. Gestion des erreurs

* `@ControllerAdvice` centralise le traitement des exceptions.
* Les exceptions métier renvoient un code et un message cohérents.
* Jamais de stack trace exposée au client.
* Les erreurs non gérées renvoient un `500` générique sans détail interne.

## Exemples d'exceptions métier

| Exception | Code | Message exemple |
|-----------|------|-----------------|
| ResourceNotFoundException | 404 | Chantier introuvable avec id 5 |
| DuplicateException | 409 | Un utilisateur avec cet email existe déjà |
| AccessDeniedException | 403 | Permission CHANTIER_VIEW requise |
| InvalidCredentialsException | 401 | Email ou mot de passe incorrect |

---

# 8. Authentification des requêtes

* En-tête : `Authorization: Bearer <token>`
* Les endpoints publics sont : `/api/v1/auth/login`, `/api/v1/auth/refresh`.
* Tous les autres endpoints exigent un JWT valide.
* Les permissions sont vérifiées sur les endpoints protégés.

---

# 9. Documentation API

* Swagger/OpenAPI génère la documentation interactive.
* Accessible en dev sur `/swagger-ui.html`.

---

FIN DU DOCUMENT
