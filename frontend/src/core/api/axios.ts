import axios, { type AxiosError } from "axios";
import type { ApiErrorResponse } from "./types";
import { getToken, removeToken } from "./token";

const BACKEND_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8091";

export const api = axios.create({
  baseURL: `${BACKEND_URL}/api/v1`,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status;

    if (status === 401) {
      // Session invalide ou expirée : on supprime le token,
      // on nettoie l'état d'authentification et on redirige vers /login.
      removeToken();
      localStorage.removeItem("locatysbtp-user");
      window.dispatchEvent(new CustomEvent("auth:unauthorized"));
      if (!window.location.hash.includes("login")) {
        window.location.hash = "#/login";
      }
    }

    // 403 : pas de déconnexion. Les pages affichent leur état d'accès refusé.

    return Promise.reject(error);
  }
);

/**
 * Convertit une erreur HTTP du backend en message lisible pour l'utilisateur.
 */
export function getApiErrorMessage(error: unknown): string {
  if (!axios.isAxiosError(error)) return "Une erreur est survenue.";

  const status = error.response?.status;
  const data = error.response?.data as ApiErrorResponse | undefined;

  if (status === 401) return data?.message ?? "Email ou mot de passe incorrect.";
  if (status === 403) return data?.message ?? "Accès refusé.";
  if (status === 409) return data?.message ?? "Cette ressource existe déjà.";
  if (status === 400) {
    const fieldErrors = data?.errors;
    if (fieldErrors && Object.keys(fieldErrors).length > 0) {
      return Object.values(fieldErrors).join(", ");
    }
    return data?.message ?? "Données invalides.";
  }
  if (status && status >= 500) return "Une erreur serveur est survenue. Veuillez réessayer.";
  return data?.message ?? "Une erreur est survenue.";
}
