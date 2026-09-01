# MLD RELATIONNEL — CMS

Version : **Finale** (modèle implémenté en base)
Référence : migrations Flyway V1 → V22

Conventions : tables et colonnes en `snake_case`. Type logique générique (PostgreSQL : `bigserial` pour les PK, `bigint` pour les FK, `varchar` pour les textes, `date`/`timestamp` selon le besoin).

---

## 1. Schéma global

```
utilisateur
profil
permission
profil_permission
utilisateur_permission
utilisateur_permission_chantier
chantier
equipe
membre_equipe
affectation_equipe_chantier
tache
affectation_tache_utilisateur
affectation_tache_equipe
validation_tache
affectation_utilisateur_chantier         <- relation ternaire Utilisateur x Chantier x Profil
template_chantier
template_tache
template_chantier_tache
template_tache_tache
```

---

## 2. Détail des tables

### Entités « mères » (créées par elles-mêmes)

| Table | Entité mère | Attributs | Clés étrangères |
| --- | --- | --- | --- |
| `utilisateur` | Utilisateur | id, nom, prenom, email, password, telephone, date_creation, date_modification | profil_id → profil |
| `profil` | Profil | id, nom, description, date_creation, date_modification | — |
| `permission` | Permission | id, nom, nom_permission, module, description | — |
| `chantier` | Chantier | id, nom, description, adresse_saisie, statut, date_debut, date_fin, progression, latitude, longitude, adresse_geocodee, origine_coordonnees, fiabilite_coordonnees, date_creation, date_modification | responsable_id → utilisateur |
| `equipe` | Equipe | id, nom, description, date_creation, date_modification | — |
| `tache` | Tache | id, titre, description, priorite, status, progression, date_debut, date_fin, date_realisation, date_creation, date_modification | chantier_id → chantier, created_by → utilisateur |
| `validation_tache` | ValidationTache | id, statut, commentaire, date_validation, date_modification | tache_id → tache, validateur_id → utilisateur |

### Entités « nées d'une association » (créées par les entités mères de la relation)

| Table | Entités mères | Attributs | Clés étrangères |
| --- | --- | --- | --- |
| `profil_permission` | Profil + Permission | id | profil_id → profil, permission_id → permission |
| `utilisateur_permission` | Utilisateur + Permission | id, type, date_creation, date_modification | utilisateur_id → utilisateur, permission_id → permission, created_by → utilisateur |
| `membre_equipe` | Utilisateur + Equipe | id, role_dans_equipe, date_integration | utilisateur_id → utilisateur, equipe_id → equipe |
| `affectation_equipe_chantier` | Equipe + Chantier | id, statut, date_debut, date_fin | equipe_id → equipe, chantier_id → chantier |
| `affectation_tache_utilisateur` | Tache + Utilisateur | id, role, date_affectation, date_creation, date_modification | tache_id → tache, utilisateur_id → utilisateur |
| `affectation_tache_equipe` | Tache + Equipe | id, role, date_affectation | tache_id → tache, equipe_id → equipe |

### Relations ternaires : les deux liaisons entre utilisateur, chantier et droits

Deux tables ternaires relient les mondes Utilisateur / Chantier / droits d'accès :

| Table | Entités mères (ternaire) | Attributs | Clés étrangères | Règle |
| --- | --- | --- | --- | --- |
| `affectation_utilisateur_chantier` | **Utilisateur + Chantier + Profil** | id, date_affectation, date_debut, date_fin | utilisateur_id → utilisateur, chantier_id → chantier, profil_id → profil | Un seul et unique profil par chantier et par période (aucun chevauchement). `date_fin` exclue, nullable = période ouverte. Permet à un utilisateur de porter plusieurs profils (selon chantier/période). |
| `utilisateur_permission_chantier` | **Utilisateur + Chantier + Permission** | id, type (ACCORDER / REFUSER), date_creation, date_modification | utilisateur_id → utilisateur, chantier_id → chantier, permission_id → permission, created_by → utilisateur | Exceptions de permissions scopées à un chantier précis : ACCORDER ajoute, REFUSER retire (REFUSER prioritaire sur CE chantier). Complète le profil du chantier. |

---

## 3. Relations entre tables

| Table | FK | Table cible |
| --- | --- | --- |
| utilisateur | profil_id | profil |
| profil_permission | profil_id / permission_id | profil / permission |
| utilisateur_permission | utilisateur_id / permission_id / created_by | utilisateur / permission / utilisateur |
| utilisateur_permission_chantier | utilisateur_id / chantier_id / permission_id / created_by | utilisateur / chantier / permission / utilisateur |
| chantier | responsable_id | utilisateur |
| membre_equipe | utilisateur_id / equipe_id | utilisateur / equipe |
| affectation_equipe_chantier | equipe_id / chantier_id | equipe / chantier |
| tache | chantier_id / created_by | chantier / utilisateur |
| affectation_tache_utilisateur | tache_id / utilisateur_id | tache / utilisateur |
| affectation_tache_equipe | tache_id / equipe_id | tache / equipe |
| validation_tache | tache_id / validateur_id | tache / utilisateur |
| affectation_utilisateur_chantier | utilisateur_id / chantier_id / profil_id | utilisateur / chantier / profil |
| template_chantier | created_by | utilisateur |
| template_tache | created_by | utilisateur |
| template_chantier_tache | template_chantier_id / template_tache_id | template_chantier / template_tache |
| template_tache_tache | template_tache_id / created_by | template_tache / utilisateur |

---

## 4. Règles métier essentielles

1. **RBAC dynamique** : droits effectifs = permissions du profil + ACCORDER − REFUSER (REFUSER prioritaire), relus en base à chaque requête.
2. **RBAC de chantier** : `utilisateur_permission_chantier` ajoute/retire des permissions scopées à un chantier ; le profil de `affectation_utilisateur_chantier` (période active) ajoute les permissions de son profil sur ce chantier.
3. **Profil par chantier** : un utilisateur = un seul profil actif par chantier tant que la période est valide, plusieurs profils possibles sinon.
4. **Sécurité par périmètre** : visibilité chantiers via `affectation_equipe_chantier` (ACTIVE) + `affectation_utilisateur_chantier` ; équipes via `membre_equipe` ; tâches via `affectation_tache_*`.
5. **Affectation de tâche** : au moins une cible — soit une affectation utilisateur, soit une affectation équipe.
6. **Validation de tâche** : statut VALIDE / REFUSE ; l'historique des validations est conservé.
7. **Chantier** : statut PREVU / EN_COURS / TERMINE / ANNULE ; progression entre 0 et 100 ; date_fin >= date_debut.
8. **Contraintes d'unicité** : email (utilisateur), nom (profil), nom_permission (permission), couple (profil_id, permission_id), couple (utilisateur_id, equipe_id).

FIN — DERNIÈRE VERSION DU MLD (modèle implémenté).