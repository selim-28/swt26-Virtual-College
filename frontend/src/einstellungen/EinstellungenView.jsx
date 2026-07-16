// Modul "einstellungen": Konto-Anzeige (read-only) + persoenliche UI-Schalter.

import { LogOut } from "lucide-react";
import { SectionTitle } from "../gemeinsam/Bausteine.jsx";

// Einstellungen: zeigt das eingeloggte Konto (read-only) + UI-Schalter.
export default function EinstellungenView({ profil, prefs, onPrefs, onLogout }) {
  return (
    <section className="workspace narrow">
      <SectionTitle kicker="Profil" title="Einstellungen" />
      <form className="settings-form" onSubmit={(e) => e.preventDefault()}>
        <label>
          E-Mail (Login)
          <input value={profil.email} readOnly />
        </label>
        <label>
          Name
          <input value={profil.name} readOnly />
        </label>
        <label>
          Rolle
          <input value={profil.rolle} readOnly />
        </label>
        <label className="toggle-row">
          <input
            type="checkbox"
            checked={prefs.standortTeilen}
            onChange={(e) => onPrefs({ ...prefs, standortTeilen: e.target.checked })}
          />
          Position auf der Karte anzeigen
        </label>
        <label className="toggle-row">
          <input
            type="checkbox"
            checked={prefs.benachrichtigungen}
            onChange={(e) => onPrefs({ ...prefs, benachrichtigungen: e.target.checked })}
          />
          Neue Pinwand-Posts melden
        </label>
        <button type="button" className="primary-action danger" onClick={onLogout}>
          <LogOut size={16} />
          Abmelden
        </button>
      </form>
      <p className="muted">Name und Rolle stammen aus deinem Konto und werden beim Reservieren und Posten verwendet.</p>
    </section>
  );
}
