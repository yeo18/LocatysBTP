import { api } from "../core/api/axios";
import type {
  ApiResponse,
  ChantierResume,
  ChantierResponse,
  ChantierSearchParams,
  ChantierStatut,
  ConfirmerLocalisationRequest,
  CreateChantierRequest,
  PageResponse,
  UpdateChantierRequest,
  UtilisateurPermissionChantier,
  UtilisateurResume,
} from "../core/api/types";

export async function getChantiers(
  params?: ChantierSearchParams
): Promise<PageResponse<ChantierResume>> {
  const response = await api.get<ApiResponse<PageResponse<ChantierResume>>>("/chantiers", { params });
  return response.data.data;
}

export async function getChantier(id: number): Promise<ChantierResponse> {
  const response = await api.get<ApiResponse<ChantierResponse>>(`/chantiers/${id}`);
  return response.data.data;
}

export async function createChantier(data: CreateChantierRequest): Promise<ChantierResponse> {
  const response = await api.post<ApiResponse<ChantierResponse>>("/chantiers", data);
  return response.data.data;
}

export async function updateChantier(
  id: number,
  data: UpdateChantierRequest
): Promise<ChantierResponse> {
  const response = await api.put<ApiResponse<ChantierResponse>>(`/chantiers/${id}`, data);
  return response.data.data;
}

export async function deleteChantier(id: number): Promise<ChantierResponse> {
  const response = await api.delete<ApiResponse<ChantierResponse>>(`/chantiers/${id}`);
  return response.data.data;
}

export async function confirmerLocalisation(
  id: number,
  data: ConfirmerLocalisationRequest
): Promise<ChantierResponse> {
  const response = await api.put<ApiResponse<ChantierResponse>>(`/chantiers/${id}/localisation`, data);
  return response.data.data;
}

// ---- Affectations directes utilisateur <-> chantier ----

export async function getChantierUtilisateurs(chantierId: number): Promise<UtilisateurResume[]> {
  const response = await api.get<ApiResponse<UtilisateurResume[]>>(`/chantiers/${chantierId}/utilisateurs`);
  return response.data.data;
}

export async function affecterUtilisateurChantier(
  chantierId: number,
  utilisateurId: number
): Promise<void> {
  await api.post<ApiResponse<unknown>>(`/chantiers/${chantierId}/utilisateurs`, { utilisateurId });
}

export async function retirerUtilisateurChantier(
  chantierId: number,
  utilisateurId: number
): Promise<void> {
  await api.delete<ApiResponse<void>>(`/chantiers/${chantierId}/utilisateurs/${utilisateurId}`);
}

// ---- Permissions scopées par chantier ----

export async function getChantierPermissions(
  chantierId: number
): Promise<UtilisateurPermissionChantier[]> {
  const response = await api.get<ApiResponse<UtilisateurPermissionChantier[]>>(
    `/chantiers/${chantierId}/permissions`
  );
  return response.data.data;
}

export async function accorderPermissionChantier(
  chantierId: number,
  utilisateurId: number,
  permissionId: number
): Promise<UtilisateurPermissionChantier> {
  const response = await api.post<ApiResponse<UtilisateurPermissionChantier>>(
    `/chantiers/${chantierId}/permissions/${utilisateurId}/${permissionId}/accorder`
  );
  return response.data.data;
}

export async function refuserPermissionChantier(
  chantierId: number,
  utilisateurId: number,
  permissionId: number
): Promise<UtilisateurPermissionChantier> {
  const response = await api.post<ApiResponse<UtilisateurPermissionChantier>>(
    `/chantiers/${chantierId}/permissions/${utilisateurId}/${permissionId}/refuser`
  );
  return response.data.data;
}

export async function retirerPermissionChantier(
  chantierId: number,
  exceptionId: number
): Promise<void> {
  await api.delete<ApiResponse<void>>(`/chantiers/${chantierId}/permissions/${exceptionId}`);
}

/**
 * Libellés d'affichage des statuts backend (enum). La traduction sert
 * uniquement à l'affichage : les valeurs envoyées restent les valeurs réelles.
 */
export const CHANTIER_STATUT_LABELS: Record<ChantierStatut, string> = {
  PREVU: "Planifié",
  EN_COURS: "En cours",
  TERMINE: "Terminé",
  ANNULE: "Annulé",
};

export function chantierStatutLabel(statut: ChantierStatut): string {
  return CHANTIER_STATUT_LABELS[statut] ?? statut;
}

export function chantierStatutEnum(label: string): ChantierStatut | undefined {
  const entry = (Object.entries(CHANTIER_STATUT_LABELS) as [ChantierStatut, string][]).find(
    ([, l]) => l === label
  );
  return entry?.[0];
}
