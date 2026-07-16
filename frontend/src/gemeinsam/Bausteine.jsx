// Gemeinsame UI-Bausteine, die mehrere Fachmodule nutzen:
// App-Kopf, Statusleiste und kleine Anzeige-Elemente (Titel, Kennzahl, ...).

import { Bus, LogOut, MapPin, MessageSquarePlus, RefreshCcw, Settings, User } from "lucide-react";

// Kopfbereich mit Navigation und eingeloggtem Nutzer.
export function Header({ view, onView, konto, onLogout }) {
  const tabs = [
    ["campus", "Live Campus", MapPin],
    ["pinwand", "Pinwand", MessageSquarePlus],
    ["daten", "ÖPNV", Bus],
    ["einstellungen", "Einstellungen", Settings]
  ];

  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">Virtual College</p>
        <h1>Digitaler Campus</h1>
      </div>
      <nav className="tabs" aria-label="Hauptbereiche">
        {tabs.map(([key, label, Icon]) => (
          <button
            className={`tab ${view === key ? "active" : ""}`}
            key={key}
            onClick={() => onView(key)}
            type="button"
          >
            <Icon size={16} />
            {label}
          </button>
        ))}
      </nav>
      <div className="account">
        <span className="account-name" title={konto.email}>
          <User size={15} /> {konto.name}
          <small>{konto.rolle}</small>
        </span>
        <button type="button" className="icon-button" onClick={onLogout} aria-label="Abmelden" title="Abmelden">
          <LogOut size={18} />
        </button>
      </div>
    </header>
  );
}

// Kleine Statusleiste fuer Ladezustand und manuelles Aktualisieren.
export function StatusBar({ busy, status, onRefresh }) {
  return (
    <section className="statusbar">
      <span className={busy ? "pulse" : ""}>{status}</span>
      <button type="button" className="icon-button" onClick={onRefresh} aria-label="Daten aktualisieren">
        <RefreshCcw size={18} />
      </button>
    </section>
  );
}

// Wiederverwendbare Ueberschrift.
export function SectionTitle({ kicker, title }) {
  return (
    <div className="section-title">
      <p className="eyebrow">{kicker}</p>
      <h2>{title}</h2>
    </div>
  );
}

// Kleines Kennzahlen-Element.
export function Metric({ label, value }) {
  return (
    <div className="metric-row">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

// Ausstattung als Tags.
export function TagList({ items }) {
  return (
    <div className="tag-list">
      {items.map((item) => <span key={item}>{item}</span>)}
    </div>
  );
}

// Kleine Liste fuer Sidepanel-Inhalte.
export function MiniList({ title, icon: Icon, items }) {
  return (
    <section className="mini-list">
      <h3><Icon size={16} /> {title}</h3>
      {items.length === 0 ? <p className="muted">Keine Eintraege.</p> : items.map((item) => <p key={item}>{item}</p>)}
    </section>
  );
}

// Kachel fuer Live-Daten.
export function DataPanel({ title, icon: Icon, subtitle, wide = false, children }) {
  return (
    <section className={`data-panel ${wide ? "wide" : ""}`}>
      <h3><Icon size={18} /> {title}</h3>
      <p className="muted">{subtitle}</p>
      <div className="data-list">{children}</div>
    </section>
  );
}

// Eine Zeile in einer Datenkachel.
export function DataRow({ title, subtitle, value }) {
  return (
    <article className="data-row">
      <div>
        <strong>{title}</strong>
        <small>{subtitle}</small>
      </div>
      <span>{value}</span>
    </article>
  );
}
