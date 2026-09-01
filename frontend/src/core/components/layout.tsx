import { useState, type ReactNode } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { FaChartColumn, FaBuilding, FaSquareCheck, FaHelmetSafety, FaGauge, FaRightFromBracket, FaBars, FaMoon, FaSatelliteDish, FaMagnifyingGlass, FaShieldHalved, FaSun, FaCircleUser, FaUsers, FaXmark } from "react-icons/fa6";
import { cn } from "../utils/cn";
import { useApp } from "../store/AppProvider";
import { Avatar, IconButton } from "./ui";

interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  /** Permission requise pour afficher le lien (admin = toutes permissions). */
  perm?: string;
}
const NAV: { group: string; items: NavItem[] }[] = [
  {
    group: "Pilotage",
    items: [
      { to: "/dashboard", label: "Tableau de bord", icon: <FaGauge className="h-[18px] w-[18px]" /> },
      { to: "/stats", label: "Mes statistiques", icon: <FaChartColumn className="h-[18px] w-[18px]" />, perm: "TACHE_LIRE" },
    ],
  },
  {
    group: "Production",
    items: [
      { to: "/chantiers", label: "Chantiers", icon: <FaBuilding className="h-[18px] w-[18px]" />, perm: "CHANTIER_LIRE" },
      { to: "/analyse-site", label: "Analyse du site", icon: <FaSatelliteDish className="h-[18px] w-[18px]" />, perm: "CHANTIER_LIRE" },
      { to: "/taches", label: "Tâches", icon: <FaSquareCheck className="h-[18px] w-[18px]" />, perm: "TACHE_LIRE" },
      { to: "/equipes", label: "Équipes", icon: <FaHelmetSafety className="h-[18px] w-[18px]" />, perm: "EQUIPE_LIRE" },
    ],
  },
  {
    group: "Organisation",
    items: [
      { to: "/utilisateurs", label: "Utilisateurs", icon: <FaUsers className="h-[18px] w-[18px]" />, perm: "UTILISATEUR_LIRE" },
      { to: "/habilitations", label: "Habilitations", icon: <FaShieldHalved className="h-[18px] w-[18px]" />, perm: "PROFIL_LIRE" },
      { to: "/profil", label: "Mon profil", icon: <FaCircleUser className="h-[18px] w-[18px]" /> },
    ],
  },
];

function Brand() {
  return (
    <Link to="/dashboard" className="flex items-center gap-3 px-5 py-5">
      <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-500/15 text-amber-400 ring-1 ring-amber-500/30">
        <FaHelmetSafety className="h-5 w-5" />
      </div>
      <div className="leading-tight">
        <p className="text-base font-black text-white">
          LOCATYS<span className="text-amber-400">BTP</span>
        </p>
        <p className="text-[11px] font-medium text-slate-400">Gestion de chantiers</p>
      </div>
    </Link>
  );
}

function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
  const { user, logout, hasPerm } = useApp();
  const navigate = useNavigate();

  return (
    <div className="flex h-full flex-col">
      <Brand />
      <nav className="flex-1 space-y-6 overflow-y-auto px-3 py-2 no-scrollbar">
        {NAV.map((section) => {
          const items = section.items.filter((item) => !item.perm || hasPerm(item.perm));
          if (items.length === 0) return null;
          return (
            <div key={section.group}>
              <p className="px-3 pb-2 text-[10px] font-bold uppercase tracking-wider text-slate-500">
                {section.group}
              </p>
              <div className="space-y-1">
                {items.map((item) => (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    onClick={onNavigate}
                    className={({ isActive }) =>
                      cn(
                        "group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold transition-all",
                        isActive
                          ? "bg-white/10 text-white"
                          : "text-slate-400 hover:bg-white/5 hover:text-slate-200"
                      )
                    }
                  >
                    {({ isActive }) => (
                      <>
                        <span className={cn("transition-colors", isActive ? "text-amber-400" : "text-slate-500 group-hover:text-slate-300")}>
                          {item.icon}
                        </span>
                        {item.label}
                      </>
                    )}
                  </NavLink>
                ))}
              </div>
            </div>
          );
        })}
      </nav>
      <div className="border-t border-white/10 p-3">
        <Link
          to="/profil"
          onClick={onNavigate}
          className="flex items-center gap-3 rounded-xl px-2 py-2 transition hover:bg-white/5"
        >
          <Avatar prenom={user?.prenom} nom={user?.nom} gradient={user?.avatar} size={38} />
          <div className="min-w-0 flex-1 leading-tight">
            <p className="truncate text-sm font-semibold text-white">
              {user?.prenom} {user?.nom}
            </p>
            <p className="truncate text-xs text-slate-400">{user?.profilNom}</p>
          </div>
        </Link>
        <button
          onClick={() => {
            logout();
            navigate("/login");
          }}
          className="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold text-slate-400 transition hover:bg-rose-500/10 hover:text-rose-300"
        >
          <FaRightFromBracket className="h-[18px] w-[18px]" />
          Déconnexion
        </button>
      </div>
    </div>
  );
}

function Header({ onMenu }: { onMenu: () => void }) {
  const { theme, toggle, user, permCount } = useApp();
  const [q, setQ] = useState("");
  const navigate = useNavigate();

  return (
    <header className="sticky top-0 z-40 border-b border-line glass">
      <div className="flex h-16 items-center gap-3 px-4 sm:px-6 lg:px-8">
        <IconButton className="lg:hidden" onClick={onMenu} aria-label="Menu">
          <FaBars className="h-5 w-5 text-ink" />
        </IconButton>

        <form
          onSubmit={(e) => {
            e.preventDefault();
            navigate(`/chantiers?q=${encodeURIComponent(q)}`);
          }}
          className="relative hidden max-w-md flex-1 md:block"
        >
          <FaMagnifyingGlass className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="Rechercher un chantier, une tâche…"
            className="w-full rounded-lg border border-line bg-surface/70 py-2 pl-9 pr-3 text-sm text-ink placeholder:text-muted focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/30"
          />
        </form>

        <div className="ml-auto flex items-center gap-1.5">
          <IconButton onClick={toggle} aria-label="Thème" className="text-ink">
            {theme === "dark" ? <FaSun className="h-5 w-5" /> : <FaMoon className="h-5 w-5" />}
          </IconButton>
          <Link
            to="/profil"
            className="flex items-center gap-2.5 rounded-xl border border-line bg-surface py-1.5 pl-1.5 pr-3 transition hover:bg-app"
          >
            <Avatar prenom={user?.prenom} nom={user?.nom} gradient={user?.avatar} size={32} />
            <div className="hidden text-left leading-tight sm:block">
              <p className="text-sm font-bold text-ink">
                {user?.prenom} {user?.nom}
              </p>
              <p className="text-[11px] text-muted">{permCount} permission(s)</p>
            </div>
          </Link>
        </div>
      </div>
    </header>
  );
}

export function AppLayout() {
  const [drawer, setDrawer] = useState(false);
  const location = useLocation();

  return (
    <div className="min-h-screen bg-app">
      {/* Desktop sidebar */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 bg-[#0f172a] lg:block">
        <SidebarNav />
      </aside>

      {/* Mobile drawer */}
      <AnimatePresence>
        {drawer && (
          <div className="fixed inset-0 z-50 lg:hidden">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setDrawer(false)}
              className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm"
            />
            <motion.aside
              initial={{ x: "-100%" }}
              animate={{ x: 0 }}
              exit={{ x: "-100%" }}
              transition={{ type: "spring", damping: 30, stiffness: 320 }}
              className="absolute inset-y-0 left-0 w-72 bg-[#0f172a] shadow-2xl"
            >
              <button
                onClick={() => setDrawer(false)}
                className="absolute right-3 top-5 z-10 text-slate-400 hover:text-white"
              >
                <FaXmark className="h-5 w-5" />
              </button>
              <SidebarNav onNavigate={() => setDrawer(false)} />
            </motion.aside>
          </div>
        )}
      </AnimatePresence>

      <div className="lg:pl-64">
        <Header onMenu={() => setDrawer(true)} />
        <main className="mx-auto max-w-[1400px] px-4 py-6 sm:px-6 lg:px-8">
          <motion.div
            key={location.pathname}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.28, ease: "easeOut" }}
          >
            <Outlet />
          </motion.div>
        </main>
      </div>
    </div>
  );
}
