import { cn } from "../utils/cn";

export interface Slice {
  name: string;
  value: number;
  color: string;
}

export function DonutChart({
  data,
  size = 190,
  thickness = 26,
}: {
  data: Slice[];
  size?: number;
  thickness?: number;
}) {
  const total = data.reduce((s, d) => s + d.value, 0) || 1;
  const radius = (size - thickness) / 2;
  const circ = 2 * Math.PI * radius;
  let offset = 0;
  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          strokeWidth={thickness}
          className="stroke-slate-100 dark:stroke-white/10"
        />
        {data.map((d, i) => {
          const len = (d.value / total) * circ;
          const dash = `${len} ${circ - len}`;
          const el = (
            <circle
              key={i}
              cx={size / 2}
              cy={size / 2}
              r={radius}
              fill="none"
              stroke={d.color}
              strokeWidth={thickness}
              strokeDasharray={dash}
              strokeDashoffset={-offset}
              strokeLinecap="butt"
            />
          );
          offset += len;
          return el;
        })}
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="text-3xl font-black text-ink">{total}</span>
        <span className="text-xs text-muted">total</span>
      </div>
    </div>
  );
}

export function BarChart({
  data,
  color = "#3b82f6",
  height = 220,
  suffix = "",
}: {
  data: { name: string; value: number }[];
  color?: string;
  height?: number;
  suffix?: string;
}) {
  const max = Math.max(...data.map((d) => d.value), 1);
  return (
    <div className="flex items-end gap-3" style={{ height }}>
      {data.map((d, i) => (
        <div key={i} className="flex flex-1 flex-col items-center gap-2 min-w-0">
          <div className="flex w-full flex-1 items-end justify-center">
            <div className="group relative flex w-full max-w-[46px] items-end justify-center">
              <div
                className="w-full rounded-t-md transition-all duration-500 hover:opacity-90"
                style={{ height: `${(d.value / max) * (height - 48)}px`, background: color, minHeight: 4 }}
              />
              <div className="pointer-events-none absolute -top-7 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-md bg-ink px-2 py-1 text-[10px] font-semibold text-app opacity-0 shadow transition group-hover:opacity-100">
                {d.value}
                {suffix}
              </div>
            </div>
          </div>
          <span className="line-clamp-1 text-center text-[10px] font-medium text-muted" title={d.name}>
            {d.name}
          </span>
        </div>
      ))}
    </div>
  );
}

export function AreaChart({
  data,
  color = "#1e3a5f",
  height = 180,
}: {
  data: { name: string; value: number }[];
  color?: string;
  height?: number;
}) {
  const w = 560;
  const h = height;
  const pad = 24;
  const max = Math.max(...data.map((d) => d.value), 1);
  const stepX = data.length > 1 ? (w - pad * 2) / (data.length - 1) : 0;
  const pts = data.map((d, i) => ({
    x: pad + i * stepX,
    y: h - pad - (d.value / max) * (h - pad * 2),
  }));
  const line = pts.map((p, i) => `${i === 0 ? "M" : "L"}${p.x},${p.y}`).join(" ");
  const area = `${line} L${pts[pts.length - 1]?.x ?? pad},${h - pad} L${pts[0]?.x ?? pad},${h - pad} Z`;
  const gid = "ag-" + color.replace("#", "");
  return (
    <svg viewBox={`0 0 ${w} ${h}`} className="w-full" style={{ height }} preserveAspectRatio="none">
      <defs>
        <linearGradient id={gid} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.35" />
          <stop offset="100%" stopColor={color} stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={area} fill={`url(#${gid})`} />
      <path d={line} fill="none" stroke={color} strokeWidth="2.5" strokeLinejoin="round" strokeLinecap="round" />
      {pts.map((p, i) => (
        <circle key={i} cx={p.x} cy={p.y} r="3.5" fill={color} />
      ))}
    </svg>
  );
}

export function LegendDot({ color, label, value }: { color: string; label: string; value?: number | string }) {
  return (
    <div className={cn("flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm")}>
      <span className="h-2.5 w-2.5 shrink-0 rounded-full" style={{ background: color }} />
      <span className="min-w-0 flex-1 truncate text-body">{label}</span>
      {value !== undefined && <span className="shrink-0 font-semibold text-ink">{value}</span>}
    </div>
  );
}
