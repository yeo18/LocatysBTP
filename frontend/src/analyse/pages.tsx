import { useEffect, useMemo, useState } from "react";
import { FaArrowLeft, FaBuilding, FaCloudSun, FaCrosshairs, FaLocationDot, FaPen, FaPlay, FaRotate, FaShieldHalved } from "react-icons/fa6";
import { toast } from "react-toastify";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { analyserSite, geocoder } from "../analyse/api";
import { confirmerLocalisation, getChantier, getChantiers } from "../chantiers/api";
import type {
  AnalyseSiteResponse,
  ChantierResponse,
  ChantierResume,
  GeocodageResultatResponse,
  SectionEnvironnementResponse,
  SectionMeteoResponse,
} from "../core/api/types";
import { getApiErrorMessage } from "../core/api/axios";
import { useApp } from "../core/store/AppProvider";
import { AnalyseMap, type LatLng } from "../analyse/components/analyseMap";
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Field,
  FullSpinner,
  PageHeader,
  TextArea,
  TextInput,
} from "../core/components/ui";
import { cn } from "../core/utils/cn";
import {
  formatDateTime,
  formatJourRelatif,
  formatNombre,
  isNombreValide,
} from "../core/lib/format";

// Codes de condition OpenWeather (fallback quand la description textuelle
// fournie par l'API est absente).
function meteoLabel(code: number): string {
  if (code >= 200 && code < 300) return "Orage";
  if (code >= 300 && code < 400) return "Bruine";
  if (code >= 500 && code < 600) return "Pluie";
  if (code >= 600 && code < 700) return "Neige";
  if (code >= 700 && code < 800) return "Brouillard";
  if (code === 800) return "Ciel dégagé";
  if (code === 801) return "Légèrement nuageux";
  if (code === 802) return "Partiellement nuageux";
  if (code === 803 || code === 804) return "Nuageux";
  return "Indéterminé";
}

function directionVentLabel(deg: number): string {
  const dirs = ["N", "NE", "E", "SE", "S", "SO", "O", "NO"];
  return dirs[Math.round(((deg % 360) / 45)) % 8];
}

const AQI_TONE = { 1: "emerald", 2: "cyan", 3: "amber", 4: "violet", 5: "rose" } as const;

function SectionMeteo({ data }: { data: SectionMeteoResponse }) {
  if (!data.actuel) {
    return <SectionCard icon={<FaCloudSun className="h-4 w-4" />} title="Météo" statut={data.statut} message={data.message} />;
  }
  const a = data.actuel;
  const jours = data.quotidiennes;
  const conditionActuelle = a.description || meteoLabel(a.codeMeteo);

  // Fiabilité des données : part des mesures fournies par la source
  const mesures = [
    a.temperature, a.temperatureRessentie, a.humidite, a.pression,
    a.precipitations, a.vent, a.rafales, a.couvertureNuageuse, a.visibilite,
  ];
  const mesuresValides = mesures.filter(isNombreValide).length;
  const fiable = mesuresValides / mesures.length;
  const fiabilite =
    fiable >= 1 ? { label: "Données complètes", tone: "emerald" as const }
    : fiable >= 0.6 ? { label: "Données partielles", tone: "amber" as const }
    : { label: "Données limitées", tone: "rose" as const };

  const direction = isNombreValide(a.directionVent) && a.directionVent > 0
    ? `${formatNombre(a.vent)} km/h ${directionVentLabel(a.directionVent)}`
    : `${formatNombre(a.vent)} km/h`;

  const q = data.qualiteAir;
  const aqiTone = q && isNombreValide(q.aqi) ? (AQI_TONE[q.aqi as keyof typeof AQI_TONE] ?? "slate") : "slate";
  const polluants = q
    ? [
        { label: "PM2.5", v: q.pm25 },
        { label: "PM10", v: q.pm10 },
        { label: "O3", v: q.o3 },
        { label: "NO2", v: q.no2 },
        { label: "SO2", v: q.so2 },
        { label: "CO", v: q.co },
      ]
    : [];

  return (
    <SectionCard icon={<FaCloudSun className="h-4 w-4" />} title="Météo" statut={data.statut} message={data.message}>
      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <p className="text-sm font-semibold text-ink">{conditionActuelle} actuellement</p>
        <span className="inline-flex items-center gap-2 text-xs text-muted">
          Source : <span className="font-semibold text-body">OpenWeather</span>
          <span aria-hidden>·</span>
          Mis à jour : <span className="font-semibold text-body">{formatDateTime(a.heureMiseAJour)}</span>
          <span aria-hidden>·</span>
          <Badge tone={fiabilite.tone} dot>{fiabilite.label}</Badge>
        </span>
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-6">
        {[
          { label: "Température", value: formatNombre(a.temperature, { unite: "°C" }) },
          { label: "Ressenti", value: formatNombre(a.temperatureRessentie, { unite: "°C" }) },
          { label: "Humidité", value: formatNombre(a.humidite, { unite: "%" }) },
          { label: "Pression", value: formatNombre(a.pression, { unite: "hPa" }) },
          { label: "Pluie actuelle", value: formatNombre(a.precipitations, { unite: "mm", decimals: 1 }) },
          { label: "Vent", value: direction },
        ].map((m) => (
          <div key={m.label} className="rounded-xl bg-app p-3 text-center">
            <p className="text-xs font-medium text-muted">{m.label}</p>
            <p className="mt-0.5 text-lg font-black text-ink">{m.value}</p>
          </div>
        ))}
      </div>
      <div className="mt-3 flex flex-wrap gap-x-5 gap-y-1 text-xs text-muted">
        {isNombreValide(a.neige) && a.neige > 0 && <span>Neige : {formatNombre(a.neige, { unite: "mm", decimals: 1 })}</span>}
        <span>Rafales : {formatNombre(a.rafales, { unite: "km/h" })}</span>
        {isNombreValide(a.couvertureNuageuse) && a.couvertureNuageuse > 0 && <span>Nuages : {formatNombre(a.couvertureNuageuse, { unite: "%" })}</span>}
        {isNombreValide(a.visibilite) && a.visibilite > 0 && (
          <span>Visibilité : {a.visibilite >= 1000 ? `${(a.visibilite / 1000).toFixed(1)} km` : `${Math.round(a.visibilite)} m`}</span>
        )}
        {fiable < 1 && <span className="text-warning">Données manquantes : {mesures.length - mesuresValides} sur {mesures.length}</span>}
      </div>

      {q ? (
        <div className="mt-4 rounded-xl border border-line p-3">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <p className="text-sm font-semibold text-ink">Qualité de l'air</p>
            <Badge tone={aqiTone} dot>
              {isNombreValide(q.aqi) && q.aqi > 0 ? `Indice ${q.aqi}/5 · ${q.libelle || "-"}` : "Indisponible"}
            </Badge>
          </div>
          <p className="mt-1 text-xs text-muted">Mis à jour : {formatDateTime(q.heureMiseAJour)}</p>
          <div className="mt-2 grid grid-cols-3 gap-2 sm:grid-cols-6">
            {polluants.map((p) => (
              <div key={p.label} className="rounded-lg bg-app px-2 py-1.5 text-center">
                <p className="text-[11px] font-medium text-muted">{p.label}</p>
                <p className="text-sm font-bold text-ink">{formatNombre(p.v, { unite: "µg/m³" })}</p>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <p className="mt-4 text-xs text-muted">Qualité de l'air : <span className="text-body">non disponible pour cette position</span>.</p>
      )}

      {jours.length > 0 && (
        <div className="mt-4">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted">Prévisions OpenWeather sur {jours.length} jours</p>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-line text-left text-xs uppercase tracking-wide text-muted">
                  <th className="px-2 py-2 font-semibold">Jour</th>
                  <th className="px-2 py-2 font-semibold">T° min / max</th>
                  <th className="px-2 py-2 font-semibold">Humidité</th>
                  <th className="px-2 py-2 font-semibold">Précipitations prévues</th>
                  <th className="px-2 py-2 font-semibold">Risque</th>
                  <th className="px-2 py-2 font-semibold">Vent</th>
                  <th className="px-2 py-2 font-semibold">Nuages</th>
                  <th className="px-2 py-2 font-semibold">Temps</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {jours.map((j, i) => (
                  <tr key={j.date}>
                    <td className="px-2 py-2 font-semibold text-ink">{formatJourRelatif(j.date, i)}</td>
                    <td className="px-2 py-2 text-body">
                      {formatNombre(j.tempMin, { unite: "°" })} / {formatNombre(j.tempMax, { unite: "°" })}
                    </td>
                    <td className="px-2 py-2 text-body">{formatNombre(j.humiditeMax, { unite: "%" })}</td>
                    <td className="px-2 py-2 text-body">{formatNombre(j.precipitations, { unite: "mm", decimals: 1 })}</td>
                    <td className="px-2 py-2 text-body">{formatNombre(j.probaPrecipitations, { unite: "%" })}</td>
                    <td className="px-2 py-2 text-body">{formatNombre(j.ventMax, { unite: "km/h" })}</td>
                    <td className="px-2 py-2 text-body">{formatNombre(j.couvertureNuageuse, { unite: "%" })}</td>
                    <td className="px-2 py-2 text-body">{j.description || meteoLabel(j.codeMeteo)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </SectionCard>
  );
}

const STATUT_LABEL: Record<string, string> = {
  DISPONIBLE: "Données disponibles",
  INDISPONIBLE: "Aucune donnée",
  ERREUR: "Source indisponible",
};

function StatutBadge({ statut }: { statut: string }) {
  const tone = statut === "DISPONIBLE" ? "emerald" : statut === "INDISPONIBLE" ? "slate" : "rose";
  return <Badge tone={tone}>{STATUT_LABEL[statut] ?? statut}</Badge>;
}

function SectionCard({
  icon,
  title,
  statut,
  message,
  children,
}: {
  icon: React.ReactNode;
  title: string;
  statut: string;
  message?: string | null;
  children?: React.ReactNode;
}) {
  return (
    <Card className="p-5">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-app text-accent">{icon}</div>
          <h3 className="font-bold text-ink">{title}</h3>
        </div>
        <StatutBadge statut={statut} />
      </div>
      {message && statut !== "DISPONIBLE" && (
        <p className="mb-3 rounded-lg bg-app px-3 py-2 text-sm text-body">{message}</p>
      )}
      {children}
    </Card>
  );
}

function SectionEnvironnement({ data }: { data: SectionEnvironnementResponse }) {
  return (
    <SectionCard
      icon={<FaBuilding className="h-4 w-4" />}
      title={`Environnement (${data.elements.length} point(s) d'intérêt)`}
      statut={data.statut}
      message={data.message}
    >
      <div className="flex flex-wrap gap-2">
        {data.elements.slice(0, 20).map((e, i) => (
          <span key={i} className="inline-flex items-center gap-1.5 rounded-full bg-app px-3 py-1.5 text-xs font-semibold text-body">
            {e.type}
            {e.nom ? ` · ${e.nom}` : ""}
            <span className="text-muted">({formatNombre(e.distanceKm, { decimals: 1, unite: "km" })})</span>
          </span>
        ))}
      </div>
    </SectionCard>
  );
}

function SyntheseCard({ analyse }: { analyse: AnalyseSiteResponse }) {
  const s = analyse.synthese;
  const tone =
    s.niveauVigilance === "ELEVEE"
      ? "rose"
      : s.niveauVigilance === "MODEREE"
        ? "amber"
        : s.niveauVigilance === "LEGERE"
          ? "blue"
          : "emerald";
  return (
    <Card className="p-5 ring-2 ring-accent/10">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-app text-accent">
            <FaShieldHalved className="h-4 w-4" />
          </div>
          <h3 className="font-bold text-ink">Synthèse de l'analyse</h3>
        </div>
        <Badge tone={tone}>Vigilance {s.niveauVigilance.toLowerCase()}</Badge>
      </div>

      <p className="mb-4 text-sm leading-relaxed text-body">{s.resume}</p>

      <div className="grid gap-4 md:grid-cols-3">
        {s.pointsFavorables.length > 0 && (
          <div>
            <p className="mb-2 text-xs font-bold uppercase tracking-wide text-success">Points favorables</p>
            <ul className="space-y-1.5">
              {s.pointsFavorables.map((p, i) => (
                <li key={i} className="flex gap-2 text-sm text-body">
                  <span className="mt-0.5 text-success">•</span> {p}
                </li>
              ))}
            </ul>
          </div>
        )}
        {s.pointsAttention.length > 0 && (
          <div>
            <p className="mb-2 text-xs font-bold uppercase tracking-wide text-warning">Points d'attention</p>
            <ul className="space-y-1.5">
              {s.pointsAttention.map((p, i) => (
                <li key={i} className="flex gap-2 text-sm text-body">
                  <span className="mt-0.5 text-warning">•</span> {p}
                </li>
              ))}
            </ul>
          </div>
        )}
        {s.recommandations.length > 0 && (
          <div>
            <p className="mb-2 text-xs font-bold uppercase tracking-wide text-accent">Recommandations</p>
            <ul className="space-y-1.5">
              {s.recommandations.map((p, i) => (
                <li key={i} className="flex gap-2 text-sm text-body">
                  <span className="mt-0.5 text-accent">•</span> {p}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      <p className="mt-5 border-t border-line pt-3 text-[11px] text-muted">
        Cette synthèse est générée automatiquement à partir des données publiques collectées. Elle aide à la préparation du chantier
        mais ne remplace pas une expertise technique, géotechnique ou structurelle.
      </p>
    </Card>
  );
}

type Step = "select" | "locate" | "map" | "ready" | "results";

const STEPS: { key: Step; label: string }[] = [
  { key: "select", label: "1. Chantier" },
  { key: "locate", label: "2. Localisation" },
  { key: "map", label: "3. Carte" },
  { key: "ready", label: "4. Analyse" },
];

function Stepper({ current }: { current: Step }) {
  const idx = STEPS.findIndex((s) => s.key === current);
  return (
    <div className="mb-6 flex items-center gap-2 overflow-x-auto no-scrollbar">
      {STEPS.map((s, i) => {
        const done = i < idx;
        const active = i === idx;
        return (
          <div key={s.key} className="flex shrink-0 items-center gap-2">
            <span
              className={cn(
                "flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold",
                done && "bg-success text-white",
                active && "bg-accent text-white",
                !done && !active && "bg-app text-muted"
              )}
            >
              {done ? "✓" : i + 1}
            </span>
            <span className={cn("whitespace-nowrap text-xs font-semibold", active ? "text-ink" : "text-muted")}>
              {s.label}
            </span>
            {i < STEPS.length - 1 && <div className="h-px w-6 bg-line" />}
          </div>
        );
      })}
    </div>
  );
}

export default function AnalyseSitePage() {
  const { hasPermOnChantier } = useApp();
  const queryClient = useQueryClient();

  const [chantierId, setChantierId] = useState<number | null>(null);
  const [step, setStep] = useState<Step>("select");
  const [description, setDescription] = useState("");
  const [geoResults, setGeoResults] = useState<GeocodageResultatResponse[]>([]);
  const [geoLoading, setGeoLoading] = useState(false);
  const [position, setPosition] = useState<LatLng | null>(null);
  const [positionFromGeo, setPositionFromGeo] = useState(false);
  const [locDescription, setLocDescription] = useState("");
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [analyseLoading, setAnalyseLoading] = useState(false);
  const [analyse, setAnalyse] = useState<AnalyseSiteResponse | null>(null);

  const chantiersQuery = useQuery({
    queryKey: ["chantiers", { page: 0, size: 100 }],
    queryFn: () => getChantiers({ page: 0, size: 100 }),
  });

  const chantierQuery = useQuery({
    queryKey: ["chantier", chantierId],
    queryFn: () => getChantier(chantierId!),
    enabled: chantierId != null,
  });

  const chantier: ChantierResponse | null = chantierQuery.data ?? null;
  const confirmed = chantier != null && chantier.latitude != null && chantier.longitude != null;
  const chantiers: ChantierResume[] = useMemo(() => chantiersQuery.data?.content ?? [], [chantiersQuery.data]);

  useEffect(() => {
    if (!chantier) return;
    if (confirmed) {
      setStep("ready");
      setPosition({ lat: Number(chantier.latitude), lng: Number(chantier.longitude) });
    } else {
      setStep("locate");
    }
  }, [chantier?.id, confirmed]);

  if (chantiersQuery.isLoading) return <FullSpinner label="Chargement des chantiers…" />;
  if (chantiersQuery.isError) return <ErrorState message={getApiErrorMessage(chantiersQuery.error)} />;

  const selectChantier = (id: number) => {
    setAnalyse(null);
    setGeoResults([]);
    setDescription("");
    setPosition(null);
    setLocDescription("");
    setPositionFromGeo(false);
    setChantierId(id);
    setStep("locate");
  };

  const onLocaliser = async () => {
    if (!description.trim()) {
      toast.error("Décrivez d'abord le lieu (adresse, quartier, ville).");
      return;
    }
    setGeoLoading(true);
    try {
      const results = await geocoder(description);
      setGeoResults(results);
      if (results.length === 0) toast.info("Aucune position trouvée pour cette description.");
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setGeoLoading(false);
    }
  };

  const onPickGeocode = (r: GeocodageResultatResponse) => {
    setPosition({ lat: Number(r.latitude), lng: Number(r.longitude) });
    setLocDescription(r.libelle);
    setPositionFromGeo(true);
    setStep("map");
  };

  const onMoveMarker = (p: LatLng) => {
    setPosition(p);
    setPositionFromGeo(false);
  };

  const onConfirmer = async () => {
    if (!chantier || !position) return;
    setConfirmLoading(true);
    try {
      await confirmerLocalisation(chantier.id, {
        latitude: position.lat,
        longitude: position.lng,
        adresseGeocodee: locDescription || description || undefined,
        origineCoordonnees: positionFromGeo ? "GEOCODAGE" : "CARTE",
        fiabiliteCoordonnees: positionFromGeo ? "APPROXIMATIVE" : "PRECISE",
      });
      toast.success("Position confirmée pour ce chantier.");
      await queryClient.invalidateQueries({ queryKey: ["chantier", chantier.id] });
      setStep("ready");
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setConfirmLoading(false);
    }
  };

  const onLancerAnalyse = async () => {
    if (!chantier) return;
    setAnalyseLoading(true);
    try {
      const result = await analyserSite(chantier.id);
      setAnalyse(result);
      setStep("results");
    } catch (err) {
      toast.error(getApiErrorMessage(err));
    } finally {
      setAnalyseLoading(false);
    }
  };

  const onReLocaliser = () => {
    setPosition(null);
    setGeoResults([]);
    setDescription("");
    setLocDescription("");
    setPositionFromGeo(false);
    setStep("locate");
  };

  const canAnalyser = chantier != null && confirmed && hasPermOnChantier("CHANTIER_LIRE", chantier.id);
  const canConfirmer = chantier != null && hasPermOnChantier("CHANTIER_MODIFIER", chantier.id);

  return (
    <div>
      <PageHeader
        title="Analyse du site"
        subtitle="Localisez précisément un chantier puis analysez son environnement (météo, points d'intérêt)."
      />

      <Stepper current={step} />

      {step === "select" && (
        <Card className="p-5">
          <h3 className="mb-1 font-bold text-ink">Choisir un chantier</h3>
          <p className="mb-4 text-sm text-muted">Le chantier doit être accessible en lecture (profil ou exception scopée).</p>
          {chantiers.length === 0 ? (
            <EmptyState title="Aucun chantier" description="Créez d'abord un chantier pour pouvoir l'analyser." />
          ) : (
            <ul className="space-y-2">
              {chantiers.map((c) => (
                <li key={c.id}>
                  <button
                    onClick={() => selectChantier(c.id)}
                    className="flex w-full items-center justify-between gap-3 rounded-xl border border-line px-4 py-3 text-left transition hover:border-accent/40 hover:bg-app"
                  >
                    <span className="flex items-center gap-3">
                      <FaBuilding className="h-4 w-4 text-muted" />
                      <span>
                        <span className="block text-sm font-bold text-ink">{c.nom}</span>
                        <span className="block text-xs text-muted">{c.adresseSaisie || "Adresse non renseignée"}</span>
                      </span>
                    </span>
                    <Badge tone={c.statut === "EN_COURS" ? "blue" : c.statut === "TERMINE" ? "emerald" : "slate"}>
                      {c.statut === "PREVU"
                        ? "Planifié"
                        : c.statut === "EN_COURS"
                          ? "En cours"
                          : c.statut === "TERMINE"
                            ? "Terminé"
                            : "Annulé"}
                    </Badge>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </Card>
      )}

      {step === "locate" && chantier && (
        <Card className="p-5">
          <div className="mb-4 flex items-center justify-between gap-3">
            <div>
              <h3 className="font-bold text-ink">Localiser « {chantier.nom} »</h3>
              <p className="text-sm text-muted">Décrivez le lieu : la recherche s'appuie sur OpenStreetMap (Nominatim).</p>
            </div>
            <Button variant="secondary" onClick={() => setStep("select")} icon={<FaArrowLeft className="h-4 w-4" />}>
              Autre chantier
            </Button>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <Field
              label="Description du lieu"
              hint="Format précis : « quartier, ville » ou « repère, quartier, ville ». Ex : Plateau, Abidjan ; CHU de Cocody ; mairie de Treichville ; Angré, Abidjan"
              className="flex-1"
            >
              <TextArea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Adresse, quartier, ville, repère…"
              />
            </Field>
            <Button loading={geoLoading} onClick={onLocaliser} icon={<FaCrosshairs className="h-4 w-4" />}>
              Localiser
            </Button>
          </div>

          {geoResults.length > 0 && (
            <div className="mt-5">
              <p className="mb-2 text-xs font-bold uppercase tracking-wide text-muted">
                {geoResults.length} position(s) proposée(s)
              </p>
              <ul className="space-y-2">
                {geoResults.map((r, i) => (
                  <li key={i}>
                    <button
                      onClick={() => onPickGeocode(r)}
                      className="flex w-full items-start justify-between gap-3 rounded-xl border border-line px-4 py-3 text-left transition hover:border-accent/40 hover:bg-app"
                    >
                      <span className="flex items-start gap-3">
                        <FaLocationDot className="mt-0.5 h-4 w-4 shrink-0 text-accent" />
                        <span>
                          <span className="block text-sm font-semibold text-ink">{r.libelle}</span>
                          <span className="block text-xs text-muted">
                            {[r.quartier, r.commune, r.ville, r.pays].filter(Boolean).join(" · ") || r.type}
                          </span>
                        </span>
                      </span>
                      <span className="shrink-0 text-xs font-semibold text-muted">
                        {Number(r.latitude).toFixed(5)}, {Number(r.longitude).toFixed(5)}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </Card>
      )}

      {step === "map" && chantier && (
        <Card className="p-5">
          <div className="mb-4 flex items-center justify-between gap-3">
            <div>
              <h3 className="font-bold text-ink">Vérifier la position sur la carte</h3>
              <p className="text-sm text-muted">Déplacez le marqueur (glisser ou clic) puis confirmez la position.</p>
            </div>
            <Button variant="secondary" onClick={() => setStep("locate")} icon={<FaArrowLeft className="h-4 w-4" />}>
              Revenir
            </Button>
          </div>

          <AnalyseMap position={position} onPositionChange={onMoveMarker} height="h-[380px]" />

          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <Field label="Latitude">
              <TextInput
                value={position?.lat.toFixed(7) ?? ""}
                onChange={(e) => {
                  const v = parseFloat(e.target.value);
                  if (!isNaN(v)) {
                    setPosition((p) => ({ lat: v, lng: p?.lng ?? 0 }));
                    setPositionFromGeo(false);
                  }
                }}
              />
            </Field>
            <Field label="Longitude">
              <TextInput
                value={position?.lng.toFixed(7) ?? ""}
                onChange={(e) => {
                  const v = parseFloat(e.target.value);
                  if (!isNaN(v)) {
                    setPosition((p) => ({ lat: p?.lat ?? 0, lng: v }));
                    setPositionFromGeo(false);
                  }
                }}
              />
            </Field>
          </div>

          {locDescription && <p className="mt-3 text-xs text-muted">Description : {locDescription}</p>}

          <div className="mt-5 flex flex-wrap items-center justify-end gap-2">
            {!canConfirmer && (
              <p className="mr-auto text-xs text-danger">
                Permission CHANTIER_MODIFIER requise pour confirmer la position.
              </p>
            )}
            <Button variant="secondary" onClick={onReLocaliser} icon={<FaRotate className="h-4 w-4" />}>
              Refaire la recherche
            </Button>
            <Button loading={confirmLoading} disabled={!canConfirmer} onClick={onConfirmer} icon={<FaLocationDot className="h-4 w-4" />}>
              Confirmer la position
            </Button>
          </div>
        </Card>
      )}

      {step === "ready" && chantier && confirmed && (
        <div className="space-y-5">
          <Card className="p-5">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div>
                <h3 className="font-bold text-ink">Position confirmée : {chantier.nom}</h3>
                <p className="mt-1 text-sm text-muted">
                  {isNombreValide(Number(chantier.latitude)) && isNombreValide(Number(chantier.longitude))
                    ? `Latitude ${formatNombre(chantier.latitude, { decimals: 7 })} · Longitude ${formatNombre(chantier.longitude, { decimals: 7 })}`
                    : "Position confirmée (coordonnées à venir)"}
                  {chantier.fiabiliteCoordonnees === "PRECISE"
                    ? " · Précision fine (carte)"
                    : " · Position approximative (géocodage)"}
                </p>
                {chantier.adresseGeocodee && (
                  <p className="mt-0.5 text-sm text-body">Description : {chantier.adresseGeocodee}</p>
                )}
              </div>
              <Button variant="secondary" onClick={() => setStep("map")} icon={<FaPen className="h-4 w-4" />}>
                Modifier la position
              </Button>
            </div>
            <div className="mt-4">
              <AnalyseMap position={position} readonly height="h-[280px]" />
            </div>
          </Card>

          <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-line bg-card p-5">
            <div className="min-w-0">
              <p className="font-bold text-ink">Lancer l'analyse</p>
              <p className="text-sm text-muted">
                Appels parallèles : météo (5 jours), points d'intérêt.
              </p>
            </div>
            {!canAnalyser && <p className="text-xs text-danger">Accès en lecture à ce chantier requis.</p>}
            <Button loading={analyseLoading} disabled={!canAnalyser} onClick={onLancerAnalyse} icon={<FaPlay className="h-4 w-4" />}>
              Analyser le site
            </Button>
          </div>
        </div>
      )}

      {step === "results" && analyse && (
        <div className="space-y-5">
          <SyntheseCard analyse={analyse} />
          <SectionMeteo data={analyse.meteo} />
          <SectionEnvironnement data={analyse.environnement} />
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setStep("ready")} icon={<FaArrowLeft className="h-4 w-4" />}>
              Revenir à la position
            </Button>
            <Button onClick={onLancerAnalyse} loading={analyseLoading} icon={<FaRotate className="h-4 w-4" />}>
              Relancer l'analyse
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
