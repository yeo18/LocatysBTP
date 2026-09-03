# LocatysBTP

Application de gestion de chantiers BTP : suivi des chantiers, des équipes, des tâches et des utilisateurs, avec authentification (JWT) et gestion fine des droits (RBAC).

- **Backend** : API REST **Spring Boot 3** (Java 25, Maven, PostgreSQL, Flyway pour les migrations, JWT pour la sécurité)
- **Frontend** : application web **React** (TypeScript, Vite, Tailwind CSS)

Ce guide vous accompagne de **A à Z** : de l'installation des outils jusqu'au lancement complet de l'application, sur Linux/macOS (bash) comme sur Windows (PowerShell).

---

## Sommaire

1. [Prérequis](#1-prerequis)
2. [Cloner le projet](#2-cloner-le-projet)
3. [Configuration](#3-configuration)
4. [Démarrer la base de données](#4-demarrer-la-base-de-donnees)
5. [Installer et lancer le backend](#5-installer-et-lancer-le-backend)
6. [Installer et lancer le frontend](#6-installer-et-lancer-le-frontend)
7. [Vérifier que tout fonctionne](#7-verifier-que-tout-fonctionne)
8. [Arrêter l'application](#8-arreter-lapplication)
9. [Dépannage](#9-depannage)

---

## 1. Prérequis

Installez les outils suivants avant de continuer, avec au minimum ces versions :

| Outil | Version requise | Vérifier avec |
|-------|-----------------|---------------|
| Git | n'importe quelle récente | `git --version` |
| Java (JDK) | **25** | `java -version` |
| Maven | **3.9+** | `mvn -version` |
| Node.js | **20+** | `node --version` |
| npm | inclus avec Node.js | `npm --version` |
| Docker | n'importe quelle récente | `docker --version` |

> La version de Java est importante : le projet compile avec Java 25. Si plusieurs versions de Java sont installées, assurez-vous que `java -version` affiche bien la version 25.

---

## 2. Cloner le projet

```bash
git clone https://github.com/yeo18/LocatysBTP.git
cd LocatysBTP
```

Vous devez obtenir cette structure :

```
LocatysBTP/
├── backend/        # API Spring Boot
├── frontend/       # application web React
├── docker/         # base de données PostgreSQL (Docker Compose)
├── README.md
└── start.ps1       # script de lancement rapide (Windows)
```

---

## 3. Configuration

Aucune configuration n'est obligatoire pour démarrer en local : le backend embarque des **valeurs par défaut de développement** (base `admin`/`admin123`, secret JWT de dev) qui correspondent à la base Docker Compose. Vous ne devez donc **rien créer** pour lancer l'application.

Pour personnaliser, ces **variables d'environnement** (définies dans votre terminal ou dans l'IDE) sont prises en compte :

| Variable | Rôle | Valeur par défaut |
|----------|------|-------------------|
| `DB_USERNAME` | Utilisateur PostgreSQL | `admin` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | `admin123` |
| `JWT_SECRET` | Clé de signature des tokens JWT | secret de dev (déjà présent, **à changer en production**) |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées pour le frontend | `http://localhost:3000,http://localhost:5173` |
| `OPENWEATHER_API_KEY` | Clé API OpenWeather (module météo) | vide = météo désactivée, **optionnel** |

### 3.1 Exemple de définition sur Linux/macOS

```bash
export DB_USERNAME=admin
export DB_PASSWORD=admin123
```

### 3.2 Exemple sur Windows (PowerShell)

```powershell
$env:DB_USERNAME = "admin"
$env:DB_PASSWORD = "admin123"
```

> **Optionnel — `frontend/.env`** : le frontend fonctionne sans aucun fichier `.env` (l'URL `http://localhost:8091` est la valeur par défaut). Si vous devez la changer, créez `frontend/.env` à côté de `package.json` :
>
> ```
> VITE_API_URL=http://localhost:8091
> ```
>
> `VITE_API_URL` est l'adresse du backend vue par le navigateur. Les fichiers `.env` sont ignorés par Git et ne seront jamais poussés.

---

## 4. Démarrer la base de données

La base PostgreSQL est fournie via Docker Compose (fichier `docker/docker-compose.yml`).

```bash
cd docker
docker compose up -d
```

Cela démarre deux conteneurs :

- **PostgreSQL** → port `5432`
  - base : `gestion_de_chantier`
  - utilisateur : `admin`
  - mot de passe : `admin123`
- **pgAdmin** (interface de gestion) → `http://localhost:5050`
  - email : `admin@admin.com`
  - mot de passe : `admin`

Vérifiez que la base est prête :

```bash
docker compose ps
```

Les deux conteneurs doivent être `Up`. La base `gestion_de_chantier` sera utilisée automatiquement par le backend.

> **Alternative sans Docker** : si vous avez déjà PostgreSQL installé, créez simplement une base vide nommée `gestion_de_chantier` avec un utilisateur `admin` / mot de passe `admin123` (ou adaptez les variables `DB_USERNAME` / `DB_PASSWORD` de l'étape 3).

---

## 5. Installer et lancer le backend

### 5.1 Installer les dépendances et compiler

```bash
cd backend
mvn clean package -DskipTests
```

En cas de succès, vous verrez `BUILD SUCCESS` (et le fichier `target/cms-backend-0.1.0.jar` sera créé).

### 5.2 Lancement (développement)

```bash
mvn spring-boot:run
```

Attendez le message de démarrage (`Started CmsApplication`). Le backend écoute alors sur **http://localhost:8091**.

Au premier démarrage, les **migrations Flyway** s'exécutent automatiquement : les tables sont créées dans la base `gestion_de_chantier`, il n'y a rien d'autre à faire.

> Le backend utilise ses valeurs par défaut de développement (base `admin`/`admin123`, aucun secret réel). Les variables d'environnement définies à l'étape 3 sont prises en compte si elles existent.

---

## 6. Installer et lancer le frontend

Ouvrez un **deuxième terminal**.

### 6.1 Installer les dépendances

```bash
cd frontend
npm install
```

### 6.2 Lancement

```bash
npm run dev
```

Le frontend démarre sur **http://localhost:3000**.

---

## 7. Vérifier que tout fonctionne

1. **Frontend** : ouvrez `http://localhost:3000` dans le navigateur.
2. **Créer un compte** : cliquer sur le lien d'inscription, saisir email + mot de passe, valider.
3. **Se connecter** : vous arrivez sur le tableau de bord.
4. **API en direct** : ouvrez `http://localhost:8091/swagger-ui.html` pour la documentation Swagger de l'API (elle liste tous les endpoints et leur format).
5. **Base de données** : ouvrez `http://localhost:5050` (pgAdmin) et connectez-vous avec `admin@admin.com` / `admin` pour voir les tables créées dans `gestion_de_chantier`.

Si le tableau de bord s'affiche après connexion : **l'application fonctionne de bout en bout** (frontend → API → base de données).

---

## 8. Arrêter l'application

- **Frontend** : `Ctrl+C` dans son terminal.
- **Backend** : `Ctrl+C` dans son terminal.
- **Base de données** : dans le dossier `docker` :

  ```bash
  docker compose down
  ```

Relancer plus tard : refaire simplement les étapes [4](#4-demarrer-la-base-de-donnees), [5.2](#52-lancement-developpement) et [6.2](#62-lancement) (la base de données conserve les données entre deux arrêts).

---

## 9. Dépannage

| Problème | Solution |
|----------|----------|
| `BUILD FAILURE` au build Maven | Vérifiez `java -version` (doit afficher la version 25) puis `mvn clean` avant de refaire `package` |
| Le backend ne démarre pas / erreur de connexion à la base | Vérifiez que Docker tourne et que `docker compose ps` affiche PostgreSQL `Up`, puis que `backend/.env` contient bien `admin` / `admin123` |
| `Error creating bean ... DataSource` | La base `gestion_de_chantier` n'existe pas : `docker compose up -d` la crée automatiquement ; sinon créez-la à la main |
| Le frontend affiche des erreurs réseau à la connexion | Vérifiez que le backend tourne sur `http://localhost:8091` et que `frontend/.env` (si créé) contient `VITE_API_URL=http://localhost:8091` |
| Migration Flyway en erreur | Supprimez les données de la base (`docker compose down` puis relancez `up -d`) ou consultez pgAdmin |
| Port déjà occupé | Par défaut : backend sur `8091`, frontend sur `3000` — arrêtez le programme qui occupe le port |

### Raccourci Windows

Sur Windows, le script **`start.ps1`** à la racine lance automatiquement Docker, la base et le backend :

```powershell
.\start.ps1
```

---

## Tests

Les tests du backend s'exécutent avec :

```bash
cd backend
mvn test
```

> Note : certains tests de l'API réelle utilisent Docker (Testcontainers) ; Docker doit donc être démarré.

---

> LocatysBTP — projet de gestion de chantiers. Ne déboguez jamais de secrets (`.env`, clés API, mots de passe) : ils doivent rester locaux, jamais commités.