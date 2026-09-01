import type {
  ChantierStatut,
  ChantierType,
  Priorite,
  TacheStatut,
} from "./types";

export type Tone =
  | "slate"
  | "blue"
  | "amber"
  | "emerald"
  | "rose"
  | "violet"
  | "indigo"
  | "cyan";

export const toneBadge: Record<Tone, string> = {
  slate: "bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300",
  blue: "bg-info-light text-info",
  amber: "bg-warning-light text-warning",
  emerald: "bg-success-light text-success",
  rose: "bg-danger-light text-danger",
  violet: "bg-violet-100 text-violet-600 dark:bg-violet-500/15 dark:text-violet-300",
  indigo: "bg-indigo-100 text-indigo-600 dark:bg-indigo-500/15 dark:text-indigo-300",
  cyan: "bg-cyan-100 text-cyan-600 dark:bg-cyan-500/15 dark:text-cyan-300",
};

export const toneDot: Record<Tone, string> = {
  slate: "bg-slate-400",
  blue: "bg-info",
  amber: "bg-warning",
  emerald: "bg-success",
  rose: "bg-danger",
  violet: "bg-violet-500",
  indigo: "bg-indigo-500",
  cyan: "bg-cyan-500",
};

export const toneBar: Record<Tone, string> = {
  slate: "bg-slate-400",
  blue: "bg-info",
  amber: "bg-warning",
  emerald: "bg-gradient-to-r from-emerald-400 to-emerald-500",
  rose: "bg-danger",
  violet: "bg-gradient-to-r from-violet-400 to-violet-500",
  indigo: "bg-gradient-to-r from-indigo-400 to-indigo-500",
  cyan: "bg-gradient-to-r from-cyan-400 to-cyan-500",
};

export function chantierStatutTone(s: ChantierStatut): Tone {
  switch (s) {
    case "En cours":
      return "blue";
    case "Planifié":
    case "En attente":
    case "En pause":
      return "amber";
    case "Terminé":
    case "Livré":
      return "emerald";
    case "Annulé":
      return "rose";
    default:
      return "slate";
  }
}

export const typeTone: Record<ChantierType, Tone> = {
  Résidentiel: "indigo",
  Commercial: "cyan",
  Industriel: "amber",
  Infrastructure: "violet",
  Rénovation: "emerald",
  "-": "slate",
};

export function prioriteTone(p: Priorite): Tone {
  switch (p) {
    case "Urgente":
      return "rose";
    case "Haute":
      return "amber";
    case "Moyenne":
      return "blue";
    case "Basse":
      return "slate";
    default:
      return "slate";
  }
}

export function tacheStatutTone(s: TacheStatut): Tone {
  switch (s) {
    case "En cours":
      return "blue";
    case "Validée":
      return "emerald";
    case "Bloquée":
    case "Refusée":
      return "rose";
    case "À faire":
      return "slate";
    default:
      return "slate";
  }
}

