# ============================================================
# CMS — DÉMARRAGE AUTOMATIQUE DU PROJET
# Lance Docker Desktop, les bases PostgreSQL, pgAdmin,
# puis le backend Spring Boot CMS.
#
# Utilisation :  .\start.ps1
# ============================================================

$ErrorActionPreference = "SilentlyContinue"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  CMS - DEMARRAGE DU PROJET" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

# ------------------------------------------------------------
# 1. Démarrer Docker Desktop si besoin
# ------------------------------------------------------------
$dockerRunning = $false
try {
    docker info *> $null
    if ($?) { $dockerRunning = $true }
} catch { $dockerRunning = $false }

if (-not $dockerRunning) {
    Write-Host "[1/5] Demarrage de Docker Desktop..." -ForegroundColor Yellow
    Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    Start-Service "com.docker.service" -ErrorAction SilentlyContinue

    Write-Host "      Attente du moteur Docker..." -ForegroundColor Yellow
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 3
        docker info *> $null
        if ($?) { $ready = $true; break }
    }
    if (-not $ready) {
        Write-Host "[ERREUR] Docker ne demarre pas apres 3 min." -ForegroundColor Red
        exit 1
    }
    Write-Host "      Docker est pret." -ForegroundColor Green
} else {
    Write-Host "[1/5] Docker est deja en cours d'execution." -ForegroundColor Green
}

# ------------------------------------------------------------
# 2. Démarrer les conteneurs
# ------------------------------------------------------------
Write-Host "[2/6] Demarrage des conteneurs..." -ForegroundColor Yellow

foreach ($c in @("chantier-db", "pgadmin4")) {
    $state = docker inspect -f '{{.State.Running}}' $c 2>$null
    if ($state -eq "true") {
        Write-Host "      $c : deja actif" -ForegroundColor Green
    } else {
        docker start $c *> $null
        if ($?) {
            Write-Host "      $c : demarre" -ForegroundColor Green
        } else {
            Write-Host "      $c : echec de demarrage" -ForegroundColor Red
        }
    }
}

# SearXNG supprimé : le module Analyse n'utilise plus que Nominatim,
# OpenWeather et Overpass (aucun moteur de recherche local).

# Attendre que PostgreSQL accepte les connexions
Write-Host "      Attente de PostgreSQL..." -ForegroundColor Yellow
for ($i = 0; $i -lt 30; $i++) {
    docker exec chantier-db pg_isready -U admin *> $null
    if ($?) { break }
    Start-Sleep -Seconds 2
}
Write-Host "      PostgreSQL pret." -ForegroundColor Green

# ------------------------------------------------------------
# 3. Vérifier la base de données cible
# ------------------------------------------------------------
Write-Host "[3/6] Verification de la base de donnees..." -ForegroundColor Yellow
$dbExists = docker exec chantier-db psql -U admin -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='gestion_de_chantier'" 2>$null
if ($dbExists -ne "1") {
    docker exec chantier-db psql -U admin -d postgres -c "CREATE DATABASE gestion_de_chantier OWNER admin;" *> $null
    Write-Host "      Base gestion_de_chantier creee." -ForegroundColor Green
} else {
    Write-Host "      Base gestion_de_chantier presente." -ForegroundColor Green
}

# ------------------------------------------------------------
# 4. Démarrer le backend Spring Boot
# ------------------------------------------------------------
Write-Host "[4/6] Demarrage du backend Spring Boot..." -ForegroundColor Yellow

# Utilisation des chemins relatifs au script (portable sur n'importe quelle machine).
$backendDir = Join-Path $PSScriptRoot "backend"
$log = Join-Path $PSScriptRoot "backend\target\cms-backend.log"

# Maven : si MAVEN_HOME n'est pas positionne, on essaie mvn dans le PATH.
# Pour utiliser Maven dans le dossier outils, definir MAVEN_HOME avant de lancer :
#   $env:MAVEN_HOME = "C:\chemin\vers\apache-maven-3.9.11"
if ($env:MAVEN_HOME) {
    $env:Path = "$env:MAVEN_HOME\bin;$env:Path"
}

# Vérifier que rien n'écoute déjà sur 8091
$portBusy = netstat -ano | Select-String "LISTENING" | Select-String ":8091 "
if ($portBusy) {
    Write-Host "      Backend deja demarre sur le port 8091." -ForegroundColor Green
} else {
    Start-Process -FilePath "$env:MAVEN_HOME\bin\mvn.cmd" `
        -ArgumentList "spring-boot:run" `
        -WorkingDirectory $backendDir `
        -RedirectStandardOutput $log `
        -RedirectStandardError "$log.err" `
        -WindowStyle Hidden

    Write-Host "      Attente du demarrage (max 60s)..." -ForegroundColor Yellow
    $started = $false
    for ($i = 0; $i -lt 20; $i++) {
        Start-Sleep -Seconds 3
        $check = Select-String -Path $log -Pattern "Started CmsApplication" -ErrorAction SilentlyContinue
        if ($check) { $started = $true; break }
        $failed = Select-String -Path $log -Pattern "APPLICATION FAILED" -ErrorAction SilentlyContinue
        if ($failed) { break }
    }
    if ($started) {
        Write-Host "      Backend demarre (http://localhost:8091)." -ForegroundColor Green
    } else {
        Write-Host "      Backend : demarrage en cours ou probleme, voir target\cms-backend.log" -ForegroundColor Yellow
    }
}

# ------------------------------------------------------------
# 5. Ouvrir les interfaces
# ------------------------------------------------------------
Write-Host "[5/6] Ouverture des interfaces..." -ForegroundColor Yellow

Start-Process "http://localhost:5050"   # pgAdmin
Start-Sleep -Seconds 2
Start-Process "http://localhost:8091"   # Backend CMS

# ------------------------------------------------------------
Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  PROJET PRET" -ForegroundColor Green
Write-Host "  pgAdmin :  http://localhost:5050" -ForegroundColor Green
Write-Host "    login  :  admin@admin.com / admin" -ForegroundColor Green
Write-Host "  Backend :  http://localhost:8091" -ForegroundColor Green
Write-Host "  Frontend:  http://localhost:3000 (lancer : cd frontend && npm run dev)" -ForegroundColor Green
Write-Host "  Base    :  gestion_de_chantier (admin/admin123, port 5432)" -ForegroundColor Green
Write-Host "  Log     :  CMS\backend\target\cms-backend.log" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
