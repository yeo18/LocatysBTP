# Stratégie de migrations — CMS (Flyway)

Version : 0.1
Statut : Active

---

# 1. Principe

Flyway est le **système officiel et unique** de gestion de la structure de la base de données.

- Aucune modification manuelle de la base.
- Aucune création automatique par Hibernate (`ddl-auto: none`).
- Toute évolution de structure passe par un script dans `db/migration/`.

---

# 2. Convention de nommage

```
V<numero>__<description>.sql
```

Exemples :

```
V1__init.sql
V2__create_utilisateur.sql
V3__create_profil.sql
V4__create_permission.sql
V5__create_profil_permission.sql
V6__create_chantier.sql
V7__create_tache.sql
V8__create_equipe.sql
V9__create_historique_action.sql
```

Règles :
- Version incrémentale (`V1`, `V2`, `V3`, ...).
- Double underscore `__` entre le numéro et la description.
- Description en minuscules avec underscores.
- Ne jamais modifier un script déjà appliqué (créer un V(x+1) à la place).

---

# 3. Règles de création d'une migration

Chaque migration doit être :
- **Atomique** : une migration = une évolution logique.
- **Réversible si possible** : penser à l'action inverse (DROP) même si non appliquée automatiquement.
- **Documentée** : en-tête de commentaire expliquant le but.
- **Testée** : validée sur l'environnement dev avant mise en prod.

Structure type d'un fichier :

```sql
-- ============================================================
-- MIGRATION V2 : Création table utilisateur
-- ============================================================
-- Description du besoin et des règles métier.
-- ============================================================

CREATE TABLE utilisateur (...);
```

---

# 4. Configuration technique

| Paramètre | Valeur | Rôle |
|-----------|--------|------|
| `spring.flyway.enabled` | true | Actif |
| `spring.flyway.locations` | classpath:db/migration | Emplacement des scripts |
| `spring.flyway.encoding` | UTF-8 | Encodage des scripts |
| `spring.flyway.validate-on-migrate` | true | Validation des migrations appliquées |
| `spring.flyway.baseline-on-migrate` | false | Interdiction d'initialiser sans historique |

Environnements : dev, test, prod — tous actifs.

---

# 5. Interaction avec Hibernate

- `spring.jpa.hibernate.ddl-auto: none`
- Hibernate ne crée, ne modifie ni ne supprime jamais de table.
- Le schéma est entièrement piloté par Flyway.
- Les Entity JPA (créées en Phase 2) doivent correspondre au schéma produit par les migrations.

---

# 6. Table d'historique

Flyway maintient automatiquement `flyway_schema_history` :
- version, description, type, script, checksum, succès.
- Utilisée pour valider les migrations au prochain démarrage.
- Ne jamais la modifier ni la supprimer manuellement.

---

# 7. Bonnes pratiques

- Vérifier après chaque migration : `SELECT * FROM flyway_schema_history;`
- Tester le démarrage après ajout d'une migration.
- Une migration doit rester idempotente et reproductible.
- En cas d'erreur Flyway au démarrage, corriger le script fautif et créer un nouveau numéro de version.

---

FIN DU DOCUMENT
