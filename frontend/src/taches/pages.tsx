import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { FaArrowLeft, FaCircleCheck, FaBuilding, FaCalendarDays, FaCheck, FaChevronRight, FaCircleHalfStroke, FaClock, FaEye, FaTableCellsLarge, FaSpinner, FaLocationDot, FaPen, FaPlus, FaRotate, FaTrashCan, FaXmark, FaCircleXmark } from "react-icons/fa6";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import {
  Badge,
  Button,
  Card,
  ConfirmModal,
  EmptyState,
  ErrorState,
  Field,
  FullSpinner,
  InfoGrid,
  InfoItem,
  Modal,
  PageHeader,
  PriorityBadge,
  Pill,
  Progress,
  SearchBox,
  SearchSelect,
  SelectInput,
  TextArea,
  TextInput,
} from "../core/components/ui";
import { formatDate, durationLabel } from "../core/lib/format";
import { chantierStatutTone, tacheStatutTone } from "../core/lib/styles";
import { validateDateRange, isValidDate, today } from "../core/lib/validation";
import { cn } from "../core/utils/cn";
import type { ChantierStatut, Priorite, Tache, TacheStatut } from "../core/lib/types";
import type { ChantierResume, CreateAffectationTacheRequest, TacheResponse, TacheResume, TacheStatus, UpdateTacheRequest } from "../core/api/types";
import { getApiErrorMessage } from "../core/api/axios";
import { chantierStatutLabel, getChantiers } from "../chantiers/api";
import { getEquipes, getEquipeMembres } from "../equipes/api";
import { getUsers } from "../utilisateurs/api";
import { useApp } from "../core/store/AppProvider";
import {
  affecterTache,
  createTache,
  deleteTache,
  getTache,
  getTacheAffectations,
  getTaches,
  getValidations,
  refuserTache,
  retirerAffectation,
  tachePrioriteEnum,
  tachePrioriteLabel,
  tacheStatutEnum,
  tacheStatutLabel,
  updateTache,
  validerTache,
} from "../taches/api";

/** Priorités réellement supportées par le backend (pas de "Urgente"). */
const PRIOS: Priorite[] = ["Basse", "Moyenne", "Haute"];
/** Statuts réellement supportés par le backend (pas de "Bloquée"). */
const STATUTS: TacheStatut[] = ["À faire", "En cours", "Validée", "Refusée"];
const STATUT_ICONS = [<FaCircleHalfStroke className="h-5 w-5" />, <FaSpinner className="h-5 w-5" />, <FaCircleCheck className="h-5 w-5" />, <FaCircleCheck className="h-5 w-5" />, <FaCircleXmark className="h-5 w-5" />];

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

function fromDetail(r: TacheResponse): Tache {
  return {
    id: r.id,
    titre: r.titre,
    description: r.description ?? "",
    chantierId: r.chantierId,
    chantierNom: r.chantierNom,
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

/** Construit le payload PUT à partir d'une tâche locale (et d'éventuels champs modifiés). */
function buildUpdate(t: Tache, overrides?: Partial<UpdateTacheRequest>): UpdateTacheRequest {
  return {
    titre: t.titre,
    description: t.description || undefined,
    priorite: tachePrioriteEnum(t.priorite) ?? "MOYENNE",
    status: tacheStatutEnum(t.statut) as TacheStatus,
    progression: t.progression ?? 0,
    dateDebut: t.dateDebut || undefined,
    dateFin: t.dateFin || undefined,
    ...overrides,
  };
}

export function TachesList() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, hasPerm } = useApp();
  const [searchParams, setSearchParams] = useSearchParams();
  const [q, setQ] = useState("");
  const [prio, setPrio] = useState("");
  const [statut, setStatut] = useState("");
  const [view, setView] = useState<"grid" | "kanban">("grid");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Tache | null>(null);
  const [toDelete, setToDelete] = useState<Tache | null>(null);
  const [selectedChantier, setSelectedChantier] = useState<ChantierResume | null>(null);
  // bloque la restauration depuis l'URL quand l'utilisateur a explicitement
  // cliqué « Sélectionner un autre chantier » (sinon course entre le state
  // et la navigation : le premier clic restaure par erreur l'ancien chantier).
  const clearedManually = useRef(false);
  const [chantierSearch, setChantierSearch] = useState("");
  const [chantierStatutFilter, setChantierStatutFilter] = useState("");

  const [form, setForm] = useState({
    titre: "",
    description: "",
    chantierId: 0,
    priorite: "Moyenne" as Priorite,
    statut: "À faire" as TacheStatut,
    dateDebut: "",
    dateFin: "",
    progression: 0,
    affecterUserId: "",
    affecterEquipeId: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const chantiersQuery = useQuery({
    queryKey: ["chantiers", "taches"],
    queryFn: () => getChantiers({ page: 0, size: 100 }),
  });

  const allTachesQuery = useQuery({
    queryKey: ["taches", "toutes"],
    queryFn: () => getTaches({ page: 0, size: 100, sort: "id", direction: "DESC" }),
  });

  const canSeeUsers = hasPerm("UTILISATEUR_LIRE");
  const canCreateTaches = hasPerm("TACHE_CREER");
  const canEditTaches = hasPerm("TACHE_MODIFIER");
  const canDeleteTaches = hasPerm("TACHE_SUPPRIMER");
  const canCreateChantiers = hasPerm("CHANTIER_CREER");
  const canValiderTaches = hasPerm("TACHE_VALIDER");

  const usersQuery = useQuery({
    queryKey: ["users", "affectation"],
    queryFn: () => getUsers({ page: 0, size: 100 }),
    enabled: canSeeUsers,
  });

  const equipesQuery = useQuery({
    queryKey: ["equipes", "affectation"],
    queryFn: getEquipes,
  });

  const chantiers = chantiersQuery.data?.content ?? [];
  const chantierName = (id: number) => chantiers.find((c) => c.id === id)?.nom ?? "-";

  // Tâches du chantier sélectionné (comptage + stats), sinon toutes.
  const allResume = (allTachesQuery.data?.content ?? []).map(fromResume);
  const chantiersAvecTaches = useMemo(() => {
    const counts = new Map<number, number>();
    for (const t of allResume) counts.set(t.chantierId, (counts.get(t.chantierId) ?? 0) + 1);
    return counts;
  }, [allResume]);

  const scopedTaches = selectedChantier
    ? allResume.filter((t) => t.chantierId === selectedChantier.id)
    : allResume;

  // Mémorise le chantier choisi dans l'URL (?chantier=) pour le garder au
  // rafraîchissement / retour navigation ; restaure la sélection au chargement.
  useEffect(() => {
    if (clearedManually.current) return;
    if (!selectedChantier) {
      const id = Number(searchParams.get("chantier"));
      const found = chantiers.find((c) => c.id === id && c.statut !== "ANNULE");
      if (found) setSelectedChantier(found);
    }
  }, [searchParams, chantiers, selectedChantier]);

  const selectChantier = (c: ChantierResume) => {
    clearedManually.current = false;
    setSelectedChantier(c);
    const next = new URLSearchParams(searchParams);
    next.set("chantier", String(c.id));
    setSearchParams(next, { replace: true });
  };
  const clearChantier = () => {
    clearedManually.current = true;
    setSelectedChantier(null);
    const next = new URLSearchParams(searchParams);
    next.delete("chantier");
    setSearchParams(next, { replace: true });
  };

  const filteredChantiers = useMemo(() => {
    const s = chantierSearch.toLowerCase();
    return chantiers.filter(
      (c) =>
        c.statut !== "ANNULE" &&
        c.nom.toLowerCase().includes(s) &&
        (!chantierStatutFilter || c.statut === chantierStatutFilter)
    );
  }, [chantiers, chantierSearch, chantierStatutFilter]);

  const filtered = useMemo(
    () =>
      scopedTaches.filter(
        (t) =>
          t.titre.toLowerCase().includes(q.toLowerCase()) &&
          (!prio || t.priorite === prio) &&
          (!statut || t.statut === statut)
      ),
    [scopedTaches, q, prio, statut]
  );

  const totalPct = scopedTaches.length
    ? Math.round(scopedTaches.reduce((s, t) => s + (t.progression ?? 0), 0) / scopedTaches.length)
    : 0;
  const totalValidees = scopedTaches.filter((t) => t.statut === "Validée").length;
  const activeFilters = q || prio || statut;
  const reset = () => { setQ(""); setPrio(""); setStatut(""); };

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["taches"] });
    // La progression des chantiers dépend des tâches : on la rafraîchit
    // aussi pour que les cartes affichent le vrai pourcentage.
    queryClient.invalidateQueries({ queryKey: ["chantiers"] });
  };

  const saveMutation = useMutation({
    mutationFn: async () => {
      const ers: Record<string, string> = {};
      if (!form.titre.trim()) ers.titre = "Le titre est obligatoire.";
      else if (/[<>{}]/.test(form.titre)) ers.titre = "Le titre contient des caractères non autorisés.";
      if (!editing && !form.chantierId) ers.chantierId = "Le chantier est obligatoire.";
      if (form.dateDebut && !isValidDate(form.dateDebut)) ers.dateDebut = "La date de début ne peut pas être dans le passé.";
      if (form.dateFin && !isValidDate(form.dateFin)) ers.dateFin = "La date de fin ne peut pas être dans le passé.";
      const rng = validateDateRange(form.dateDebut, form.dateFin);
      if (rng) {
        if (rng.includes("fin")) ers.dateFin = rng; else ers.dateDebut = rng;
      }
      if (Object.keys(ers).length > 0) {
        setErrors(ers);
        throw { validation: true };
      }
      setErrors({});
      if (editing) {
        return updateTache(editing.id, buildUpdate(editing, {
          titre: form.titre,
          description: form.description || undefined,
          priorite: tachePrioriteEnum(form.priorite) ?? "MOYENNE",
          status: tacheStatutEnum(form.statut) as TacheStatus,
          progression: form.statut === "Validée" ? 100 : form.progression,
          dateDebut: form.dateDebut || undefined,
          dateFin: form.dateFin || undefined,
        }));
      }
      const created = await createTache({
        titre: form.titre,
        description: form.description || undefined,
        priorite: tachePrioriteEnum(form.priorite) ?? "MOYENNE",
        dateDebut: form.dateDebut || undefined,
        dateFin: form.dateFin || undefined,
        chantierId: form.chantierId,
      });
      // La liste GET /taches est filtrée par accès : sans affectation,
      // la tâche est invisible pour le créateur. On affecte donc la
      // cible choisie dans le formulaire (utilisateur ou équipe), sinon
      // le créateur (REALISATEUR) pour qu'elle apparaisse immédiatement.
      const dateAffectation = new Date().toISOString().slice(0, 10);
      const role = "REALISATEUR" as const;
      const ciblage: CreateAffectationTacheRequest | null =
        form.affecterUserId || form.affecterEquipeId || (user && user.id != null)
          ? {
              tacheId: created.id,
              role,
              dateAffectation,
              ...(form.affecterUserId ? { utilisateurId: Number(form.affecterUserId) } : {}),
              ...(form.affecterEquipeId ? { equipeId: Number(form.affecterEquipeId) } : {}),
              ...(!form.affecterUserId && !form.affecterEquipeId && user && user.id != null
                ? { utilisateurId: Number(user.id) }
                : {}),
            }
          : null;
      if (ciblage) {
        try {
          await affecterTache(ciblage);
        } catch {
          // L'affectation échouera si l'utilisateur n'a pas TACHE_MODIFIER
          // ou n'a pas accès au chantier : la tâche reste créée.
        }
      }
      return created;
    },
    onSuccess: () => {
      toast.success(editing ? "La tâche a été modifiée." : "La tâche a été créée.");
      invalidate();
      setOpen(false);
    },
    onError: (err) => {
      if ((err as { validation?: boolean }).validation) {
        toast.error("Veuillez corriger les champs signalés.");
        return;
      }
      toast.error(getApiErrorMessage(err));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteTache(id),
    onSuccess: () => {
      toast.success("La tâche a été supprimée.");
      invalidate();
      setToDelete(null);
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const statutMutation = useMutation({
    mutationFn: (payload: { t: Tache; statut: TacheStatut }) =>
      updateTache(payload.t.id, buildUpdate(payload.t, { status: tacheStatutEnum(payload.statut) as TacheStatus })),
    onSuccess: () => {
      toast.success("Statut mis à jour.");
      invalidate();
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const validerMutation = useMutation({
    mutationFn: (t: Tache) => validerTache(t.id, {
      dateValidation: new Date().toISOString().slice(0, 10),
    }),
    onSuccess: () => {
      toast.success("Tâche validée.");
      invalidate();
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const openNew = () => {
    setEditing(null);
    setErrors({});
    setForm({ titre: "", description: "", chantierId: selectedChantier?.id ?? chantiers[0]?.id ?? 0, priorite: "Moyenne", statut: "À faire", dateDebut: "", dateFin: "", progression: 0, affecterUserId: "", affecterEquipeId: "" });
    setOpen(true);
  };
  const openEdit = (t: Tache) => {
    setEditing(t);
    setErrors({});
    setForm({
      titre: t.titre,
      description: t.description,
      chantierId: t.chantierId,
      priorite: t.priorite,
      statut: t.statut,
      dateDebut: t.dateDebut,
      dateFin: t.dateFin,
      progression: t.progression ?? 0,
      affecterUserId: "",
      affecterEquipeId: "",
    });
    setOpen(true);
  };

  return (
    <div>
      {!selectedChantier ? (
        <>
          <PageHeader title="Tâches" subtitle="Choisissez un chantier, puis créez et suivez ses tâches ici." />
          <Card className="mb-4 p-4">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
              <SearchBox value={chantierSearch} onChange={setChantierSearch} placeholder="Rechercher un chantier…" />
              <SelectInput value={chantierStatutFilter} onChange={(e) => setChantierStatutFilter(e.target.value)}>
                <option value="">Tous les statuts</option>
                <option value="PREVU">Planifié</option>
                <option value="EN_COURS">En cours</option>
                <option value="TERMINE">Terminé</option>
              </SelectInput>
              <div className="flex items-center justify-end">
                {canCreateChantiers && <Button icon={<FaPlus className="h-4 w-4" />} onClick={() => navigate("/chantiers")}>Nouveau chantier</Button>}
              </div>
            </div>
          </Card>

          {chantiersQuery.isLoading ? (
            <FullSpinner label="Chargement des chantiers…" />
          ) : chantiersQuery.isError ? (
            <ErrorState message={getApiErrorMessage(chantiersQuery.error)} />
          ) : filteredChantiers.length === 0 ? (
            <EmptyState title="Aucun chantier" description="Créez d'abord un chantier pour pouvoir ajouter des tâches." />
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
              {filteredChantiers.map((c) => (
                <div
                  key={c.id}
                  onClick={() => selectChantier(c)}
                  role="button"
                  tabIndex={0}
                  onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); selectChantier(c); } }}
                  className="group relative flex cursor-pointer flex-col rounded-3xl border border-line bg-card p-5 text-left shadow-card transition hover:-translate-y-0.5 hover:shadow-lg"
                >
                  <span className="absolute right-4 top-4 flex h-7 w-7 items-center justify-center rounded-full bg-accent/10 text-accent transition group-hover:bg-accent group-hover:text-white"><FaChevronRight className="h-4 w-4" /></span>
                  <span className="mb-2 flex h-11 w-11 items-center justify-center rounded-xl bg-info-light text-info"><FaBuilding className="h-5 w-5" /></span>
                  <p className="truncate font-bold text-ink">{c.nom}</p>
                  <p className="mt-0.5 flex items-center gap-1 truncate text-xs text-muted"><FaLocationDot className="h-3 w-3" />{c.adresseSaisie || "-"}</p>
                  <div className="mt-3 flex items-center justify-between">
                    <Badge tone={chantierStatutTone(chantierStatutLabel(c.statut) as ChantierStatut)} dot>{chantierStatutLabel(c.statut)}</Badge>
                    <span className="text-xs font-semibold text-muted">{chantiersAvecTaches.get(c.id) ?? 0} tâche(s)</span>
                  </div>
                  <div className="mt-3 border-t border-line pt-3"><div className="mb-1 flex items-center justify-between text-xs"><span className="text-muted">Avancement</span><span className="font-semibold text-ink">{c.progression}%</span></div><Progress value={c.progression} tone={c.progression >= 100 ? "emerald" : "blue"} showLabel={false} className="h-1.5" /></div>
                  <button
                    onClick={(e) => { e.stopPropagation(); selectChantier(c); }}
                    className="mt-3 inline-flex items-center justify-center gap-1.5 rounded-lg bg-app px-2.5 py-1.5 text-xs font-semibold text-accent transition hover:bg-accent hover:text-white"
                  >
                    <FaChevronRight className="h-3.5 w-3.5" /> Sélectionner le chantier
                  </button>
                </div>
              ))}
            </div>
          )}
        </>
      ) : (
        <>
          <PageHeader title="Tâches" subtitle={`Chantier : ${selectedChantier.nom}`}>
            <Button variant="secondary" icon={<FaArrowLeft className="h-4 w-4" />} onClick={clearChantier}>Sélectionner un autre chantier</Button>
            <div className="flex items-center gap-1 rounded-lg border border-line bg-surface p-1">
              <button onClick={() => setView("grid")} className={cn("flex items-center gap-1.5 rounded-md px-3 py-1.5 text-xs font-semibold", view === "grid" ? "bg-accent text-white" : "text-muted")}><FaTableCellsLarge className="h-3.5 w-3.5" /> Grille</button>
              <button onClick={() => setView("kanban")} className={cn("rounded-md px-3 py-1.5 text-xs font-semibold", view === "kanban" ? "bg-accent text-white" : "text-muted")}>Kanban</button>
            </div>
            {canCreateTaches && <Button icon={<FaPlus className="h-4 w-4" />} onClick={openNew}>Nouvelle tâche</Button>}
          </PageHeader>

          <div className="mb-4 grid grid-cols-1 gap-4 lg:grid-cols-3">
            <Card className="flex items-center gap-4 p-4">
              <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-info-light text-info"><FaCircleCheck className="h-5 w-5" /></span>
              <div className="flex-1">
                <p className="text-xs text-muted">Progression moyenne</p>
                <Progress value={totalPct} tone="emerald" showLabel className="mt-1" />
              </div>
            </Card>
            <Card className="flex items-center justify-between p-4">
              <div><p className="text-xs text-muted">Tâches du chantier</p><p className="text-2xl font-black text-ink">{scopedTaches.length}</p></div>
              <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-300"><FaClock className="h-5 w-5" /></span>
            </Card>
            <Card className="flex items-center justify-between p-4">
              <div><p className="text-xs text-muted">Validées</p><p className="text-2xl font-black text-ink">{totalValidees}</p></div>
              <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-success-light text-success"><FaCheck className="h-5 w-5" /></span>
            </Card>
          </div>

          <div className="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-5">
            {STATUTS.map((s, i) => {
              const t = tacheStatutTone(s);
              return (
                <Card key={s} className="flex items-center gap-3 p-4">
                  <span className={cn("flex h-10 w-10 items-center justify-center rounded-xl", t === "blue" && "bg-info-light text-info", t === "emerald" && "bg-success-light text-success", t === "rose" && "bg-danger-light text-danger", t === "slate" && "bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-300")}>{STATUT_ICONS[i]}</span>
                  <div><p className="text-2xl font-black text-ink">{scopedTaches.filter((x) => x.statut === s).length}</p><p className="text-xs text-muted">{s}</p></div>
                </Card>
              );
            })}
          </div>

          <Card className="mb-4 p-4">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <SearchBox value={q} onChange={setQ} placeholder="Rechercher une tâche…" />
              <div className="flex items-center rounded-lg border border-line bg-app px-3 py-2 text-sm text-body"><FaBuilding className="mr-2 h-4 w-4 text-muted" /><span className="truncate">{selectedChantier.nom}</span></div>
              <SelectInput value={prio} onChange={(e) => setPrio(e.target.value)}>
                <option value="">Toutes priorités</option>
                {PRIOS.map((p) => <option key={p}>{p}</option>)}
              </SelectInput>
              <SelectInput value={statut} onChange={(e) => setStatut(e.target.value)}>
                <option value="">Tous statuts</option>
                {STATUTS.map((s) => <option key={s}>{s}</option>)}
              </SelectInput>
            </div>
            {activeFilters && (
              <div className="mt-3 flex flex-wrap items-center gap-2 border-t border-line pt-3">
                <span className="text-xs font-semibold text-muted">Filtres actifs :</span>
                {q && <Pill tone="blue">{q} <button onClick={() => setQ("")}><FaXmark className="h-3 w-3" /></button></Pill>}
                {prio && <Pill tone="amber">{prio} <button onClick={() => setPrio("")}><FaXmark className="h-3 w-3" /></button></Pill>}
                {statut && <Pill tone="emerald">{statut} <button onClick={() => setStatut("")}><FaXmark className="h-3 w-3" /></button></Pill>}
                <Button variant="ghost" className="px-2 py-1 text-xs" icon={<FaRotate className="h-3 w-3" />} onClick={reset}>Réinitialiser</Button>
              </div>
            )}
          </Card>

          {allTachesQuery.isLoading ? (
            <FullSpinner label="Chargement des tâches…" />
          ) : allTachesQuery.isError ? (
            <ErrorState message={getApiErrorMessage(allTachesQuery.error)} />
          ) : filtered.length === 0 ? (
            <EmptyState title="Aucune tâche trouvée." description="Ajustez vos filtres ou créez une nouvelle tâche." />
          ) : view === "grid" ? (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
              {filtered.map((t) => (
                <TaskCard
                  key={t.id}
                  t={t}
                  chantierNom={chantierName(t.chantierId)}
                  onEdit={() => openEdit(t)}
                  onDelete={() => setToDelete(t)}
                  onView={() => navigate(`/taches/${t.id}`)}
                  onValidate={() => validerMutation.mutate(t)}
                  onStatut={(s) => statutMutation.mutate({ t, statut: s })}
                  canEdit={canEditTaches}
                  canDelete={canDeleteTaches}
                  canValider={canValiderTaches}
                />
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-5">
              {STATUTS.map((s) => (
                <div key={s} className="rounded-2xl border border-line bg-app/40 p-3">
                  <div className="mb-3 flex items-center justify-between px-1">
                    <span className="flex items-center gap-2 text-sm font-bold text-ink"><span className={cn("h-2 w-2 rounded-full", tacheStatutTone(s) === "blue" && "bg-info", tacheStatutTone(s) === "emerald" && "bg-success", tacheStatutTone(s) === "rose" && "bg-danger", tacheStatutTone(s) === "slate" && "bg-slate-400")} />{s}</span>
                    <span className="rounded-full bg-surface px-2 py-0.5 text-xs font-semibold text-muted">{filtered.filter((t) => t.statut === s).length}</span>
                  </div>
                  <div className="space-y-3">
                    {filtered.filter((t) => t.statut === s).map((t) => (
                      <TaskCard
                        key={t.id}
                        t={t}
                        compact
                        chantierNom={chantierName(t.chantierId)}
                        onEdit={() => openEdit(t)}
                        onDelete={() => setToDelete(t)}
                        onView={() => navigate(`/taches/${t.id}`)}
                        onValidate={() => validerMutation.mutate(t)}
                        onStatut={(s2) => statutMutation.mutate({ t, statut: s2 })}
                        canEdit={canEditTaches}
                        canDelete={canDeleteTaches}
                        canValider={canValiderTaches}
                      />
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}

        </>
      )}

      <Modal
        open={open}
        onClose={() => setOpen(false)}
        size="lg"
        title={editing ? "Modifier la tâche" : "Nouvelle tâche"}
        footer={<><Button variant="secondary" onClick={() => setOpen(false)}>Annuler</Button><Button loading={saveMutation.isPending} onClick={() => saveMutation.mutate()}>{editing ? "Enregistrer" : "Créer"}</Button></>}
      >
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="Titre" required className="sm:col-span-2" error={errors.titre}><TextInput invalid={!!errors.titre} value={form.titre} onChange={(e) => setForm({ ...form, titre: e.target.value })} /></Field>
          {editing ? (
            <Field label="Chantier" className="sm:col-span-2">
              <p className="rounded-lg border border-line bg-app px-3 py-2 text-sm text-body">{chantierName(editing.chantierId)}</p>
            </Field>
          ) : selectedChantier ? (
            <Field label="Chantier" required className="sm:col-span-2">
              <p className="rounded-lg border border-line bg-app px-3 py-2 text-sm text-body"><FaBuilding className="mr-2 inline h-4 w-4 text-muted" />{selectedChantier.nom}</p>
            </Field>
          ) : (
            <Field label="Chantier" required error={errors.chantierId}><SelectInput value={form.chantierId} onChange={(e) => setForm({ ...form, chantierId: Number(e.target.value) })}>{chantiers.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}</SelectInput></Field>
          )}
          <Field label="Priorité"><SelectInput value={form.priorite} onChange={(e) => setForm({ ...form, priorite: e.target.value as Priorite })}>{PRIOS.map((p) => <option key={p}>{p}</option>)}</SelectInput></Field>
          {editing && (
            <>
              <Field label="Statut"><SelectInput value={form.statut} onChange={(e) => setForm({ ...form, statut: e.target.value as TacheStatut })}>{STATUTS.map((s) => <option key={s}>{s}</option>)}</SelectInput></Field>
              <Field label="Progression (%)" hint="0 à 100"><TextInput type="number" min={0} max={100} value={form.progression} onChange={(e) => setForm({ ...form, progression: Math.min(100, Math.max(0, Number(e.target.value))) })} /></Field>
            </>
          )}
          <Field label="Début" error={errors.dateDebut}><TextInput invalid={!!errors.dateDebut} type="date" min={today()} value={form.dateDebut} onChange={(e) => setForm({ ...form, dateDebut: e.target.value })} /></Field>
          <Field label="Fin" error={errors.dateFin}><TextInput invalid={!!errors.dateFin} type="date" min={form.dateDebut || today()} value={form.dateFin} onChange={(e) => setForm({ ...form, dateFin: e.target.value })} /></Field>
          <Field label="Description" className="sm:col-span-2"><TextArea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
          {!editing && (
            <>
              {canSeeUsers && (
                <Field label="Affecter à (utilisateur)" hint="Laissez vide pour vous affecter">
                  <SearchSelect
                    value={form.affecterUserId}
                    onChange={(v) => setForm({ ...form, affecterUserId: v })}
                    options={(usersQuery.data?.content ?? []).map((u) => ({ value: String(u.id), label: `${u.prenom} ${u.nom} · ${u.email}` }))}
                    placeholder="Moi (créateur)"
                  />
                </Field>
              )}
              <Field label="Affecter à (équipe)" hint="Vous pouvez choisir un utilisateur ET une équipe">
                <SearchSelect
                  value={form.affecterEquipeId}
                  onChange={(v) => setForm({ ...form, affecterEquipeId: v })}
                  options={(equipesQuery.data ?? []).map((eq) => ({ value: String(eq.id), label: eq.nom }))}
                  placeholder="Aucune équipe"
                />
              </Field>
            </>
          )}
        </div>
      </Modal>

      <ConfirmModal
        open={!!toDelete}
        onClose={() => setToDelete(null)}
        loading={deleteMutation.isPending}
        onConfirm={() => { if (toDelete) deleteMutation.mutate(toDelete.id); }}
        message={`Supprimer la tâche « ${toDelete?.titre} » ?`}
      />
    </div>
  );
}

function TaskCard({ t, compact, chantierNom, onView, onEdit, onDelete, onValidate, onStatut, canEdit, canDelete, canValider }: {
  t: Tache; compact?: boolean; chantierNom?: string;
  onView: () => void; onEdit: () => void; onDelete: () => void; onValidate: () => void; onStatut: (s: TacheStatut) => void;
  canEdit: boolean; canDelete: boolean; canValider: boolean;
}) {
  const borderTone = { Urgente: "border-danger/40", Haute: "border-warning/40", Moyenne: "border-info/30", Basse: "border-line" }[t.priorite];
  return (
    <div className={cn("group flex flex-col rounded-3xl border bg-card p-4 shadow-card transition hover:-translate-y-0.5", borderTone)}>
      <div className="mb-1 flex items-center justify-between gap-2">
        <span className="truncate text-xs font-semibold text-accent">{chantierNom ?? "-"}</span>
      </div>
      <button onClick={onView} className="text-left">
        <h3 className="font-bold text-ink transition group-hover:text-accent">{t.titre}</h3>
      </button>
      <p className="mt-1 line-clamp-2 text-sm text-muted">{t.description || "Aucune description."}</p>
      <div className="mt-3"><PriorityBadge priority={t.priorite} /></div>

      <div className="mt-3 space-y-2 border-t border-line pt-3 text-xs">
        <div className="flex items-center justify-between">
          <span className="flex items-center gap-1.5 text-muted"><FaSpinner className="h-3.5 w-3.5" /> Statut</span>
          {canEdit ? <SelectInput value={t.statut} onChange={(e) => onStatut(e.target.value as TacheStatut)} className="w-auto rounded-md px-2 py-1 text-xs">{STATUTS.map((s) => <option key={s}>{s}</option>)}</SelectInput> : <span className="font-semibold text-ink">{t.statut}</span>}
        </div>
        <div className="flex items-center justify-between">
          <span className="flex items-center gap-1.5 text-muted"><FaCalendarDays className="h-3.5 w-3.5" /> Échéance</span>
          <span className="font-semibold text-ink">{formatDate(t.dateFin)}</span>
        </div>
        <div className="flex items-center justify-between gap-3">
          <span className="flex items-center gap-1.5 text-muted"><FaClock className="h-3.5 w-3.5" /> Progression</span>
          <span className="flex items-center gap-2 font-semibold text-ink"><Progress value={t.progression ?? 0} tone="emerald" className="w-16" />{t.progression ?? 0}%</span>
        </div>
      </div>

      {!compact && (
        <div className="mt-3 flex items-center gap-1 border-t border-line pt-3">
          <Button variant="ghost" className="flex-1 px-2 py-1.5 text-xs" icon={<FaEye className="h-4 w-4" />} onClick={onView}>Voir</Button>
          {canValider && t.statut !== "Validée" && <Button variant="success" className="flex-1 px-2 py-1.5 text-xs" icon={<FaCheck className="h-4 w-4" />} onClick={onValidate}>Valider</Button>}
          {canEdit && <button onClick={onEdit} className="rounded-lg p-2 text-muted hover:bg-app hover:text-accent"><FaPen className="h-4 w-4" /></button>}
          {canDelete && <button onClick={onDelete} className="rounded-lg p-2 text-muted hover:bg-danger-light hover:text-danger"><FaTrashCan className="h-4 w-4" /></button>}
        </div>
      )}
    </div>
  );
}

export function TaskDetail() {
  const { id } = useParams();
  const numId = Number(id);
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { hasPerm } = useApp();
  const canEditTaches = hasPerm("TACHE_MODIFIER");
  const canDeleteTaches = hasPerm("TACHE_SUPPRIMER");
  const canValiderTaches = hasPerm("TACHE_VALIDER");
  const canRefuserTaches = hasPerm("TACHE_REFUSER");
  const [valComment, setValComment] = useState("");
  const [affType, setAffType] = useState<"equipe" | "user">("equipe");
  const [affTarget, setAffTarget] = useState("");
  const [affRole, setAffRole] = useState<"REALISATEUR" | "CONTROLEUR">("REALISATEUR");

  const tacheQuery = useQuery({
    queryKey: ["tache", numId],
    queryFn: () => getTache(numId),
    enabled: Number.isFinite(numId),
  });

  const validationsQuery = useQuery({
    queryKey: ["validations", numId],
    queryFn: () => getValidations(numId),
    enabled: Number.isFinite(numId) && !!tacheQuery.data,
  });

  const affectationsQuery = useQuery({
    queryKey: ["tache-affectations", numId],
    queryFn: () => getTacheAffectations(numId),
    enabled: Number.isFinite(numId) && !!tacheQuery.data,
  });

  const retirerMutation = useMutation({
    mutationFn: (affectationId: number) => retirerAffectation(affectationId),
    onSuccess: () => {
      toast.success("Affectation retirée.");
      queryClient.invalidateQueries({ queryKey: ["tache-affectations", numId] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  // Cibles disponibles pour affecter la tâche : utilisateurs et équipes.
  const usersQuery = useQuery({
    queryKey: ["users", "affectation"],
    queryFn: () => getUsers({ page: 0, size: 200 }),
  });
  const equipesQuery = useQuery({
    queryKey: ["equipes", "affectation"],
    queryFn: getEquipes,
  });
  const equipeMembresQuery = useQuery({
    queryKey: ["equipe-membres", "counts"],
    queryFn: async () => {
      const list = equipesQuery.data ?? [];
      const entries = await Promise.all(
        list.map(async (e) => [e.id, (await getEquipeMembres(e.id)).length] as const)
      );
      return Object.fromEntries(entries) as Record<number, number>;
    },
    enabled: (equipesQuery.data?.length ?? 0) > 0,
  });
  const equipeMembres = equipeMembresQuery.data ?? {};

  const affecterMutation = useMutation({
    mutationFn: () => {
      if (!affTarget) throw new Error("Choisissez une équipe ou un utilisateur.");
      const dateAffectation = new Date().toISOString().slice(0, 10);
      const base = { tacheId: numId, role: affRole, dateAffectation };
      return affecterTache(
        affType === "equipe"
          ? { ...base, equipeId: Number(affTarget) }
          : { ...base, utilisateurId: Number(affTarget) }
      );
    },
    onSuccess: () => {
      toast.success("Tâche affectée.");
      setAffTarget("");
      queryClient.invalidateQueries({ queryKey: ["tache-affectations", numId] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const statutMutation = useMutation({
    mutationFn: (statut: TacheStatut) => updateTache(numId, buildUpdate(fromDetail(tacheQuery.data!), { status: tacheStatutEnum(statut) as TacheStatus, ...(statut === "Validée" ? { progression: 100 } : {}) })),
    onSuccess: () => {
      toast.success("Statut mis à jour.");
      queryClient.invalidateQueries({ queryKey: ["tache", numId] });
      queryClient.invalidateQueries({ queryKey: ["chantiers"] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const progressionMutation = useMutation({
    mutationFn: (progression: number) => updateTache(numId, buildUpdate(fromDetail(tacheQuery.data!), { progression })),
    onSuccess: () => {
      toast.success("Progression mise à jour.");
      queryClient.invalidateQueries({ queryKey: ["tache", numId] });
      queryClient.invalidateQueries({ queryKey: ["chantiers"] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const validerMutation = useMutation({
    mutationFn: () => validerTache(numId, {
      commentaire: valComment || undefined,
      dateValidation: new Date().toISOString().slice(0, 10),
    }),
    onSuccess: () => {
      toast.success("Tâche validée.");
      setValComment("");
      queryClient.invalidateQueries({ queryKey: ["tache", numId] });
      queryClient.invalidateQueries({ queryKey: ["chantiers"] });
      queryClient.invalidateQueries({ queryKey: ["validations", numId] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const refuserMutation = useMutation({
    mutationFn: () => refuserTache(numId, {
      commentaire: valComment || undefined,
      dateValidation: new Date().toISOString().slice(0, 10),
    }),
    onSuccess: () => {
      toast.success("Tâche refusée.");
      setValComment("");
      queryClient.invalidateQueries({ queryKey: ["tache", numId] });
      queryClient.invalidateQueries({ queryKey: ["chantiers"] });
      queryClient.invalidateQueries({ queryKey: ["validations", numId] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  if (tacheQuery.isLoading) return <FullSpinner label="Chargement de la tâche…" />;
  if (tacheQuery.isError || !tacheQuery.data) return <EmptyState title="Tâche introuvable" description={getApiErrorMessage(tacheQuery.error)} action={<Button onClick={() => navigate("/taches")}>Retour</Button>} />;

  const t = fromDetail(tacheQuery.data);
  const validations = validationsQuery.data ?? [];

  return (
    <div>
      <button onClick={() => navigate("/taches")} className="mb-4 inline-flex items-center gap-1.5 text-sm font-semibold text-muted hover:text-ink"><FaArrowLeft className="h-4 w-4" /> Retour aux tâches</button>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="p-6 lg:col-span-2">
          <div className="mb-4 flex flex-wrap items-center gap-2">
            <PriorityBadge priority={t.priorite} />
            <Badge tone={tacheStatutTone(t.statut)} dot>{t.statut}</Badge>
            {t.chantierNom && <Badge tone="indigo">{t.chantierNom}</Badge>}
          </div>
          <h1 className="text-2xl font-black text-ink">{t.titre}</h1>
          <p className="mt-2 text-body">{t.description || "Aucune description."}</p>

          <div className="mt-6">
            <InfoGrid>
              <InfoItem label="Priorité" value={<PriorityBadge priority={t.priorite} />} />
              <InfoItem label="Statut" value={<Badge tone={tacheStatutTone(t.statut)} dot>{t.statut}</Badge>} />
              <InfoItem label="Progression" value={<span className="flex items-center gap-2"><Progress value={t.progression ?? 0} tone="emerald" className="w-24" />{t.progression ?? 0}%</span>} />
              <InfoItem label="Date de début" value={formatDate(t.dateDebut)} />
              <InfoItem label="Date de fin" value={formatDate(t.dateFin)} />
              <InfoItem label="Durée" value={durationLabel(t.dateDebut, t.dateFin)} />
              <InfoItem label="Chantier" value={t.chantierNom ?? "-"} />
            </InfoGrid>
          </div>

          <div className="mt-6 border-t border-line pt-4">
            <p className="mb-2 text-sm font-bold text-ink">Affectations</p>

            {canEditTaches && (
            <div className="mb-3 rounded-lg border border-line bg-surface p-3">
              <div className="mb-2 flex items-center gap-1 rounded-lg border border-line bg-card p-1">
                <button
                  onClick={() => { setAffType("equipe"); setAffTarget(""); }}
                  className={cn("flex-1 rounded-md px-2 py-1.5 text-xs font-semibold transition", affType === "equipe" ? "bg-accent text-white" : "text-muted")}
                >
                  À une équipe
                </button>
                <button
                  onClick={() => { setAffType("user"); setAffTarget(""); }}
                  className={cn("flex-1 rounded-md px-2 py-1.5 text-xs font-semibold transition", affType === "user" ? "bg-accent text-white" : "text-muted")}
                >
                  À un utilisateur
                </button>
              </div>
              <div className="grid grid-cols-1 gap-2 sm:grid-cols-[1fr_auto]">
                <SearchSelect
                  value={affTarget}
                  onChange={setAffTarget}
                  options={
                    affType === "equipe"
                      ? (equipesQuery.data ?? []).map((eq) => ({ value: String(eq.id), label: `${eq.nom} (${equipeMembres[eq.id] ?? 0} membre${equipeMembres[eq.id] === 1 ? "" : "s"})` }))
                      : (usersQuery.data?.content ?? []).map((u) => ({ value: String(u.id), label: `${u.prenom} ${u.nom} · ${u.email}` }))
                  }
                  placeholder={affType === "equipe" ? "Choisir une équipe…" : "Choisir un utilisateur…"}
                />
                <div className="flex items-center gap-2">
                  <SelectInput value={affRole} onChange={(e) => setAffRole(e.target.value as "REALISATEUR" | "CONTROLEUR")} className="w-auto">
                    <option value="REALISATEUR">Réalisateur</option>
                    <option value="CONTROLEUR">Contrôleur</option>
                  </SelectInput>
                  <Button
                    disabled={!affTarget}
                    loading={affecterMutation.isPending}
                    onClick={() => affecterMutation.mutate()}
                    icon={<FaPlus className="h-4 w-4" />}
                    className="px-3 py-1.5 text-xs"
                  >
                    Affecter
                  </Button>
                </div>
              </div>
            </div>
            )}

            {affectationsQuery.isLoading ? (
              <p className="text-sm text-muted">Chargement…</p>
            ) : affectationsQuery.isError ? (
              <p className="text-sm text-danger">{getApiErrorMessage(affectationsQuery.error)}</p>
            ) : (affectationsQuery.data ?? []).length === 0 ? (
              <p className="text-sm text-muted">Aucune affectation. Cette tâche n'est assignée à personne.</p>
            ) : (
              <div className="space-y-2">
                {(affectationsQuery.data ?? []).map((a) => (
                  <div key={a.id} className="flex items-center justify-between gap-3 rounded-lg border border-line bg-app px-3 py-2">
                    <div className="flex min-w-0 items-center gap-3">
                      <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-accent/10 font-bold text-accent">
                        {a.utilisateurId != null ? a.utilisateurNom?.[0] ?? "U" : a.equipeNom?.[0] ?? "E"}
                      </span>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-semibold text-ink">
                          {a.utilisateurNom ? a.utilisateurNom : null}
                          {a.utilisateurNom && a.equipeNom ? " + " : null}
                          {a.equipeNom ? a.equipeNom : null}
                        </p>
                        <p className="truncate text-xs text-muted">
                          {[a.utilisateurId != null ? "Utilisateur" : null, a.equipeId != null ? "Équipe" : null]
                            .filter(Boolean)
                            .join(" + ")}
                          {a.utilisateurId == null && a.equipeId == null ? "-" : null} · {formatDate(a.dateAffectation)}
                        </p>
                      </div>
                    </div>
                    <div className="flex shrink-0 items-center gap-2">
                      <Badge tone={a.role === "CONTROLEUR" ? "indigo" : "blue"}>
                        {a.role === "CONTROLEUR" ? "Contrôleur" : "Réalisateur"}
                      </Badge>
                      {canDeleteTaches && (
                        <button
                          onClick={() => retirerMutation.mutate(a.id)}
                          disabled={retirerMutation.isPending}
                          className="rounded-lg p-1.5 text-muted hover:bg-danger-light hover:text-danger"
                          title="Retirer cette affectation"
                        >
                          <FaTrashCan className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="mt-6 border-t border-line pt-4">
            <p className="mb-2 text-sm font-bold text-ink">Historique des validations</p>
            {validationsQuery.isLoading ? (
              <p className="text-sm text-muted">Chargement…</p>
            ) : validations.length === 0 ? (
              <p className="text-sm text-muted">Aucune validation enregistrée.</p>
            ) : (
              <div className="space-y-2">
                {validations.map((v) => (
                  <div key={v.id} className="flex items-center justify-between rounded-lg border border-line bg-app px-3 py-2">
                    <div>
                      <p className="text-sm font-semibold text-ink">{v.validateurPrenom} {v.validateurNom}</p>
                      <p className="text-xs text-muted">{formatDate(v.dateValidation)}{v.commentaire ? ` · ${v.commentaire}` : ""}</p>
                    </div>
                    <Badge tone={v.statut === "VALIDE" ? "emerald" : "rose"}>{v.statut === "VALIDE" ? "Validée" : "Refusée"}</Badge>
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>

        <Card className="h-fit p-5">
          <h3 className="font-bold text-ink">Actions rapides</h3>
          {canEditTaches || canValiderTaches || canRefuserTaches ? (<>
          <p className="text-xs text-muted">Mettre à jour cette tâche</p>
          <div className="mt-4 space-y-3">
            {canEditTaches && <Field label="Statut">
              <SelectInput value={t.statut} onChange={(e) => statutMutation.mutate(e.target.value as TacheStatut)}>{STATUTS.map((s) => <option key={s}>{s}</option>)}</SelectInput>
            </Field>}
            {canEditTaches && <Field label="Progression (%)" hint="0 à 100">
              <TextInput type="number" min={0} max={100} value={t.progression ?? 0} onChange={(e) => progressionMutation.mutate(Math.min(100, Math.max(0, Number(e.target.value))))} />
            </Field>}
            {(canValiderTaches || canRefuserTaches) && <div className={canEditTaches ? "border-t border-line pt-3" : ""}>
              <p className="mb-2 text-sm font-bold text-ink">Validation</p>
              <div className="space-y-2">
                <TextInput placeholder="Commentaire (optionnel)" value={valComment} onChange={(e) => setValComment(e.target.value)} />
                <div className="flex gap-2">
                  {canValiderTaches && <Button variant="success" className="flex-1" icon={<FaCheck className="h-4 w-4" />} loading={validerMutation.isPending} onClick={() => validerMutation.mutate()}>Valider</Button>}
                  {canRefuserTaches && <Button variant="danger" className="flex-1" icon={<FaXmark className="h-4 w-4" />} loading={refuserMutation.isPending} onClick={() => refuserMutation.mutate()}>Refuser</Button>}
                </div>
              </div>
            </div>}
          </div>
          </>) : (
          <p className="text-xs text-muted">Vous n'avez pas les droits de modification sur cette tâche.</p>
          )}
        </Card>
      </div>
    </div>
  );
}
