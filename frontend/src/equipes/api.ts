import { api } from "../core/api/axios";
import type {
  AffectationEquipeChantierResume,
  AffectationEquipeChantierResponse,
  ApiResponse,
  CreateAffectationEquipeChantierRequest,
  CreateEquipeRequest,
  CreateMembreEquipeRequest,
  EquipeDetail,
  EquipeResponse,
  EquipeResume,
  MembreEquipeResponse,
  UpdateEquipeRequest,
} from "../core/api/types";

export async function getEquipes(): Promise<EquipeResume[]> {
  const response = await api.get<ApiResponse<EquipeResume[]>>("/equipes");
  return response.data.data;
}

export async function getEquipesDetaillees(): Promise<EquipeDetail[]> {
  const response = await api.get<ApiResponse<EquipeDetail[]>>("/equipes/detaillees");
  return response.data.data;
}

export async function getEquipe(id: number): Promise<EquipeResponse> {
  const response = await api.get<ApiResponse<EquipeResponse>>(`/equipes/${id}`);
  return response.data.data;
}

export async function createEquipe(data: CreateEquipeRequest): Promise<EquipeResponse> {
  const response = await api.post<ApiResponse<EquipeResponse>>("/equipes", data);
  return response.data.data;
}

export async function updateEquipe(id: number, data: UpdateEquipeRequest): Promise<EquipeResponse> {
  const response = await api.put<ApiResponse<EquipeResponse>>(`/equipes/${id}`, data);
  return response.data.data;
}

// ---- Membres d'équipe ----

export async function getEquipeMembres(equipeId: number): Promise<MembreEquipeResponse[]> {
  const response = await api.get<ApiResponse<MembreEquipeResponse[]>>(`/equipes/${equipeId}/membres`);
  return response.data.data;
}

export async function addEquipeMembre(data: CreateMembreEquipeRequest): Promise<MembreEquipeResponse> {
  const response = await api.post<ApiResponse<MembreEquipeResponse>>("/equipes/membres", data);
  return response.data.data;
}

export async function removeEquipeMembre(membreId: number): Promise<void> {
  await api.delete<ApiResponse<void>>(`/equipes/membres/${membreId}`);
}

// ---- Affectations équipe → chantier ----

export async function getEquipeAffectations(equipeId: number): Promise<AffectationEquipeChantierResponse[]> {
  const response = await api.get<ApiResponse<AffectationEquipeChantierResponse[]>>(`/equipes/${equipeId}/affectations`);
  return response.data.data;
}

/** Équipes affectées à un chantier (résumé). */
export async function getChantierEquipes(chantierId: number): Promise<AffectationEquipeChantierResume[]> {
  const response = await api.get<ApiResponse<AffectationEquipeChantierResume[]>>(
    `/equipes/chantiers/${chantierId}/affectations`
  );
  return response.data.data;
}

export async function affecterEquipeChantier(
  data: CreateAffectationEquipeChantierRequest
): Promise<AffectationEquipeChantierResponse> {
  const response = await api.post<ApiResponse<AffectationEquipeChantierResponse>>("/equipes/affectations", data);
  return response.data.data;
}

export async function terminerAffectation(
  affectationId: number
): Promise<AffectationEquipeChantierResponse> {
  const response = await api.put<ApiResponse<AffectationEquipeChantierResponse>>(
    `/equipes/affectations/${affectationId}/terminer`
  );
  return response.data.data;
}

/** Libellés d'affichage des rôles dans une équipe (enum backend). */
export const ROLE_DANS_EQUIPE_LABELS: Record<string, string> = {
  CHEF: "Chef d'équipe",
  OUVRIER: "Ouvrier",
};

export function roleDansEquipeLabel(role: string): string {
  return ROLE_DANS_EQUIPE_LABELS[role] ?? role;
}

/** Libellés d'affichage du statut d'une affectation équipe → chantier. */
export const AFFECTATION_STATUT_LABELS: Record<string, string> = {
  ACTIVE: "Active",
  TERMINEE: "Terminée",
};

export function affectationStatutLabel(statut: string): string {
  return AFFECTATION_STATUT_LABELS[statut] ?? statut;
}
