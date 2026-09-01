import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { FaArrowLeft, FaBuilding, FaCircleCheck, FaChevronRight, FaHelmetSafety, FaLocationDot, FaPen, FaPlus, FaRotate, FaUserMinus, FaUsers, FaXmark } from "react-icons/fa6";
import { toast } from "react-toastify";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getApiErrorMessage } from "../core/api/axios";
import { useApp } from "../core/store/AppProvider";
import { chantierStatutLabel, getChantierUtilisateurs, getChantiers } from "../chantiers/api";
import {
  addEquipeMembre,
  affecterEquipeChantier,
  createEquipe,
  getEquipe,
  getEquipeAffectations,
  getEquipeMembres,
  getEquipesDetaillees,
  removeEquipeMembre,
  terminerAffectation,
  updateEquipe,
} from "../equipes/api";
import type {
  AffectationEquipeChantierResponse,
  ChantierResume,
  MembreEquipeResponse,
  UtilisateurResume,
} from "../core/api/types";
import {
  Avatar,
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Field,
  FullSpinner,
  Modal,
  PageHeader,
  Pill,
  Progress,
  SearchBox,
  SelectInput,
  Table,
  TextInput,
} from "../core/components/ui";
import { cn } from "../core/utils/cn";
import { chantierStatutTone } from "../core/lib/styles";
import type { ChantierStatut } from "../core/lib/types";

/** Ligne enrichie : résumé + description + chantier actif + membres (API réelle). */
interface EquipeRow {
  id: number;
  nom: string;
  description: string;
  chantierId: number | null;
  chantierNom: string | null;
  affectations: AffectationEquipeChantierResponse[];
  membres: MembreEquipeResponse[];
}

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

async function loadEquipes(): Promise<EquipeRow[]> {
  const list = await getEquipesDetaillees();
  return list.map((e) => {
    const active = e.affectations.find((a) => a.statut === "ACTIVE");
    return {
      id: e.id,
      nom: e.nom,
      description: e.description ?? "",
      chantierId: active?.chantierId ?? null,
      chantierNom: active?.chantierNom ?? null,
      affectations: e.affectations,
      membres: e.membres,
    };
  });
}

export function EquipesList() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { hasPerm } = useApp();
  const canCreateEquipes = hasPerm("EQUIPE_CREER");
  const canEditEquipes = hasPerm("EQUIPE_MODIFIER");
  const [searchParams, setSearchParams] = useSearchParams();
  const [q, setQ] = useState("");
  const [chantierSearch, setChantierSearch] = useState("");
  const [chantierStatutFilter, setChantierStatutFilter] = useState("");
  const [selectedChantier, setSelectedChantier] = useState<ChantierResume | null>(null);
  // bloque la restauration depuis l'URL quand l'utilisateur a explicitement
  // cliqué « Sélectionner un autre chantier » (sinon course entre le state
  // et la navigation : le premier clic restaure par erreur l'ancien chantier).
  const clearedManually = useRef(false);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<EquipeRow | null>(null);
  const [form, setForm] = useState({ nom: "", description: "", chantierId: "" });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const query = useQuery({ queryKey: ["equipes", "detaillees"], queryFn: loadEquipes });
  const chantiersQuery = useQuery({
    queryKey: ["chantiers"],
    queryFn: () => getChantiers({ page: 0, size: 100 }),
  });

  const equipes = query.data ?? [];
  const chantiers: ChantierResume[] = (chantiersQuery.data?.content ?? []).filter(
    (c) => c.statut !== "ANNULE"
  );

  // Mémorise le chantier choisi dans l'URL (?chantier=) ; restauration au chargement.
  useEffect(() => {
    if (clearedManually.current) return;
    if (!selectedChantier) {
      const id = Number(searchParams.get("chantier"));
      const found = chantiers.find((c) => c.id === id);
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

  // Cartes chantiers : filtres recherche + statut (au cas où il y en a beaucoup).
  const filteredChantiers = useMemo(
    () =>
      chantiers.filter(
        (c) =>
          c.nom.toLowerCase().includes(chantierSearch.toLowerCase()) &&
          (!chantierStatutFilter || c.statut === chantierStatutFilter)
      ),
    [chantiers, chantierSearch, chantierStatutFilter]
  );

  // Nombre d'équipes par chantier (pour les cartes).
  const equipesParChantier = useMemo(() => {
    const m = new Map<number, number>();
    for (const e of equipes) {
      if (e.chantierId != null) m.set(e.chantierId, (m.get(e.chantierId) ?? 0) + 1);
    }
    return m;
  }, [equipes]);

  const scopedEquipes = selectedChantier
    ? equipes.filter((e) => e.chantierId === selectedChantier.id)
    : equipes;

  // Filtres des équipes du chantier sélectionné.
  const filtered = useMemo(
    () =>
      scopedEquipes
        .filter(
          (e) =>
            e.nom.toLowerCase().includes(q.toLowerCase()) ||
            e.description.toLowerCase().includes(q.toLowerCase())
        )
        .sort((a, b) => a.nom.localeCompare(b.nom)),
    [scopedEquipes, q]
  );
  const totalMembers = scopedEquipes.reduce((s, e) => s + e.membres.length, 0);
  const activeFilters = q;
  const reset = () => { setQ(""); };

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["equipes"] });
  };

  const saveMutation = useMutation({
    mutationFn: async () => {
      const ers: Record<string, string> = {};
      if (!form.nom.trim()) ers.nom = "Le nom est obligatoire.";
      else if (/[<>{}]/.test(form.nom)) ers.nom = "Le nom contient des caractères non autorisés.";
      if (Object.keys(ers).length > 0) {
        setErrors(ers);
        throw { validation: true };
      }
      setErrors({});
      if (editing) {
        await updateEquipe(editing.id, { nom: form.nom, description: form.description || undefined });
        const newChantierId = form.chantierId ? Number(form.chantierId) : null;
        if (newChantierId !== editing.chantierId) {
          const active = editing.affectations.find((a) => a.statut === "ACTIVE");
          if (active) await terminerAffectation(active.id);
          if (newChantierId) {
            await affecterEquipeChantier({ equipeId: editing.id, chantierId: newChantierId, dateDebut: today() });
          }
        }
      } else {
        const chantierId = selectedChantier?.id ?? (form.chantierId ? Number(form.chantierId) : null);
        const created = await createEquipe({ nom: form.nom, description: form.description || undefined });
        if (chantierId) {
          await affecterEquipeChantier({ equipeId: created.id, chantierId, dateDebut: today() });
        }
      }
    },
    onSuccess: () => {
      toast.success(editing ? "Équipe modifiée." : "Équipe créée.");
      setOpen(false);
      invalidate();
    },
    onError: (err) => {
      if ((err as { validation?: boolean }).validation) {
        toast.error("Veuillez corriger les champs signalés.");
        return;
      }
      toast.error(getApiErrorMessage(err));
    },
  });

  const openNew = () => {
    setEditing(null);
    setForm({ nom: "", description: "", chantierId: selectedChantier?.id ? String(selectedChantier.id) : "" });
    setErrors({});
    setOpen(true);
  };
  const openEdit = (e: EquipeRow) => {
    setEditing(e);
    setForm({ nom: e.nom, description: e.description, chantierId: e.chantierId ? String(e.chantierId) : "" });
    setErrors({});
    setOpen(true);
  };

  return (
    <div>
      {!selectedChantier ? (
        <>
          <PageHeader title="Équipes" subtitle="Choisissez un chantier, puis constituez et gérez ses équipes ici." />
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
                <Button icon={<FaPlus className="h-4 w-4" />} onClick={() => navigate("/chantiers")}>Nouveau chantier</Button>
              </div>
            </div>
          </Card>

          {chantiersQuery.isLoading || query.isLoading ? (
            <FullSpinner label="Chargement des chantiers…" />
          ) : chantiersQuery.isError ? (
            <ErrorState message={getApiErrorMessage(chantiersQuery.error)} />
          ) : filteredChantiers.length === 0 ? (
            <EmptyState title="Aucun chantier" description="Créez d'abord un chantier pour pouvoir constituer des équipes." />
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
                    <span className="text-xs font-semibold text-muted">{equipesParChantier.get(c.id) ?? 0} équipe(s)</span>
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
          <PageHeader title="Équipes" subtitle={`Chantier : ${selectedChantier.nom}`}>
            <Button variant="secondary" icon={<FaArrowLeft className="h-4 w-4" />} onClick={clearChantier}>Sélectionner un autre chantier</Button>
            {canCreateEquipes && <Button icon={<FaPlus className="h-4 w-4" />} onClick={openNew}>Nouvelle équipe</Button>}
          </PageHeader>

          <div className="mb-4 grid grid-cols-1 gap-4 lg:grid-cols-3">
            <StatCard tone="blue" icon={<FaHelmetSafety className="h-5 w-5" />} label="Équipes du chantier" value={scopedEquipes.length} />
            <StatCard tone="emerald" icon={<FaUsers className="h-5 w-5" />} label="Membres total" value={totalMembers} />
            <StatCard tone="violet" icon={<FaCircleCheck className="h-5 w-5" />} label="Avancement du chantier" value={<Progress value={selectedChantier.progression} tone="blue" showLabel className="mt-1 w-24" />} />
          </div>

          <Card className="mb-4 p-4">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
              <SearchBox value={q} onChange={setQ} placeholder="Rechercher une équipe…" />
              <div className="flex items-center rounded-lg border border-line bg-app px-3 py-2 text-sm text-body"><FaBuilding className="mr-2 h-4 w-4 text-muted" /><span className="truncate">{selectedChantier.nom}</span></div>
              {canCreateEquipes && <div className="flex items-center justify-end"><Button icon={<FaPlus className="h-4 w-4" />} onClick={openNew}>Nouvelle équipe</Button></div>}
            </div>
            {activeFilters && (
              <div className="mt-3 flex flex-wrap items-center gap-2 border-t border-line pt-3">
                <span className="text-xs font-semibold text-muted">Filtres actifs :</span>
                {q && <Pill tone="blue">{q} <button onClick={() => setQ("")}><FaXmark className="h-3 w-3" /></button></Pill>}
                <Button variant="ghost" className="px-2 py-1 text-xs" icon={<FaRotate className="h-3 w-3" />} onClick={reset}>Réinitialiser</Button>
              </div>
            )}
          </Card>

          {query.isLoading ? (
            <FullSpinner label="Chargement des équipes…" />
          ) : query.isError ? (
            <ErrorState message={getApiErrorMessage(query.error)} />
          ) : filtered.length === 0 ? (
            <EmptyState title="Aucune équipe sur ce chantier" description="Créez la première équipe de ce chantier." />
          ) : (
            <Card className="p-4">
              <Table
                head={["Équipe", "Domaine", "Membres", "Actions"]}
                rows={filtered.map((e) => [
                  <button onClick={() => navigate(`/equipes/${e.id}`)} className="flex items-center gap-2 font-semibold text-ink hover:text-accent">
                    <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent/10 text-accent"><FaHelmetSafety className="h-4 w-4" /></span>{e.nom}
                  </button>,
                  <span className="text-body">{e.description || "-"}</span>,
                  <div className="flex items-center gap-2">{e.membres.slice(0, 4).map((m) => <Avatar key={m.id} prenom={m.utilisateurPrenom} nom={m.utilisateurNom} size={26} />)}{e.membres.length === 0 ? <span className="text-xs text-muted">Aucun</span> : <span className="text-xs font-semibold text-muted">{e.membres.length}</span>}</div>,
                  <div className="flex justify-end gap-1">
                    <button onClick={() => navigate(`/equipes/${e.id}`)} className="inline-flex items-center gap-1 rounded-lg px-2 py-1.5 text-xs font-semibold text-accent hover:bg-accent/10">Voir</button>
                    {canEditEquipes && <button onClick={() => openEdit(e)} className="rounded-lg p-2 text-muted hover:bg-app hover:text-accent"><FaPen className="h-4 w-4" /></button>}
                  </div>,
                ])}
              />
            </Card>
          )}
        </>
      )}

      <Modal open={open} onClose={() => setOpen(false)} title={editing ? "Modifier l'équipe" : "Nouvelle équipe"} footer={<><Button variant="secondary" onClick={() => setOpen(false)}>Annuler</Button><Button onClick={() => saveMutation.mutate()} loading={saveMutation.isPending}>{editing ? "Enregistrer" : "Créer"}</Button></>}>
        <div className="space-y-4">
          <Field label="Nom" required error={errors.nom}><TextInput invalid={!!errors.nom} value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} /></Field>
          <Field label="Domaine"><TextInput value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
          {editing ? (
            <Field label="Chantier"><SelectInput value={form.chantierId} onChange={(e) => setForm({ ...form, chantierId: e.target.value })}><option value="">Aucun</option>{chantiers.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}</SelectInput></Field>
          ) : selectedChantier ? (
            <Field label="Chantier" required><p className="rounded-lg border border-line bg-app px-3 py-2 text-sm text-body"><FaBuilding className="mr-2 inline h-4 w-4 text-muted" />{selectedChantier.nom}</p></Field>
          ) : (
            <Field label="Chantier"><SelectInput value={form.chantierId} onChange={(e) => setForm({ ...form, chantierId: e.target.value })}><option value="">Aucun</option>{chantiers.map((c) => <option key={c.id} value={c.id}>{c.nom}</option>)}</SelectInput></Field>
          )}
        </div>
      </Modal>
    </div>
  );
}

export function EquipeDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { hasPerm } = useApp();
  const canEditEquipes = hasPerm("EQUIPE_MODIFIER");
  const canDeleteEquipes = hasPerm("EQUIPE_SUPPRIMER");
  const numId = Number(id);
  const [search, setSearch] = useState("");
  const [pendingUserIds, setPendingUserIds] = useState<number[]>([]);

  const teamQuery = useQuery({ queryKey: ["equipe", numId], queryFn: () => getEquipe(numId), enabled: !!numId });
  const membresQuery = useQuery({ queryKey: ["equipe-membres", numId], queryFn: () => getEquipeMembres(numId), enabled: !!numId });
  const affectationsQuery = useQuery({ queryKey: ["equipe-affectations", numId], queryFn: () => getEquipeAffectations(numId), enabled: !!numId });

  const team = teamQuery.data;
  const membres = membresQuery.data ?? [];
  const affectations = affectationsQuery.data ?? [];
  const chantier = affectations.find((a) => a.statut === "ACTIVE");
  const chantierId = chantier?.chantierId ?? null;

  // Candidats = uniquement les utilisateurs affectés au chantier de l'équipe.
  const usersQuery = useQuery({
    queryKey: ["chantier-utilisateurs", chantierId],
    queryFn: () => getChantierUtilisateurs(chantierId!),
    enabled: !!chantierId,
  });
  const users: UtilisateurResume[] = usersQuery.data ?? [];
  const memberIds = membres.map((m) => m.utilisateurId);
  const availableUsers = users.filter(
    (u) =>
      !memberIds.includes(u.id) &&
      `${u.prenom} ${u.nom} ${u.email}`.toLowerCase().includes(search.trim().toLowerCase())
  );
  const userById = new Map(users.map((u) => [u.id, u]));

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["equipe-membres", numId] });
    queryClient.invalidateQueries({ queryKey: ["equipe", numId] });
    queryClient.invalidateQueries({ queryKey: ["equipes"] });
  };

  const addMutation = useMutation({
    mutationFn: (utilisateurs: number[]) => Promise.all(
      utilisateurs.map((utilisateurId) =>
        addEquipeMembre({ utilisateurId, equipeId: numId, roleDansEquipe: "OUVRIER", dateIntegration: today() })
      )
    ),
    onSuccess: () => {
      toast.success("Membre(s) ajouté(s).");
      setSearch("");
      setPendingUserIds([]);
      invalidate();
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const removeMutation = useMutation({
    mutationFn: (membreId: number) => removeEquipeMembre(membreId),
    onSuccess: () => {
      toast.success("Membre retiré.");
      invalidate();
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  if (teamQuery.isLoading) return <FullSpinner label="Chargement de l'équipe…" />;
  if (teamQuery.isError || !team) return <EmptyState title="Équipe introuvable" action={<Button onClick={() => navigate("/equipes")}>Retour</Button>} />;

  return (
    <div>
      <button onClick={() => navigate("/equipes")} className="mb-4 inline-flex items-center gap-1.5 text-sm font-semibold text-muted hover:text-ink"><FaArrowLeft className="h-4 w-4" /> Retour aux équipes</button>

      <Card className="p-6">
        <div className="mb-4 flex items-center gap-4">
          <span className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-accent to-accent-soft text-white"><FaHelmetSafety className="h-7 w-7" /></span>
          <div className="flex-1">
            <h1 className="text-2xl font-black text-ink">{team.nom}</h1>
            <p className="text-sm text-muted">{team.description || "-"}{chantier && <> · <span className="text-body">{chantier.chantierNom}</span></>}</p>
          </div>
          <Badge tone="blue" dot>{membres.length} membre{membres.length > 1 ? "s" : ""}</Badge>
        </div>

        <div className="mb-4 border-y border-line py-4">
          {canEditEquipes ? (
            !chantierId ? (
              <p className="text-sm text-muted">Cette équipe n'est affectée à aucun chantier. Affectez-la d'abord à un chantier pour ajouter des membres.</p>
            ) : (
            <>
            <div className="flex flex-col gap-2 sm:flex-row">
              <div className="flex-1"><SearchBox value={search} onChange={setSearch} placeholder="Rechercher un utilisateur…" /></div>
              <Button onClick={() => { if (pendingUserIds.length) addMutation.mutate(pendingUserIds); }} disabled={!pendingUserIds.length} loading={addMutation.isPending} icon={<FaPlus className="h-4 w-4" />}>Ajouter ({pendingUserIds.length})</Button>
            </div>
            <div className="mt-3 max-h-64 space-y-1 overflow-y-auto rounded-xl border border-line p-2">
              {availableUsers.length === 0 ? (
                <p className="px-2 py-3 text-center text-sm text-muted">{search.trim() ? `Aucun utilisateur ne correspond à « ${search} »` : "Aucun utilisateur disponible. Affectez des utilisateurs à ce chantier depuis la page Utilisateurs."}</p>
              ) : (
                availableUsers.map((u) => {
                  const checked = pendingUserIds.includes(u.id);
                  return (
                    <label key={u.id} className="flex cursor-pointer items-center gap-2 rounded-lg px-2 py-1.5 text-sm hover:bg-app">
                      <input
                        type="checkbox"
                        className="h-4 w-4 rounded border-line accent-accent"
                        checked={checked}
                        onChange={(e) =>
                          setPendingUserIds((prev) =>
                            e.target.checked ? [...prev, u.id] : prev.filter((id) => id !== u.id)
                          )
                        }
                      />
                      <Avatar prenom={u.prenom} nom={u.nom} size={24} />
                      <span className="text-ink">{u.prenom} {u.nom}</span>
                      <span className="truncate text-xs text-muted">{u.email}</span>
                    </label>
                  );
                })
              )}
            </div>
            </>
            )
          ) : (
          <p className="text-sm text-muted">Vous n'avez pas les droits de modification des membres de cette équipe.</p>
          )}
        </div>

        {membres.length === 0 ? (
          <EmptyState title="Aucun membre" description="Ajoutez des utilisateurs à cette équipe." />
        ) : (
          <div className="divide-y divide-line">
            {membres.map((m) => {
              const u = userById.get(m.utilisateurId);
              return (
                <div key={m.id} className="flex items-center gap-3 py-3">
                  <Avatar prenom={m.utilisateurPrenom} nom={m.utilisateurNom} size={40} />
                  <div className="min-w-0 flex-1"><p className="truncate text-sm font-semibold text-ink">{m.utilisateurPrenom} {m.utilisateurNom}</p><p className="truncate text-xs text-muted">{u?.email ?? "-"}</p></div>
                  {canDeleteEquipes && <Button variant="ghost" className="px-2 py-1.5 text-xs text-danger hover:bg-danger-light" icon={<FaUserMinus className="h-4 w-4" />} onClick={() => removeMutation.mutate(m.id)}>Retirer</Button>}
                </div>
              );
            })}
          </div>
        )}
      </Card>
    </div>
  );
}

function StatCard({ tone, icon, label, value }: { tone: "blue" | "emerald" | "violet"; icon: React.ReactNode; label: string; value: React.ReactNode }) {
  return (
    <Card className="flex items-center gap-3 p-4">
      <span className={cn("flex h-10 w-10 items-center justify-center rounded-xl", tone === "blue" && "bg-info-light text-info", tone === "emerald" && "bg-success-light text-success", tone === "violet" && "bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-300")}>{icon}</span>
      <div><p className="text-2xl font-black text-ink">{value}</p><p className="text-xs text-muted">{label}</p></div>
    </Card>
  );
}
