# PLAN D'OR — STRUCTURE DU REPO GIT (CMS)

> Objectif : dépôt GitHub **impeccable**, lisible par le maître de stage.
> Ce fichier liste EXACTEMENT ce qui doit apparaître sur Git et ce qui doit être exclu.

---

## 1. ARBRE CIBLE (ce qui doit apparaître sur Git)

```
CMS/                                  <- racine du dépôt (https://github.com/<toi>/CMS)
│
├── .gitignore                        <- OBLIGATOIRE (voir §4)
├── .env.example                      <- OBLIGATOIRE (place du .env, voir §5)
├── README.md                         <- OBLIGATOIRE (voir §6)
├── start.ps1                         <- script de démarrage (garder)
│
├── backend/                          <- Spring Boot 3 + Maven + Flyway
│   ├── pom.xml
│   ├── .gitignore
│   └── src/
│       ├── main/java/com/cms/        <- code (auth, analyse, chantier, equipe,
│       │                                permission, profil, security, tache,
│       │                                template, utilisateur, common, config)
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── application-dev.yml
│       │   ├── application-test.yml
│       │   ├── application-prod.yml
│       │   ├── db/migration/         <- V1__init.sql ... V22 (Flyway)
│       │   ├── messages/
│       │   ├── static/
│       │   └── templates/
│       └── test/java/com/cms/        <- 50+ tests unitaires/integration
│
├── frontend/                         <- React 19 + Vite + TS + Tailwind 4
│   ├── package.json
│   ├── package-lock.json             <- GARDE pour reproductibilite npm install
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── index.html
│   ├── .env.example                  <- au lieu de .env
│   └── src/
│       ├── main.tsx / App.tsx / index.css
│       ├── core/        (axios, store, composants, lib, utils)
│       ├── analyse/ auth/ chantiers/ dashboard/ equipes/ profil/
│       ├── stats/ taches/ utilisateurs/
│       └── api/ (...)
│
├── docker/
│   └── docker-compose.yml            <- À CRÉER (postgres + pgadmin), voir §7
│
└── docs/
    ├── uml/                          <- diagrammes (png/svg/puml)
    └── *.md                          <- 35 documents d'architecture/conception
```

**Décisions de rangement :**
- `MASTER_PLAN.md` et `ANALYSE_FRONTEND.md` → **déplacer sous `docs/plans/`** pour garder la racine nette (2 fichiers en moins à la racine).
- `docker/searxng/` → **obsolete** (le module Analyse n'utilise plus SearXNG, cf. `start.ps1` ligne 64). **À supprimer.**

---

## 2. CE QUE L'ON COMMIT — LISTE VERTE ✅

| Élément | Raison |
|---|---|
| Tout le code Java sous `backend/src/main` et `backend/src/test` | Cœur du projet |
| `backend/pom.xml`, `backend/.gitignore` | Build Maven reproductible |
| Toutes les migrations `V1__...V22__*.sql` | Base de données reproductible |
| `application.yml` + 3 profils (dev/test/prod) | Config par environnement (secrets = variables d'env) |
| Tout `frontend/src`, `package.json`, `package-lock.json`, `tsconfig.json`, `vite.config.ts`, `index.html` | Cœur du frontend |
| `start.ps1` | Script de démarrage documenté |
| `docs/**` (35 .md + UML) | Documentation = argument de soutenance |
| `README.md`, `.gitignore`, `.env.example` | Professionnalisme du dépôt |
| `docker-compose.yml` (à créer) | Infrastructure reproductible |

---

## 3. CE QUE L'ON N'ENTRE JAMAIS — LISTE ROUGE ❌

| Élément | Pourquoi |
|---|---|
| `frontend/node_modules/` (~300 Mo) | Dépendances npm, regénérées par `npm install` |
| `frontend/dist/` | Build de production, généré par `npm run build` |
| `backend/target/` (~100 Mo) | Artefacts Maven + logs, générés par `mvn` |
| `frontend/vite-dev.log`, `vite-dev.log.err` | Logs locaux |
| `backend/target/*.log` (cms-backend.log...) | Logs locaux |
| `.idea/` et `.idea/**` | Config locale de ton IDE (IntelliJ) |
| `*.iml` | Fichier local IntelliJ |
| `.context/` (template-summary.md) | Contexte interne de l'IA, pas du produit |
| `frontend/.env` | Config locale (remplacée par `.env.example`) |
| `backend/src/main/resources/**/application-dev.yml` secret inclusions | Dev-only, remplacée par var d'env |

⚠️ Le `application-dev.yml` contient un **JWT secret de dev en dur** (`8f2e4d6b...`). C'est acceptable en dev, mais rappelle-toi : en prod c'est forcé via `JWT_SECRET` et `DB_PASSWORD` (déjà le cas dans `application-prod.yml` ✅). Aucun secret de production n'existe dans le projet → bonne pratique déjà respectée.

---

## 4. LE `.gitignore` À COPIER À LA RACINE

```
# IDE
.idea/
*.iml
.vscode/

# Backend
backend/target/
*.class
*.log

# Frontend
frontend/node_modules/
frontend/dist/
frontend/.env
frontend/vite-dev.log
frontend/vite-dev.log.err

# Contexte IA / divers
.context/
.DS_Store
```

> Le `backend/.gitignore` existant est déjà bon → on le garde tel quel.

---

## 5. FICHIER `.env.example` (à la place de `.env`)

Créer `frontend/.env.example` :
```
# Copier ce fichier vers .env puis ajuster si besoin
VITE_API_URL=http://localhost:8091
```

---

## 6. `README.md` — structure recommandée (maître de stage = première impression)

```markdown
# CMS — Gestion de Chantiers
Système complet (Spring Boot + React) pour la gestion de chantiers :
utilisateurs, équipes, tâches, RBAC dynamique, analyse de site (météo, OSM).

## 🏗 Stack
- Backend : Java 25, Spring Boot 3, Maven, PostgreSQL, Flyway, JWT, Swagger
- Frontend : React 19, TypeScript, Vite 7, Tailwind 4, Redux, Recharts

## 📁 Structure
- backend/  → API REST Spring Boot
- frontend/ → SPA React + Vite
- docker/   → Infrastructure (Postgres, pgAdmin)
- docs/     → Conception, architecture, UML

## 🚀 Démarrage rapide
1. Démarrer l'infra : `.\start.ps1`   (ou `docker compose up -d` dans docker/)
2. Backend : `cd backend && mvn spring-boot:run` → http://localhost:8091
3. Frontend : `cd frontend && npm install && npm run dev` → http://localhost:3000

## 🗄 Base de données
- Migrations Flyway : backend/src/main/resources/db/migration/
- pgAdmin : http://localhost:5050 (admin@admin.com / admin)

## ✅ Tests
`cd backend && mvn test`  → 50+ tests (unitaires + intégration)

## 📚 Documentation
- Architecture : docs/architecture.md
- API/REST : docs/controllers-rest-api.md, docs/swagger-documentation.md
```

---

## 7. `docker-compose.yml` À CRÉER (sous `docker/`)

Le dossier `docker/` est vide de tout conteneur utile → tu dois fournir l'infra reproductible pour que le maître de stage puisse relancer sans avoir tes conteneurs `chantier-db`/`batiflow-db`/`pgadmin4` existants :

```yaml
services:
  db:
    image: postgres:16
    container_name: chantier-db
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
      POSTGRES_DB: gestion_de_chantier
    ports:
      - "5432:5432"

  pgadmin:
    image: dpage/pgadmin4
    container_name: pgadmin4
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    depends_on:
      - db
```

---

## 8. PROCEDURE D'INITIALISATION (à suivre dans l'ordre)

```powershell
# 1. Nettoyage local (n'envoie jamais ces dossiers)
Remove-Item -Recurse -Force "frontend\node_modules", "frontend\dist", "backend\target", "docker\searxng", ".context"

# 2. Ranger les 2 gros docs à la racine
New-Item -ItemType Directory -Path "docs\plans"
Move-Item "MASTER_PLAN.md", "ANALYSE_FRONTEND.md" "docs\plans\"

# 3. Créer .gitignore racine, .env.example, README.md, docker-compose.yml
#    (contenus fournis §4, §5, §6, §7)

# 4. Initialiser le dépôt
git init -b main

# 5. Vérifier que la liste rouge est bien ignorée
git status            # node_modules, target, dist, .env NE doivent PAS apparaitre

# 6. Premier commit propre
git add .
git commit -m "chore: initialisation du depot CMS (backend, frontend, docs, docker)"

# 7. Lier et pousser sur GitHub (repo PRIVE cree en amont)
git remote add origin https://github.com/<toi>/CMS.git
git branch -M main
git push -u origin main
```

---

## 9. STRATÉGIE DE BRANCHES (propre pour le suivi de stage)

```
main            → version stable, protégée
├── develop     → intégration
│   ├── feature/module-taches
│   ├── feature/rbac-dynamique
│   └── fix/...
```

**Règles :**
- Jamais de commit direct sur `main` ; toujours via Pull Request.
- Un module = une branche = une PR documentée pour le maître de stage.
- Conventions de commit : `feat(chantiers): ...`, `fix(rbac): ...`, `docs(uml): ...`, `test(security): ...`.

---

## 10. CHECKLIST FINALE AVANT POUSSÉE ✅

- [ ] `git status` ne montre **aucun** `node_modules/`, `target/`, `dist/`, `.env`, `*.log`, `.idea/`
- [ ] `README.md` complet (stack, structure, démarrage, tests)
- [ ] `.gitignore` racine présent
- [ ] `.env.example` présent (pas de `.env` commité)
- [ ] `docker-compose.yml` créé pour reproduire l'infra
- [ ] Tous les `application-*.yml` sans secret réel (uniquement `${VAR_ENV}`)
- [ ] Migrations Flyway V1→V22 présentes
- [ ] Docs et UML versionnés
- [ ] Repo GitHub **privé** (un projet de stage ne doit pas être public sans autorisation)
- [ ] Premier commit : "chore: initialisation..."
```