import { Link } from "react-router-dom";
import { FaCalendarDays } from "react-icons/fa6";
import { cn } from "../utils/cn";
import type { Tache, Priorite } from "../lib/types";
import { Badge, EmptyState, PriorityBadge } from "./ui";
import { formatDate } from "../lib/format";
import { prioriteTone, toneDot, type Tone } from "../lib/styles";

const prioriteIconColor: Record<Priorite, string> = {
  Urgente: "bg-danger-light text-danger",
  Haute: "bg-warning-light text-warning",
  Moyenne: "bg-info-light text-info",
  Basse: "bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-300",
};

export function TaskMiniList({
  tasks,
  onSelect,
  emptyLabel = "Aucune tâche",
}: {
  tasks: Tache[];
  onSelect?: (t: Tache) => void;
  emptyLabel?: string;
}) {
  if (!tasks.length) return <EmptyState title={emptyLabel} description="Aucune tâche à afficher." />;
  return (
    <div className="divide-y divide-line">
      {tasks.map((t) => (
        <button
          key={t.id}
          onClick={() => onSelect?.(t)}
          className="flex w-full items-center gap-3 px-1 py-3 text-left transition hover:bg-app/60"
        >
          <span className={cn("flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-[11px] font-black", prioriteIconColor[t.priorite])}>
            <span className={cn("h-2 w-2 rounded-full", toneDot[prioriteTone(t.priorite)])} />
          </span>
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-semibold text-ink">{t.titre}</p>
            <p className="flex items-center gap-1 text-xs text-muted">
              <FaCalendarDays className="h-3 w-3" /> {formatDate(t.dateFin)}
            </p>
          </div>
          <PriorityBadge priority={t.priorite} />
        </button>
      ))}
    </div>
  );
}

export function CountBadge({ to, count, tone = "blue", label }: { to: string; count: number; tone?: Tone; label: string }) {
  return (
    <Link to={to} className="group flex items-center gap-2 rounded-full bg-white/15 px-3 py-1.5 text-xs font-semibold text-white backdrop-blur transition hover:bg-white/25">
      <span className={cn("h-2 w-2 rounded-full", toneDot[tone])} />
      {count} {label}
    </Link>
  );
}

export { Badge };
