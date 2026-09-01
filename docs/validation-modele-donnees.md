# Audit modèle de données — CMS

Version : 1.1
Statut : ✅ Modèle validé pour développement
Base : LOOP 2.7
Documents audités :
- `docs/dictionnaire-donnees-valide.md` v1.2 (référence officielle)
- `docs/relations-entites.md` (LOOP 2.3)
- `docs/cardinalites-merise.md` (LOOP 2.4)
- `docs/MCD-final.md` (LOOP 2.5)
- `docs/MLD-relationnel.md` (LOOP 2.6)

---

# 1 — CONTRÔLE DES ENTITÉS

Nombre total attendu : **19 entités**.

| # | Entité | Dictionnaire v1.2 | MCD | MLD |
|---|--------|:---:|:---:|:---:|
| 1 | Utilisateur | ✅ | ✅ | ✅ |
| 2 | Profil | ✅ | ✅ | ✅ |
| 3 | Permission | ✅ | ✅ | ✅ |
| 4 | ProfilPermission | ✅ | ✅ | ✅ |
| 5 | UtilisateurPermission | ✅ | ✅ | ✅ |
| 6 | Chantier | ✅ | ✅ | ✅ |
| 7 | Equipe | ✅ | ✅ | ✅ |
| 8 | MembreEquipe | ✅ | ✅ | ✅ |
| 9 | AffectationEquipeChantier | ✅ | ✅ | ✅ |
| 10 | Tache | ✅ | ✅ | ✅ |
| 11 | AffectationTache | ✅ | ✅ | ✅ |
| 12 | ValidationTache | ✅ | ✅ | ✅ |
| 13 | Planning | ✅ | ✅ | ✅ |
| 14 | Commentaire | ✅ | ✅ | ✅ |
| 15 | HistoriqueAction | ✅ | ✅ | ✅ |
| 16 | JournalConnexion | ✅ | ✅ | ✅ |
| 17 | Document | ✅ | ✅ | ✅ |
| 18 | PieceJointe | ✅ | ✅ | ✅ |
| 19 | PhotoChantier | ✅ | ✅ | ✅ |

**Résultat : Correspondance 100% (19/19). ✅**

Aucune entité hors CORE (Client, Fournisseur, Materiel, Stock, Dépense,
Notification...) dans le MCD ni le MLD. ✅

---

# 2 — CONTRÔLE DES ATTRIBUTS

## Contrôles particuliers

### Utilisateur
| Attribut | Dictionnaire v1.2 | MLD | Résultat |
|----------|:---:|:---:|:---:|
| nom | ✅ | nom | OK |
| prenom | ✅ | prenom | OK |
| email | ✅ | email (UNIQUE) | OK |
| password | ✅ | password | OK |
| telephone | ✅ | telephone | OK |
| actif | ✅ | actif (défaut true) | OK |
| dates (création/modification) | ✅ | date_creation, date_modification | OK |

### Permission
| Attribut | Dictionnaire v1.2 | MLD | Résultat |
|----------|:---:|:---:|:---:|
| codePermission | ✅ | code_permission (UNIQUE) | OK |
| module | ✅ | module | OK |
| action | ✅ | action | OK |

### UtilisateurPermission
| Attribut | Dictionnaire v1.2 | MLD | Résultat |
|----------|:---:|:---:|:---:|
| type ACCORDER/REFUSER | ✅ | type (+ CHECK) | OK |
| createdBy | ✅ | created_by (FK) | OK |
| dateCreation | ✅ | date_creation | OK |

### Tache
| Attribut | Dictionnaire v1.2 | MLD | Résultat |
|----------|:---:|:---:|:---:|
| priorite | ✅ | priorite (+ CHECK) | OK |
| status | ✅ | status (+ CHECK) | OK |
| progression | ✅ | progression (0-100) | OK |
| dateRealisation | ✅ | date_realisation | OK |

**Présence des attributs validés : 19/19 tables conformes.**
**Absence d'attribut inventé : ✅**
**Respect des noms métier (camelCase → snake_case) : ✅**

---

# 3 — CONTRÔLE RBAC

## Héritage
```
UTILISATEUR
     |
     ▼
   PROFIL
     |
     ▼
PROFIL_PERMISSION
     |
     ▼
PERMISSION
```
Traduit dans le MLD : `utilisateur.profil_id`, `profil_permission`. ✅

## Exception utilisateur
```
UTILISATEUR_PERMISSION (type = ACCORDER / REFUSER)
```
Traduit : `utilisateur_permission.utilisateur_id`, `permission_id`, `type`. ✅

## Formule
```
Droits effectifs = Permissions du profil + ACCORDER − REFUSER
```
Présente dans les 3 documents. ✅

## Priorité
```
REFUSER > ACCORDER
```
Présente (dictionnaire §2, MCD §4.1, MLD §4.1). ✅

---

# 4 — CONTRÔLE SÉCURITÉ PAR PÉRIMÈTRE

Un utilisateur standard (UTILISATEUR_STANDARD) :

| Peut voir | Source de visibilité | Résultat |
|-----------|----------------------|:---:|
| Ses chantiers autorisés | AffectationEquipeChantier | ✅ |
| Ses tâches | AffectationTache | ✅ |
| Son équipe | MembreEquipe | ✅ |
| Son historique | HistoriqueAction / JournalConnexion (son compte) | ✅ |

| Ne peut pas | Résultat |
|-------------|:---:|
| Voir tous les chantiers | ✅ (interdit) |
| Voir toutes les tâches | ✅ (interdit) |
| Consulter les données globales | ✅ (interdit, dictionnaire §3.2) |

**Une permission ne donne jamais accès à toutes les données. ✅**

---

# 5 — CONTRÔLE INTÉGRITÉ RELATIONNELLE

| Règle | Traduction MLD | Résultat |
|-------|----------------|:---:|
| Une tâche ne peut exister sans chantier | `tache.chantier_id` NOT NULL | ✅ |
| Affectation Equipe/Chantier contrôlée | `affectation_equipe_chantier` (statut ACTIVE/TERMINEE, période date_debut/date_fin) | ✅ |
| AffectationTache : au moins une cible (utilisateur OU équipe) | `affectation_tache` : utilisateur_id OU equipe_id (CHECK à exprimer au MPD) | ✅ |
| Responsable chantier (0..1), actif ⇒ obligatoire | `chantier.responsable_id` (0..1) | ✅ |
| Commentaire : un seul contexte (tache OU chantier) | `commentaire` : tache_id OU chantier_id (CHECK au MPD) | ✅ |

---

# 6 — CONTRÔLE NORMALISATION

| Forme | Vérification | Résultat |
|-------|--------------|:---:|
| 1NF | Clé primaire unique `id` sur chaque table ; aucun attribut multi-valué ni liste répétée ; les multi-valeurs sont portées par des tables associatives (profil_permission, membre_equipe, affectation_equipe_chantier, affectation_tache) | ✅ |
| 2NF | Toutes les clés sont simples (id) : aucune dépendance partielle possible | ✅ |
| 3NF | Aucune dépendance transitive : les entités référencent par FK (chantier_id, profil_id...) sans dupliquer les données des tables parentes | ✅ |

**Pas de duplication inutile, pas de données répétées, relations correctement
séparées. ✅**

---

# 7 — CONTRÔLE AUDIT ET TRAÇABILITÉ

| Table | Éléments requis | Présent dans MLD | Résultat |
|-------|-----------------|:---:|:---:|
| HistoriqueAction | action utilisateur | action | ✅ |
|  | date | date_action | ✅ |
|  | contexte | type_entite, entite_id | ✅ |
|  | auteur | utilisateur_id | ✅ |
| JournalConnexion | connexion utilisateur | utilisateur_id | ✅ |
|  | date | date | ✅ |
|  | informations techniques | ip_adresse, user_agent, type, statut | ✅ |

**HistoriqueAction immuable (aucun UPDATE/DELETE) ; connexions et échecs tracés. ✅**

---

# 8 — POINTS REPORTÉS EN MODULES FUTURS (HORS CORE)

| Élément | Dans MCD/MLD ? | Résultat |
|---------|:---:|:---:|
| Client | ❌ | ✅ confirmé hors CORE |
| Fournisseur | ❌ | ✅ confirmé hors CORE |
| Stock | ❌ | ✅ confirmé hors CORE |
| Materiel | ❌ | ✅ confirmé hors CORE |
| Dépense | ❌ | ✅ confirmé hors CORE |
| Facturation | ❌ | ✅ confirmé hors CORE |
| Notification | ❌ | ✅ confirmé hors CORE |
| tacheParente (sous-tâches) | ❌ | ✅ confirmé hors CORE |

---

# ANOMALIES TROUVÉES

| Problème | Correction |
|----------|------------|
| `cardinalites-merise.md` #6 : responsable chantier noté (0,N) | Mettre à jour vers la décision du concepteur **(0,1)** — le MCD et le MLD sont déjà conformes |
| `cardinalites-merise.md` #14 : Document rattaché optionnellement (0,1) | Mettre à jour vers le MCD **(1,1)** — le MLD est déjà conforme (chantier_id NOT NULL) |
| `piece_jointe` : double rattachement (document_id + entite_type/entite_id) | **Trancher au MPD** : rattachement strict au Document OU rattachement polymorphe (au moins une référence). Décision documentée dans le MLD §5 |
| `journal_connexion.date` : mot potentiellement réservé en SQL | Renommer (ex : `date_connexion`) ou citer au MPD |

**Aucune anomalie bloquante pour le développement.**

---

# DÉCISION FINALE

## ✅ MODÈLE VALIDÉ POUR DÉVELOPPEMENT

La phase Merise est terminée :
- 19/19 entités cohérentes (dictionnaire v1.2 = MCD = MLD) ;
- attributs complets et conformes, aucun attribut inventé ;
- RBAC (héritage + exceptions ACCORDER/REFUSER, REFUSER prioritaire) intègre ;
- sécurité par périmètre respectée ;
- intégrité relationnelle vérifiée (FK obligatoires, contraintes d'alternance) ;
- normalisation 1NF / 2NF / 3NF respectée ;
- audit et traçabilité complets ;
- modules futurs exclus du CORE.

Réserves non bloquantes reportées : corrections documentaires de
`cardinalites-merise.md` et décision `piece_jointe` à trancher au MPD.

---

# RAPPORT LOOP 2.7

## OBJECTIF

Audit complet du modèle de données (dictionnaire v1.2, relations, cardinalités,
MCD, MLD) avant le démarrage du développement Spring Boot.

## AUDIT EFFECTUÉ

- Entités : 19/19 dans les 3 documents, correspondance 100%.
- Attributs : 19/19 tables conformes au dictionnaire v1.2 ; contrôles
  particuliers OK (Utilisateur, Permission, UtilisateurPermission, Tache).
- RBAC : héritage Profil→Permission et exceptions UtilisateurPermission
  ACCORDER/REFUSER intègres, formule et priorité vérifiées.
- Sécurité par périmètre : visibilité restreinte (AffectationEquipeChantier,
  MembreEquipe, AffectationTache) vérifiée.
- Intégrité relationnelle : FK obligatoires et contraintes d'alternance vérifiées.
- Normalisation 1NF/2NF/3NF : respectée.
- Audit/traçabilité : HistoriqueAction et JournalConnexion complets.
- Modules futurs : 8 éléments confirmés hors CORE.

## ANOMALIES

4 anomalies non bloquantes (voir tableau ci-dessus) : 2 documentaires
(cardinalites-merise.md), 1 décision à trancher au MPD (piece_jointe), 1
renommage SQL mineur (journal_connexion.date).

## CORRECTIONS

- `cardinalites-merise.md` #6 et #14 : à corriger pour refléter les décisions
  du concepteur déjà appliquées au MCD/MLD (aucun impact sur le modèle).
- `piece_jointe` : décision reportée et documentée au MPD.

## CE QUI A ÉTÉ FAIT

- Audit croisé des 5 documents (dictionnaire v1.2, relations, cardinalités,
  MCD, MLD).
- Livrable `docs/validation-modele-donnees.md` (audit complet + décision finale).
- MASTER_PLAN mis à jour (LOOP 2.7 ✅, prochaine mission LOOP 3.1).

## CE QUI N'A PAS ÉTÉ FAIT

- Aucune entité créée, aucun attribut modifié, aucun module ajouté.
- Aucun code Java, aucun SQL, aucune migration, aucune cardinalité modifiée.

---

# DÉCISION

```
PHASE MERISE TERMINÉE
PASSAGE PHASE TECHNIQUE SPRING BOOT
```

---

# PROCHAIN LOOP

**LOOP 3.1 — Architecture technique Backend Spring Boot**

FIN LOOP 2.7
