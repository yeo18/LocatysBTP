import { api } from "../core/api/axios";
import type {
  ApiResponse,
  ChangerMotDePasseRequest,
  LoginRequest,
  MeResponse,
  RegisterRequest,
  TokenResponse,
  UtilisateurResponse,
} from "../core/api/types";

export async function login(email: string, password: string): Promise<TokenResponse> {
  const body: LoginRequest = { email, password };
  const response = await api.post<ApiResponse<TokenResponse>>("/auth/login", body);
  return response.data.data;
}

export async function register(data: RegisterRequest): Promise<UtilisateurResponse> {
  const response = await api.post<ApiResponse<UtilisateurResponse>>("/auth/register", data);
  return response.data.data;
}

export async function getMe(): Promise<MeResponse> {
  const response = await api.get<ApiResponse<MeResponse>>("/auth/me");
  return response.data.data;
}

export async function changerMotDePasse(data: ChangerMotDePasseRequest): Promise<void> {
  await api.post<ApiResponse<void>>("/auth/changer-mot-de-passe", data);
}
