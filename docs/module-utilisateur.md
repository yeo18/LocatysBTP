# Module Utilisateur — CMS

Version : 1.0
Statut : ✅ Module Utilisateur validé (Service + inscription + gestion, sans Controller/JWT/Flyway)
Base : LOOP 3.7
Source : Entity (LOOP 3.3), Repository (LOOP 3.4), DTO/Mapper (LOOP 3.5), Architecture Service (LOOP 3.6)

---

# 1. FONCTIONNALITÉS

| Cas d'utilisation | Méthode Service | Description |
|-------------------|-----------------|-------------|
| Inscription | `creerUtilisateur` | Crée un compte : email unique, mot de passe BCrypt, profil attribué, compte actif |
| Consultation | `trouverParId`, `trouverParEmail`, `trouverTous` | Détail, recherche par identifiant, liste des actifs |
| Recherche | `rechercher` | Recherche paginée (mot-clé nom/prénom/email, tri) |
| Modification | `modifierUtilisateur` | nom, prénom, email, téléphone (rôle inchangé) |
| Désactivation | `desactiverUtilisateur` | Désactivation logique : `actif = false` (aucune suppression physique) |
| Réactivation | `activerUtilisateur` | Réactive un compte : `actif = true` |

---

# 2. RÈGLES MÉTIER

## Inscription
1. Email obligatoire et **unique** (doublon → `EmailDejaUtiliseException`).
2. Mot de passe **toujours encodé en BCrypt** (jamais stocké en clair).
3. Profil attribué : si `profilId` fourni → profil existant (sinon `ResourceNotFoundException`) ; sinon profil système par défaut `UTILISATEUR_STANDARD`.
4. `dateCreation` et `dateModification` horodatées.
5. Compte créé **actif** (`actif = true`).

## Modification
- Seuls les champs autorisés sont modifiables : nom, prénom, email, téléphone.
- Le **profil/role n'est pas modifiable** via ce cas d'utilisation (changement de rôle = permission spécifique, non implémentée ici).
- `actif` n'est pas modifiable via la modification (dédié à activer/desactiver).
- Email modifié : unicité re-vérifiée.

## Désactivation
- Désactivation **logique** uniquement (`actif = false`) : aucune suppression en base.
- L'utilisateur reste présent et traçable (historique, périmètre).

---

# 3. VALIDATIONS

## Bean Validation (DTO, LOOP 3.5)
- `@NotBlank` : nom, prénom, email, password
- `@Email` : email
- `@Size` : bornes des chaînes (conformes aux colonnes JPA)
- `@Pattern` : téléphone (chiffres, +, espaces, points, tirets)

## Validations métier (Service)
| Contrôle | Comportement |
|----------|--------------|
| Email déjà utilisé | `EmailDejaUtiliseException` (HTTP 409) |
| Utilisateur inexistant | `UtilisateurNonTrouveException` (HTTP 404) |
| Profil inexistant | `ResourceNotFoundException` (HTTP 404) |
| Utilisateur déjà inactif / déjà actif | `ResourceNotFoundException` (HTTP 404) |

Aucune méthode ne retourne `null` : les absences lèvent une exception `CmsException`.

---

# 4. MÉTHODES DU SERVICE

## UtilisateurService (interface)
```java
UtilisateurResponse creerUtilisateur(CreateUtilisateurRequest request)
UtilisateurResponse modifierUtilisateur(Long id, UpdateUtilisateurRequest request)
UtilisateurResponse desactiverUtilisateur(Long id)
UtilisateurResponse activerUtilisateur(Long id)
UtilisateurResponse trouverParId(Long id)
UtilisateurResponse trouverParEmail(String email)
List<UtilisateurResumeResponse> trouverTous()
Page<UtilisateurResumeResponse> rechercher(SearchRequest search)
```

## UtilisateurServiceImpl
- `@Service` : composant Spring.
- Injection par constructeur : `UtilisateurRepository`, `ProfilRepository`, `UtilisateurMapper`, `PasswordEncoder`.
- Retours DTO uniquement (jamais d'Entity exposée).

---

# 5. SÉCURITÉ APPLIQUÉE

| Point | Mise en œuvre |
|-------|---------------|
| Mot de passe | `PasswordEncoder` BCrypt (bean `BeanConfig`) — hash BCrypt, jamais en clair |
| Données sensibles | `UtilisateurResponse` ne contient jamais `password` |
| RBAC / rôle | Changement de rôle interdit sans permission dédiée (LOOP ultérieur) |
| Périmètre | Restriction des données prévue en implémentation RBAC (Niveau 2) |
| JWT | **Aucun** : authentification complète au LOOP 3.8 |

---

# 6. TRANSACTIONS

| Type | Annotation |
|------|------------|
| Lecture (trouver, rechercher, trouverTous) | `@Transactional(readOnly = true)` |
| Écriture (créer, modifier, activer, désactiver) | `@Transactional` |

Rollback automatique sur toute exception `CmsException` / `RuntimeException`.

---

# 7. AUDIT (préparation)

Les événements à tracer sont préparés (finalisation avec le module Audit) :

| Événement | action (HistoriqueAction) |
|-----------|---------------------------|
| Création utilisateur | `CREATION_UTILISATEUR` |
| Modification utilisateur | `MODIFICATION_UTILISATEUR` |
| Activation | `ACTIVATION_UTILISATEUR` |
| Désactivation | `DESACTIVATION_UTILISATEUR` |

---

# 8. TESTS (UtilisateurServiceImplTest — 11 tests)

| Test | Vérifie |
|------|---------|
| `creerUtilisateur_fonctionne_avecProfilEtPasswordEncode` | création OK, profil attribué, password BCrypt, dates renseignées |
| `creerUtilisateur_emailDoublon_refuse` | doublon email → `EmailDejaUtiliseException`, aucun enregistrement |
| `creerUtilisateur_profilParDefaut_siAbsent` | profil par défaut `UTILISATEUR_STANDARD` |
| `modifierUtilisateur_neModifiePasLeProfil` | rôle conservé, `actif` conservé |
| `desactiverUtilisateur_passeActifAFalse` | désactivation logique |
| `desactiverUtilisateur_dejaInactif_leveException` | double désactivation refusée |
| `trouverParId_inexistant_leveException` | `UtilisateurNonTrouveException` |
| `trouverParId_existant_retourneReponse` | retour DTO complet |
| `trouverParEmail_inexistant_leveException` | `UtilisateurNonTrouveException` |
| `trouverTous_retourneUtilisateursActifs` | liste des actifs |
| `rechercher_sansMotCle_retournePage` | pagination OK |

Résultats : **16/16 tests OK** (11 module utilisateur + 5 existants), BUILD SUCCESS.

---

# 9. CE QUI N'A PAS ÉTÉ FAIT

- ❌ aucun Controller REST
- ❌ aucun JWT complet
- ❌ aucune migration Flyway
- ❌ aucune modification des Entity existantes
- ❌ aucune permission métier créée
- ❌ aucune logique RBAC contournée

---

FIN DE LA DOCUMENTATION
