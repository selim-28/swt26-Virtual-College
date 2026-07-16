// Modul "transit": OePNV-Live-Abfahrten + Start der Fremddaten-Simulation.

import { Navigation, RefreshCcw } from "lucide-react";
import { DataPanel, DataRow, SectionTitle } from "../gemeinsam/Bausteine.jsx";

// OePNV-Seite: Live-Abfahrten ab "Dortmund Universitaet S" + Start der
// Fremddaten-Simulation. (Der Mensa-Speiseplan ist zur Mensa kostBar auf der
// Campus-Karte umgezogen - dort gehoert er fachlich hin.)
export default function DatenView({ transit, onSimulation, busy }) {
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
