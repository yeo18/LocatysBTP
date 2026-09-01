import { useMemo, useState } from "react";
import { FaArrowLeft, FaBuilding, FaChartColumn, FaChartLine, FaChartPie, FaChevronRight, FaCircleCheck, FaCubes, FaFire, FaLocationDot, FaStopwatch } from "react-icons/fa6";
import { useQuery } from "@tanstack/react-query";
import { Badge, Button, Card, EmptyState, ErrorState, Field, FullSpinner, InfoItem, PageHeader, Progress, SearchBox, SelectInput } from "../core/components/ui";
import { BarChart } from "../core/components/charts";
import { getApiErrorMessage } from "../core/api/axios";
import { getChantiers, chantierStatutLabel } from "../chantiers/api";
import { getTaches } from "../taches/api";
import { chantierStatutTone } from "../core/lib/styles";
import { cn } from "../core/utils/cn";
import type { ChantierResume, TacheResume } from "../core/api/types";
import type { ChantierStatut } from "../core/lib/types";

const STATUT_LABELS: Record<string, string> = {
  A_FAIRE: "À faire",
  EN_COURS: "En cours",
  VALIDE: "Validée",
  REFUSE: "Refusée",
};
const STATUT_COLORS: Record<string, string> = {
  A_FAIRE: "#94a3b8",
  EN_COURS: "#3b82f6",
  VALIDE: "#10b981",
  REFUSE: "#ef4444",
};

/** Une tâche est retenue si elle correspond au chantier et si sa période
 *  chevauche [du, au] (dates nulles = non borné). */
function inRange(t: TacheResume, chantierId: string, du: string, au: string): boolean {
  if (chantierId && t.chantierId !== Number(chantierId)) return false;
  const d = du ? new Date(du).getTime() : null;
  const a = au ? new Date(au).getTime() : null;
  const start = t.dateDebut ? new Date(t.dateDebut).getTime() : null;
  const end = t.dateFin ? new Date(t.dateFin).getTime() : null;
  if (d !== null && end !== null && end < d) return false;
  if (a !== null && start !== null && start > a) return false;
  return true;
}

export default function MesStatistiques() {
  const [mode, setMode] = useState<"select" | "global" | "chantier">("select");
  const [selectedChantier, setSelectedChantier] = useState<ChantierResume | null>(null);
  const [du, setDu] = useState("");
  const [au, setAu] = useState("");
  const [chantierSearch, setChantierSearch] = useState("");
  const [chantierStatutFilter, setChantierStatutFilter] = useState("");

  const tachesQuery = useQuery({
    queryKey: ["taches-stats"],
    queryFn: () => getTaches({ page: 0, size: 100 }),
  });
  const chantiersQuery = useQuery({
    queryKey: ["chantiers-stats"],
    queryFn: () => getChantiers({ page: 0, size: 100 }),
  });

  const allTaches = tachesQuery.data?.content ?? [];
  const chantiers = chantiersQuery.data?.content ?? [];

  const countParChantier = useMemo(() => {
    const counts = new Map<number, number>();
    for (const t of allTaches) counts.set(t.chantierId, (counts.get(t.chantierId) ?? 0) + 1);
    return counts;
  }, [allTaches]);

  const filteredChantiers = useMemo(() => {
    const s = chantierSearch.toLowerCase();
    return chantiers.filter(
      (c) =>
        c.statut !== "ANNULE" &&
        c.nom.toLowerCase().includes(s) &&
        (!chantierStatutFilter || c.statut === chantierStatutFilter)
    );
  }, [chantiers, chantierSearch, chantierStatutFilter]);

  if (tachesQuery.isLoading || chantiersQuery.isLoading) {
    return <FullSpinner label="Chargement des statistiques…" />;
  }
  if (tachesQuery.isError || chantiersQuery.isError) {
    const err = tachesQuery.error ?? chantiersQuery.error;
    return <ErrorState message={getApiErrorMessage(err)} />;
  }

  const chantierId = selectedChantier ? String(selectedChantier.id) : "";
  const taches = allTaches.filter((t) => inRange(t, chantierId, du, au));
  const filtered = !!(du || au);

  const done = taches.filter((t) => t.status === "VALIDE").length;
  const inProgress = taches.filter((t) => t.status === "EN_COURS").length;
  const high = taches.filter((t) => t.priorite === "HAUTE").length;
  const chantierCount = new Set(taches.map((t) => t.chantierId)).size;
  const completion = taches.length ? Math.round((done / taches.length) * 100) : 0;
  const progression = taches.length ? Math.round(taches.reduce((s, t) => s + t.progression, 0) / taches.length) : 0;

  const stats = [
    { label: "Tâches visibles", value: taches.length, tone: "blue", icon: <FaChartColumn className="h-5 w-5" /> },
    { label: "Validées", value: done, tone: "emerald", icon: <FaCircleCheck className="h-5 w-5" /> },
    { label: "Haute priorité", value: high, tone: "amber", icon: <FaFire className="h-5 w-5" /> },
    { label: "Chantiers", value: chantierCount, tone: "violet", icon: <FaCubes className="h-5 w-5" /> },
    { label: "En cours", value: inProgress, tone: "indigo", icon: <FaStopwatch className="h-5 w-5" /> },
    { label: "Progression moyenne", value: `${progression}%`, tone: "cyan", icon: <FaChartLine className="h-5 w-5" /> },
  ];

  const chartParChantier = chantiers
    .map((c) => ({ name: c.nom.split(" ").slice(0, 2).join(" "), value: taches.filter((t) => t.chantierId === c.id).length }))
    .filter((d) => d.value > 0);

  const chartParStatut = ["A_FAIRE", "EN_COURS", "VALIDE", "REFUSE"]
    .map((s) => ({ name: STATUT_LABELS[s], value: taches.filter((t) => t.status === s).length, color: STATUT_COLORS[s] }));

  const details = [
    { label: "Total tâches", value: taches.length, tone: "blue" },
    { label: "Taux de complétion", value: `${completion}%`, tone: "emerald" },
    { label: "En cours", value: inProgress, tone: "indigo" },
    { label: "Haute priorité", value: high, tone: "amber" },
    { label: "Progression moyenne", value: `${progression}%`, tone: "cyan" },
    { label: "Chantiers", value: chantierCount, tone: "violet" },
  ] as const;

  const resetDates = () => {
    setDu("");
    setAu("");
  };

  const selectChantier = (c: ChantierResume) => {
    setSelectedChantier(c);
    setMode("chantier");
    setChantierSearch("");
    setChantierStatutFilter("");
  };
  const selectGlobal = () => {
    setSelectedChantier(null);
    setMode("global");
    setChantierSearch("");
    setChantierStatutFilter("");
  };
  const retourSelection = () => {
    setSelectedChantier(null);
    setMode("select");
  };

  const dateInputClass = "w-full rounded-lg border border-line bg-surface px-3 py-2.5 text-sm text-ink focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/30";

  if (mode === "select") {
    return (
      <div>
        <PageHeader title="Mes statistiques" subtitle="Choisissez un chantier pour voir ses indicateurs, ou affichez les statistiques globales." />

        <Card className="mb-4 p-4">
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <SearchBox value={chantierSearch} onChange={setChantierSearch} placeholder="Rechercher un chantier…" />
            <SelectInput value={chantierStatutFilter} onChange={(e) => setChantierStatutFilter(e.target.value)}>
              <option value="">Tous les statuts</option>
              <option value="PREVU">Planifié</option>
              <option value="EN_COURS">En cours</option>
              <option value="TERMINE">Terminé</option>
            </SelectInput>
          </div>
        </Card>

        <Card className="mb-4 overflow-hidden">
          <button
            type="button"
            onClick={selectGlobal}
            className="group flex w-full items-center justify-between gap-4 bg-gradient-to-r from-accent to-accent-soft p-5 text-left transition hover:brightness-110 sm:p-6"
          >
            <div className="flex items-center gap-4">
              <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-white/15 text-white ring-1 ring-white/20">
                <FaChartPie className="h-6 w-6" />
              </span>
              <div>
                <p className="text-lg font-black text-white">Tous les chantiers</p>
                <p className="text-sm text-white/80">Statistiques globales de l'ensemble de vos chantiers.</p>
              </div>
            </div>
            <FaChevronRight className="h-5 w-5 text-white/70 transition group-hover:translate-x-1 group-hover:text-white" />
          </button>
        </Card>

        {chantiersQuery.isLoading ? (
          <FullSpinner label="Chargement des chantiers…" />
        ) : filteredChantiers.length === 0 ? (
          <EmptyState title="Aucun chantier" description="Créez d'abord un chantier pour consulter ses statistiques." />
        ) : (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
            {filteredChantiers.map((c) => (
              <button
                key={c.id}
                type="button"
                onClick={() => selectChantier(c)}
                className="group relative flex cursor-pointer flex-col rounded-3xl border border-line bg-card p-5 text-left shadow-card transition hover:-translate-y-0.5 hover:shadow-lg"
              >
                <span className="absolute right-4 top-4 flex h-7 w-7 items-center justify-center rounded-full bg-accent/10 text-accent transition group-hover:bg-accent group-hover:text-white"><FaChevronRight className="h-4 w-4" /></span>
                <span className="mb-2 flex h-11 w-11 items-center justify-center rounded-xl bg-info-light text-info"><FaBuilding className="h-5 w-5" /></span>
                <p className="truncate font-bold text-ink">{c.nom}</p>
                <p className="mt-0.5 flex items-center gap-1 truncate text-xs text-muted"><FaLocationDot className="h-3 w-3" />{c.adresseSaisie || "-"}</p>
                <div className="mt-3 flex items-center justify-between">
                  <Badge tone={chantierStatutTone(chantierStatutLabel(c.statut) as ChantierStatut)} dot>{chantierStatutLabel(c.statut)}</Badge>
                  <span className="text-xs font-semibold text-muted">{countParChantier.get(c.id) ?? 0} tâche(s)</span>
                </div>
                <div className="mt-3 border-t border-line pt-3">
                  <div className="mb-1 flex items-center justify-between text-xs">
                    <span className="text-muted">Avancement</span>
                    <span className="font-semibold text-ink">{c.progression}%</span>
                  </div>
                  <Progress value={c.progression} tone={c.progression >= 100 ? "emerald" : "blue"} showLabel={false} className="h-1.5" />
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title={mode === "global" ? "Mes statistiques" : "Statistiques du chantier"}
        subtitle={mode === "global" ? "Statistiques globales de tous vos chantiers." : `Chantier : ${selectedChantier?.nom}`}
      >
        <Button variant="secondary" icon={<FaArrowLeft className="h-4 w-4" />} onClick={retourSelection}>Sélectionner un chantier</Button>
      </PageHeader>

      <Card className="mb-4 p-4">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Field label="Chantier">
            <div className="flex items-center rounded-lg border border-line bg-app px-3 py-2.5 text-sm text-body">
              <span className={cn("mr-2.5 flex h-6 w-6 items-center justify-center rounded-md", mode === "global" ? "bg-accent/10 text-accent" : "bg-info-light text-info")}>
                {mode === "global" ? <FaChartPie className="h-3.5 w-3.5" /> : <FaBuilding className="h-3.5 w-3.5" />}
              </span>
              <span className="truncate font-semibold">{mode === "global" ? "Tous les chantiers" : selectedChantier?.nom}</span>
            </div>
          </Field>
          <Field label="Du"><input type="date" value={du} onChange={(e) => setDu(e.target.value)} className={dateInputClass} /></Field>
          <Field label="Au"><input type="date" value={au} onChange={(e) => setAu(e.target.value)} className={dateInputClass} /></Field>
          <div className="flex items-end">
            <Button variant="secondary" className="w-full" onClick={resetDates}>Réinitialiser</Button>
          </div>
        </div>
        {filtered && <p className="mt-2 text-xs text-muted">{taches.length} tâche(s) affichée(s) sur {allTaches.length} au total.</p>}
      </Card>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-3 xl:grid-cols-6">
        {stats.map((s) => (
          <Card key={s.label} className="p-4">
            <span className={cn("flex h-10 w-10 items-center justify-center rounded-xl", s.tone === "blue" && "bg-info-light text-info", s.tone === "emerald" && "bg-success-light text-success", s.tone === "amber" && "bg-warning-light text-warning", s.tone === "violet" && "bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-300", s.tone === "indigo" && "bg-indigo-100 text-indigo-600 dark:bg-indigo-500/15 dark:text-indigo-300", s.tone === "cyan" && "bg-cyan-100 text-cyan-600 dark:bg-cyan-500/15 dark:text-cyan-300")}>{s.icon}</span>
            <p className="mt-3 text-2xl font-black text-ink">{s.value}</p>
            <p className="text-xs text-muted">{s.label}</p>
          </Card>
        ))}
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card className="p-5">
          <h3 className="font-bold text-ink">{mode === "global" ? "Répartition par chantier" : "Répartition par statut"}</h3>
          <p className="text-xs text-muted">{mode === "global" ? "Nombre de tâches visibles par chantier" : "État des tâches de ce chantier"}</p>
          <div className="mt-6">
            {mode === "global" ? (
              chartParChantier.length ? <BarChart data={chartParChantier} color="#1e3a5f" /> : <p className="py-10 text-center text-sm text-muted">Aucune tâche visible.</p>
            ) : (
              chartParStatut.length ? <BarChart data={chartParStatut.map((d) => ({ name: d.name, value: d.value }))} color="#3b82f6" /> : <p className="py-10 text-center text-sm text-muted">Aucune tâche visible.</p>
            )}
          </div>
          {mode === "chantier" && (
            <div className="mt-3 flex flex-wrap gap-2">
              {chartParStatut.map((d) => (
                <span key={d.name} className="inline-flex items-center gap-1.5 rounded-full bg-app px-2.5 py-1 text-xs font-semibold text-body">
                  <span className="h-2 w-2 rounded-full" style={{ background: d.color }} />
                  {d.name} · {d.value}
                </span>
              ))}
            </div>
          )}
        </Card>

        <Card className="p-5">
          <h3 className="mb-3 font-bold text-ink">Détail de ma production</h3>
          <div className="grid grid-cols-2 gap-4">
            {details.map((d) => (
              <InfoItem key={d.label} label={d.label} value={<span className={cn(d.tone === "blue" && "text-info", d.tone === "emerald" && "text-success", d.tone === "amber" && "text-warning", d.tone === "violet" && "text-violet-500", d.tone === "indigo" && "text-indigo-500", d.tone === "cyan" && "text-cyan-500")}>{d.value}</span>} />
            ))}
          </div>
        </Card>
      </div>

      <p className="mt-6 text-center text-xs text-muted">Taux de complétion global : <span className="font-bold text-ink">{completion}%</span> · Progression moyenne : <span className="font-bold text-ink">{progression}%</span></p>
    </div>
  );
}