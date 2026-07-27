// Modul "campus", Teil 1: die Karte.

import { useEffect, useRef, useState } from "react";
import { Layers, LocateFixed, MapPin, Navigation } from "lucide-react";
import { escapeHtml, richtungsWinkel, statusClass } from "../campusUtils.mjs";

// Leaflet-Karte mit Browser-Standort und Fussroute von OSRM. Ohne Internet
// bleibt die lokale Ersatzkarte bedienbar (US 2.1).
export default function CampusMap({ campus, selectedRoom, onSelectRoom, standortTeilen }) {
  const nodeRef = useRef(null);
  const mapRef = useRef(null);
  const markerLayerRef = useRef(null);
  const routeLayerRef = useRef(null);
  const userMarkerRef = useRef(null);
  const [kartenStatus, setKartenStatus] = useState(window.L ? "Karte bereit" : "Offline-Fallback aktiv");
  const [position, setPosition] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);
  const [routing, setRouting] = useState(false);
  // Ausgefallener Kartendienst oder bewusster Wechsel: beides fuehrt zur
  // FallbackCampusMap.
  const [kartendienstAus, setKartendienstAus] = useState(false);
  const [manuellFallback, setManuellFallback] = useState(false);

  useEffect(() => {
    if (!window.L || manuellFallback || !nodeRef.current || mapRef.current) return undefined;
    const L = window.L;
    // Startansicht: Mittelpunkt der drei FH-Gebaeude; Zoom 17 zeigt alle drei.
    const map = L.map(nodeRef.current, { zoomControl: true }).setView([51.4939, 7.4199], 17);
    const strasse = L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 20,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    });
    const satellit = L.tileLayer(
      "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}",
      { maxZoom: 20, attribution: "Tiles &copy; Esri &mdash; Source: Esri and imagery providers" }
    );
    strasse.addTo(map);
    L.control.layers({ "Straße": strasse, "Satellit": satellit }, null, { position: "topright" }).addTo(map);
    L.control.scale({ imperial: false }).addTo(map);

    // Haeufen sich Kachelfehler, bieten wir die Ersatzkarte an (US 2.1).
    let kachelFehler = 0;
    const beiKachelErfolg = () => { kachelFehler = 0; setKartendienstAus(false); };
    const beiKachelFehler = () => { kachelFehler += 1; if (kachelFehler >= 6) setKartendienstAus(true); };
    strasse.on("tileload", beiKachelErfolg);
    satellit.on("tileload", beiKachelErfolg);
    strasse.on("tileerror", beiKachelFehler);
    satellit.on("tileerror", beiKachelFehler);

    markerLayerRef.current = L.layerGroup().addTo(map);
    routeLayerRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;
    window.setTimeout(() => map.invalidateSize(), 0);
    return () => {
      map.remove();
      mapRef.current = null;
      markerLayerRef.current = null;
      routeLayerRef.current = null;
      userMarkerRef.current = null;
    };
  }, [manuellFallback]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !window.L || !markerLayerRef.current) return;
    const L = window.L;
    markerLayerRef.current.clearLayers();
    campus.raeume.forEach((raum) => {
      const aktiv = selectedRoom?.id === raum.id ? " selected" : "";
      const icon = L.divIcon({
        className: "campus-pin-wrap",
        html: `<span class="campus-pin ${statusClass(raum.status)}${aktiv}"><b>${escapeHtml(raum.name)}</b><small>${escapeHtml(raum.status)}</small></span>`,
        iconSize: [126, 54],
        iconAnchor: [63, 54]
      });
      L.marker([raum.lat, raum.lng], { icon, title: raum.name })
        .on("click", () => onSelectRoom(raum.id))
        .addTo(markerLayerRef.current);
    });
  }, [campus.raeume, selectedRoom, onSelectRoom]);

  function ermittlePosition() {
    return new Promise((resolve, reject) => {
      if (!standortTeilen) {
        reject(new Error("Standortfreigabe ist in den Einstellungen deaktiviert."));
        return;
      }
      if (!navigator.geolocation) {
        reject(new Error("Dieser Browser unterstützt keine Standortbestimmung."));
        return;
      }
      setKartenStatus("Standort wird gesucht …");
      navigator.geolocation.getCurrentPosition(
        ({ coords }) => {
          const pos = [coords.latitude, coords.longitude];
          setPosition(pos);
          setKartenStatus(`Position gefunden (±${Math.round(coords.accuracy)} m)`);
          if (mapRef.current && window.L) {
            if (userMarkerRef.current) userMarkerRef.current.remove();
            userMarkerRef.current = window.L.circleMarker(pos, {
              radius: 9, color: "#fff", weight: 3, fillColor: "#2f69d9", fillOpacity: 1
            }).bindTooltip("Deine Position").addTo(mapRef.current);
            mapRef.current.setView(pos, 17);
          }
          resolve(pos);
        },
        (fehler) => reject(new Error(fehler.code === 1
          ? "Standortzugriff wurde nicht erlaubt. Bitte im Browser freigeben."
          : "Position konnte nicht bestimmt werden.")),
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 30000 }
      );
    });
  }

  async function standortAnzeigen() {
    try {
      await ermittlePosition();
    } catch (error) {
      setKartenStatus(error.message);
    }
  }

  async function routeBerechnen() {
    if (!selectedRoom) {
      setKartenStatus("Bitte zuerst einen Raum auf der Karte auswählen.");
      return;
    }
    setRouting(true);
    setRouteInfo(null);
    try {
      const start = position ?? await ermittlePosition();
      const ziel = [selectedRoom.lat, selectedRoom.lng];
      const url = "https://routing.openstreetmap.de/routed-foot/route/v1/driving/"
        + `${start[1]},${start[0]};${ziel[1]},${ziel[0]}`
        + "?overview=full&geometries=geojson&steps=true";
      const response = await fetch(url);
      if (!response.ok) throw new Error(`Routing-Dienst antwortet mit ${response.status}`);
      const data = await response.json();
      if (!data.routes?.length) throw new Error("Keine Fußroute gefunden.");
      zeichneRoute(data.routes[0].geometry.coordinates, start, ziel);
      const route = data.routes[0];
      setRouteInfo({
        ziel: selectedRoom.name,
        meter: Math.round(route.distance),
        minuten: Math.max(1, Math.round(route.duration / 60))
      });
      setKartenStatus(`Fußweg zu ${selectedRoom.name} berechnet`);
    } catch (error) {
      setKartenStatus(`Route nicht verfügbar: ${error.message}`);
    } finally {
      setRouting(false);
    }
  }

  function zeichneRoute(koordinaten, start, ziel) {
    if (!mapRef.current || !window.L) return;
    const L = window.L;
    const layer = routeLayerRef.current;
    layer.clearLayers();
    const latLngs = koordinaten.map(([lng, lat]) => [lat, lng]);
    L.polyline(latLngs, { color: "#fff", weight: 9, opacity: 0.9 }).addTo(layer);
    L.polyline(latLngs, { color: "#2f69d9", weight: 5, opacity: 0.95 }).addTo(layer);
    const schritt = Math.max(2, Math.floor(latLngs.length / 8));
    for (let i = schritt; i < latLngs.length - 1; i += schritt) {
      const winkel = richtungsWinkel(latLngs[i - 1], latLngs[i + 1]);
      L.marker(latLngs[i], {
        interactive: false,
        icon: L.divIcon({
          className: "route-arrow-wrap",
          html: `<span class="route-arrow" style="transform:rotate(${winkel}deg)">➤</span>`,
          iconSize: [24, 24], iconAnchor: [12, 12]
        })
      }).addTo(layer);
    }
    mapRef.current.fitBounds(L.latLngBounds([start, ziel]).pad(0.25), { maxZoom: 18 });
  }

  // Ohne Leaflet oder nach bewusstem Wechsel uebernimmt die Ersatzkarte.
  if (!window.L || manuellFallback) {
    return (
      <FallbackCampusMap
        campus={campus}
        selectedRoom={selectedRoom}
        onSelectRoom={onSelectRoom}
        onZurueck={window.L ? () => { setManuellFallback(false); setKartendienstAus(false); } : undefined}
      />
    );
  }

  return (
    <div className="real-map-shell">
      <div className="map-toolbar" aria-label="Kartenfunktionen">
        <button type="button" onClick={standortAnzeigen} disabled={!standortTeilen}>
          <LocateFixed size={16} /> Meine Position
        </button>
        <button type="button" className="route-button" onClick={routeBerechnen} disabled={routing || !standortTeilen}>
          <Navigation size={16} /> {routing ? "Route wird berechnet …" : "Fußweg zum Raum"}
        </button>
        <button type="button" onClick={() => setManuellFallback(true)} title="Zur Offline-Ersatzkarte wechseln">
          <MapPin size={16} /> Offline-Karte
        </button>
        <span><Layers size={15} /> Straße/Satellit oben rechts</span>
      </div>
      <div ref={nodeRef} className="leaflet-campus-map" aria-label="Interaktive Karte des Campus Emil-Figge-Straße" />
      <div className="map-feedback" role="status">
        <span>{kartenStatus}</span>
        {kartendienstAus && (
          <button type="button" className="route-button" onClick={() => setManuellFallback(true)}>
            ⚠ Kartendienst gestört – zur Offline-Karte wechseln
          </button>
        )}
        {routeInfo && <strong>{routeInfo.meter} m · ca. {routeInfo.minuten} Min. bis {routeInfo.ziel}</strong>}
      </div>
    </div>
  );
}

function FallbackCampusMap({ campus, selectedRoom, onSelectRoom, onZurueck }) {
  return (
    <div className="campus-map" aria-label="Offline-Fallback der Campus-Karte">
      <div className="offline-badge">Offline-Karte · Internet für Satellit und Route erforderlich</div>
      {onZurueck && (
        <button type="button" className="offline-back" onClick={onZurueck} title="Zurück zur Live-Karte">
          <MapPin size={14} /> Zur Live-Karte
        </button>
      )}
      <div className="road road-v" />
      <div className="road road-h" />
      <div className="green-zone zone-one" />
      <div className="green-zone zone-two" />

      {campus.raeume.map((raum) => (
        <button
          key={raum.id}
          type="button"
          className={`room-marker ${selectedRoom?.id === raum.id ? "selected" : ""} ${statusClass(raum.status)}`}
          style={{ left: `${raum.x}%`, top: `${raum.y}%` }}
          onClick={() => onSelectRoom(raum.id)}
        >
          <span>{raum.name}</span>
          <small>{raum.status}</small>
        </button>
      ))}
    </div>
  );
}
