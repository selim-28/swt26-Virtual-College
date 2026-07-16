// Hauptkomponente: haelt den App-Zustand, spricht ueber die gemeinsame
// API-Schicht mit den Backends und verdrahtet die Fachmodule.
//
// Die Ansichten selbst liegen - wie die Backend-Pakete (Vertical Slicing) -
// in einem Ordner pro Fachthema:
//   auth/    campus/    mensa/    pinwand/    transit/    einstellungen/
// Gemeinsames (API, Formatierung, UI-Bausteine) liegt in gemeinsam/.

import { useEffect, useMemo, useRef, useState } from "react";
import { API, apiJson, gespeichertesKonto, merkeKonto, vergissKonto } from "./gemeinsam/api.js";
import { POLL_INTERVALL_MS, sollAktualisieren, neuerPostHinweis } from "./campusUtils.mjs";
import { Header, StatusBar } from "./gemeinsam/Bausteine.jsx";
import AuthView from "./auth/AuthView.jsx";
import CampusView from "./campus/CampusView.jsx";
import PinwandView from "./pinwand/PinwandView.jsx";
import DatenView from "./transit/DatenView.jsx";
import EinstellungenView from "./einstellungen/EinstellungenView.jsx";

// Persoenliche UI-Einstellungen (gehoeren NICHT zum Konto, nur zur Anzeige).
const START_PREFS = {
  standortTeilen: true,
  benachrichtigungen: true
};

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
