import { api } from "../core/api/axios";
import type {
  AffectationTacheResponse,
  ApiResponse,
  CreateAffectationTacheRequest,
  CreateTacheRequest,
  PageResponse,
  SearchParams,
  TachePriorite,
  TacheResponse,
  TacheResume,
  TacheStatus,
  UpdateTacheRequest,
  ValidationTacheResponse,
} from "../core/api/types";

export async function getTaches(params?: SearchParams): Promise<PageResponse<TacheResume>> {
  const response = await api.get<ApiResponse<PageResponse<TacheResume>>>("/taches", { params });
  return response.data.data;
}

export async function getTache(id: number): Promise<TacheResponse> {
  const response = await api.get<ApiResponse<TacheResponse>>(`/taches/${id}`);
  return response.data.data;
}

export async function createTache(data: CreateTacheRequest): Promise<TacheResponse> {
  const response = await api.post<ApiResponse<TacheResponse>>("/taches", data);
  return response.data.data;
}

export async function updateTache(id: number, data: UpdateTacheRequest): Promise<TacheResponse> {
  const response = await api.put<ApiResponse<TacheResponse>>(`/taches/${id}`, data);
  return response.data.data;
}

export async function deleteTache(id: number): Promise<void> {
  await api.delete(`/taches/${id}`);
}

export async function affecterTache(data: CreateAffectationTacheRequest): Promise<AffectationTacheResponse> {
  const response = await api.post<ApiResponse<AffectationTacheResponse>>("/taches/affectations", data);
  return response.data.data;
}

export async function getTacheAffectations(tacheId: number): Promise<AffectationTacheResponse[]> {
  const response = await api.get<ApiResponse<AffectationTacheResponse[]>>(`/taches/${tacheId}/affectations`);
  return response.data.data;
}

export async function retirerAffectation(affectationId: number): Promise<void> {
  await api.delete(`/taches/affectations/${affectationId}`);
}

export async function getValidations(tacheId: number): Promise<ValidationTacheResponse[]> {
  const response = await api.get<ApiResponse<ValidationTacheResponse[]>>(`/taches/${tacheId}/validations`);
  return response.data.data;
}

export async function validerTache(
  tacheId: number,
  data: { commentaire?: string; dateValidation: string }
): Promise<ValidationTacheResponse> {
  const response = await api.post<ApiResponse<ValidationTacheResponse>>(`/taches/${tacheId}/valider`, {
    tacheId,
    statut: "VALIDE",
    commentaire: data.commentaire,
    dateValidation: data.dateValidation,
  });
  return response.data.data;
}

export async function refuserTache(
  tacheId: number,
  data: { commentaire?: string; dateValidation: string }
): Promise<ValidationTacheResponse> {
  const response = await api.post<ApiResponse<ValidationTacheResponse>>(`/taches/${tacheId}/refuser`, {
    tacheId,
    statut: "REFUSE",
    commentaire: data.commentaire,
    dateValidation: data.dateValidation,
  });
  return response.data.data;
}

/**
 * Libellés d'affichage des statuts backend (enum). La traduction sert
 * uniquement à l'affichage : les valeurs envoyées restent les valeurs réelles.
 */
export const TACHE_STATUT_LABELS: Record<TacheStatus, string> = {
  A_FAIRE: "À faire",
  EN_COURS: "En cours",
  VALIDE: "Validée",
  REFUSE: "Refusée",
};

export function tacheStatutLabel(statut: TacheStatus): string {
  return TACHE_STATUT_LABELS[statut] ?? statut;
}

export function tacheStatutEnum(label: string): TacheStatus | undefined {
  const entry = (Object.entries(TACHE_STATUT_LABELS) as [TacheStatus, string][]).find(([, l]) => l === label);
  return entry?.[0];
}

export const TACHE_PRIORITE_LABELS: Record<TachePriorite, string> = {
  HAUTE: "Haute",
  MOYENNE: "Moyenne",
  BASSE: "Basse",
};

export function tachePrioriteLabel(priorite: TachePriorite): string {
  return TACHE_PRIORITE_LABELS[priorite] ?? priorite;
}

export function tachePrioriteEnum(label: string): TachePriorite | undefined {
  const entry = (Object.entries(TACHE_PRIORITE_LABELS) as [TachePriorite, string][]).find(([, l]) => l === label);
  return entry?.[0];
}
