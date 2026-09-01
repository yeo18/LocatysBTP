import { useState } from "react";
import { FaCalendarDays, FaKey, FaEnvelope, FaPhone, FaFloppyDisk, FaUser } from "react-icons/fa6";
import { toast } from "react-toastify";
import { useApp } from "../core/store/AppProvider";
import { getApiErrorMessage } from "../core/api/axios";
import { changerMotDePasse } from "../auth/api";
import { updateUser } from "../utilisateurs/api";
import { Badge, Button, Card, Field, PageHeader, TextInput } from "../core/components/ui";
import { formatDate } from "../core/lib/format";
import { isValidEmail, isValidName, isValidPhone } from "../core/lib/validation";

export default function MonProfil() {
  const { user, updateUser: syncUser } = useApp();
  const [form, setForm] = useState({
    prenom: user?.prenom ?? "",
    nom: user?.nom ?? "",
    email: user?.email ?? "",
    telephone: user?.telephone ?? "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const [pwd, setPwd] = useState({ ancienMotDePasse: "", nouveauMotDePasse: "", confirmation: "" });
  const [savingPwd, setSavingPwd] = useState(false);

  if (!user) return null;

  const save = async () => {
    const ers: Record<string, string> = {};
    if (!isValidName(form.prenom)) ers.prenom = "Le prénom est obligatoire.";
    if (!isValidName(form.nom)) ers.nom = "Le nom est obligatoire.";
    if (!isValidEmail(form.email)) ers.email = "Adresse email invalide.";
    if (!isValidPhone(form.telephone)) ers.telephone = "Format de téléphone invalide (chiffres, +, espaces, . ou - uniquement).";
    if (Object.keys(ers).length > 0) {
      setErrors(ers);
      toast.error("Veuillez corriger les champs signalés.");
      return;
    }
    setErrors({});
    setSaving(true);
    try {
      const updated = await updateUser(user.id, {
        nom: form.nom,
        prenom: form.prenom,
        email: form.email,
        telephone: form.telephone,
        profilId: user.profilId,
      });
      syncUser({
        ...user,
        ...form,
        profilId: Number(updated.profilId),
        profilNom: updated.profilNom,
        telephone: updated.telephone ?? form.telephone,
      });
      toast.success("Profil mis à jour avec succès.");
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const changePassword = async () => {
    if (pwd.nouveauMotDePasse !== pwd.confirmation) {
      toast.error("La confirmation ne correspond pas au nouveau mot de passe.");
      return;
    }
    if (pwd.nouveauMotDePasse.length < 8) {
      toast.error("Le nouveau mot de passe doit contenir au moins 8 caractères.");
      return;
    }
    setSavingPwd(true);
    try {
      await changerMotDePasse(pwd);
      toast.success("Mot de passe changé avec succès.");
      setPwd({ ancienMotDePasse: "", nouveauMotDePasse: "", confirmation: "" });
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setSavingPwd(false);
    }
  };

  const profileTone = user.role === "admin" ? "emerald" : "blue";

  return (
    <div>
      <PageHeader title="Mon profil" subtitle="Gérez vos informations personnelles et la sécurité de votre compte." />
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[340px_1fr]">
        {/* Identity card */}
        <Card className="flex flex-col items-center p-8 text-center">
          <div className="relative">
            <div className={`flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br ${user.avatar} text-3xl font-black text-white shadow-lg`}>
              {user.prenom[0]}{user.nom[0]}
            </div>
            <span className="absolute -bottom-1 -right-1 flex h-7 w-7 items-center justify-center rounded-full border-4 border-card bg-success text-white">
              <FaUser className="h-3.5 w-3.5" />
            </span>
          </div>
          <h2 className="mt-5 text-xl font-black text-ink">{user.prenom} {user.nom}</h2>
          <Badge tone={profileTone} dot className="mt-2">{user.profilNom || "-"}</Badge>

          <div className="mt-6 w-full space-y-3">
            <div className="flex items-center gap-3 rounded-xl bg-app px-4 py-3 text-left">
              <FaEnvelope className="h-4 w-4 shrink-0 text-muted" />
              <div className="min-w-0">
                <p className="text-xs text-muted">Email</p>
                <p className="truncate text-sm font-semibold text-ink">{user.email}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 rounded-xl bg-app px-4 py-3 text-left">
              <FaPhone className="h-4 w-4 shrink-0 text-muted" />
              <div className="min-w-0">
                <p className="text-xs text-muted">Téléphone</p>
                <p className="truncate text-sm font-semibold text-ink">{user.telephone || "-"}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 rounded-xl bg-app px-4 py-3 text-left">
              <FaCalendarDays className="h-4 w-4 shrink-0 text-muted" />
              <div className="min-w-0">
                <p className="text-xs text-muted">Compte créé</p>
                <p className="truncate text-sm font-semibold text-ink">{formatDate(user.dateCreation)}</p>
              </div>
            </div>
          </div>

          <p className="mt-6 text-xs font-semibold text-accent">LOCATYSBTP · Gestion de chantiers</p>
        </Card>

        {/* Single form : personal info + password */}
        <div className="space-y-6">
          <Card className="p-6">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-info-light text-info">
                <FaUser className="h-5 w-5" />
              </div>
              <div>
                <h3 className="text-base font-bold text-ink">Informations personnelles</h3>
                <p className="text-sm text-muted">Mettez à jour vos coordonnées.</p>
              </div>
            </div>

            <div className="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Prénom" required error={errors.prenom}><TextInput invalid={!!errors.prenom} value={form.prenom} onChange={(e) => setForm({ ...form, prenom: e.target.value })} /></Field>
              <Field label="Nom" required error={errors.nom}><TextInput invalid={!!errors.nom} value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} /></Field>
              <Field label="Email" required error={errors.email}><TextInput invalid={!!errors.email} type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></Field>
              <Field label="Téléphone" error={errors.telephone} hint="Ex. +225 07 12 34 56 78"><TextInput invalid={!!errors.telephone} value={form.telephone} onChange={(e) => setForm({ ...form, telephone: e.target.value })} /></Field>
            </div>

            <div className="mt-6 flex justify-end">
              <Button icon={<FaFloppyDisk className="h-4 w-4" />} loading={saving} onClick={save}>Enregistrer les modifications</Button>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-danger-light text-danger">
                <FaKey className="h-5 w-5" />
              </div>
              <div>
                <h3 className="text-base font-bold text-ink">Sécurité</h3>
                <p className="text-sm text-muted">Changer le mot de passe de votre compte.</p>
              </div>
            </div>

            <div className="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Ancien mot de passe" className="sm:col-span-2">
                <TextInput
                  type="password"
                  value={pwd.ancienMotDePasse}
                  onChange={(e) => setPwd({ ...pwd, ancienMotDePasse: e.target.value })}
                  placeholder="••••••••"
                  autoComplete="current-password"
                />
              </Field>
              <Field label="Nouveau mot de passe" hint="Au moins 8 caractères">
                <TextInput
                  type="password"
                  value={pwd.nouveauMotDePasse}
                  onChange={(e) => setPwd({ ...pwd, nouveauMotDePasse: e.target.value })}
                  placeholder="••••••••"
                  autoComplete="new-password"
                />
              </Field>
              <Field label="Confirmation du nouveau mot de passe">
                <TextInput
                  type="password"
                  value={pwd.confirmation}
                  onChange={(e) => setPwd({ ...pwd, confirmation: e.target.value })}
                  placeholder="••••••••"
                  autoComplete="new-password"
                />
              </Field>
            </div>

            <div className="mt-6 flex justify-end">
              <Button icon={<FaKey className="h-4 w-4" />} loading={savingPwd} onClick={changePassword} variant="danger">
                Mettre à jour le mot de passe
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
