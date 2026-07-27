// Hauptkomponente: haelt den App-Zustand, spricht ueber die gemeinsame
// API-Schicht mit den Backends und verdrahtet die Fachmodule. Die Ansichten
// liegen wie die Backend-Pakete in je einem Ordner pro Fachthema, Gemeinsames
// in gemeinsam/.

import { useEffect, useMemo, useRef, useState } from "react";
import { API, apiJson, gespeichertesKonto, merkeKonto, vergissKonto } from "./gemeinsam/api.js";
import { POLL_INTERVALL_MS, sollAktualisieren, neuerPostHinweis } from "./campusUtils.mjs";
import { Header, StatusBar } from "./gemeinsam/Bausteine.jsx";
import AuthView from "./auth/AuthView.jsx";
import CampusView from "./campus/CampusView.jsx";
import PinwandView from "./pinwand/PinwandView.jsx";
import DatenView from "./transit/DatenView.jsx";
import EinstellungenView from "./einstellungen/EinstellungenView.jsx";

// Persoenliche Anzeigeeinstellungen; sie gehoeren nicht zum Benutzerkonto.
const START_PREFS = {
  standortTeilen: true,
  benachrichtigungen: true
};

export default function App() {
  const [view, setView] = useState("campus");

  // null bedeutet: nicht eingeloggt.
  const [konto, setKonto] = useState(gespeichertesKonto);
  const [prefs, setPrefs] = useState(START_PREFS);
  // Konto und Einstellungen zusammen ergeben das Profil der Fachansichten.
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
  // Als Ref statt State, damit der Polling-Takt den aktuellen Wert sieht und
  // sich Ladungen nicht ueberholen.
  const ladeLaeuftRef = useRef(false);

  // Der Polling-Takt laeuft in einer aelteren Closure; ueber die Refs sieht er
  // trotzdem die aktuelle Beitragsliste und die aktuellen Einstellungen.
  const postsRef = useRef([]);
  const prefsRef = useRef(prefs);
  prefsRef.current = prefs;

  // Pflegt State und Ref gemeinsam.
  function uebernimmPosts(liste) {
    postsRef.current = liste;
    setPosts(liste);
  }

  const selectedRoom = useMemo(
    () => campus.raeume.find((raum) => raum.id === selectedRoomId) ?? null,
    [campus.raeume, selectedRoomId]
  );

  // Beim Start das gespeicherte Token pruefen: gueltig heisst eingeloggt bleiben
  // und Daten laden, sonst abmelden.
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
    // Bewusst nur einmal beim Mounten pruefen.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Automatische Aktualisierung: MQTT liefert frische Daten nur bis ins Backend,
  // das Frontend fragt in festen Abstaenden nach - nur angemeldet und nur bei
  // sichtbarem Tab.
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
    // Timer stoppen, wenn sich das Konto aendert, etwa beim Logout.
    return () => window.clearInterval(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [konto]);

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

  // Legt ein Konto an und meldet direkt an.
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

  // Macht das Token am Backend ungueltig und vergisst es lokal.
  function logout() {
    apiJson(API.authLogout, { method: "POST" }).catch(() => {});
    vergissKonto();
    setKonto(null);
    setView("campus");
    setStatus("Abgemeldet");
  }

  // Laedt alle Daten der Startseite. Mit stumm=true laeuft die automatische
  // Aktualisierung ohne Ladeanzeige und ohne Fehlermeldung.
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
      // US 4.3: vor dem Uebernehmen pruefen, ob neue Beitraege dazugekommen sind.
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
      // Beim automatischen Aktualisieren keine Fehlermeldung zeigen, der naechste
      // Takt versucht es ohnehin erneut.
      if (!stumm) setStatus(`Fehler beim Laden: ${error.message}`);
    } finally {
      ladeLaeuftRef.current = false;
      if (!stumm) setBusy(false);
    }
  }

  // Startet das Fremdsystem, wartet kurz auf MQTT und laedt danach neu.
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

  async function postErstellen(event) {
    event.preventDefault();
    // Das Formularelement jetzt sichern: nach einem await setzt React
    // event.currentTarget auf null.
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
      formEl.reset();
      uebernimmPosts(await apiJson(API.posts));
      setStatus("Post gespeichert und Pinwand aktualisiert");
    } catch (error) {
      setStatus(`Post konnte nicht gespeichert werden: ${error.message}`);
    } finally {
      setBusy(false);
    }
  }

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

  // Loeschen ist im Backend auf die Admin-Rolle beschraenkt.
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

  // Nur der Admin sieht die Verwaltungsfunktionen.
  const istAdmin = konto?.rolle === "Admin";

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
