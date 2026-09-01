import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { FaCircleCheck, FaEnvelope, FaPhone, FaTriangleExclamation, FaUser } from "react-icons/fa6";
import { toast } from "react-toastify";
import { useApp } from "../core/store/AppProvider";
import { Button, Field } from "../core/components/ui";
import { isValidEmail, isValidName, isValidPhone } from "../core/lib/validation";
import {
  AuthCard,
  AuthHero,
  AuthShell,
  BlueprintBackdrop,
  ChampIcone,
  ChampMotDePasse,
  MessageErreur,
} from "./components";

export function Login() {
  const { login } = useApp();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [motDePasse, setMotDePasse] = useState("");
  const [erreur, setErreur] = useState("");
  const [chargement, setChargement] = useState(false);

  const soumettre = async (e: React.FormEvent) => {
    e.preventDefault();
    setErreur("");
    if (!isValidEmail(email)) {
      setErreur("Veuillez renseigner une adresse e-mail valide.");
      return;
    }
    if (!motDePasse) {
      setErreur("Veuillez renseigner votre mot de passe.");
      return;
    }
    setChargement(true);
    const resultat = await login(email, motDePasse);
    setChargement(false);
    if (resultat.ok) navigate("/dashboard");
    else setErreur(resultat.error ?? "Connexion impossible.");
  };

  const motDePasseOublie = () => {
    toast.info("Contactez votre administrateur pour réinitialiser votre mot de passe.");
  };

  return (
    <AuthShell>
      <AuthHero />
      <main className="flex items-center justify-center lg:justify-end">
        <AuthCard
          refCode="LOC-ACC-001"
          title="Bienvenue sur LOCATYSBTP"
          subtitle="Connectez-vous pour accéder à votre espace."
        >
          {erreur && <MessageErreur message={erreur} />}

          <form onSubmit={soumettre} className="space-y-4">
            <Field label="Adresse e-mail" required>
              <ChampIcone
                icon={<FaEnvelope className="h-4 w-4" />}
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="vous@entreprise.com"
                required
                autoComplete="email"
              />
            </Field>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="text-sm font-semibold text-ink">
                  Mot de passe <span className="text-danger">*</span>
                </label>
                <button
                  type="button"
                  onClick={motDePasseOublie}
                  className="text-xs font-semibold text-accent transition hover:text-accent-soft"
                >
                  Mot de passe oublié ?
                </button>
              </div>
              <ChampMotDePasse value={motDePasse} onChange={(e) => setMotDePasse(e.target.value)} />
            </div>

            <Button type="submit" loading={chargement} className="w-full">
              Se connecter
              <span aria-hidden>→</span>
            </Button>
          </form>

          <div className="mt-6">
            <Link
              to="/register"
              className="flex w-full items-center justify-center gap-2 rounded-lg border border-line bg-surface px-4 py-2.5 text-sm font-semibold text-ink transition hover:border-accent/40 hover:bg-app"
            >
              Créer un compte
            </Link>
          </div>
        </AuthCard>
      </main>
    </AuthShell>
  );
}

export function Register() {
  const { register, login } = useApp();
  const navigate = useNavigate();
  const [formulaire, setFormulaire] = useState({
    prenom: "",
    nom: "",
    email: "",
    telephone: "",
    motDePasse: "",
    confirmation: "",
  });
  const [erreurs, setErreurs] = useState<Record<string, string>>({});
  const [erreur, setErreur] = useState("");
  const [chargement, setChargement] = useState(false);

  const mettreAJourChamp = (champ: keyof typeof formulaire) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setFormulaire((f) => ({ ...f, [champ]: e.target.value }));

  const soumettre = async (e: React.FormEvent) => {
    e.preventDefault();
    setErreur("");
    const nouvellesErreurs: Record<string, string> = {};
    if (!isValidName(formulaire.prenom)) nouvellesErreurs.prenom = "Le prénom est obligatoire.";
    if (!isValidName(formulaire.nom)) nouvellesErreurs.nom = "Le nom est obligatoire.";
    if (!isValidEmail(formulaire.email)) nouvellesErreurs.email = "Veuillez renseigner une adresse e-mail valide.";
    if (!isValidPhone(formulaire.telephone)) nouvellesErreurs.telephone = "Format de téléphone invalide.";
    if (formulaire.motDePasse.length < 8)
      nouvellesErreurs.motDePasse = "Le mot de passe doit contenir au moins 8 caractères.";
    if (formulaire.confirmation !== formulaire.motDePasse)
      nouvellesErreurs.confirmation = "Les mots de passe ne correspondent pas.";
    if (Object.keys(nouvellesErreurs).length > 0) {
      setErreurs(nouvellesErreurs);
      setErreur("Veuillez corriger les champs signalés.");
      return;
    }
    setErreurs({});
    setChargement(true);
    const resultat = await register({
      prenom: formulaire.prenom,
      nom: formulaire.nom,
      email: formulaire.email,
      telephone: formulaire.telephone,
      password: formulaire.motDePasse,
    });
    if (resultat.ok) {
      // Le backend ne renvoie pas de token à l'inscription, on enchaîne un login.
      const auto = await login(formulaire.email.trim(), formulaire.motDePasse);
      setChargement(false);
      if (auto.ok) {
        toast.success("Compte créé avec succès, bienvenue !");
        navigate("/dashboard");
      } else {
        toast.success("Compte créé, connectez-vous !");
        navigate("/login");
      }
    } else {
      setChargement(false);
      setErreur(resultat.error ?? "Inscription impossible.");
    }
  };

  return (
    <AuthShell>
      <AuthHero />
      <main className="flex items-center justify-center lg:justify-end">
        <AuthCard
          refCode="LOC-ACC-002"
          title="Créer votre compte"
          subtitle="Rejoignez LOCATYSBTP pour gérer efficacement vos chantiers et vos équipes."
        >
          {erreur && <MessageErreur message={erreur} />}

          <form onSubmit={soumettre} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Field label="Prénom" required error={erreurs.prenom}>
                <ChampIcone
                  icon={<FaUser className="h-4 w-4" />}
                  invalid={!!erreurs.prenom}
                  value={formulaire.prenom}
                  onChange={mettreAJourChamp("prenom")}
                  required
                  autoComplete="given-name"
                />
              </Field>
              <Field label="Nom" required error={erreurs.nom}>
                <ChampIcone
                  icon={<FaUser className="h-4 w-4" />}
                  invalid={!!erreurs.nom}
                  value={formulaire.nom}
                  onChange={mettreAJourChamp("nom")}
                  required
                  autoComplete="family-name"
                />
              </Field>
            </div>

            <Field label="Email professionnel" required error={erreurs.email}>
              <ChampIcone
                icon={<FaEnvelope className="h-4 w-4" />}
                type="email"
                invalid={!!erreurs.email}
                value={formulaire.email}
                onChange={mettreAJourChamp("email")}
                placeholder="vous@entreprise.com"
                required
                autoComplete="email"
              />
            </Field>

            <Field
              label="Téléphone"
              error={erreurs.telephone}
              hint="Optionnel, ex. 07 12 34 56 78"
            >
              <ChampIcone
                icon={<FaPhone className="h-4 w-4" />}
                invalid={!!erreurs.telephone}
                value={formulaire.telephone}
                onChange={mettreAJourChamp("telephone")}
                placeholder="+225 ..."
                autoComplete="tel"
              />
            </Field>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Mot de passe" required error={erreurs.motDePasse} hint="8 caractères minimum.">
                <ChampMotDePasse
                  value={formulaire.motDePasse}
                  onChange={mettreAJourChamp("motDePasse")}
                  invalid={!!erreurs.motDePasse}
                />
              </Field>

              <Field label="Confirmation" required error={erreurs.confirmation}>
                <ChampMotDePasse
                  value={formulaire.confirmation}
                  onChange={mettreAJourChamp("confirmation")}
                  invalid={!!erreurs.confirmation}
                  autoComplete="new-password"
                />
              </Field>
            </div>

            <Button type="submit" loading={chargement} className="w-full">
              Créer mon compte
              <span aria-hidden>→</span>
            </Button>
          </form>

          <div className="mt-6 flex items-center gap-3">
            <span className="h-px flex-1 bg-line" />
            <span className="text-xs font-semibold uppercase tracking-wide text-muted">
              Vous avez déjà un compte ?
            </span>
            <span className="h-px flex-1 bg-line" />
          </div>
          <Link
            to="/login"
            className="mt-4 flex w-full items-center justify-center gap-2 rounded-lg border border-line bg-surface px-4 py-2.5 text-sm font-semibold text-ink transition hover:border-accent/40 hover:bg-app"
          >
            Se connecter
          </Link>
        </AuthCard>
      </main>
    </AuthShell>
  );
}

export function AccessDenied() {
  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#0a1628] px-5">
      <BlueprintBackdrop />
      <motion.div
        initial={{ opacity: 0, y: 18 }}
        animate={{ opacity: 1, y: 0 }}
        className="relative w-full max-w-md rounded-3xl border border-line bg-card p-8 text-center shadow-card"
      >
        <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-2xl bg-danger-light text-danger ring-1 ring-danger/20">
          <FaTriangleExclamation className="h-8 w-8" />
        </div>
        <div className="mx-auto mb-2 flex items-center justify-center gap-2 text-[10px] font-bold uppercase tracking-[0.25em] text-muted">
          <span className="h-1.5 w-1.5 rounded-full bg-danger" />
          Référence LOC-ACC-403
        </div>
        <h1 className="text-2xl font-black text-ink">Accès refusé</h1>
        <p className="mt-2 text-sm text-muted">
          Vous n'avez pas les permissions requises pour accéder à cette section. Contactez un
          administrateur si vous pensez qu'il s'agit d'une erreur.
        </p>
        <Link
          to="/dashboard"
          className="mt-6 inline-flex items-center justify-center gap-2 rounded-lg bg-accent px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-accent-soft active:scale-[0.97]"
        >
          <FaCircleCheck className="h-4 w-4" />
          Retour au tableau de bord
        </Link>
      </motion.div>
    </div>
  );
}