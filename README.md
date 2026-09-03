# LocatysBTP

Application de gestion de chantiers BTP :
- **Backend** : API REST Spring Boot 3 (Java 25, Maven, PostgreSQL, Flyway, JWT)
- **Frontend** : application web React (TypeScript, Vite, Tailwind CSS)

---

## Prérequis

| Outil | Version |
|-------|---------|
| Git | n'importe quelle récente |
| Java | 25 (JDK) |
| Maven | 3.9+ |
| Node.js | 20+ (avec npm) |
| Docker | n'importe quelle récente (pour la base de données) |

Les exemples de commandes ci-dessous sont pour Windows (PowerShell) et Linux/macOS (bash).

---

## Cloner le projet

```bash
git clone https://github.com/yeo18/LocatysBTP.git
cd LocatysBTP
```

---

## Configuration

### 1) Base de données (PostgreSQL)

Une base PostgreSQL est requise. Deux options :

**Option A — Docker (recommandé)** : crée PostgreSQL + pgAdmin :

```bash
cd docker
docker compose up -d
```

Cela crée :
- PostgreSQL sur le port `5432`
  - base : `gestion_de_chantier`
  - utilisateur : `admin`
  - mot de passe : `admin123`
- pgAdmin sur `http://localhost:5050` (`admin@admin.com` / `admin`)

**Option B — PostgreSQL déjà installé** : crée une base vide nommée `gestion_de_chantier`.

### 2) Backend

Créer le fichier `backend/.env` (à partir de l'exemple fourni) :

```bash
# Ajuster si votre base n'est pas celle de Docker
DB_USERNAME=admin
DB_PASSWORD=admin123

# Secret de signature JWT (>= 32 caractères, à changer !)
JWT_SECRET=dev-secret-cms-2026-8f2e4d6b9c1a3e5f7d8b0c2a4e6f9a0b

# Optionnel : clé API OpenWeather pour le module d'analyse météo
OPENWEATHER_API_KEY=
```

### 3) Frontend

Créer le fichier `frontend/.env` :

```bash
VITE_API_URL=http://localhost:8091
```

### 4) Vérifier qu'aucun secret n'est versionné

```bash
git check-ignore backend/.env frontend/.env && echo "OK : .env bien ignorés"
```

---

## Installation

### Backend (dépendances Maven)

```bash
cd backend
mvn clean package -DskipTests
```

### Frontend (dépendances npm)

```bash
cd frontend
npm install
```

---

## Lancement

### 1) Base de données

```bash
cd docker
docker compose up -d
```

### 2) Backend

```bash
cd backend
mvn spring-boot:run
```

Le backend démarre sur `http://localhost:8091`. Les migrations de base de données (Flyway) s'appliquent automatiquement au premier démarrage.

### 3) Frontend

```bash
cd frontend
npm run dev
```

Le frontend démarre sur `http://localhost:3000`.

> Astuce : sur Windows, le script `start.ps1` à la racine lance Docker et le backend automatiquement.

---

## Vérification

1. **API** : ouvrir `http://localhost:8091/api/v1/auth/login` ou `http://localhost:8091/swagger-ui.html` (documentation Swagger).
2. **Application** : ouvrir `http://localhost:3000`, créer un compte, se connecter.
3. **Tests backend** : `cd backend && mvn test`.

---

## Arrêt

```bash
cd docker
docker compose down
```

Arrêter le backend : `Ctrl+C` dans son terminal. Arrêter le frontend : `Ctrl+C` dans son terminal.

---

## Déploiement

Le fichier `render.yaml` permet de déployer le projet sur [Render.com](https://render.com) (base PostgreSQL sur **Neon** ou Render, backend en Docker, frontend en site statique). Voir les variables attendues dans `render.yaml`.

> LocatysBTP est une application d'étudiant en stage. Il ne faut jamais committer de secrets réels (`.env`, clés API, mots de passe).