// Types de la couche API - alignés sur les DTO du backend Spring Boot.
// On ne duplique ici que ce dont la couche API a besoin.

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ApiErrorResponse {
  success: boolean;
  message: string;
  code?: string;
  timestamp: string;
  errors?: Record<string, string>;
}

// ---- Auth ----

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
  telephone?: string;
}

export interface ChangerMotDePasseRequest {
  ancienMotDePasse: string;
  nouveauMotDePasse: string;
  confirmation: string;
}

export interface UtilisateurResponse {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  profilId: number;
  profilNom: string;
  dateCreation: string;
  dateModification: string;
}

export interface TokenResponse {
  token: string;
  type: string;
  expiresIn: number;
  utilisateur: UtilisateurResponse;
}

export interface MeResponse {
  utilisateur: UtilisateurResponse;
  permissions: string[];
  /** Droits effectifs par chantier (id chantier -> codes) - exceptions scopées incluses. */
  permissionsParChantier?: Record<string, string[]>;
}

// ---- Pagination / recherche ----

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface SearchParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: "ASC" | "DESC";
  motCle?: string;
}

// ---- Utilisateurs ----

export interface UtilisateurResume {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  profilNom: string;
}

export interface CreateUserRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
  telephone?: string;
  profilId?: number;
}

export interface UpdateUserRequest {
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  profilId: number;
}

export type UtilisateurPermissionType = "ACCORDER" | "REFUSER";

export interface UtilisateurPermission {
  id: number;
  utilisateurId: number;
  permissionId: number;
  nomPermission: string;
  type: UtilisateurPermissionType;
  createdById: number;
  dateCreation: string;
  dateModification: string;
}

export interface UtilisateurPermissionChantier {
  id: number;
  utilisateurId: number;
  utilisateurNom: string;
  utilisateurPrenom: string;
  chantierId: number;
  chantierNom: string;
  permissionId: number;
  nomPermission: string;
  permissionNom: string;
  type: UtilisateurPermissionType;
  createdById: number;
  dateCreation: string;
  dateModification: string;
}

// ---- Profils ----

export interface ProfilResume {
  id: number;
  nom: string;
}

export interface ProfilResponse {
  id: number;
  nom: string;
  description: string;
  dateCreation: string;
  dateModification: string;
}

export interface CreateProfilRequest {
  nom: string;
  description?: string;
}

export interface UpdateProfilRequest {
  nom: string;
  description?: string;
}

export interface ProfilPermission {
  id: number;
  profilId: number;
  profilNom: string;
  permissionId: number;
  nomPermission: string;
  permissionNom: string;
}

// ---- Permissions ----

export interface PermissionResume {
  id: number;
  nom: string;
  nomPermission: string;
  module: string;
}

export interface PermissionResponse {
  id: number;
  nom: string;
  nomPermission: string;
  module: string;
  description: string;
}

export interface CreatePermissionRequest {
  nom: string;
  nomPermission: string;
  module: string;
  description?: string;
}

export interface UpdatePermissionRequest {
  nom: string;
  nomPermission: string;
  module: string;
  description?: string;
}

// ---- Chantiers ----

export type ChantierStatut = "PREVU" | "EN_COURS" | "TERMINE" | "ANNULE";

export interface ChantierResume {
  id: number;
  nom: string;
  adresseSaisie: string;
  statut: ChantierStatut;
  dateDebut: string;
  dateFin: string;
  progression: number;
}

export interface ChantierResponse {
  id: number;
  nom: string;
  description: string;
  adresseSaisie: string;
  latitude: number | null;
  longitude: number | null;
  adresseGeocodee: string | null;
  origineCoordonnees: OrigineCoordonnees | null;
  fiabiliteCoordonnees: FiabiliteCoordonnees | null;
  statut: ChantierStatut;
  dateDebut: string;
  dateFin: string;
  progression: number;
  responsableId: number;
  responsableNom: string;
  dateCreation: string;
  dateModification: string;
}

export type OrigineCoordonnees = "GEOCODAGE" | "CARTE";
export type FiabiliteCoordonnees = "APPROXIMATIVE" | "PRECISE";

export interface ConfirmerLocalisationRequest {
  latitude: number;
  longitude: number;
  adresseGeocodee?: string;
  origineCoordonnees: OrigineCoordonnees;
  fiabiliteCoordonnees: FiabiliteCoordonnees;
}

export interface CreateChantierRequest {
  nom: string;
  description?: string;
  adresseSaisie?: string;
  statut: ChantierStatut;
  dateDebut?: string;
  dateFin?: string;
  responsableId?: number;
}

export interface UpdateChantierRequest {
  nom: string;
  description?: string;
  adresseSaisie?: string;
  statut: ChantierStatut;
  dateDebut?: string;
  dateFin?: string;
  responsableId?: number;
}

export interface ChantierSearchParams extends SearchParams {
  statut?: ChantierStatut;
}

// ---- Tâches ----

export type TacheStatus = "A_FAIRE" | "EN_COURS" | "VALIDE" | "REFUSE";
export type TachePriorite = "HAUTE" | "MOYENNE" | "BASSE";
export type ValidationTacheStatut = "VALIDE" | "REFUSE";

export interface TacheResume {
  id: number;
  titre: string;
  priorite: TachePriorite;
  status: TacheStatus;
  progression: number;
  dateDebut: string | null;
  dateFin: string | null;
  chantierId: number;
}

export interface TacheResponse {
  id: number;
  titre: string;
  description: string;
  priorite: TachePriorite;
  status: TacheStatus;
  progression: number;
  dateDebut: string | null;
  dateFin: string | null;
  chantierId: number;
  chantierNom: string;
}

export interface CreateTacheRequest {
  titre: string;
  description?: string;
  priorite: TachePriorite;
  dateDebut?: string;
  dateFin?: string;
  chantierId: number;
}

export interface UpdateTacheRequest {
  titre: string;
  description?: string;
  priorite: TachePriorite;
  status: TacheStatus;
  progression: number;
  dateDebut?: string;
  dateFin?: string;
}

export interface CreateValidationTacheRequest {
  statut: ValidationTacheStatut;
  commentaire?: string;
  dateValidation: string;
}

export interface ValidationTacheResponse {
  id: number;
  tacheId: number;
  tacheTitre: string;
  validateurId: number;
  validateurNom: string;
  validateurPrenom: string;
  statut: ValidationTacheStatut;
  commentaire: string | null;
  dateValidation: string;
  dateModification: string;
}

// ---- Équipes ----

export type RoleDansEquipe = "CHEF" | "OUVRIER";

export type AffectationEquipeChantierStatut = "ACTIVE" | "TERMINEE";

export interface EquipeResume {
  id: number;
  nom: string;
}

export interface EquipeResponse {
  id: number;
  nom: string;
  description: string | null;
}

export interface EquipeDetail {
  id: number;
  nom: string;
  description: string | null;
  membres: MembreEquipeResponse[];
  affectations: AffectationEquipeChantierResponse[];
}

export interface CreateEquipeRequest {
  nom: string;
  description?: string;
}

export interface UpdateEquipeRequest {
  nom: string;
  description?: string;
}

export interface MembreEquipeResponse {
  id: number;
  utilisateurId: number;
  utilisateurNom: string;
  utilisateurPrenom: string;
  equipeId: number;
  equipeNom: string;
  roleDansEquipe: RoleDansEquipe;
  dateIntegration: string;
}

export interface CreateMembreEquipeRequest {
  utilisateurId: number;
  equipeId: number;
  roleDansEquipe: RoleDansEquipe;
  dateIntegration: string;
}

export interface AffectationEquipeChantierResponse {
  id: number;
  equipeId: number;
  equipeNom: string;
  chantierId: number;
  chantierNom: string;
  dateDebut: string;
  dateFin: string | null;
  statut: AffectationEquipeChantierStatut;
}

export interface AffectationEquipeChantierResume {
  id: number;
  equipeNom: string;
  chantierNom: string;
  dateDebut: string;
  dateFin: string | null;
  statut: AffectationEquipeChantierStatut;
}

export interface CreateAffectationEquipeChantierRequest {
  equipeId: number;
  chantierId: number;
  dateDebut: string;
  dateFin?: string;
}

export type AffectationTacheRole = "REALISATEUR" | "CONTROLEUR";

export interface AffectationTacheResponse {
  id: number;
  tacheId: number;
  tacheTitre: string;
  utilisateurId: number | null;
  utilisateurNom: string | null;
  equipeId: number | null;
  equipeNom: string | null;
  role: AffectationTacheRole;
  dateAffectation: string;
}

export interface CreateAffectationTacheRequest {
  tacheId: number;
  utilisateurId?: number;
  equipeId?: number;
  role: AffectationTacheRole;
  dateAffectation: string;
}

// ---- Analyse du site ----

export type StatutSource = "DISPONIBLE" | "INDISPONIBLE" | "ERREUR";

export interface GeocodageResultatResponse {
  libelle: string;
  latitude: number;
  longitude: number;
  type: string;
  ville: string;
  commune: string;
  quartier: string;
  pays: string;
}

export interface MeteoActuelleResponse {
  temperature: number;
  temperatureRessentie: number;
  humidite: number;
  pression: number;
  precipitations: number;
  neige: number;
  vent: number;
  directionVent: number;
  rafales: number;
  couvertureNuageuse: number;
  visibilite: number;
  conditions: string;
  description: string;
  codeMeteo: number;
  heureMiseAJour: string | null;
}

export interface MeteoQuotidienneResponse {
  date: string;
  tempMax: number;
  tempMin: number;
  precipitations: number;
  probaPrecipitations: number;
  ventMax: number;
  rafales: number;
  humiditeMax: number;
  couvertureNuageuse: number;
  description: string;
  codeMeteo: number;
}

export interface MeteoHoraireResponse {
  dateHeure: string;
  temperature: number;
  temperatureRessentie: number;
  precipitations: number;
  probaPrecipitations: number;
  vent: number;
  humidite: number;
  couvertureNuageuse: number;
  description: string;
  codeMeteo: number;
}

export interface QualiteAirResponse {
  aqi: number;
  libelle: string;
  pm25: number;
  pm10: number;
  o3: number;
  no2: number;
  so2: number;
  co: number;
  heureMiseAJour: string | null;
}

export interface SectionMeteoResponse {
  statut: StatutSource;
  message: string | null;
  actuel: MeteoActuelleResponse | null;
  quotidiennes: MeteoQuotidienneResponse[];
  horaires: MeteoHoraireResponse[];
  qualiteAir: QualiteAirResponse | null;
}

export interface ElementEnvironnementResponse {
  nom: string;
  type: string;
  latitude: number;
  longitude: number;
  distanceKm: number;
}

export interface SectionEnvironnementResponse {
  statut: StatutSource;
  message: string | null;
  rayonMetres: number;
  elements: ElementEnvironnementResponse[];
}

export interface SyntheseResponse {
  niveauVigilance: string;
  pointsFavorables: string[];
  pointsAttention: string[];
  recommandations: string[];
  resume: string;
}

export interface AnalyseSiteResponse {
  chantierId: number;
  latitude: number;
  longitude: number;
  meteo: SectionMeteoResponse;
  environnement: SectionEnvironnementResponse;
  synthese: SyntheseResponse;
}