import { api } from "../core/api/axios";
import type {
  ApiResponse,
  CreatePermissionRequest,
  PageResponse,
  PermissionResume,
  PermissionResponse,
  SearchParams,
  UpdatePermissionRequest,
} from "../core/api/types";

export async function getPermissions(params?: SearchParams): Promise<PageResponse<PermissionResume>> {
  const response = await api.get<ApiResponse<PageResponse<PermissionResume>>>("/permissions", { params });
  return response.data.data;
}

export async function getPermission(id: number): Promise<PermissionResponse> {
  const response = await api.get<ApiResponse<PermissionResponse>>(`/permissions/${id}`);
  return response.data.data;
}

export async function getPermissionsByModule(module: string): Promise<PermissionResume[]> {
  const response = await api.get<ApiResponse<PermissionResume[]>>(`/permissions/modules/${module}`);
  return response.data.data;
}

export async function createPermission(data: CreatePermissionRequest): Promise<PermissionResponse> {
  const response = await api.post<ApiResponse<PermissionResponse>>("/permissions", data);
  return response.data.data;
}

export async function updatePermission(
  id: number,
  data: UpdatePermissionRequest
): Promise<PermissionResponse> {
  const response = await api.put<ApiResponse<PermissionResponse>>(`/permissions/${id}`, data);
  return response.data.data;
}

export async function deletePermission(id: number): Promise<void> {
  await api.delete<ApiResponse<void>>(`/permissions/${id}`);
}

export async function deactivatePermission(id: number): Promise<PermissionResponse> {
  const response = await api.patch<ApiResponse<PermissionResponse>>(`/permissions/${id}/desactiver`);
  return response.data.data;
}

export async function activatePermission(id: number): Promise<PermissionResponse> {
  const response = await api.patch<ApiResponse<PermissionResponse>>(`/permissions/${id}/activer`);
  return response.data.data;
}
