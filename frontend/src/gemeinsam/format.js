// Gemeinsame Anzeige-Formatierung (Geld, Zeit) fuer alle Fachmodule.

// Format fuer Euro-Preise in der Mensa.
export function euro(value) {
  return new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" }).format(value);
}

// Kurze Uhrzeit/Datum-Anzeige fuer Posts und Reservierungen.
export function kurzZeit(isoText) {
  return new Date(isoText).toLocaleString("de-DE", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}
