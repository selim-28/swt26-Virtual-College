import { useEffect, useMemo, useRef, useState } from "react";
import {
  Bus,
  CalendarCheck,
  CheckCircle2,
  ClipboardList,
  Heart,
  History,
  LogIn,
  LogOut,
  MapPin,
  LocateFixed,
  Layers,
  MessageSquarePlus,
  Navigation,
  RefreshCcw,
  Send,
  Settings,
  Soup,
  Trash2,
  User,
  UserPlus
} from "lucide-react";
import { escapeHtml, richtungsWinkel, statusClass, POLL_INTERVALL_MS, sollAktualisieren, darfRaumReservieren, neuerPostHinweis } from "./campusUtils.mjs";

// Zentrale API-Adressen. Nginx leitet diese relativen Pfade an die Backends weiter.
const API = {
  campus: "/api/campus/karte",
  reservierungen: "/api/campus/reservierungen",
  posts: "/api/pinwand/posts",
  mensa: "/api/mensa/speiseplan",
  historie: "/api/mensa/historie",
  transit: "/api/transit/abfahrten",
  simulation: "/api/simulation/start",
  // Authentifizierung (Registrieren / Login / "wer bin ich" / Logout).
  authRegister: "/api/auth/registrieren",
  authLogin: "/api/auth/login",
  authIch: "/api/auth/ich",
  authLogout: "/api/auth/logout"
};

// Persoenliche UI-Einstellungen (gehoeren NICHT zum Konto, nur zur Anzeige).
const START_PREFS = {
  standortTeilen: true,
  benachrichtigungen: true
};

// --- Token-Verwaltung im Browser (bleibt auch nach Reload erhalten) ---
function gespeichertesToken() {
  try { return localStorage.getItem("vc_token"); } catch { return null; }
}
function gespeichertesKonto() {
  try { const s = localStorage.getItem("vc_konto"); return s ? JSON.parse(s) : null; } catch { return null; }
}
function merkeKonto(data) {
  try {
    localStorage.setItem("vc_token", data.token);
    localStorage.setItem("vc_konto", JSON.stringify(data));
  } catch { /* localStorage nicht verfuegbar: dann eben nur fuer diese Sitzung */ }
}
function vergissKonto() {
  try { localStorage.removeItem("vc_token"); localStorage.removeItem("vc_konto"); } catch { /* egal */ }
}

// Kleine Helferfunktion fuer Fetch-Aufrufe: haengt automatisch das Login-Token
// an und liest bei Fehlern die Klartext-Meldung des Backends aus.
// Das Token ist jetzt ein JWT und wird als "Authorization: Bearer <token>"
// gesendet (Standard fuer token-basierte APIs mit Spring Security).
async function apiJson(url, options = {}) {
  const token = gespeichertesToken();
  const headers = { ...(options.headers || {}) };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const response = await fetch(url, { ...options, headers });
  if (!response.ok) {
    let meldung = `${response.status} ${response.statusText}`;
    try {
      const body = await response.json();
      if (body && body.message) meldung = body.message;
    } catch { /* keine JSON-Fehlermeldung vorhanden */ }
    throw new Error(meldung);
  }
  // Manche Endpunkte (z. B. Logout) antworten ohne Inhalt.
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

// Format fuer Euro-Preise in der Mensa.
function euro(value) {
  return new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" }).format(value);
}

// Kurze Uhrzeit/Datum-Anzeige fuer Posts und Reservierungen.
function kurzZeit(isoText) {
  return new Date(isoText).toLocaleString("de-DE", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

// Hauptkomponente: haelt den App-Zustand und verdrahtet alle Teilansichten.
export default function App() {
  // Aktiver Navigationsbereich.
  const [view, setView] = useState("campus");

  // Eingeloggtes Benutzerkonto (oder null = nicht eingeloggt).
  const [konto, setKonto] = useState(gespeichertesKonto);
  // Persoenliche UI-Einstellungen (Standort teilen, Benachrichtigungen).
  const [prefs, setPrefs] = useState(START_PREFS);
  // Aus Konto + Einstellungen wird das "Profil", das Reservierungen/Posts nutzen.
  const profil = {
    name: konto?.name ?? "",
    rolle: konto?.rolle ?? "Student",
    email: konto?.email ?? "",
    ...prefs
  };

  // Daten aus den Backends.
  const [campus, setCampus] = useState({ raeume: [], veranstaltungen: [], reservierungen: [] });
  const [posts, setPosts] = useState([]);
  const [mensa, setMensa] = useState({ quelle: "lade...", gerichte: [] });
  const [transit, setTransit] = useState({ quelle: "lade...", haltestelle: "", abfahrten: [] });
  const [historie, setHistorie] = useState({ anzahlGesamt: 0, eintraege: [] });

  // UI-Zustand fuer Dialog, Statusmeldungen und Ladezustand.
  const [selectedRoomId, setSelectedRoomId] = useState(null);
  const [status, setStatus] = useState("Bereit");
  const [busy, setBusy] = useState(false);
  // Merkt sich, ob GERADE eine Ladung laeuft. Als Ref (nicht State), damit der
  // Polling-Takt immer den aktuellen Wert sieht und Ladungen sich nicht ueberholen.
  const ladeLaeuftRef = useRef(false);

  // Refs fuer die Benachrichtigung ueber neue Posts (US 4.3): Der Polling-Takt
  // laeuft in einer alten Closure - ueber die Refs sieht er trotzdem immer die
  // AKTUELLE Post-Liste und die AKTUELLEN Einstellungen.
  const postsRef = useRef([]);
  const prefsRef = useRef(prefs);
  prefsRef.current = prefs;

  // Posts-Liste zentral uebernehmen (State + Ref gemeinsam pflegen).
  function uebernimmPosts(liste) {
    postsRef.current = liste;
    setPosts(liste);
  }

  // Aus der Raum-ID wird das aktuell ausgewaehlte Raumobjekt.
  const selectedRoom = useMemo(
    () => campus.raeume.find((raum) => raum.id === selectedRoomId) ?? null,
    [campus.raeume, selectedRoomId]
  );

  // Beim Start: gespeichertes Token pruefen. Ist es noch gueltig, bleibt man
  // eingeloggt; sonst wird ausgeloggt. Danach (und bei jedem Login) Daten laden.
  useEffect(() => {
    if (!konto) return;
    apiJson(API.authIch)
      .then((daten) => {
        merkeKonto(daten);
        setKonto(daten);
        ladeAlles();
      })
      .catch(() => {
        vergissKonto();
        setKonto(null);
      });
    // Absicht: nur einmal beim Mounten pruefen.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // POLLING (letzte Meile zu EPIC 7): MQTT bringt frische Daten nur bis ins
  // backend_receive. Das Frontend spricht HTTP und muss aktiv nachfragen. Statt
  // den "Aktualisieren"-Knopf manuell zu druecken, fragt die App nun selbst in
  // festen Abstaenden nach – aber nur eingeloggt und nur bei sichtbarem Tab.
  useEffect(() => {
    if (!konto) return undefined;
    const id = window.setInterval(() => {
      const darf = sollAktualisieren({
        eingeloggt: Boolean(konto),
        seiteSichtbar: document.visibilityState === "visible",
        ladeLaeuft: ladeLaeuftRef.current
      });
      if (darf) ladeAlles({ stumm: true });
    }, POLL_INTERVALL_MS);
    // Aufraeumen: Timer stoppen, wenn sich das Konto aendert (z.B. Logout).
    return () => window.clearInterval(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [konto]);

  // Login: Token holen, merken, Daten laden.
  async function login(email, passwort) {
    const daten = await apiJson(API.authLogin, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, passwort })
    });
    merkeKonto(daten);
    setKonto(daten);
    setStatus(`Eingeloggt als ${daten.name}`);
    await ladeAlles();
  }

  // Registrieren: legt ein Konto an und loggt direkt ein.
  async function registrieren({ email, name, rolle, passwort }) {
    const daten = await apiJson(API.authRegister, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, name, rolle, passwort })
    });
    merkeKonto(daten);
    setKonto(daten);
    setStatus(`Willkommen, ${daten.name}`);
    await ladeAlles();
  }

  // Logout: Token am Backend ungueltig machen und lokal vergessen.
  function logout() {
    apiJson(API.authLogout, { method: "POST" }).catch(() => {});
    vergissKonto();
    setKonto(null);
    setView("campus");
    setStatus("Abgemeldet");
  }

  // Laedt alle Daten, die die Startseite braucht.
  // stumm=true = automatische Aktualisierung (Polling): ohne "Lade..."-Puls und
  // ohne Fehlermeldung, damit sie im Hintergrund nicht stoert.
  async function ladeAlles({ stumm = false } = {}) {
    ladeLaeuftRef.current = true;
    if (!stumm) setBusy(true);
    try {
      const [campusData, postData, mensaData, transitData, historieData] = await Promise.all([
        apiJson(API.campus),
        apiJson(API.posts),
        apiJson(API.mensa),
        apiJson(API.transit),
        apiJson(API.historie)
      ]);
      // US 4.3: VOR dem Uebernehmen pruefen, ob neue Beitraege dazugekommen
      // sind - registrierte Beobachter (Schalter "Neue Pinwand-Posts melden")
      // bekommen dann eine 🔔-Meldung in der Statuszeile.
      const hinweis = neuerPostHinweis(
        postsRef.current, postData, prefsRef.current.benachrichtigungen);

      setCampus(campusData);
      uebernimmPosts(postData);
      setMensa(mensaData);
      setTransit(transitData);
      setHistorie(historieData);
      setStatus(hinweis ?? (stumm
        ? `Automatisch aktualisiert (${new Date().toLocaleTimeString("de-DE")})`
        : "Daten geladen"));
    } catch (error) {
      // Beim automatischen Aktualisieren NICHT mit Fehlern nerven – der naechste
      // Takt versucht es ohnehin gleich wieder. Nur beim manuellen Laden zeigen.
      if (!stumm) setStatus(`Fehler beim Laden: ${error.message}`);
    } finally {
      ladeLaeuftRef.current = false;
      if (!stumm) setBusy(false);
    }
  }

  // Startet das Fremdsystem, wartet kurz auf MQTT und aktualisiert danach alle Daten.
  async function simulationStarten() {
    setBusy(true);
    setStatus("Simulation laeuft: Factory -> MQTT -> Adapter -> DB");
    try {
      await apiJson(API.simulation, { method: "POST" });
      await new Promise((resolve) => window.setTimeout(resolve, 650));
      await ladeAlles();
      setView("daten");
      setStatus("Simulation erfolgreich: Live-Daten und Historie aktualisiert");
    } catch (error) {
      setStatus(`Simulation fehlgeschlagen: ${error.message}`);
    } finally {
      setBusy(false);
    }
  }

  // Speichert eine echte Raumreservierung im Backend.
  async function reservieren(event) {
    event.preventDefault();
    if (!selectedRoom) {
      return;
    }
    const form = new FormData(event.currentTarget);
    setBusy(true);
    try {
      await apiJson(API.reservierungen, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          raumId: selectedRoom.id,
          reserviertVon: profil.name,
          von: form.get("von"),
          bis: form.get("bis")
        })
      });
      await ladeAlles();
      setStatus(`${selectedRoom.name} wurde reserviert`);
    } catch (error) {
      setStatus(`Reservierung fehlgeschlagen: ${error.message}`);
    } finally {
      setBusy(false);
    }
  }

  // Speichert einen neuen Pinwand-Post in der Datenbank.
  async function postErstellen(event) {
    event.preventDefault();
    // WICHTIG: das Formular-Element JETZT sichern. Nach einem "await" setzt React
    // event.currentTarget auf null – ein spaeteres event.currentTarget.reset()
    // wuerde sonst mit "Cannot read properties of null (reading 'reset')" abstuerzen.
    const formEl = event.currentTarget;
    const form = new FormData(formEl);
    setBusy(true);
    try {
      await apiJson(API.posts, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          autor: profil.name,
          rolle: profil.rolle,
          kategorie: form.get("kategorie"),
          titel: form.get("titel"),
          inhalt: form.get("inhalt")
        })
      });
      formEl.reset();   // gesicherte Referenz benutzen (nicht event.currentTarget)
      uebernimmPosts(await apiJson(API.posts));
      setStatus("Post gespeichert und Pinwand aktualisiert");
    } catch (error) {
      setStatus(`Post konnte nicht gespeichert werden: ${error.message}`);
    } finally {
      setBusy(false);
    }
  }

  // Like auf einen Post speichern und Liste neu laden.
  async function postLiken(id) {
    setBusy(true);
    try {
      await apiJson(`${API.posts}/${id}/like`, { method: "POST" });
      uebernimmPosts(await apiJson(API.posts));
      setStatus("Like gespeichert");
    } catch (error) {
      setStatus(`Like fehlgeschlagen: ${error.message}`);
    } finally {
      setBusy(false);
    }
  }

  // ADMIN: Pinwand-Post loeschen (das Backend erlaubt DELETE nur der Rolle Admin).
  async function postLoeschen(id) {
    setBusy(true);
    try {
      await apiJson(`${API.posts}/${id}`, { method: "DELETE" });
      uebernimmPosts(await apiJson(API.posts));
      setStatus("Post geloescht");
    } catch (error) {
      setStatus(`Loeschen fehlgeschlagen: ${error.message}`);
    } finally {
      setBusy(false);
    }
  }

  // ADMIN: Reservierung loeschen.
  async function reservierungLoeschen(id) {
    setBusy(true);
    try {
      await apiJson(`${API.reservierungen}/${id}`, { method: "DELETE" });
      await ladeAlles();
      setStatus("Reservierung geloescht");
    } catch (error) {
      setStatus(`Loeschen fehlgeschlagen: ${error.message}`);
    } finally {
      setBusy(false);
    }
  }

  // Nur der Admin bekommt Verwaltungs-Funktionen (Loeschknoepfe) angezeigt.
  const istAdmin = konto?.rolle === "Admin";

  // Nicht eingeloggt? Dann zeigen wir nur die Login-/Registrier-Seite.
  if (!konto) {
    return <AuthView onLogin={login} onRegister={registrieren} />;
  }

  return (
    <div className="app-shell">
      <Header view={view} onView={setView} konto={konto} onLogout={logout} />

      <main>
        <StatusBar busy={busy} status={status} onRefresh={ladeAlles} />

        {view === "campus" && (
          <CampusView
            campus={campus}
            selectedRoom={selectedRoom}
            onSelectRoom={setSelectedRoomId}
            onReserve={reservieren}
            profil={profil}
            mensa={mensa}
            historie={historie}
            istAdmin={istAdmin}
            onDeleteReservierung={reservierungLoeschen}
          />
        )}

        {view === "pinwand" && (
          <PinwandView
            posts={posts}
            onCreate={postErstellen}
            onLike={postLiken}
            istAdmin={istAdmin}
            onDelete={postLoeschen}
          />
        )}

        {view === "daten" && (
          <DatenView transit={transit} onSimulation={simulationStarten} busy={busy} />
        )}

        {view === "einstellungen" && (
          <EinstellungenView profil={profil} prefs={prefs} onPrefs={setPrefs} onLogout={logout} />
        )}
      </main>
    </div>
  );
}

// Kopfbereich mit Navigation und eingeloggtem Nutzer.
function Header({ view, onView, konto, onLogout }) {
  const tabs = [
    ["campus", "Live Campus", MapPin],
    ["pinwand", "Pinwand", MessageSquarePlus],
    ["daten", "ÖPNV", Bus],
    ["einstellungen", "Einstellungen", Settings]
  ];

  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">Virtual College</p>
        <h1>Digitaler Campus</h1>
      </div>
      <nav className="tabs" aria-label="Hauptbereiche">
        {tabs.map(([key, label, Icon]) => (
          <button
            className={`tab ${view === key ? "active" : ""}`}
            key={key}
            onClick={() => onView(key)}
            type="button"
          >
            <Icon size={16} />
            {label}
          </button>
        ))}
      </nav>
      <div className="account">
        <span className="account-name" title={konto.email}>
          <User size={15} /> {konto.name}
          <small>{konto.rolle}</small>
        </span>
        <button type="button" className="icon-button" onClick={onLogout} aria-label="Abmelden" title="Abmelden">
          <LogOut size={18} />
        </button>
      </div>
    </header>
  );
}

// Kleine Statusleiste fuer Ladezustand und manuelles Aktualisieren.
function StatusBar({ busy, status, onRefresh }) {
  return (
    <section className="statusbar">
      <span className={busy ? "pulse" : ""}>{status}</span>
      <button type="button" className="icon-button" onClick={onRefresh} aria-label="Daten aktualisieren">
        <RefreshCcw size={18} />
      </button>
    </section>
  );
}

// Campus-Seite mit Karte und Raumdetails.
function CampusView({ campus, selectedRoom, onSelectRoom, onReserve, profil,
                      mensa, historie, istAdmin, onDeleteReservierung }) {
  return (
    <section className="page-grid campus-grid">
      <div className="workspace">
        <SectionTitle kicker="Echter Campus Emil-Figge-Straße" title="Straßenkarte, Satellit und Navigation" />
        <CampusMap
          campus={campus}
          selectedRoom={selectedRoom}
          onSelectRoom={onSelectRoom}
          standortTeilen={profil.standortTeilen}
        />
      </div>
      <RoomPanel
        selectedRoom={selectedRoom}
        campus={campus}
        profil={profil}
        onReserve={onReserve}
        mensa={mensa}
        historie={historie}
        istAdmin={istAdmin}
        onDeleteReservierung={onDeleteReservierung}
      />
    </section>
  );
}

// Echte Leaflet-Karte. Sie nutzt die Geolocation des Browsers und holt eine
// Fussroute von OSRM. Ohne CDN/Internet bleibt die lokale Fallback-Karte nutzbar.
function CampusMap({ campus, selectedRoom, onSelectRoom, standortTeilen }) {
  const nodeRef = useRef(null);
  const mapRef = useRef(null);
  const markerLayerRef = useRef(null);
  const routeLayerRef = useRef(null);
  const userMarkerRef = useRef(null);
  const [kartenStatus, setKartenStatus] = useState(window.L ? "Karte bereit" : "Offline-Fallback aktiv");
  const [position, setPosition] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);
  const [routing, setRouting] = useState(false);
  // Kartendienst-Ausfall (Kacheln laden nicht) bzw. bewusster Wechsel auf die
  // Offline-Ersatzkarte. Beides fuehrt zur nutzbaren FallbackCampusMap.
  const [kartendienstAus, setKartendienstAus] = useState(false);
  const [manuellFallback, setManuellFallback] = useState(false);

  useEffect(() => {
    if (!window.L || manuellFallback || !nodeRef.current || mapRef.current) return undefined;
    const L = window.L;
    // Startansicht: Mittelpunkt der drei FH-Gebaeude (Hoersaalgeb./Informatik/Haus 44),
    // Zoom 17 zeigt alle drei Cluster; beim Hineinzoomen trennen sich die Raeume.
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

    // Ausfall des Kartendienstes erkennen: Laedt keine einzige Kachel, aber es
    // haeufen sich Fehler, bieten wir die Offline-Ersatzkarte an (US 2.1).
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

  // Ohne Leaflet-Bibliothek (CDN blockiert) ODER bei bewusstem Wechsel zeigen
  // wir die Offline-Ersatzkarte. Sie bleibt immer bedienbar (US 2.1).
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

// Rechte Detailspalte fuer Raum, Veranstaltungen und Reservierungen.
// Die Mensa (kostBar) zeigt hier direkt ihren LIVE-SPEISEPLAN aus den
// Fremddaten (EPIC 5) - man muss dafuer nicht mehr in einen anderen Tab.
function RoomPanel({ selectedRoom, campus, profil, onReserve,
                     mensa, historie, istAdmin, onDeleteReservierung }) {
  const reservierungen = selectedRoom
    ? campus.reservierungen.filter((reservierung) => reservierung.raumId === selectedRoom.id)
    : campus.reservierungen.slice(0, 3);
  const veranstaltungen = selectedRoom
    ? campus.veranstaltungen.filter((veranstaltung) => veranstaltung.raumId === selectedRoom.id)
    : campus.veranstaltungen;

  const istMensa = selectedRoom?.typ === "Mensa";
  // Rollenregel (Spiegel des Backends): Hoersaele nur Professor/Admin usw.
  const darfBuchen = darfRaumReservieren(selectedRoom, profil.rolle);

  return (
    <aside className="side-panel">
      <SectionTitle
        kicker={selectedRoom ? selectedRoom.gebaeude : "Campus"}
        title={selectedRoom ? selectedRoom.name : "Raum auswaehlen"}
      />
      {selectedRoom ? (
        <>
          <p className="muted">{selectedRoom.beschreibung}</p>
          <Metric label="Auslastung" value={`${selectedRoom.personen}/${selectedRoom.kapazitaet}`} />
          <Metric label="Status" value={selectedRoom.status} />
          <TagList items={selectedRoom.ausstattung} />

          {istMensa && <MensaPanel mensa={mensa} historie={historie} />}

          {!istMensa && darfBuchen && <ReserveForm profil={profil} onReserve={onReserve} />}

          {!istMensa && !darfBuchen && (
            // US 3.2 + Rollenregel: verstaendlich erklaeren, warum es hier
            // kein Formular gibt (Backend lehnt solche Anfragen ohnehin ab).
            <p className="muted">
              {selectedRoom.typ === "Vorlesung"
                ? `${selectedRoom.name} ist ein Hoersaal - reservieren duerfen ihn nur Professor:innen oder die Verwaltung (Admin).`
                : `${selectedRoom.name} kann nicht reserviert werden (${selectedRoom.typ}). Reservierbar sind freie Lern- und Seminarraeume.`}
            </p>
          )}
        </>
      ) : (
        <p className="muted">Klicke auf einen Raum in der Karte, um Details und Aktionen zu sehen.</p>
      )}

      <MiniList title="Veranstaltungen" icon={CalendarCheck} items={veranstaltungen.map((v) => `${v.titel} (${v.von}-${v.bis})`)} />

      {/* Reservierungen mit Details; der Admin bekommt Loeschknoepfe. */}
      <section className="mini-list">
        <h3><ClipboardList size={16} /> Reservierungen</h3>
        {reservierungen.length === 0 ? (
          <p className="muted">Keine Eintraege.</p>
        ) : (
          reservierungen.map((r) => (
            <p key={r.id} className="reservation-row">
              <span>{r.raumName}: {r.von}-{r.bis} ({r.reserviertVon})</span>
              {istAdmin && (
                <button
                  type="button"
                  className="icon-button danger"
                  title="Reservierung loeschen (Admin)"
                  aria-label={`Reservierung ${r.id} loeschen`}
                  onClick={() => onDeleteReservierung(r.id)}
                >
                  <Trash2 size={14} />
                </button>
              )}
            </p>
          ))
        )}
      </section>
    </aside>
  );
}

// Live-Speiseplan der Mensa kostBar direkt im Raum-Panel (EPIC 5 trifft EPIC 2).
function MensaPanel({ mensa, historie }) {
  return (
    <section className="mini-list">
      <h3><Soup size={16} /> Speiseplan heute</h3>
      <p className="muted">{mensa.quelle}</p>
      {mensa.gerichte.length === 0 ? (
        <p className="muted">Noch keine Daten - starte die Simulation im Bereich ÖPNV.</p>
      ) : (
        mensa.gerichte.map((gericht) => (
          <p key={gericht.name} className="reservation-row">
            <span>{gericht.name}</span>
            <b>{euro(gericht.preis)}</b>
          </p>
        ))
      )}
      <p className="muted">
        <History size={13} /> Historie: {historie.anzahlGesamt} gespeicherte Gerichte in der Datenbank
      </p>
    </section>
  );
}

// Formular fuer eine neue Reservierung.
function ReserveForm({ profil, onReserve }) {
  return (
    <form className="reserve-form" onSubmit={onReserve}>
      <label>
        Von
        <input name="von" type="time" defaultValue="12:00" required />
      </label>
      <label>
        Bis
        <input name="bis" type="time" defaultValue="13:00" required />
      </label>
      <button className="primary-action" type="submit">
        <CheckCircle2 size={16} />
        Fuer {profil.rolle} reservieren
      </button>
    </form>
  );
}

// Pinwand-Seite mit echtem Speichern und Likes.
// Der Admin kann Beitraege zusaetzlich LOESCHEN (verwalten).
function PinwandView({ posts, onCreate, onLike, istAdmin, onDelete }) {
  return (
    <section className="page-grid pinwand-grid">
      <div className="workspace">
        <SectionTitle kicker="Observer-Muster" title="Virtuelle Pinwand" />
        <form className="post-form" onSubmit={onCreate}>
          <div className="form-row">
            <label>
              Kategorie
              <select name="kategorie" defaultValue="Campus">
                <option>Campus</option>
                <option>Wartung</option>
                <option>Lerngruppe</option>
                <option>Betreuung</option>
              </select>
            </label>
            <label>
              Titel
              <input name="titel" placeholder="Kurzer Titel" required />
            </label>
          </div>
          <label>
            Inhalt
            <textarea name="inhalt" placeholder="Was soll die Campus-Community wissen?" required />
          </label>
          <button className="primary-action" type="submit">
            <Send size={16} />
            Post speichern
          </button>
        </form>
      </div>

      <div className="post-list">
        {posts.map((post) => (
          <article className="post-card" key={post.id}>
            <span className="badge">{post.kategorie}</span>
            <h3>{post.titel}</h3>
            <p>{post.inhalt}</p>
            <footer>
              <span>{post.autor} - {post.rolle} - {kurzZeit(post.erstelltAm)}</span>
              <span className="post-actions">
                {istAdmin && (
                  <button
                    type="button"
                    className="icon-button danger"
                    title="Post loeschen (Admin)"
                    aria-label={`Post ${post.titel} loeschen`}
                    onClick={() => onDelete(post.id)}
                  >
                    <Trash2 size={15} />
                  </button>
                )}
                <button type="button" className="like-button" onClick={() => onLike(post.id)}>
                  <Heart size={15} />
                  {post.likes}
                </button>
              </span>
            </footer>
          </article>
        ))}
      </div>
    </section>
  );
}

// OePNV-Seite: Live-Abfahrten ab "Dortmund Universitaet S" + Start der
// Fremddaten-Simulation. (Der Mensa-Speiseplan ist zur Mensa kostBar auf der
// Campus-Karte umgezogen - dort gehoert er fachlich hin.)
function DatenView({ transit, onSimulation, busy }) {
  return (
    <section className="workspace">
      <div className="section-heading">
        <SectionTitle kicker="Fremdsystem -> MQTT -> Live-Abfahrten" title="ÖPNV ab Dortmund Universität S" />
        <button className="primary-action" type="button" onClick={onSimulation} disabled={busy}>
          <RefreshCcw size={16} />
          Simulation starten
        </button>
      </div>

      <div className="data-grid">
        <DataPanel title={`Abfahrten - ${transit.haltestelle || "Dortmund Universität S"}`} icon={Navigation} subtitle={transit.quelle} wide>
          {(transit.abfahrten ?? []).map((abfahrt) => (
            <DataRow key={`${abfahrt.linie}-${abfahrt.zeit}`} title={abfahrt.linie} value={abfahrt.zeit} subtitle={abfahrt.ziel} />
          ))}
        </DataPanel>
      </div>
    </section>
  );
}

// Einstellungen: zeigt das eingeloggte Konto (read-only) + UI-Schalter.
function EinstellungenView({ profil, prefs, onPrefs, onLogout }) {
  return (
    <section className="workspace narrow">
      <SectionTitle kicker="Profil" title="Einstellungen" />
      <form className="settings-form" onSubmit={(e) => e.preventDefault()}>
        <label>
          E-Mail (Login)
          <input value={profil.email} readOnly />
        </label>
        <label>
          Name
          <input value={profil.name} readOnly />
        </label>
        <label>
          Rolle
          <input value={profil.rolle} readOnly />
        </label>
        <label className="toggle-row">
          <input
            type="checkbox"
            checked={prefs.standortTeilen}
            onChange={(e) => onPrefs({ ...prefs, standortTeilen: e.target.checked })}
          />
          Position auf der Karte anzeigen
        </label>
        <label className="toggle-row">
          <input
            type="checkbox"
            checked={prefs.benachrichtigungen}
            onChange={(e) => onPrefs({ ...prefs, benachrichtigungen: e.target.checked })}
          />
          Neue Pinwand-Posts melden
        </label>
        <button type="button" className="primary-action danger" onClick={onLogout}>
          <LogOut size={16} />
          Abmelden
        </button>
      </form>
      <p className="muted">Name und Rolle stammen aus deinem Konto und werden beim Reservieren und Posten verwendet.</p>
    </section>
  );
}

// Login- und Registrier-Seite (wird gezeigt, solange niemand eingeloggt ist).
function AuthView({ onLogin, onRegister }) {
  const [modus, setModus] = useState("login"); // "login" oder "registrieren"
  const [fehler, setFehler] = useState("");
  const [busy, setBusy] = useState(false);

  async function absenden(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setFehler("");
    setBusy(true);
    try {
      if (modus === "login") {
        await onLogin(form.get("email"), form.get("passwort"));
      } else {
        await onRegister({
          email: form.get("email"),
          name: form.get("name"),
          rolle: form.get("rolle"),
          passwort: form.get("passwort")
        });
      }
    } catch (error) {
      setFehler(error.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">Virtual College</p>
        <h1>Digitaler Campus</h1>
        <p className="muted">Bitte melde dich an, um den Campus zu nutzen.</p>

        <div className="auth-switch">
          <button type="button" className={modus === "login" ? "active" : ""} onClick={() => { setModus("login"); setFehler(""); }}>
            <LogIn size={15} /> Login
          </button>
          <button type="button" className={modus === "registrieren" ? "active" : ""} onClick={() => { setModus("registrieren"); setFehler(""); }}>
            <UserPlus size={15} /> Registrieren
          </button>
        </div>

        <form className="settings-form" onSubmit={absenden}>
          {modus === "registrieren" && (
            <>
              <label>
                Name
                <input name="name" placeholder="Vor- und Nachname" required />
              </label>
              <label>
                Rolle
                {/* Admin ist bewusst NICHT waehlbar - den gibt es nur als
                    eingerichtetes Demo-Konto (admin@fh-dortmund.de). */}
                <select name="rolle" defaultValue="Student">
                  <option>Student</option>
                  <option>Professor</option>
                </select>
              </label>
            </>
          )}
          <label>
            E-Mail
            <input name="email" type="email" placeholder="name@fh-dortmund.de" required />
          </label>
          <label>
            Passwort
            <input name="passwort" type="password" placeholder="mindestens 6 Zeichen" required minLength={6} />
          </label>

          {fehler && <p className="auth-error">{fehler}</p>}

          <button className="primary-action" type="submit" disabled={busy}>
            {modus === "login" ? <LogIn size={16} /> : <UserPlus size={16} />}
            {modus === "login" ? "Einloggen" : "Konto erstellen"}
          </button>
        </form>

        <p className="muted small-hint">Demo-Konto zum Ausprobieren: <b>student@fh-dortmund.de</b> / <b>passwort123</b></p>
      </section>
    </div>
  );
}

// Wiederverwendbare Ueberschrift.
function SectionTitle({ kicker, title }) {
  return (
    <div className="section-title">
      <p className="eyebrow">{kicker}</p>
      <h2>{title}</h2>
    </div>
  );
}

// Kleines Kennzahlen-Element.
function Metric({ label, value }) {
  return (
    <div className="metric-row">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

// Ausstattung als Tags.
function TagList({ items }) {
  return (
    <div className="tag-list">
      {items.map((item) => <span key={item}>{item}</span>)}
    </div>
  );
}

// Kleine Liste fuer Sidepanel-Inhalte.
function MiniList({ title, icon: Icon, items }) {
  return (
    <section className="mini-list">
      <h3><Icon size={16} /> {title}</h3>
      {items.length === 0 ? <p className="muted">Keine Eintraege.</p> : items.map((item) => <p key={item}>{item}</p>)}
    </section>
  );
}

// Kachel fuer Live-Daten.
function DataPanel({ title, icon: Icon, subtitle, wide = false, children }) {
  return (
    <section className={`data-panel ${wide ? "wide" : ""}`}>
      <h3><Icon size={18} /> {title}</h3>
      <p className="muted">{subtitle}</p>
      <div className="data-list">{children}</div>
    </section>
  );
}

// Eine Zeile in einer Datenkachel.
function DataRow({ title, subtitle, value }) {
  return (
    <article className="data-row">
      <div>
        <strong>{title}</strong>
        <small>{subtitle}</small>
      </div>
      <span>{value}</span>
    </article>
  );
}

// Status wird in CSS-Klassen uebersetzt.
