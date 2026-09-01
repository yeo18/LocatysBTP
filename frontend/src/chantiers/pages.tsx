import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { FaArrowLeft, FaBuilding, FaCalendarDays, FaCircleCheck, FaClipboardList, FaEye, FaFileLines, FaHelmetSafety, FaLocationDot, FaPen, FaPlus, FaTrashCan, FaUsers } from "react-icons/fa6";
import { toast } from "react-toastify";
import { useMutation, useQuery, useQueries, useQueryClient } from "@tanstack/react-query";
import { useApp } from "../core/store/AppProvider";
import {
  Badge,
  Button,
  Card,
  ConfirmModal,
  EmptyState,
  ErrorState,
  Field,
  FullSpinner,
  InfoItem,
  Modal,
  PageHeader,
  Pagination,
  Progress,
  SearchBox,
  SelectInput,
  Tabs,
  TextArea,
  TextInput,
} from "../core/components/ui";
import { TaskMiniList } from "../core/components/lists";
import { BarChart } from "../core/components/charts";
import {
  formatDate,
  formatDateShort,
} from "../core/lib/format";
import { validateDateRange, isValidDate, today } from "../core/lib/validation";
import {
  chantierStatutTone,
  typeTone,
} from "../core/lib/styles";
import { cn } from "../core/utils/cn";
import type { Chantier, ChantierStatut, ChantierType, Priorite, Tache } from "../core/lib/types";
import {
  chantierStatutEnum,
  chantierStatutLabel,
  createChantier,
  deleteChantier,
  getChantier,
  getChantiers,
  updateChantier,
} from "../chantiers/api";
import { getTaches, tacheStatutLabel, tachePrioriteLabel } from "../taches/api";
import {
  addEquipeMembre,
  affectationStatutLabel,
  getChantierEquipes,
  getEquipeMembres,
  getEquipes,
  removeEquipeMembre,
} from "../equipes/api";
import { getUsers } from "../utilisateurs/api";
import type {
  ChantierResume,
  ChantierResponse,
  TacheResume,
  MembreEquipeResponse,
} from "../core/api/types";
import { getApiErrorMessage } from "../core/api/axios";

const TYPES: ChantierType[] = ["Résidentiel", "Commercial", "Industriel", "Infrastructure", "Rénovation"];
// Statuts réels du backend (enum) - la traduction sert uniquement à l'affichage.
const STATUTS: ChantierStatut[] = ["Planifié", "En cours", "Terminé", "Annulé"];

const SIZE = 5;

const emptyChantier: Omit<Chantier, "id" | "equipeIds"> = {
  nom: "", type: "Résidentiel" as ChantierType, statut: "Planifié" as ChantierStatut,
  localisation: "", coordonnees: "", dateDebut: "", dateFin: "",
  photo: "", description: "",
};

// ---- Adaptation backend -> interface existante (champs absents : valeur neutre) ----

function fromResume(r: ChantierResume): Chantier {
  return {
    id: r.id,
    nom: r.nom,
    type: "-",
    statut: chantierStatutLabel(r.statut) as ChantierStatut,
    localisation: r.adresseSaisie || "-",
    coordonnees: "",
    dateDebut: r.dateDebut ?? "",
    dateFin: r.dateFin ?? "",
    photo: "",
    description: "",
    equipeIds: [],
    progression: r.progression,
  };
}

function fromDetail(r: ChantierResponse): Chantier {
  return {
    id: r.id,
    nom: r.nom,
    type: "-",
    statut: chantierStatutLabel(r.statut) as ChantierStatut,
    localisation: r.adresseSaisie || "-",
    coordonnees: "",
    dateDebut: r.dateDebut ?? "",
    dateFin: r.dateFin ?? "",
    photo: "",
    description: r.description ?? "",
    equipeIds: [],
    progression: r.progression,
    responsableNom: r.responsableNom ?? undefined,
  };
}

export function ChantiersList() {
  const queryClient = useQueryClient();
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const { hasPerm } = useApp();
  const canCreateChantiers = hasPerm("CHANTIER_CREER");
  const canEditChantiers = hasPerm("CHANTIER_MODIFIER");
  const canDeleteChantiers = hasPerm("CHANTIER_SUPPRIMER");

  const [q, setQ] = useState(params.get("q") ?? "");
  const [type, setType] = useState("");
  const [statut, setStatut] = useState("");
  const [page, setPage] = useState(1);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Chantier | null>(null);
  const [form, setForm] = useState(emptyChantier);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [toDelete, setToDelete] = useState<Chantier | null>(null);

  useEffect(() => {
    setQ(params.get("q") ?? "");
  }, [params]);

  const statutEnum = statut ? chantierStatutEnum(statut) : undefined;

  const chantiersQuery = useQuery({
    queryKey: ["chantiers", { page, q, statutEnum }],
    queryFn: () =>
      getChantiers({
        page: page - 1,
        size: SIZE,
        motCle: q || undefined,
        statut: statutEnum,
      }),
  });

  // Compteur réel du nombre de tâches accessibles (KPI du header).
  const totalTachesQuery = useQuery({
    queryKey: ["taches", "total"],
    queryFn: () => getTaches({ page: 0, size: 1 }),
  });

  const data = chantiersQuery.data;
  const chantiers = (data?.content ?? []).map(fromResume);
  const pages = data?.totalPages ?? 1;
  const total = data?.totalElements ?? 0;

  // Filtre « type » : le backend ne connaît pas ce champ - transformation
  // locale sur les données déjà récupérées uniquement.
  const pageItems = useMemo(
    () => chantiers.filter((c) => !type || c.type === type),
    [chantiers, type]
  );

  const avg = Math.round(
    chantiers.reduce((s, c) => s + (c.progression ?? 0), 0) / (chantiers.length || 1)
  );

  const openNew = () => {
    setEditing(null);
    setForm(emptyChantier);
    setErrors({});
    setOpen(true);
  };
  const openEdit = (c: Chantier) => {
    setEditing(c);
    setForm({ ...c });
    setErrors({});
    setOpen(true);
  };

  const saveMutation = useMutation({
    mutationFn: () => {
      const ers: Record<string, string> = {};
      if (!form.nom.trim()) ers.nom = "Le nom est obligatoire.";
      else if (/[<>{}]/.test(form.nom)) ers.nom = "Le nom contient des caractères non autorisés.";
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
      const payload = {
        nom: form.nom,
        description: form.description || undefined,
        adresseSaisie: form.localisation || undefined,
        statut: chantierStatutEnum(form.statut) ?? "PREVU",
        dateDebut: form.dateDebut || undefined,
        dateFin: form.dateFin || undefined,
      };
      return editing ? updateChantier(editing.id, payload) : createChantier(payload);
    },
    onSuccess: () => {
      toast.success(editing ? "Chantier modifié." : "Chantier créé.");
      queryClient.invalidateQueries({ queryKey: ["chantiers"] });
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
    mutationFn: (id: number) => deleteChantier(id),
    onSuccess: () => {
      toast.success("Chantier annulé.");
      queryClient.invalidateQueries({ queryKey: ["chantiers"] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const save = () => saveMutation.mutate();

  return (
    <div>
      <PageHeader title="Chantiers" subtitle="Gérez l'ensemble de vos projets de construction.">
        {canCreateChantiers && <Button icon={<FaPlus className="h-4 w-4" />} onClick={openNew}>Nouveau chantier</Button>}
      </PageHeader>

      <div className="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-4">
        <MiniStat label="Total" value={total} tone="blue" icon={<FaBuilding className="h-5 w-5" />} />
        <MiniStat label="Avancement moyen" value={`${avg}%`} tone="emerald" icon={<Progress value={avg} className="w-8" />} />
        <MiniStat label="En progression" value={chantiers.filter((c) => c.statut === "En cours").length} tone="amber" icon={<FaBuilding className="h-5 w-5" />} />
        <MiniStat label="Tâches cumulées" value={totalTachesQuery.data?.totalElements ?? 0} tone="violet" icon={<FaFileLines className="h-5 w-5" />} />
      </div>

      <Card className="p-4">
        <div className="mb-4 grid grid-cols-1 gap-3 sm:grid-cols-3">
          <SearchBox value={q} onChange={(v) => { setQ(v); setPage(1); }} placeholder="Rechercher un chantier…" />
          <SelectInput value={type} onChange={(e) => { setType(e.target.value); setPage(1); }}>
            <option value="">Tous les types</option>
            {TYPES.map((t) => <option key={t}>{t}</option>)}
          </SelectInput>
          <SelectInput value={statut} onChange={(e) => { setStatut(e.target.value); setPage(1); }}>
            <option value="">Tous les statuts</option>
            {STATUTS.map((s) => <option key={s}>{s}</option>)}
          </SelectInput>
        </div>

        {chantiersQuery.isLoading ? (
          <FullSpinner label="Chargement des chantiers…" />
        ) : chantiersQuery.isError ? (
          <ErrorState message={getApiErrorMessage(chantiersQuery.error)} />
        ) : pageItems.length === 0 ? (
          <EmptyState title="Aucun chantier" description="Ajustez vos filtres ou créez un nouveau chantier." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[820px] border-collapse text-sm">
              <thead>
                <tr className="border-b border-line text-left text-xs uppercase tracking-wide text-muted">
                  <th className="px-3 py-3 font-semibold">Chantier</th>
                  <th className="px-3 py-3 font-semibold">Type</th>
                  <th className="px-3 py-3 font-semibold">Statut</th>
                  <th className="px-3 py-3 font-semibold">Période</th>
                  <th className="px-3 py-3 font-semibold">Avancement</th>
                  <th className="px-3 py-3 text-right font-semibold">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {pageItems.map((c) => {
                  const av = c.progression ?? 0;
                  return (
                    <tr key={c.id} className="group transition hover:bg-app/50">
                      <td className="px-3 py-3">
                        <button onClick={() => navigate(`/chantiers/${c.id}`)} className="flex items-center gap-3 text-left">
                          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-app text-accent">
                            <FaBuilding className="h-4 w-4" />
                          </span>
                          <span>
                            <span className="block font-semibold text-ink group-hover:text-accent">{c.nom}</span>
                            <span className="flex items-center gap-1 text-xs text-muted"><FaLocationDot className="h-3 w-3" />{c.localisation}</span>
                          </span>
                        </button>
                      </td>
                      <td className="px-3 py-3"><Badge tone={typeTone[c.type]}>{c.type}</Badge></td>
                      <td className="px-3 py-3"><Badge tone={chantierStatutTone(c.statut)} dot>{c.statut}</Badge></td>
                      <td className="px-3 py-3 text-xs text-body">
                        <span className="flex items-center gap-1"><FaCalendarDays className="h-3 w-3 text-muted" />{formatDateShort(c.dateDebut)}</span>
                        <span className="text-muted">→ {formatDateShort(c.dateFin)}</span>
                      </td>
                      <td className="px-3 py-3">
                        <Progress value={av} showLabel tone={av >= 100 ? "emerald" : "blue"} className="w-32" />
                      </td>
                      <td className="px-3 py-3">
                        <div className="flex items-center justify-end gap-1">
                          <Button variant="ghost" className="px-2 py-1.5 text-xs" icon={<FaEye className="h-4 w-4" />} onClick={() => navigate(`/chantiers/${c.id}`)}>Voir</Button>
                          {canEditChantiers && <button onClick={() => openEdit(c)} className="rounded-lg p-2 text-muted hover:bg-app hover:text-accent"><FaPen className="h-4 w-4" /></button>}
                          {canDeleteChantiers && <button onClick={() => setToDelete(c)} className="rounded-lg p-2 text-muted hover:bg-danger-light hover:text-danger"><FaTrashCan className="h-4 w-4" /></button>}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        <div className="mt-4">
          <Pagination page={page} pages={pages} onPage={setPage} />
        </div>
      </Card>

      <Modal
        open={open}
        onClose={() => setOpen(false)}
        size="lg"
        title={editing ? "Modifier le chantier" : "Nouveau chantier"}
        subtitle="Renseignez les informations du projet."
        footer={<><Button variant="secondary" onClick={() => setOpen(false)}>Annuler</Button><Button loading={saveMutation.isPending} onClick={save}>{editing ? "Enregistrer" : "Créer"}</Button></>}
      >
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <Field label="Nom du chantier" required className="sm:col-span-2" error={errors.nom}><TextInput invalid={!!errors.nom} value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} placeholder="Ex. Résidence Les Alizés" /></Field>
          <Field label="Type" hint="Non enregistré côté serveur"><SelectInput value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value as ChantierType })}>{TYPES.map((t) => <option key={t}>{t}</option>)}</SelectInput></Field>
          <Field label="Statut"><SelectInput value={form.statut} onChange={(e) => setForm({ ...form, statut: e.target.value as ChantierStatut })}>{STATUTS.map((s) => <option key={s}>{s}</option>)}</SelectInput></Field>
          <Field label="Localisation" className="sm:col-span-2"><TextInput value={form.localisation} onChange={(e) => setForm({ ...form, localisation: e.target.value })} /></Field>
          <Field label="Coordonnées" hint="Non enregistré côté serveur"><TextInput value={form.coordonnees} onChange={(e) => setForm({ ...form, coordonnees: e.target.value })} /></Field>
          <Field label="Date de début" error={errors.dateDebut}><TextInput invalid={!!errors.dateDebut} type="date" min={today()} value={form.dateDebut} onChange={(e) => setForm({ ...form, dateDebut: e.target.value })} /></Field>
          <Field label="Date de fin" error={errors.dateFin}><TextInput invalid={!!errors.dateFin} type="date" min={form.dateDebut || today()} value={form.dateFin} onChange={(e) => setForm({ ...form, dateFin: e.target.value })} /></Field>
          <Field label="Photo (URL)" hint="Non enregistré côté serveur"><TextInput value={form.photo} onChange={(e) => setForm({ ...form, photo: e.target.value })} placeholder="https://…" /></Field>
          <Field label="Description" className="sm:col-span-2"><TextArea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
        </div>
      </Modal>

      <ConfirmModal
        open={!!toDelete}
        onClose={() => setToDelete(null)}
        onConfirm={() => { if (toDelete) deleteMutation.mutate(toDelete.id); setToDelete(null); }}
        message={`Annuler le chantier « ${toDelete?.nom} » ?`}
        confirmLabel="Annuler"
      />
    </div>
  );
}

function MiniStat({ label, value, tone, icon }: { label: string; value: React.ReactNode; tone: any; icon: React.ReactNode }) {
  return (
    <Card className="flex items-center gap-3 p-4">
      <span className={cn(
        "flex h-10 w-10 items-center justify-center rounded-xl",
        tone === "blue" && "bg-info-light text-info",
        tone === "emerald" && "bg-success-light text-success",
        tone === "amber" && "bg-warning-light text-warning",
        tone === "violet" && "bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-300"
      )}>{icon}</span>
      <div><p className="text-xl font-black text-ink">{value}</p><p className="text-xs text-muted">{label}</p></div>
    </Card>
  );
}

const TABS = ["Infos", "Tâches", "Équipes", "Utilisateurs", "Statistiques"];

function fromTacheResume(r: TacheResume): Tache {
  return {
    id: r.id,
    titre: r.titre,
    description: "",
    chantierId: r.chantierId,
    priorite: tachePrioriteLabel(r.priorite) as Priorite,
    statut: tacheStatutLabel(r.status) as Tache["statut"],
    assigneId: null,
    equipeId: null,
    dateDebut: r.dateDebut ?? "",
    dateFin: r.dateFin ?? "",
    cout: 0,
    checklist: [],
    progression: r.progression,
  };
}

export function ChantierDetail() {
  const { id } = useParams();
  const { hasPerm } = useApp();
  const canEditChantiers = hasPerm("CHANTIER_MODIFIER");
  const canDeleteMembres = hasPerm("EQUIPE_SUPPRIMER");
  const navigate = useNavigate();
  const numId = Number(id);
  const queryClient = useQueryClient();
  const [tab, setTab] = useState("Infos");
  const [assignId, setAssignId] = useState("");

  const query = useQuery({
    queryKey: ["chantier", numId],
    queryFn: () => getChantier(numId),
    enabled: !!numId,
  });

  // ---- Données connectées (backend réel) ----
  const tachesQuery = useQuery({
    queryKey: ["taches", "chantier", numId],
    queryFn: () => getTaches({ page: 0, size: 100 }),
    enabled: !!numId,
  });
  const affectationsQuery = useQuery({
    queryKey: ["chantier-equipes", numId],
    queryFn: () => getChantierEquipes(numId),
    enabled: !!numId,
  });
  const usersQuery = useQuery({ queryKey: ["users"], queryFn: () => getUsers({ page: 0, size: 100 }) });
  const equipesQuery = useQuery({ queryKey: ["equipes"], queryFn: getEquipes });

  const allTaches = (tachesQuery.data?.content ?? []).map(fromTacheResume);
  const cTaches = allTaches.filter((t) => t.chantierId === numId);

  // Le DTO résumé d'affectation ne porte pas l'id d'équipe : on le retrouve
  // par le nom via la liste des équipes actives.
  const equipeIdByNom = new Map((equipesQuery.data ?? []).map((e) => [e.nom, e.id]));
  const teamIds = Array.from(
    new Set(
      (affectationsQuery.data ?? [])
        .map((a) => equipeIdByNom.get(a.equipeNom))
        .filter((id): id is number => id != null)
    )
  );

  const membresQueries = useQueries({
    queries: teamIds.map((equipeId) => ({
      queryKey: ["equipe-membres", equipeId],
      queryFn: () => getEquipeMembres(equipeId),
    })),
  });
  const membres: MembreEquipeResponse[] = membresQueries.flatMap((q) => q.data ?? []);

  const allUsers = usersQuery.data?.content ?? [];
  const memberUserIds = new Set(membres.map((m) => m.utilisateurId));
  const assignableUsers = allUsers.filter((u) => !memberUserIds.has(u.id));

  const invalidateEquipes = () => {
    queryClient.invalidateQueries({ queryKey: ["chantier-equipes"] });
    queryClient.invalidateQueries({ queryKey: ["equipe-membres"] });
  };

  const addMemberMutation = useMutation({
    mutationFn: (utilisateurId: number) => {
      const equipeId = teamIds[0];
      if (!equipeId) throw new Error("Aucune équipe affectée à ce chantier.");
      return addEquipeMembre({
        utilisateurId,
        equipeId,
        roleDansEquipe: "OUVRIER",
        dateIntegration: new Date().toISOString().slice(0, 10),
      });
    },
    onSuccess: () => {
      toast.success("Utilisateur assigné à l'équipe.");
      setAssignId("");
      invalidateEquipes();
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const removeMemberMutation = useMutation({
    mutationFn: (membreId: number) => removeEquipeMembre(membreId),
    onSuccess: () => {
      toast.success("Utilisateur retiré de l'équipe.");
      invalidateEquipes();
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  if (query.isLoading) return <FullSpinner label="Chargement du chantier…" />;
  if (query.isError) return <ErrorState message={getApiErrorMessage(query.error)} />;
  if (!query.data) return <EmptyState title="Chantier introuvable" action={<Button onClick={() => navigate("/chantiers")}>Retour</Button>} />;

  const chantier = fromDetail(query.data);
  const av = chantier.progression ?? 0;

  const assignUser = () => {
    if (!assignId) return;
    addMemberMutation.mutate(Number(assignId));
  };

  return (
    <div>
      <div className="mb-4 flex items-center justify-between gap-3">
        <button onClick={() => navigate("/chantiers")} className="inline-flex items-center gap-1.5 text-sm font-semibold text-muted hover:text-ink">
          <FaArrowLeft className="h-4 w-4" /> Retour aux chantiers
        </button>
      </div>

      {/* Bannière */}
      <div className="relative mb-4 overflow-hidden rounded-3xl bg-gradient-to-br from-accent via-accent-soft to-[#132c4a] shadow-card">
        <div className="absolute inset-0 bg-blueprint opacity-40" />
        <div aria-hidden className="pointer-events-none absolute -right-16 -top-16 h-64 w-64 rounded-full bg-amber-400/20 blur-3xl" />
        <div aria-hidden className="pointer-events-none absolute -bottom-24 left-1/3 h-56 w-56 rounded-full bg-sky-400/10 blur-3xl" />
        <div className="relative flex flex-col gap-6 p-6 sm:p-8 lg:flex-row lg:items-center lg:justify-between">
          <div className="min-w-0">
            <div className="mb-3 flex flex-wrap items-center gap-2">
              <Badge tone={chantierStatutTone(chantier.statut)} dot>{chantier.statut}</Badge>
              <span className="rounded-full bg-white/15 px-3 py-1 text-xs font-semibold text-white backdrop-blur">{av}% réalisé</span>
              <span className="rounded-full bg-white/15 px-3 py-1 text-xs font-semibold text-white backdrop-blur">{cTaches.length} tâche(s)</span>
              <span className="rounded-full bg-white/15 px-3 py-1 text-xs font-semibold text-white backdrop-blur">{(affectationsQuery.data ?? []).length} équipe(s)</span>
            </div>
            <h1 className="truncate text-2xl font-black tracking-tight text-white sm:text-3xl">{chantier.nom}</h1>
            <div className="mt-2 flex flex-wrap items-center gap-x-5 gap-y-1.5 text-sm text-slate-300">
              <span className="inline-flex items-center gap-1.5"><FaLocationDot className="h-4 w-4 text-amber-300" />{chantier.localisation || "Localisation non renseignée"}</span>
              <span className="inline-flex items-center gap-1.5"><FaCalendarDays className="h-4 w-4 text-amber-300" />{formatDateShort(chantier.dateDebut)} → {formatDateShort(chantier.dateFin)}</span>
            </div>
          </div>
          <div className="w-full max-w-sm shrink-0 lg:w-72">
            <div className="rounded-2xl border border-white/15 bg-white/10 p-4 backdrop-blur-sm">
              <div className="mb-2 flex items-center justify-between text-xs text-white/85"><span className="font-semibold">Avancement</span><span className="font-black text-white">{av}%</span></div>
              <div className="h-2 w-full overflow-hidden rounded-full bg-white/20">
                <div className="h-full rounded-full bg-gradient-to-r from-amber-400 to-amber-500" style={{ width: `${av}%` }} />
              </div>
              <div className="mt-3 flex items-center justify-between text-[11px] text-white/75">
                <span className="inline-flex items-center gap-1.5"><FaCircleCheck className="h-3.5 w-3.5 text-emerald-300" />{cTaches.filter((t) => t.statut === "Validée").length} validée(s)</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* KPI */}
      <div className="mb-4 grid grid-cols-2 gap-4 lg:grid-cols-3">
        <KpiCard icon={<FaClipboardList className="h-5 w-5" />} label="Tâches" value={cTaches.length} tone="blue" />
        <KpiCard icon={<FaUsers className="h-5 w-5" />} label="Équipes" value={(affectationsQuery.data ?? []).length} tone="emerald" />
        <KpiCard icon={<FaHelmetSafety className="h-5 w-5" />} label="Membres" value={membres.length} tone="violet" />
      </div>

      <Card className="p-2">
        <Tabs tabs={TABS.map((t) => ({ key: t, label: t }))} active={tab} onChange={setTab} layoutId="chantierTabs" />
      </Card>

      <Card className="mt-4 p-5">
        {tab === "Infos" && (
          <div className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <InfoItem label="Statut" value={<Badge tone={chantierStatutTone(chantier.statut)} dot>{chantier.statut}</Badge>} />
              <InfoItem label="Responsable" value={chantier.responsableNom ?? "-"} />
              <InfoItem label="Localisation" value={chantier.localisation} />
              <InfoItem label="Date de début" value={formatDate(chantier.dateDebut)} />
              <InfoItem label="Date de fin" value={formatDate(chantier.dateFin)} />
            </div>
            <div className="rounded-xl border border-line bg-app/40 p-4">
              <div className="mb-2 flex items-center justify-between"><span className="text-sm font-semibold text-ink">Avancement</span><span className="text-sm font-black text-ink">{av}%</span></div>
              <Progress value={av} tone={av >= 100 ? "emerald" : "blue"} />
            </div>
            {chantier.description && <p className="text-sm leading-relaxed text-body">{chantier.description}</p>}
          </div>
        )}

        {tab === "Tâches" && (
          <div>
            {tachesQuery.isLoading ? (
              <FullSpinner label="Chargement des tâches…" />
            ) : tachesQuery.isError ? (
              <ErrorState message={getApiErrorMessage(tachesQuery.error)} />
            ) : (
              <TaskMiniList tasks={cTaches} onSelect={(t) => navigate(`/taches/${t.id}`)} emptyLabel="Aucune tâche pour ce chantier" />
            )}
          </div>
        )}

        {tab === "Équipes" && (
          <div>
            {affectationsQuery.isError ? (
              <ErrorState message={getApiErrorMessage(affectationsQuery.error)} />
            ) : (affectationsQuery.data ?? []).length === 0 ? (
              <EmptyState title="Aucune équipe affectée" description="Affectez une équipe depuis le module Équipes." />
            ) : (
              <div className="space-y-2">
                {(affectationsQuery.data ?? []).map((a) => {
                  const equipeId = equipeIdByNom.get(a.equipeNom);
                  const count = membres.filter((m) => m.equipeId === equipeId).length;
                  return (
                    <div key={a.id} className="flex items-center gap-3 rounded-xl border border-line bg-app/40 p-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-accent/10 text-accent"><FaUsers className="h-5 w-5" /></div>
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-semibold text-ink">{a.equipeNom}</p>
                        <p className="truncate text-xs text-muted">{count} membre(s) · {formatDate(a.dateDebut)}{a.dateFin ? ` → ${formatDate(a.dateFin)}` : ""}</p>
                      </div>
                      <Badge tone={a.statut === "ACTIVE" ? "emerald" : "slate"}>{affectationStatutLabel(a.statut)}</Badge>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {tab === "Utilisateurs" && (
          <div>
            {membres.length === 0 ? (
              <EmptyState title="Aucun membre dans les équipes affectées" description="Les membres des équipes de ce chantier apparaissent ici." />
            ) : (
              <div className="divide-y divide-line">
                {membres.map((m) => (
                  <div key={m.id} className="flex items-center gap-3 py-3">
                    <span className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 text-xs font-bold text-white">{m.utilisateurPrenom[0]}{m.utilisateurNom[0]}</span>
                    <div className="min-w-0 flex-1"><p className="truncate text-sm font-semibold text-ink">{m.utilisateurPrenom} {m.utilisateurNom}</p><p className="truncate text-xs text-muted">{m.equipeNom}</p></div>
                    <Badge tone={m.roleDansEquipe === "CHEF" ? "amber" : "slate"}>{m.roleDansEquipe === "CHEF" ? "Chef d'équipe" : "Ouvrier"}</Badge>
                    {canDeleteMembres && <Button variant="ghost" className="px-2 py-1.5 text-xs text-danger hover:bg-danger-light" loading={removeMemberMutation.isPending} onClick={() => removeMemberMutation.mutate(m.id)}>Retirer</Button>}
                  </div>
                ))}
              </div>
            )}
            <div className="mt-4 flex flex-col gap-2 border-t border-line pt-4 sm:flex-row">
              {canEditChantiers ? (
                <>
                <SelectInput value={assignId} onChange={(e) => setAssignId(e.target.value)} className="flex-1">
                  <option value="">Ajouter un utilisateur…</option>
                  {assignableUsers.map((u) => <option key={u.id} value={u.id}>{u.prenom} {u.nom}</option>)}
                </SelectInput>
                <Button onClick={assignUser} disabled={!assignId || teamIds.length === 0} loading={addMemberMutation.isPending}>Assigner</Button>
                </>
              ) : (
                <p className="text-sm text-muted">Vous n'avez pas les droits de gestion des membres de ce chantier.</p>
              )}
            </div>
            {teamIds.length === 0 && <p className="mt-2 text-xs text-muted">Affectez d'abord une équipe à ce chantier pour y ajouter des membres.</p>}
          </div>
        )}

        {tab === "Statistiques" && (
          <div>
            <div className="mb-4 grid grid-cols-2 gap-4">
              <InfoItem label="Tâches" value={cTaches.length} />
              <InfoItem label="Validées" value={cTaches.filter((t) => t.statut === "Validée").length} />
            </div>
            <BarChart data={["À faire", "En cours", "Validée", "Refusée"].map((s) => ({ name: s, value: cTaches.filter((t) => t.statut === s).length }))} color="#1e3a5f" />
            <div className="mt-4 space-y-3">
              {["À faire", "En cours", "Validée", "Refusée"].map((s, i) => {
                const tones = ["slate", "blue", "emerald", "rose"] as const;
                const count = cTaches.filter((t) => t.statut === s).length;
                return <div key={s}><div className="mb-1 flex justify-between text-xs"><span className="text-body">{s}</span><span className="font-semibold text-ink">{count}</span></div><Progress value={cTaches.length ? (count / cTaches.length) * 100 : 0} tone={tones[i]} /></div>;
              })}
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}

export function SimpleTable({ head, rows, empty = "Aucune donnée" }: { head: string[]; rows: React.ReactNode[][]; empty?: string }) {
  if (!rows.length) return <EmptyState title={empty} />;
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[480px] text-sm">
        <thead><tr className="border-b border-line text-left text-xs uppercase tracking-wide text-muted">{head.map((h) => <th key={h} className="px-2 py-2 font-semibold">{h}</th>)}</tr></thead>
        <tbody className="divide-y divide-line">{rows.map((r, i) => <tr key={i} className="hover:bg-app/50">{r.map((c, j) => <td key={j} className="px-2 py-3 text-body">{c}</td>)}</tr>)}</tbody>
      </table>
    </div>
  );
}

function KpiCard({ icon, label, value, tone }: { icon: React.ReactNode; label: string; value: React.ReactNode; tone: "blue" | "emerald" | "violet" | "amber" }) {
  const tones = {
    blue: "bg-info-light text-info",
    emerald: "bg-success-light text-success",
    violet: "bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-300",
    amber: "bg-warning-light text-warning",
  } as const;
  return (
    <Card className="flex items-center gap-3 p-4">
      <span className={cn("flex h-11 w-11 shrink-0 items-center justify-center rounded-xl", tones[tone])}>{icon}</span>
      <div className="min-w-0">
        <p className="truncate text-xl font-black text-ink">{value}</p>
        <p className="text-xs text-muted">{label}</p>
      </div>
    </Card>
  );
}
