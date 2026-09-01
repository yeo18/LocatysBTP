# Relations entre les entités métier — CMS

Version : 1.0
Statut : Conceptuel (aucun code, aucune cardinalité formelle définie)
Base : LOOP 2.3
Source : dictionnaire des données validé v1.2 (`dictionnaire-donnees-valide.md`)

Ce document définit les **associations métier** entre les 19 entités validées.
Les **cardinalités Merise** seront formalisées au LOOP 2.4 (MCD).

---

# 1. MODULE RBAC

## ASSOCIATION : UTILISATEUR_POSSEDE_PROFIL

Entité A : Utilisateur
Entité B : Profil
Sens métier : un utilisateur appartient à un profil qui définit ses droits par défaut.
Règles :
* Un utilisateur possède un profil (obligatoire).
* Le profil d'un utilisateur est modifiable par un administrateur.
* Tout changement de profil recalcule les droits effectifs de l'utilisateur.

## ASSOCIATION : PROFIL_POSSEDE_PERMISSION (via ProfilPermission)

Entité A : Profil
Entité B : Permission
Sens métier : un profil reçoit ses droits par défaut par attribution de permissions.
Règles :
* Un profil peut regrouper plusieurs permissions.
* Une permission peut être attribuée à plusieurs profils.
* L'ajout/retrait d'une permission à un profil est dynamique (géré en base via ProfilPermission).
* Seul l'administrateur peut attribuer des permissions.

## ASSOCIATION : UTILISATEUR_POSSEDE_PERMISSION_SPECIFIQUE (via UtilisateurPermission)

Entité A : Utilisateur
Entité B : Permission
Sens métier : exceptions individuelles accordées ou refusées à un utilisateur.
Règles :
* Le type de l'exception est obligatoire : **ACCORDER** (ajout) ou **REFUSER** (refus).
* **REFUSER est prioritaire** sur le profil et sur ACCORDER.
* Droits effectifs = Permissions du profil + ACCORDER − REFUSER.
* La décision est tracée (`createdBy`, `dateCreation`).
* Un couple (utilisateur, permission, type) est unique.

---

# 2. MODULE ORGANISATION

## ASSOCIATION : CHANTIER_CONTIENT_TACHE

Entité A : Chantier
Entité B : Tache
Sens métier : un chantier regroupe les tâches à réaliser.
Règles :
* Toute tâche appartient à un chantier (obligatoire).
* Un chantier peut avoir plusieurs tâches.
* La suppression d'un chantier est protégée (désactivation préférable à la suppression physique).

## ASSOCIATION : UTILISATEUR_APPARTIENT_EQUIPE (via MembreEquipe)

Entité A : Utilisateur
Entité B : Equipe
Sens métier : composition des équipes de travail.
Règles :
* Un utilisateur peut appartenir à plusieurs équipes (point à valider).
* Le rôle au sein de l'équipe est tracé (CHEF / OUVRIER).
* Un utilisateur peut être membre d'une équipe sur plusieurs chantiers via les affectations.

## ASSOCIATION : EQUIPE_PARTICIPE_CHANTIER (via AffectationEquipeChantier)

Entité A : Equipe
Entité B : Chantier
Sens métier : une équipe travaille sur un ou plusieurs chantiers sur des périodes définies.
Règles :
* Une équipe peut être affectée à plusieurs chantiers (multi-chantiers).
* Un chantier peut recevoir plusieurs équipes.
* L'affectation est bornée par une période (dateDebut / dateFin) et un statut (ACTIVE / TERMINEE).
* Cette relation est la base de la **sécurité des données** : l'utilisateur ne voit que les chantiers de ses équipes affectées.

## ASSOCIATION : UTILISATEUR_RESPONSABLE_CHANTIER

Entité A : Utilisateur
Entité B : Chantier
Sens métier : responsabilité d'un chantier (sans attribut `responsable`).
Règles :
* Un chantier peut avoir un ou plusieurs responsables (point à valider).
* Un utilisateur peut être responsable de plusieurs chantiers.
* Le responsable dispose de la gestion du chantier et de sa visibilité complète sur ce chantier.
* La cardinalité exacte sera formalisée au LOOP 2.4.

---

# 3. MODULE TÂCHES

## ASSOCIATION : TACHE_POSSEDE_AFFECTATION (via AffectationTache)

Entité A : Tache
Entité B : Utilisateur **ou** Equipe
Sens métier : affectation d'une tâche à un exécutant.
Règles :
* Au moins une cible obligatoire : utilisateur OU équipe (ou les deux).
* Le rôle est tracé (REALISATEUR / CONTROLEUR).
* Une tâche peut avoir plusieurs affectations (plusieurs exécutants/contrôleurs).
* La vue « mes tâches » et la visibilité (sécurité des données) découlent de cette relation.

## ASSOCIATION : TACHE_POSSEDE_VALIDATION (via ValidationTache)

Entité A : Tache
Entité B : Utilisateur (validateur)
Sens métier : contrôle qualité d'une tâche terminée.
Règles :
* La validation est réalisée par un utilisateur autorisé (permission TACHE_VALIDATE ou rôle CONTROLEUR).
* Une tâche peut être soumise à plusieurs validations (historique des décisions VALIDE / REFUSE).
* Le statut de la tâche évolue : TERMINE → VALIDE / REFUSE selon la dernière décision.
* Peut-on valider sa propre tâche ? — point à valider.

## ASSOCIATION : TACHE_POSSEDE_COMMENTAIRE

Entité A : Tache (ou Chantier)
Entité B : Commentaire
Sens métier : échanges textuels autour d'une tâche (ou d'un chantier).
Règles :
* Un commentaire appartient à un auteur (Utilisateur).
* Il porte sur une tâche OU un chantier (au moins une référence obligatoire).
* Lecture soumise à la sécurité des données.

## ASSOCIATION : CHANTIER_POSSEDE_PLANNING

Entité A : Chantier
Entité B : Planning
Sens métier : organisation temporelle des travaux d'un chantier.
Règles :
* Un chantier peut avoir plusieurs plannings (types : GLOBAL, HEBDOMADAIRE, MENSUEL).
* Une tâche peut être rattachée à un planning (relation complémentaire Tache → Planning).
* Le planning structure l'avancement sans créer de lien de dépendance obligatoire entre tâches.

---

# 4. MODULE DOCUMENTS

## ASSOCIATION : CHANTIER_POSSEDE_DOCUMENT

Entité A : Chantier
Entité B : Document
Sens métier : documents liés au chantier (contrats, plans, devis...).
Règles :
* Un document est déposé par un utilisateur (uploader).
* Le rattachement à un chantier est possible (optionnel).
* L'accès aux documents suit la sécurité des données.

## ASSOCIATION : DOCUMENT_POSSEDE_PIECE_JOINTE — NON RETENUE

Entité A : Document
Entité B : PieceJointe
Sens métier : à analyser.
Règles / décision :
* Dans le dictionnaire v1.2, **PieceJointe est polymorphique** (entiteType =
  TACHE / COMMENTAIRE / VALIDATION).
* Le rattachement d'une PieceJointe à un Document n'est **pas prévu** : le
  Document porte directement son fichier (`cheminFichier`).
* Si un besoin apparaît, il sera traité comme proposition à valider (extension
  de `entiteType` ou module dédié).

## ASSOCIATION : CHANTIER_POSSEDE_PHOTO (via PhotoChantier)

Entité A : Chantier
Entité B : PhotoChantier
Sens métier : photos terrain d'avancement.
Règles :
* Une photo est prise par un utilisateur (auteur) et rattachée à un chantier.
* Le type (AVANT / PENDANT / APRES) permet le suivi visuel de l'avancement.
* Accès soumis à la sécurité des données.

---

# 5. MODULE TRAÇABILITÉ

## ASSOCIATION : UTILISATEUR_GENERE_HISTORIQUE (via HistoriqueAction)

Entité A : Utilisateur
Entité B : HistoriqueAction
Sens métier : trace de toutes les actions sensibles.
Règles :
* Toutes les actions sensibles sont tracées : création/modification/suppression, changements RBAC, connexions, validations.
* L'objet concerné est identifié par typeEntite + entiteId.
* L'historique est immuable (aucune modification/suppression).
* Les actions système (sans utilisateur connecté) utilisent un compte technique (point à valider).

## ASSOCIATION : UTILISATEUR_POSSEDE_JOURNAL_CONNEXION (via JournalConnexion)

Entité A : Utilisateur
Entité B : JournalConnexion
Sens métier : historique des connexions et déconnexions.
Règles :
* Chaque LOGIN / LOGOUT est tracé avec son résultat (SUCCES / ECHEC).
* Les échecs sont tracés (sécurité).
* Une durée de conservation est à définir (règle d'archivage).

---

# 6. RELATIONS COMPLÉMENTAIRES (issues du dictionnaire, hors liste imposée)

* **Utilisateur — crée — Tache** : `createdBy` (créateur de la tâche).
* **Utilisateur — dépose — Document** : `uploader`.
* **Utilisateur — photographie — PhotoChantier** : `utilisateur` (auteur).
* **Utilisateur — écrit — Commentaire** : `auteur`.
* **Utilisateur — valide — Tache** : `validateur` (via ValidationTache).
* **Tache — rattachée à — Planning** : `planning` (FK Tache → Planning).
* **Chantier — reçoit — Commentaire** : commentaires portant sur le chantier.

---

# 7. RELATIONS NON RETENUES

| Association | Motif |
|-------------|-------|
| Document → PieceJointe | PieceJointe polymorphique (TACHE/COMMENTAIRE/VALIDATION) ; Document porte son fichier directement |

---

# 8. POINTS NÉCESSITANT VALIDATION

1. Un utilisateur peut-il appartenir à **plusieurs équipes** ?
2. Un chantier a-t-il **un ou plusieurs responsables** ?
3. Un utilisateur peut-il **valider sa propre tâche** ?
4. Faut-il une contrainte de **chef d'équipe unique** par équipe (MembreEquipe.roleDansEquipe) ?
5. Compte **technique** pour les actions système dans HistoriqueAction ?
6. Durée de **conservation** du JournalConnexion et de l'HistoriqueAction ?

---

FIN DU DOCUMENT
