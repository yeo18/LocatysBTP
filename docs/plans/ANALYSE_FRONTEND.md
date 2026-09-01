# Analyse Reverse-Engineering — Frontend BatiFlow ERP (CMS)

> Cible analysée : `C:\Users\tcher\Desktop\CMS\frontend`
> Type : application web **SPA React** (démo front-only, données en mémoire + localStorage)
> Langue de l'UI : **français** (tout le contenu, toutes les dates, tous les formats)
> Date de l'analyse : 08/08/2026

---

## 1. Vue d'ensemble

Application de **gestion de chantiers BTP** (ERP léger) : chantiers, tâches, équipes, utilisateurs, habilitations, documents, rapports, templates, statistiques. 

**Stack exacte (package.json / vite.config.ts / index.html) :**
| Techno | Version | Usage |
|---|---|---|
| React | 19.2.6 | UI |
| react-dom | 19.2.6 | |
| Vite | 7.3.2 | Build |
| TypeScript | 5.9.3 | Langage |
| tailwindcss | 4.1.17 + `@tailwindcss/vite` | Styling (tokens via `@theme inline`) |
| react-router-dom | ^7.18.2 | Routage (`HashRouter`) |
| framer-motion | ^13.0.0 | Animations |
| lucide-react | ^1.28.0 | Icônes |
| recharts | ^3.10.1 | Dépendance (3 graphiques maison SVG la remplacent) |
| react-toastify | ^11.1.0 | Notifications |
| clsx + tailwind-merge | 2.1.1 / 3.4.0 | `cn()` |
| vite-plugin-singlefile | 2.3.0 | Bundle en un seul fichier |

- Alias `@` → `src` (déclaré mais non utilisé dans le code, les imports sont relatifs).
- Police : **Inter** 400/500/600/700/800/900 (Google Fonts), `<html lang="fr">`, title « BatiFlow ERP — Gestion de chantiers ».
- Persistance : **localStorage** — clés `batiflow-theme`, `batiflow-auth` (id user), `batiflow-users`. Données métier (chantiers/tâches/…) = seed en mémoire uniquement.
- Auth : compte admin hardcodé `admin@example.com` / `admin123`. Inscription libre (profil 5 « Utilisateur »).

---

## 2. Layout global

```
┌─────────────────────────────────────────────────────────────┐
│ SIDEBAR (fixed, w-64, #0f172a)  │  HEADER (sticky, glass)    │
│                                 │                            │
│  logo + nav 3 groupes           │  search (md+)  [thème][user]│
│  user + déconnexion en bas      ├────────────────────────────┤
│                                 │  <main> max-w-[1400px]     │
│                                 │  px-4 sm:px-6 lg:px-8      │
│                                 │  py-6                      │
│                                 │  animate page on change    │
└─────────────────────────────────┴────────────────────────────┘
```
- Conteneur racine : `min-h-screen bg-app`.
- Sidebar desktop : `fixed inset-y-0 left-0 hidden w-64 bg-[#0f172a] lg:block` (fond bleu nuit fixe, **même en light**).
- Contenu : `lg:pl-64`, header `sticky top-0 z-40 border-b border-line glass`.
- Main : `mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:px-8`.
- Transition de page : `motion.div` keyed sur `location.pathname`, `opacity 0→1`, `y 12→0`, 0.28s easeOut.
- Mobile : sidebar devient **drawer** (w-72) avec overlay `bg-slate-900/60 backdrop-blur-sm`, ouverture spring (damping 30, stiffness 320).

---

## 3. Header (layout.tsx:148)

- Hauteur **h-16**, `sticky top-0 z-40 border-b border-line glass` (fond `rgba(255,255,255,0.72)` light / `rgba(15,23,42,0.6)` dark + `backdrop-filter: blur(14px)`).
- **Bouton menu** burger (`Menu` h-5 w-5) visible `lg:hidden` → ouvre le drawer.
- **Search bar** : `hidden md:block`, max-w-md, icône `Search` à gauche, placeholder « Rechercher un chantier, une tâche… ». Au submit → `navigate('/chantiers?q=...')`.
- **Côté droit** (ml-auto) : bouton thème (`Sun`/`Moon` selon thème), puis **carte utilisateur** : Avatar 32px + nom (`text-sm font-bold text-ink`) + sous-texte « N permission(s) » (`text-[11px] text-muted`). Style : `rounded-xl border border-line bg-surface py-1.5 pl-1.5 pr-3 hover:bg-app`. Nom masqué `hidden sm:block`.

---

## 4. Sidebar (layout.tsx:76-146)

- **Brand** : logo casque `HardHat` dans carré `h-10 w-10 rounded-xl bg-amber-500/15 text-amber-400 ring-1 ring-amber-500/30`, puis `Bati` (blanc) `Flow` (ambre) en `font-black`, sous-titre `ERP chantier` `text-[11px] text-slate-400`. Padding `px-5 py-5`.
- **3 groupes de navigation** (libellés `text-[10px] font-bold uppercase tracking-wider text-slate-500`):
  | Groupe | Éléments | Icône (lucide, 18px) |
  |---|---|---|
  | **Pilotage** | Tableau de bord, Mes statistiques, Rapports | LayoutDashboard, BarChart3, FileText |
  | **Production** | Chantiers, Tâches, Équipes, Documents | Building2, CheckSquare, HardHat, FileText |
  | **Organisation** | Utilisateurs, Habilitations, Templates, Mon profil | Users, ShieldCheck, LayoutTemplate, UserCircle |
- Item actif : `bg-white/10 text-white` + icône `text-amber-400` ; inactif : `text-slate-400 hover:bg-white/5 hover:text-slate-200`, icône `text-slate-500`.
- Item : `rounded-xl px-3 py-2.5 text-sm font-semibold transition-all`, icône 18px, gap-3.
- **Pied de sidebar** : séparateur `border-t border-white/10 p-3`. Lien profil : Avatar 38px + nom (`text-sm font-semibold text-white`) + profil (`text-xs text-slate-400`). Bouton **Déconnexion** : `text-slate-400 hover:bg-rose-500/10 hover:text-rose-300`, icône LogOut 18px. Déconnexion → logout + `navigate('/login')`.
- Scrollbar de nav masquée (`.no-scrollbar`).

---

## 5. Sections / pages (routes — App.tsx)

Toutes sous `HashRouter`. Gardes : `RequireAuth` (redirige `/login` si non connecté), `AdminGuard` sur `/habilitations` (sinon page « Accès refusé »).

| Route | Page | Rôle |
|---|---|---|
| `/login`, `/register` | auth.tsx | Pages publiques hors layout |
| `/dashboard` (index) | dashboard.tsx | Tableau de bord |
| `/stats` | stats.tsx | Mes statistiques |
| `/rapports`, `/rapports/:id` | rapports.tsx | Liste + détail rapports |
| `/chantiers`, `/chantiers/:id` | chantiers.tsx | Liste + détail chantier (8 onglets) |
| `/taches`, `/taches/:id` | taches.tsx | Liste (grille/kanban) + détail tâche |
| `/equipes`, `/equipes/:id` | equipes.tsx | Liste + détail équipe |
| `/documents`, `/documents/:id` | documents.tsx | Liste + détail document |
| `/utilisateurs`, `/utilisateurs/:id` | users.tsx | Liste (cartes) + détail utilisateur |
| `/habilitations` | users.tsx | Profils / permissions / utilisateurs (admin) |
| `/templates`, `/templates/:id` | templates.tsx | Templates chantier/tâche |
| `/profil` | profile.tsx | Mon profil |
| `*` | — | Redirect `/dashboard` |

Chaque page (sauf auth/templates) suit le pattern : `PageHeader` + rangée de **mini-statistiques** (cartes) + zone principale.

**PageHeader** (ui.tsx:432) : titre `text-2xl font-black tracking-tight text-ink sm:text-[28px]`, sous-titre `text-sm text-muted`, actions à droite (`sm:flex-row sm:items-end sm:justify-between`, `mb-6`).

---

## 6. Cartes (ui.tsx:180)

`Card` : `rounded-2xl border border-line bg-card shadow-card`. Utilisée partout avec padding variable (`p-4`, `p-5`, `p-6`).

Motifs récurrents de **cartes statistiques** :
- Icône dans un carré `h-10 w-10 rounded-xl` teinté (light) : `bg-info-light text-info`, `bg-success-light text-success`, `bg-warning-light text-warning`, `bg-danger-light text-danger`, `bg-violet-100 text-violet-600` (dark : `bg-violet-500/15 text-violet-300`), idem indigo/cyan.
- Valeur `text-xl`/`text-2xl font-black text-ink`, libellé `text-xs text-muted`.
- Les **KPI du dashboard** sont des `Link` cliquables vers la page associée : `Card p-5 hover:-translate-y-0.5 hover:shadow-lg group relative overflow-hidden` + **pastille de dégradé décorative** `absolute -right-8 -top-8 h-28 w-28 rounded-full opacity-10 blur-2xl` avec le même gradient que l'icône. Icône 48px `rounded-xl bg-gradient-to-br text-white shadow-sm`. Badge tendance en haut à droite (`ArrowUpRight`/`ArrowDownRight`/`Minus` 12px) : up=`bg-success-light text-success`, down=`bg-danger-light text-danger`, flat=`bg-slate-100 text-slate-400`. Valeur `text-3xl font-black tracking-tight text-ink`.

---

## 7. Tableaux (ui.tsx:588 + chantiers.tsx:435)

Composant **`Table`** générique :
- `overflow-x-auto`, `minW` paramétrable (640 par défaut ; 720 documents, 820 rapports).
- thead : `border-b border-line text-left text-xs uppercase tracking-wide text-muted`, th `px-3 py-3 font-semibold` (dernier colonne `text-right`).
- tbody : `divide-y divide-line`, lignes `transition hover:bg-app/50`, td `px-3 py-3 text-body` (dernier `text-right`).
- Vide → `EmptyState`.

Tableau **chantiers** (chantiers.tsx:149) : identique mais avec cellules riches (icône 36px `rounded-lg bg-app text-accent` + nom `font-semibold text-ink group-hover:text-accent` + localisation avec `MapPin` 12px ; dates avec `CalendarDays` ; `Progress w-32 showLabel` ; actions : bouton ghost « Voir », boutons icône Pencil/Trash2).

**Pagination** (ui.tsx:629) : `Préc.`/`Suiv.` (Button secondary, `px-3 py-2`) + numéros `h-9 w-9 rounded-lg text-sm font-semibold`, page active `bg-accent text-white`, sinon `text-body hover:bg-app`, ellipses `…`. Masquée si `pages <= 1`.

---

## 8. Formulaires

**Field** (ui.tsx:149) : `<label class="block space-y-1.5">`, libellé `text-sm font-semibold text-ink` + `*` rouge si required, hint `text-xs text-muted`, erreur `text-xs font-medium text-danger`.

**TextInput / SelectInput / TextArea** (base `fieldBase`) :
```
w-full rounded-lg border border-line bg-surface px-3.5 py-2.5 text-sm text-ink
placeholder:text-muted transition focus:outline-none focus:border-accent
focus:ring-2 focus:ring-accent/30 disabled:opacity-60
```
- TextInput : + option `invalid` (bordure + ring danger).
- SelectInput : + `cursor-pointer appearance-none pr-9` (flèche native masquée ; aucune flèche custom).
- TextArea : `min-h-[90px] resize-y`.

**Modals** (ui.tsx:275) — le cœur du formulaire de création/édition :
- Overlay : `fixed inset-0 z-[100] bg-slate-900/60 backdrop-blur-sm`, fade.
- Panneau : bottom-sheet sur mobile (`items-end`, `rounded-t-3xl`), centré desktop (`sm:items-center sm:rounded-2xl`), `max-h-[92vh]`, spring (damping 26, stiffness 300), entrée `y:40 scale:0.98`, sortie `y:20`.
- Tailles : sm=`max-w-md`, md=`max-w-lg`, lg=`max-w-2xl`.
- Header : titre `text-lg font-bold text-ink` + sous-titre `text-sm text-muted`, bouton X (IconButton), `border-b border-line px-6 py-4`.
- Body : `max-h-[70vh] overflow-y-auto px-6 py-5`.
- Footer : `flex justify-end gap-2 border-t border-line bg-app/40 px-6 py-4`.
- Fermeture : `document.body.style.overflow = hidden` (nettoyé au unmount).

**ConfirmModal** : `size sm`, icône triangle `AlertTriangle` dans cercle `h-11 w-11 rounded-full bg-danger-light text-danger`, titre « Confirmer la suppression », boutons Annuler (secondary) + Supprimer (danger, icône alerte). Message = phrase de confirmation contextuelle.

**SearchBox** (ui.tsx:461) : input + icône loupe SVG inline 16px à gauche (`pl-9`).

**Formulaires de la page Templates** (templates.tsx) : style **distinct**, palette **slate/blue standard** (pas de tokens) : champs `border-slate-200 bg-slate-50 dark:border-slate-600 dark:bg-slate-900`, boutons `bg-blue-600`. *Volontairement différent du reste de l'app — à reproduire tel quel.*

---

## 9. Typographie

- Police : **Inter** (Google Fonts, 400→900). `--font-sans` par défaut.
- Hiérarchie :
  | Usage | Classe |
  |---|---|
  | Titre de page | `text-2xl sm:text-[28px] font-black tracking-tight text-ink` |
  | Titre de carte / section | `text-base font-bold text-ink` |
  | Valeur KPI | `text-3xl font-black tracking-tight text-ink` |
  | Valeur stat | `text-2xl font-black text-ink` |
  | Corps | `text-sm text-body` (muted : `text-sm text-muted`) |
  | Petits libellés | `text-xs text-muted` / `text-xs font-medium text-muted` |
  | Labels de champ | `text-sm font-semibold text-ink` |
  | En-têtes tableau | `text-xs uppercase tracking-wide text-muted font-semibold` |
  | Nav sidebar | `text-sm font-semibold` + groupes `text-[10px] font-bold uppercase tracking-wider` |
  | Badge tendance | `text-xs font-bold` |
  | Tooltip graphique | `text-[10px] font-semibold` |

---

## 10. Palette de couleurs (tokens — index.css)

**Light (`:root`) :**
| Token | HEX | Sémantique |
|---|---|---|
| `--bg-app` | `#eef2f7` | fond global |
| `--bg-surface` | `#ffffff` | inputs, surfaces |
| `--bg-card` | `#ffffff` | cartes |
| `--text-strong` (ink) | `#0f172a` | titres |
| `--text-base` (body) | `#475569` | texte courant |
| `--text-muted` | `#94a3b8` | texte atténué |
| `--border-color` (line) | `#e6e9ef` | bordures |
| `--accent` | `#1e3a5f` | bleu nuit (boutons primaires, liens) |
| `--accent-soft` | `#2d5483` | hover accent |
| `--glass` | `rgba(255,255,255,0.72)` | header |
| `--success` / light | `#10b981` / `#d1fae5` | |
| `--warning` / light | `#f59e0b` / `#fef3c7` | |
| `--danger` / light | `#ef4444` / `#fee2e2` | |
| `--info` / light | `#3b82f6` / `#dbeafe` | |

**Dark (`.dark`) :**
| Token | HEX |
|---|---|
| `--bg-app` | `#070d18` |
| `--bg-surface` | `#0f1a2e` |
| `--bg-card` | `#121f36` |
| `--text-strong` | `#f1f5f9` |
| `--text-base` | `#cbd5e1` |
| `--text-muted` | `#64748b` |
| `--border-color` | `#1e2d47` |
| `--accent` | `#5b8bc2` |
| `--accent-soft` | `#76a4d6` |
| `--glass` | `rgba(15,23,42,0.6)` |
| success/warning/danger/info | `#34d399`/`#fbbf24`/`#f87171`/`#60a5fa` |
| -light (dark) | `rgba(couleur, 0.16)` |

**Classes utilitaires mappées** (`@theme inline`) : `bg-app, bg-surface, bg-card, text-ink, text-body, text-muted, border-line, bg-accent, bg-accent-soft, bg-success, bg-success-light, bg-warning(-light), bg-danger(-light), bg-info(-light)`, ombre `shadow-card` : `0 1px 3px rgba(15,23,42,0.07), 0 14px 38px -18px rgba(15,23,42,0.2)`.

**Couleurs chart (hex bruts)** : statut À faire `#94a3b8`, En cours `#3b82f6`, Bloquée `#ef4444`, Terminée `#10b981`. Barres/aires : `#3b82f6` (dashboard statuts) ou `#1e3a5f` (avancement chantiers, tâches/chantier, stats). Sidebar : `#0f172a` (nuit fixe).

**Couleurs fixées sans token** : sidebar `#0f172a`, ambre logo (`text-amber-400`/`text-amber-500`, `bg-amber-500/15`, ring `amber-500/30`), violet/indigo/cyan/slate via Tailwind, page Templates en bleu Tailwind `blue-600`/`slate`.

---

## 11. Icônes

Bibliothèque **lucide-react**, stroke par défaut, tailles 12→32px. Icônes utilisées (grep complet) :
- **Navigation** : LayoutDashboard, BarChart3, FileText, Building2, CheckSquare, HardHat, Users, ShieldCheck, LayoutTemplate, UserCircle (18px sidebar).
- **Header** : Menu, Moon, Sun, Search, LogOut, X (drawer).
- **Dashboard** : Building2, CheckSquare, HardHat, Users, Zap, TrendingUp, ArrowUpRight, ArrowDownRight, Minus.
- **Chantiers** : Plus, Building2, MapPin, CalendarDays, Eye, Pencil, Trash2, ArrowLeft, Layers, FileText.
- **Tâches** : Plus, Eye, Pencil, Trash2, Check, CheckCircle2, Clock, Loader, PauseCircle, CircleDashed, CalendarDays, Coins, User, LayoutGrid, RefreshCw, X, ArrowLeft.
- **Équipes** : HardHat, Users2, UserMinus, Pencil, Plus, Trash2, ArrowLeft.
- **Utilisateurs** : Users, ShieldCheck, UserCircle, KeyRound, Pencil, Trash2, Plus, Check, ArrowLeft.
- **Documents** : FileText, Eye, Plus, Trash2, ArrowLeft.
- **Rapports** : FileBarChart, Eye, Plus, Trash2, ArrowLeft.
- **Templates** : Layers, ListChecks, CalendarClock, Coins, CheckSquare, PackagePlus, Rocket, Pencil, Plus, Trash2, Check, ArrowLeft.
- **Profile/Stats/Auth** : KeyRound, Save, Eye, EyeOff, BarChart3, Blocks, CheckCircle2, Coins, Flame, Timer, HardHat, ShieldAlert.
- **UI** : Loader2, AlertTriangle, Inbox, X.

Conventions : icônes d'action `h-4 w-4`, icônes de ligne `h-3 w-3`/`h-3.5 w-3.5`, icônes de stats `h-5 w-5`, grandes icônes de détail `h-7/h-8`, header `h-5 w-5`.

---

## 12. Boutons (ui.tsx:38-112)

`Button` : `inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition-all focus-visible:ring-2 ring-accent/40 ring-offset-app active:scale-[0.97] disabled:opacity-50`. Loading → icône `Loader2 animate-spin`.

| Variante | Style |
|---|---|
| primary (défaut) | `bg-accent text-white hover:bg-accent-soft shadow-sm` |
| secondary | `bg-surface text-ink border border-line hover:bg-app` |
| danger | `bg-danger text-white hover:opacity-90 shadow-sm` |
| success | `bg-success text-white hover:opacity-90 shadow-sm` |
| ghost | `text-body hover:bg-app hover:text-ink` |

Autres : **IconButton** (`h-9 w-9 rounded-lg text-muted hover:bg-app hover:text-ink`, scale 0.95), **LinkButton** (texte accent, `hover:text-accent-soft`, scale 0.97).

Actions de ligne type « Voir » : `Button variant="ghost" px-2 py-1.5 text-xs`. Actions icône : `rounded-lg p-2 text-muted hover:bg-app hover:text-accent` (modifier) / `hover:bg-danger-light hover:text-danger` (supprimer).

**Exception (page Templates)** : boutons `rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 active:scale-[0.97]`.

---

## 13. Badges / Pills / états

- **Badge** (ui.tsx:196) : `rounded-full px-2.5 py-0.5 text-xs font-semibold`, option `dot` (pastille `h-1.5 w-1.5`). Teintes via `toneBadge` (styles.ts:20) : slate/blue/amber/emerald/rose/violet/indigo/cyan.
- **PriorityBadge** : priorité → tone (Basse=slate, Moyenne=blue, Haute=amber, Urgente=rose), avec dot.
- **Mapping statut → tone** (styles.ts) :
  - Chantier : En cours=blue, Planifié/En attente/En pause=amber, Terminé/Livré=emerald, Annulé=rose.
  - Type chantier : Résidentiel=indigo, Commercial=cyan, Industriel=amber, Infrastructure=violet, Rénovation=emerald.
  - Tâche : En cours=blue, Terminée=emerald, Bloquée=rose, À faire=slate.
  - Doc : PDF=rose, Image=violet, Doc=blue, Tableur=emerald, Autre=slate.
  - Rapport : Rapport=blue, Avancement=indigo, Financier=emerald, Technique=amber, Autre=slate.
- **Pill** : `rounded-full px-3 py-1 text-sm font-semibold` + tone.
- **Filtres actifs** (tâches) : Pills avec bouton `X` h-3 w-3 pour retirer + bouton Réinitialiser (RefreshCw).
- **Pastilles de contexte** carte tâche : « À moi » (`bg-info-light text-info`), « Équipe » (`bg-teal-100 text-teal-600`, dark `bg-teal-500/15 text-teal-300`).
- **Chips permissions** : `rounded-full bg-app px-2 py-0.5 text-[10px]`, débordement `+N` en `bg-accent/10 text-accent`.
- **Progress** (ui.tsx:227) : rail `h-2 rounded-full bg-slate-100 dark:bg-white/10`, fill arrondi (tone : emerald/blue/info/slate/rose/violet/indigo/cyan — certains en dégradé), `transition-all duration-500`, libellé optionnel `%` à droite (`w-9 text-right text-xs font-bold`).

---

## 14. Graphiques (charts.tsx — SVG maison, pas recharts)

- **DonutChart** (default size 190, thickness 26) : cercle de fond `stroke-slate-100 dark:stroke-white/10`, rotation `-rotate-90`, segments en `strokeDasharray` (Linecap butt), centre = total (`text-3xl font-black`) + label « total » (`text-xs text-muted`). Légendes via `LegendDot` (pastille `h-2.5 w-2.5 rounded-full` + label + valeur `ml-auto font-semibold`).
- **BarChart** (default height 220, color `#3b82f6`) : barres `rounded-t-md`, max-width 46px, `min-height 4`, hauteur proportionnelle `(value/max)*(height-48)`, `transition-all duration-500 hover:opacity-90`, **tooltip hover** `bg-ink text-app text-[10px]` au-dessus de la barre, label sous la barre `text-[10px] line-clamp-1`.
- **AreaChart** (default height 180, color `#1e3a5f`) : SVG viewBox 560×height, gradient vertical de la couleur (0.35 → 0), ligne `stroke-width 2.5`, points `r 3.5`, `preserveAspectRatio="none"`.

---

## 15. Spacing (système Tailwind)

- Grilles de stats : `grid gap-4`, `grid-cols-1 sm:grid-cols-2 xl:grid-cols-4` (dashboard KPI), `lg:grid-cols-3`, `grid-cols-2 lg:grid-cols-6` (stats perso).
- Sections espacées de `mt-4`, pageHeader `mb-6`.
- Champs de formulaire : `space-y-4`, grille `grid-cols-1 sm:grid-cols-2 gap-4`, champs pleine largeur `sm:col-span-2`.
- Paddings de carte : `p-4` (dense), `p-5` (standard), `p-6/p-7/p-8` (formulaires/auth).
- Marges internes tableau : `px-3 py-3`.
- Espacement liste : `divide-y divide-line`, lignes `py-3`.
- Échelle utilisée : 1→4 (quart), 1.5, 2, 3, 4, 5, 6, 8, 11, 12.

---

## 16. Radius

- `rounded-2xl` : cartes, tableaux, containers.
- `rounded-lg` : boutons, inputs, badges icône, cellules.
- `rounded-xl` : cartes icône, liens nav sidebar, avatar logos, rows.
- `rounded-full` : badges, pills, avatars, pastilles, boutons ronds.
- `rounded-3xl` : banner chantier, cartes tâches, modal mobile.
- `rounded-t-2xl/rounded-t-3xl` : modals mobile.
- Barres : `rounded-md` (top des barres chart), `rounded-full` (rails progress).

---

## 17. Ombres

- `shadow-card` (token) : `0 1px 3px rgba(15,23,42,0.07), 0 14px 38px -18px rgba(15,23,42,0.2)` — toutes les cartes.
- `shadow-sm` : boutons primaires, avatars KPI.
- `shadow-lg` : hover KPI dashboard (`hover:shadow-lg`).
- `shadow-md` : hover cartes templates.
- `shadow-2xl` : drawer mobile.
- `shadow-xl` : modals templates.
- Élévation au survol des cartes : `hover:-translate-y-0.5`.

---

## 18. Responsive (breakpoints)

| Point | Comportement |
|---|---|
| **< lg (1024)** | Sidebar cachée → drawer + burger. Header sans search. |
| **md (768)** | Search bar visible (header). Grilles 2 colonnes. |
| **sm (640)** | Modals centrées (au lieu de bottom-sheet), paddings élargis. |
| **xl (1280)** | Grilles 4 (KPI) / 3 (cartes tâches) / 6 (stats). |
| Général | Tableaux `overflow-x-auto` + `min-w`. Cartes grille `grid-cols-1 sm:grid-cols-2 xl:grid-cols-3`. |

Auth : `grid min-h-screen lg:grid-cols-2` — panneau gauche branding `hidden lg:flex`, formulaire centré avec logo mobile `lg:hidden`.

---

## 19. Interactions

- **Boutons** : `active:scale-[0.97]`, focus ring accent.
- **Cartes cliquables** : `hover:-translate-y-0.5 hover:shadow-lg` (KPI, tâches).
- **Tableau** : ligne `hover:bg-app/50`, nom `group-hover:text-accent`.
- **Tâches** : changement de statut direct via select inline sur la carte ; bouton « Valider » (success) → statut Terminée.
- **Tabs** : indicateur animé `motion.div layoutId` (spring 400/28), `h-0.5` sous l'onglet actif, texte `text-accent`.
- **Modals** : bottom-sheet mobile / centrée desktop, spring, body scroll locké.
- **Drawer** : spring x:-100%→0.
- **Transition de page** : fade + slide y (0.28s).
- **Theme toggle** : classe `.dark` sur `<html>` + localStorage.
- **Search header** → `/chantiers?q=...` (chantiers.tsx lit `params.get("q")`).
- **Apply template** : importe les tâches d'un template dans un chantier + toast succès.
- **Login/Register** : simulation de latence `setTimeout 350ms` + spinner sur bouton.

---

## 20. États (loading / vide / erreur)

- **Chargement** : `FullSpinner` (`h-[60vh]`, anneau `border-[3px] border-amber-600 border-b-transparent` 36px, label « Chargement… ») ; spinner inline `h-5 w-5 border-2 border-current border-t-transparent` ; bouton loading = Loader2.
- **Vide** : `EmptyState` — `rounded-2xl border border-dashed border-line px-6 py-14`, icône `Inbox` 24px dans `h-14 w-14 rounded-2xl bg-app text-muted`, titre `font-semibold text-ink`, description `text-sm text-muted`, action optionnelle.
- **Erreur** : `ErrorState` — `bg-danger-light/40`, icône `AlertTriangle`. Auth : bandeau `border border-danger/30 bg-danger-light text-danger` (texte `font-semibold`).
- **Non trouvé (détail)** : `EmptyState title="… introuvable"` + bouton Retour.
- **Accès refusé** : page `AccessDenied` — carte centrée, icône `ShieldAlert` 32px ambre, bouton « Retour au tableau de bord ».

---

## 21. Design system / tokens

Récapitulatif des primitives CSS : `--bg-app`, `--bg-surface`, `--bg-card`, `--text-strong`, `--text-base`, `--text-muted`, `--border-color`, `--accent`, `--accent-soft`, `--glass`, `--success(-light)`, `--warning(-light)`, `--danger(-light)`, `--info(-light)`, `--shadow-card`, `--font-sans`. Classes utilitaires : `.glass` (blur 14px), `.no-scrollbar`, scrollbar custom 9px (`track` transparent, `thumb` `var(--text-muted)` radius full), override `.Toastify__toast` (font Inter, radius 0.75rem).

Le `@custom-variant dark` rend le mode sombre piloté par la classe `.dark` (et non `prefers-color-scheme`). Initialisation du thème dans AppProvider : localStorage → sinon `matchMedia(prefers-color-scheme: dark)`.

---

## 22. Arbre des composants React

```
<AppProvider>
  <HashRouter>
    <Routes>
      <Login/Register (auth.tsx: AuthShell>AuthLeft + formulaire)
      <RequireAuth> → <AppLayout (layout.tsx)>
        ├─ <SidebarNav> : Brand + NAV (3 groupes NavLink) + user/logout
        ├─ <Header> : menu, search, toggle theme, user chip
        └─ <main><Outlet/>
            ├─ <Dashboard> : Kpi ×4, DonutChart+Légendes, BarChart, AreaChart+Pills, TaskMiniList, mini-stats
            ├─ <MesStatistiques> : stat cards ×6, BarChart, filtres, InfoGrid
            ├─ <ChantiersList> : MiniStat ×4, SearchBox/Select, <Table>, <Pagination>, <Modal>, <ConfirmModal>
            ├─ <ChantierDetail> : banner photo, <Tabs> (8), <InfoGrid>, <TaskMiniList>, <TeamMiniList>, <SimpleTable>, timeline, <BarChart>, modal import template
            ├─ <TachesList> : stat cards, filtres+Pills, toggle Grille/Kanban, <TaskCard>×N, <Modal>, <ConfirmModal>
            ├─ <TaskDetail> : PriorityBadge/Badge, <InfoGrid>, checklist, actions rapides
            ├─ <EquipesList>/<EquipeDetail> : <Table>, <Avatar> empilés, modal
            ├─ <UsersList>/<UserDetail>/<Habilitations> : cartes user, <PermsModal>, matrice permissions, <ProfilFormModal>
            ├─ <DocumentsList>/<DocumentDetail>, <RapportsList>/<RapportDetail> : <Table> + <Modal>
            ├─ <TemplatesList>/<TemplateDetail> : onglets maison, cards slate/blue, modal custom
            └─ <MonProfil> : carte identité + formulaire + changement mdp
  <ToastContainer> (position top-right, autoClose 2800, theme light)
```

Sous-composants de `ui.tsx` réutilisés partout : `Button, IconButton, LinkButton, TextInput, SelectInput, TextArea, Field, Card, Badge, PriorityBadge, Progress, Avatar, Modal, ConfirmModal, EmptyState, ErrorState, PageHeader, Pill, SearchBox, Tabs, InfoGrid, InfoItem, MiniMetric, Table, Pagination, Spinner, FullSpinner`.

---

## 23. HTML sémantique

- `header` (sticky), `main`, `aside` (sidebar/drawer), `nav` (sidebar), `form` (search, auth), `label` (champs), `table/thead/tbody/tr/th/td`.
- Boutons partout (les `button` dans des `tr`, `label` cliquables pour checklist).
- Pas d'`<h2>` au-delà de `h1`/`h2`/`h3`/`h4` (h4 dans templates détail). `aria-label` sur IconButtons (Menu, Thème, Fermer).
- `lang="fr"`, `<title>`, meta viewport.
- Attributs `title` sur labels tronqués (barres chart, boutons icône silencieux).

---

## 24. Implémentation recommandée (pour reconstruction)

1. **Boilerplate** : Vite + React 19 + TS + Tailwind v4 (`@tailwindcss/vite`) + react-router-dom (HashRouter) + framer-motion + lucide-react + react-toastify + clsx/tailwind-merge.
2. **index.css** : copier les tokens light/dark + `@theme inline` + `.glass` + `.no-scrollbar` + scrollbar custom + override Toastify (cf. §10/§21).
3. **Ordre de construction** : `cn.ts` → `format.ts`, `styles.ts`, `types.ts` → `data.ts` → `ui.tsx` (toutes les primitives) → `charts.tsx` → `lists.tsx` → `AppProvider.tsx` → `layout.tsx` → pages (`auth`, `dashboard`, puis CRUD par entité).
4. **Conventions à respecter scrupuleusement** :
   - Toute carte = `rounded-2xl border border-line bg-card shadow-card`.
   - Tout icône de stat = carré `h-10 w-10 rounded-xl` teinté light/dark.
   - Toute valeur forte = `font-black text-ink` ; libellé = `text-xs text-muted`.
   - Tout formulaire = `Modal` + `Field` + `TextInput/SelectInput/TextArea` + footer Annuler/Créer.
   - Tableau = composant `Table` (minW, last col right).
   - Suppression = `ConfirmModal` ; feedback = toast.
   - Page = `PageHeader` + stats + contenu.
   - Sidebar/header = fidèle à layout.tsx (fond `#0f172a`, ambre logo, glass header).
   - Page Templates = style slate/blue distinct (cf. templates.tsx).
5. **Données** : répliquer le seed (6 chantiers, 14 tâches, 5 équipes, 8 users, 5 profils, 8 documents, 5 rapports, 4 templates chantier, 19 templates tâche, 6 historiques — cf. data.ts).
6. **Routage/accès** : RequireAuth + AdminGuard (habilitations), redirection `*`→dashboard.

---

## 25. Prompt de reconstruction (prêt à l'emploi)

> « Construis une SPA React (Vite + TypeScript + Tailwind v4 + react-router-dom HashRouter + framer-motion + lucide-react + react-toastify), entièrement en français, "BatiFlow ERP — Gestion de chantiers". Design system : tokens CSS light/dark (accent bleu nuit #1e3a5f / #5b8bc2, fond #eef2f7 / #070d18, carte #ffffff / #121f36, texte #0f172a / #f1f5f9, muted #94a3b8 / #64748b, bordures #e6e9ef / #1e2d47, success/warning/danger/info avec variantes light), ombre card, police Inter. Layout : sidebar fixe 256px fond #0f172a (3 groupes : Pilotage, Production, Organisation) + header glass sticky h-16 (search, toggle thème, carte user) + main max-w 1400px. Composants : Button (5 variantes), Card rounded-2xl shadow-card, Badge à 8 teintes avec dot, Progress, Avatar gradient, Modal bottom-sheet mobile, ConfirmModal, Table, Pagination, Tabs animés, PageHeader, Pill, SearchBox, EmptyState, DonutChart/BarChart/AreaChart SVG maison. Pages : login/register (panneau branding gauche), tableau de bord (KPI cliquables + 3 graphiques + activité récente), chantiers (liste filtrée paginée + détail 8 onglets avec banner photo), tâches (grille/kanban + détail + checklist), équipes, documents, rapports, utilisateurs (cartes + permissions + matrice d'habilitations admin), templates (style bleu slate distinct), mon profil. Auth mockée (admin@example.com/admin123), données seed en mémoire + localStorage pour thème/auth/users. »

---

## 26. Checklist fidélité

### Observé (prouvé dans le code)
- [x] Tokens CSS exacts light/dark, mapping Tailwind, `.glass`, `.no-scrollbar`, scrollbar 9px, override Toastify.
- [x] Couleurs sidebar `#0f172a`, logo ambre, header glass `blur(14px)`.
- [x] Composants UI complets (styles, variantes, radius, padding exacts).
- [x] Structure des 3 graphiques SVG (paramètres, couleurs, tooltips).
- [x] Navigation 3 groupes + icônes 18px + état actif ambre.
- [x] Routes + gardes + redirections.
- [x] Formatage fr-FR (dates `jj mois aaaa` / `jj/mm/aaaa`, nombres `toLocaleString('fr-FR')`, compacteur `Md/M/k`).
- [x] Mapping statuts/types/priorités → teintes.
- [x] Seed complet des données (noms sénégalais, budgets FCFA).
- [x] Auth admin hardcodée + inscription + localStorage.

### Estimé (inféré avec confiance haute)
- [~] Radius du shadow-card et valeurs de blur (repris du token, cohérent à l'écran).
- [~] Espacement vertical pageHeader (`mb-6`) partout, cohérent.
- [~] Couleur exacte des barres par défaut (`#3b82f6` / `#1e3a5f`).
- [~] Ordre de lecture des modals/footer (toujours Annuler à gauche, action à droite).

### Déduit (non visible directement)
- [?] Comportement de défilement vertical des pages longues (hérité du navigateur, main sans scroll custom).
- [?] Focus order / accessibilité clavier complet (les buttons natifs le fournissent).
- [?] Rendu exact des avatars dégradés (combinés `from-* to-*` par utilisateur).

### Non déterminable
- [✗] Il n'y a pas de maquette Figma/design référence externe — le design documenté ici EST la référence.
- [✗] Polices/fallback en cas de non-chargement Google Fonts (tombent sur ui-sans-serif/system-ui).
- [✗] Comportement offline (l'app est 100% locale, aucune API).

---

*Fin de l'analyse. Fichier source : `C:\Users\tcher\Desktop\CMS\frontend` — 100% des fichiers lus (App.tsx, main.tsx, index.css, index.html, vite.config.ts, store/AppProvider.tsx, components/{layout,ui,charts,lists}.tsx, lib/{types,styles,format,derive,data}.ts, utils/cn.ts, pages/{auth,dashboard,chantiers,taches,equipes,users,profile,stats,documents,rapports,templates}.tsx).*
