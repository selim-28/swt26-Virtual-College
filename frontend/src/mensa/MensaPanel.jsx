// Modul "mensa": Live-Speiseplan der Mensa kostBar direkt im Raum-Panel
// (EPIC 5 trifft EPIC 2). Die Daten kommen ueber MQTT ins backend_receive.

import { History, Soup } from "lucide-react";
import { euro } from "../gemeinsam/format.js";

export default function MensaPanel({ mensa, historie }) {
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
