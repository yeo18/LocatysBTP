import { useState, type InputHTMLAttributes, type ReactNode } from "react";
import { motion } from "framer-motion";
import { FaEye, FaEyeSlash, FaHelmetSafety, FaLock, FaTriangleExclamation } from "react-icons/fa6";
import { cn } from "../core/utils/cn";
import { TextInput } from "../core/components/ui";

export function LogoLocatysBtp({
  className,
  iconClassName = "bg-amber-500/15 text-amber-400 ring-amber-500/40",
  wordmarkClassName = "text-white",
  iconSizeClassName = "h-11 w-11",
  iconSize = 24,
}: {
  className?: string;
  iconClassName?: string;
  wordmarkClassName?: string;
  iconSizeClassName?: string;
  iconSize?: number;
}) {
  return (
    <span className={cn("inline-flex items-center gap-3", className)}>
      <span
        className={cn(
          "flex shrink-0 items-center justify-center rounded-xl ring-1",
          iconSizeClassName,
          iconClassName
        )}
      >
        <FaHelmetSafety size={iconSize} />
      </span>
      <span className={cn("text-2xl font-black tracking-tight", wordmarkClassName)}>
        LOCATYS<span className="text-amber-400">BTP</span>
      </span>
    </span>
  );
}

export function BlueprintBackdrop() {
  return (
    <div aria-hidden className="pointer-events-none absolute inset-0 overflow-hidden">
      <div className="absolute inset-0 bg-blueprint" />
      <div
        className="absolute -right-32 -top-32 h-[26rem] w-[26rem] rounded-full opacity-25 blur-3xl"
        style={{ background: "radial-gradient(circle, #f59e0b 0%, transparent 70%)" }}
      />
      <div
        className="absolute -bottom-40 left-1/4 h-[24rem] w-[24rem] rounded-full opacity-15 blur-3xl"
        style={{ background: "radial-gradient(circle, #93c5fd 0%, transparent 70%)" }}
      />

      <svg
        viewBox="0 0 420 320"
        className="absolute left-[5%] top-1/2 hidden w-[30rem] -translate-y-1/2 opacity-[0.14] lg:block"
      >
        <g fill="none" stroke="#93c5fd" strokeWidth="1.2">
          <rect x="30" y="20" width="360" height="260" strokeWidth="2" />
          <line x1="120" y1="20" x2="120" y2="160" />
          <line x1="270" y1="20" x2="270" y2="160" strokeDasharray="5 5" />
          <line x1="30" y1="160" x2="270" y2="160" />
          <line x1="270" y1="160" x2="270" y2="280" strokeWidth="2" />
          <rect x="156" y="160" width="80" height="60" />
          <circle cx="120" cy="240" r="24" />
          <path d="M 393 280 H 410 v -90" />
        </g>
        <g stroke="#f59e0b" strokeWidth="1" opacity="0.9">
          <line x1="30" y1="300" x2="390" y2="300" />
          <line x1="30" y1="294" x2="30" y2="306" />
          <line x1="390" y1="294" x2="390" y2="306" />
          <path d="M 26 60 h -10 v 120 h 10" />
          <path d="M 394 210 h 10 v -90 h -10" />
        </g>
      </svg>

      <svg
        viewBox="0 0 220 300"
        className="absolute right-[4%] top-1/2 hidden w-[17rem] -translate-y-1/2 opacity-[0.11] lg:block"
      >
        <g fill="none" stroke="#93c5fd" strokeWidth="1.4">
          <path d="M 40 290 V 60 H 190" />
          <line x1="40" y1="60" x2="150" y2="120" />
          <line x1="150" y1="120" x2="190" y2="120" />
          <line x1="40" y1="60" x2="150" y2="120" />
          <line x1="150" y1="120" x2="150" y2="200" />
          <circle cx="150" cy="120" r="4" />
          <path d="M 60 290 v 8" strokeDasharray="2 4" />
          <path d="M 110 290 v 8" strokeDasharray="2 4" />
          <path d="M 160 290 v 8" strokeDasharray="2 4" />
        </g>
      </svg>
    </div>
  );
}

export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <div className="relative min-h-screen overflow-hidden bg-[#0a1628]">
      <BlueprintBackdrop />
      <div className="relative z-10 mx-auto grid min-h-screen max-w-6xl grid-cols-1 content-center gap-10 px-5 py-10 lg:grid-cols-[1.05fr_0.95fr] lg:items-center lg:gap-14 lg:px-8">
        <div className="lg:hidden">
          <LogoLocatysBtp />
        </div>
        {children}
      </div>
      <p className="absolute bottom-4 left-1/2 z-10 w-full -translate-x-1/2 whitespace-nowrap px-4 text-center text-[11px] text-slate-500">
        © 2026 LOCATYSBTP. Plateforme de gestion de chantiers.
      </p>
    </div>
  );
}

export function AuthHero() {
  return (
    <aside className="relative z-10 hidden flex-col justify-center gap-10 pb-8 lg:flex lg:min-h-[640px]">
      <LogoLocatysBtp />
      <div>
        <p className="text-[13px] font-bold uppercase tracking-[0.3em] text-amber-400">
          Gestion numérique de chantiers
        </p>
        <h1 className="mt-3 max-w-md text-4xl font-black leading-[1.15] tracking-tight text-white">
          Pilotez vos chantiers du plan au terrain.
        </h1>
      </div>
    </aside>
  );
}

export function AuthCard({
  refCode,
  title,
  subtitle,
  children,
}: {
  refCode: string;
  title: string;
  subtitle: string;
  children: ReactNode;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: "easeOut" }}
      className="w-full max-w-md justify-self-center lg:justify-self-end"
    >
      <div className="relative overflow-hidden rounded-2xl border border-line bg-card shadow-card">
        <div className="h-1 w-full bg-gradient-to-r from-amber-400 via-amber-500 to-amber-400" />
        <div className="px-7 py-8 sm:px-10">
          <div className="mb-7 flex flex-col items-center text-center">
            <LogoLocatysBtp
              className="flex-col gap-2.5"
              iconClassName="bg-accent/10 text-accent ring-accent/25"
              iconSizeClassName="h-12 w-12"
              iconSize={26}
              wordmarkClassName="text-ink text-[22px]"
            />
            <h2 className="mt-4 text-2xl font-black tracking-tight text-ink">{title}</h2>
            <p className="mt-1.5 text-sm text-muted">{subtitle}</p>
            <span className="mt-3 font-mono text-[10px] uppercase tracking-[0.25em] text-muted/70">
              {refCode}
            </span>
          </div>
          {children}
        </div>
      </div>
    </motion.div>
  );
}

export function ChampIcone({
  icon,
  invalid,
  className,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { icon: ReactNode; invalid?: boolean }) {
  return (
    <div className="relative">
      <span className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-muted">
        {icon}
      </span>
      <TextInput invalid={invalid} {...props} className={cn("pl-10", className)} />
    </div>
  );
}

export function ChampMotDePasse({
  value,
  onChange,
  invalid,
  placeholder = "••••••••",
  autoComplete = "current-password",
}: {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  invalid?: boolean;
  placeholder?: string;
  autoComplete?: string;
}) {
  const [visible, setVisible] = useState(false);
  return (
    <div className="relative">
      <ChampIcone
        icon={<FaLock className="h-4 w-4" />}
        type={visible ? "text" : "password"}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required
        autoComplete={autoComplete}
        className="pr-11"
        invalid={invalid}
      />
      <button
        type="button"
        onClick={() => setVisible((v) => !v)}
        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-muted transition hover:text-ink"
        aria-label={visible ? "Masquer le mot de passe" : "Afficher le mot de passe"}
      >
        {visible ? <FaEyeSlash className="h-4 w-4" /> : <FaEye className="h-4 w-4" />}
      </button>
    </div>
  );
}

export function MessageErreur({ message }: { message: string }) {
  return (
    <div
      role="alert"
      className="mb-5 flex items-start gap-2.5 rounded-lg border border-danger/30 bg-danger-light px-4 py-3 text-sm font-semibold text-danger"
    >
      <FaTriangleExclamation className="mt-0.5 h-4 w-4 shrink-0" />
      {message}
    </div>
  );
}