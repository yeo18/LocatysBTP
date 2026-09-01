import { api } from "../core/api/axios";
import type {
  AnalyseSiteResponse,
  ApiResponse,
  GeocodageResultatResponse,
} from "../core/api/types";

/**
 * Géocodage Nominatim (via le backend Spring - jamais d'appel direct côté
 * navigateur). Recherche explicite, jamais en autocomplétion.
 */
export async function geocoder(description: string): Promise<GeocodageResultatResponse[]> {
  const response = await api.get<ApiResponse<GeocodageResultatResponse[]>>("/analyse-site/geocoder", {
    params: { q: description },
  });
  return response.data.data;
}

/**
 * Analyse complète d'un chantier (météo, environnement, synthèse).
 * Nécessite une position confirmée.
 */
export async function analyserSite(chantierId: number): Promise<AnalyseSiteResponse> {
  const response = await api.get<ApiResponse<AnalyseSiteResponse>>(
    `/analyse-site/chantiers/${chantierId}/analyser`
  );
  return response.data.data;
}