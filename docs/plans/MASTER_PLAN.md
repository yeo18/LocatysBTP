# MASTER PLAN — CMS (Chantier Management System)

Version : 1.0
Projet : Application professionnelle de gestion de chantier sécurisée
Méthode : Loop Engineering

---

# 1. IDENTITÉ DU PROJET

## Nom

CMS - Chantier Management System

## Objectif

Construire une application professionnelle permettant aux entreprises de BTP de gérer :

* les chantiers ;
* les équipes ;
* les employés ;
* les tâches ;
* les validations ;
* les historiques ;
* les droits d'accès.

L'application doit être sécurisée avec un système RBAC dynamique.

---

# 2. PRINCIPES NON NÉGOCIABLES

## Sécurité

Principe :

> Un utilisateur voit uniquement les ressources auxquelles il a droit.

Le frontend ne doit jamais être considéré comme une sécurité.

Toutes les vérifications doivent être faites dans le backend.

---

## Architecture

Backend :

Spring Boot

Frontend :

React

Base :

PostgreSQL

Sécurité :

Spring Security + JWT

Autorisation :

RBAC dynamique

---

# 3. RÈGLE DE TRAVAIL LOOP ENGINEERING

Chaque intervention doit respecter :

## Avant modification

L'agent doit :

1. Lire MASTER_PLAN.md.
2. Vérifier l'état actuel.
3. Identifier le LOOP concerné.
4. Expliquer les fichiers impactés.
5. Vérifier les risques.

---

## Après modification

L'agent doit produire obligatoirement :

# RAPPORT DE FIN DE LOOP

Format obligatoire :

## LOOP réalisé

Nom :

Numéro :

## Objectif initial

...

## Fichiers créés

Liste complète :

...

## Fichiers modifiés

Liste complète :

...

## Opérations réalisées

Décrire :

* classes créées ;
* méthodes ajoutées ;
* configurations modifiées ;
* dépendances ajoutées ;
* commandes exécutées.

## Tests effectués

Indiquer :

* tests lancés ;
* résultats.

## Ce qui fonctionne

...

## Ce qui reste à faire

...

## Problèmes rencontrés

...

## État du projet

Terminé :

En cours :

À venir :

---

# 4. ARCHITECTURE TECHNIQUE

## Backend

Technologie :

* Java
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL
* Flyway
* JWT
* Swagger
* JUnit
* TestContainers

---

## Structure recommandée

```
backend/src/main/java/com/cms


config/

security/

common/

exception/


utilisateur/

profil/

permission/

chantier/

equipe/

tache/

historique/

```

Chaque module possède :

```
entity/

repository/

service/

controller/

dto/

mapper/

```

---

# 5. MODÈLE MÉTIER

## Utilisateur

Un utilisateur possède :

* nom
* prénom
* email
* mot de passe
* profil

Un utilisateur peut :

* appartenir à une équipe ;
* travailler sur plusieurs chantiers ;
* recevoir des tâches.

---

## Profil

Un profil représente un rôle métier.

Exemples :

* ADMINISTRATEUR
* CHEF_EQUIPE
* OUVRIER

---

## Permission

Une permission représente une action.

Exemples :

```
USER_CREATE

USER_DELETE

CHANTIER_VIEW

TACHE_VALIDATE
```

---

## RBAC

Relations :

```
PROFIL
 |
 |
PROFIL_PERMISSION
 |
 |
PERMISSION
```

Exception :

```
UTILISATEUR
 |
 |
UTILISATEUR_PERMISSION
```

---

## Chantier

Un chantier contient :

* équipes ;
* utilisateurs ;
* tâches.

---

## Tâche

Une tâche possède :

* titre ;
* description ;
* priorité ;
* statut ;
* responsables.

Statuts :

```
A_FAIRE
EN_COURS
TERMINE
VALIDE
REFUSE
```

---

# 6. ÉTAT D'AVANCEMENT DU PROJET

## PHASE 0 — Architecture

Etat :

✅ Terminée (LOOP 0.1 à 0.5)

LOOP 0.5 : ✅ Terminé
Résumé : Validation finale de l'architecture. Audit de l'arborescence (253 dossiers, 16 packages racines, 12 modules × 8 sous-packages), vérification des dépendances (5 starters présents, Spring Security/Lombok/Flyway/OpenAPI à ajouter en Phase 1), configurations validées (application.yml + 3 profils cohérents, datasource gestion_de_chantier), compilation Maven BUILD SUCCESS, démarrage Spring Boot validé (Tomcat port 8090, 404 sur / attendu sans endpoint). Conventions globalement respectées. Divergences mineures notées : module historique nommé audit, profil/permission fusionnés dans rbac, modules supplémentaires (notification, dashboard, planning, document, materiel, stock) non documentés. Décision : architecture entièrement validée pour démarrer la Phase 1.

LOOP 0.4 : ✅ Terminé
Résumé : Ossature professionnelle du backend créée (structure uniquement, aucun code métier). Arborescence src/main/java/com/cms avec packages : config (SwaggerConfig, JacksonConfig, BeanConfig vides), security (config, jwt, filter, service, handler, permission, annotation), common (constants, enums, response, utils, validation), exception (handler, custom), et 12 modules (audit, utilisateur, chantier, tache, equipe, rbac, notification, dashboard, planning, document, materiel, stock) chacun avec sous-packages entity/repository/service/controller/dto/mapper/validator/specification. Projet Spring Boot de base : CmsApplication.java, pom.xml (Spring Boot 3.5.3, Java 25), application.yml + profils dev/test/prod, dossier db/migration vide (aucune migration SQL), src/test/java/com/cms symétrique. Aucune Entity, aucun Repository/Service/Controller métier, aucune API REST, aucune logique JWT/RBAC.

LOOP 0.4 — Suite : Maven 3.9.11 installé (C:\Users\tcher\Documents\outils\apache-maven-3.9.11), JAVA_HOME et MAVEN_HOME configurés. Build vérifié (mvn compile OK) et démarrage validé : application compilée, Tomcat démarré, serveur répond sur http://localhost:8090 (404 sur / car aucun endpoint, comportement attendu). Port modifié de 8080 à 8090 car 8080 occupé par Docker Desktop et 8081 par httpd.

LOOP 0.1 : ✅ Terminé
Résumé : Audit complet du projet. Projet vide (backend/, frontend/, docs/ vides). Seul MASTER_PLAN.md existe. PostgreSQL opérationnel (conteneur chantier-db, port 5432, base gestion_de_chantier créée mais sans tables). Environnement : Java 25.0.2 LTS et Node 24.13.0 installés ; Maven absent. Aucun code backend/frontend existant. Aucune table CMS. Aucun système d'authentification. Le projet part de zéro, structure cible à créer (Spring Boot + React + PostgreSQL).

LOOP 0.2 : ✅ Terminé
Résumé : Documentation technique complète créée dans docs/ : architecture.md (vision + architecture globale), database-design.md (9 tables prévues, cardinalités Merise, MLD), security-design.md (JWT, refresh token, RBAC dynamique, règles métier), backend-structure.md (packages com.cms, pattern module), frontend-structure.md (React, routes, composants, connexion API), api-convention.md (/api/v1, format réponse succès/erreur, codes HTTP, pagination), development-workflow.md (branches Git, Loop Engineering), project-status.md (CMS v0.1, état). Aucune table ni code créé, documentation uniquement.

LOOP 0.3 : ✅ Terminé
Résumé : Guide officiel des conventions de développement créé (docs/development-conventions.md) : principes Clean Code, chaîne Controller→Service→Repository→Database, structure des packages com.cms, nommage des classes Java (Entity, Repository, Service/ServiceImpl, Controller, DTO), noms de méthodes standards, REST API /api/v1, format de réponse commun succès/erreur, gestion des exceptions (@ControllerAdvice, GlobalExceptionHandler), sécurité (JWT, BCrypt, RBAC dynamique, jamais uniquement côté frontend), nommage BDD snake_case, Git (main/develop/feature/bugfix/hotfix, commits feat/fix/docs), tests (JUnit, Spring Boot Test, TestContainers), Loop Engineering. Documentation uniquement, aucun code créé.

LOOPS :

* 0.1 Analyse projet
* 0.2 Documentation
* 0.3 Convention code
* 0.4 Structure backend
* 0.5 Validation architecture

---

## PHASE 1 — Socle Spring Boot

Etat :

✅ Terminée (LOOP 1.1 à 1.8)

LOOP 1.8 : ✅ Terminé
Résumé : Validation finale du socle technique Spring Boot (audit complet de la Phase 1). Audit général : 12 modules métier vides, config/security/common/exception corrects, 11 docs cohérentes, aucune Entity/Repository/Service/Controller/DTO métier (seul @RestControllerAdvice du handler), migration V1 technique pure, 2 incohérences docs corrigées (module historique→audit, .properties→.yml). Maven : clean compile BUILD SUCCESS, tests 3/3 OK. Spring Boot : démarrage 5.5s OK, profil dev actif confirmé. PostgreSQL : seule table flyway_schema_history (ddl-auto none respecté). Flyway : version 1 (init) success=true, "Schema up to date". Swagger : UI 200 + OpenAPI 200 (title CMS v0.1.0, serveur localhost:8090). Outils : Lombok OK, MapStruct génère ToolsValidationMapperImpl, Bean Validation présent. Sécurité future préparée (spring-boot-starter-security + SecurityConfig permitAll + schéma Bearer OpenAPI + SystemRoles). Note de transition Phase 2 créée (docs/transition-phase2.md : MCC, inventaire entités, cardinalités, MLD, plan MCD→MLD→MPD). Aucun code métier créé.
LOOP 1.7 : ✅ Terminé
Résumé : Infrastructure commune et configuration centralisée. Config centralisée : ApplicationProperties (prefix app, groupes application/security/api, secrets via variables d'env), blocs app dédupliqués des profils. Constantes dans common/constants : Messages, ApiRoutes, SystemRoles, SpringProfiles, DateFormats, TechnicalValues (6 classes finales). Enums techniques dans common/enums : EnvironmentType, ApiVersion, ApplicationMode. Réponse API standard : ApiResponse<T> (success/message/data/timestamp + méthodes statiques success/error). Gestion erreurs : CmsException (base abstraite), BadRequestException, ResourceNotFoundException, UnauthorizedException, ForbiddenException, GlobalExceptionHandler (@RestControllerAdvice). Logs organisés par profil (dev : root INFO/com.cms DEBUG/SQL DEBUG ; test : WARN/DEBUG ; prod : WARN/INFO). Validation : compile + tests 3/3 OK, démarrage 5.5s OK, Swagger 200 OK, aucun conflit. Documentation mise à jour (development-conventions §14, architecture, project-status). Aucun code métier.

LOOP 1.6 : ✅ Terminé
Résumé : Configuration des outils de développement. Lombok : présent (version gérée par Spring Boot, exclusion dans spring-boot-maven-plugin OK). MapStruct 1.6.3 ajouté avec processeur d'annotations configuré (annotationProcessorPaths : lombok + mapstruct-processor + lombok-mapstruct-binding 0.2.0), composant Spring par défaut (-Amapstruct.defaultComponentModel=spring, corrigé en annotationProcessorArgs). Bean Validation : spring-boot-starter-validation présent, annotations Jakarta disponibles (@NotNull, @NotBlank, @Size, @Email, @Positive, @Past, @Future). Packages communs : common/validation et common/mapper (préparés). Test technique ToolsValidationTest créé (source/target/mapper/test) validant les 3 outils ensemble : 3/3 tests réussis, implémentation MapStruct générée avec @Component (Spring Component Model). Base gestion_de_chantier_test créée pour le profil test. Documentation mise à jour (development-conventions.md : stratégies DTO/Mapper/Validation, project-status.md). Aucun code métier créé.

LOOP 1.5 : ✅ Terminé
Résumé : Configuration d'OpenAPI/Swagger. Dépendance springdoc-openapi-starter-webmvc-ui 2.8.5 validée (compatible Spring Boot 3.5). Package dédié com.cms.config.openapi créé avec OpenApiConfig centralisée (titre, description, version 0.1.0, contact, licence, serveur dev localhost:8090, schéma de sécurité HTTP Bearer JWT préparé pour les futures API). Swagger UI configuré (tri tags alpha, tri opérations par méthode, doc-expansion none, chemin /swagger-ui.html, api-docs /v3/api-docs). Validation : compilation BUILD SUCCESS, démarrage OK, URLs testées toutes en 200 (swagger-ui.html, swagger-ui/index.html, v3/api-docs), schéma OpenAPI valide (0 endpoint métier, aucun warning critique). Documentation docs/swagger-documentation.md créée.

LOOP 1.4 : ✅ Terminé
Résumé : Configuration professionnelle de Flyway. Dépendances validées : flyway-core 11.7.2, flyway-database-postgresql 11.7.2, postgresql 42.7.7, spring-data-jpa 3.5.1, HikariCP 6.3.0 (aucun doublon). Hibernate : ddl-auto=none (ne modifie jamais la base). Flyway configuré (dev/test/prod) : enabled=true, locations=classpath:db/migration, encoding=UTF-8, validate-on-migrate=true, baseline-on-migrate=false. Dossier db/migration/ préparé. Migration technique V1__init.sql créée (aucune table métier, marqueur SELECT 1). Validation : démarrage réussi, table d'historique flyway_schema_history créée, "Successfully applied 1 migration... now at version v1", aucune table métier créée. Documentation docs/flyway-strategy.md créée (convention Vx__nom.sql, règles atomique/réversible/documentée/testée, bonnes pratiques).

LOOP 1.3 : ✅ Terminé
Résumé : Configuration de la connexion PostgreSQL. Environnement vérifié : PostgreSQL 16.14 (Docker, conteneur chantier-db), port 5432 accessible, base gestion_de_chantier présente (user admin), pgAdmin accessible. application-dev.yml : datasource complète (url jdbc:postgresql://localhost:5432/gestion_de_chantier, user admin/admin123, Hikari pool 10/2, timeout 30s), JPA ddl-auto=none (jamais de création auto de table), show-sql true, open-in-view false, dialect retiré (détection auto). application-test.yml : base dédiée gestion_de_chantier_test (pool réduit). application-prod.yml : variables d'environnement uniquement (DB_URL, DB_USERNAME, DB_PASSWORD), pool 20/5. Exclusions temporaires BDD retirées d'application.yml. Validation : compilation BUILD SUCCESS, démarrage réussi (HikariPool-1 Start completed, connexion PgConnection établie, Started CmsApplication en 11.7s), aucune table créée automatiquement, aucune erreur Hibernate. Aucune Entity/table/migration créée.

LOOP 1.2 : ✅ Terminé
Résumé : Configuration des profils Spring Boot. application.yml : point d'entrée, profil actif par défaut = dev, exclusions temporaires BDD conservées. application-dev.yml : structure complète commentée (datasource locale gestion_de_chantier, JPA ddl-auto none, flyway disabled, logging DEBUG, server 8090, springdoc /swagger-ui.html, app.name/version). application-test.yml : config indépendante (base gestion_de_chantier_test, logging WARN/DEBUG, flyway disabled). application-prod.yml : structure seule, toutes les valeurs via variables d'environnement (DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET), flyway enabled, logging WARN/INFO. Compilation BUILD SUCCESS, démarrage validé avec profil "dev" actif confirmé, aucun conflit ni warning de profil, arrêt propre. Aucune connexion PostgreSQL réelle, aucune fonctionnalité métier.

LOOP 1.1 : ✅ Terminé
Résumé : Audit et normalisation du projet Spring Boot. Versions vérifiées (Java 25.0.2, Spring Boot 3.5.3, Maven 3.9.11, packaging jar, UTF-8). pom.xml normalisé : ajout de spring-boot-starter-security, Lombok, flyway-core + flyway-database-postgresql, springdoc-openapi 2.8.5, spring-security-test, Testcontainers (junit-jupiter + postgresql, BOM 1.20.4) ; maven-compiler-plugin configuré (release 25, UTF-8). application.yml réorganisé avec commentaires, exclusions temporaires des auto-configurations BDD (Data Source, Flyway, JPA) en attendant le LOOP de configuration PostgreSQL. Profils dev/test/prod épurés (config complète au LOOP 1.2). SecurityConfig minimal ajouté (permitAll temporaire, csrf désactivé) pour permettre le démarrage. Compilation BUILD SUCCESS, tests BUILD SUCCESS, démarrage validé (Tomcat port 8090, 404 attendu) et arrêt propre confirmé. Aucune fonctionnalité métier créée.

LOOPS :

* 1.1 Création projet
* 1.2 Maven
* 1.3 PostgreSQL
* 1.4 Swagger
* 1.5 Démarrage

---

## PHASE 2 — Modèle JPA

Etat :

✅ Terminée (LOOP 3.16 terminé — modules Document et Audit complets)

LOOP 3.16 : ✅ Terminé
Résumé : Finalisation des modules Document (Document, PieceJointe, PhotoChantier) et Audit (HistoriqueAction, JournalConnexion). Aucune Entity/Enum/attribut/relation/module/package/modification de référentiel de permissions/.md créé. Créés : 5 Services + 5 ServiceImpl (conformes aux contrats LOOP 3.6 : deposer/modifier/changerStatut/trouverParId/listerParChantier/rechercher pour Document ; ajouter/supprimer/listerParDocument pour PieceJointe ; ajouter/modifierDescription/trouverParId/listerParChantier pour PhotoChantier ; enregistrer/listerParUtilisateur/rechercher pour les 2 journaux audit, immuables sans update/delete) + 5 Controllers REST (/api/v1/documents, /pieces-jointes, /photos-chantiers, /historique-actions, /journal-connexions). Règles métier appliquées : Document rattaché obligatoirement au chantier (MCD 1,1), uploader/priseur = utilisateur connecté (CurrentUserService), PieceJointe un seul mode de rattachement (document_id OU entite_type/entite_id → 400 si aucun ou les deux), HistoriqueAction/JournalConnexion écriture seule. RBAC : permissions existantes réutilisées, aucune créée — CHANTIER_* pour document (décision utilisateur), UTILISATEUR_* pour audit. Routes ajoutées dans ApiRoutes. Tests : 5 test de service (10+7+9+4+5 = 35) + 5 test de controller (6+4+6+4+4 = 24) — 252/252 tests OK, BUILD SUCCESS (59 nouveaux).

LOOP 3.14 : ✅ Terminé
Résumé : Implémentation complète du module Équipe (version stricte, LOOP exécuté avant le LOOP 3.13 Chantier par décision utilisateur). Aucune Entity ajoutée/modifiée, aucun attribut hors dictionnaire, aucune relation/cardinalité modifiée, aucune entité ChefEquipe/ResponsableEquipe (le chef d'équipe est uniquement MembreEquipe.roleDansEquipe = CHEF). Créés : equipe/service/EquipeService(Impl) (creer, modifier, activer, desactiver, trouverParId, listerActives), MembreEquipeService(Impl) (integrer, changerRole, retirer, listerMembres, listerEquipesDeUtilisateur), AffectationEquipeChantierService(Impl) (affecter, terminer, listerEquipesDuChantier, listerChantiersDeEquipe) + equipe/controller/EquipeController (/api/v1/equipes, 14 endpoints). DTO alignés strictement : CreateEquipeRequest réduit à nom+description (champ actif retiré), EquipeResponse réduit à id+nom+description+actif (dates techniques retirées). Règles métier validées appliquées : équipe créée active, désactivation logique, unicité membre (utilisateur,equipe) → 409, dates cohérentes affectation (dateFin ≥ dateDebut), une affectation ACTIVE unique par (equipe,chantier) → 409, terminer() → TERMINEE. RBAC @PreAuthorize sur tous les endpoints (EQUIPE_CREER/LIRE/MODIFIER/SUPPRIMER référencées, aucune permission créée). Tests : EquipeControllerTest (6, dont les 4 cas obligatoires : création admin 201, ajout membre 201, rôle CHEF/OUVRIER 200, 403 sans permission), EquipeServiceImplTest (8), MembreEquipeServiceImplTest (8), AffectationEquipeChantierServiceImplTest (11) — 124/124 tests OK, BUILD SUCCESS. Livrable docs/module-equipe.md. Points signalés non implémentés (à valider LOOP ultérieur) : sécurité par données Niveau 2 (§195) et règle nom unique équipe (§2.6).

LOOP 3.12 : ✅ Terminé
Résumé : Finalisation du module Profil/Permission — RBAC administrable (Services/Controllers existants de LOOP 3.9/3.10 vérifiés et complétés, aucune Entity modifiée, aucune table créée). Ajouté : PermissionService.activerPermission + endpoint PATCH /api/v1/permissions/{id}/activer (référentiel permanent, ré-attribution requise — la désactivation purge les octrois). Préparation de l'audit : common/constants/AuditEvents (CREATION_PROFIL, MODIFICATION_PROFIL, ATTRIBUTION_PERMISSION, RETRAIT_PERMISSION, ACCORD_PERMISSION, REFUS_PERMISSION, ...) référencés dans les journaux des services (AUDIT|<code>|...). Droits effectifs confirmés : (permissions profil) + ACCORDER − REFUSER, REFUSER prioritaire, relecture PostgreSQL à chaque requête, aucune permission dans le JWT, aucun rôle en dur, @PreAuthorize partout (aucun if(admin)). Tests : RbacAdministrationTest (5 cas obligatoires : création profil, attribution permission, héritage profil, ACCORDER ajoute, REFUSER retire même si profil autorisé) + 2 tests activerPermission — 91/91 tests OK, BUILD SUCCESS. Livrable docs/rbac-administration.md.

LOOP 3.11 : ✅ Terminé
Résumé : Gestion globale des exceptions, validation API et format d'erreur professionnel (aucune Entity / relation JPA / module métier / logique RBAC modifiée). Créés : exception/custom/BusinessException (400, statut ajustable) + DuplicateResourceException (409, fabrique pourConflit(champ,valeur)) héritant de CmsException ; common/response/ErrorResponse ({ success, message, code, timestamp, errors[] }, @JsonInclude NON_NULL, fabriques of(code,message) et of(code,message,errors)). GlobalExceptionHandler réécrit : CmsException → statut HTTP de l'exception + code dérivé du nom (EmailDejaUtiliseException → EMAIL_DEJA_UTILISE) + logs WARN/ERROR ; MethodArgumentNotValidException + ConstraintViolationException → 400 VALIDATION_ERROR avec détail champ->message ; HttpMessageNotReadableException → 400 REQUETE_INVALIDE ; AccessDeniedException → 403 ACCES_REFUSE ; AuthenticationException → 401 AUTHENTIFICATION_REQUISE ; Exception → 500 ERREUR_INTERNE (message générique, stack trace uniquement en log, jamais de mot de passe/token). Handlers de filtres alignés sur ErrorResponse : UnauthorizedHandler (401) + AccessDeniedHandlerImpl (403). Tests : GlobalExceptionHandlerTest (6 cas : 409 email doublon, 404 utilisateur inexistant, 400 validation DTO avec errors.nom/errors.email, 403 sans permission, 500 simulé masqué, 401 identifiants) — 84/84 tests OK, BUILD SUCCESS. Livrable docs/gestion-erreurs-api.md.

LOOP 3.10 : ✅ Terminé
Résumé : Couche REST créée pour les modules dont les Services existent (décision utilisateur) : Auth, Utilisateur, Profil, Permission. Base de routes /api/v1 (convention projet, décision utilisateur). Créés : auth/dto/LoginRequest + TokenResponse, auth/service/AuthenticationService(Impl) (inscription via UtilisateurService ; connexion via AuthenticationManager + JwtService, 401 UnauthorizedException sur identifiants incorrects ou compte désactivé), auth/controller/AuthController (POST /api/v1/auth/register et /api/v1/auth/login, routes publiques), utilisateur/controller/UtilisateurController (CRUD + permissions individuelles accorder/refuser/retirer/lister), profil/controller/ProfilController (CRUD + activer/desactiver + attribution permissions), permission/controller/PermissionController (CRUD + code/module/desactiver). Sécurité : @PreAuthorize("hasPermission('MODULE','ACTION')") sur chaque endpoint protégé (RBAC dynamique). Format : ApiResponse<T>, pagination PageResponse<T> (méthode statique from(Page), SearchRequest via @ModelAttribute). Swagger : @Tag/@Operation/@ApiResponses. Modifs annexes : SecurityConfig (/api/v1/auth/** permitAll), ApiRoutes (PROFILS, PERMISSIONS), GlobalExceptionHandler (AccessDeniedException → 403). Tests : AuthenticationServiceImplTest (3), AuthControllerTest (3), UtilisateurControllerTest (4, dont 401 sans token et 403 sans permission), ProfilControllerTest (2), PermissionControllerTest (2) — 78/78 tests OK, BUILD SUCCESS. Aucune logique métier / aucun accès Repository / aucune Entity exposée / aucune migration Flyway. Controllers Chantier/Equipe/Tache/Document/Audit reportés (Services absents). Livrable docs/controllers-rest-api.md.

LOOP 3.9 : ✅ Terminé
Résumé : Implémentation complète du RBAC dynamique (aucun Controller, aucune migration Flyway, aucune modification des Entity). Créés : permission/service/PermissionService(Impl) (8 méthodes : creer/modifier/supprimer/desactiver + trouverParId/trouverParCode/listerParModule/rechercher ; code unique, normalisation MODULE_ACTION en MAJUSCULES, purge des octrois à la suppression/désactivation) ; profil/service/ProfilService(Impl) (10 méthodes, profils système ADMINISTRATEUR/UTILISATEUR_STANDARD protégés : non supprimables/renommables/désactivables, nom unique, ajouter/retirer/lister permissions) ; UtilisateurService étendu (accorderPermission/refuserPermission/retirerPermissionIndividuelle/listerPermissionsIndividuelles, trace createdBy via CurrentUserService, resoudreProfil refuse les profils désactivés). Moteur des droits effectifs security/service/DroitsService (formule officielle : (permissions profil) + ACCORDER − REFUSER, REFUSER prioritaire ; ADMINISTRATEUR = toutes les permissions du référentiel relues en base ; aucune permission dans le JWT, aucun cache, relecture PostgreSQL à chaque requête). Expositions : PermissionEvaluatorImpl (hasPermission(module,action) → module.toUpperCase()+"_"+action.toUpperCase()), SpringPermissionEvaluator (adaptateur Spring Security) + MethodSecurityConfig (handler @PreAuthorize("hasPermission('TACHE','MODIFIER')")). 6 nouvelles exceptions CmsException (PermissionIntrouvable 404, CodePermissionDejaExistant 409, ProfilIntrouvable 404, NomProfilDejaExistant 409, ProfilSystemeProtege 409, PermissionDejaAttribuee 409). Repos complétés (existsByCodePermission, findByProfilIdAndPermissionId, findByPermissionId, findByActifTrue). Tests : DroitsServiceTest (5), PermissionEvaluatorImplTest (4), SpringPermissionEvaluatorTest (2), PermissionServiceImplTest (8), ProfilServiceImplTest (13), UtilisateurServiceImplTest (15) — 64/64 tests OK, BUILD SUCCESS. Sécurité par périmètre Niveau 2 (chantier) laissée aux Services (CurrentUserService), NON implémentée. Livrable docs/rbac-dynamique.md.

LOOP 3.8 : ✅ Terminé
Résumé : Socle d'authentification Spring Security + JWT (aucun Controller, aucun endpoint, aucune migration, aucun accès chantier). Créés dans com.cms.security : jwt/JwtService (génération/validation/lecture, HS256 via Keys.hmacShaKeyFor, token limité à sub (= userId) + userId + iat + exp, expiration app.security.jwt.expiration-ms, erreur claire si secret absent) ; service/PrincipalUtilisateur (UserDetails enveloppant Utilisateur, autorités vides — RBAC LOOP 3.9) ; service/CustomUserDetailsService (loadUserByUsername(email) + loadUserById(id), compte actif obligatoire → DisabledException) ; service/CurrentUserService (utilisateur courant du SecurityContext, utilisé pour le périmètre Niveau 2, jamais paramètre frontend) ; filter/JwtAuthenticationFilter (OncePerRequestFilter : header Authorization "Bearer ", validation signature+expiration, rechargement base, popule SecurityContext, échec → aucune auth → 401) ; handler/UnauthorizedHandler (401 ApiResponse) + AccessDeniedHandlerImpl (403 ApiResponse) ; permission/PermissionEvaluator (interface contrat hasPermission(module,action)/hasPermission(code)) ; annotation/@RequirePermission (module+action). config/SecurityConfig remplacé (permitAll → CSRF off, CORS, STATELESS, /api/auth/** + Swagger permitAll, /api/** authenticated, @EnableMethodSecurity, beans AuthenticationProvider DaoAuthenticationProvider (UserDetailsService + BCrypt) + AuthenticationManager). Secrets : app.security.jwt.secret ajouté en dev et test (fallback JWT_SECRET). Tests : JwtServiceTest (7) + JwtSecurityTest (5 : succès token + requête authentifiée, mauvais password → refus, compte désactivé → refus, token invalide → aucune auth, sans header) — 28/28 tests OK, BUILD SUCCESS. Aucune permission codée en dur, aucun Controller métier, aucune migration Flyway, aucun accès chantier implémenté. Livrable docs/security-jwt.md.

LOOP 3.7 : ✅ Terminé
Résumé : Implémentation du module Utilisateur (Service + inscription + gestion, aucun Controller/JWT/Flyway). Créés : UtilisateurService (interface, 8 méthodes) + UtilisateurServiceImpl (@Service, injection par constructeur) ; exceptions métier UtilisateurNonTrouveException (404) et EmailDejaUtiliseException (409) ; PasswordEncoder BCrypt (bean BeanConfig) ; constante SystemRoles.UTILISATEUR_STANDARD ajoutée. Règles métier : email unique, mot de passe BCrypt (jamais en clair), profil par défaut UTILISATEUR_STANDARD si non fourni, compte créé actif, désactivation logique (actif=false), rôle/profil non modifiable via modifierUtilisateur, email re-vérifié à la modification, exceptions CmsException (jamais de null). Transactions : @Transactional en écriture, @Transactional(readOnly=true) en lecture. Tests unitaires Mockito (UtilisateurServiceImplTest, 11 tests : création OK, doublon refusé, password encodé vérifié BCrypt.matches, profil attribué, profil par défaut, modification sans changement de rôle, désactivation, exceptions) — 16/16 tests OK, BUILD SUCCESS. Aucune modification des Entity existantes, aucun RBAC contourné. Livrable docs/module-utilisateur.md.

LOOP 3.6 : ✅ Terminé
Résumé : Architecture de la couche Service définie et FIGÉE (conception uniquement, aucun code implémenté). Livrable docs/architecture-services.md. 18 Services planifiés (16 entités métier + associations) organisés par module (utilisateur : UtilisateurService + AuthenticationService ; profil : ProfilService ; permission : PermissionService ; chantier : ChantierService ; equipe : EquipeService, MembreEquipeService, AffectationEquipeChantierService ; tache : TacheService, PlanningService, ValidationTacheService, CommentaireService, AffectationTacheService ; document : DocumentService, PieceJointeService, PhotoChantierService ; audit : HistoriqueActionService, JournalConnexionService). Responsabilités par service documentées (rôle, opérations, règles métier, dépendances autorisées). Règles de dépendance figées : Controller→Service uniquement, Service→Repository/Service public autre module, interdits (Repository→Repository, Controller→Repository, Entity→Service). Stratégie @Transactional (readOnly=true en lecture, transaction sur la méthode publique, rollback sur CmsException, pas de @Transactional privé). Stratégie logs (INFO/WARN/ERROR + événements à journaliser + canal HistoriqueAction). Contrats des 18 Services listés (signatures publiques uniquement, retours DTO, jamais d'Entity exposée). Règles cross-cutting (interface+implémentation @Service, injection par constructeur, mapping MapStruct, exceptions CmsException, périmètre RBAC Niveau 2 en implémentation, immuabilité audit). Rien d'implémenté : aucun Controller, aucune logique métier, aucun JWT, aucun Flyway, aucun code de sécurité.

LOOP 3.5 : ✅ Terminé
Résumé : Création des DTO (Request / Response) et des Mapper MapStruct pour les 19 entités (aucun service/controller/security/migration créé). Conventions de nommage imposées par le LOOP 3.5 : CreateXXXRequest / UpdateXXXRequest / XXXResponse / XXXResumeResponse (4 DTO par entité, sauf HistoriqueAction et JournalConnexion — audit immuable, sans Update). Total : 74 DTO répartis dans les 8 modules (utilisateur 6, profil 8, permission 4, chantier 4, equipe 12, tache 20, document 12, audit 6) + 19 mappers MapStruct (@Mapper componentModel spring, relations imbriquées → ids, champs techniques id/dates/collections ignorés dans Create/Update). Contrat API uniforme préparé : ApiResponse<T> (existant) + PageResponse<T> (content/page/size/totalElements/totalPages/first/last) + SearchRequest (page/size/sort/direction/motCle). Validation : Bean Validation sur tous les Request (@NotBlank/@NotNull/@Email/@Size/@Pattern/@Positive/@Digits/@Min/@Max), gestion globale différée. Réponses jamais sensibles (password absent des UtilisateurResponse). Vérifications : mvn compile BUILD SUCCESS, génération 19 Mapper*Impl, tests 5/5 OK (MapperInjectionTest 19/19 injectés), aucun Controller/Service/SQL. Livrable docs/dto-mapper.md.

LOOP 3.4 : ✅ Terminé
Résumé : Création des 19 interfaces Repository JPA (une par entité, clé Long) réparties dans les modules (utilisateur 2, profil 2, permission 1, chantier 2 dont AffectationEquipeChantier, equipe 2, tache 5, document 3, audit 2). Méthodes dérivées Spring Data uniquement (aucune @Query complexe) : findByEmail/existsByEmail/findByActifTrue (Utilisateur), findByNom (Profil), findByCodePermission/findByModule (Permission), findByStatut (paginé)/findByResponsableId (Chantier), findByChantierId (paginé)/findByStatus/findByPriorite (Tache), plus recherches de périmètre (affectations, membres, validations, commentaires, documents, photos, historiques par utilisateur/chantier/tâche). Retours Optional pour mono-résultat, List/Page sinon ; pagination préparée (Pageable). Aucune logique métier, aucune annotation @Repository (superflue). Vérifications : compile BUILD SUCCESS, contexte Spring OK, injection des 19 Repository validée par RepositoryInjectionTest, tests 4/4 OK, aucune erreur JPA. Livrable docs/repositories-jpa.md.

LOOP 3.3 : ✅ Terminé
Résumé : Création des 19 Entity JPA à partir du MLD validé (aucun repository/service/controller, aucune table/migration créée). 19 entités réparties dans les 8 modules (utilisateur : Utilisateur, UtilisateurPermission ; profil : Profil, ProfilPermission ; permission : Permission ; chantier : Chantier ; equipe : Equipe, MembreEquipe, AffectationEquipeChantier ; tache : Tache, AffectationTache, ValidationTache, Planning, Commentaire ; document : Document, PieceJointe, PhotoChantier ; audit : HistoriqueAction, JournalConnexion) + 16 enums métier (statuts, types, rôles conformes au MLD). Mappage : @Table/@JoinColumn snake_case alignés MLD, contraintes UNIQUE (email, profil.nom, code_permission, profil_permission, utilisateur_permission, membre_equipe), relations @ManyToOne/@OneToMany(mappedBy) conformes au MCD. Décisions : JournalConnexion.dateConnexion (renommage date → date_connexion, mot réservé SQL validé LOOP 2.7 §6.4) ; PieceJointe garde document_id + entite_type/entite_id (résolution MPD documentée) ; AffectationTache et Commentaire (au moins une cible/référence) contrôlés en service (CHECK au MPD). Vérifications : mvn compile BUILD SUCCESS, contexte Spring chargé (Hibernate métamodèle 19 entités OK) et tests 3/3 OK. PostgreSQL relancé (Docker Desktop + conteneur chantier-db). Correction : DocumentType.java package corrigé (document.entity).

LOOP 3.2 : ✅ Terminé
Résumé : Initialisation technique Spring Boot (configuration uniquement, aucun code métier). Vérifications : Java 25.0.2 / Spring Boot 3.5.3 / Maven 3.9.11 / PostgreSQL 16.14 (conteneur chantier-db) validés, package com.cms confirmé, mvn compile BUILD SUCCESS + tests 3/3. Ajouts LOOP 3.2 : (1) dépendance JJWT 0.12.6 (api/impl/jackson) avec choix documenté dans pom.xml ; (2) config CORS — config/CorsConfig.java (bean CorsConfigurationSource, origines via app.cors.allowed-origins : dev http://localhost:3000, prod CORS_ALLOWED_ORIGINS, méthodes GET/POST/PUT/PATCH/DELETE/OPTIONS, headers Authorization/Content-Type/Accept, credentials) activé dans SecurityConfig via http.cors() ; (3) application.yml + application-prod.yml enrichis (app.cors.allowed-origins, CORS_ALLOWED_ORIGINS en prod). JWT déjà configuré (secret JWT_SECRET, expiration 1h, refresh 7j). Flyway V1__init.sql existant ; structure future V2 tables / V3 security / V4 constraints documentée, aucun SQL créé. Modules transverses config/common/exception prêts ; modules métier vides en attente des entités. Nettoyage modules vides obsolètes (rbac, planning) différé. Livrable docs/configuration-projet-spring.md.

LOOP 3.1 : ✅ Terminé
Résumé : Architecture technique backend Spring Boot définie et FIGÉE (documentation uniquement, aucun code/Entity/Controller/table créé). Livrable docs/architecture-backend-spring.md. Décisions : (1) package racine com.cms CONSERVÉ (validé Phase 0/1, compilé, aligné pom.xml/tests) — le com.projet.chantier du modèle LOOP 3.1 n'est pas retenu ; (2) architecture par domaine adaptée : 8 modules métier couvrant les 19 entités (utilisateur, profil, permission, chantier, equipe, tache, document, audit) + 4 transverses (config, security, common, exception) ; (3) correspondance entités→modules documentée (19/19) ; (4) RBAC technique : JwtService, JwtAuthenticationFilter, CustomUserDetailsService (droits effectifs = profil + ACCORDER − REFUSER, relecture base à chaque requête, token sans permissions), SecurityConfig, PermissionEvaluator (Niveau 1), contrôle de périmètre dans les Services (Niveau 2) ; (5) erreurs : hiérarchie CmsException existante + ValidationException, GlobalExceptionHandler → ApiResponse uniforme ; (6) configuration : PostgreSQL/JPA/Flyway/logs déjà en place, JWT à ajouter (LOOP 3.2, secret via JWT_SECRET) ; (7) conventions nommage et ordre de développement 1→9 documentés. Nettoyage modules vides obsolètes (rbac, planning) reporté au LOOP 3.2.

LOOP 2.7 : ✅ Terminé
Résumé : Validation finale du modèle de données avant développement (audit de cohérence, aucun code/table créé). Audit complet : entités 19/19 (dictionnaire v1.2 = MCD = MLD, correspondance 100%), attributs 19/19 (contrôles particuliers Utilisateur/Permission/UtilisateurPermission/Tache, aucun attribut inventé), RBAC (héritage + ACCORDER/REFUSER, REFUSER prioritaire), sécurité par périmètre (UTILISATEUR_STANDARD limité), intégrité relationnelle (tache.chantier_id obligatoire, affectations contrôlées, au moins une cible affectation_tache), normalisation 1NF/2NF/3NF respectée, audit/traçabilité (HistoriqueAction immuable, JournalConnexion), 8 modules futurs confirmés hors CORE (Client, Fournisseur, Stock, Materiel, Dépense, Facturation, Notification, tacheParente). Verdict : ✅ MODÈLE VALIDÉ POUR DÉVELOPPEMENT. Anomalies non bloquantes : cardinalites-merise.md #6 responsable (0,1) et #14 Document (1,1) à corriger (décisions concepteur déjà appliquées au MCD/MLD), piece_jointe (rattachement Document vs polymorphique) à trancher au MPD, journal_connexion.date mot réservé SQL à renommer au MPD. Livrable docs/validation-modele-donnees.md v1.1. PHASE MERISE TERMINÉE, PASSAGE PHASE TECHNIQUE SPRING BOOT.

LOOP 2.6 : ✅ Terminé
Résumé : Passage MCD vers MLD relationnel (logique, aucun SQL/table/Entity créé). Livrable docs/MLD-relationnel.md : schéma global des 19 tables (snake_case), détail de chaque table (PK id, colonnes avec type logique et obligatoire, FKs, contraintes), tableau des 31 relations entre tables, contraintes métier (RBAC, sécurité périmètre, responsable chantier 0..1, affectation tâche au moins une cible, commentaire un seul contexte, traçabilité immuable). Transformations Merise appliquées : entités → tables, relations 1,N → FK côté N, relations N,N → tables associatives (profil_permission, utilisateur_permission, membre_equipe, affectation_equipe_chantier). Responsable chantier → FK responsable_id (0..1). Points de vigilance MPD : piece_jointe (document_id MCD vs polymorphique dictionnaire), document.chantier_id (1,1), CHECK d'alternance, types énumérés.

LOOP 2.5 : ✅ Terminé
Résumé : Construction du MCD final (conceptuel, aucun code/table). Livrable docs/MCD-final.md : diagramme Mermaid + ASCII par module, liste des 19 entités avec rôle, tableau des 20 associations (entités, cardinalités, règles), associations portées par attributs (createdBy, uploader, auteur, validateur, createdBy exception), contraintes métier (RBAC dynamique, sécurité par périmètre, responsable de chantier 0,1 avec obligation si actif, affectation de tâche avec au moins une cible, commentaire à contexte unique, traçabilité immuable), profils système. Cardinalités du concepteur appliquées : Responsable Chantier (0,1), Document (1,1) rattaché au chantier, Document—PieceJointe (1,1)-(0,N) réintroduite. 3 points de vigilance pour le MLD (PieceJointe polymorphique vs relation, Chantier-Document obligatoire, Tache-Planning optionnelle). 19 entités présentes, aucune inventée/supprimée, RBAC conservé.

LOOP 2.4 : ✅ Terminé
Résumé : Définition des cardinalités Merise des 18 relations (conceptuel, aucun code/table). Livrable docs/cardinalites-merise.md : tableau global des cardinalités + justification métier par relation + 6 cardinalités complémentaires + 5 points à valider. Décisions : Utilisateur (0,N)-(1,1) Profil ; Profil/Permission et Utilisateur/Permission en (0,N)/(0,N) via tables de liaison ; Equipe/Chantier (0,N)/(0,N) via AffectationEquipeChantier ; Chantier (1,1)-(0,N) Tache, Planning, PhotoChantier ; Tache (1,1)-(0,N) AffectationTache et ValidationTache ; AffectationTache cible Utilisateur (0,1) ou Equipe (0,1) avec au moins une cible ; Commentaire (0,1) côté Tache/Chantier et (0,N) côté Commentaire ; Document (0,1) côté Chantier ; HistoriqueAction et JournalConnexion (1,1) côté Utilisateur. Relation Document-PieceJointe NON RETENUE. Points à valider : nb de responsables, multi-équipes simultanées, validations multiples, compte technique, alternance cible AffectationTache.

LOOP 2.3 : ✅ Terminé
Résumé : Définition des relations métier entre les 19 entités validées (conceptuel, aucun code/cardinalité). Livrable docs/relations-entites.md : 16 associations analysées au format imposé (entités, sens métier, règles) — RBAC (Utilisateur-Profil, Profil-Permission via ProfilPermission, Utilisateur-Permission via UtilisateurPermission ACCORDER/REFUSER), Organisation (Chantier-Tache, Utilisateur-Equipe via MembreEquipe, Equipe-Chantier via AffectationEquipeChantier, Utilisateur responsable Chantier sans attribut), Tâches (AffectationTache utilisateur ou équipe, ValidationTache, Commentaire, Planning), Documents (Document, PieceJointe NON retenue sur Document car polymorphique TACHE/COMMENTAIRE/VALIDATION, PhotoChantier), Traçabilité (HistoriqueAction, JournalConnexion). Relations complémentaires listées (createdBy, uploader, auteur, validateur, Tache-Planning, Commentaire-Chantier). 6 points nécessitant validation (multi-équipes, nb de responsables, auto-validation, chef unique, compte technique, durée de conservation). Cardinalités Merise laissées au LOOP 2.4.

LOOP 2.2.3 : ✅ Terminé
Résumé : Précisions finales du dictionnaire (conceptuel, aucun code/table créé). Livrable : docs/dictionnaire-donnees-valide.md v1.2 + Historique des corrections v1.2. Deux précisions appliquées : (1) section « Profils système initiaux » détaillée — ADMINISTRATEUR (gestion complète, profils, permissions, chantiers, équipes) et UTILISATEUR_STANDARD (accès limité, ne peut pas consulter les données globales) ; (2) Chantier.budget conservé et précisé (estimation financière indicative, pas un module financier complet ; dépense/facture/paiement/fournisseur/comptabilité hors CORE). Aucune entité ajoutée/supprimée, RBAC inchangé. Dictionnaire prêt pour la validation finale et le LOOP 2.3.

LOOP 2.2.2 : ✅ Terminé
Résumé : Correction des anomalies du dictionnaire avant validation finale (conceptuel, aucun code/table créé). Livrable mis à jour : docs/dictionnaire-donnees-valide.md v1.1 + section Historique des corrections. 8 corrections appliquées : (1) Chantier.responsable retiré, relation prévue au LOOP 2.3 (UTILISATEUR — responsable de — CHANTIER) ; (2) Client confirmé hors CORE (module futur NON VALIDÉ) ; (3) formule droits effectifs RBAC ajoutée (Profil + ACCORDER − REFUSER, REFUSER prioritaire) ; (4) sécurité par périmètre en 2 niveaux explicites (fonctionnelle + restriction données, « une permission ne donne jamais accès à toutes les données ») ; (5) Tache.progression et Chantier.progression conservées + remarque mode de calcul (manuel ou auto) ; (6) AffectationTache : utilisateur (N) + equipe (N), au moins une cible obligatoire ; (7) règle transversale attributs techniques (id/dateCreation/dateModification génériques, createdBy/modifiedBy seulement si justification) ; (8) modules futurs regroupés hors CORE (Client, Materiel, Stock, Budget avancé, Depense, Fournisseur, Notification). 19 entités inchangées. MASTER_PLAN et dictionnaire v1.1 à jour.

LOOP 2.2.1 : ✅ Terminé
Résumé : Validation finale du dictionnaire des données (conceptuel, aucun code/table créé). Livrable officiel docs/dictionnaire-donnees-valide.md (version 1.0, référence unique pour MCD/MLD/MPD/Entity/Flyway). Règles de gouvernance instaurées (proposition→justification→validation concepteur→intégration). Corrections obligatoires appliquées : suppression de l'entité Client (hors périmètre CORE), retrait de Notification et Materiel (hors liste validée), retrait de Tache.tacheParente et Chantier.budget (en attente). 19 entités validées (Sécurité : Utilisateur, Profil, Permission, ProfilPermission, UtilisateurPermission ; Organisation : Chantier, Equipe, MembreEquipe, AffectationEquipeChantier ; Travail : Tache, AffectationTache, ValidationTache, Planning, Commentaire ; Traçabilité : HistoriqueAction, JournalConnexion ; Document : Document, PieceJointe, PhotoChantier). RBAC officiel figé (ProfilPermission + UtilisateurPermission type ACCORDER/REFUSER). Profils initiaux système : ADMINISTRATEUR, UTILISATEUR_STANDARD. Sécurité par périmètre : fonctionnelle (permissions) + données (AffectationEquipeChantier, MembreEquipe, AffectationTache). Attributs concepteur conservés, ajouts validés intégrés, rejets et en attente listés. Docs 2.1/2.2 marqués historiques.

LOOP 2.2 : ✅ Terminé
Résumé : Définition précise des attributs de toutes les entités (conceptuel, aucun code/table créé). Livrable docs/dictionnaire-donnees.md : dictionnaire complet (23 entités, attributs avec rôle, type indicatif, caractère obligatoire, validation). Attributs du concepteur conservés (Utilisateur : id, nom, prenom, email, password, dateCreation, dateModification ; Profil : id, nom ; Permission : id, nom ; UtilisateurPermission : id, utilisateur, permission ; Tache : id, titre, description, priorite, status, dateDebut, dateFin ; Equipe : id, nom, dateCreation, dateModification ; Chantier : minimum attendu complet ; associatives id+FK). Fusion décidée : UtilisateurPermissionRefusee → UtilisateurPermission avec champ type (ACCORDER/REFUSER). Client = entité indépendante + lien optionnel vers un compte Utilisateur. AffectationTache = affectation par utilisateur (pas équipe). Attributs ajoutés justifiés (actif, dates techniques, codePermission/module/action, progression, createdBy, roleDansEquipe, role, adresse, budget...). Attributs refusés justifiés (photoProfil, derniereConnexion, compteBloque différé Phase 4, commentaireValidation, responsableEquipe). Questions métier à valider listées. MASTER_PLAN et conception-entites.md mis à jour.

LOOP 2.1 : ✅ Terminé
Résumé : Inventaire complet des entités métier du CMS (étape purement conceptuelle, aucun code/table créé). Livrable docs/conception-entites.md : 23 entités retenues réparties en 6 domaines — Sécurité (Utilisateur, Profil, Permission, ProfilPermission, UtilisateurPermission, UtilisateurPermissionRefusee), Organisation (Client, Chantier, Équipe, MembreEquipe, AffectationEquipeChantier), Travail (Tâche, AffectationTâche, Planning, ValidationTâche, Commentaire), Suivi (HistoriqueAction, JournalConnexion, Notification), Documentaire (Document, PièceJointe, PhotoChantier), Ressources (Matériel). Modèle RBAC 4 niveaux validé (Profil→Permissions, ajout individuel, refus individuel via table séparée). Entités supprimées : Stock, Fournisseur, Dépense, Budget (budget porté par Chantier.budget_previsionnel). Équipe conçue mobile (affectation chantier par période → sécurité par chantier). Chaque entité documentée (description, raison d'être, attributs, relations).

LOOPS :

* 2.1 ✅ Inventaire complet des entités métier (fait — conception-entites.md)
* 2.2 ✅ Dictionnaire des données v1 (fait — dictionnaire-donnees.md)
* 2.2.1 ✅ Validation finale du dictionnaire v1.0 (fait — dictionnaire-donnees-valide.md)
* 2.2.2 ✅ Corrections anomalies dictionnaire v1.1 (fait)
* 2.2.3 ✅ Précisions finales dictionnaire v1.2 (fait — DÉFINITIF)
* 2.3 ✅ Relations métier entre entités (fait — relations-entites.md)
* 2.4 ✅ Cardinalités Merise (fait — cardinalites-merise.md)
* 2.5 ✅ MCD final (fait — MCD-final.md)
* 2.6 ✅ MLD relationnel (fait — MLD-relationnel.md)
* 2.7 ✅ Validation finale du modèle de données (fait — validation-modele-donnees.md)
* 3.1 ✅ Architecture technique Backend Spring Boot (fait — architecture-backend-spring.md)
* 3.2 ✅ Initialisation technique Spring Boot (fait — configuration-projet-spring.md)
* 3.3 ✅ Création des Entity JPA (fait — 19 entités + 16 enums, MLD)
* 3.4 ✅ Création des Repository JPA (fait — 19 interfaces)
* 3.5 ✅ DTO (Request / Response) + Mapper MapStruct (fait — 74 DTO + 19 mappers)
* 3.6 ✅ Architecture de la couche Service (fait — architecture-services.md, 18 services définis)
* 3.7 ✅ Implémentation du module Utilisateur (fait — UtilisateurService/Impl, BCrypt, 11 tests)
* 3.8 ✅ Authentification et sécurité Spring Security + JWT (fait — JwtService, filtre, SecurityConfig, 12 tests)
* 3.9 ✅ RBAC dynamique complet (fait — PermissionService, ProfilService, DroitsService, PermissionEvaluator, droits effectifs profil + ACCORDER − REFUSER, 36 nouveaux tests)
* 3.10 ✅ Controllers REST (fait — Auth, Utilisateur, Profil, Permission ; @PreAuthorize RBAC, 14 nouveaux tests)
* 3.11 ✅ Gestion globale des exceptions, validation API et format d'erreur professionnel (fait — BusinessException, DuplicateResourceException, ErrorResponse, GlobalExceptionHandler réécrit, handlers 401/403 alignés, 6 nouveaux tests)
* 3.12 ✅ Finalisation module Profil/Permission — RBAC administrable (fait — activerPermission, AuditEvents, RbacAdministrationTest 5 cas, 7 nouveaux tests)
* 3.13 À venir — Implémentation complète du module Chantier (Service + Controller + sécurité périmètre)
* 3.14 ✅ Implémentation complète du module Équipe (version stricte, fait — module-equipe.md, 124/124 tests)
* 3.15 ✅ Implémentation complète du module Tâche (fait — Planning/Commentaire/Tache/Affectation/Validation, 193 tests)
* 3.16 ✅ Finalisation des modules Document et Audit (fait — 5 Services + 5 Controllers, CHANTIER_*/UTILISATEUR_*, 252/252 tests)

---

## PHASE 3 — Utilisateur

Etat :

⬜ Non commencé

---

## PHASE 4 — JWT

Etat :

⬜ Non commencé

---

## PHASE 5 — RBAC dynamique

Etat :

⬜ Non commencé

---

## PHASE 6 — Gestion chantier

Etat :

⬜ Non commencé

---

## PHASE 7 — Gestion équipe

Etat :

⬜ Non commencé

---

## PHASE 8 — Gestion tâche

Etat :

⬜ Non commencé

---

## PHASE 9 — Audit historique

Etat :

⬜ Non commencé

---

## PHASE 10 — API finale

Etat :

⬜ Non commencé

---

## PHASE 11 — Tests qualité

Etat :

⬜ Non commencé

---

## PHASE 12 — Déploiement

Etat :

⬜ Non commencé

---

# 7. RÈGLES POUR L'AGENT IA

L'agent ne doit jamais :

❌ supprimer une fonctionnalité sans validation

❌ modifier l'architecture globale seul

❌ créer du code inutile

❌ contourner la sécurité

L'agent doit toujours :

✅ expliquer avant une modification importante

✅ conserver la compatibilité frontend

✅ écrire du code maintenable

✅ tester après modification

✅ mettre à jour ce fichier après chaque LOOP

---

# 8. PROCHAIN LOOP À EXÉCUTER

LOOP actuel :

5.12 — Validation intégration module Template (terminé)

Prochaine mission :

Aucune — module Template complet
(conception 5.1→5.4, implémentation 5.5→5.11, validation 5.12)

---

# 9. MODULE TEMPLATE — CONCEPTION (LOOP 5.x)

Phase de conception du module Template (modèles de chantier réutilisables).
Aucun code, aucune table créée — affichage à l'écran puis livrables docs/.

LOOP 5.1 : ✅ Terminé
Résumé : Analyse métier Template (définition, entités proposées, relations, règles d'import snapshot, impact MCD, points à valider). Affiche à l'écran.

LOOP 5.2 : ✅ Terminé
Résumé : Extension MCD Template (3 entités : template_chantier, template_tache, template_chantier_tache, associations, cardinalités Merise, règles métier, réponses aux 6 questions de conception). Affiche à l'écran.

LOOP 5.3 : ✅ Terminé
Résumé : Dictionnaire de données Template (attributs validés/refusés, règles métier, points à valider dont durée non copiée). Affiche à l'écran.

LOOP 5.4 : ✅ Terminé
Résumé : MLD relationnel du module Template livré (livrable docs/MLD-template.md v1.1). Périmètre exact : 2 concepts métier — TemplateChantier (modèle complet de construction) et TemplateTache (groupe réutilisable de tâches, jamais un chantier). 3 tables logiques créées (template_chantier, template_tache, template_chantier_tache), 0 table CORE modifiée. Association N:N template_chantier ↔ template_tache portant l'ordre d'exécution unique (uk_template_chantier_tache_ordre). 3 scénarios d'import documentés (CAS 1 TemplateChantier→Chantier, CAS 2 TemplateTache→chantier existant, CAS 3 TemplateChantier contient TemplateTache), tous en snapshot (données indépendantes). Enums alignés CORE (statut ACTIF/INACTIF, priorite HAUTE/MOYENNE/BASSE, type_construction MAISON_R1/IMMEUBLE_R5/MAGASIN/VILLA/ENTREPOT). duree_estimee_jours jamais recopiée vers la tâche réelle. RÈGLE ABSOLUE respectée : aucune Entity/attribut/relation/Repository/Service/Controller/DTO/Mapper/fichier Java/migration existante/permission RBAC modifiée ; aucune nouvelle permission créée. Points à valider : PK simple vs composite, nom UNIQUE, created_by NOT NULL, traçabilité CAS 1 non portée (évite toute modification CORE), contenu migration V2.

LOOPS MODULE TEMPLATE :

* 5.1 ✅ Analyse métier
* 5.2 ✅ Extension MCD
* 5.3 ✅ Dictionnaire de données
* 5.4 ✅ MLD (livrable — MLD-template.md)
* 5.5 ✅ Migration Flyway V2 (3 tables)
* 5.6 ✅ Entity/Repository JPA Template
* 5.7 ✅ Service + Controller + RBAC Template
* 5.8 ✅ DTO + Services métier (289 tests)
* 5.9 ✅ Mapper MapStruct (302 tests)
* 5.10 ✅ Controllers REST (324 tests)
* 5.11 ✅ RBAC + sécurité (338 tests — BUILD SUCCESS)
* 5.12 ✅ Validation intégration PostgreSQL (361 tests — BUILD SUCCESS)

LOOP 5.8 : ✅ Terminé
Résumé : DTO créés (6 : CreateTemplateChantierRequest, UpdateTemplateChantierRequest, TemplateChantierResponse, CreateTemplateTacheRequest, UpdateTemplateTacheRequest, TemplateTacheResponse). Services + Impl (TemplateChantierService/Impl, TemplateTacheService/Impl) avec association ordonnée (duplicata → 409 DuplicateResourceException), désactivation TemplateChantier, imports transactionnels (snapshot : statut A_FAIRE, progression 0, ordre, durée non copiée) avec sécurité périmètre. Méthode existsByTemplateChantierIdAndTemplateTacheId ajoutée au repository Template. Décision conservée : TemplateTache sans statut. Tests : 289/289 OK.

LOOP 5.9 : ✅ Terminé
Résumé : DTO résumés (TemplateChantierResumeResponse, TemplateTacheResumeResponse) + Mapper MapStruct (TemplateChantierMapper, TemplateTacheMapper, componentModel=spring, toResponse/toResumeResponse/toEntity/update, champs serveur ignorés). Services refactorés pour utiliser les Mapper (constructeur augmenté). Aucun DTO d'import séparé. Tests : 302/302 OK.

LOOP 5.10 : ✅ Terminé
Résumé : Controllers TemplateChantierController (+ /taches association) et TemplateTacheController ; routes ApiRoutes.TEMPLATE_CHANTIERS/TEMPLATE_TACHES ; imports via @RequestParam chantierId ; aucun DELETE physique ; test réflexif aucunEndpointDesactivationSurTemplateTache. Tests : 324/324 OK.

LOOP 5.11 : ✅ Terminé
Résumé : Migration V3__template_permissions.sql (6 permissions : TEMPLATE_CHANTIER_LIRE/CREER/MODIFIER, TEMPLATE_TACHE_LIRE/CREER/MODIFIER — pas de SUPPRIMER/DESACTIVER/IMPORT). @PreAuthorize hasPermission remplacés dans les 2 Controllers (LIRE sur lectures+imports+tâches associées, MODIFIER sur PUT+PATCH+association/retrait, CREER sur POST). Tests Controllers RBAC 403 + périmètre import hors chantier refusé. Test TemplateRbacTest (héritage profil, ACCORDER, REFUSER, REFUSER prioritaire, ADMINISTRATEUR dynamique). Permissions existantes modifiées = 0, ACCORDER/REFUSER non modifiés, RBAC dynamique conservé, aucune profil_permission insérée pour ADMINISTRATEUR. Tests : 338/338 OK — BUILD SUCCESS. .md créés = 0.

LOOP 5.12 : ✅ Terminé
Résumé : Validation complète du module Template par tests d'intégration PostgreSQL réel (base gestion_de_chantier_test). 3 classes créées : TemplatePersistenceIntegrationTest (16 tests : persistance réelle, contraintes SQL UNIQUE/NOT NULL/CHECK/FK via JdbcTemplate, repository), TemplateImportIntegrationTest (6 tests : CAS 1 import TemplateChantier→Chantier snapshot, CAS 2 TemplateTache→chantier sans FK, CAS 3 modification template après import → chantier inchangé), TemplateImportRollbackIntegrationTest (1 test : rollback transactionnel réel — 1re tache insérée par vraie requête SQL dans la transaction du service, 2e lève une exception → aucune donnée partielle en base). Correction du test FK (approche JdbcTemplate, l'insertion Hibernate interférait avec le contexte de persistance) ; problème @SpyBean + doCallRealMethod sur repository Spring Data (méthode abstraite) contourné par @MockBean + insertion SQL réelle. Problèmes liés à la base de test polluée par le test rollback non transactionnel : nettoyage psql préalable + tearDown avec suivi des ids. ANOMALIE LISTÉE (non corrigée, interdiction de modifier) : TemplateChantierResumeResponse + TemplateChantierMapper.toResumeResponse inutilisés en production (aucun controller/service ne les appelle ; seul TemplateChantierMapperTest les référence). PERF : relations LAZY partout (fetch type LAZY), pas de jointure en fetch — import en boucle save() + accès getTemplateTache() LAZY par association → N+1 potentiel sur templates de grande taille (volume réel faible : quelques tâches par template) — non modifié. Tests : 361/361 OK — BUILD SUCCESS. CORE modifié = 0, permission ajoutée = 0, .md créés = 0.

---

FIN DU MASTER PLAN
