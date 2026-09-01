# Gestion des erreurs API — Format professionnel uniforme

Version : 1.0
LOOP : 3.11
Date : 06/08/2026

---

## 1. Objectif

Uniformiser la gestion des erreurs sur toute l'API REST : chaque erreur est
convertie en une réponse JSON unique (`ErrorResponse`), avec un code HTTP
pertinent, un code machine lisible, un message sûr (jamais d'exception Spring
brute), et un détail champ par champ pour les erreurs de validation.

Le frontend peut ainsi se brancher sur un contrat d'erreur stable.

---

## 2. Format d'erreur — `ErrorResponse`

Classe : `com.cms.common.response.ErrorResponse`

```json
{
  "success": false,
  "message": "L'email est deja utilise : dupont@example.com",
  "code": "EMAIL_DEJA_UTILISE",
  "timestamp": "2026-08-06T02:09:51Z",
  "errors": {
    "nom": "ne doit pas etre vide",
    "email": "doit etre une adresse electronique valide"
  }
}
```

| Champ | Type | Description |
|-------|------|-------------|
| `success` | boolean | toujours `false` en erreur |
| `message` | String | message lisible, sans information sensible |
| `code` | String | code machine (ex : `VALIDATION_ERROR`, `ACCES_REFUSE`) |
| `timestamp` | LocalDateTime | horodatage de l'erreur |
| `errors` | Map<String,String> | détail `champ -> message`, présent uniquement en cas d'erreur de validation (omis sinon) |

Fabriques :

- `ErrorResponse.of(code, message)` — erreur simple ;
- `ErrorResponse.of(code, message, errors)` — erreur de validation.

`@JsonInclude(NON_NULL)` : le champ `errors` est omis quand il n'y a pas de
détail par champ.

---

## 3. Codes HTTP et codes machine

| Cas | HTTP | `code` retourné |
|-----|------|-----------------|
| Erreur métier (`CmsException`) | statut porté par l'exception | nom de l'exception en SCREAMING_SNAKE (sans suffixe `Exception`) |
| Validation DTO (`@RequestBody`) | 400 | `VALIDATION_ERROR` |
| Validation paramètres/path | 400 | `VALIDATION_ERROR` |
| Corps de requête illisible (JSON malformé) | 400 | `REQUETE_INVALIDE` |
| Accès refusé (`@PreAuthorize`) | 403 | `ACCES_REFUSE` |
| Échec d'authentification | 401 | `AUTHENTIFICATION_REQUISE` |
| Erreur inattendue | 500 | `ERREUR_INTERNE` |

Dérivation automatique du code métier depuis le nom de l'exception :

- `EmailDejaUtiliseException` → `EMAIL_DEJA_UTILISE` ;
- `UtilisateurNonTrouveException` → `UTILISATEUR_NON_TROUVE` ;
- `ProfilSystemeProtegeException` → `PROFIL_SYSTEME_PROTEGE` ;
- etc.

---

## 4. Hiérarchie des exceptions

```
RuntimeException
└── CmsException (abstraite — porte le statut HTTP)
    ├── BusinessException            (400 — règle métier générique)
    ├── DuplicateResourceException   (409 — doublon de ressource)
    ├── BadRequestException          (400)
    ├── ResourceNotFoundException    (404)
    ├── UnauthorizedException        (401)
    ├── ForbiddenException           (403)
    ├── EmailDejaUtiliseException    (409)
    ├── UtilisateurNonTrouveException(404)
    ├── ProfilIntrouvableException   (404)
    ├── PermissionIntrouvableException (404)
    ├── CodePermissionDejaExistantException (409)
    ├── NomProfilDejaExistantException (409)
    ├── ProfilSystemeProtegeException (409)
    └── PermissionDejaAttribueeException (409)
```

Nouvelles exceptions du LOOP 3.11 (package `com.cms.exception.custom`) :

- `BusinessException(message)` / `BusinessException(message, status)` : règle
  métier non spécifique (400 par défaut) ;
- `DuplicateResourceException(message)` + fabrique
  `pourConflit(champ, valeur)` : doublon de ressource (409).

Utilisation des nouvelles exceptions : dès qu'une règle métier ou un doublon
ne correspond pas à une exception dédiée existante, préférer ces deux classes
à la création d'une nouvelle exception.

---

## 5. `GlobalExceptionHandler`

Classe : `com.cms.exception.handler.GlobalExceptionHandler` (`@RestControllerAdvice`)

### 5.1 `CmsException` → statut HTTP de l'exception

- HTTP ≥ 500 : log `ERROR` ;
- HTTP < 500 : log `WARN`.

### 5.2 Validation DTO → 400 + détails

`MethodArgumentNotValidException` et `ConstraintViolationException` sont
converties en map `champ -> message` (les doublons de champ sont dédupliqués,
le premier message est conservé).

### 5.3 Sécurité

- `AccessDeniedException` → 403 `ACCES_REFUSE` (un refus RBAC ne doit jamais
  devenir un 500) ;
- `AuthenticationException` → 401 `AUTHENTIFICATION_REQUISE`.

### 5.4 Erreur inattendue → 500 masqué

`Exception` → 500 `ERREUR_INTERNE` avec message générique
(`Messages.ERREUR_GENERIQUE`). Le détail technique est loggé en `ERROR` (avec
la stack trace) mais **jamais** renvoyé au frontend.

### 5.5 Journalisation

- Ne jamais logger un mot de passe ni un token ;
- Niveaux : `WARN` pour les erreurs métier/validation/sécurité, `ERROR` pour
  les erreurs serveur inattendues.

---

## 6. Handlers de sécurité (filtres)

Les réponses écrites hors Spring MVC (chaîne de filtres) utilisent le même
format `ErrorResponse` :

- `security/handler/UnauthorizedHandler` (401) :
  `ErrorResponse.of("AUTHENTIFICATION_REQUISE", Messages.AUTHENTIFICATION_REQUISE)` ;
- `security/handler/AccessDeniedHandlerImpl` (403) :
  `ErrorResponse.of("ACCES_REFUSE", Messages.ACCES_REFUSE)`.

Les deux sérialisent via `ObjectMapper` (codage UTF-8, `Content-Type:
application/json`).

---

## 7. Messages centralisés

`com.cms.common.constants.Messages` :

- `AUTHENTIFICATION_REQUISE` — « Authentification requise » ;
- `ACCES_REFUSE` — « Acces refuse » ;
- `ERREUR_GENERIQUE` — « Une erreur est survenue ».

---

## 8. Exemples de réponses

### 409 — email déjà utilisé

```json
{
  "success": false,
  "message": "L'email est deja utilise : nouveau@example.com",
  "code": "EMAIL_DEJA_UTILISE",
  "timestamp": "2026-08-06T02:09:51Z"
}
```

### 400 — validation de DTO

```json
{
  "success": false,
  "message": "Erreur de validation",
  "code": "VALIDATION_ERROR",
  "timestamp": "2026-08-06T02:09:51Z",
  "errors": {
    "nom": "ne doit pas etre vide",
    "email": "doit etre une adresse electronique valide"
  }
}
```

### 403 — accès refusé

```json
{
  "success": false,
  "message": "Acces refuse",
  "code": "ACCES_REFUSE",
  "timestamp": "2026-08-06T02:09:51Z"
}
```

### 500 — erreur inattendue (détail technique jamais exposé)

```json
{
  "success": false,
  "message": "Une erreur est survenue",
  "code": "ERREUR_INTERNE",
  "timestamp": "2026-08-06T02:09:51Z"
}
```

---

## 9. Tests

Classe : `com.cms.exception.handler.GlobalExceptionHandlerTest` (6 tests,
intégration MockMvc) :

| Cas | Résultat vérifié |
|-----|------------------|
| email déjà utilisé | 409, `code=EMAIL_DEJA_UTILISE`, message explicite |
| utilisateur inexistant | 404, `code=UTILISATEUR_NON_TROUVE` |
| validation DTO invalide | 400, `code=VALIDATION_ERROR`, `errors.nom` + `errors.email` |
| accès refusé sans permission | 403, `code=ACCES_REFUSE` |
| erreur inattendue | 500, `code=ERREUR_INTERNE`, message générique |
| identifiants incorrects | 401, `code=UNAUTHORIZED` |

Résultat global : **84/84 tests OK, BUILD SUCCESS**.

---

## 10. Notes pour les prochains LOOP

- Les exceptions existantes ne doivent pas être dupliquées : réutiliser les
  classes de `com.cms.exception.custom` ;
- Toute nouvelle exception métier doit hériter de `CmsException` pour
  bénéficier automatiquement du handler ;
- Le format `ErrorResponse` est le contrat frontend : ne pas le modifier sans
  décision ;
- Jamais d'exception Spring brute ni de stack trace dans une réponse.
