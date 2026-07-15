/*
 * Unit-Tests fuer die Campus-Karten-Hilfsfunktionen (EPIC 2).
 * Laeuft OHNE Zusatz-Bibliothek mit Nodes eingebautem Test-Runner:
 *   npm test        (bzw. node --test)
 */
import { test } from "node:test";
import assert from "node:assert/strict";

import { escapeHtml, richtungsWinkel, statusClass, POLL_INTERVALL_MS, sollAktualisieren, darfRaumReservieren, neuerPostHinweis } from "./campusUtils.mjs";

// --- escapeHtml: schuetzt Karten-Pins vor gefaehrlichem Text (US 2.1) ---

test("escapeHtml wandelt spitze Klammern und & um", () => {
  assert.equal(escapeHtml("<b>Raum</b>"), "&lt;b&gt;Raum&lt;/b&gt;");
  assert.equal(escapeHtml("A & B"), "A &amp; B");
});

test("escapeHtml maskiert Anfuehrungszeichen", () => {
  assert.equal(escapeHtml("\"x\" 'y'"), "&quot;x&quot; &#39;y&#39;");
});

test("escapeHtml laesst harmlosen Text unveraendert", () => {
  assert.equal(escapeHtml("Audimax 42"), "Audimax 42");
});

// --- richtungsWinkel: dreht die Richtungspfeile der Fussroute (US 2.3) ---

const nahe = (ist, soll) => assert.ok(Math.abs(ist - soll) < 1e-6, `${ist} != ${soll}`);

test("richtungsWinkel: nach Norden = 0 Grad", () => {
  nahe(richtungsWinkel([0, 0], [1, 0]), 0);
});

test("richtungsWinkel: nach Osten = 90 Grad", () => {
  nahe(richtungsWinkel([0, 0], [0, 1]), 90);
});

test("richtungsWinkel: nach Westen = -90 Grad", () => {
  nahe(richtungsWinkel([0, 0], [0, -1]), -90);
});

test("richtungsWinkel: nach Sueden = 180 Grad", () => {
  nahe(Math.abs(richtungsWinkel([1, 0], [0, 0])), 180);
});

// --- statusClass: waehlt die Farbe/CSS-Klasse je Raum-Status (US 2.1) ---

test("statusClass erkennt freie Raeume", () => {
  assert.equal(statusClass("frei"), "is-free");
});

test("statusClass erkennt 'bald' (wenn nicht schon frei)", () => {
  assert.equal(statusClass("bald verfuegbar"), "is-soon");
});

test("statusClass erkennt 'geo'", () => {
  assert.equal(statusClass("geo"), "is-open");
});

test("statusClass faellt sonst auf belegt zurueck", () => {
  assert.equal(statusClass("belegt"), "is-busy");
});

test("statusClass: 'frei' hat Vorrang vor 'bald'", () => {
  // "bald frei" enthaelt beides -> "frei" wird zuerst geprueft.
  assert.equal(statusClass("bald frei"), "is-free");
});

// --- sollAktualisieren: steuert das automatische Nachladen (Polling / EPIC 7) ---

test("sollAktualisieren: eingeloggt, sichtbar, nichts laeuft -> ja", () => {
  assert.equal(
    sollAktualisieren({ eingeloggt: true, seiteSichtbar: true, ladeLaeuft: false }),
    true
  );
});

test("sollAktualisieren: nicht eingeloggt -> nein", () => {
  assert.equal(
    sollAktualisieren({ eingeloggt: false, seiteSichtbar: true, ladeLaeuft: false }),
    false
  );
});

test("sollAktualisieren: Tab im Hintergrund -> nein", () => {
  assert.equal(
    sollAktualisieren({ eingeloggt: true, seiteSichtbar: false, ladeLaeuft: false }),
    false
  );
});

test("sollAktualisieren: es laeuft schon eine Ladung -> nein (kein Ueberholen)", () => {
  assert.equal(
    sollAktualisieren({ eingeloggt: true, seiteSichtbar: true, ladeLaeuft: true }),
    false
  );
});

test("POLL_INTERVALL_MS ist ein sinnvoller, positiver Abstand", () => {
  assert.ok(POLL_INTERVALL_MS >= 1000, "mindestens 1 Sekunde, um den Server nicht zu fluten");
});

// --- darfRaumReservieren: Rollenregel der Reservierung (EPIC 3 / US 3.2) ---

const labor = { typ: "Labor", reservierbar: true };
const hoersaal = { typ: "Vorlesung", reservierbar: false };
const mensa = { typ: "Mensa", reservierbar: false };

test("darfRaumReservieren: Student darf reservierbare Raeume buchen", () => {
  assert.equal(darfRaumReservieren(labor, "Student"), true);
});

test("darfRaumReservieren: Student darf KEINEN Hoersaal buchen", () => {
  assert.equal(darfRaumReservieren(hoersaal, "Student"), false);
});

test("darfRaumReservieren: Professor und Admin duerfen Hoersaele buchen", () => {
  assert.equal(darfRaumReservieren(hoersaal, "Professor"), true);
  assert.equal(darfRaumReservieren(hoersaal, "Admin"), true);
});

test("darfRaumReservieren: die Mensa darf NIEMAND buchen (auch Admin nicht)", () => {
  assert.equal(darfRaumReservieren(mensa, "Admin"), false);
  assert.equal(darfRaumReservieren(mensa, "Student"), false);
});

test("darfRaumReservieren: ohne Raum keine Reservierung", () => {
  assert.equal(darfRaumReservieren(null, "Admin"), false);
});

// --- neuerPostHinweis: Benachrichtigung ueber neue Beitraege (EPIC 4 / US 4.3) ---

const alteListe = [{ id: 1, titel: "Alt", autor: "Anna" }];

test("neuerPostHinweis meldet einen neuen Beitrag mit Titel und Autor", () => {
  const neu = [{ id: 2, titel: "Mensa zu", autor: "Verwaltung" }, ...alteListe];
  const meldung = neuerPostHinweis(alteListe, neu, true);
  assert.ok(meldung.includes("Mensa zu"));
  assert.ok(meldung.includes("Verwaltung"));
});

test("neuerPostHinweis fasst mehrere neue Beitraege zusammen", () => {
  const neu = [{ id: 3, titel: "A", autor: "X" }, { id: 2, titel: "B", autor: "Y" }, ...alteListe];
  const meldung = neuerPostHinweis(alteListe, neu, true);
  assert.ok(meldung.includes("2 neue"));
});

test("neuerPostHinweis: abgeschaltete Benachrichtigungen -> keine Meldung", () => {
  const neu = [{ id: 2, titel: "Mensa zu", autor: "V" }, ...alteListe];
  assert.equal(neuerPostHinweis(alteListe, neu, false), null);
});

test("neuerPostHinweis: beim allerersten Laden wird nichts gemeldet", () => {
  assert.equal(neuerPostHinweis([], alteListe, true), null);
});

test("neuerPostHinweis: keine neuen Beitraege -> keine Meldung", () => {
  assert.equal(neuerPostHinweis(alteListe, alteListe, true), null);
});
