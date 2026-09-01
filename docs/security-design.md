# Sécurité — CMS

Version : 0.1
Statut : Documentation (aucune implémentation)

---

# 1. Principe fondamental

> Un utilisateur voit uniquement les ressources auxquelles il a droit.

* Toutes les vérifications se font dans le backend.
* Le frontend ne doit jamais être considéré comme une sécurité.
* Les mots de passe sont stockés hachés (BCrypt), jamais en clair.

---

# 2. Authentification

## Connexion

* Endpoint public : `POST /api/v1/auth/login`
* L'utilisateur envoie email + mot de passe.
* Le backend vérifie les identifiants (BCrypt).

## JWT (JSON Web Token)

* À la connexion réussie, le backend émet un JWT signé.
* Le frontend envoie le token dans l'en-tête : `Authorization: Bearer <token>`
* Le token contient : sujet (id utilisateur), claims, date d'expiration.

## Expiration du token

* Access token : durée courte (ex. 30 min à 2 h selon le choix retenu).
* Après expiration, l'API renvoie `401 Unauthorized`.

## Refresh token

* Un refresh token (durée longue, ex. 7 à 30 jours) permet de renouveler l'access token sans reconnexion.
* Stocké côté serveur (table ou mécanisme dédié) et/ou cookie sécurisé.
* Le refresh token peut être révoqué (ex. déconnexion, compromission).

---

# 3. Autorisation — RBAC dynamique

## Principe

Les droits sont stockés en base de données et appliqués dynamiquement à chaque requête.

```
┌────────────┐
│ Utilisateur │
└──────┬─────┘
       │ appartient à
       ▼
┌────────────┐
│   Profil    │  (rôle métier)
└──────┬─────┘
       │ possède (via PROFIL_PERMISSION)
       ▼
┌────────────┐
│ Permission  │  (action autorisée)
└────────────┘
```

## Permissions exceptionnelles

Un utilisateur peut recevoir des permissions directes en plus de celles de son profil :

```
┌────────────┐
│ Utilisateur │
└──────┬─────┘
       │ possède (via UTILISATEUR_PERMISSION)
       ▼
┌────────────┐
│ Permission  │
└────────────┘
```

Union appliquée : `permissions_utilisateur = permissions(profil) ∪ permissions_directes`

## Exemples de permissions

| Code | Description |
|------|-------------|
| USER_CREATE | Créer un utilisateur |
| USER_VIEW | Voir les utilisateurs |
| USER_UPDATE | Modifier un utilisateur |
| USER_DELETE | Supprimer un utilisateur |
| CHANTIER_VIEW | Voir les chantiers |
| CHANTIER_CREATE | Créer un chantier |
| CHANTIER_UPDATE | Modifier un chantier |
| TACHE_VALIDATE | Valider une tâche |
| TACHE_ASSIGN | Assigner une tâche |
| PERMISSION_MANAGE | Gérer les permissions |

---

# 4. Règles métier d'accès

| Acteur | Accès |
|--------|-------|
| OUVRIER | Ne voit que ses propres tâches |
| CHEF_EQUIPE | Ne voit que ses chantiers et son équipe |
| ADMINISTRATEUR | Gère utilisateurs, permissions, tout le système |

## Cas particuliers

* Un chef d'équipe peut voir les tâches de son équipe (via l'équipe), même si les permissions globales ne le permettraient pas.
* Un utilisateur ne peut modifier/supprimer que ce que ses permissions autorisent.
* L'historique des actions est en lecture seule sauf pour administrateur.

---

# 5. Bonnes pratiques prévues

* Bcrypt pour le hachage des mots de passe.
* Secret JWT stocké dans la configuration (jamais dans le code source).
* Validation d'entrées sur toutes les API.
* Journalisation (audit) des actions sensibles.
* CORS configuré uniquement pour les origines autorisées.
* `@PreAuthorize` / annotations de sécurité sur les controllers.

---

FIN DU DOCUMENT
