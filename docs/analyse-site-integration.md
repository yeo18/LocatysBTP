# Intégration « Analyse du site » — CMS BatiFlow

Date : 20/08/2026 (mise à jour : Open-Meteo remplacé par OpenWeather)
Module : analyse géographique d'un chantier (localisation + météo + environnement).

---

## 1. Vue d'ensemble

Le module ajoute au CMS une fonctionnalité « Analyse du site » :

1. **Sélection d'un chantier** (lecture RBAC requise).
2. **Description de la localisation** → géocodage **Nominatim** (OpenStreetMap).
3. **Carte Leaflet** (marqueur déplaçable, clic, champs lat/lon) → **confirmation obligatoire** de la position.
4. **Analyse parallèle** des sources indépendantes :
   - **OpenWeather** : conditions actuelles + prévisions quotidiennes (5 j, plan gratuit) et horaires (pas de 3 h) + **qualité de l'air** (API Air Pollution, indice AQI 1-5 + polluants).

### Données météo récupérées (OpenWeather)

**Conditions actuelles** (`/data/2.5/weather`) :
température, ressenti, humidité, pression, précipitations (1 h/3 h), neige, vitesse / direction du vent (converti km/h), rafales, couverture nuageuse, visibilité, conditions + description, code condition, heure de mise à jour.

**Prévisions quotidiennes** (agrégées depuis `/data/2.5/forecast`, pas de 3 h) :
date, min/max de température, **humidité maximale**, cumul de précipitations, probabilité maximale, vent max, rafales max, **couverture nuageuse moyenne**, condition dominante.

**Prévisions horaires** (pas de 3 h, brutes) :
date-heure, température, ressenti, précipitations, probabilité, vent, **humidité**, **couverture nuageuse**, description.

Aucune donnée n'est fabriquée : la pluie « actuelle » d'une part et la « probabilité » de précipitations d'autre part sont deux champs distincts, jamais confondus (`precipitations` en mm vs `probaPrecipitations` en %).
   - **Overpass (OSM)** : points d'intérêt dans un rayon configurable (défaut 1000 m).
5. **Synthèse déterministe** (aucune IA) : niveau de vigilance + points favorables / attention + recommandations.

Principe strict : **le navigateur n'appelle jamais une API externe**. Tout passe par le backend Spring Boot.

---

## 2. Architecture

```
React (front)  ──HTTP──▶  Spring Boot (back)  ──HTTP──▶  APIs externes
                                                          ├─ Nominatim (géocodage + reverse)
                                                          ├─ OpenWeather
                                                          └─ Overpass API
```

### Backend — packages créés (`com.cms.analyse`)
| Composant | Rôle |
|---|---|
| `dto/*` | DTO de réponse (sections + synthèse), `StatutSource` |
| `cache/TtlCache` | Cache mémoire simple (TTL), ConcurrentHashMap |
| `client/NominatimClient` | Géocodage + reverse, cache 24 h, User-Agent configurable |
| `client/OpenWeatherClient` | Endpoints gratuits `/weather` (actuel) + `/forecast` (5 j / 3 h) + `/air_pollution` (AQI), agrégation quotidienne, cache 30 min, clé via `OPENWEATHER_API_KEY` |
| `client/OverpassClient` | Requête Overpass, rayon paramétrable, tri par distance, haversine |
| `service/SyntheseService` | Synthèse déterministe (règles métier, aucun contenu inventé) |
| `service/AnalyseSiteService` / `Impl` | Orchestration parallèle (`CompletableFuture` + `ExecutorService`) |
| `controller/AnalyseSiteController` | Endpoints REST |

### Backend — fichiers modifiés
| Fichier | Modification |
|---|---|
| `Chantier.java` | + `latitude`, `longitude`, `localisationDescription`, `sourceLocalisation`, `precisionLocalisation` |
| `ChantierService` / `Impl` | + `confirmerLocalisation(id, request)` |
| `ChantierController` | + `PUT /{id}/localisation` |
| `ChantierResponse` | + champs de localisation |
| `ChantierMapper` | ignore la localisation lors des conversions `toEntity` / update |
| `BeanConfig` | + `RestTemplate` (timeouts 5 s / 15 s), `ExecutorService` (4 threads) |
| `application.yml` | bloc `app.analyse.*` (voir §3) |
| `ApiRoutes` | + `ANALYSE_SITE` |
| `db/migration/V12__analyse_site.sql` | colonnes additifs sur `chantier` |

### Frontend — fichiers créés
| Fichier | Rôle |
|---|---|
| `src/api/analyse.ts` | `geocoder(description)`, `analyserSite(id)` |
| `src/components/analyseMap.tsx` | Carte Leaflet (marqueur déplaçable, clic, mode lecture) |
| `src/pages/analyseSite.tsx` | Assistant 4 étapes : chantier → localisation → carte → analyse |

### Frontend — fichiers modifiés
| Fichier | Modification |
|---|---|
| `src/api/types.ts` | Champs localisation du `ChantierResponse` + types du module analyse |
| `src/api/chantiers.ts` | + `confirmerLocalisation(id, request)` |
| `src/App.tsx` | + route `/analyse-site` (guard `CHANTIER_LIRE`) |
| `src/components/layout.tsx` | + entrée « Analyse du site » (icône Radar, perm `CHANTIER_LIRE`) |
| `package.json` | + `leaflet`, `react-leaflet`, `@types/leaflet` |

**Affichage frontend** (section Météo du module analyse) :
- Conditions actuelles : température, ressenti, humidité, pression, **pluie actuelle en mm** (distincte de la probabilité), vent + direction cardinale, rafales, nuages, visibilité, conditions, heure de mise à jour (source OpenWeather) ;
- **Qualité de l'air** : badge indice AQI 1-5 + 6 polluants ;
- **Prévisions** : tableau « Aujourd'hui / Demain / J+N » sur 5 jours avec T° min/max, humidité max, précipitations prévues (mm), risque (%), vent, nuages, temps ;
- Badge de fiabilité « Données complètes / partielles / limitées » — aucun `NaN`/`undefined` affiché (`formatNombre`), une section indisponible affiche son statut sans bloquer le reste.

---

## 3. Variables d'environnement et configuration

`application.yml` (bloc `app.analyse`):

```yaml
app.analyse:
  environnement:
    rayon-metres: 1000        # rayon de recherche Overpass
  nominatim:
    user-agent: "BatiFlowCMS/1.0 (...)"   # obligatoire pour la politique Nominatim
  openweather:
    api-key: ${OPENWEATHER_API_KEY:}   # variable d'environnement obligatoire pour la météo
    units: metric                        # °C, m/s, hPa (m/s converti en km/h côté métier)
    lang: fr                             # langue des descriptions météo
    cache-secondes: 1800                 # TTL du cache des réponses
```

- **OpenWeather est optionnel** : sans `OPENWEATHER_API_KEY`, la section météo renvoie `INDISPONIBLE` avec un message explicite (le reste de l'analyse fonctionne). La clé n'est jamais committée ni exposée au front.
- En cas d'erreur OpenWeather (clé invalide, quota, indisponibilité, délai), le client renvoie un résultat vide → section `INDISPONIBLE`/`ERREUR`, jamais d'échec global de l'analyse.
- **La qualité de l'air est complémentaire** : son échec n'altère pas la météo (`qualiteAir = null`, mention « non disponible » dans la section).
- Timeouts `RestTemplate` volontairement courts (connect 5 s, read 15 s) : une source externe lente ne bloque pas l'analyse.
- Toutes les sources sont **isolées** : une erreur produit une section `ERREUR` ou `INDISPONIBLE`, jamais un échec global.

---

## 4. API REST

| Méthode | URL | Permission | Description |
|---|---|---|---|
| GET | `/api/v1/analyse-site/geocoder?q={description}` | `CHANTIER_LIRE` | Recherche Nominatim (action explicite, max 5) |
| GET | `/api/v1/analyse-site/chantiers/{id}/analyser` | `CHANTIER_LIRE` scopée | Analyse complète (position confirmée requise) |
| PUT | `/api/v1/chantiers/{id}/localisation` | `CHANTIER_MODIFIER` scopée | Confirme la position (source GEOCODAGE/CARTE, précision APPROXIMATIVE/PRECISE) |

Règles métier importantes :
- `GET /analyser` lève **400** si le chantier n'a pas de position confirmée.
- La position doit être **confirmée explicitement** (PUT localisation) avant toute analyse.
- Une source indisponible ne fait jamais échouer l'analyse (statut par section).

---

## 5. Sécurité / RBAC

- Contrôle **Niveau 1** : `@PreAuthorize("hasPermission(#id, 'CHANTIER', 'LIRE')")` / `MODIFIER` sur les endpoints scopés.
- Contrôle **Niveau 2** : `DataAccessService.verifierAccesChantier(id)` dans `analyser`.
- La confirmation de position exige la permission `CHANTIER_MODIFIER` (profil ou exception scopée) ; le géocodage exige `CHANTIER_LIRE`.

---

## 6. Tests

- `OpenWeatherClientTest` (11 tests) : parsing actuel + prévisions via `MockRestServiceServer`, conversion m/s→km/h, pluie présente/absente, humidité et couverture nuageuse (horaire + agrégation quotidienne), clé manquante, clé invalide (401), serveur en erreur → résultat vide sans exception ; qualité de l'air (indice + polluants, liste vide, non configurée, 401).
- `AnalyseSiteServiceImplTest` (9 tests) : analyse complète 4 sources OK, météo non configurée / indisponible / en erreur → analyse continue, qualité de l'air indisponible → météo reste disponible, chantier sans coordonnées → 400, chantier introuvable → 404, géocodage (délégation + description vide → 400).
- `SyntheseServiceTest` (7 tests) : règles qualité de l'air (mauvaise/moyenne/bonne, particules fines), aucun ajout si `qualiteAir` absent, message « N jours » réel, **règle humidité élevée** (+ absence de mention si l'humidité reste normale).
- Compilation backend : `mvn compile` OK.
- Contextes Spring + migration Flyway **V12** : OK (test d'intégration lancé sur la base `gestion_de_chantier`).
- Front : `npx tsc --noEmit` OK, `npm run build` OK.

> Échecs de test **pré-existants** (sans lien avec ce module, confirmés après le refactor des enums) :
> - `ChantierControllerTest` (consultation/modification/annulation) : les tests ne stubent pas `DroitsService.calculerDroitsSurChantier`, introduit par le refactor RBAC scopé → 403. À corriger en ajoutant le stub.
> - `TemplateChantierControllerTest`, `TemplateTacheControllerTest`, `TemplateTacheServiceImplTest`, `TemplateImportIntegrationTest` : NPE / stubbing désynchronisés avec l'implémentation (compteurs d'ordres, contrôles de titre). Indépendants de ce module.

---

## 7. Améliorations possibles

- Ajouter des tests `ChantierControllerTest` pour le cas `/localisation` (confirmation = source GEOCODAGE/CARTE).
- Passer à l'API **One Call 3.0** d'OpenWeather (payante) pour des prévisions horaires plus détaillées et sur plus de 5 jours.
- Stocker l'historique des analyses (table dédiée + migration V13).
