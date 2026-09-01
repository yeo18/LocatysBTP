import {
  forwardRef,
  useEffect,
  useRef,
  useState,
  type ButtonHTMLAttributes,
  type InputHTMLAttributes,
  type ReactNode,
  type SelectHTMLAttributes,
  type TextareaHTMLAttributes,
} from "react";
import { AnimatePresence, motion } from "framer-motion";
import { FaChevronDown, FaInbox, FaMagnifyingGlass, FaSpinner, FaTriangleExclamation, FaXmark } from "react-icons/fa6";
import { cn } from "../utils/cn";
import { initials } from "../lib/format";
import { type Tone, toneBadge, toneDot, toneBar } from "../lib/styles";

// ============ Spinner ============
export function Spinner({ className }: { className?: string }) {
  return (
    <span
      className={cn(
        "inline-block h-5 w-5 animate-spin rounded-full border-2 border-current border-t-transparent",
        className
      )}
    />
  );
}

export function FullSpinner({ label = "Chargement…" }: { label?: string }) {
  return (
    <div className="flex h-[60vh] flex-col items-center justify-center gap-3 text-muted">
      <span className="h-9 w-9 animate-spin rounded-full border-[3px] border-amber-600 border-b-transparent" />
      <p className="text-sm font-medium">{label}</p>
    </div>
  );
}

// ============ Button ============
type Variant = "primary" | "secondary" | "danger" | "success" | "ghost";
const variants: Record<Variant, string> = {
  primary: "bg-accent text-white hover:bg-accent-soft shadow-sm",
  secondary:
    "bg-surface text-ink border border-line hover:bg-app",
  danger: "bg-danger text-white hover:opacity-90 shadow-sm",
  success: "bg-success text-white hover:opacity-90 shadow-sm",
  ghost: "text-body hover:bg-app hover:text-ink",
};

export const Button = forwardRef<
  HTMLButtonElement,
  ButtonHTMLAttributes<HTMLButtonElement> & {
    variant?: Variant;
    loading?: boolean;
    icon?: ReactNode;
  }
>(function Button(
  { variant = "primary", loading, icon, className, children, disabled, ...props },
  ref
) {
  return (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition-all",
        "focus:outline-none focus-visible:ring-2 focus-visible:ring-accent/40 focus-visible:ring-offset-2 focus-visible:ring-offset-app",
        "active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-50",
        variants[variant],
        className
      )}
      {...props}
    >
      {loading ? <FaSpinner className="h-4 w-4 animate-spin" /> : icon}
      {children}
    </button>
  );
});

export function IconButton({
  className,
  children,
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      className={cn(
        "inline-flex h-9 w-9 items-center justify-center rounded-lg text-muted transition-all hover:bg-app hover:text-ink focus:outline-none focus-visible:ring-2 focus-visible:ring-accent/40 active:scale-[0.95]",
        className
      )}
      {...props}
    >
      {children}
    </button>
  );
}

export function LinkButton({
  className,
  children,
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      className={cn(
        "inline-flex items-center gap-1.5 rounded-lg text-sm font-semibold text-accent transition-colors hover:text-accent-soft active:scale-[0.97]",
        className
      )}
      {...props}
    >
      {children}
    </button>
  );
}

// ============ Inputs ============
const fieldBase =
  "w-full rounded-lg border border-line bg-surface px-3.5 py-2.5 text-sm text-ink placeholder:text-muted transition focus:outline-none focus:border-accent focus:ring-2 focus:ring-accent/30 disabled:opacity-60";

export const TextInput = forwardRef<
  HTMLInputElement,
  InputHTMLAttributes<HTMLInputElement> & { invalid?: boolean }
>(function TextInput({ className, invalid, ...props }, ref) {
  return (
    <input
      ref={ref}
      className={cn(fieldBase, invalid && "border-danger focus:border-danger focus:ring-danger/30", className)}
      {...props}
    />
  );
});

export const SelectInput = forwardRef<
  HTMLSelectElement,
  SelectHTMLAttributes<HTMLSelectElement>
>(function SelectInput({ className, children, ...props }, ref) {
  return (
    <select ref={ref} className={cn(fieldBase, "cursor-pointer appearance-none pr-9", className)} {...props}>
      {children}
    </select>
  );
});

// ============ Select recherchable (combobox) ============
export function SearchSelect({
  value,
  onChange,
  options,
  placeholder = "Rechercher…",
  className,
}: {
  value: string;
  onChange: (v: string) => void;
  options: { value: string; label: string }[];
  placeholder?: string;
  className?: string;
}) {
  const [open, setOpen] = useState(false);
  const [q, setQ] = useState("");
  const wrapRef = useRef<HTMLDivElement>(null);

  const selected = options.find((o) => o.value === value);
  const filtered = q.trim()
    ? options.filter((o) => o.label.toLowerCase().includes(q.trim().toLowerCase()))
    : options;

  useEffect(() => {
    const onDoc = (e: MouseEvent) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", onDoc);
    return () => document.removeEventListener("mousedown", onDoc);
  }, []);

  return (
    <div ref={wrapRef} className={cn("relative", className)}>
      <input
        value={open ? q : selected?.label ?? ""}
        onChange={(e) => { setOpen(true); setQ(e.target.value); }}
        onFocus={() => { setOpen(true); setQ(""); }}
        placeholder={placeholder}
        className={cn(fieldBase, "pr-9")}
      />
<span className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-muted">
        {value && !open ? (
          <FaXmark className="h-4 w-4" />
        ) : (
          <FaChevronDown className="h-4 w-4" />
        )}
      </span>
      {open && (
        <div className="absolute z-20 mt-1 max-h-60 w-full overflow-y-auto rounded-lg border border-line bg-card py-1 shadow-lg">
          {filtered.length === 0 ? (
            <p className="px-3 py-2 text-sm text-muted">Aucun résultat</p>
          ) : (
            filtered.map((o) => (
              <button
                key={o.value}
                type="button"
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => { onChange(o.value); setOpen(false); setQ(""); }}
                className={cn("block w-full px-3 py-2 text-left text-sm transition hover:bg-app", o.value === value && "bg-app font-semibold text-accent")}
              >
                {o.label}
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export const TextArea = forwardRef<
  HTMLTextAreaElement,
  TextareaHTMLAttributes<HTMLTextAreaElement>
>(function TextArea({ className, ...props }, ref) {
  return <textarea ref={ref} className={cn(fieldBase, "min-h-[90px] resize-y", className)} {...props} />;
});

export function Field({
  label,
  children,
  hint,
  error,
  required,
  className,
}: {
  label?: string;
  children: ReactNode;
  hint?: string;
  error?: string;
  required?: boolean;
  className?: string;
}) {
  return (
    <label className={cn("block space-y-1.5", className)}>
      {label && (
        <span className="flex items-center gap-1 text-sm font-semibold text-ink">
          {label}
          {required && <span className="text-danger">*</span>}
        </span>
      )}
      {children}
      {hint && !error && <span className="block text-xs text-muted">{hint}</span>}
      {error && <span className="block text-xs font-medium text-danger">{error}</span>}
    </label>
  );
}

// ============ Card ============
export function Card({
  className,
  children,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn("rounded-2xl border border-line bg-card shadow-card", className)}
      {...props}
    >
      {children}
    </div>
  );
}

// ============ Badge ============
export function Badge({
  tone = "slate",
  children,
  dot,
  className,
}: {
  tone?: Tone;
  children: ReactNode;
  dot?: boolean;
  className?: string;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold",
        toneBadge[tone],
        className
      )}
    >
      {dot && <span className={cn("h-1.5 w-1.5 rounded-full", toneDot[tone])} />}
      {children}
    </span>
  );
}

export function PriorityBadge({ priority }: { priority: import("../lib/types").Priorite }) {
  const tone = ({ Basse: "slate", Moyenne: "blue", Haute: "amber", Urgente: "rose" } as const)[priority];
  return <Badge tone={tone} dot>{priority}</Badge>;
}

// ============ Progress ============
export function Progress({
  value,
  tone = "emerald",
  className,
  showLabel,
}: {
  value: number;
  tone?: Tone;
  className?: string;
  showLabel?: boolean;
}) {
  const v = Math.max(0, Math.min(100, Math.round(value)));
  return (
    <div className={cn("flex items-center gap-2", className)}>
      <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-white/10">
        <div
          className={cn("h-full rounded-full transition-all duration-500", toneBar[tone])}
          style={{ width: `${v}%` }}
        />
      </div>
      {showLabel && <span className="w-9 text-right text-xs font-bold text-ink">{v}%</span>}
    </div>
  );
}

// ============ Avatar ============
export function Avatar({
  prenom,
  nom,
  gradient = "from-slate-400 to-slate-500",
  size = 40,
}: {
  prenom?: string;
  nom?: string;
  gradient?: string;
  size?: number;
}) {
  return (
    <div
      className={cn("flex shrink-0 items-center justify-center rounded-full bg-gradient-to-br font-bold text-white", gradient)}
      style={{ width: size, height: size, fontSize: size * 0.36 }}
    >
      {initials(prenom, nom)}
    </div>
  );
}

// ============ Modal ============
export function Modal({
  open,
  onClose,
  title,
  subtitle,
  children,
  footer,
  size = "md",
}: {
  open: boolean;
  onClose: () => void;
  title?: string;
  subtitle?: string;
  children: ReactNode;
  footer?: ReactNode;
  size?: "sm" | "md" | "lg";
}) {
  useEffect(() => {
    if (open) {
      document.body.style.overflow = "hidden";
      return () => {
        document.body.style.overflow = "";
      };
    }
  }, [open]);

  const sizes = { sm: "max-w-md", md: "max-w-lg", lg: "max-w-2xl" };

  return (
    <AnimatePresence>
      {open && (
        <div className="fixed inset-0 z-[100] flex items-end justify-center p-0 sm:items-center sm:p-4">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm"
          />
          <motion.div
            initial={{ opacity: 0, y: 40, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 20, scale: 0.98 }}
            transition={{ type: "spring", damping: 26, stiffness: 300 }}
            className={cn(
              "relative z-10 max-h-[92vh] w-full overflow-hidden rounded-t-3xl bg-card shadow-card sm:rounded-2xl",
              sizes[size]
            )}
          >
            {(title || subtitle) && (
              <div className="flex items-start justify-between gap-4 border-b border-line px-6 py-4">
                <div>
                  {title && <h3 className="text-lg font-bold text-ink">{title}</h3>}
                  {subtitle && <p className="mt-0.5 text-sm text-muted">{subtitle}</p>}
                </div>
                <IconButton onClick={onClose} aria-label="Fermer">
                  <FaXmark className="h-5 w-5" />
                </IconButton>
              </div>
            )}
            <div className="max-h-[70vh] overflow-y-auto px-6 py-5">{children}</div>
            {footer && (
              <div className="flex justify-end gap-2 border-t border-line bg-app/40 px-6 py-4">
                {footer}
              </div>
            )}
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}

export function ConfirmModal({
  open,
  onClose,
  onConfirm,
  title = "Confirmer la suppression",
  message,
  confirmLabel = "Supprimer",
  loading,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title?: string;
  message: string;
  confirmLabel?: string;
  loading?: boolean;
}) {
  return (
    <Modal
      open={open}
      onClose={onClose}
      size="sm"
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>
            Annuler
          </Button>
          <Button variant="danger" loading={loading} onClick={onConfirm} icon={<FaTriangleExclamation className="h-4 w-4" />}>
            {confirmLabel}
          </Button>
        </>
      }
    >
      <div className="flex items-start gap-4">
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-danger-light text-danger">
          <FaTriangleExclamation className="h-5 w-5" />
        </div>
        <div>
          <h3 className="text-base font-bold text-ink">{title}</h3>
          <p className="mt-1 text-sm text-body">{message}</p>
        </div>
      </div>
    </Modal>
  );
}

// ============ Empty / Error ============
export function EmptyState({
  icon,
  title,
  description,
  action,
}: {
  icon?: ReactNode;
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-line px-6 py-14 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-app text-muted">
        {icon ?? <FaInbox className="h-6 w-6" />}
      </div>
      <div>
        <p className="font-semibold text-ink">{title}</p>
        {description && <p className="mt-1 text-sm text-muted">{description}</p>}
      </div>
      {action}
    </div>
  );
}

export function ErrorState({ message = "Une erreur est survenue." }: { message?: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-2xl border border-line bg-danger-light/40 px-6 py-14 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-danger-light text-danger">
        <FaTriangleExclamation className="h-6 w-6" />
      </div>
      <p className="font-semibold text-ink">{message}</p>
    </div>
  );
}

// ============ Page header ============
export function PageHeader({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children?: ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-2xl font-black tracking-tight text-ink sm:text-[28px]">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-muted">{subtitle}</p>}
      </div>
      {children && <div className="flex flex-wrap items-center gap-2">{children}</div>}
    </div>
  );
}

export function Pill({ children, tone = "blue" }: { children: ReactNode; tone?: Tone }) {
  return (
    <span className={cn("inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-sm font-semibold", toneBadge[tone])}>
      {children}
    </span>
  );
}

// ============ Search box ============
export function SearchBox({
  value,
  onChange,
  placeholder = "Rechercher…",
}: {
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
}) {
  return (
    <div className="relative w-full">
      <FaMagnifyingGlass className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
      <TextInput
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="pl-9"
      />
    </div>
  );
}

// ============ Tabs ============
export function Tabs({
  tabs,
  active,
  onChange,
  layoutId = "tabs",
}: {
  tabs: { key: string; label: ReactNode; count?: number }[];
  active: string;
  onChange: (k: string) => void;
  layoutId?: string;
}) {
  return (
    <div className="flex gap-1 overflow-x-auto border-b border-line no-scrollbar">
      {tabs.map((t) => {
        const isActive = active === t.key;
        return (
          <button
            key={t.key}
            onClick={() => onChange(t.key)}
            className={cn(
              "relative whitespace-nowrap px-4 py-3 text-sm font-semibold transition-colors",
              isActive ? "text-accent" : "text-muted hover:text-ink"
            )}
          >
            {t.label}
            {t.count !== undefined && (
              <span className={cn("ml-1.5 rounded-full px-1.5 py-0.5 text-[10px]", isActive ? "bg-accent/15 text-accent" : "bg-app text-muted")}>
                {t.count}
              </span>
            )}
            {isActive && (
              <motion.div
                layoutId={layoutId}
                className="absolute inset-x-0 -bottom-px h-0.5 rounded-full bg-accent"
                transition={{ type: "spring", damping: 28, stiffness: 400 }}
              />
            )}
          </button>
        );
      })}
    </div>
  );
}

// ============ Info grid ============
export function InfoGrid({ children }: { children: ReactNode }) {
  return <div className="grid grid-cols-1 gap-x-6 gap-y-4 sm:grid-cols-2">{children}</div>;
}

export function InfoItem({
  label,
  value,
  icon,
}: {
  label: string;
  value: ReactNode;
  icon?: ReactNode;
}) {
  return (
    <div className="space-y-1">
      <p className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-muted">
        {icon}
        {label}
      </p>
      <div className="text-sm font-semibold text-ink">{value || "-"}</div>
    </div>
  );
}

// ============ MiniMetric ============
export function MiniMetric({
  label,
  value,
  icon,
  tone = "blue",
}: {
  label: string;
  value: ReactNode;
  icon: ReactNode;
  tone?: Tone;
}) {
  return (
    <Card className="flex items-center gap-3 p-4">
      <div className={cn("flex h-10 w-10 shrink-0 items-center justify-center rounded-xl", toneBadge[tone])}>
        {icon}
      </div>
      <div className="min-w-0">
        <p className="truncate text-xs font-medium text-muted">{label}</p>
        <p className="text-xl font-black text-ink">{value}</p>
      </div>
    </Card>
  );
}

// ============ Responsive table ============
export function Table({
  head,
  rows,
  empty = "Aucune donnée",
  minW = 640,
}: {
  head: string[];
  rows: ReactNode[][];
  empty?: string;
  minW?: number;
}) {
  if (!rows.length) return <EmptyState title={empty} />;
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm" style={{ minWidth: minW }}>
        <thead>
          <tr className="border-b border-line text-left text-xs uppercase tracking-wide text-muted">
            {head.map((h, i) => (
              <th key={h} className={cn("px-3 py-3 font-semibold", i === head.length - 1 && "text-right")}>
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-line">
          {rows.map((r, i) => (
            <tr key={i} className="transition hover:bg-app/50">
              {r.map((c, j) => (
                <td key={j} className={cn("px-3 py-3 text-body", j === r.length - 1 && "text-right")}>
                  {c}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ============ Pagination ============
export function Pagination({
  page,
  pages,
  onPage,
}: {
  page: number;
  pages: number;
  onPage: (p: number) => void;
}) {
  if (pages <= 1) return null;
  const items: (number | string)[] = [];
  for (let n = 1; n <= pages; n++) {
    if (n === 1 || n === pages || Math.abs(n - page) <= 1) items.push(n);
    else if (items[items.length - 1] !== "…") items.push("…");
  }
  return (
    <div className="flex items-center justify-center gap-1">
      <Button variant="secondary" className="px-3 py-2" disabled={page === 1} onClick={() => onPage(page - 1)}>
        Préc.
      </Button>
      {items.map((it, i) =>
        it === "…" ? (
          <span key={`e${i}`} className="px-2 text-muted">
            …
          </span>
        ) : (
          <button
            key={it}
            onClick={() => onPage(it as number)}
            className={cn(
              "h-9 w-9 rounded-lg text-sm font-semibold transition",
              it === page ? "bg-accent text-white" : "text-body hover:bg-app"
            )}
          >
            {it}
          </button>
        )
      )}
      <Button variant="secondary" className="px-3 py-2" disabled={page === pages} onClick={() => onPage(page + 1)}>
        Suiv.
      </Button>
    </div>
  );
}
