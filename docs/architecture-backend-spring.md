# Architecture Backend Spring Boot — CMS

Version : 1.0
Statut : Architecture FIGÉE (aucun code, aucune Entity, aucune table)
Base : LOOP 3.1
Sources : modèle de données validé (LOOP 2.7), MASTER_PLAN §4, backend-structure.md, conventions (LOOP 0.3)

Référence pour : LOOP 3.2 (initialisation projet), puis le développement par couches.

---

# 1. DÉCISION STRUCTURELLE — PACKAGE RACINE

Le modèle du LOOP 3.1 propose `com.projet.chantier`. L'architecture déjà
**validée et compilée** (Phase 0/1) utilise `com.cms`.

**Décision : conserver `com.cms`.**

Justification :
* le package `com.cms` est figé depuis le LOOP 0.5 (validation architecture) ;
* tout le socle Phase 1 existe sous `com.cms` (config, common, exception) et
  compile (BUILD SUCCESS) ;
* le `pom.xml` (groupId `com.cms`) et les tests sont alignés ;
* `backend-structure.md` et MASTER_PLAN §4 utilisent déjà `com.cms`.

Renommer en `com.projet.chantier` invaliderait l'existant sans gain
fonctionnel → non retenu.

---

# 2. STRUCTURE DES PACKAGES

```
backend/src/main/java/com/cms
│
├── CmsApplication.java                ← point d'entrée Spring Boot
│
├── config/                            ← beans, CORS, OpenAPI, propriétés (ApplicationProperties)
│
├── security/                          ← authentification / autorisation / RBAC technique
│   ├── config/                        ← SecurityConfig, SecurityFilterChain
│   ├── jwt/                           ← JwtAuthenticationFilter, JwtService
│   ├── service/                       ← CustomUserDetailsService, AuthenticationService
│   ├── permission/                    ← PermissionEvaluator (droits + périmètre)
│   └── annotation/                    ← annotations @RequirePermission
│
├── common/                            ← ApiResponse, constantes, enums, utils, validation
│
├── exception/                         ← GlobalExceptionHandler + exceptions métier
│
│   ── MODULES MÉTIER (1 module = 1 domaine de la donnée validée) ──
│
├── utilisateur/                       ← Utilisateur, UtilisateurPermission
├── profil/                            ← Profil, ProfilPermission
├── permission/                        ← Permission
├── chantier/                          ← Chantier, PhotoChantier
├── equipe/                            ← Equipe, MembreEquipe, AffectationEquipeChantier
├── tache/                             ← Tache, AffectationTache, ValidationTache, Planning, Commentaire
├── document/                          ← Document, PieceJointe
└── audit/                             ← HistoriqueAction, JournalConnexion
```

Chaque module métier suit le pattern vertical :

```
module/
├── entity/        ← Entité JPA
├── repository/    ← Interface Spring Data JPA
├── service/       ← Interface + Impl (logique métier + sécurité)
├── controller/    ← API REST
├── dto/           ← Request / Response
└── mapper/        ← Conversion Entity ↔ DTO
```

## Analyse de l'architecture par domaine

**Adaptée ✅** — le découpage par domaine métier épouse le modèle Merise validé :
* cohésion : chaque module contient toutes les couches d'un même domaine ;
* évolutivité : ajout d'un module (Client, Notification...) sans toucher aux autres ;
* sécurité : le contrôle de périmètre se fait au niveau du module concerné.

## Correspondance entités (19) → modules (8)

| Module | Entités | Nombre |
|--------|---------|:---:|
| utilisateur | Utilisateur, UtilisateurPermission | 2 |
| profil | Profil, ProfilPermission | 2 |
| permission | Permission | 1 |
| chantier | Chantier, PhotoChantier | 2 |
| equipe | Equipe, MembreEquipe, AffectationEquipeChantier | 3 |
| tache | Tache, AffectationTache, ValidationTache, Planning, Commentaire | 5 |
| document | Document, PieceJointe | 2 |
| audit | HistoriqueAction, JournalConnexion | 2 |
| **Total** | | **19** |

## Modules existants hors CORE (placeholders)

`notification`, `dashboard`, `materiel`, `stock` : modules vides créés en Phase 0,
sans entité CORE. **Conservés vides** (modules futurs, aucun code à ce stade).

`planning` (vide) et `rbac` (vide) : obsolètes car les entités sont réparties dans
`tache` (Planning) et `profil`/`permission` (RBAC). **Nettoyage à faire au LOOP 3.2**
(suppression des packages vides, sans impact fonctionnel).

---

# 3. RESPONSABILITÉ DES COUCHES

## Entity
* Représentation de la table (mapping colonnes, types).
* Relations JPA (`@ManyToOne`, `@OneToMany`, `@ManyToMany` via tables associatives).
* **Ne contient aucune logique métier complexe** (aucune règle, aucun calcul).

## Repository
* Accès aux données via Spring Data JPA.
* Méthodes dérivées (findByX) ou `@Query` pour requêtes complexes / pagination.
* **Jamais appelé depuis un Controller** — toujours via le Service.

## Service (interface + Impl)
* Toute la logique métier (transactions `@Transactional`).
* **Sécurité métier** : contrôle du périmètre (Niveau 2) et vérification des
  permissions fonctionnelles (Niveau 1) — jamais dans le Controller.
* Validation métier et orchestration des Repository.

## Controller
* Exposition REST (`/api/v1/...`).
* Réception des requêtes, délégation au Service, retour des DTO.
* **Aucune logique métier**, aucune manipulation d'Entity exposée directement.

## DTO
* Objets d'échange API (Request / Response) indépendants des Entity.
* Validation d'entrée via Bean Validation (`@NotBlank`, `@Email`...).
* **Les Entity ne sont jamais exposées directement à l'API.**

## Mapper
* Conversion Entity ↔ DTO (MapStruct).
* Règles de mapping centralisées et testables.

## Règles de couche (transverses)
* Controller → Service → Repository (dépendance descendante uniquement).
* Aucune Entity en sortie d'API ; aucun DTO en entrée de Repository.
* Les DTO d'un module ne dépendent pas des DTO d'un autre module (préférence).

---

# 4. ARCHITECTURE RBAC TECHNIQUE

## Composants (package security/)

| Composant | Responsabilité |
|-----------|----------------|
| `JwtService` | Génération, validation, lecture des tokens JWT (claims : subject = userId) |
| `JwtAuthenticationFilter` | Intercepte les requêtes, valide le Bearer token, peuple le SecurityContext |
| `CustomUserDetailsService` | Charge l'utilisateur + **droits effectifs** (profil + ACCORDER − REFUSER) |
| `SecurityConfig` | SecurityFilterChain : stateless, CSRF off, routes publiques/protégées, `@EnableMethodSecurity` |
| `PermissionEvaluator` | Vérifie la permission fonctionnelle (Niveau 1) + prépare le contrôle de périmètre (Niveau 2) |
| `AuthenticationService` | Login (vérification BCrypt, émission JWT), logout, actualisation |

## Flux d'authentification

```
POST /api/v1/auth/login (public)
   → AuthenticationService vérifie credentials (BCrypt)
   → JwtService génère le token (claims : userId)
   → réponse : token + profil + permissions effectives

Requêtes suivantes :
   Authorization: Bearer <token>
   → JwtAuthenticationFilter valide le token
   → CustomUserDetailsService charge l'utilisateur + ses droits effectifs depuis la base
   → SecurityContext initialisé
```

## Autorisation — RBAC dynamique (100 % en base)

* **Niveau 1 (fonctionnelle)** : permissions en base
  (`profil_permission`, `utilisateur_permission`). Chargées à chaque requête
  → une modification (ajout/retrait de permission) est **effective immédiatement**,
  sans reconnexion.
* Droits effectifs = permissions du profil + ACCORDER − REFUSER
  (REFUSER prioritaire). Calcul réalisé dans `CustomUserDetailsService`.
* Contrôle au point d'entrée :
  * annotations `@RequirePermission("TACHE_VIEW")` ou
    `@PreAuthorize("hasAuthority('TACHE_VIEW')")` ;
  * le token ne transporte **pas** les permissions (sécurité : toujours
    relecture depuis la base).

## Autorisation — Périmètre (Niveau 2, restriction des données)

Le contrôle de visibilité ne peut pas être exprimé uniquement dans les
annotations : il est **exécuté dans la couche Service** via un helper sécurité.

| Ressource | Règle de visibilité |
|-----------|---------------------|
| Chantier | visible si une équipe de l'utilisateur y est affectée (`affectation_equipe_chantier`) ou s'il est responsable |
| Equipe | visible si l'utilisateur en est membre (`membre_equipe`) |
| Tache | visible si affectée à l'utilisateur (`affectation_tache`) ou à l'une de ses équipes |
| Historique | visible : le sien (JournalConnexion) / habilitations pour l'historique global |

`PermissionEvaluator` (Niveau 1) valide le **droit d'action** ;
le Service (Niveau 2) applique la **restriction des données**.
Une permission ne donne jamais accès à toutes les données.

---

# 5. GESTION DES ERREURS

## Hiérarchie d'exceptions (package exception/)

| Exception | Code HTTP | Usage |
|-----------|-----------|-------|
| `CmsException` (base) | — | Règle métier (message + code) |
| `BadRequestException` | 400 | Requête invalide |
| `ResourceNotFoundException` | 404 | Ressource absente |
| `UnauthorizedException` | 401 | Non authentifié / token invalide |
| `ForbiddenException` | 403 | Permission refusée |
| `ValidationException` | 400 | Échec de validation métier |

## GlobalExceptionHandler (@RestControllerAdvice)

* Capture toutes les exceptions ci-dessus.
* Gère les erreurs Spring : `MethodArgumentNotValidException` (erreurs @Valid),
  `HttpMessageNotReadableException`, `AccessDeniedException`, `AuthenticationException`.
* Retourne toujours le format standard :

```json
{
  "success": false,
  "message": "...",
  "data": null,
  "timestamp": "..."
}
```

Le `ApiResponse` et le handler existent déjà (LOOP 1.7) — le LOOP 3.2 ajoutera
`ValidationException` et le mapping `@Valid` si manquant.

---

# 6. CONFIGURATION (resources/)

## Fichiers

```
src/main/resources/
├── application.yml        ← configuration principale (commun)
├── application-dev.yml    ← dev : base locale, logs DEBUG
├── application-test.yml   ← tests : base dédiée, logs WARN
├── application-prod.yml   ← prod : variables d'environnement uniquement
└── db/migration/          ← scripts Flyway (créés plus tard)
```

## Blocs de configuration

| Bloc | Contenu | État |
|------|---------|------|
| PostgreSQL | URL, user, pool Hikari (dev/test/prod) | ✅ déjà configuré (Phase 1) |
| JPA | `ddl-auto: none`, `open-in-view: false` | ✅ déjà configuré |
| Flyway | locations, validate-on-migrate | ✅ déjà configuré |
| JWT | `secret` (env `JWT_SECRET`), expiration access/refresh | ⏳ à ajouter (LOOP 3.2) |
| Logs | par profil (dev DEBUG / test WARN / prod WARN+INFO) | ✅ déjà configuré (LOOP 1.7) |
| App | `app.*` via `ApplicationProperties` | ✅ déjà configuré |

## Paramètres d'environnement (prod)

```
DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET
```

---

# 7. CONVENTIONS DE CODE

## Nommage

| Élément | Convention | Exemple |
|---------|------------|---------|
| Classe métier | Entité + Rôle | `UtilisateurService`, `ChantierController`, `TacheRepository` |
| Impl Service | `XxxServiceImpl` | `UtilisateurServiceImpl` |
| DTO | `XxxRequest` / `XxxResponse` | `TacheRequest`, `TacheResponse` |
| Mapper | `XxxMapper` (MapStruct) | `TacheMapper` |
| Tables SQL | `snake_case`, singulier | `utilisateur`, `profil_permission` |
| Colonnes SQL | `snake_case` | `date_creation`, `role_dans_equipe` |
| Variables Java | `camelCase` | `dateCreation`, `profilId` |
| Routes API | `/api/v1/ressource` | `/api/v1/taches` |

## API

* Préfixe `/api/v1`, versionnage dans l'URL.
* Format de réponse uniforme : `ApiResponse<T>` (succès/erreur).
* Identifiants en `Long` ; énumérés en constantes/`enum` Java (mêmes libellés que le MLD : `EN_COURS`, `REFUSER`...).

---

# 8. STRATÉGIE DE DÉVELOPPEMENT

Ordre de création (par module, découpage vertical) :

| # | Étape | Contenu |
|---|-------|---------|
| 1 | Configuration projet | pom (déjà prêt), JWT config, Flyway initialisation |
| 2 | Entités JPA | les 19 entités (mapping MLD) |
| 3 | Repository | interfaces Spring Data JPA |
| 4 | DTO | Request / Response + validation |
| 5 | Mapper | MapStruct Entity ↔ DTO |
| 6 | Service | logique métier + sécurité de périmètre |
| 7 | Controller | API REST + annotations de permission |
| 8 | Sécurité | RBAC, permissions effectives, tests d'accès |
| 9 | Tests | unitaires (service/mapper) + intégration (repository/controller) |

Ordre d'implémentation des modules (dépendances) :
1. `profil`, `permission`, `utilisateur` (RBAC, base de tout) ;
2. `audit` (traçabilité, utile partout) ;
3. `chantier`, `equipe` (organisation) ;
4. `tache` (dépend de chantier/equipe/utilisateur) ;
5. `document` (dépend de chantier/utilisateur).

Les données initiales (profils ADMINISTRATEUR / UTILISATEUR_STANDARD, permissions
système) seront insérées via **migration Flyway / seed** lors de l'implémentation.

---

# 9. CE QUI RESTE À FAIRE (prochains loops)

* **LOOP 3.2** : initialisation du projet, nettoyage des modules vides obsolètes
  (`rbac`, `planning`), configuration JWT, vérification du démarrage.
* **LOOP 3.3** : migrations Flyway (tables + seed profils/permissions), puis
  entités JPA et développement par couches (étapes 2 à 9 du §8).

---

FIN DU DOCUMENT — ARCHITECTURE BACKEND FIGÉE
