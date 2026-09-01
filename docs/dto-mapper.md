# DTO et Mapper MapStruct — CMS

Version : 1.0
Statut : ✅ DTO + Mapper validés (aucun service/controller/security/migration)
Base : LOOP 3.5
Source : 19 Entity JPA (LOOP 3.3), conventions de nommage du LOOP 3.5

---

# 1. CONVENTIONS DE NOMMAGE (OBJECTIF 8)

Pour chaque entité métier `XXX`, on crée **4 DTO** :

| DTO | Rôle |
|-----|------|
| `CreateXXXRequest` | Payload de création (avec Bean Validation) |
| `UpdateXXXRequest` | Payload de mise à jour (avec Bean Validation) |
| `XXXResponse` | Réponse détaillée (jamais de données sensibles) |
| `XXXResumeResponse` | Réponse résumée (listes, selecteurs, pagination) |

**Exceptions :**
- Entités d'audit immuables (`HistoriqueAction`, `JournalConnexion`) : pas de `UpdateXXXRequest` (aucune mise à jour applicative).

**Mappers** : `XXXMapper` (interface MapStruct, `@Mapper(componentModel = "spring")`).

Emplacement : `com.cms.<module>.dto` et `com.cms.<module>.mapper`.

---

# 2. CONTRAT API UNIFORME (OBJECTIF 5)

Toutes les réponses du backend utiliseront `ApiResponse<T>` :

```
com.cms.common.response.ApiResponse<T>
```

| Champ | Type | Description |
|-------|------|-------------|
| `success` | `boolean` | Succès ou échec de l'opération |
| `message` | `String` | Message (succès ou erreur) |
| `data` | `T` | Données de la réponse |
| `timestamp` | `LocalDateTime` | Horodatage de la réponse |
| `errors` | `List` | (préparé, non encore présent) |

Méthodes statiques : `success(T)`, `success(String, T)`, `error(String)`.

---

# 3. PAGINATION (OBJECTIF 6)

```
com.cms.common.response.PageResponse<T>
```

| Champ | Type | Description |
|-------|------|-------------|
| `content` | `List<T>` | Éléments de la page |
| `page` | `int` | Numéro de page (0-based) |
| `size` | `int` | Taille de la page |
| `totalElements` | `long` | Nombre total d'éléments |
| `totalPages` | `int` | Nombre total de pages |
| `first` | `boolean` | Première page ? |
| `last` | `boolean` | Dernière page ? |

Toutes les listes devront pouvoir être paginées (Source : `Page<T>` Spring Data).

---

# 4. RECHERCHE ET FILTRES (OBJECTIF 7)

```
com.cms.common.dto.SearchRequest
```

| Champ | Type | Défaut | Description |
|-------|------|--------|-------------|
| `page` | `int` | 0 | Numéro de page |
| `size` | `int` | 20 | Taille de page |
| `sort` | `String` | null | Champ de tri |
| `direction` | `String` | "ASC" | Sens du tri (ASC/DESC) |
| `motCle` | `String` | null | Recherche par mot-clé |

Les recherches avancées seront implémentées dans les Services (Specifications).

---

# 5. LISTE DES DTO PAR MODULE

## 5.1 Utilisateur

| DTO | Champs principaux |
|-----|-------------------|
| `CreateUtilisateurRequest` | nom, prenom, email, password, telephone, actif, profilId |
| `UpdateUtilisateurRequest` | nom, prenom, email, telephone, actif, profilId |
| `UtilisateurResponse` | id, nom, prenom, email, telephone, actif, profilId, profilNom, dates |
| `UtilisateurResumeResponse` | id, nom, prenom, email, actif, profilNom |

> ❌ `password` n'apparaît **jamais** dans les Response.

## 5.2 UtilisateurPermission

| DTO | Champs principaux |
|-----|-------------------|
| `CreateUtilisateurPermissionRequest` | utilisateurId, permissionId, type |
| `UpdateUtilisateurPermissionRequest` | permissionId, type |
| `UtilisateurPermissionResponse` | id, utilisateurId, permissionId, permissionCode, type, createdById, dateCreation |
| `UtilisateurPermissionResumeResponse` | id, permissionCode, type |

## 5.3 Profil

| DTO | Champs principaux |
|-----|-------------------|
| `CreateProfilRequest` | nom, description, actif |
| `UpdateProfilRequest` | nom, description, actif |
| `ProfilResponse` | id, nom, description, actif, dates |
| `ProfilResumeResponse` | id, nom, actif |

## 5.4 ProfilPermission

| DTO | Champs principaux |
|-----|-------------------|
| `CreateProfilPermissionRequest` | profilId, permissionId |
| `UpdateProfilPermissionRequest` | permissionId |
| `ProfilPermissionResponse` | id, profilId, profilNom, permissionId, permissionCode, permissionNom |
| `ProfilPermissionResumeResponse` | id, permissionCode |

## 5.5 Permission

| DTO | Champs principaux |
|-----|-------------------|
| `CreatePermissionRequest` | nom, codePermission, module, action, description |
| `UpdatePermissionRequest` | nom, codePermission, module, action, description |
| `PermissionResponse` | id, nom, codePermission, module, action, description |
| `PermissionResumeResponse` | id, nom, codePermission, module, action |

## 5.6 Chantier

| DTO | Champs principaux |
|-----|-------------------|
| `CreateChantierRequest` | nom, description, adresse, budget, statut, dates, responsableId |
| `UpdateChantierRequest` | nom, description, adresse, budget, statut, dates, responsableId |
| `ChantierResponse` | id, nom, description, adresse, budget, statut, dates, progression, responsableId, responsableNom |
| `ChantierResumeResponse` | id, nom, adresse, statut, dates, progression |

## 5.7 Equipe

| DTO | Champs principaux |
|-----|-------------------|
| `CreateEquipeRequest` | nom, description, actif |
| `UpdateEquipeRequest` | nom, description, actif |
| `EquipeResponse` | id, nom, description, actif, dates |
| `EquipeResumeResponse` | id, nom, actif |

## 5.8 MembreEquipe

| DTO | Champs principaux |
|-----|-------------------|
| `CreateMembreEquipeRequest` | utilisateurId, equipeId, roleDansEquipe, dateIntegration |
| `UpdateMembreEquipeRequest` | roleDansEquipe, dateIntegration |
| `MembreEquipeResponse` | id, utilisateurId, utilisateurNom, utilisateurPrenom, equipeId, equipeNom, roleDansEquipe, dateIntegration |
| `MembreEquipeResumeResponse` | id, utilisateurId, utilisateurNom, equipeId, equipeNom, roleDansEquipe |

## 5.9 AffectationEquipeChantier

| DTO | Champs principaux |
|-----|-------------------|
| `CreateAffectationEquipeChantierRequest` | equipeId, chantierId, dateDebut, dateFin, statut |
| `UpdateAffectationEquipeChantierRequest` | dateDebut, dateFin, statut |
| `AffectationEquipeChantierResponse` | id, equipeId, equipeNom, chantierId, chantierNom, dates, statut |
| `AffectationEquipeChantierResumeResponse` | id, equipeNom, chantierNom, dates, statut |

## 5.10 Tache

| DTO | Champs principaux |
|-----|-------------------|
| `CreateTacheRequest` | titre, description, priorite, status, ordre, progression, dates, chantierId, planningId |
| `UpdateTacheRequest` | titre, description, priorite, status, ordre, progression, dates, chantierId, planningId |
| `TacheResponse` | id, titre, description, priorite, status, progression, dates, chantierId, chantierNom, planningId, createdById |
| `TacheResumeResponse` | id, titre, priorite, status, progression, dates, chantierId |

## 5.11 AffectationTache

| DTO | Champs principaux |
|-----|-------------------|
| `CreateAffectationTacheRequest` | tacheId, utilisateurId, equipeId, role, dateAffectation |
| `UpdateAffectationTacheRequest` | utilisateurId, equipeId, role, dateAffectation |
| `AffectationTacheResponse` | id, tacheId, tacheTitre, utilisateurId, utilisateurNom, equipeId, equipeNom, role, dateAffectation |
| `AffectationTacheResumeResponse` | id, tacheId, utilisateurId, equipeId, role, dateAffectation |

> ⚠️ Au moins une cible (utilisateur OU equipe) — règle contrôlée dans le Service (LOOP 3.6).

## 5.12 ValidationTache

| DTO | Champs principaux |
|-----|-------------------|
| `CreateValidationTacheRequest` | tacheId, statut, commentaire, dateValidation |
| `UpdateValidationTacheRequest` | statut, commentaire, dateValidation |
| `ValidationTacheResponse` | id, tacheId, tacheTitre, validateurId, validateurNom, validateurPrenom, statut, commentaire, dateValidation |
| `ValidationTacheResumeResponse` | id, tacheId, statut, dateValidation |

## 5.13 Planning

| DTO | Champs principaux |
|-----|-------------------|
| `CreatePlanningRequest` | chantierId, libelle, type, dateDebut, dateFin, statut |
| `UpdatePlanningRequest` | libelle, type, dateDebut, dateFin, statut |
| `PlanningResponse` | id, chantierId, chantierNom, libelle, type, dates, statut, dateCreation |
| `PlanningResumeResponse` | id, libelle, type, dates, statut |

## 5.14 Commentaire

| DTO | Champs principaux |
|-----|-------------------|
| `CreateCommentaireRequest` | tacheId, chantierId, contenu |
| `UpdateCommentaireRequest` | contenu |
| `CommentaireResponse` | id, auteurId, auteurNom, auteurPrenom, tacheId, chantierId, contenu, dateCreation |
| `CommentaireResumeResponse` | id, auteurNom, contenu, dateCreation |

> ⚠️ Un seul contexte (tache OU chantier) — règle contrôlée dans le Service (LOOP 3.6).

## 5.15 Document

| DTO | Champs principaux |
|-----|-------------------|
| `CreateDocumentRequest` | titre, type, chantierId, cheminFichier, extension, taille, statut |
| `UpdateDocumentRequest` | titre, type, chantierId, cheminFichier, extension, taille, statut |
| `DocumentResponse` | id, titre, type, chantierId, chantierNom, uploaderId, uploaderNom, cheminFichier, extension, taille, statut, dateUpload |
| `DocumentResumeResponse` | id, titre, type, extension, statut, dateUpload |

## 5.16 PieceJointe

| DTO | Champs principaux |
|-----|-------------------|
| `CreatePieceJointeRequest` | documentId, entiteType, entiteId, cheminFichier, nomOriginal, extension, taille |
| `UpdatePieceJointeRequest` | documentId, entiteType, entiteId, cheminFichier, nomOriginal, extension, taille |
| `PieceJointeResponse` | id, documentId, entiteType, entiteId, cheminFichier, nomOriginal, extension, taille, dateAjout |
| `PieceJointeResumeResponse` | id, nomOriginal, extension, taille, entiteType, dateAjout |

## 5.17 PhotoChantier

| DTO | Champs principaux |
|-----|-------------------|
| `CreatePhotoChantierRequest` | chantierId, chemin, description, type, datePrise |
| `UpdatePhotoChantierRequest` | chemin, description, type, datePrise |
| `PhotoChantierResponse` | id, chantierId, chantierNom, utilisateurId, utilisateurNom, chemin, description, type, datePrise |
| `PhotoChantierResumeResponse` | id, chemin, description, type, datePrise |

## 5.18 HistoriqueAction (audit, immuable)

| DTO | Champs principaux |
|-----|-------------------|
| `CreateHistoriqueActionRequest` | utilisateurId, action, typeEntite, entiteId, details, ipAdresse |
| `HistoriqueActionResponse` | id, utilisateurId, utilisateurNom, utilisateurPrenom, action, typeEntite, entiteId, details, ipAdresse, dateAction |
| `HistoriqueActionResumeResponse` | id, action, typeEntite, dateAction |

## 5.19 JournalConnexion (audit, immuable)

| DTO | Champs principaux |
|-----|-------------------|
| `CreateJournalConnexionRequest` | utilisateurId, type, statut, ipAdresse, userAgent |
| `JournalConnexionResponse` | id, utilisateurId, utilisateurNom, utilisateurPrenom, type, statut, ipAdresse, userAgent, dateConnexion |
| `JournalConnexionResumeResponse` | id, utilisateurId, type, statut, dateConnexion |

---

# 6. LISTE DES MAPPER MAPSTRUCT

| Mapper | Entité | Conversion |
|--------|--------|-----------|
| `UtilisateurMapper` | Utilisateur | Response / ResumeResponse / Create→Entité / Update→Entité |
| `UtilisateurPermissionMapper` | UtilisateurPermission | idem |
| `ProfilMapper` | Profil | idem |
| `ProfilPermissionMapper` | ProfilPermission | idem |
| `PermissionMapper` | Permission | idem |
| `ChantierMapper` | Chantier | idem |
| `AffectationEquipeChantierMapper` | AffectationEquipeChantier | idem |
| `EquipeMapper` | Equipe | idem |
| `MembreEquipeMapper` | MembreEquipe | idem |
| `TacheMapper` | Tache | idem |
| `AffectationTacheMapper` | AffectationTache | idem |
| `ValidationTacheMapper` | ValidationTache | idem |
| `PlanningMapper` | Planning | idem |
| `CommentaireMapper` | Commentaire | idem |
| `DocumentMapper` | Document | idem |
| `PieceJointeMapper` | PieceJointe | idem |
| `PhotoChantierMapper` | PhotoChantier | idem |
| `HistoriqueActionMapper` | HistoriqueAction | Response / ResumeResponse / Create→Entité |
| `JournalConnexionMapper` | JournalConnexion | Response / ResumeResponse / Create→Entité |

**Nombre total : 19 mappers.**

---

# 7. STRATÉGIE DE MAPPING (OBJECTIF 4)

## 7.1 Relations imbriquées → identifiants

Les associations `@ManyToOne` ne sont **jamais** exposées en objet imbriqué dans les Response :

```java
@Mapping(source = "profil.id", target = "profilId")
@Mapping(source = "profil.nom", target = "profilNom")
UtilisateurResponse toResponse(Utilisateur entity);
```

## 7.2 Create/Update → Entité

Les relations sont ignorées (résolution de l'entité par `Id` dans le Service) :

```java
@Mapping(target = "profil", ignore = true)
@Mapping(target = "id", ignore = true)
@Mapping(target = "dateCreation", ignore = true)
@Mapping(target = "dateModification", ignore = true)
Utilisateur toEntity(CreateUtilisateurRequest request);
```

Champs techniques gérés par le Service : `id`, `dateCreation`, `dateModification`, collections.

## 7.3 Immuabilité

`HistoriqueAction` et `JournalConnexion` : pas de méthode `update` (tables immuables).

---

# 8. VALIDATION (OBJECTIF 2)

Tous les `CreateXXXRequest` / `UpdateXXXRequest` utilisent la Bean Validation (Jakarta) :

| Annotation | Usage |
|------------|-------|
| `@NotBlank` | Chaînes obligatoires (nom, titre, libellé, code...) |
| `@NotNull` | FK et champs obligatoires non-chaîne |
| `@Email` | Email utilisateur |
| `@Size` | Bornes des chaînes (conformes aux `length` JPA) |
| `@Pattern` | `codePermission` en MAJUSCULES/underscore, téléphone |
| `@Positive` / `@PositiveOrZero` | taille, budget |
| `@Digits` | budget (13 entiers, 2 décimales) |
| `@Min` / `@Max` | progression tâche (0-100) |

La gestion globale des erreurs de validation sera réalisée dans un LOOP ultérieur (GlobalExceptionHandler — déjà préparé en Phase 1).

---

# 9. VÉRIFICATIONS (OBJECTIF CONTRÔLES)

- ✅ aucun Controller créé
- ✅ aucun Service créé
- ✅ aucun JWT créé
- ✅ aucun Flyway / SQL créé
- ✅ aucune logique métier
- ✅ compilation : `mvn compile` BUILD SUCCESS
- ✅ tests : `mvn test` 5/5 OK (dont `MapperInjectionTest` : 19/19 mappers injectés)

---

FIN DE LA DOCUMENTATION
