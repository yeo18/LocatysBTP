# Structure Frontend — CMS

Version : 0.2
Statut : Structure par module implémentée (backend + frontend)

---

# 1. Vue d'ensemble

* Framework : React 19 + TypeScript
* Build : Vite 7
* Gestion d'état : Context API (AppProvider) + TanStack Query
* Appels API : Axios
* Routage : React Router (HashRouter)

L'architecture frontend est calquée sur celle du backend : **un dossier par module
métier**, chacun contenant son API, ses pages et ses composants spécifiques.
Les éléments partagés (composants UI, formatage, styles, validation, client Axios)
vivent dans `core/`.

---

# 2. Arborescence actuelle

```
frontend/src/
├── main.tsx
├── App.tsx
├── index.css
├── core/                        ← partagé entre tous les modules
│   ├── api/
│   │       axios.ts             (instance Axios, injection du JWT, gestion 401)
│   │       token.ts             (localStorage)
│   │       types.ts             (types d'API partagés)
│   ├── components/
│   │       ui.tsx               (boutons, inputs, modales, badges, tableaux)
│   │       charts.tsx           (graphiques recharts)
│   │       lists.tsx            (listes de tâches)
│   │       layout.tsx           (Navbar, Sidebar, layout principal)
│   ├── lib/
│   │       format.ts
│   │       styles.ts            (couleurs/tones des statuts)
│   │       types.ts             (types métier)
│   │       validation.ts
│   ├── store/
│   │       AppProvider.tsx      (contexte auth/theme/permissions)
│   └── utils/
│           cn.ts
├── auth/
│   ├── api.ts                   (login, register, me, mot de passe)
│   └── pages.tsx                (Login, Register, AccessDenied)
├── chantiers/
│   ├── api.ts
│   └── pages.tsx                (ChantiersList, ChantierDetail)
├── equipes/
│   ├── api.ts
│   └── pages.tsx                (EquipesList, EquipeDetail)
├── taches/
│   ├── api.ts
│   └── pages.tsx                (TachesList, TaskDetail)
├── templates/
│   ├── api.ts
│   └── pages.tsx                (TemplatesList, TemplateDetail, TemplateTacheDetail)
├── utilisateurs/
│   ├── api.ts
│   ├── permissions.ts
│   ├── profils.ts
│   └── pages.tsx                (UsersList, UserDetail, Habilitations, ProfilDetail)
├── analyse/
│   ├── api.ts
│   ├── components/
│   │       analyseMap.tsx       (carte Leaflet)
│   └── pages.tsx                (AnalyseSitePage)
├── dashboard/
│   └── pages.tsx                (Dashboard)
├── stats/
│   └── pages.tsx                (MesStatistiques)
└── profil/
    └── pages.tsx                (MonProfil)
```

Règle : une page ne doit jamais importer un composant « par-dessus » son module
sauf via `core/`. Les composants spécifiques à un module restent dans son dossier
(ex. `analyse/components/analyseMap.tsx`).

---

# 3. Routes

| Route | Page | Accès |
|-------|------|-------|
| /login | Login | Public |
| /register | Register | Public |
| / | Dashboard | Authentifié |
| /dashboard | Dashboard | Authentifié |
| /stats | MesStatistiques | Authentifié |
| /chantiers | ChantiersList | CHANTIER_LIRE |
| /chantiers/:id | ChantierDetail | CHANTIER_LIRE |
| /analyse-site | AnalyseSitePage | CHANTIER_LIRE |
| /taches | TachesList | TACHE_LIRE |
| /taches/:id | TaskDetail | TACHE_LIRE |
| /equipes | EquipesList | EQUIPE_LIRE |
| /equipes/:id | EquipeDetail | EQUIPE_LIRE |
| /utilisateurs | UsersList | UTILISATEUR_LIRE |
| /utilisateurs/:id | UserDetail | UTILISATEUR_LIRE |
| /habilitations | Habilitations | ADMINISTRATEUR |
| /habilitations/profil/:id | ProfilDetail | ADMINISTRATEUR |
| /templates | TemplatesList | TEMPLATE_CHANTIER_LIRE |
| /templates/tache/:id | TemplateTacheDetail | TEMPLATE_TACHE_LIRE |
| /templates/:id | TemplateDetail | TEMPLATE_CHANTIER_LIRE |
| /profil | MonProfil | Authentifié |

## Gardes

* `ProtectedRoute` : redirige vers /login si non authentifié.
* `PermissionRoute` : vérifie la permission (ex. USER_CREATE) avant d'afficher.

---

# 4. Composants

* `core/components/ui.tsx` : boutons, champs de formulaire, modales, tableaux, badges de statut, états d'erreur.
* `core/components/layout.tsx` : barre de navigation (avec menu selon les permissions), layout principal.
* `core/components/charts.tsx` : graphiques (recharts).
* `core/components/lists.tsx` : listes de tâches réutilisées par plusieurs modules.
* Composants spécifiques d'un module : dans le dossier du module (ex. `analyse/components/analyseMap.tsx`).

---

# 5. Gestion d'état

* `AppProvider` (Context API, `core/store/AppProvider.tsx`) : utilisateur courant,
  token, permissions, thème.
* Les données serveur sont chargées via TanStack Query dans chaque module.

Règles :
* Le token JWT est stocké dans localStorage et injecté par l'intercepteur Axios
  (`core/api/axios.ts`).
* En cas de `401`, l'intercepteur déconnecte et redirige vers /login.

---

# 6. Communication API

* Instance Axios unique (`core/api/axios.ts`).
* Intercepteur de requête : ajoute `Authorization: Bearer <token>`.
* Intercepteur de réponse : si `401`, déconnexion + redirection vers /login.
* Chaque module expose ses appels via son propre fichier `api.ts`.

## Points de connexion avec le backend

| API backend | Fichier frontend |
|-------------|------------------|
| POST /api/v1/auth/login | auth/api.ts |
| GET/POST /api/v1/chantiers | chantiers/api.ts |
| GET/POST /api/v1/taches | taches/api.ts |
| GET/POST /api/v1/equipes | equipes/api.ts |
| GET/POST/PUT/DELETE /api/v1/users | utilisateurs/api.ts |
| GET/POST /api/v1/permissions | utilisateurs/permissions.ts |
| GET/POST /api/v1/profils | utilisateurs/profils.ts |
| GET/POST /api/v1/templates | templates/api.ts |
| GET /api/v1/analyse/* | analyse/api.ts |

---

# 7. Sécurité côté frontend

* Le frontend n'affiche les actions que si la permission est présente (UX).
* La sécurité réelle reste 100 % côté backend (le frontend cache seulement les éléments pour l'ergonomie).

---

FIN DU DOCUMENT
