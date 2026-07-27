// Modul "campus", Teil 2: die Seite mit Karte links und Raumdetails rechts.

import { CalendarCheck, CheckCircle2, ClipboardList, Trash2 } from "lucide-react";
import { darfRaumReservieren } from "../campusUtils.mjs";
import { Metric, MiniList, SectionTitle, TagList } from "../gemeinsam/Bausteine.jsx";
import CampusMap from "./CampusKarte.jsx";
import MensaPanel from "../mensa/MensaPanel.jsx";

export default function CampusView({ campus, selectedRoom, onSelectRoom, onReserve, profil,
                                     mensa, historie, istAdmin, onDeleteReservierung }) {
  return (
    <section className="page-grid campus-grid">
      <div className="workspace">
        <SectionTitle kicker="Echter Campus Emil-Figge-Straße" title="Straßenkarte, Satellit und Navigation" />
        <CampusMap
          campus={campus}
          selectedRoom={selectedRoom}
          onSelectRoom={onSelectRoom}
          standortTeilen={profil.standortTeilen}
        />
      </div>
      <RoomPanel
        selectedRoom={selectedRoom}
        campus={campus}
        profil={profil}
        onReserve={onReserve}
        mensa={mensa}
        historie={historie}
        istAdmin={istAdmin}
        onDeleteReservierung={onDeleteReservierung}
      />
    </section>
  );
}

// Detailspalte fuer Raum, Veranstaltungen und Reservierungen. Bei der Mensa
// erscheint hier direkt der Live-Speiseplan aus den Fremddaten.
function RoomPanel({ selectedRoom, campus, profil, onReserve,
                     mensa, historie, istAdmin, onDeleteReservierung }) {
  const reservierungen = selectedRoom
    ? campus.reservierungen.filter((reservierung) => reservierung.raumId === selectedRoom.id)
    : campus.reservierungen.slice(0, 3);
  const veranstaltungen = selectedRoom
    ? campus.veranstaltungen.filter((veranstaltung) => veranstaltung.raumId === selectedRoom.id)
    : campus.veranstaltungen;

  const istMensa = selectedRoom?.typ === "Mensa";
  // Spiegelt die Rollenregel des Backends.
  const darfBuchen = darfRaumReservieren(selectedRoom, profil.rolle);

  return (
    <aside className="side-panel">
      <SectionTitle
        kicker={selectedRoom ? selectedRoom.gebaeude : "Campus"}
        title={selectedRoom ? selectedRoom.name : "Raum auswaehlen"}
      />
      {selectedRoom ? (
        <>
          <p className="muted">{selectedRoom.beschreibung}</p>
          <Metric label="Auslastung" value={`${selectedRoom.personen}/${selectedRoom.kapazitaet}`} />
          <Metric label="Status" value={selectedRoom.status} />
          <TagList items={selectedRoom.ausstattung} />

          {istMensa && <MensaPanel mensa={mensa} historie={historie} />}

          {!istMensa && darfBuchen && <ReserveForm profil={profil} onReserve={onReserve} />}

          {!istMensa && !darfBuchen && (
            // Erklaert, warum hier kein Reservierungsformular erscheint.
            <p className="muted">
              {selectedRoom.typ === "Vorlesung"
                ? `${selectedRoom.name} ist ein Hoersaal - reservieren duerfen ihn nur Professor:innen oder die Verwaltung (Admin).`
                : `${selectedRoom.name} kann nicht reserviert werden (${selectedRoom.typ}). Reservierbar sind freie Lern- und Seminarraeume.`}
            </p>
          )}
        </>
      ) : (
        <p className="muted">Klicke auf einen Raum in der Karte, um Details und Aktionen zu sehen.</p>
      )}

      <MiniList title="Veranstaltungen" icon={CalendarCheck} items={veranstaltungen.map((v) => `${v.titel} (${v.von}-${v.bis})`)} />

      {/* Der Admin bekommt zusaetzlich Loeschknoepfe. */}
      <section className="mini-list">
        <h3><ClipboardList size={16} /> Reservierungen</h3>
        {reservierungen.length === 0 ? (
          <p className="muted">Keine Eintraege.</p>
        ) : (
          reservierungen.map((r) => (
            <p key={r.id} className="reservation-row">
              <span>{r.raumName}: {r.von}-{r.bis} ({r.reserviertVon})</span>
              {istAdmin && (
                <button
                  type="button"
                  className="icon-button danger"
                  title="Reservierung loeschen (Admin)"
                  aria-label={`Reservierung ${r.id} loeschen`}
                  onClick={() => onDeleteReservierung(r.id)}
                >
                  <Trash2 size={14} />
                </button>
              )}
            </p>
          ))
        )}
      </section>
    </aside>
  );
}

function ReserveForm({ profil, onReserve }) {
  return (
    <form className="reserve-form" onSubmit={onReserve}>
      <label>
        Von
        <input name="von" type="time" defaultValue="12:00" required />
      </label>
      <label>
        Bis
        <input name="bis" type="time" defaultValue="13:00" required />
      </label>
      <button className="primary-action" type="submit">
        <CheckCircle2 size={16} />
        Fuer {profil.rolle} reservieren
      </button>
    </form>
  );
}
