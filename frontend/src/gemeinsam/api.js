// Gemeinsame API-Schicht: Adressen, Token-Verwaltung und fetch-Helfer.
// Alle Fachmodule sprechen ausschliesslich hierueber mit den Backends.

// Nginx leitet diese relativen Pfade an die passenden Backends weiter.
export const API = {
  campus: "/api/campus/karte",
  reservierungen: "/api/campus/reservierungen",
  posts: "/api/pinwand/posts",
  mensa: "/api/mensa/speiseplan",
  historie: "/api/mensa/historie",
  transit: "/api/transit/abfahrten",
  simulation: "/api/simulation/start",
  authRegister: "/api/auth/registrieren",
  authLogin: "/api/auth/login",
  authIch: "/api/auth/ich",
  authLogout: "/api/auth/logout"
};

// Token-Verwaltung im Browser; bleibt auch nach einem Reload erhalten.
export function gespeichertesToken() {
  try { return localStorage.getItem("vc_token"); } catch { return null; }
}
export function gespeichertesKonto() {
  try { const s = localStorage.getItem("vc_konto"); return s ? JSON.parse(s) : null; } catch { return null; }
}
export function merkeKonto(data) {
  try {
    localStorage.setItem("vc_token", data.token);
    localStorage.setItem("vc_konto", JSON.stringify(data));
  } catch { /* Ohne localStorage gilt die Anmeldung nur fuer diese Sitzung. */ }
}
export function vergissKonto() {
  try { localStorage.removeItem("vc_token"); localStorage.removeItem("vc_konto"); } catch { /* egal */ }
}

// Haengt das Login-Token als "Authorization: Bearer <token>" an und liest bei
// Fehlern die Klartextmeldung des Backends aus.
export async function apiJson(url, options = {}) {
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
  // Manche Endpunkte, etwa Logout, antworten ohne Inhalt.
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}
