// ============ Formatting & helpers ============

export function moneyFull(n: number): string {
  return n.toLocaleString("fr-FR");
}

export function moneyCompact(n: number): string {
  if (n === 0) return "0";
  const abs = Math.abs(n);
  if (abs >= 1_000_000_000)
    return (n / 1_000_000_000).toFixed(2).replace(".", ",").replace(/,00$/, "") + " Md";
  if (abs >= 1_000_000) return Math.round(n / 1_000_000) + " M";
  if (abs >= 1_000) return Math.round(n / 1_000) + " k";
  return String(n);
}

/**
 * Parse une date sans décalage de fuseau horaire : une chaîne « YYYY-MM-DD »
 * (LocalDate) est interprétée comme une date locale pure, pas comme un
 * instant UTC (ce qui affichait un jour en moins sur les fuseaux négatifs).
 */
function parseDate(iso: string): Date | null {
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(iso);
  if (m) {
    return new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]));
  }
  const d = new Date(iso);
  return isNaN(d.getTime()) ? null : d;
}

export function formatDate(iso?: string): string {
  if (!iso) return "-";
  const d = parseDate(iso);
  if (!d) return "-";
  return d.toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" });
}

export function formatDateShort(iso?: string): string {
  if (!iso) return "-";
  const d = parseDate(iso);
  if (!d) return "-";
  return d.toLocaleDateString("fr-FR", { day: "2-digit", month: "2-digit", year: "numeric" });
}

/** Date courte avec jour de la semaine (ex : « jeu. 20/08 »). */
export function formatJourCourt(iso?: string): string {
  if (!iso) return "-";
  const d = parseDate(iso);
  if (!d) return "-";
  return d.toLocaleDateString("fr-FR", { weekday: "short", day: "2-digit", month: "2-digit" });
}

/**
 * Libellé relatif d'un jour de prévision : « Aujourd'hui », « Demain », « J+2 »…
 * avec la date en rappel (ex : « Demain · ven. 21/08 »).
 */
export function formatJourRelatif(iso?: string, index?: number): string {
  if (!iso) return "-";
  const d = parseDate(iso);
  if (!d) return "-";
  const label = index === 0 ? "Aujourd'hui" : index === 1 ? "Demain" : `J+${index ?? ""}`;
  return `${label} · ${d.toLocaleDateString("fr-FR", { weekday: "short", day: "2-digit", month: "short" })}`;
}

/** Date + heure (ISO LocalDateTime ou date seule), sans décalage de fuseau (ex : « 20/08/2026 07:15 »). */
export function formatDateTime(iso?: string | null): string {
  if (!iso) return "-";
  const [datePart, heurePart] = iso.split("T");
  if (!/^\d{4}-\d{2}-\d{2}$/.test(datePart)) {
    const d = parseDate(iso);
    if (!d) return "-";
    return d.toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" });
  }
  const jour = parseDate(datePart);
  if (!jour) return "-";
  const dateAff = jour.toLocaleDateString("fr-FR", { day: "2-digit", month: "short", year: "numeric" });
  const heure = heurePart?.slice(0, 5);
  return heure && /^\d{2}:\d{2}$/.test(heure) ? `${dateAff} ${heure}` : dateAff;
}

/** Vrai si la valeur est un nombre fini utilisable (exclut NaN, Infinity, undefined, null). */
export function isNombreValide(v: unknown): v is number {
  return typeof v === "number" && Number.isFinite(v);
}

/**
 * Formate un nombre en le protégeant des valeurs invalides : NaN, Infinity,
 * undefined et null deviennent « - » (jamais de « NaN »/« undefined » à l'écran).
 */
export function formatNombre(v: unknown, opts?: { unite?: string; decimals?: number }): string {
  if (!isNombreValide(v)) return "-";
  const unite = opts?.unite ?? "";
  const dec = opts?.decimals ?? 0;
  const arrondi = Math.round(v * 10 ** dec) / 10 ** dec;
  const nombre = dec > 0 ? arrondi.toFixed(dec) : String(Math.round(v));
  return unite ? `${nombre} ${unite}` : nombre;
}

export function initials(prenom?: string, nom?: string): string {
  return ((prenom?.[0] ?? "") + (nom?.[0] ?? "")).toUpperCase() || "?";
}

/** Duration like "1a 2m 15j" */
export function durationLabel(start?: string, end?: string): string {
  if (!start || !end) return "-";
  const a = new Date(start).getTime();
  const b = new Date(end).getTime();
  if (isNaN(a) || isNaN(b) || b < a) return "-";
  let months =
    (new Date(end).getFullYear() - new Date(start).getFullYear()) * 12 +
    (new Date(end).getMonth() - new Date(start).getMonth());
  if (new Date(end).getDate() >= new Date(start).getDate()) months += 1;
  if (months < 0) months = 0;
  const years = Math.floor(months / 12);
  const remMonths = months % 12;
  const parts: string[] = [];
  if (years) parts.push(`${years}a`);
  if (remMonths) parts.push(`${remMonths}m`);
  if (!parts.length) parts.push("< 1m");
  return parts.join(" ");
}

export function clampPct(n: number): number {
  return Math.max(0, Math.min(100, Math.round(n)));
}
