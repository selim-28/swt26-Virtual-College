/*
 * REINE HILFSFUNKTIONEN fuer die Campus-Karte (EPIC 2).
 *
 * Bewusst OHNE React/Leaflet, damit man sie einzeln (Unit-Test) pruefen kann.
 * Genutzt werden sie in App.jsx von der Live-Karte und der Offline-Ersatzkarte.
 */

/*
 * escapeHtml: macht Text sicher, bevor er per innerHTML in einen Karten-Pin
 * geschrieben wird. Sonderzeichen wie < oder & werden in ihre HTML-Form
 * umgewandelt, damit ein Raumname kein Layout/Script "einschleusen" kann.
 * Beispiel:  <b>  ->  &lt;b&gt;
 */
export function escapeHtml(text) {
  return String(text).replace(/[&<>'"]/g, (zeichen) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
  })[zeichen]);
}

/*
 * richtungsWinkel: berechnet die Himmelsrichtung (Kompass-Winkel in Grad) von
 * einem Punkt A zu einem Punkt B. Damit werden die Richtungspfeile entlang der
 * Fussroute gedreht (US 2.3). 0deg = Norden, 90deg = Osten, -90deg = Westen,
 * 180deg = Sueden. Punkte sind [Breitengrad, Laengengrad].
 */
export function richtungsWinkel([lat1, lng1], [lat2, lng2]) {
  const y = Math.sin((lng2 - lng1) * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180);
  const x = Math.cos(lat1 * Math.PI / 180) * Math.sin(lat2 * Math.PI / 180)
    - Math.sin(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180)
    * Math.cos((lng2 - lng1) * Math.PI / 180);
  return Math.atan2(y, x) * 180 / Math.PI;
}

/*
 * statusClass: uebersetzt den Raum-Status (Text) in eine CSS-Klasse, damit der
 * Pin/Marker die passende Farbe bekommt (US 2.1 "ausgewaehlter Raum / Status").
 * Reihenfolge beachten: "frei" wird zuerst geprueft.
 */
export function statusClass(status) {
  if (status.includes("frei")) return "is-free";
  if (status.includes("bald")) return "is-soon";
  if (status.includes("geo")) return "is-open";
  return "is-busy";
}

/*
 * AUTOMATISCHE AKTUALISIERUNG (Polling) – "letzte Meile" zu EPIC 7.
 *
 * MQTT bringt frische Daten (Mensa, OEPNV, ...) nur bis ins backend_receive.
 * Das Frontend spricht aber HTTP und muss aktiv NACHFRAGEN. Statt den Knopf
 * "Aktualisieren" von Hand zu druecken, fragt die App in festen Abstaenden
 * selbst nach. POLL_INTERVALL_MS legt fest, WIE OFT (hier alle 8 Sekunden).
 */
export const POLL_INTERVALL_MS = 8000;

/*
 * sollAktualisieren: entscheidet bei jedem Takt, OB gerade automatisch neu
 * geladen werden soll. Bewusst sparsam, um unnoetige Anfragen zu vermeiden:
 *   - nur wenn ein Nutzer eingeloggt ist (sonst gibt es nichts zu laden),
 *   - nur wenn der Browser-Tab sichtbar ist (kein Polling im Hintergrund),
 *   - nicht, wenn gerade schon eine Ladung laeuft (kein "Ueberholen").
 * Reine Funktion ohne React/Timer -> einzeln testbar.
 */
export function sollAktualisieren({ eingeloggt, seiteSichtbar, ladeLaeuft }) {
  return Boolean(eingeloggt) && Boolean(seiteSichtbar) && !ladeLaeuft;
}

/*
 * ROLLENREGEL fuer die Reservierung (EPIC 3, US 3.2) - Spiegel der Backend-Regel:
 *   - reservierbare Raeume (Labore, Seminarraeume, PC-Pool): alle Angemeldeten
 *   - Hoersaele (typ "Vorlesung"): nur Professor:innen und Admins
 *   - Mensa/Bibliothek: niemand (offene Orte)
 * Das Frontend blendet damit das Formular passend ein/aus; die verbindliche
 * Pruefung macht IMMER das Backend (hier nur Anzeige-Logik).
 */
export function darfRaumReservieren(raum, rolle) {
  if (!raum) return false;
  if (raum.reservierbar) return true;
  return raum.typ === "Vorlesung" && (rolle === "Professor" || rolle === "Admin");
}

/*
 * BENACHRICHTIGUNG ueber neue Pinwand-Beitraege (EPIC 4, US 4.3).
 *
 * Wird bei jeder (auch automatischen) Aktualisierung aufgerufen und vergleicht
 * die alte mit der neuen Post-Liste. Gibt einen Meldetext zurueck - oder null,
 * wenn nichts zu melden ist:
 *   - null, wenn der Nutzer Benachrichtigungen ausgeschaltet hat
 *     ("nicht registrierte Beobachter erhalten keine Benachrichtigung"),
 *   - null beim allerersten Laden (alles waere "neu" - das waere nur Laerm),
 *   - null, wenn keine neuen Beitraege dazugekommen sind.
 * Reine Funktion ohne React -> einzeln testbar.
 */
export function neuerPostHinweis(vorherigePosts, aktuellePosts, benachrichtigungenAktiv) {
  if (!benachrichtigungenAktiv) return null;
  if (!Array.isArray(vorherigePosts) || vorherigePosts.length === 0) return null;
  const bekannt = new Set(vorherigePosts.map((p) => p.id));
  const neue = (aktuellePosts ?? []).filter((p) => !bekannt.has(p.id));
  if (neue.length === 0) return null;
  const erster = neue[0];
  return neue.length === 1
    ? `🔔 Neuer Pinwand-Beitrag: "${erster.titel}" von ${erster.autor}`
    : `🔔 ${neue.length} neue Pinwand-Beiträge, z. B. "${erster.titel}" von ${erster.autor}`;
}
