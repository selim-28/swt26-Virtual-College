/*
 * Reine Hilfsfunktionen der Campus-Karte, bewusst ohne React und Leaflet,
 * damit sie einzeln testbar bleiben.
 */

/** Maskiert Sonderzeichen, bevor Text per innerHTML in einen Karten-Pin kommt. */
export function escapeHtml(text) {
  return String(text).replace(/[&<>'"]/g, (zeichen) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
  })[zeichen]);
}

/**
 * Kompasswinkel in Grad von Punkt A nach Punkt B, fuer die Richtungspfeile
 * entlang der Fussroute (US 2.3). Punkte sind [Breitengrad, Laengengrad].
 */
export function richtungsWinkel([lat1, lng1], [lat2, lng2]) {
  const y = Math.sin((lng2 - lng1) * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180);
  const x = Math.cos(lat1 * Math.PI / 180) * Math.sin(lat2 * Math.PI / 180)
    - Math.sin(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180)
    * Math.cos((lng2 - lng1) * Math.PI / 180);
  return Math.atan2(y, x) * 180 / Math.PI;
}

/** Uebersetzt den Raumstatus in die CSS-Klasse des Markers. */
export function statusClass(status) {
  if (status.includes("frei")) return "is-free";
  if (status.includes("bald")) return "is-soon";
  if (status.includes("geo")) return "is-open";
  return "is-busy";
}

/**
 * Taktrate der automatischen Aktualisierung. MQTT liefert frische Daten nur bis
 * ins Backend, das Frontend muss aktiv nachfragen.
 */
export const POLL_INTERVALL_MS = 8000;

/**
 * Entscheidet, ob im aktuellen Takt nachgeladen wird: nur mit angemeldetem
 * Nutzer, nur bei sichtbarem Tab und nicht waehrend einer laufenden Ladung.
 */
export function sollAktualisieren({ eingeloggt, seiteSichtbar, ladeLaeuft }) {
  return Boolean(eingeloggt) && Boolean(seiteSichtbar) && !ladeLaeuft;
}

/**
 * Spiegelt die Rollenregel des Backends, um das Formular passend ein- oder
 * auszublenden. Verbindlich geprueft wird immer serverseitig.
 */
export function darfRaumReservieren(raum, rolle) {
  if (!raum) return false;
  if (raum.reservierbar) return true;
  return raum.typ === "Vorlesung" && (rolle === "Professor" || rolle === "Admin");
}

/**
 * Vergleicht alte und neue Beitragsliste und liefert den Hinweistext fuer neue
 * Beitraege (US 4.3) - oder null, wenn Benachrichtigungen aus sind, es sich um
 * den ersten Ladevorgang handelt oder nichts dazugekommen ist.
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
