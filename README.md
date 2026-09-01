# LocatysBTP — Gestion de Chantiers

Système complet de gestion de chantiers : utilisateurs, équipes, tâches, RBAC dynamique et analyse de site (météo, géolocalisation).

## Stack

- **Backend** : Java 25, Spring Boot 3, Maven, PostgreSQL, Flyway, JWT, Swagger
- **Frontend** : React 19, TypeScript, Vite 7, Tailwind 4, Redux, Recharts, Leaflet

## Structure

```
backend/   → API REST Spring Boot (modules auth, chantier, equipe, tache, template, rbac...)
frontend/  → SPA React + Vite
docker/    → Infrastructure (PostgreSQL, pgAdmin)
docs/      → Conception, architecture, UML (dossier de référence pour le suivi de stage)
```

## Démarrage rapide

1. **Infrastructure** : démarre Docker Desktop puis les bases (PostgreSQL, pgAdmin)

   ```powershell
   .\start.ps1
   ```

   ou manuellement :

   ```powershell
   cd docker
   docker compose up -d
   ```

2. **Backend** : API sur http://localhost:8091

   ```powershell
   cd backend
   mvn spring-boot:run
   ```

3. **Frontend** : SPA sur http://localhost:3000

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

## Base de données

- Migrations Flyway : `backend/src/main/resources/db/migration/` (V1 → V22)
- pgAdmin : http://localhost:5050 (`admin@admin.com` / `admin`)
- Base : `gestion_de_chantier` (port 5432, `admin` / `admin123`)

## Tests

```powershell
cd backend
mvn test
```

50+ tests unitaires et d'intégration (JWT, RBAC, templates, analyse de site).

## Déploiement (Render.com)

Le fichier `render.yaml` (blueprint) déploie automatiquement **3 ressources** : une base PostgreSQL, l'API Spring Boot et le frontend SPA.

1. **Pousser le code** sur GitHub puis aller sur https://dashboard.render.com
2. **New** → **Blueprint** → connecter le repo `LocatysBTP` → **Apply**
3. Render crée la base, le backend et le frontend (~5 min)
4. **Après le premier déploiement**, deux variables à renseigner (menus **Environment** de chaque service) :
   - Backend → `CORS_ALLOWED_ORIGINS` = URL du frontend (ex. `https://locatysbtp-frontend.onrender.com`)
   - Frontend → `VITE_API_URL` = URL du backend (ex. `https://locatysbtp-backend.onrender.com`)
5. Rebuild sur Render (bouton **Manual Deploy** → **Deploy branch**) puis ouvrir l'application.

> `JWT_SECRET` est généré automatiquement par Render. Les identifiants DB sont reliés entre services.

## Documentation

- Architecture : `docs/architecture.md`, `docs/architecture-backend-spring.md`
- API REST : `docs/controllers-rest-api.md`, `docs/swagger-documentation.md`
- Base de données : `docs/MLD-relationnel.md`, `docs/database-design.md`
- Sécurité : `docs/security-jwt.md`, `docs/rbac-dynamique.md`
- Frontend : `docs/frontend-structure.md`