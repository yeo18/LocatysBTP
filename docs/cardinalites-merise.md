# Cardinalités Merise — CMS

Version : 1.0
Statut : Conceptuel (aucun code, aucune table)
Base : LOOP 2.4
Sources : `dictionnaire-donnees-valide.md` v1.2, `relations-entites.md`

Convention Merise :

```
ENTITÉ A (min,max) ---- ASSOCIATION ---- (min,max) ENTITÉ B
```

La cardinalité (min,max) écrite à côté d'une entité exprime le nombre
d'occurrences de **cette entité** associées à une occurrence de l'autre entité.

---

# 1. TABLEAU GLOBAL DES CARDINALITÉS

| # | Association | Entité A | Card. A | Entité B | Card. B |
|---|-------------|----------|---------|----------|---------|
| 1 | UTILISATEUR_POSSEDE_PROFIL | UTILISATEUR | (0,N) | PROFIL | (1,1) |
| 2 | PROFIL_POSSEDE_PERMISSION (via ProfilPermission) | PROFIL | (0,N) | PERMISSION | (0,N) |
| 3 | UTILISATEUR_POSSEDE_PERMISSION_SPECIFIQUE (via UtilisateurPermission) | UTILISATEUR | (0,N) | PERMISSION | (0,N) |
| 4 | UTILISATEUR_APPARTIENT_EQUIPE (via MembreEquipe) | UTILISATEUR | (0,N) | EQUIPE | (0,N) |
| 5 | EQUIPE_PARTICIPE_CHANTIER (via AffectationEquipeChantier) | EQUIPE | (0,N) | CHANTIER | (0,N) |
| 6 | UTILISATEUR_RESPONSABLE_CHANTIER | UTILISATEUR | (0,N) | CHANTIER | (0,N) |
| 7 | CHANTIER_CONTIENT_TACHE | CHANTIER | (1,1) | TACHE | (0,N) |
| 8 | TACHE_POSSEDE_AFFECTATION | TACHE | (1,1) | AFFECTATION_TACHE | (0,N) |
| 9 | UTILISATEUR_RECEVOIR_AFFECTATION | UTILISATEUR | (0,N) | AFFECTATION_TACHE | (0,1) |
| 10 | EQUIPE_RECEVOIR_AFFECTATION | EQUIPE | (0,N) | AFFECTATION_TACHE | (0,1) |
| 11 | TACHE_POSSEDE_VALIDATION | TACHE | (1,1) | VALIDATION_TACHE | (0,N) |
| 12 | TACHE_POSSEDE_COMMENTAIRE | TACHE | (0,1) | COMMENTAIRE | (0,N) |
| 13 | CHANTIER_POSSEDE_PLANNING | CHANTIER | (1,1) | PLANNING | (0,N) |
| 14 | CHANTIER_POSSEDE_DOCUMENT | CHANTIER | (0,1) | DOCUMENT | (0,N) |
| 15 | DOCUMENT_POSSEDE_PIECE_JOINTE | — | NON RETENUE | — | — |
| 16 | CHANTIER_POSSEDE_PHOTO | CHANTIER | (1,1) | PHOTO_CHANTIER | (0,N) |
| 17 | UTILISATEUR_GENERE_HISTORIQUE | UTILISATEUR | (0,N) | HISTORIQUE_ACTION | (1,1) |
| 18 | UTILISATEUR_POSSEDE_JOURNAL_CONNEXION | UTILISATEUR | (0,N) | JOURNAL_CONNEXION | (1,1) |

---

# 2. JUSTIFICATION MÉTIER PAR RELATION

## 1 — UTILISATEUR / PROFIL : (0,N) / (1,1)

* Un utilisateur possède **exactement un profil** (1,1 côté PROFIL) : le RBAC est
  porté par un profil unique par utilisateur.
* Un profil peut exister **sans utilisateur** (0,N côté UTILISATEUR) : les profils
  système (ADMINISTRATEUR, UTILISATEUR_STANDARD) existent avant tout utilisateur.
* Règle : le profil d'un utilisateur peut être changé par un administrateur.

## 2 — PROFIL / PERMISSION : (0,N) / (0,N)

* Un profil peut regrouper **plusieurs permissions** (0,N).
* Une permission peut appartenir à **plusieurs profils** (0,N).
* Un profil peut exister sans permission (0,N) ; une permission peut exister
  sans être attribuée (0,N).
* Règle : attribution dynamique via ProfilPermission.

## 3 — UTILISATEUR / PERMISSION : (0,N) / (0,N)

* Exceptions individuelles : un utilisateur peut avoir **plusieurs** permissions
  spécifiques (0,N) ; une permission peut concerner plusieurs utilisateurs (0,N).
* Règle : type obligatoire ACCORDER / REFUSER ; REFUSER prioritaire.

## 4 — UTILISATEUR / EQUIPE : (0,N) / (0,N)

* Un utilisateur peut appartenir à **plusieurs équipes** (0,N) ; une équipe peut
  être **vide** (0,N).
* L'historique d'appartenance est conservé (MembreEquipe avec dateIntegration).
* Point à valider : appartenance simultanée à plusieurs équipes.

## 5 — EQUIPE / CHANTIER : (0,N) / (0,N)

* Une équipe peut travailler sur **plusieurs chantiers** (0,N) ; un chantier peut
  recevoir **plusieurs équipes** (0,N).
* Règle : affectation bornée par période (dateDebut/dateFin) et statut
  (ACTIVE/TERMINEE) → base de la sécurité des données.

## 6 — UTILISATEUR / CHANTIER (responsable) : (0,N) / (0,N)

* Un chantier peut avoir **0 ou plusieurs responsables** (0,N) : la responsabilité
  n'est pas obligatoire à la création.
* Un utilisateur peut gérer **plusieurs chantiers** (0,N).
* Point à valider : imposer au moins un responsable (1,N) pour les chantiers actifs ?

## 7 — CHANTIER / TACHE : (1,1) / (0,N)

* Un chantier peut exister **sans tâche** (0,N) ; toute tâche appartient à
  **un et un seul chantier** (1,1).

## 8 — TACHE / AFFECTATION_TACHE : (1,1) / (0,N)

* Une tâche peut avoir **plusieurs affectations** (0,N) ; une affectation porte
  sur **une seule tâche** (1,1).
* Une tâche peut être créée **sans affectation** (0,N).

## 9 — UTILISATEUR / AFFECTATION_TACHE : (0,N) / (0,1)

* Un utilisateur peut recevoir **plusieurs tâches** (0,N).
* Une affectation peut concerner **0 ou 1 utilisateur** (0,1) : la cible peut être
  une équipe (correction LOOP 2.2.2).
* Règle : au moins une cible (utilisateur OU équipe) obligatoire.

## 10 — EQUIPE / AFFECTATION_TACHE : (0,N) / (0,1)

* Une équipe peut recevoir **directement des tâches** (0,N) — décision LOOP 2.2.2.
* Une affectation peut concerner **0 ou 1 équipe** (0,1).

## 11 — TACHE / VALIDATION_TACHE : (1,1) / (0,N)

* Une validation porte sur **une seule tâche** (1,1).
* Une tâche peut avoir **plusieurs validations** (0,N) : historique des décisions
  VALIDE / REFUSE.
* Point à valider : une seule validation « courante » par tâche (règle applicative).

## 12 — TACHE / COMMENTAIRE : (0,1) / (0,N)

* Un commentaire peut porter sur **0 ou 1 tâche** (0,1) : il peut être rattaché au
  chantier.
* Une tâche peut recevoir **plusieurs commentaires** (0,N), non obligatoires.

## 13 — CHANTIER / PLANNING : (1,1) / (0,N)

* Un planning appartient à **un seul chantier** (1,1).
* Un chantier peut avoir **plusieurs plannings** (0,N).

## 14 — CHANTIER / DOCUMENT : (0,1) / (0,N)

* Un chantier peut avoir **plusieurs documents** (0,N), optionnels.
* Un document peut être rattaché à **0 ou 1 chantier** (0,1) (rattachement optionnel).

## 15 — DOCUMENT / PIECE_JOINTE : NON RETENUE

* PieceJointe est polymorphique (TACHE / COMMENTAIRE / VALIDATION) et Document
  porte son fichier directement. Aucune cardinalité à définir.

## 16 — CHANTIER / PHOTO_CHANTIER : (1,1) / (0,N)

* Une photo appartient à **un seul chantier** (1,1).
* Un chantier peut avoir un **nombre illimité de photos** (0,N).

## 17 — UTILISATEUR / HISTORIQUE_ACTION : (0,N) / (1,1)

* Chaque action tracée est générée par **un utilisateur** (1,1) : un compte
  technique couvre les actions système (point à valider).
* Un utilisateur génère **plusieurs actions** (0,N).
* L'historique est immuable.

## 18 — UTILISATEUR / JOURNAL_CONNEXION : (0,N) / (1,1)

* Chaque connexion est liée à **un utilisateur** (1,1).
* Un utilisateur possède **plusieurs enregistrements de connexion** (0,N).
* Les échecs sont tracés.

---

# 3. CARDINALITÉS COMPLÉMENTAIRES (issues du LOOP 2.3)

| Association | Entité A | Card. A | Entité B | Card. B |
|-------------|----------|---------|----------|---------|
| CHANTIER_RECOIT_COMMENTAIRE | CHANTIER | (0,1) | COMMENTAIRE | (0,N) |
| TACHE_RATTACHEE_PLANNING | PLANNING | (0,N) | TACHE | (0,1) |
| UTILISATEUR_CREE_TACHE | UTILISATEUR | (0,N) | TACHE | (1,1) |
| UTILISATEUR_DEPOSE_DOCUMENT | UTILISATEUR | (0,N) | DOCUMENT | (1,1) |
| UTILISATEUR_PHOTOGRAPHIE | UTILISATEUR | (0,N) | PHOTO_CHANTIER | (1,1) |
| UTILISATEUR_ECRIT_COMMENTAIRE | UTILISATEUR | (0,N) | COMMENTAIRE | (1,1) |

---

# 4. POINTS NÉCESSITANT VALIDATION

1. **Responsable de chantier** : (0,N) retenu — imposer au moins un responsable
   (1,N) pour les chantiers en cours ?
2. **Multi-équipes simultanées** : un utilisateur membre de plusieurs équipes en
   même temps ?
3. **Validations multiples** : autoriser l'historique multiple (0,N) ou une seule
   décision courante par tâche ?
4. **Compte technique** : confirmer le (1,1) côté Utilisateur pour
   HistoriqueAction via un compte technique système.
5. **AffectationTache** : confirmer la contrainte d'alternance « au moins une
   cible, jamais de cible vide ».

---

FIN DU DOCUMENT
