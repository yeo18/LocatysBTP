import { useMemo } from "react";
import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { FaArrowTrendDown, FaArrowTrendUp, FaBuilding, FaSquareCheck, FaHelmetSafety, FaMinus, FaChartLine, FaUsers, FaBolt } from "react-icons/fa6";
import { useQuery } from "@tanstack/react-query";
import { getApiErrorMessage } from "../core/api/axios";
import { getChantiers } from "../chantiers/api";
import { getEquipes } from "../equipes/api";
import { getTaches, tachePrioriteLabel, tacheStatutLabel } from "../taches/api";
import { getUsers } from "../utilisateurs/api";
import type { TacheResume } from "../core/api/types";
import { useApp } from "../core/store/AppProvider";
import { Card, ErrorState, FullSpinner, PageHeader, Pill } from "../core/components/ui";
import { AreaChart, BarChart, DonutChart, LegendDot } from "../core/components/charts";
import { TaskMiniList } from "../core/components/lists";
import { cn } from "../core/utils/cn";
import type { Priorite, Tache, TacheStatut } from "../core/lib/types";
import { tacheStatutTone } from "../core/lib/styles";

function fromResume(r: TacheResume): Tache {
  return {
    id: r.id,
    titre: r.titre,
    description: "",
    chantierId: r.chantierId,
    priorite: tachePrioriteLabel(r.priorite) as Priorite,
    statut: tacheStatutLabel(r.status) as TacheStatut,
    assigneId: null,
    equipeId: null,
    dateDebut: r.dateDebut ?? "",
    dateFin: r.dateFin ?? "",
    cout: 0,
    checklist: [],
    progression: r.progression,
  };
}

function Kpi({
  to,
  icon,
  label,
  value,
  trend,
  gradient,
}: {
  to: string;
  icon: React.ReactNode;
  label: string;
  value: string | number;
  trend: { dir: "up" | "down" | "flat"; value: string };
  gradient: string;
}) {
  return (
    <Link to={to}>
      <Card className="group relative overflow-hidden p-5 transition hover:-translate-y-0.5 hover:shadow-lg">
        <div className={cn("absolute -right-8 -top-8 h-28 w-28 rounded-full opacity-10 blur-2xl", gradient)} />
        <div className="flex items-start justify-between">
          <div className={cn("flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br text-white shadow-sm", gradient)}>
            {icon}
          </div>
          <span
            className={cn(
              "inline-flex items-center gap-0.5 rounded-full px-2 py-0.5 text-xs font-bold",
              trend.dir === "up" && "bg-success-light text-success",
              trend.dir === "down" && "bg-danger-light text-danger",
              trend.dir === "flat" && "bg-slate-100 text-slate-400 dark:bg-white/10"
            )}
          >
            {trend.dir === "up" && <FaArrowTrendUp className="h-3 w-3" />}
            {trend.dir === "down" && <FaArrowTrendDown className="h-3 w-3" />}
            {trend.dir === "flat" && <FaMinus className="h-3 w-3" />}
            {trend.value}
          </span>
        </div>
        <p className="mt-4 text-3xl font-black tracking-tight text-ink">{value}</p>
        <p className="mt-0.5 text-sm font-medium text-muted">{label}</p>
      </Card>
    </Link>
  );
}

export default function Dashboard() {
  const { user, hasPerm } = useApp();
  const navigate = useNavigate();

  const canChantiers = hasPerm("CHANTIER_LIRE");
  const canTaches = hasPerm("TACHE_LIRE");
  const canEquipes = hasPerm("EQUIPE_LIRE");
  const canUsers = hasPerm("UTILISATEUR_LIRE");

  const chantiersQuery = useQuery({ queryKey: ["chantiers"], queryFn: () => getChantiers({ page: 0, size: 100 }), enabled: canChantiers });
  const tachesQuery = useQuery({ queryKey: ["taches"], queryFn: () => getTaches({ page: 0, size: 100 }), enabled: canTaches });
  const equipesQuery = useQuery({ queryKey: ["equipes"], queryFn: () => getEquipes(), enabled: canEquipes });
  const usersQuery = useQuery({ queryKey: ["users"], queryFn: () => getUsers({ page: 0, size: 100 }), enabled: canUsers });

  const chantiers = chantiersQuery.data?.content ?? [];
  const taches = useMemo(() => (tachesQuery.data?.content ?? []).map(fromResume), [tachesQuery.data]);
  const equipes = equipesQuery.data ?? [];
  const users = usersQuery.data?.content ?? [];

  const enCours = chantiers.filter((c) => c.statut === "EN_COURS").length;
  const tachesActives = taches.filter((t) => t.statut !== "Validée").length;
  const urgentes = taches.filter((t) => t.priorite === "Haute").length;

  const statutData = useMemo(() => {
    const statuses = ["À faire", "En cours", "Validée", "Refusée"] as const;
    const colors: Record<string, string> = {
      "À faire": "#94a3b8",
      "En cours": "#3b82f6",
      Validée: "#10b981",
      Refusée: "#ef4444",
    };
    return statuses.map((s) => ({
      name: s,
      value: taches.filter((t) => t.statut === s).length,
      color: colors[s],
    }));
  }, [taches]);

  const avancementData = useMemo(
    () =>
      chantiers.slice(0, 6).map((c) => ({
        name: c.nom.split(" ").slice(0, 2).join(" "),
        value: c.progression,
      })),
    [chantiers]
  );

  const prioriteData = useMemo(() => {
    const order = ["Basse", "Moyenne", "Haute"] as const;
    return order.map((p) => ({ name: p, value: taches.filter((t) => t.priorite === p).length }));
  }, [taches]);

  const recent = useMemo(
    () => [...taches].sort((a, b) => b.id - a.id).slice(0, 6),
    [taches]
  );

  const onSelectTask = (t: Tache) => navigate(`/taches/${t.id}`);

  if (chantiersQuery.isLoading || tachesQuery.isLoading || equipesQuery.isLoading || usersQuery.isLoading) {
    return <FullSpinner label="Chargement du tableau de bord…" />;
  }

  const activeError =
    (canChantiers ? chantiersQuery.error : null) ??
    (canTaches ? tachesQuery.error : null) ??
    (canEquipes ? equipesQuery.error : null) ??
    (canUsers ? usersQuery.error : null);
  if (activeError) {
    return <ErrorState message={getApiErrorMessage(activeError)} />;
  }

  return (
    <div>
      <PageHeader title="Tableau de bord" subtitle="Vue d'ensemble de votre activité de chantier.">
        <Pill tone="blue">
          {taches.length} tâches · {chantiers.length} chantiers · {equipes.length} équipes · {users.length} utilisateurs
        </Pill>
      </PageHeader>

      {/* KPIs */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {canChantiers && <Kpi to="/chantiers" icon={<FaBuilding className="h-6 w-6" />} label="Chantiers actifs" value={enCours} trend={{ dir: enCours > 0 ? "up" : "flat", value: enCours > 0 ? `${Math.min(100, enCours * 7)}%` : "0%" }} gradient="from-blue-500 to-indigo-600" />}
        {canTaches && <Kpi to="/taches" icon={<FaSquareCheck className="h-6 w-6" />} label="Tâches en cours" value={tachesActives} trend={{ dir: tachesActives > 0 ? "up" : "flat", value: tachesActives > 0 ? `${Math.min(100, tachesActives * 5)}%` : "0%" }} gradient="from-violet-500 to-purple-600" />}
        {canEquipes && <Kpi to="/equipes" icon={<FaHelmetSafety className="h-6 w-6" />} label="Équipes" value={equipes.length} trend={{ dir: equipes.length > 0 ? "up" : "flat", value: equipes.length > 0 ? `${equipes.length * 3}%` : "0%" }} gradient="from-emerald-500 to-teal-600" />}
        {canUsers && <Kpi to="/utilisateurs" icon={<FaUsers className="h-6 w-6" />} label="Utilisateurs" value={users.length} trend={{ dir: "flat", value: "0%" }} gradient="from-rose-500 to-pink-600" />}
      </div>

      {/* Charts row */}
      <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="p-5">
          <h3 className="font-bold text-ink">Répartition par statut</h3>
          <p className="text-xs text-muted">État des tâches du système</p>
          <div className="mt-4 flex flex-col items-center gap-5">
            <DonutChart data={statutData} />
            <div className="w-full space-y-1">
              {statutData.map((s) => (
                <LegendDot key={s.name} color={s.color} label={s.name} value={s.value} />
              ))}
            </div>
          </div>
        </Card>

        <Card className="p-5 lg:col-span-2">
          <h3 className="font-bold text-ink">Avancement des chantiers</h3>
          <p className="text-xs text-muted">Taux de complétion (%)</p>
          <div className="mt-6">
            <BarChart data={avancementData} color="#3b82f6" suffix="%" />
          </div>
        </Card>
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="p-5">
          <h3 className="font-bold text-ink">Priorité des tâches</h3>
          <p className="text-xs text-muted">Charge par niveau de priorité</p>
          <div className="mt-4">
            <AreaChart data={prioriteData} color="#1e3a5f" />
          </div>
          <div className="mt-2 flex flex-wrap gap-2">
            {prioriteData.map((p) => (
              <span key={p.name} className="inline-flex items-center gap-1.5 rounded-full bg-app px-2.5 py-1 text-xs font-semibold text-body">
                <span
                  className={cn(
                    "h-2 w-2 rounded-full",
                    p.name === "Haute" && "bg-danger",
                    p.name === "Moyenne" && "bg-info",
                    p.name === "Basse" && "bg-slate-400"
                  )}
                />
                {p.name} · {p.value}
              </span>
            ))}
          </div>
        </Card>

        <Card className="p-5 lg:col-span-2">
          <div className="mb-1 flex items-center justify-between">
            <div>
              <h3 className="font-bold text-ink">Activité récente</h3>
              <p className="text-xs text-muted">Dernières tâches mises à jour</p>
            </div>
            <Link to="/taches" className="inline-flex items-center gap-1 text-sm font-semibold text-accent hover:text-accent-soft">
              <FaChartLine className="h-4 w-4" /> Voir tout
            </Link>
          </div>
          <TaskMiniList tasks={recent} onSelect={onSelectTask} />
        </Card>
      </div>

      {/* Mini stats */}
      <div className="mt-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
        {[
          { label: "Équipes", value: equipes.length, tone: tacheStatutTone("En cours"), icon: <FaHelmetSafety className="h-5 w-5" /> },
          { label: "Membres", value: users.length, tone: tacheStatutTone("Validée"), icon: <FaUsers className="h-5 w-5" /> },
          { label: "Tâches", value: taches.length, tone: tacheStatutTone("À faire"), icon: <FaSquareCheck className="h-5 w-5" /> },
          { label: "Haute priorité", value: urgentes, tone: tacheStatutTone("Bloquée"), icon: <FaBolt className="h-5 w-5" /> },
        ].map((s) => (
          <motion.div key={s.label}>
            <Card className="flex items-center gap-3 p-4">
              <span className={cn(
                "flex h-10 w-10 items-center justify-center rounded-xl",
                s.tone === "blue" && "bg-info-light text-info",
                s.tone === "emerald" && "bg-success-light text-success",
                s.tone === "slate" && "bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-300",
                s.tone === "rose" && "bg-danger-light text-danger"
              )}>
                {s.icon}
              </span>
              <div>
                <p className="text-2xl font-black text-ink">{s.value}</p>
                <p className="text-xs font-medium text-muted">{s.label}</p>
              </div>
            </Card>
          </motion.div>
        ))}
      </div>

      <p className="mt-6 text-center text-xs text-muted">
        Connecté en tant que <span className="font-semibold text-ink">{user?.prenom} {user?.nom}</span>
      </p>
    </div>
  );
}
