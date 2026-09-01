# RBAC dynamique — Droits effectifs, permissions, profils (LOOP 3.9)

Livrable : LOOP 3.9 (implémentation complète du RBAC dynamique)
Projet : CMS (Chantier Management System)
Version : 1.0

---

# 1. Objectif

Implémenter la couche d'autorisation fonctionnelle du CMS, 100 % dynamique
(chargée depuis PostgreSQL à chaque requête) :

- gestion du référentiel des **permissions** (`PermissionService`) ;
- gestion des **profils** et de leurs permissions par défaut (`ProfilService`) ;
- droits **individuels** d'un utilisateur : ACCORDER / REFUSER
  (`UtilisateurService`) ;
- calcul des **droits effectifs** = (permissions du profil) + (ACCORDER)
  − (REFUSER), avec **REFUSER prioritaire** (`DroitsService`) ;
- exposition à Spring Security via `hasPermission('MODULE','ACTION')`
  (`PermissionEvaluator` → `@PreAuthorize`).

Aucun Controller, aucun endpoint REST, aucune migration Flyway et aucun
contrôle de périmètre chantier (Niveau 2) n'ont été créés dans ce LOOP.

---

# 2. Formule officielle des droits effectifs

```
Droits effectifs =
  (Permissions du Profil)
  + (Permissions ACCORDER)      // exceptions individuelles positives
  - (Permissions REFUSER)       // exceptions individuelles négatives — PRIORITAIRES
```

- Pour le profil système `ADMINISTRATEUR`, les permissions du profil =
  **toutes les permissions du référentiel** (relues en base, jamais en dur).
  Un `REFUSER` individuel reste prioritaire, même pour ce profil.
- Pour tout autre profil : les permissions attribuées via `profil_permission`.

## Priorité des exceptions

| Situation | Résultat |
|-----------|----------|
| Profil accorde + utilisateur ACCORDER | accordé |
| Profil accorde + utilisateur REFUSER | **refusé** |
| Profil ne donne pas + utilisateur ACCORDER | accordé |
| Profil ne donne pas + rien | refusé |

---

# 3. Convention de codage `MODULE_ACTION`

Une permission porte un **code technique unique** au format `MODULE_ACTION`
(MAJUSCULES, underscore). Exemples :

```
TACHE_LIRE
TACHE_CREER
TACHE_MODIFIER
CHANTIER_LIRE
EQUIPE_SUPPRIMER
```

- Le module et l'action sont **normalisés en MAJUSCULES** par les services à la
  création comme à la modification.
- `PermissionEvaluatorImpl.hasPermission(module, action)` construit
  `module.toUpperCase() + "_" + action.toUpperCase()`.

---

# 4. Architecture des composants

```
com.cms
├── permission
│   ├── repository/PermissionRepository          (+ existsByCodePermission)
│   └── service/PermissionService(Impl).java     (référentiel des permissions)
├── profil
│   ├── repository/ProfilRepository              (+ findByActifTrue)
│   ├── repository/ProfilPermissionRepository    (+ findByProfilIdAndPermissionId, findByPermissionId)
│   └── service/ProfilService(Impl).java         (profils + attributions par défaut)
├── utilisateur
│   ├── repository/UtilisateurPermissionRepository  (+ findByPermissionId)
│   └── service/UtilisateurService(Impl).java       (+ accorder/refuser/retirer/lister)
├── security
│   ├── config/MethodSecurityConfig.java         (handler @PreAuthorize hasPermission)
│   ├── permission/
│   │   ├── PermissionEvaluator.java             (contrat LOOP 3.8)
│   │   ├── PermissionEvaluatorImpl.java         (implémentation DroitsService)
│   │   └── SpringPermissionEvaluator.java       (adaptateur Spring Security)
│   ├── service/CurrentUserService.java          (utilisateur courant — LOOP 3.8)
│   └── service/DroitsService.java               (calcul des droits effectifs)
└── exception/custom
    ├── PermissionIntrouvableException (404)
    ├── CodePermissionDejaExistantException (409)
    ├── ProfilIntrouvableException (404)
    ├── NomProfilDejaExistantException (409)
    ├── ProfilSystemeProtegeException (409)
    └── PermissionDejaAttribueeException (409)
```

---

# 5. Référentiel des permissions (`PermissionService`)

Méthodes :

| Méthode | Rôle |
|---------|------|
| `creerPermission(CreatePermissionRequest)` | crée une permission (code unique, module/action MAJUSCULES) |
| `modifierPermission(id, UpdatePermissionRequest)` | modifie nom / code / module / action / description |
| `supprimerPermission(id)` | suppression définitive + nettoyage des octrois (profil_permission, utilisateur_permission) |
| `desactiverPermission(id)` | retire tous les octrois, conserve le référentiel |
| `trouverParId(id)` / `trouverParCode(code)` | recherche mono-résultat |
| `listerParModule(module)` | liste des permissions d'un module |
| `rechercher(SearchRequest)` | recherche paginée (nom, code, module, action) |

Règles :

- le code technique est **unique** (`CodePermissionDejaExistantException` 409) ;
- un code de permission **n'est jamais réutilisé** : la ré-activation passe par
  `desactiverPermission` puis une re-attribution, pas par un code recyclé ;
- la désactivation/suppression **purge les octrois** pour empêcher tout accès
  résiduel.

---

# 6. Profils et permissions par défaut (`ProfilService`)

Méthodes :

| Méthode | Rôle |
|---------|------|
| `creerProfil(CreateProfilRequest)` | crée un profil (nom unique) |
| `modifierProfil(id, UpdateProfilRequest)` | modifie nom / description (sauf profils système) |
| `supprimerProfil(id)` | suppression (interdite pour profils système et profils attribués) |
| `desactiverProfil(id)` / `activerProfil(id)` | activation / désactivation logique |
| `trouverParId(id)` / `listerActifs()` / `rechercher(SearchRequest)` | lectures |
| `ajouterPermission(profilId, permissionId)` | attribue une permission (association unique) |
| `retirerPermission(profilId, permissionId)` | retire une permission |
| `listerPermissions(profilId)` | liste des associations du profil |

Règles :

- **nom unique** (`NomProfilDejaExistantException` 409) ;
- un profil **désactivé ne peut plus être attribué** (`resoudreProfil` refuse) ;
- **profils système protégés** : `ADMINISTRATEUR` et `UTILISATEUR_STANDARD`
  ne sont ni supprimables, ni renommables, ni désactivables
  (`ProfilSystemeProtegeException` 409) ;
- une association profil/permission est **unique** (déjà présente →
  `PermissionDejaAttribueeException` 409) ;
- l'ajout d'une permission à un profil système est inutile pour
  ADMINISTRATEUR (il possède tout), mais reste sans effet bloquant.

---

# 7. Exceptions individuelles (`UtilisateurService`)

Méthodes ajoutées :

| Méthode | Rôle |
|---------|------|
| `accorderPermission(utilisateurId, permissionId)` | exception ACCORDER (si déjà REFUSER → bascule en ACCORDER) |
| `refuserPermission(utilisateurId, permissionId)` | exception REFUSER (si déjà ACCORDER → bascule en REFUSER) |
| `retirerPermissionIndividuelle(utilisateurId, permissionId)` | supprime l'exception individuelle |
| `listerPermissionsIndividuelles(utilisateurId)` | liste les exceptions de l'utilisateur |

Règles :

- l'utilisateur ciblé **doit exister** (`UtilisateurNonTrouveException` 404) ;
- la permission ciblée **doit exister** (`PermissionIntrouvableException` 404) ;
- la contrainte unique `(utilisateur_id, permission_id, type)` est préservée :
  une exception existante du même type est refusée, un type opposé **bascule**
  la ligne existante ;
- chaque exception individuelle est **tracée** (`createdBy` = utilisateur
  connecté via `CurrentUserService`).

---

# 8. Moteur de calcul (`DroitsService`)

```java
@Transactional(readOnly = true)
Set<String> calculerDroits(Long utilisateurId)
```

Algorithmique :

1. charge l'utilisateur (sinon `UtilisateurNonTrouveException`) ;
2. permissions du profil : ADMINISTRATEUR → `permissionRepository.findAll()`
   sinon `profilPermissionRepository.findByProfilId(...)` ;
3. applique chaque exception individuelle : `ACCORDER` → ajout, `REFUSER` →
   retrait (l'ordre de la liste ne change pas le résultat final).

**Aucun cache, aucune permission dans le JWT** : chaque appel relit
PostgreSQL. Une modification d'octroi est donc effective immédiatement, sans
reconnexion (Objectif 8).

---

# 9. Exposition à Spring Security

## 9.1 `PermissionEvaluator` (contrat — LOOP 3.8)

```java
boolean hasPermission(String module, String action);
boolean hasPermission(String codePermission);
```

Implémenté par `PermissionEvaluatorImpl` :

```java
@Override
public boolean hasPermission(String module, String action) {
    return hasPermission(module.toUpperCase() + "_" + action.toUpperCase());
}

@Override
public boolean hasPermission(String codePermission) {
    Long userId = currentUserService.getCurrentUserId();
    return droitsService.calculerDroits(userId).contains(codePermission);
}
```

## 9.2 `SpringPermissionEvaluator` (adaptateur)

Implémente `org.springframework.security.access.PermissionEvaluator` et
traduit l'appel Spring en appel du contrôleur CMS :

```java
@Override
public boolean hasPermission(Authentication authentication,
                             Object targetDomainObject, Object permission) {
    return permissionEvaluator.hasPermission(
        String.valueOf(targetDomainObject),   // module
        String.valueOf(permission));          // action
}
```

## 9.3 `MethodSecurityConfig`

Enregistre l'adaptateur dans `DefaultMethodSecurityExpressionHandler`.
`@EnableMethodSecurity` (déjà actif sur `SecurityConfig`) rend utilisable :

```java
@PreAuthorize("hasPermission('TACHE','MODIFIER')")
```

> Les annotations sont prévues sur les **Controllers** au LOOP 3.10. Les
> Services restent accessibles aux composants internes ; le contrôle
> fonctionnel est appliqué au niveau de l'API.

---

# 10. Exceptions HTTP

| Exception | HTTP | Cas |
|-----------|------|-----|
| `PermissionIntrouvableException` | 404 | permission inexistante |
| `CodePermissionDejaExistantException` | 409 | code technique déjà utilisé |
| `ProfilIntrouvableException` | 404 | profil inexistant |
| `NomProfilDejaExistantException` | 409 | nom de profil déjà utilisé |
| `ProfilSystemeProtegeException` | 409 | suppression / renommage / désactivation d'un profil système |
| `PermissionDejaAttribueeException` | 409 | association profil/permission ou utilisateur/permission déjà présente |

Toutes héritent de `CmsException` → traitées par `GlobalExceptionHandler` au
format `ApiResponse`.

---

# 11. Tests effectués

| Test | Cas couverts | Résultat |
|------|--------------|----------|
| `DroitsServiceTest` (5) | cas 1 ADMINISTRATEUR = toutes les permissions ; cas 2 profil standard = permissions attribuées seulement ; cas 3 ACCORDER ajoute ; cas 4 REFUSER retire ; cas 4bis REFUSER prioritaire même sur ADMINISTRATEUR | ✅ |
| `PermissionEvaluatorImplTest` (4) | hasPermission(module, action) OK / KO, construction `MODULE_ACTION` MAJUSCULES, hasPermission(code) | ✅ |
| `SpringPermissionEvaluatorTest` (2) | traduction (module, action), 4-arg → false | ✅ |
| `PermissionServiceImplTest` (8) | création, code dupliqué refusé, normalisation MAJUSCULES, modification, permission inexistante, désactivation purge octrois, suppression, recherche | ✅ |
| `ProfilServiceImplTest` (13) | création, nom dupliqué refusé, profils système protégés (suppression / désactivation / renommage), suppression profil attribué, désactivation, modification, attribution / doublon / retrait, lectures | ✅ |
| `UtilisateurServiceImplTest` (15) | 11 existants + accorder, doublon ACCORDER refusé, refuser, lister les exceptions individuelles | ✅ |

Total : **64/64 tests OK**, `mvn test` BUILD SUCCESS.

---

# 12. Sécurité par périmètre (Niveau 2 — futur)

Ce LOOP implémente le **RBAC fonctionnel (Niveau 1)** : une permission
détermine *si* une action est autorisée.

Le **Niveau 2** (restriction des données) sera implémenté dans les Services au
moyen de `CurrentUserService` (jamais par un paramètre frontend) :

- Utilisateur → équipes (via `MembreEquipe`) ;
- équipes → chantiers (via `AffectationEquipeChantier`) ;
- périmètre d'une tâche (via son chantier).

Règle immuable : **une permission ne donne jamais accès à toutes les données**
du système pour un `UTILISATEUR_STANDARD`.

---

# 13. Ce qui reste à faire

- **LOOP 3.10** : Controllers (API REST) + endpoints `/api/auth/**`
  (connexion, token) ; application de `@PreAuthorize` sur les Controllers.
- **MPD + migrations Flyway** : tables `profil_permission`,
  `utilisateur_permission`, seed des permissions et des profils système.
- **Niveau 2** : contrôle de périmètre chantier dans les Services.
- Journalisation des connexions et des actions (`HistoriqueAction`).

---

# 14. Vérifications de conformité (LOOP 3.9)

- ✅ aucune permission codée en dur (ADMINISTRATEUR = réferentiel en base) ;
- ✅ droits effectifs = profil + ACCORDER − REFUSER, REFUSER prioritaire ;
- ✅ aucune stratégie de cache : relecture PostgreSQL à chaque requête ;
- ✅ aucun Controller métier créé, aucune migration Flyway ;
- ✅ aucune modification des Entity existantes ;
- ✅ token JWT inchangé (aucune permission dans le token) ;
- ✅ contrôle de périmètre Niveau 2 non implémenté (prévu dans les Services) ;
- ✅ convention `MODULE_ACTION` normalisée en MAJUSCULES.
