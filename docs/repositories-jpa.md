# Repository JPA — CMS

Version : 1.0
Statut : ✅ Repository JPA validés (aucun service/controller/dto/mapper/security/migration)
Base : LOOP 3.4
Source : 19 Entity JPA (LOOP 3.3), MLD validé (LOOP 2.6)

---

# 1. LISTE DES REPOSITORY

| Repository | Entité | Rôle |
|------------|--------|------|
| UtilisateurRepository | Utilisateur | Accès comptes utilisateurs |
| UtilisateurPermissionRepository | UtilisateurPermission | Exceptions RBAC individuelles |
| ProfilRepository | Profil | Accès profils (rôles) |
| ProfilPermissionRepository | ProfilPermission | Permissions par défaut d'un profil |
| PermissionRepository | Permission | Accès permissions |
| ChantierRepository | Chantier | Accès chantiers |
| AffectationEquipeChantierRepository | AffectationEquipeChantier | Affectations équipe↔chantier (sécurité périmètre) |
| EquipeRepository | Equipe | Accès équipes |
| MembreEquipeRepository | MembreEquipe | Appartenances utilisateur↔équipe |
| TacheRepository | Tache | Accès tâches |
| AffectationTacheRepository | AffectationTache | Affectations de tâches |
| ValidationTacheRepository | ValidationTache | Validations de tâches |
| PlanningRepository | Planning | Accès plannings |
| CommentaireRepository | Commentaire | Accès commentaires |
| DocumentRepository | Document | Accès documents |
| PieceJointeRepository | PieceJointe | Pièces jointes |
| PhotoChantierRepository | PhotoChantier | Photos de chantiers |
| HistoriqueActionRepository | HistoriqueAction | Journal d'audit des actions |
| JournalConnexionRepository | JournalConnexion | Journal des connexions |

**Nombre total : 19** (toutes les entités JPA, une interface par entité).

Type de clé : `Long` (identifiants `IDENTITY` du MLD).

---

# 2. MÉTHODES PERSONNALISÉES

## UtilisateurRepository
| Méthode | Justification |
|---------|---------------|
| `findByEmail(String)` | Connexion (auth) et recherche par identifiant |
| `existsByEmail(String)` | Contrôle d'unicité à la création |
| `findByActifTrue()` | Liste des comptes actifs |

## UtilisateurPermissionRepository
| Méthode | Justification |
|---------|---------------|
| `findByUtilisateurId(Long)` | Droits effectifs d'un utilisateur (RBAC) |
| `findByUtilisateurIdAndPermissionIdAndType(...)` | Vérifier une exception existante (ACCORDER/REFUSER) |

## ProfilRepository
| Méthode | Justification |
|---------|---------------|
| `findByNom(String)` | Récupération d'un profil par nom (profils système) |
| `existsByNom(String)` | Unicité du nom de profil |

## ProfilPermissionRepository
| Méthode | Justification |
|---------|---------------|
| `findByProfilId(Long)` | Permissions du profil (droits effectifs RBAC) |

## PermissionRepository
| Méthode | Justification |
|---------|---------------|
| `findByCodePermission(String)` | Vérification/chargement par code technique (ex : TACHE_VIEW) |
| `findByModule(String)` | Permissions d'un module (gestion RBAC) |

## ChantierRepository
| Méthode | Justification |
|---------|---------------|
| `findByStatut(ChantierStatut, Pageable)` | Recherche par état, paginée (liste chantiers) |
| `findByResponsableId(Long)` | Chantiers dont l'utilisateur est responsable |

## AffectationEquipeChantierRepository
| Méthode | Justification |
|---------|---------------|
| `findByChantierId(Long)` | Équipes affectées à un chantier (périmètre) |
| `findByEquipeId(Long)` | Chantiers d'une équipe |

## EquipeRepository
| Méthode | Justification |
|---------|---------------|
| `findByActifTrue()` | Liste des équipes actives |

## MembreEquipeRepository
| Méthode | Justification |
|---------|---------------|
| `findByUtilisateurId(Long)` | Équipes d'un utilisateur (périmètre) |
| `findByEquipeId(Long)` | Membres d'une équipe |

## TacheRepository
| Méthode | Justification |
|---------|---------------|
| `findByChantierId(Long, Pageable)` | Tâches d'un chantier, paginée |
| `findByStatus(TacheStatus)` | Tâches par statut |
| `findByPriorite(Priorite)` | Tâches par priorité |

## AffectationTacheRepository
| Méthode | Justification |
|---------|---------------|
| `findByTacheId(Long)` | Affectations d'une tâche |
| `findByUtilisateurId(Long)` | Tâches affectées à un utilisateur (périmètre) |
| `findByEquipeId(Long)` | Tâches affectées à une équipe (périmètre) |

## ValidationTacheRepository
| Méthode | Justification |
|---------|---------------|
| `findByTacheId(Long)` | Historique des validations d'une tâche |

## PlanningRepository
| Méthode | Justification |
|---------|---------------|
| `findByChantierId(Long)` | Plannings d'un chantier |

## CommentaireRepository
| Méthode | Justification |
|---------|---------------|
| `findByTacheId(Long)` | Commentaires d'une tâche |
| `findByChantierId(Long)` | Commentaires d'un chantier |

## DocumentRepository
| Méthode | Justification |
|---------|---------------|
| `findByChantierId(Long)` | Documents d'un chantier |

## PieceJointeRepository
| Méthode | Justification |
|---------|---------------|
| `findByDocumentId(Long)` | Pièces jointes d'un document |

## PhotoChantierRepository
| Méthode | Justification |
|---------|---------------|
| `findByChantierId(Long)` | Photos d'un chantier |

## HistoriqueActionRepository
| Méthode | Justification |
|---------|---------------|
| `findByUtilisateurId(Long)` | Actions d'un utilisateur |
| `findByTypeEntiteAndEntiteId(...)` | Actions sur une entité donnée (contexte) |

## JournalConnexionRepository
| Méthode | Justification |
|---------|---------------|
| `findByUtilisateurId(Long)` | Connexions d'un utilisateur |

---

# 3. BONNES PRATIQUES

## Pagination
* Les listes potentiellement volumineuses sont paginées : `Page<T>` +
  `Pageable` (chantiers par statut, tâches par chantier).
* Le détri/page vient du Controller (Service), pas du Repository.

## Optional
* Retours mono-résultat en `Optional<T>` (`findByEmail`, `findByNom`,
  `findByCodePermission`, exception RBAC) → évite les `null`, explicite
  l'absence.

## Requêtes dérivées Spring Data
* Toutes les méthodes sont des requêtes dérivées (déduction du nom) :
  aucune requête `@Query` complexe, conformément à la consigne.
* Les recherches liées au RBAC/périmètre seront réalisées dans les Services
  (règles de visibilité), pas dans les Repository.

## Séparation Repository / Service
* Les Repository ne contiennent **aucune** logique métier : lecture/écriture
  uniquement.
* Les Services orchestrent les Repository et appliquent sécurité, validation
  et règles métier (prochains loops).

## Annotations
* Aucune annotation `@Repository` ajoutée (non nécessaire : les interfaces
  `JpaRepository` sont détectées automatiquement par Spring Data).

---

# 4. VÉRIFICATIONS

| Contrôle | Résultat |
|----------|:---:|
| Compilation Maven | ✅ BUILD SUCCESS |
| Démarrage Spring Boot (contexte) | ✅ OK |
| Injection des 19 Repository | ✅ (RepositoryInjectionTest) |
| Absence d'erreur JPA | ✅ (métamodèle Hibernate chargé) |
| Tests | ✅ 4/4 |

---

FIN DU DOCUMENT — REPOSITORY JPA VALIDÉS
