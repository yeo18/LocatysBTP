import { useEffect } from "react";
import L from "leaflet";
import { MapContainer, Marker, TileLayer, useMap, useMapEvents } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { cn } from "../../core/utils/cn";

export interface LatLng {
  lat: number;
  lng: number;
}

// Icône SVG inline : évite les soucis de résolution des PNG Leaflet par le bundler.
const pinIcon = L.divIcon({
  className: "bg-transparent",
  html: `
    <svg width="34" height="42" viewBox="0 0 24 32" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 0C5.4 0 0 5.4 0 12c0 9 12 20 12 20s12-11 12-20C24 5.4 18.6 0 12 0z"
        fill="#ef4444" stroke="#ffffff" stroke-width="1.5"/>
      <circle cx="12" cy="12" r="5" fill="#ffffff"/>
    </svg>`,
  iconSize: [34, 42],
  iconAnchor: [17, 42],
});

const DEFAULT_CENTER: LatLng = { lat: 5.32, lng: -4.02 }; // Abidjan

function ClickHandler({ onClick, enabled }: { onClick: (p: LatLng) => void; enabled: boolean }) {
  useMapEvents({
    click(e) {
      if (enabled) onClick({ lat: e.latlng.lat, lng: e.latlng.lng });
    },
  });
  return null;
}

function CenterFollower({ position }: { position: LatLng | null }) {
  const map = useMap();
  useEffect(() => {
    if (position) map.flyTo(position, Math.max(map.getZoom(), 16), { duration: 0.7 });
  }, [position, map]);
  return null;
}

/**
 * Carte Leaflet avec marqueur déplaçable (glisser ou clic). En mode
 * lecture (`readonly`), le marqueur est figé et la carte en lecture seule.
 */
export function AnalyseMap({
  position,
  onPositionChange,
  height = "h-[380px]",
  readonly = false,
  className,
}: {
  position: LatLng | null;
  onPositionChange?: (p: LatLng) => void;
  height?: string;
  readonly?: boolean;
  className?: string;
}) {
  const center = position ?? DEFAULT_CENTER;
  return (
    <div className={cn("overflow-hidden rounded-xl border border-line", height, className)}>
      <MapContainer
        center={[center.lat, center.lng]}
        zoom={position ? 16 : 12}
        scrollWheelZoom
        style={{ height: "100%", width: "100%" }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {!readonly && (
          <ClickHandler
            enabled={!readonly}
            onClick={(p) => onPositionChange?.(p)}
          />
        )}
        {position && (
          <Marker
            position={[position.lat, position.lng]}
            icon={pinIcon}
            draggable={!readonly}
            eventHandlers={
              !readonly && onPositionChange
                ? {
                    dragend: (e) => {
                      const m = e.target as L.Marker;
                      const p = m.getLatLng();
                      onPositionChange({ lat: p.lat, lng: p.lng });
                    },
                  }
                : undefined
            }
          />
        )}
        <CenterFollower position={position} />
      </MapContainer>
    </div>
  );
}
