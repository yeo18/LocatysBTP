import { api } from "../core/api/axios";
import type {
  ApiResponse,
  CreateUserRequest,
  PageResponse,
  SearchParams,
  UpdateUserRequest,
  UtilisateurPermission,
  UtilisateurResponse,
  UtilisateurResume,
} from "../core/api/types";

export async function getUsers(params?: SearchParams): Promise<PageResponse<UtilisateurResume>> {
  const response = await api.get<ApiResponse<PageResponse<UtilisateurResume>>>("/users", { params });
  return response.data.data;
}

export async function getUser(id: number): Promise<UtilisateurResponse> {
  const response = await api.get<ApiResponse<UtilisateurResponse>>(`/users/${id}`);
  return response.data.data;
}

export async function createUser(data: CreateUserRequest): Promise<UtilisateurResponse> {
  const response = await api.post<ApiResponse<UtilisateurResponse>>("/users", data);
  return response.data.data;
}

export async function updateUser(id: number, data: UpdateUserRequest): Promise<UtilisateurResponse> {
  const response = await api.put<ApiResponse<UtilisateurResponse>>(`/users/${id}`, data);
  return response.data.data;
}

export async function getUserPermissions(id: number): Promise<UtilisateurPermission[]> {
  const response = await api.get<ApiResponse<UtilisateurPermission[]>>(`/users/${id}/permissions`);
  return response.data.data;
}

export async function grantUserPermission(
  id: number,
  permissionId: number
): Promise<UtilisateurPermission> {
  const response = await api.post<ApiResponse<UtilisateurPermission>>(
    `/users/${id}/permissions/${permissionId}/accorder`
  );
  return response.data.data;
}

export async function refuseUserPermission(
  id: number,
  permissionId: number
): Promise<UtilisateurPermission> {
  const response = await api.post<ApiResponse<UtilisateurPermission>>(
    `/users/${id}/permissions/${permissionId}/refuser`
  );
  return response.data.data;
}

export async function removeUserPermission(exceptionId: number): Promise<void> {
  await api.delete<ApiResponse<void>>(`/users/permissions/${exceptionId}`);
}
