# Méthodologie de travail — CMS

Version : 0.1
Statut : Documentation

---

# 1. Git

## Modèle de branches

```
main
 ├── develop
 │    ├── feature/*
 │    └── bugfix/*
```

| Branche | Usage |
|---------|-------|
| main | Version stable, déployable uniquement |
| develop | Intégration continue des développements |
| feature/* | Développement d'une fonctionnalité |
| bugfix/* | Correction d'un bug |

## Convention de commit

Format :

```
<type>(<module>): <description>
```

Types :

| Type | Usage |
|------|-------|
| feat | Nouvelle fonctionnalité |
| fix | Correction de bug |
| docs | Documentation |
| refactor | Refactorisation |
| test | Ajout/modification de tests |
| chore | Tâche technique (dépendances, config) |

Exemples :

```
feat(chantier): ajout endpoint création chantier
fix(tache): correction statut en double
docs(security): précision RBAC
```

---

# 2. Loop Engineering

Chaque intervention suit le cycle suivant :

```
┌────────────────┐
│    Analyse     │  lire MASTER_PLAN + docs, vérifier l'état actuel
└──────┬─────────┘
       ▼
┌────────────────┐
│  Modification  │  implémenter le LOOP, fichiers concernés
└──────┬─────────┘
       ▼
┌────────────────┐
│      Test      │  vérifier le fonctionnement (build, tests, API)
└──────┬─────────┘
       ▼
┌────────────────┐
│    Rapport     │  RAPPORT DE FIN DE LOOP obligatoire
└──────┬─────────┘
       ▼
┌────────────────────────┐
│ MAJ MASTER_PLAN.md     │  état d'avancement + résumé
└────────────────────────┘
```

## Règles avant toute modification

1. Lire MASTER_PLAN.md.
2. Vérifier l'état actuel du projet.
3. Identifier le LOOP concerné.
4. Expliquer les fichiers impactés.
5. Vérifier les risques.

## Règles après modification

Produire obligatoirement le RAPPORT DE FIN DE LOOP avec :
* LOOP réalisé (nom, numéro) ;
* objectif initial ;
* fichiers créés/modifiés ;
* opérations réalisées ;
* tests effectués ;
* ce qui fonctionne ;
* ce qui reste à faire ;
* problèmes rencontrés ;
* état du projet.

---

# 3. Cycle de vie d'une fonctionnalité

1. Créer la branche `feature/<nom>` depuis `develop`.
2. Développer selon les conventions (couches, DTO, tests).
3. Tester localement.
4. Merger vers `develop`.
5. Valider en recette.
6. Merger vers `main` pour la production.

---

# 4. Ordre d'implémentation (phases MASTER_PLAN)

* Phase 0 : Architecture (documentation) ✅
* Phase 1 : Socle Spring Boot (création projet, Maven, PostgreSQL, Swagger)
* Phase 2 : Modèle JPA (profil, permission, utilisateur, RBAC, modules, repository)
* Phase 3 : Utilisateur
* Phase 4 : JWT
* Phase 5 : RBAC dynamique
* Phase 6 : Gestion chantier
* Phase 7 : Gestion équipe
* Phase 8 : Gestion tâche
* Phase 9 : Audit historique
* Phase 10 : API finale
* Phase 11 : Tests qualité
* Phase 12 : Déploiement

---

FIN DU DOCUMENT
