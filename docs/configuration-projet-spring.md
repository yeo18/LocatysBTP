# Configuration Projet Spring Boot — CMS

Version : 1.0
Statut : Socle technique PRÊT (aucun code métier)
Base : LOOP 3.2
Sources : MASTER_PLAN, LOOP 3.1 (architecture), Phase 1 (socle déjà en place)

Référence pour : LOOP 3.3 (entités JPA) et le développement métier.

---

# 1. VERSIONS (ENVIRONNEMENT VALIDÉ)

| Élément | Version | Statut |
|---------|---------|--------|
| Java | 25.0.2 (Oracle JDK) | ✅ validé |
| Spring Boot | 3.5.3 (parent Maven) | ✅ compatible |
| Maven | 3.9.11 | ✅ validé |
| PostgreSQL | 16.14 (Docker, conteneur `chantier-db`) | ✅ validé |
| GroupId / ArtifactId | `com.cms` / `cms-backend` | ✅ figé |
| Package racine | `com.cms` | ✅ figé |

Vérification : `mvn compile` → **BUILD SUCCESS** ; `mvn test` → **3/3 OK**.

---

# 2. DÉPENDANCES MAVEN (pom.xml)

| Dépendance | Version | Rôle |
|------------|---------|------|
| spring-boot-starter-web | géré (3.5.3) | API REST |
| spring-boot-starter-data-jpa | géré | Accès données |
| spring-boot-starter-security | géré | Sécurité (préparée) |
| spring-boot-starter-validation | géré | Validation DTO |
| postgresql | géré (42.x) | Driver PostgreSQL |
| flyway-core + flyway-database-postgresql | géré | Migrations |
| lombok | géré | Réduction de code |
| mapstruct 1.6.3 + processeur | 1.6.3 | Mapping Entity ↔ DTO |
| springdoc-openapi-starter-webmvc-ui | 2.8.5 | Documentation API |
| **jjwt-api / jjwt-impl / jjwt-jackson** | **0.12.6** | **JWT (ajouté LOOP 3.2)** |
| spring-boot-starter-test | géré | Tests |
| spring-security-test | géré | Tests sécurité |
| testcontainers (junit-jupiter, postgresql, BOM 1.20.4) | 1.20.4 | Tests intégration |

## Choix de la bibliothèque JWT

**Retenue : JJWT (`io.jsonwebtoken`, version 0.12.6).**

| Bibliothèque | Pour / Contre |
|--------------|---------------|
| **JJWT 0.12.x** ✅ | API fluide simple (`Jwts.builder()/parser()`), support HMAC/EC/RSA, JSON via Jackson (déjà présent), maintenue activement, largement utilisée en Spring |
| spring-security-oauth2-jose (Nimbus JOSE) | Standard robuste mais API plus lourde, orientée OAuth2 |
| auth0 java-jwt | Simple mais moins intégrée à l'écosystème Spring Security |

Décision : **JJWT 0.12.6** (api + impl runtime + jackson runtime). Config déjà
préparée dans `application.yml` (`app.security.jwt.*`).

---

# 3. CONFIGURATION BASE DE DONNÉES (PostgreSQL Docker)

| Paramètre | Valeur |
|-----------|--------|
| Hôte / Port | localhost:5432 |
| Base (dev) | `gestion_de_chantier` |
| Utilisateur (dev) | `admin` / `admin123` |
| Base (test) | `gestion_de_chantier_test` |
| Production | variables d'environnement `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |

* `ddl-auto: none` → Hibernate ne crée/modifie **jamais** les tables.
* Toutes les tables viendront de **Flyway** (aucune table créée à ce stade).
* HikariCP : pool dev 10/2, prod 20/5, timeout 30 s.

---

# 4. ENVIRONNEMENTS

```
resources
├── application.yml        ← commun (app.*, JWT, CORS, port, profil actif)
├── application-dev.yml    ← PostgreSQL local Docker + logs DEBUG (SQL)
├── application-test.yml   ← base dédiée, logs WARN/DEBUG
└── application-prod.yml   ← 100 % variables d'environnement, sécurité
```

| Environnement | Datasource | Logs | Flyway |
|---------------|------------|------|--------|
| dev (défaut) | locale Docker | root INFO / com.cms DEBUG / Hibernate SQL DEBUG | enabled |
| test | `gestion_de_chantier_test` | WARN / DEBUG | enabled |
| prod | env vars | WARN / INFO | enabled |

Variables d'environnement (prod) : `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`JWT_SECRET`, `CORS_ALLOWED_ORIGINS`.

---

# 5. SÉCURITÉ PRÉPARÉE

## Configuration présente
* `spring-boot-starter-security` + `SecurityConfig` (stateless prévu, CSRF désactivé).
* JWT configuré dans `application.yml` :
  * `app.security.jwt.secret` → env `JWT_SECRET` (aucun secret en clair) ;
  * `app.security.jwt.expiration-ms` = 3 600 000 (1 h) ;
  * `app.security.jwt.refresh-expiration-ms` = 604 800 000 (7 j).
* Bibliothèque JJWT 0.12.6 prête (LOOP 3.2).

## Composants à implémenter (phase JWT)
`JwtService`, `JwtAuthenticationFilter`, `CustomUserDetailsService`,
`PermissionEvaluator`, `AuthenticationService` (cf. architecture LOOP 3.1 §4).
**Non créés à ce stade.**

## CORS — Frontend React (ajouté LOOP 3.2)
| Paramètre | Valeur |
|-----------|--------|
| Origines autorisées | `app.cors.allowed-origins` → dev `http://localhost:3000`, prod `CORS_ALLOWED_ORIGINS` |
| Méthodes HTTP | GET, POST, PUT, PATCH, DELETE, OPTIONS |
| Headers autorisés | Authorization, Content-Type, Accept |
| Headers exposés | Authorization |
| Credentials | true |

Implémentation : `config/CorsConfig.java` (bean `CorsConfigurationSource`)
activé dans `SecurityConfig` via `http.cors(...)`.

---

# 6. OUTILS DE QUALITÉ

## Formatage / conventions
* Conventions définies au LOOP 0.3 (`docs/development-conventions.md`) :
  indentation, nommage Java `camelCase`, tables SQL `snake_case`.
* Architecture et nommage par domaine : LOOP 3.1.

## Logs (SLF4J / Logback)
* SLF4J fourni par Spring Boot ; niveaux par profil (dev DEBUG, test WARN, prod WARN/INFO).
* Logs SQL Hibernate en dev uniquement.

## Documentation API (OpenAPI/Swagger)
* `springdoc-openapi` 2.8.5 configuré (LOOP 1.5) :
  * Swagger UI : `/swagger-ui.html` ;
  * OpenAPI JSON : `/v3/api-docs` ;
  * schéma de sécurité Bearer JWT préparé.

---

# 7. STRUCTURE INITIALE DES PACKAGES

## Transverses (présents et prêts)

```
com.cms
├── config/          ← BeanConfig, CorsConfig (LOOP 3.2), JacksonConfig,
│                      SecurityConfig, SwaggerConfig, openapi/, properties/
├── common/          ← constants/, enums/, response/ (ApiResponse), mapper/, validation/
├── exception/       ← custom/ (CmsException + 4 dérivées), handler/ (GlobalExceptionHandler)
└── security/        ← vide (préparé pour la phase JWT)
```

## Modules métier (placeholders vides, à remplir après validation des entités)

`utilisateur`, `profil`, `permission`, `chantier`, `equipe`, `tache`,
`document`, `audit` → à créer/activer au LOOP 3.3.

Modules vides non-CORE : `notification`, `dashboard`, `materiel`, `stock`
(conservés pour les modules futurs).
Modules obsolètes `rbac` et `planning` : **nettoyage différé** (aucun impact,
packages vides — suppression sans danger au prochain loop de refactoring).

## Flyway (organisation future)

```
resources/db/migration/
├── V1__init.sql            ← existe (marqueur technique)
├── V2__create_tables.sql   ← à venir (LOOP 3.3, tables métier)
├── V3__security.sql        ← à venir (RBAC, profils, permissions)
└── V4__constraints.sql     ← à venir (contraintes CHECK, index)
```

Aucun fichier SQL métier créé dans ce loop.

---

# 8. VÉRIFICATIONS EFFECTUÉES (LOOP 3.2)

| Vérification | Résultat |
|--------------|:---:|
| Java 25.0.2 compatible | ✅ |
| Spring Boot 3.5.3 compatible | ✅ |
| Projet compilable (`mvn compile`) | ✅ BUILD SUCCESS |
| Tests (`mvn test`) | ✅ 3/3 |
| Aucune Entity / Controller / Service / Repository métier | ✅ |
| Aucune table SQL / migration métier créée | ✅ |

---

FIN DU DOCUMENT — SOCLE TECHNIQUE PRÊT
