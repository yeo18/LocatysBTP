# Sécurité — Authentification JWT et Spring Security (LOOP 3.8)

Livrable : LOOP 3.8 (socle d'authentification)
Projet : CMS (Chantier Management System)
Version : 1.0

---

# 1. Objectif

Mettre en place le socle d'authentification de l'application :

- vérification du couple **email / mot de passe** (BCrypt) ;
- émission et validation de **tokens JWT** ;
- filtre qui authentifie chaque requête protégée ;
- préparation du **RBAC dynamique** (contrat `PermissionEvaluator`,
  annotation `@RequirePermission`) — implémentation complète au LOOP 3.9.

Aucun Controller, aucun endpoint REST, aucune migration Flyway et aucun accès
au périmètre chantier n'ont été créés dans ce LOOP.

---

# 2. Règles de sécurité figées (LOOP 3.8)

| Règle | Conséquence |
|-------|-------------|
| Le token ne contient que `sub`, `userId`, `iat`, `exp` | aucune permission, aucun profil, aucune liste d'accès dans le JWT |
| Les droits sont toujours relus en base | RBAC dynamique : modification effective immédiatement |
| Aucune permission codée en dur | pas de rôles fixes dans `SecurityConfig` |
| Aucun Controller métier | l'authentification est testée via les composants internes |
| Compte désactivé = refus systématique | `actif = false` → `DisabledException` (401) |

---

# 3. Architecture des composants

```
com.cms
├── security
│   ├── annotation/
│   │   └── RequirePermission.java          (préparation RBAC Niveau 1)
│   ├── config/                             (réservé — config centralisée)
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java    (valide le Bearer token, peuple le contexte)
│   ├── handler/
│   │   ├── UnauthorizedHandler.java        (réponse 401 uniforme)
│   │   └── AccessDeniedHandlerImpl.java    (réponse 403 uniforme)
│   ├── jwt/
│   │   └── JwtService.java                 (génération, validation, lecture)
│   ├── permission/
│   │   └── PermissionEvaluator.java        (contrat RBAC Niveau 1)
│   └── service/
│       ├── PrincipalUtilisateur.java       (UserDetails enveloppant Utilisateur)
│       ├── CustomUserDetailsService.java   (chargement base + compte actif)
│       └── CurrentUserService.java         (utilisateur courant du contexte)
└── config
    └── SecurityConfig.java                 (chaîne de filtres + AuthenticationManager)
```

---

# 4. Fonctionnement

## 4.1 Connexion (prévue au LOOP auth — non implémentée ici)

```
POST /api/v1/auth/login (route publique)
   → AuthenticationService vérifie email / mot de passe (BCrypt)
   → JwtService.genererToken(userId) émet le token (sub + userId + iat + exp)
   → réponse : token (jamais de permissions)
```

## 4.2 Requêtes suivantes

```
Authorization: Bearer <token>
   → JwtAuthenticationFilter :
       1. lit le header, vérifie le préfixe "Bearer "
       2. jwtService.estValide(token)   (signature HS256 + expiration)
       3. jwtService.extraireUserId(token)
       4. userDetailsService.loadUserById(userId)   (relecture base, compte actif)
       5. Authentication positionnée dans le SecurityContext
   → en cas d'échec : aucune authentification → 401 (UnauthorizedHandler)
```

## 4.3 Contenu du token

| Claim | Valeur |
|-------|--------|
| `sub` | `String.valueOf(userId)` |
| `userId` | identifiant de l'utilisateur (Long) |
| `iat` | date d'émission |
| `exp` | `iat + app.security.jwt.expiration-ms` (1 h en dev) |

Algorithme : **HS256**, clé dérivée de `app.security.jwt.secret`
(variable d'environnement `JWT_SECRET` en production).

---

# 5. Configuration Spring Security

`config/SecurityConfig.java` :

- CSRF désactivé, CORS activé (bean `CorsConfig`) ;
- **sans état** : `SessionCreationPolicy.STATELESS` ;
- routes publiques : `/api/auth/**`, Swagger (`/swagger-ui.html`,
  `/swagger-ui/**`, `/v3/api-docs/**`) ;
- routes protégées : `/api/**` (authentifié) ;
- `JwtAuthenticationFilter` ajouté avant `UsernamePasswordAuthenticationFilter` ;
- réponses d'erreur uniformes (401 / 403 au format `ApiResponse`) ;
- `@EnableMethodSecurity` activé (préparation `@PreAuthorize` / `@RequirePermission`) ;
- beans `AuthenticationProvider` (DaoAuthenticationProvider : UserDetailsService
  + BCrypt) et `AuthenticationManager`.

---

# 6. RBAC — préparation (implémentation LOOP 3.9)

- `PermissionEvaluator` (interface) : contrat `hasPermission(module, action)` et
  `hasPermission(codePermission)`. L'implémentation utilisera
  `ProfilPermissionRepository` + `UtilisateurPermissionRepository`
  (droits effectifs = profil + ACCORDER − REFUSER, REFUSER prioritaire).
- `@RequirePermission(module, action)` : annotation prête pour les Controllers.
- `CurrentUserService` : expose l'utilisateur courant (utilisé par les Services
  pour le **périmètre Niveau 2**, jamais par un paramètre frontend).

---

# 7. Sécurité des mots de passe

- stockage uniquement en **BCrypt** (`BeanConfig.passwordEncoder`) ;
- `CustomUserDetailsService` refuse les comptes désactivés ;
- aucun secret en clair dans la base ni dans les réponses ;
- `UtilisateurResponse` n'expose jamais le mot de passe.

---

# 8. Configuration

## application.yml (base)

```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET:}
      expiration-ms: 3600000
      refresh-expiration-ms: 604800000
```

## application-dev.yml

```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET:dev-secret-cms-2026-...}
```

> En production : `JWT_SECRET` obligatoire (>= 256 bits pour HS256).

---

# 9. Tests effectués

| Test | Cas couverts | Résultat |
|------|--------------|----------|
| `JwtServiceTest` (7) | génération + claims (sub/userId/iat/exp), token modifié → invalide, expiré → invalide, mauvais secret → invalide, secret absent → erreur | ✅ |
| `JwtSecurityTest` (5) | **succès** : token émis + requête suivante authentifiée ; **mauvais mot de passe** → refus ; **compte désactivé** → refus ; **token invalide** → aucune authentification (401) ; sans header → aucune authentification | ✅ |
| Tests existants | ToolsValidation 3, MapperInjection 1, RepositoryInjection 1, UtilisateurService 11 | ✅ |

Total : **28/28 tests OK**, `mvn test` BUILD SUCCESS.

---

# 10. Ce qui reste à faire

- **LOOP 3.9** : MPD + migrations Flyway (tables, seed profils/permissions),
  implémentation complète du RBAC dynamique (`PermissionEvaluator`,
  droits effectifs dans `CustomUserDetailsService`).
- **LOOP 3.10** : Controllers (API REST) + endpoints `/api/auth/**`.
- Endpoints de connexion/déconnexion et actualisation du token.
- Journalisation des connexions (`JournalConnexion`) via `AuthenticationService`.

---

# 11. Vérifications de conformité (LOOP 3.8)

- ✅ aucune permission codée en dur ;
- ✅ aucun Controller métier créé ;
- ✅ aucune migration Flyway créée ;
- ✅ aucun accès au périmètre chantier implémenté ;
- ✅ token limité à `sub`, `userId`, `iat`, `exp`.
