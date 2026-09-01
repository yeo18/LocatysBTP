# Architecture de la couche Service — CMS

Version : 1.0
Statut : ✅ Architecture des Services validée (conception uniquement, aucun code implémenté)
Base : LOOP 3.6
Source : 19 Entity (LOOP 3.3), 19 Repository (LOOP 3.4), 75 DTO + 19 Mappers (LOOP 3.5)

---

# 1. ORGANISATION DES SERVICES

Un Service **par entité métier** (sauf associations portées par leur entité racine), organisé par module.

```
utilisateur
 ├── service
 │     ├── UtilisateurService / UtilisateurServiceImpl
 │     └── AuthenticationService / AuthenticationServiceImpl

profil
 ├── service
 │     ├── ProfilService / ProfilServiceImpl

permission
 ├── service
 │     ├── PermissionService / PermissionServiceImpl

chantier
 ├── service
 │     ├── ChantierService / ChantierServiceImpl

equipe
 ├── service
 │     ├── EquipeService / EquipeServiceImpl
 │     ├── MembreEquipeService / MembreEquipeServiceImpl
 │     └── AffectationEquipeChantierService / AffectationEquipeChantierServiceImpl

tache
 ├── service
 │     ├── TacheService / TacheServiceImpl
 │     ├── PlanningService / PlanningServiceImpl
 │     ├── ValidationTacheService / ValidationTacheServiceImpl
 │     ├── CommentaireService / CommentaireServiceImpl
 │     └── AffectationTacheService / AffectationTacheServiceImpl

document
 ├── service
 │     ├── DocumentService / DocumentServiceImpl
 │     ├── PieceJointeService / PieceJointeServiceImpl
 │     └── PhotoChantierService / PhotoChantierServiceImpl

audit
 ├── service
 │     ├── HistoriqueActionService / HistoriqueActionServiceImpl
 │     └── JournalConnexionService / JournalConnexionServiceImpl
```

**Total : 16 Services** (interfaces + implémentations).

## Module des associations

| Association | Service porteur |
|-------------|-----------------|
| UtilisateurPermission | UtilisateurService (gestion RBAC individuelle) |
| ProfilPermission | ProfilService (gestion RBAC profil) |
| MembreEquipe | MembreEquipeService |
| AffectationEquipeChantier | AffectationEquipeChantierService |
| AffectationTache | AffectationTacheService |

Les tables d'association sont pilotées par leur Service dédié (équipe, affectation), pas par les entités qu'elles relient.

---

# 2. RESPONSABILITÉS PAR SERVICE

## 2.1 UtilisateurService
- Rôle : gestion du cycle de vie des comptes utilisateur.
- Opérations : inscription, activation/désactivation, consultation (par id/email/résumé), modification du profil, gestion des exceptions RBAC individuelles (UtilisateurPermission ACCORDER/REFUSER).
- Règles générales : mot de passe haché côté implémentation (jamais en clair) ; email unique ; un utilisateur actif possède un profil valide. **Pas de logique JWT**.
- Dépendances autorisées : UtilisateurRepository, UtilisateurPermissionRepository, ProfilService (profilId), PermissionService (permissionId).

## 2.2 AuthenticationService
- Rôle : enregistrer les événements d'authentification (succès/échec) et tracer les connexions.
- Opérations : journaliser une connexion (succès/échec, IP, user-agent).
- Règles générales : délégue la vérification du couple email/mot de passe au LOOP 3.7 (JWT non implémenté ici) ; table de journal immuable (création seule).
- Dépendances autorisées : JournalConnexionService.

## 2.3 ProfilService
- Rôle : gestion des profils (rôles RBAC) et de leurs permissions par défaut.
- Opérations : création, modification, activation/désactivation, consultation, gestion des ProfilPermission.
- Règles générales : nom de profil unique ; un profil désactivé ne peut pas être attribué (contrôle en implémentation) ; profils système (ADMINISTRATEUR, UTILISATEUR_STANDARD) non supprimables.
- Dépendances autorisées : ProfilRepository, ProfilPermissionRepository, PermissionService.

## 2.4 PermissionService
- Rôle : gestion du référentiel des permissions.
- Opérations : création, modification, consultation, recherche par module, validation d'un code technique.
- Règles générales : `codePermission` unique et en MAJUSCULES/underscore (validé en DTO) ; référentiel quasi-statique, aucun attribut dépendant d'un autre module.
- Dépendances autorisées : PermissionRepository.

## 2.5 ChantierService
- Rôle : gestion du cycle de vie des chantiers.
- Opérations : création, modification, consultation (détail/résumé, paginée), changement de statut, calcul de progression.
- Règles générales : un chantier actif (EN_COURS) doit avoir un responsable (0..1 → obligation si actif) ; dates cohérentes (dateFin ≥ dateDebut) ; budget indicatif.
- Dépendances autorisées : ChantierRepository, AffectationEquipeChantierRepository, UtilisateurService (responsable), TacheService (progression).

## 2.6 EquipeService
- Rôle : gestion des équipes.
- Opérations : création, modification, activation/désactivation, consultation.
- Règles générales : nom unique dans le module, équipe active = disponible pour affectation.
- Dépendances autorisées : EquipeRepository.

## 2.7 MembreEquipeService
- Rôle : gestion de l'appartenance des utilisateurs aux équipes.
- Opérations : intégrer/retirer un membre, changer de rôle, lister les membres d'une équipe et les équipes d'un utilisateur.
- Règles générales : unicité (utilisateur, equipe) ; un utilisateur ne peut pas être membre deux fois de la même équipe ; rôle dans l'équipe (CHEF/OUVRIER).
- Dépendances autorisées : MembreEquipeRepository, UtilisateurService, EquipeService.

## 2.8 AffectationEquipeChantierService
- Rôle : affectation d'une équipe à un chantier (base de la sécurité par périmètre).
- Opérations : affecter, désaffecter, changer de période/statut, lister les équipes d'un chantier.
- Règles générales : dateFin ≥ dateDebut ; une seule affectation ACTIVE par (équipe, chantier) ; statut ACTIVE par défaut.
- Dépendances autorisées : AffectationEquipeChantierRepository, EquipeService, ChantierService.

## 2.9 TacheService
- Rôle : gestion des tâches de chantier.
- Opérations : création, modification, changement de statut, progression, consultation (paginée par chantier, par statut, par priorité).
- Règles générales : toute tâche appartient à un chantier (obligatoire) ; progression bornée 0-100 ; rattachement planning optionnel ; status conforme au cycle (A_FAIRE → EN_COURS → TERMINE → VALIDE/REFUSE).
- Dépendances autorisées : TacheRepository, ChantierService, PlanningService, UtilisateurService (createdBy), AffectationTacheService, ValidationTacheService (statut).

## 2.10 AffectationTacheService
- Rôle : affectation d'une tâche à un utilisateur et/ou une équipe.
- Opérations : affecter, désaffecter, lister les affectations d'une tâche, les tâches d'un utilisateur/équipe.
- Règles générales : **au moins une cible obligatoire** (utilisateur OU équipe) — règle contrôlée ici ; pas de double affectation active identique.
- Dépendances autorisées : AffectationTacheRepository, TacheService, UtilisateurService, EquipeService.

## 2.11 ValidationTacheService
- Rôle : décisions de validation des tâches (historique conservé).
- Opérations : valider/refuser une tâche, consulter l'historique des validations.
- Règles générales : une validation ne concerne qu'une tâche TERMINEE (à confirmer en implémentation) ; chaque décision est une nouvelle ligne (historique) ; commentaire ≤ 500.
- Dépendances autorisées : ValidationTacheRepository, TacheService, UtilisateurService (validateur).

## 2.12 PlanningService
- Rôle : gestion des périodes de planification.
- Opérations : création, modification, consultation, rattachement de tâches.
- Règles générales : un planning appartient à un chantier ; dateFin ≥ dateDebut ; statut PREVU par défaut.
- Dépendances autorisées : PlanningRepository, ChantierService.

## 2.13 CommentaireService
- Rôle : commentaires sur les tâches et les chantiers.
- Opérations : créer, modifier, lister par tâche/chantier.
- Règles générales : **un seul contexte obligatoire** (tache OU chantier, jamais les deux) — règle contrôlée ici ; auteur = utilisateur connecté.
- Dépendances autorisées : CommentaireRepository, TacheService, ChantierService, UtilisateurService (auteur).

## 2.14 DocumentService
- Rôle : gestion des documents métier d'un chantier.
- Opérations : dépôt, modification des métadonnées, changement de statut, consultation, pièces jointes.
- Règles générales : rattachement obligatoire au chantier (1,1) ; chemin fichier obligatoire ; uploader = utilisateur connecté.
- Dépendances autorisées : DocumentRepository, ChantierService, UtilisateurService (uploader), PieceJointeService.

## 2.15 PieceJointeService
- Rôle : pièces jointes (rattachement document OU polymorphe entite_type/entite_id).
- Opérations : ajouter, supprimer, lister.
- Règles générales : un seul mode de rattachement (document OU polymorphe) — contrôle en implémentation ; table non immuable (suppression de fichier possible).
- Dépendances autorisées : PieceJointeRepository, DocumentService.

## 2.16 PhotoChantierService
- Rôle : photos d'avancement d'un chantier.
- Opérations : ajouter, modifier la description, lister par chantier.
- Règles générales : photo rattachée à un chantier ; priseur = utilisateur connecté ; type PhotoType obligatoire.
- Dépendances autorisées : PhotoChantierRepository, ChantierService, UtilisateurService.

## 2.17 HistoriqueActionService
- Rôle : journal d'audit des actions utilisateur (immuable).
- Opérations : enregistrer une action, consulter (par utilisateur, par entité, paginé).
- Règles générales : écriture seule (aucune modification/suppression) ; alimenté par les autres Services pour les événements importants.
- Dépendances autorisées : HistoriqueActionRepository.

## 2.18 JournalConnexionService
- Rôle : journal des connexions/déconnexions (immuable).
- Opérations : enregistrer une connexion (type + statut + IP + user-agent), consulter par utilisateur.
- Règles générales : écriture seule ; alimenté par AuthenticationService.
- Dépendances autorisées : JournalConnexionRepository.

---

# 3. RÈGLES DE DÉPENDANCE (OBJECTIF 3)

| Règle | Statut |
|-------|:---:|
| Un Controller appelle uniquement un Service | ✅ |
| Un Service appelle uniquement des Repository ou des Services publics d'autres modules | ✅ |
| Un Repository n'appelle jamais un autre Repository | ❌ interdit |
| Un Controller n'appelle jamais un Repository | ❌ interdit |
| Une Entity n'appelle jamais un Service | ❌ interdit |
| Un Service n'appelle jamais un autre Service du même module pour le même entité (il orchestre ses Repository) | ❌ interdit sauf cas documenté |

## Graphe de dépendances entre Services

```
Controller ──> Service ──> Repository / Service public d'un autre module
```

Autorisé :
- `ChantierService → TacheService, UtilisateurService, AffectationEquipeChantierService`
- `TacheService → ValidationTacheService, AffectationTacheService`
- `MembreEquipeService → UtilisateurService, EquipeService`
- `AuthenticationService → JournalConnexionService`
- `DocumentService → PieceJointeService`
- `UtilisateurService → ProfilService, PermissionService`

Interdit :
- injection d'un Repository dans un Controller ;
- appel de Service depuis une Entity ;
- Repository → Repository.

Règle transverse : **la sécurité (RBAC, périmètre) ne s'implémente pas dans les Repository** ; elle est orchestrée dans les Services (Niveau 2 — restriction des données) en s'appuyant sur les méthodes de périmètre des Repository.

---

# 4. STRATÉGIE TRANSACTIONNELLE (OBJECTIF 4)

| Catégorie | Annotation | Détail |
|-----------|-----------|--------|
| Lecture (consultation, recherche, détail) | `@Transactional(readOnly = true)` | sur la méthode de l'interface ou de l'implémentation |
| Écriture (création, modification, suppression, changement de statut) | `@Transactional` | transaction ouverte, commit à la fin |
| Orchestration multi-repository | `@Transactional` | la méthode du Service public porte la transaction (appel d'un seul endroit) |
| Rollback | par défaut | rollback sur toute `RuntimeException` / exception métier (`CmsException` et descendants) |

## Règles
1. **La transaction est portée par le Service public** (la méthode qui réalise l'unité de travail), jamais par le Controller.
2. **Jamais de `@Transactional` sur une méthode `private`** (proxy Spring : non appliqué).
3. **Les appels de Service → Service passent par l'interface publique** pour conserver la transaction du proxy.
4. **`readOnly = true`** pour toute méthode qui ne modifie pas l'état (guide l'optimisation Hibernate, pas une sécurité).
5. Les exceptions métier (`CmsException` et sous-classes : BadRequestException, ResourceNotFoundException, UnauthorizedException, ForbiddenException) provoquent un rollback automatique.

> ⚠️ Non implémenté dans ce LOOP : l'annotation `@Transactional` sera posée à l'implémentation (LOOP 3.7+).

---

# 5. STRATÉGIE DE JOURNALISATION (OBJECTIF 5)

## Niveaux utilisés
| Niveau | Usage |
|--------|-------|
| `INFO` | Événements normaux d'entreprise : création, modification, connexion réussie |
| `WARN` | Situations anormales non bloquantes : tentative de connexion échouée, ressource désactivée, périmètre restreint |
| `ERROR` | Erreurs fonctionnelles et techniques : exception non gérée, échec d'écriture, rollback |

## Événements à journaliser (INFO)
- Création utilisateur, activation/désactivation
- Connexion / déconnexion (réussies)
- Création / modification de chantier, changement de statut
- Création d'équipe, affectation/désaffectation équipe↔chantier
- Affectation de tâche (utilisateur ou équipe)
- Validation de tâche (VALIDE / REFUSE)
- Dépôt de document / photo

## Événements à journaliser (WARN)
- Échec de connexion (identifiants invalides)
- Utilisation d'un profil/équipe désactivé
- Tentative d'accès hors périmètre (bloquée)

## Événements à journaliser (ERROR)
- Exceptions métier non capturées
- Échec d'une transaction d'écriture

## Canal de traçabilité
- **Logs applicatifs** (SLF4J) : trace technique au niveau Service (`logger.info/warn/error`).
- **Journal métier** (`HistoriqueAction`) : traçabilité fonctionnelle persistée, alimentée par les Services (création utilisateur, chantier, affectation, validation).

---

# 6. CONTRATS DE SERVICE (OBJECTIF 6)

Signatures publiques uniquement (implémentation au LOOP 3.7+). Convention de nommage : français, verbes d'action, retours DTO (jamais d'Entity exposée au Controller).

## UtilisateurService
```
UtilisateurResponse creer(CreateUtilisateurRequest request)
UtilisateurResponse modifier(Long id, UpdateUtilisateurRequest request)
UtilisateurResponse desactiver(Long id)
UtilisateurResponse activer(Long id)
UtilisateurResponse trouverParId(Long id)
UtilisateurResponse trouverParEmail(String email)
UtilisateurResumeResponse trouverResumeParId(Long id)
Page<UtilisateurResumeResponse> rechercher(SearchRequest search)
UtilisateurPermissionResponse accorderPermission(Long utilisateurId, Long permissionId)
UtilisateurPermissionResponse refuserPermission(Long utilisateurId, Long permissionId)
List<UtilisateurPermissionResponse> listerPermissionsIndividuelles(Long utilisateurId)
void retirerPermissionIndividuelle(Long id)
```

## AuthenticationService
```
void enregistrerConnexion(Long utilisateurId, String ip, String userAgent)
void enregistrerEchecConnexion(String email, String ip, String userAgent)
```

## ProfilService
```
ProfilResponse creer(CreateProfilRequest request)
ProfilResponse modifier(Long id, UpdateProfilRequest request)
ProfilResponse desactiver(Long id)
ProfilResponse activer(Long id)
ProfilResponse trouverParId(Long id)
List<ProfilResumeResponse> listerActifs()
Page<ProfilResumeResponse> rechercher(SearchRequest search)
ProfilPermissionResponse ajouterPermission(Long profilId, Long permissionId)
void retirerPermission(Long profilId, Long permissionId)
List<ProfilPermissionResponse> listerPermissions(Long profilId)
```

## PermissionService
```
PermissionResponse creer(CreatePermissionRequest request)
PermissionResponse modifier(Long id, UpdatePermissionRequest request)
PermissionResponse trouverParId(Long id)
PermissionResponse trouverParCode(String codePermission)
List<PermissionResumeResponse> listerParModule(String module)
Page<PermissionResumeResponse> rechercher(SearchRequest search)
```

## ChantierService
```
ChantierResponse creer(CreateChantierRequest request)
ChantierResponse modifier(Long id, UpdateChantierRequest request)
ChantierResponse changerStatut(Long id, ChantierStatut statut)
ChantierResponse trouverParId(Long id)
ChantierResumeResponse trouverResumeParId(Long id)
Page<ChantierResumeResponse> rechercher(SearchRequest search)
Page<ChantierResumeResponse> listerParStatut(ChantierStatut statut, Pageable pageable)
List<ChantierResumeResponse> listerParResponsable(Long responsableId)
int calculerProgression(Long id)
```

## EquipeService
```
EquipeResponse creer(CreateEquipeRequest request)
EquipeResponse modifier(Long id, UpdateEquipeRequest request)
EquipeResponse desactiver(Long id)
EquipeResponse activer(Long id)
EquipeResponse trouverParId(Long id)
List<EquipeResumeResponse> listerActives()
Page<EquipeResumeResponse> rechercher(SearchRequest search)
```

## MembreEquipeService
```
MembreEquipeResponse integrer(Long equipeId, Long utilisateurId, RoleDansEquipe role, LocalDate dateIntegration)
MembreEquipeResponse changerRole(Long id, RoleDansEquipe role)
void retirer(Long id)
List<MembreEquipeResponse> listerMembres(Long equipeId)
List<MembreEquipeResumeResponse> listerEquipesDeUtilisateur(Long utilisateurId)
```

## AffectationEquipeChantierService
```
AffectationEquipeChantierResponse affecter(Long equipeId, Long chantierId, LocalDate dateDebut, LocalDate dateFin)
AffectationEquipeChantierResponse modifier(Long id, UpdateAffectationEquipeChantierRequest request)
void desaffecter(Long id)
List<AffectationEquipeChantierResponse> listerEquipesDuChantier(Long chantierId)
List<AffectationEquipeChantierResponse> listerChantiersDeEquipe(Long equipeId)
```

## TacheService
```
TacheResponse creer(CreateTacheRequest request)
TacheResponse modifier(Long id, UpdateTacheRequest request)
TacheResponse changerStatut(Long id, TacheStatus status)
TacheResponse mettreAJourProgression(Long id, int progression)
TacheResponse trouverParId(Long id)
Page<TacheResponse> rechercher(SearchRequest search)
Page<TacheResumeResponse> listerParChantier(Long chantierId, Pageable pageable)
List<TacheResumeResponse> listerParStatut(TacheStatus status)
List<TacheResumeResponse> listerParPriorite(Priorite priorite)
```

## AffectationTacheService
```
AffectationTacheResponse affecterUtilisateur(Long tacheId, Long utilisateurId, AffectationTacheRole role, LocalDate dateAffectation)
AffectationTacheResponse affecterEquipe(Long tacheId, Long equipeId, AffectationTacheRole role, LocalDate dateAffectation)
AffectationTacheResponse modifier(Long id, UpdateAffectationTacheRequest request)
void desaffecter(Long id)
List<AffectationTacheResponse> listerParTache(Long tacheId)
List<AffectationTacheResumeResponse> listerParUtilisateur(Long utilisateurId)
List<AffectationTacheResumeResponse> listerParEquipe(Long equipeId)
```

## ValidationTacheService
```
ValidationTacheResponse valider(Long tacheId, String commentaire)
ValidationTacheResponse refuser(Long tacheId, String commentaire)
List<ValidationTacheResponse> listerHistorique(Long tacheId)
ValidationTacheResponse trouverParId(Long id)
```

## PlanningService
```
PlanningResponse creer(CreatePlanningRequest request)
PlanningResponse modifier(Long id, UpdatePlanningRequest request)
PlanningResponse trouverParId(Long id)
List<PlanningResponse> listerParChantier(Long chantierId)
Page<PlanningResumeResponse> rechercher(SearchRequest search)
```

## CommentaireService
```
CommentaireResponse ajouterSurTache(Long tacheId, String contenu)
CommentaireResponse ajouterSurChantier(Long chantierId, String contenu)
CommentaireResponse modifier(Long id, UpdateCommentaireRequest request)
List<CommentaireResponse> listerParTache(Long tacheId)
List<CommentaireResponse> listerParChantier(Long chantierId)
```

## DocumentService
```
DocumentResponse deposer(CreateDocumentRequest request)
DocumentResponse modifier(Long id, UpdateDocumentRequest request)
DocumentResponse changerStatut(Long id, DocumentStatut statut)
DocumentResponse trouverParId(Long id)
List<DocumentResumeResponse> listerParChantier(Long chantierId)
Page<DocumentResumeResponse> rechercher(SearchRequest search)
```

## PieceJointeService
```
PieceJointeResponse ajouter(CreatePieceJointeRequest request)
void supprimer(Long id)
List<PieceJointeResponse> listerParDocument(Long documentId)
```

## PhotoChantierService
```
PhotoChantierResponse ajouter(CreatePhotoChantierRequest request)
PhotoChantierResponse modifierDescription(Long id, String description)
PhotoChantierResponse trouverParId(Long id)
List<PhotoChantierResumeResponse> listerParChantier(Long chantierId)
```

## HistoriqueActionService
```
HistoriqueActionResponse enregistrer(Long utilisateurId, String action, String typeEntite, Long entiteId, String details, String ipAdresse)
Page<HistoriqueActionResponse> listerParUtilisateur(Long utilisateurId, Pageable pageable)
Page<HistoriqueActionResponse> rechercher(SearchRequest search)
```

## JournalConnexionService
```
JournalConnexionResponse enregistrer(Long utilisateurId, JournalConnexionType type, JournalConnexionStatut statut, String ipAdresse, String userAgent)
Page<JournalConnexionResponse> listerParUtilisateur(Long utilisateurId, Pageable pageable)
Page<JournalConnexionResumeResponse> rechercher(SearchRequest search)
```

---

# 7. RÈGLES CROSS-CUTTING APPLICABLES À L'IMPLÉMENTATION

1. **Interface + Implémentation** : `XXXService` (interface publique) et `XXXServiceImpl` (classe `@Service`), injection par constructeur.
2. **Mapping systématique** : Entity ↔ DTO via les Mappers MapStruct (LOOP 3.5), jamais de manipulation directe d'Entity dans le Controller.
3. **Exceptions métier** : `CmsException` et descendants (LOOP 1.7) levées par les Services ; `ResourceNotFoundException` pour `trouverParId`, `BadRequestException` pour les règles métier.
4. **Périmètre RBAC (Niveau 2)** : les listes et `trouverParId` applicatives restreignent les données en fonction de l'utilisateur connecté (affectations équipe↔chantier, membre_equipe, affectation_tache) — mis en œuvre à partir du LOOP 3.7/RBAC.
5. **Immutabilité** : HistoriqueAction et JournalConnexion exposent uniquement `enregistrer` + lecture (aucun update/delete).

---

# 8. VÉRIFICATIONS (OBJECTIF CONTRÔLES)

- ✅ aucun Controller créé
- ✅ aucune logique métier implémentée
- ✅ aucun JWT
- ✅ aucun Flyway / SQL
- ✅ aucun code de sécurité / RBAC écrit
- ✅ document de conception uniquement : `docs/architecture-services.md`
- ✅ MASTER_PLAN mis à jour (LOOP 3.6 ✅, prochain 3.7)

---

FIN DE LA DOCUMENTATION
