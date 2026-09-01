# Module Équipe — CMS

Version : 1.0
LOOP : 3.14
Références : `docs/dictionnaire-donnees-valide.md` v1.2, `docs/cardinalites-merise.md`, `docs/architecture-backend-spring.md`

---

## 1. Périmètre

Le module Équipe couvre la gestion des équipes de travail, de leurs membres
(utilisateurs) et de leurs affectations à des chantiers. Il s'appuie
uniquement sur les entités existantes, sans aucune Entity ajoutée ni
attribut inventé.

### Entités utilisées

| Entité | Module | Rôle |
|--------|--------|------|
| `Equipe` | equipe | Groupe de travail (id, nom, description, actif, dateCreation, dateModification) |
| `MembreEquipe` | equipe | Appartenance d'un utilisateur à une équipe (id, utilisateur, equipe, roleDansEquipe, dateIntegration) |
| `AffectationEquipeChantier` | equipe | Affectation d'une équipe à un chantier par période (id, equipe, chantier, dateDebut, dateFin, statut) |
| `Utilisateur` | utilisateur | Membre potentiel d'une équipe (référencé par MembreEquipe) |
| `Chantier` | chantier | Cible des affectations d'équipes (référencé par AffectationEquipeChantier) |

Le rôle de **chef d'équipe** n'existe que via `MembreEquipe.roleDansEquipe = CHEF`.
Aucune entité `ChefEquipe` ni `ResponsableEquipe` n'existe.

## 2. Relations et cardinalités (Merise)

| Relation | Cardinalités | Porteur |
|----------|--------------|---------|
| EQUIPE - MEMBRE_EQUIPE - UTILISATEUR | Equipe (0,N) ↔ Utilisateur (0,N) | `MembreEquipe` |
| EQUIPE - AFFECTATION_EQUIPE_CHANTIER - CHANTIER | Equipe (0,N) ↔ Chantier (0,N) | `AffectationEquipeChantier` |

Conséquences :
- un utilisateur peut appartenir à plusieurs équipes ;
- une équipe peut avoir plusieurs membres ;
- une équipe peut travailler sur plusieurs chantiers ;
- un chantier peut avoir plusieurs équipes.

## 3. DTO

### CreateEquipeRequest
`nom` (obligatoire, max 100) · `description` (optionnelle, max 255)

### UpdateEquipeRequest
`nom` (obligatoire, max 100) · `description` (optionnelle, max 255) · `actif`

### EquipeResponse
`id` · `nom` · `description` · `actif`

### EquipeResumeResponse
`id` · `nom` · `actif`

### MembreEquipeResponse
`id` · `utilisateurId` · `utilisateurNom` · `utilisateurPrenom` · `equipeId` · `equipeNom` · `roleDansEquipe` · `dateIntegration`

### MembreEquipeResumeResponse
`id` · `utilisateurId` · `utilisateurNom` · `equipeId` · `equipeNom` · `roleDansEquipe`

### CreateMembreEquipeRequest
`equipeId` (obligatoire) · `utilisateurId` (obligatoire) · `roleDansEquipe` (obligatoire) · `dateIntegration` (obligatoire)

### UpdateMembreEquipeRequest
`roleDansEquipe` (obligatoire) · `dateIntegration` (obligatoire)

### AffectationEquipeChantierResponse
`id` · `equipeId` · `equipeNom` · `chantierId` · `chantierNom` · `dateDebut` · `dateFin` · `statut`

### AffectationEquipeChantierResumeResponse
`id` · `equipeNom` · `chantierNom` · `dateDebut` · `dateFin` · `statut`

### CreateAffectationEquipeChantierRequest
`equipeId` (obligatoire) · `chantierId` (obligatoire) · `dateDebut` (obligatoire) · `dateFin` (optionnelle) · `statut` (défaut ACTIVE)

### UpdateAffectationEquipeChantierRequest
`dateDebut` (obligatoire) · `dateFin` (optionnelle) · `statut` (défaut ACTIVE)

**Règle** : aucune réponse n'expose d'Entity complète (uniquement des résumés id/nom).

## 4. Repository

| Repository | Méthodes utilisées |
|------------|--------------------|
| `EquipeRepository` | `findById`, `findByActifTrue` (recherche active) |
| `MembreEquipeRepository` | `findById`, `findByEquipeId`, `findByUtilisateurId` (recherche par utilisateur) |
| `AffectationEquipeChantierRepository` | `findById`, `findByEquipeId`, `findByChantierId` (recherche par chantier) |

Aucune méthode non nécessaire n'a été ajoutée.

## 5. Services

| Service | Méthodes |
|---------|----------|
| `EquipeService` | `creer`, `modifier`, `activer`, `desactiver`, `trouverParId`, `listerActives` |
| `MembreEquipeService` | `integrer`, `changerRole`, `retirer`, `listerMembres`, `listerEquipesDeUtilisateur` |
| `AffectationEquipeChantierService` | `affecter`, `terminer`, `listerEquipesDuChantier`, `listerChantiersDeEquipe` |

### Règles métier appliquées
- une équipe est créée **active** par défaut ;
- désactivation logique (`actif = false`), réactivation possible ;
- rôle d'un membre limité à `CHEF` / `OUVRIER` (enum) ;
- un utilisateur ne peut pas être membre **deux fois de la même équipe**
  (contrôle en service → 409, doublon) ;
- `dateFin ≥ dateDebut` pour une affectation (si dateFin fournie) ;
- `dateDebut` d'affectation obligatoire ;
- une seule affectation **ACTIVE** par (équipe, chantier) (→ 409 si doublon) ;
- `terminer()` passe le statut d'une affectation à `TERMINEE`.

## 6. Endpoints REST

Base : `/api/v1/equipes` (`ApiRoutes.EQUIPES`).

| Méthode | Route | Permission | Description |
|---------|-------|------------|-------------|
| POST | `/api/v1/equipes` | EQUIPE_CREER | Créer une équipe |
| GET | `/api/v1/equipes` | EQUIPE_LIRE | Lister les équipes actives |
| GET | `/api/v1/equipes/{id}` | EQUIPE_LIRE | Consulter une équipe |
| PUT | `/api/v1/equipes/{id}` | EQUIPE_MODIFIER | Modifier une équipe |
| DELETE | `/api/v1/equipes/{id}` | EQUIPE_SUPPRIMER | Désactiver une équipe (logique) |
| PUT | `/api/v1/equipes/{id}/activer` | EQUIPE_MODIFIER | Réactiver une équipe |
| POST | `/api/v1/equipes/membres` | EQUIPE_MODIFIER | Ajouter un membre |
| PUT | `/api/v1/equipes/membres/{membreId}` | EQUIPE_MODIFIER | Changer le rôle d'un membre |
| DELETE | `/api/v1/equipes/membres/{membreId}` | EQUIPE_SUPPRIMER | Retirer un membre |
| GET | `/api/v1/equipes/{equipeId}/membres` | EQUIPE_LIRE | Lister les membres d'une équipe |
| POST | `/api/v1/equipes/affectations` | EQUIPE_MODIFIER | Affecter une équipe à un chantier |
| PUT | `/api/v1/equipes/affectations/{id}/terminer` | EQUIPE_MODIFIER | Terminer une affectation |
| GET | `/api/v1/equipes/{equipeId}/affectations` | EQUIPE_LIRE | Lister les chantiers d'une équipe |
| GET | `/api/v1/equipes/chantiers/{chantierId}/affectations` | EQUIPE_LIRE | Lister les équipes d'un chantier |

## 7. Permissions

Les permissions sont **uniquement référencées** dans les `@PreAuthorize`
(elles sont utilisées si elles existent dans le référentiel) :

`EQUIPE_CREER` · `EQUIPE_LIRE` · `EQUIPE_MODIFIER` · `EQUIPE_SUPPRIMER`

Aucune permission n'est créée par le module.

## 8. Règles non implémentées (à valider)

- `nom unique` de l'équipe (architecture-services §2.6) : nécessite une
  méthode de Repository absente de la liste autorisée du LOOP 3.14 → à
  valider et implémenter dans un LOOP ultérieur.
- Sécurité par données (Niveau 2, architecture-backend §195 : « Equipe
  visible si l'utilisateur en est membre ») : hors périmètre LOOP 3.14
  (RBAC fonctionnel uniquement) → à implémenter dans un LOOP ultérieur.

## 9. Vérifications

- `mvn clean test` : **124/124 tests OK**, BUILD SUCCESS.
- Aucune Entity ajoutée/modifiée, aucun attribut hors dictionnaire,
  aucune relation JPA modifiée, aucune entité ChefEquipe/ResponsableEquipe.
