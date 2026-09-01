import { api } from "../core/api/axios";
import type {
  ApiResponse,
  CreateProfilRequest,
  PageResponse,
  ProfilPermission,
  ProfilResponse,
  ProfilResume,
  SearchParams,
  UpdateProfilRequest,
} from "../core/api/types";

export async function getProfils(params?: SearchParams): Promise<PageResponse<ProfilResume>> {
  const response = await api.get<ApiResponse<PageResponse<ProfilResume>>>("/profils", { params });
  return response.data.data;
}

export async function getProfil(id: number): Promise<ProfilResponse> {
  const response = await api.get<ApiResponse<ProfilResponse>>(`/profils/${id}`);
  return response.data.data;
}

export async function createProfil(data: CreateProfilRequest): Promise<ProfilResponse> {
  const response = await api.post<ApiResponse<ProfilResponse>>("/profils", data);
  return response.data.data;
}

export async function updateProfil(id: number, data: UpdateProfilRequest): Promise<ProfilResponse> {
  const response = await api.put<ApiResponse<ProfilResponse>>(`/profils/${id}`, data);
  return response.data.data;
}

export async function deleteProfil(id: number): Promise<void> {
  await api.delete<ApiResponse<void>>(`/profils/${id}`);
}

export async function getProfilPermissions(id: number): Promise<ProfilPermission[]> {
  const response = await api.get<ApiResponse<ProfilPermission[]>>(`/profils/${id}/permissions`);
  return response.data.data;
}

export async function grantProfilPermission(id: number, permissionId: number): Promise<ProfilPermission> {
  const response = await api.post<ApiResponse<ProfilPermission>>(`/profils/${id}/permissions/${permissionId}`);
  return response.data.data;
}

export async function revokeProfilPermission(id: number, permissionId: number): Promise<void> {
  await api.delete<ApiResponse<void>>(`/profils/${id}/permissions/${permissionId}`);
}
