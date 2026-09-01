import { HashRouter, Navigate, Outlet, Route, Routes } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { AppProvider, useApp } from "./core/store/AppProvider";
import { AppLayout } from "./core/components/layout";
import { AccessDenied, Login, Register } from "./auth/pages";
import Dashboard from "./dashboard/pages";
import { ChantierDetail, ChantiersList } from "./chantiers/pages";
import AnalyseSitePage from "./analyse/pages";
import { TachesList, TaskDetail } from "./taches/pages";
import { EquipeDetail, EquipesList } from "./equipes/pages";
import { Habilitations, ProfilDetail, UserDetail, UsersList } from "./utilisateurs/pages";
import MonProfil from "./profil/pages";
import MesStatistiques from "./stats/pages";

function RequireAuth() {
  const { user } = useApp();
  return user ? <Outlet /> : <Navigate to="/login" replace />;
}

function PermGuard({ perm, children }: { perm: string; children: React.ReactNode }) {
  const { hasPerm } = useApp();
  return hasPerm(perm) ? <>{children}</> : <AccessDenied />;
}

function AdminGuard({ children }: { children: React.ReactNode }) {
  const { user } = useApp();
  // Source de vérité RBAC : le profil réellement renvoyé par le backend.
  return user?.profilNom === "ADMINISTRATEUR" ? <>{children}</> : <AccessDenied />;
}

export default function App() {
  return (
    <AppProvider>
      <HashRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route element={<RequireAuth />}>
            <Route element={<AppLayout />}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/stats" element={<MesStatistiques />} />
              <Route path="/chantiers" element={<PermGuard perm="CHANTIER_LIRE"><ChantiersList /></PermGuard>} />
              <Route path="/chantiers/:id" element={<PermGuard perm="CHANTIER_LIRE"><ChantierDetail /></PermGuard>} />
              <Route path="/analyse-site" element={<PermGuard perm="CHANTIER_LIRE"><AnalyseSitePage /></PermGuard>} />
              <Route path="/taches" element={<PermGuard perm="TACHE_LIRE"><TachesList /></PermGuard>} />
              <Route path="/taches/:id" element={<PermGuard perm="TACHE_LIRE"><TaskDetail /></PermGuard>} />
              <Route path="/equipes" element={<PermGuard perm="EQUIPE_LIRE"><EquipesList /></PermGuard>} />
              <Route path="/equipes/:id" element={<PermGuard perm="EQUIPE_LIRE"><EquipeDetail /></PermGuard>} />
              <Route path="/utilisateurs" element={<PermGuard perm="UTILISATEUR_LIRE"><UsersList /></PermGuard>} />
              <Route path="/utilisateurs/:id" element={<PermGuard perm="UTILISATEUR_LIRE"><UserDetail /></PermGuard>} />
              <Route
                path="/habilitations"
                element={
                  <AdminGuard>
                    <Habilitations />
                  </AdminGuard>
                }
              />
              <Route path="/habilitations/profil/:id" element={<AdminGuard><ProfilDetail /></AdminGuard>} />
              <Route path="/profil" element={<MonProfil />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </HashRouter>
      <ToastContainer
        position="top-right"
        autoClose={2800}
        theme="light"
        newestOnTop
        closeOnClick
        pauseOnHover
      />
    </AppProvider>
  );
}
