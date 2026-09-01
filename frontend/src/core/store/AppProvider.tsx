import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { login as apiLogin, register as apiRegister, getMe } from "../../auth/api";
import { getApiErrorMessage } from "../api/axios";
import { getToken, removeToken, setToken } from "../api/token";
import type { UtilisateurResponse } from "../api/types";
import type { User } from "../lib/types";

type Theme = "light" | "dark";

interface AuthCtx {
  user: User | null;
  login: (email: string, password: string) => Promise<{ ok: boolean; error?: string }>;
  register: (data: Partial<User> & { password: string }) => Promise<{ ok: boolean; error?: string }>;
  logout: () => void;
  hasPerm: (key: string) => boolean;
  /** true si la permission est détenue (global) OU scopée sur CE chantier. */
  hasPermOnChantier: (key: string, chantierId: number) => boolean;
  permCount: number;
}

interface ThemeCtx {
  theme: Theme;
  toggle: () => void;
}

interface UserCtx {
  /** Met à jour l'utilisateur connecté en mémoire (+ localStorage). */
  updateUser: (u: User) => void;
}

const Ctx = createContext<(AuthCtx & ThemeCtx & UserCtx) | null>(null);

const THEME_KEY = "locatysbtp-theme";
const USER_KEY = "locatysbtp-user";

/**
 * Convertit l'utilisateur renvoyé par le backend (UtilisateurResponse)
 * en la forme `User` utilisée par l'interface existante.
 */
function fromBackendUser(u: UtilisateurResponse, permissions: string[] = [], parChantier: Record<string, string[]> = {}): User {
  return {
    id: Number(u.id),
    prenom: u.prenom,
    nom: u.nom,
    email: u.email,
    telephone: u.telephone ?? "",
    profilId: Number(u.profilId),
    profilNom: u.profilNom ?? "",
    equipeId: null,
    permissions,
    permissionsParChantier: parChantier,
    password: "",
    role: u.profilNom === "ADMINISTRATEUR" ? "admin" : "user",
    avatar: "from-blue-500 to-indigo-600",
    dateCreation: u.dateCreation,
    dateModification: u.dateModification,
  };
}

export function AppProvider({ children }: { children: ReactNode }) {
  // ---- Theme ----
  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem(THEME_KEY) as Theme | null;
    if (saved) return saved;
    return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  });

  useEffect(() => {
    const root = document.documentElement;
    root.classList.toggle("dark", theme === "dark");
    localStorage.setItem(THEME_KEY, theme);
  }, [theme]);

  const toggle = () => setTheme((t) => (t === "dark" ? "light" : "dark"));

  // ---- Auth ----
  const [user, setUser] = useState<User | null>(() => {
    // On ne restaure une session que si un token JWT existe.
    if (!getToken()) return null;
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  });

  // Le backend a renvoyé 401 (token invalide/expiré) : on vide la session.
  useEffect(() => {
    const onUnauthorized = () => setUser(null);
    window.addEventListener("auth:unauthorized", onUnauthorized);
    return () => window.removeEventListener("auth:unauthorized", onUnauthorized);
  }, []);

  // Permissions RBAC toujours fraîches : on re-relit /auth/me au chargement
  // ET à chaque retour sur l'onglet (focus + visibilitychange + pageshow),
  // pour ne pas conserver en mémoire d'anciennes permissions (ex : un lambda
  // à qui l'admin vient d'accorder TACHE_VALIDER la verra sans se reconnecter).
  // La 1re fois (mount) est immédiate ; les suivantes sont déclenchées par
  // l'activité visible seulement, pour ne pas poller une page en arrière-plan.
  useEffect(() => {
    const refresh = () => {
      if (!getToken()) return;
      getMe()
        .then((me) => {
          const synced = fromBackendUser(me.utilisateur, me.permissions ?? [], me.permissionsParChantier ?? {});
          localStorage.setItem(USER_KEY, JSON.stringify(synced));
          setUser(synced);
        })
        .catch(() => {
          /* token invalide : l'intercepteur 401 gérera la déconnexion */
        });
    };
    const refreshOnVisible = () => {
      if (document.visibilityState === "visible") refresh();
    };
    // Autre onglet du même navigateur : une session modifiée (ou une permission
    // accordée) y est reflétée dès que cet onglet écrit dans le localStorage.
    const onStorage = (e: StorageEvent) => {
      if (e.key === USER_KEY) refresh();
    };
    // Poll tant que l'onglet est visible : un octroi fait par l'admin pendant
    // qu'on regarde la page apparaît en quelques secondes, sans action.
    const interval = window.setInterval(() => {
      if (document.visibilityState === "visible") refresh();
    }, 5000);
    refresh();
    window.addEventListener("focus", refresh);
    document.addEventListener("visibilitychange", refreshOnVisible);
    window.addEventListener("pageshow", refresh);
    window.addEventListener("hashchange", refresh);
    window.addEventListener("storage", onStorage);
    return () => {
      window.clearInterval(interval);
      window.removeEventListener("focus", refresh);
      document.removeEventListener("visibilitychange", refreshOnVisible);
      window.removeEventListener("pageshow", refresh);
      window.removeEventListener("hashchange", refresh);
      window.removeEventListener("storage", onStorage);
    };
  }, []);

  const login: AuthCtx["login"] = async (email, password) => {
    try {
      const res = await apiLogin(email, password);
      setToken(res.token);
      // Permissions effectives : toujours relues depuis /auth/me (RBAC dynamique).
      let permissions: string[] = [];
      let parChantier: Record<string, string[]> = {};
      try {
        const me = await getMe();
        permissions = me.permissions ?? [];
        parChantier = me.permissionsParChantier ?? {};
      } catch {
        /* permissions vides = modules masqués, on reste connecté */
      }
      const connected = fromBackendUser(res.utilisateur, permissions, parChantier);
      localStorage.setItem(USER_KEY, JSON.stringify(connected));
      setUser(connected);
      return { ok: true };
    } catch (err) {
      return { ok: false, error: getApiErrorMessage(err) };
    }
  };

  const register: AuthCtx["register"] = async (data) => {
    try {
      // L'inscription publique ne transmet JAMAIS de profilId : le backend
      // impose automatiquement le profil UTILISATEUR_STANDARD.
      await apiRegister({
        nom: data.nom ?? "",
        prenom: data.prenom ?? "",
        email: data.email ?? "",
        password: data.password,
        telephone: data.telephone ?? undefined,
      });
      return { ok: true };
    } catch (err) {
      return { ok: false, error: getApiErrorMessage(err) };
    }
  };

  const logout = () => {
    removeToken();
    localStorage.removeItem(USER_KEY);
    setUser(null);
  };

  const activeUser = user;

  // Permissions effectives réelles (codes backend, ex: CHANTIER_LIRE) relues
  // via /auth/me au login. L'ADMINISTRATEUR a TOUTES les permissions.
  const permCount = activeUser ? (activeUser.permissions?.length ?? 0) : 0;

  const hasPerm: AuthCtx["hasPerm"] = (key) => {
    if (!activeUser) return false;
    if (activeUser.role === "admin") return true;
    return (activeUser.permissions ?? []).includes(key);
  };

  const hasPermOnChantier: AuthCtx["hasPermOnChantier"] = (key, chantierId) => {
    if (!activeUser) return false;
    if (activeUser.role === "admin") return true;
    if ((activeUser.permissions ?? []).includes(key)) return true;
    return (activeUser.permissionsParChantier?.[chantierId] ?? []).includes(key);
  };

  const value: AuthCtx & ThemeCtx & UserCtx = {
    user: activeUser,
    login,
    register,
    logout,
    hasPerm,
    hasPermOnChantier,
    permCount,
    theme,
    toggle,
    updateUser: (u) => {
      setUser(u);
      localStorage.setItem(USER_KEY, JSON.stringify(u));
    },
  };

  return <Ctx.Provider value={useMemo(() => value, [value])}>{children}</Ctx.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useApp() {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("useApp must be used within AppProvider");
  return ctx;
}
