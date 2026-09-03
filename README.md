# LocatysBTP

Application de gestion de chantiers BTP (backend **Spring Boot 3** + frontend **React/Vite** + base **PostgreSQL**).

Ce guide explique comment **cloner, installer, configurer et lancer** l'application sur un nouveau PC.

---

## 1. Prérequis

| Outil | Version minimale |
|-------|------------------|
| Git | récente |
| Node.js + npm | **20.19** (version **22 LTS** recommandée) |
| Java (JDK) | **25** |
| Maven | **3.9** |
| Docker (avec Docker Compose) | récente (pour la base de données) |

Vérifiez vos installations :

```bash
git --version
node --version
java -version
mvn -version
docker --version
docker compose version
```

> Le backend **ne compile que sur Java 25** : vérifiez que `java -version` affiche bien la version 25.

---

## 2. Cloner le projet

```bash
git clone https://github.com/yeo18/LocatysBTP.git
cd LocatysBTP
```

Placez-vous à la **racine du projet**. Le dépôt contient :

```
backend/    # API Spring Boot
frontend/   # application web React (Vite)
docker/     # PostgreSQL + pgAdmin (Docker Compose)
start.ps1   # script de lancement rapide (Windows uniquement)
```

---

## 3. Frontend

### 3.1 Entrer dans le dossier frontend

```bash
cd frontend
```

### 3.2 Installer les dépendances

Le projet utilise **npm** :

```bash
npm install
```

### 3.3 Configuration du frontend

**Aucun fichier de configuration n'est obligatoire.** Par défaut, le frontend appelle le backend sur `http://localhost:8091` (voir `frontend/src/core/api/axios.ts`).

Si votre backend n'est pas sur ce port, créez le fichier `frontend/.env` :

```bash
VITE_API_URL=http://localhost:8091
```

`VITE_API_URL` = adresse du backend vue par le navigateur. Adaptez-la si besoin, puis **relancez** `npm run dev` (ce fichier est lu au démarrage).

### 3.4 Lancer le frontend

```bash
npm run dev
```

### 3.5 Accéder au frontend

```text
http://localhost:3000
```

---

## 4. Base de données

Le projet utilise **PostgreSQL 16** fourni par Docker Compose (`docker/docker-compose.yml`).

### Démarrer la base

```bash
cd docker
docker compose up -d
```

Cette commande crée et démarre automatiquement :

- **PostgreSQL** sur le port `5432`
  - base : `gestion_de_chantier` (créée automatiquement au premier démarrage)
  - utilisateur : `admin`
  - mot de passe : `admin123`
- **pgAdmin** sur `http://localhost:5050` (email `admin@admin.com`, mot de passe `admin`)

Vérification :

```bash
docker compose ps
```

Les deux conteneurs doivent être `Up`.

### Migrations

Aucune action manuelle : le backend applique **automatiquement** les migrations **Flyway** (`backend/src/main/resources/db/migration/V1__init.sql` → `V22...`) au premier démarrage. Les tables sont créées seules.

---

## 5. Backend

### 5.1 Entrer dans le dossier backend

```bash
cd backend
```

### 5.2 Configuration du backend

Le profil par défaut est **`dev`** (défini dans `backend/src/main/resources/application.yml` via `spring.profiles.active: dev`). Il embarque des valeurs par défaut qui correspondent à la base Docker :

- Base : `jdbc:postgresql://localhost:5432/gestion_de_chantier`
- Utilisateur : `admin` / mot de passe : `admin123`
- Secret JWT : secret de développement déjà présent

**Aucune variable d'environnement n'est donc obligatoire pour lancer en local.**

Variables optionnelles (définies dans le terminal ou l'IDE) si vous voulez changer un réglage :

| Variable | Rôle | Valeur par défaut |
|----------|------|-------------------|
| `DB_USERNAME` | Utilisateur PostgreSQL | `admin` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | `admin123` |
| `JWT_SECRET` | Clé de signature des tokens JWT (≥ 32 caractères) | secret de dev |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées (séparées par des virgules) | `http://localhost:3000,http://localhost:5173,...` |
| `OPENWEATHER_API_KEY` | Clé OpenWeather (module météo, optionnel) | vide = météo désactivée |

Exemple (PowerShell / Windows) :

```powershell
$env:DB_PASSWORD = "admin123"
```

Autres profils disponibles : `test` et `prod`, à activer via `SPRING_PROFILES_ACTIVE` (ex. `$env:SPRING_PROFILES_ACTIVE = "test"`).

### 5.3 Installer / préparer le backend

Le projet n'a **pas de Maven Wrapper** : utilisez Maven installé sur la machine. La première compilation télécharge les dépendances :

```bash
mvn clean package -DskipTests
```

Terminez avec le message `BUILD SUCCESS`.

### 5.4 Lancer le backend

```bash
mvn spring-boot:run
```

Attendez dans les logs le message `Started CmsApplication` (premières secondes du démarrage, après compilation).

- **Port du backend** : `8091`
- **URL de base de l'API** : `http://localhost:8091/api/v1`
- **Documentation Swagger** : `http://localhost:8091/swagger-ui.html`

### 5.5 Vérifier que le backend fonctionne

- Ouvrez `http://localhost:8091/swagger-ui.html` : la liste des endpoints de l'API doit s'afficher.
- Les logs doivent contenir `Started CmsApplication` sans erreur, et les migrations Flyway appliquées.
- En cas de problème, la cause la plus fréquente est la base non démarrée (voir § 7).

---

## 6. Démarrage complet

Ordre à respecter : **base de données → backend → frontend**.

### Terminal 1 — Base de données

```bash
cd docker
docker compose up -d
```

### Terminal 2 — Backend

```bash
cd backend
mvn spring-boot:run
```

### Terminal 3 — Frontend

```bash
cd frontend
npm run dev
```

Résultat :

```text
Frontend : http://localhost:3000
Backend  : http://localhost:8091   (API : http://localhost:8091/api/v1)
Swagger  : http://localhost:8091/swagger-ui.html
pgAdmin  : http://localhost:5050
```

Ouvrez `http://localhost:3000`, créez un compte puis connectez-vous : le tableau de bord doit s'afficher.

---

## 7. Problèmes courants

| Problème | Cause probable | Solution |
|----------|----------------|----------|
| `BUILD FAILURE` au build Maven | Mauvaise version de Java | Vérifier `java -version` → Java 25 |
| Le backend ne démarre pas / erreur de connexion à la base | PostgreSQL n'est pas lancé | `cd docker && docker compose up -d`, puis relancer le backend |
| `Failed to create bean ... DataSource` | La base `gestion_de_chantier` n'existe pas | `docker compose down && docker compose up -d` |
| Le frontend affiche des erreurs réseau | Le backend n'est pas lancé, ou mauvaise URL | Lancer le backend ; ou créer `frontend/.env` avec `VITE_API_URL` adapté, puis relancer `npm run dev` |
| `Port 3000 already in use` / `8091` déjà occupé | Un autre programme utilise le port | Fermer le programme concerné, ou changer de port |
| `npm ERR!` à l'installation | Version de Node trop ancienne | Installer Node 20.19+ ou 22 LTS, puis `npm install` |

> Sur Windows (PowerShell), le script `start.ps1` à la racine démarre automatiquement Docker puis le backend ; lancez ensuite le frontend à la main (`cd frontend ; npm run dev`).