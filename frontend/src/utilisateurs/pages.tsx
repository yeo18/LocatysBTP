import { useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-toastify";
import { FaArrowLeft, FaBuilding, FaCheck, FaKey, FaPen, FaPlus, FaShieldHalved, FaTrashCan, FaUsers } from "react-icons/fa6";
import { getApiErrorMessage } from "../core/api/axios";
import {
  affecterUtilisateurChantier,
  accorderPermissionChantier,
  chantierStatutLabel,
  getChantierPermissions,
  getChantiers,
  getChantierUtilisateurs,
  refuserPermissionChantier,
  retirerPermissionChantier,
  retirerUtilisateurChantier,
} from "../chantiers/api";
import {
  getUser,
  getUserPermissions,
  getUsers,
} from "../utilisateurs/api";
import {
  createProfil,
  deleteProfil,
  getProfil,
  getProfilPermissions,
  getProfils,
  grantProfilPermission,
  revokeProfilPermission,
  updateProfil,
} from "../utilisateurs/profils";
import { getPermissions } from "../utilisateurs/permissions";
import { useApp } from "../core/store/AppProvider";
import type {
  ChantierResume,
  PermissionResume,
  ProfilPermission,
  ProfilResume,
  UtilisateurResume,
} from "../core/api/types";
import {
  Avatar,
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
  Pill,
  Progress,
  SearchBox,
  SelectInput,
  Tabs,
  TextInput,
} from "../core/components/ui";
import { cn } from "../core/utils/cn";
import { formatDate } from "../core/lib/format";

const IS_ADMIN = "ADMINISTRATEUR";

// ============ UsersList ============

export function UsersList() {
  const navigate = useNavigate();
  const { hasPerm } = useApp();
  const canGrantPerms = hasPerm("PERMISSION_MODIFIER");
  const canAssignChantier = hasPerm("CHANTIER_MODIFIER");
  const [q, setQ] = useState("");
  const [profilFilter, setProfilFilter] = useState("");
  const [chantierUser, setChantierUser] = useState<UtilisateurResume | null>(null);
  const [permUser, setPermUser] = useState<UtilisateurResume | null>(null);

  const chantiersQuery = useQuery({
    queryKey: ["chantiers"],
    queryFn: () => getChantiers({ page: 0, size: 100 }),
  });
  const usersQuery = useQuery({
    queryKey: ["users-all"],
    queryFn: () => getUsers({ page: 0, size: 200 }),
  });
  const profilsQuery = useQuery({
    queryKey: ["profils"],
    queryFn: () => getProfils({ page: 0, size: 100 }),
  });

  const chantiers: ChantierResume[] = (chantiersQuery.data?.content ?? []).filter(
    (c) => c.statut !== "ANNULE"
  );
  const allUsers = usersQuery.data?.content ?? [];
  const profils = profilsQuery.data?.content ?? [];

  const chantierUsersQueries = useQueries({
    queries: chantiers.map((c) => ({
      queryKey: ["chantier-utilisateurs", c.id],
      queryFn: () => getChantierUtilisateurs(c.id),
    })),
  });

  // utilisateurId -> ensemble des chantiers où il est affecté.
  const chantiersParUtilisateur = useMemo(() => {
    const m = new Map<number, Set<number>>();
    chantiers.forEach((c, i) => {
      const users = chantierUsersQueries[i]?.data ?? [];
      for (const u of users) {
        const set = m.get(u.id) ?? new Set<number>();
        set.add(c.id);
        m.set(u.id, set);
      }
    });
    return m;
  }, [chantiers, chantierUsersQueries]);

  const chantierNom = useMemo(() => new Map(chantiers.map((c) => [c.id, c.nom])), [chantiers]);

  const filtered = useMemo(
    () =>
      allUsers.filter(
        (u) =>
          `${u.prenom} ${u.nom} ${u.email}`.toLowerCase().includes(q.toLowerCase()) &&
          (!profilFilter || u.profilNom === profilFilter)
      ),
    [allUsers, q, profilFilter]
  );

  const metrics = useMemo(
    () => [
      { label: "Utilisateurs", value: allUsers.length, tone: "blue" as const, icon: <FaUsers className="h-5 w-5" /> },
      { label: "Admins", value: allUsers.filter((u) => u.profilNom === IS_ADMIN).length, tone: "violet" as const, icon: <FaShieldHalved className="h-5 w-5" /> },
    ],
    [allUsers]
  );

  return (
    <div>
      <PageHeader title="Utilisateurs" subtitle="Tous les comptes de l'application. Affectez chaque utilisateur aux chantiers où il travaille." />

      <div className="mb-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
        {metrics.map((m) => (
          <StatCard key={m.label} tone={m.tone} icon={m.icon} label={m.label} value={m.value} />
        ))}
      </div>

      <Card className="mb-4 p-4">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <SearchBox value={q} onChange={setQ} placeholder="Rechercher un utilisateur…" />
          <SelectInput value={profilFilter} onChange={(e) => setProfilFilter(e.target.value)}>
            <option value="">Tous les profils</option>
            {profils.map((p) => <option key={p.id} value={p.nom}>{p.nom}</option>)}
          </SelectInput>
        </div>
      </Card>

      {usersQuery.isLoading ? (
        <FullSpinner label="Chargement des utilisateurs…" />
      ) : usersQuery.isError ? (
        <ErrorState message={getApiErrorMessage(usersQuery.error)} />
      ) : filtered.length === 0 ? (
        <EmptyState title="Aucun utilisateur" description="Aucun utilisateur ne correspond à votre recherche." />
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {filtered.map((u) => {
            const chantierIds = chantiersParUtilisateur.get(u.id);
            return (
              <Card key={u.id} className="flex flex-col p-5">
                <div className="flex items-start gap-3">
                  <Avatar prenom={u.prenom} nom={u.nom} gradient="from-blue-500 to-indigo-600" size={48} />
                  <div className="min-w-0 flex-1">
                    <button onClick={() => navigate(`/utilisateurs/${u.id}`)} className="block truncate text-left font-bold text-ink hover:text-accent">{u.prenom} {u.nom}</button>
                    <p className="truncate text-xs text-muted">{u.email}</p>
                  </div>
                </div>
                <div className="mt-3 flex items-center justify-between text-xs">
                  <span className="text-muted">Profil</span>
                  <Badge tone="blue">{u.profilNom}</Badge>
                </div>
                <div className="mt-3 border-t border-line pt-3">
                  <p className="mb-1.5 text-xs font-semibold uppercase tracking-wide text-muted">Chantiers</p>
                  {!chantierIds || chantierIds.size === 0 ? (
                    <p className="text-xs text-muted">Aucun chantier</p>
                  ) : (
                    <div className="flex flex-wrap gap-1.5">
                      {[...chantierIds].map((cid) => (
                        <span key={cid} className="inline-flex items-center gap-1 rounded-full bg-app px-2.5 py-1 text-xs font-medium text-body">
                          <FaBuilding className="h-3 w-3 text-muted" />{chantierNom.get(cid) ?? `#${cid}`}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
                <div className="mt-4 flex flex-wrap gap-1 border-t border-line pt-3">
                  {canAssignChantier && <Button variant="secondary" className="px-2.5 py-1.5 text-xs" icon={<FaBuilding className="h-3.5 w-3.5" />} onClick={() => setChantierUser(u)}>Chantiers</Button>}
                  {canGrantPerms && <Button variant="secondary" className="px-2.5 py-1.5 text-xs" icon={<FaKey className="h-3.5 w-3.5" />} onClick={() => setPermUser(u)}>Permissions</Button>}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {chantierUser && <GestionChantiersModal user={chantierUser} chantiers={chantiers} assigned={chantiersParUtilisateur.get(chantierUser.id) ?? new Set()} onClose={() => setChantierUser(null)} />}
      {permUser && (
        <PermsModal
          user={permUser}
          chantiers={permUser.profilNom === IS_ADMIN ? chantiers : chantiers.filter((c) => chantiersParUtilisateur.get(permUser.id)?.has(c.id))}
          onClose={() => setPermUser(null)}
        />
      )}
    </div>
  );
}

// ============ Create / Edit ============

function GestionChantiersModal({
  user,
  chantiers,
  assigned,
  onClose,
}: {
  user: UtilisateurResume;
  chantiers: ChantierResume[];
  assigned: Set<number>;
  onClose: () => void;
}) {
  const queryClient = useQueryClient();
  const [local, setLocal] = useState<Set<number>>(assigned);
  const mutation = useMutation({
    mutationFn: ({ chantierId, affecter }: { chantierId: number; affecter: boolean }) =>
      affecter ? affecterUtilisateurChantier(chantierId, user.id) : retirerUtilisateurChantier(chantierId, user.id),
    onSuccess: (_data, vars) => {
      setLocal((prev) => {
        const next = new Set(prev);
        if (vars.affecter) next.add(vars.chantierId);
        else next.delete(vars.chantierId);
        return next;
      });
      queryClient.invalidateQueries({ queryKey: ["chantier-utilisateurs", vars.chantierId] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  return (
    <Modal open onClose={onClose} title="Chantiers de l'utilisateur" subtitle={`${user.prenom} ${user.nom} : cochez les chantiers où il travaille.`} size="md">
      <div className="max-h-[50vh] space-y-1 overflow-y-auto pr-1">
        {chantiers.length === 0 ? (
          <p className="px-2 py-4 text-center text-sm text-muted">Aucun chantier. Créez d'abord un chantier.</p>
        ) : (
          chantiers.map((c) => {
            const checked = local.has(c.id);
            return (
              <label key={c.id} className="flex cursor-pointer items-center gap-3 rounded-xl border border-line px-3 py-2.5 transition hover:bg-app">
                <input type="checkbox" className="h-4 w-4 shrink-0 rounded border-line accent-accent" checked={checked} onChange={(e) => mutation.mutate({ chantierId: c.id, affecter: e.target.checked })} />
                <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-info-light text-info"><FaBuilding className="h-4 w-4" /></span>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium text-ink">{c.nom}</p>
                  <p className="truncate text-xs text-muted">{c.adresseSaisie || "-"}</p>
                </div>
              </label>
            );
          })
        )}
      </div>
    </Modal>
  );
}

// ============ Permissions par chantier (ACCORDER / REFUSER scopés) ============

function PermsModal({ user, chantiers, onClose }: { user: UtilisateurResume; chantiers: ChantierResume[]; onClose: () => void }) {
  const queryClient = useQueryClient();
  const [chantier, setChantier] = useState<ChantierResume | null>(null);
  const [search, setSearch] = useState("");
  const [statutFilter, setStatutFilter] = useState("");

  const permsQuery = useQuery({ queryKey: ["permissions"], queryFn: () => getPermissions({ page: 0, size: 100 }) });
  const exceptionsQuery = useQuery({
    queryKey: ["chantier-permissions", chantier?.id],
    queryFn: () => getChantierPermissions(chantier!.id),
    enabled: !!chantier,
  });

  const chantiersActifs = chantiers.filter((c) => c.statut !== "ANNULE");
  const perms = permsQuery.data?.content ?? [];
  const scoped = (exceptionsQuery.data ?? []).filter((e) => e.utilisateurId === user.id);

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["chantier-permissions", chantier?.id] });
    queryClient.invalidateQueries({ queryKey: ["me"] });
  };

  const toggle = useMutation({
    mutationFn: async ({ permissionId, type }: { permissionId: number; type: "ACCORDER" | "REFUSER" | null }) => {
      const existing = scoped.find((e) => e.permissionId === permissionId);
      if (existing) await retirerPermissionChantier(chantier!.id, existing.id);
      if (type) {
        if (type === "ACCORDER") await accorderPermissionChantier(chantier!.id, user.id, permissionId);
        else await refuserPermissionChantier(chantier!.id, user.id, permissionId);
      }
    },
    onSuccess: () => {
      toast.success("Permission mise à jour pour ce chantier uniquement.");
      invalidate();
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const stateFor = (permissionId: number) =>
    scoped.find((e) => e.permissionId === permissionId)?.type ?? null;

  const filteredChantiers = useMemo(
    () =>
      chantiersActifs.filter(
        (c) =>
          c.nom.toLowerCase().includes(search.toLowerCase()) &&
          (!statutFilter || c.statut === statutFilter)
      ),
    [chantiersActifs, search, statutFilter]
  );

  const grouped = useMemo(() => {
    const map = new Map<string, PermissionResume[]>();
    for (const p of perms) {
      const list = map.get(p.module) ?? [];
      list.push(p);
      map.set(p.module, list);
    }
    return [...map.entries()];
  }, [perms]);

  return (
    <Modal open onClose={onClose} title="Permissions sur un chantier" subtitle={`${user.prenom} ${user.nom}`} size="lg">
      {!chantier ? (
        <div>
          <div className="mb-3 rounded-xl border border-info/20 bg-info-light/40 px-3 py-2.5 text-xs text-body">
            <p className="mb-1 font-bold text-ink">Étape 1/2 : Sélectionnez le chantier</p>
            <p>La permission accordée ne vaudra QUE pour ce chantier (pas pour les autres où l'utilisateur est présent).</p>
          </div>
          <div className="mb-3 flex flex-col gap-2 sm:flex-row">
            <SearchBox value={search} onChange={setSearch} placeholder="Rechercher un chantier…" />
            <SelectInput value={statutFilter} onChange={(e) => setStatutFilter(e.target.value)} className="sm:w-auto">
              <option value="">Tous les statuts</option>
              {["PREVU", "EN_COURS", "TERMINE"].map((s) => (
                <option key={s} value={s}>{chantierStatutLabel(s as ChantierResume["statut"])}</option>
              ))}
            </SelectInput>
          </div>
          {chantiersActifs.length === 0 ? (
            <EmptyState
              title="Cet utilisateur n'est sur aucun chantier"
              description={`Affectez d'abord ${user.prenom} ${user.nom} à un chantier via le bouton « Chantiers », puis revenez ici pour donner ses permissions.`}
            />
          ) : filteredChantiers.length === 0 ? (
            <EmptyState title="Aucun chantier" description="Aucun chantier ne correspond à votre recherche." />
          ) : (
            <div className="max-h-[45vh] grid grid-cols-1 gap-2 overflow-y-auto pr-1 sm:grid-cols-2">
              {filteredChantiers.map((c) => (
                <button
                  key={c.id}
                  onClick={() => setChantier(c)}
                  className="flex items-center gap-3 rounded-xl border border-line bg-app px-3 py-3 text-left transition hover:border-accent hover:bg-surface"
                >
                  <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-accent to-accent-soft text-white">
                    <FaBuilding className="h-5 w-5" />
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-bold text-ink">{c.nom}</p>
                    <p className="truncate text-xs text-muted">{chantierStatutLabel(c.statut)}</p>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div>
          <div className="mb-3 flex flex-wrap items-center gap-2 rounded-xl border border-line bg-app px-3 py-2.5">
            <FaBuilding className="h-4 w-4 text-accent" />
            <p className="min-w-0 flex-1 truncate text-sm font-bold text-ink">{chantier.nom}</p>
            <button onClick={() => setChantier(null)} className="inline-flex items-center gap-1 text-xs font-semibold text-accent hover:underline">
              <FaArrowLeft className="h-3 w-3" /> Changer de chantier
            </button>
          </div>
          <div className="mb-3 rounded-xl border border-line bg-app px-3 py-2.5 text-xs">
            <p className="mb-1 font-bold text-ink">Comment ça marche</p>
            <p className="text-body"><span className="font-semibold text-success">Accorder</span> : force la permission <span className="font-semibold">sur CE chantier</span> en plus de son profil.</p>
            <p className="text-body"><span className="font-semibold text-danger">Refuser</span> : retire la permission <span className="font-semibold">sur CE chantier</span> malgré son profil (prioritaire).</p>
            <p className="text-body"><span className="font-semibold text-ink">Par défaut</span> : retire l'exception → il suit son profil sur CE chantier.</p>
          </div>
          <div className="max-h-[45vh] space-y-1 overflow-y-auto pr-1">
            {exceptionsQuery.isLoading && <FullSpinner label="Chargement…" />}
            {exceptionsQuery.isError && <ErrorState message={getApiErrorMessage(exceptionsQuery.error)} />}
            {!exceptionsQuery.isLoading && !exceptionsQuery.isError && grouped.length === 0 && (
              <EmptyState title="Aucune permission" description="Le référentiel des permissions est vide." />
            )}
            {grouped.map(([module, list]) => (
              <div key={module} className="rounded-xl border border-line p-3">
                <p className="mb-2 text-xs font-bold uppercase tracking-wide text-muted">{module}</p>
                <div className="space-y-1.5">
                  {list.map((p) => {
                    const state = stateFor(p.id);
                    return (
                      <div key={p.id} className="flex flex-wrap items-center justify-between gap-2 rounded-lg px-2 py-1.5 transition hover:bg-app">
                        <div className="min-w-0">
                          <p className="truncate text-sm font-medium text-ink">{p.nom}</p>
                          <p className="text-[11px] text-muted">{p.nomPermission}</p>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <button
                            onClick={() => toggle.mutate({ permissionId: p.id, type: state === "ACCORDER" ? null : "ACCORDER" })}
                            disabled={toggle.isPending}
                            className={cn("rounded-full border px-2.5 py-1 text-xs font-semibold transition", state === "ACCORDER" ? "border-success bg-success text-white" : "border-success/40 text-success hover:bg-success-light")}
                          >
                            {state === "ACCORDER" ? <><FaCheck className="mr-1 inline h-3 w-3" />Accordé</> : "Accorder"}
                          </button>
                          <button
                            onClick={() => toggle.mutate({ permissionId: p.id, type: state === "REFUSER" ? null : "REFUSER" })}
                            disabled={toggle.isPending}
                            className={cn("rounded-full border px-2.5 py-1 text-xs font-semibold transition", state === "REFUSER" ? "border-danger bg-danger text-white" : "border-danger/40 text-danger hover:bg-danger-light")}
                          >
                            {state === "REFUSER" ? "Refusé" : "Refuser"}
                          </button>
                          {state && (
                            <button onClick={() => toggle.mutate({ permissionId: p.id, type: null })} title="Par défaut : l'utilisateur suit son profil sur CE chantier" className="rounded-full border border-line px-2 py-1 text-xs font-semibold text-muted hover:bg-app hover:text-ink">Par défaut</button>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </Modal>
  );
}

// ============ UserDetail ============

export function UserDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const numId = Number(id);
  const query = useQuery({ queryKey: ["user", numId], queryFn: () => getUser(numId), enabled: !!numId });
  const permsQuery = useQuery({ queryKey: ["user-permissions", numId], queryFn: () => getUserPermissions(numId), enabled: !!numId });

  if (query.isLoading) return <FullSpinner label="Chargement…" />;
  if (query.isError) return <ErrorState message={getApiErrorMessage(query.error)} />;
  const u = query.data;
  if (!u) return <EmptyState title="Utilisateur introuvable" action={<Button onClick={() => navigate("/utilisateurs")}>Retour</Button>} />;
  const exceptions = permsQuery.data ?? [];

  return (
    <div>
      <button onClick={() => navigate("/utilisateurs")} className="mb-4 inline-flex items-center gap-1.5 text-sm font-semibold text-muted hover:text-ink"><FaArrowLeft className="h-4 w-4" /> Retour aux utilisateurs</button>
      <Card className="p-6">
        <div className="flex items-center gap-4">
          <Avatar prenom={u.prenom} nom={u.nom} gradient="from-blue-500 to-indigo-600" size={64} />
          <div>
            <h1 className="text-2xl font-black text-ink">{u.prenom} {u.nom}</h1>
            <p className="text-sm text-muted">{u.email}</p>
          </div>
        </div>
        <div className="mt-6"><InfoGrid>
          <InfoItem label="Email" value={u.email} />
          <InfoItem label="Téléphone" value={u.telephone || "-"} />
          <InfoItem label="Profil" value={<Badge tone="blue">{u.profilNom}</Badge>} />
          <InfoItem label="Créé le" value={formatDate(u.dateCreation)} />
        </InfoGrid></div>
        <div className="mt-6 border-t border-line pt-4">
          <p className="mb-2 text-sm font-bold text-ink">Permissions individuelles ({exceptions.length})</p>
          {exceptions.length === 0 ? (
            <p className="text-sm text-muted">Aucune exception individuelle. L'utilisateur suit son profil.</p>
          ) : (
            <div className="flex flex-wrap gap-1.5">
              {exceptions.map((e) => (
                <span key={e.id} className="rounded-full bg-app px-2.5 py-1 text-xs font-medium text-body">
                  {e.nomPermission} · <span className={e.type === "ACCORDER" ? "font-semibold text-success" : "font-semibold text-danger"}>{e.type === "ACCORDER" ? "ACCORDÉ" : "REFUSÉ"}</span>
                </span>
              ))}
            </div>
          )}
        </div>
      </Card>
    </div>
  );
}

// ============ ProfilDetail ============

export function ProfilDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const numId = Number(id);
  const queryClient = useQueryClient();
  const [q, setQ] = useState("");
  const [moduleFilter, setModuleFilter] = useState("");

  const profilQuery = useQuery({ queryKey: ["profil", numId], queryFn: () => getProfil(numId), enabled: !!numId });
  const permsQuery = useQuery({ queryKey: ["permissions"], queryFn: () => getPermissions({ page: 0, size: 100 }) });
  const profilPermsQuery = useQuery({ queryKey: ["profil-permissions", numId], queryFn: () => getProfilPermissions(numId), enabled: !!numId });
  const usersQuery = useQuery({ queryKey: ["users-all"], queryFn: () => getUsers({ page: 0, size: 200 }) });

  const granted = profilPermsQuery.data ?? [];
  const grantedIds = new Set(granted.map((pp) => pp.permissionId));
  const perms = permsQuery.data?.content ?? [];

  const grantMutation = useMutation({
    mutationFn: (permissionId: number) => grantProfilPermission(numId, permissionId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["profil-permissions", numId] }),
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });
  const revokeMutation = useMutation({
    mutationFn: (permissionId: number) => revokeProfilPermission(numId, permissionId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["profil-permissions", numId] }),
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });
  const grantAllMutation = useMutation({
    mutationFn: () => Promise.all(
      perms.filter((p) => !grantedIds.has(p.id)).map((p) => grantProfilPermission(numId, p.id))
    ),
    onSuccess: () => {
      toast.success("Toutes les permissions ont été accordées.");
      queryClient.invalidateQueries({ queryKey: ["profil-permissions", numId] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });
  const revokeAllMutation = useMutation({
    mutationFn: () => Promise.all(
      perms.filter((p) => grantedIds.has(p.id)).map((p) => revokeProfilPermission(numId, p.id))
    ),
    onSuccess: () => {
      toast.success("Toutes les permissions ont été retirées.");
      queryClient.invalidateQueries({ queryKey: ["profil-permissions", numId] });
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  if (profilQuery.isLoading) return <FullSpinner label="Chargement…" />;
  if (profilQuery.isError) return <ErrorState message={getApiErrorMessage(profilQuery.error)} />;

  const profil = profilQuery.data;
  if (!profil) return <EmptyState title="Profil introuvable" action={<Button onClick={() => navigate("/habilitations")}>Retour</Button>} />;

  const isAdmin = profil.nom === IS_ADMIN;
  const grantedCount = isAdmin ? perms.length : grantedIds.size;
  const modules = [...new Set(perms.map((p) => p.module))];
  const filteredPerms = perms.filter((p) => {
    const haystack = `${p.nom} ${p.nomPermission}`.toLowerCase();
    return haystack.includes(q.toLowerCase()) && (!moduleFilter || p.module === moduleFilter);
  });
  const visibleModules = [...new Set(filteredPerms.map((p) => p.module))];
  const nbUsers = (usersQuery.data?.content ?? []).filter((u) => u.profilNom === profil.nom).length;
  const pct = perms.length ? Math.round((grantedCount / perms.length) * 100) : 0;

  return (
    <div>
      <button onClick={() => navigate("/habilitations")} className="mb-4 inline-flex items-center gap-1.5 text-sm font-semibold text-muted hover:text-ink"><FaArrowLeft className="h-4 w-4" /> Retour aux habilitations</button>
      <Card className="p-6">
        <div className="flex items-center gap-4">
          <span className="flex h-14 w-14 items-center justify-center rounded-xl bg-gradient-to-br from-accent to-accent-soft text-white"><FaShieldHalved className="h-6 w-6" /></span>
          <div>
            <h1 className="text-2xl font-black text-ink">{profil.nom}</h1>
            {profil.description && <p className="text-sm text-muted">{profil.description}</p>}
          </div>
          <div className="ml-auto flex flex-col items-end gap-2">
            <Badge tone={isAdmin ? "blue" : "slate"} dot>{isAdmin ? "Système" : "Standard"}</Badge>
          </div>
        </div>
        <div className="mt-6"><InfoGrid>
          <InfoItem label="Permissions" value={isAdmin ? `Toutes (${perms.length})` : `${grantedCount}/${perms.length}`} />
          <InfoItem label="Utilisateurs" value={String(nbUsers)} />
          <InfoItem label="Créé le" value={formatDate(profil.dateCreation)} />
          <InfoItem label="Modifié le" value={formatDate(profil.dateModification)} />
        </InfoGrid></div>
      </Card>

      <Card className="mt-4 p-4">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="font-bold text-ink">Permissions accordées</h2>
            <p className="text-xs text-muted">{isAdmin ? "Le profil ADMINISTRATEUR dispose de toutes les permissions par défaut." : "Accordez ou retirez les permissions de ce profil."}</p>
          </div>
          <div className="flex items-center gap-2">
            {!isAdmin && perms.length > 0 && <Progress value={pct} tone="blue" className="w-24" />}
            <Pill tone="blue">{grantedCount} / {perms.length}</Pill>
          </div>
        </div>

        {!isAdmin && (
          <div className="mb-4 flex flex-wrap items-center gap-3">
            <div className="min-w-[200px] flex-1"><SearchBox value={q} onChange={setQ} placeholder="Rechercher une permission…" /></div>
            <SelectInput value={moduleFilter} onChange={(e) => setModuleFilter(e.target.value)} className="w-auto">
              <option value="">Tous les modules</option>
              {modules.map((m) => <option key={m} value={m}>{m}</option>)}
            </SelectInput>
            <Button variant="secondary" icon={<FaCheck className="h-4 w-4" />} loading={grantAllMutation.isPending} onClick={() => grantAllMutation.mutate()}>Tout accorder</Button>
            <Button variant="ghost" icon={<FaTrashCan className="h-4 w-4" />} loading={revokeAllMutation.isPending} onClick={() => revokeAllMutation.mutate()}>Tout retirer</Button>
          </div>
        )}

        {permsQuery.isLoading ? (
          <FullSpinner />
        ) : permsQuery.isError ? (
          <ErrorState message={getApiErrorMessage(permsQuery.error)} />
        ) : perms.length === 0 ? (
          <EmptyState title="Aucune permission" description="Le référentiel des permissions est vide." />
        ) : filteredPerms.length === 0 ? (
          <EmptyState title="Aucune permission" description="Aucune permission ne correspond à votre recherche." />
        ) : (
          <div className="space-y-3">
            {visibleModules.map((module) => (
              <div key={module} className="rounded-xl border border-line p-3">
                <p className="mb-2 text-xs font-bold uppercase tracking-wide text-muted">{module}</p>
                <div className="grid grid-cols-1 gap-1.5 md:grid-cols-2">
                  {filteredPerms.filter((p) => p.module === module).map((p) => {
                    const has = isAdmin || grantedIds.has(p.id);
                    return (
                      <div key={p.id} className="flex items-center justify-between gap-2 rounded-lg border border-line px-3 py-2">
                        <div className="min-w-0">
                          <p className="truncate text-sm font-medium text-ink">{p.nom}</p>
                          <p className="text-[11px] text-muted">{p.nomPermission}</p>
                        </div>
                        <button
                          onClick={() => (has ? revokeMutation.mutate(p.id) : grantMutation.mutate(p.id))}
                          disabled={isAdmin}
                          className={cn(
                            "flex h-8 w-8 shrink-0 items-center justify-center rounded-full border transition",
                            has ? "border-success bg-success text-white" : "border-line text-muted hover:border-success hover:text-success",
                            isAdmin && "cursor-default opacity-70",
                          )}
                          title={has ? "Retirer la permission" : "Accorder la permission"}
                        >
                          <FaCheck className="h-4 w-4" />
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}

// ============ Habilitations ============

export function Habilitations() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [tab, setTab] = useState("Profils");
  const [module, setModule] = useState("");
  const [editing, setEditing] = useState<ProfilResume | null>(null);
  const [creating, setCreating] = useState(false);
  const [toDelete, setToDelete] = useState<ProfilResume | null>(null);

  const profilsQuery = useQuery({ queryKey: ["profils"], queryFn: () => getProfils({ page: 0, size: 100 }) });
  const permsQuery = useQuery({ queryKey: ["permissions"], queryFn: () => getPermissions({ page: 0, size: 100 }) });
  const usersQuery = useQuery({ queryKey: ["users-all"], queryFn: () => getUsers({ page: 0, size: 200 }) });

  const profils = profilsQuery.data?.content ?? [];
  const perms = permsQuery.data?.content ?? [];
  const users = usersQuery.data?.content ?? [];

  // permissions par profil
  const profilPermsQuery = useQuery({
    queryKey: ["profil-permissions-map", profils.map((p) => p.id).join(",")],
    queryFn: async () => {
      const entries = await Promise.all(profils.map(async (p) => [p.id, await getProfilPermissions(p.id)] as const));
      return Object.fromEntries(entries) as Record<number, ProfilPermission[]>;
    },
    enabled: profils.length > 0,
  });
  const profilPermsMap = profilPermsQuery.data ?? {};

  const grantMutation = useMutation({
    mutationFn: ({ profilId, permissionId }: { profilId: number; permissionId: number }) =>
      grantProfilPermission(profilId, permissionId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["profil-permissions-map"] }),
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });
  const revokeMutation = useMutation({
    mutationFn: ({ profilId, permissionId }: { profilId: number; permissionId: number }) =>
      revokeProfilPermission(profilId, permissionId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["profil-permissions-map"] }),
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const togglePerm = (profil: ProfilResume, permissionId: number) => {
    if (profil.nom === IS_ADMIN) return;
    const has = (profilPermsMap[profil.id] ?? []).some((pp) => pp.permissionId === permissionId);
    if (has) revokeMutation.mutate({ profilId: profil.id, permissionId });
    else grantMutation.mutate({ profilId: profil.id, permissionId });
  };

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteProfil(id),
    onSuccess: () => { toast.success("Profil supprimé."); queryClient.invalidateQueries({ queryKey: ["profils"] }); },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  });

  const filteredPerms = perms.filter((p) => !module || p.module === module);
  const modules = useMemo(() => [...new Set(perms.map((p) => p.module))], [perms]);

  const profUsers = (profilNom: string) => users.filter((u) => u.profilNom === profilNom).length;

  return (
    <div>
      <PageHeader title="Habilitations" subtitle="Gérez les profils, permissions et accès des utilisateurs." />

      <Card className="mb-4 p-2"><Tabs tabs={["Profils", "Permissions", "Utilisateurs"].map((t) => ({ key: t, label: t }))} active={tab} onChange={setTab} layoutId="habTabs" /></Card>

      {profilsQuery.isLoading || permsQuery.isLoading ? (
        <FullSpinner label="Chargement des habilitations…" />
      ) : profilsQuery.isError || permsQuery.isError ? (
        <ErrorState message="Impossible de charger les habilitations." />
      ) : (
        <>
          {tab === "Profils" && (
            <div>
              <div className="mb-4 flex justify-end"><Button icon={<FaPlus className="h-4 w-4" />} onClick={() => setCreating(true)}>Nouveau profil</Button></div>
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
                {profils.map((p) => {
                  // ADMINISTRATEUR = TOUTES les permissions du référentiel par
                  // défaut (DroitsService, aucune ligne profil_permission en base).
                  const permIds =
                    p.nom === IS_ADMIN
                      ? perms.map((perm) => perm.id)
                      : (profilPermsMap[p.id] ?? []).map((pp) => pp.permissionId);
                  const pct = perms.length ? Math.round((permIds.length / perms.length) * 100) : 0;
                  return (
                    <Card key={p.id} className="flex flex-col p-5">
                      <div className="flex items-start gap-3">
                        <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-accent to-accent-soft text-white"><FaShieldHalved className="h-5 w-5" /></span>
                        <div className="flex-1"><button onClick={() => navigate(`/habilitations/profil/${p.id}`)} className="font-bold text-ink hover:text-accent">{p.nom}</button><p className="text-xs text-muted">{p.nom === IS_ADMIN ? "Accès complet (profil système), toutes les permissions" : "Profil standard"}</p></div>
                      </div>
                      <div className="mt-3 flex flex-wrap gap-1.5">
                        <Pill tone="blue">{p.nom === IS_ADMIN ? `Toutes (${perms.length}/${perms.length})` : `${permIds.length}/${perms.length} perms`}</Pill>
                        <Pill tone="slate">{profUsers(p.nom)} utilisateurs</Pill>
                      </div>
                      <div className="mt-3"><div className="mb-1 flex justify-between text-xs"><span className="text-muted">Couverture</span><span className="font-semibold text-ink">{pct}%</span></div><div className="h-1.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-white/10"><div className="h-full rounded-full bg-accent" style={{ width: `${pct}%` }} /></div></div>
                      <div className="mt-4 flex gap-1 border-t border-line pt-3">
                        <Button variant="ghost" className="flex-1 px-2 py-1.5 text-xs" icon={<FaPen className="h-3.5 w-3.5" />} onClick={() => setEditing(p)}>Modifier</Button>
                        {p.nom !== IS_ADMIN && <button onClick={() => setToDelete(p)} className="rounded-lg p-2 text-muted hover:bg-danger-light hover:text-danger"><FaTrashCan className="h-4 w-4" /></button>}
                      </div>
                    </Card>
                  );
                })}
              </div>
            </div>
          )}

          {tab === "Permissions" && (
            <Card className="p-4">
              <div className="mb-4"><SelectInput value={module} onChange={(e) => setModule(e.target.value)} className="max-w-xs"><option value="">Tous les modules</option>{modules.map((m) => <option key={m}>{m}</option>)}</SelectInput></div>
              <div className="overflow-x-auto">
                <table className="w-full min-w-[680px] text-sm">
                  <thead><tr className="border-b border-line text-left text-xs uppercase text-muted">
                    <th className="px-3 py-3 font-semibold">Permission</th>
                    {profils.map((p) => <th key={p.id} className="px-3 py-3 text-center font-semibold">{p.nom}</th>)}
                  </tr></thead>
                  <tbody className="divide-y divide-line">
                    {filteredPerms.map((perm) => (
                      <tr key={perm.id} className="hover:bg-app/50">
                        <td className="px-3 py-2"><span className="text-xs text-muted">{perm.module}</span><p className="text-sm font-medium text-ink">{perm.nom}</p></td>
                        {profils.map((p) => {
                          const has = p.nom === IS_ADMIN || (profilPermsMap[p.id] ?? []).some((pp) => pp.permissionId === perm.id);
                          return (
                            <td key={p.id} className="px-3 py-2 text-center">
                              <button onClick={() => togglePerm(p, perm.id)} disabled={p.nom === IS_ADMIN} className={cn("mx-auto flex h-7 w-7 items-center justify-center rounded-full border transition", has ? "border-success bg-success text-white" : "border-success/40 text-success hover:bg-success-light", p.nom === IS_ADMIN && "cursor-default opacity-70")}>{has ? <FaCheck className="h-3.5 w-3.5" /> : <FaPlus className="h-3.5 w-3.5" />}</button>
                            </td>
                          );
                        })}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </Card>
          )}

          {tab === "Utilisateurs" && (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
              {usersQuery.isLoading ? <FullSpinner /> : users.map((u) => (
                <Card key={u.id} className="p-4">
                  <div className="flex items-center gap-3">
                    <Avatar prenom={u.prenom} nom={u.nom} gradient="from-blue-500 to-indigo-600" size={40} />
                    <div className="min-w-0 flex-1"><p className="truncate text-sm font-bold text-ink">{u.prenom} {u.nom}</p><p className="truncate text-xs text-muted">· {u.profilNom}</p></div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </>
      )}

      {(editing || creating) && <ProfilFormModal profil={editing} onClose={() => { setEditing(null); setCreating(false); }} onCreate={async (data) => { try { const created = await createProfil(data); toast.success("Profil créé."); navigate(`/habilitations/profil/${created.id}`); } catch (err) { toast.error(getApiErrorMessage(err)); } }} onSave={async (id, data) => { try { await updateProfil(id, data); toast.success("Profil modifié."); navigate(`/habilitations/profil/${id}`); } catch (err) { toast.error(getApiErrorMessage(err)); } }} />}
      <ConfirmModal open={!!toDelete} onClose={() => setToDelete(null)} onConfirm={() => { if (toDelete) deleteMutation.mutate(toDelete.id); setToDelete(null); }} message={`Supprimer le profil « ${toDelete?.nom} » ?`} />
    </div>
  );
}

function ProfilFormModal({
  profil,
  onClose,
  onCreate,
  onSave,
}: {
  profil: ProfilResume | null;
  onClose: () => void;
  onCreate: (data: { nom: string; description?: string }) => Promise<void>;
  onSave: (id: number, data: { nom: string; description?: string }) => Promise<void>;
}) {
  const [nom, setNom] = useState(profil?.nom ?? "");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);

  const save = async () => {
    if (!nom) return;
    setSaving(true);
    try {
      if (profil) {
        await onSave(profil.id, { nom, description: description || undefined });
      } else {
        await onCreate({ nom, description: description || undefined });
      }
      onClose();
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal open onClose={onClose} size="md" title={profil ? "Modifier le profil" : "Nouveau profil"} footer={<><Button variant="secondary" onClick={onClose}>Annuler</Button><Button loading={saving} onClick={save} disabled={!nom}>{profil ? "Enregistrer" : "Créer"}</Button></>}>
      <div className="space-y-4">
        <Field label="Nom" required><TextInput value={nom} onChange={(e) => setNom(e.target.value)} /></Field>
        <Field label="Description"><TextInput value={description} onChange={(e) => setDescription(e.target.value)} /></Field>
        <p className="rounded-lg bg-app px-3 py-2 text-xs text-muted">Les permissions du profil se gèrent dans l'onglet « Permissions » (matrice).</p>
      </div>
    </Modal>
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
