// Modul "mensa": Speiseplan der Mensa direkt im Raum-Panel.
// Die Daten kommen ueber MQTT in backend_receive an.

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
