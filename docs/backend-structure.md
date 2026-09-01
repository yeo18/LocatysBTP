# Structure Backend — CMS

Version : 0.1
Statut : Documentation (aucun code créé)

---

# 1. Vue d'ensemble

* Langage : Java
* Framework : Spring Boot
* Build : Maven
* Base : PostgreSQL
* Sécurité : Spring Security + JWT
* Migrations : Flyway

---

# 2. Arborescence cible

```
backend/
└── src/main/java/com/cms
    ├── CmsApplication.java
    │
    ├── config/
    ├── security/
    ├── common/
    ├── exception/
    │
    ├── utilisateur/
    ├── chantier/
    ├── equipe/
    ├── tache/
    ├── rbac/
    └── historique/
```

---

# 3. Rôle des packages

## config

Configuration de l'application : beans Spring, configuration CORS, configuration Swagger/OpenAPI, encodage, beans utilitaires.

## security

Tout ce qui concerne l'authentification et l'autorisation :
* filtre JWT ;
* UserDetailsService / UserDetails custom ;
* fournisseur JWT (génération, validation, parse) ;
* configuration Spring Security (SecurityFilterChain) ;
* entrée d'accès refusé / non authentifié.

## common

Éléments transverses réutilisables :
* DTO de réponse standard (succès/erreur) ;
* utilitaires de pagination ;
* constantes communes ;
* annotations communes.

## exception

Gestion centralisée des erreurs :
* exceptions métier personnalisées ;
* `@ControllerAdvice` global (exception handler) ;
* mappage des erreurs vers le format de réponse standard.

## utilisateur

Module métier utilisateur.

## chantier

Module métier chantier.

## equipe

Module métier équipe.

## tache

Module métier tâche.

## rbac

Module RBAC dynamique : profil, permission, profil_permission, utilisateur_permission, gestion et vérification des droits.

## audit

Module audit : journalisation des actions (historique), consultation de l'historique.

---

# 4. Structure interne d'un module métier

Chaque module (utilisateur, chantier, equipe, tache, rbac, audit, materiel, stock, planning, document, notification, dashboard) suit le même pattern :

```
module/
├── entity/
│       Chantier.java
├── repository/
│       ChantierRepository.java
├── service/
│       ChantierService.java
├── controller/
│       ChantierController.java
├── dto/
│       ChantierRequest.java
│       ChantierResponse.java
└── mapper/
        ChantierMapper.java
```

## Rôle de chaque couche

| Couche | Responsabilité |
|--------|----------------|
| entity | Représentation JPA de la table (objet, mapping colonnes, relations) |
| repository | Accès aux données (Spring Data JPA), requêtes dérivées ou @Query |
| service | Logique métier, transactions, règles de sécurité, appels repositories |
| controller | Exposition REST, mapping URL/méthode, délégation au service |
| dto | Objets d'échange API (request/response), indépendants des entities |
| mapper | Conversion entity ↔ DTO |

## Règles de couche

* Les controllers ne contiennent pas de logique métier.
* Les services contiennent toute la logique métier et la sécurité.
* Les entities ne sont jamais exposées directement à l'API (toujours via DTO).
* Les repositories ne sont jamais appelés directement depuis les controllers.

---

# 5. Fichiers transverses prévus

* `CmsApplication.java` — point d'entrée Spring Boot
* `application.yml` — configuration principale (datasource, JWT, Flyway, app)
* `application-dev.yml` — configuration développement
* `application-test.yml` — configuration tests
* `application-prod.yml` — configuration production
* `pom.xml` — dépendances Maven
* `src/main/resources/db/migration/` — scripts Flyway (V1__init.sql, ...)
* `src/test/java/com/cms/` — tests unitaires et d'intégration

---

FIN DU DOCUMENT
