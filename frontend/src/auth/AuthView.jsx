// Modul "auth": Login- und Registrier-Seite.
// Wird gezeigt, solange niemand eingeloggt ist (App.jsx entscheidet das).

import { useState } from "react";
import { LogIn, UserPlus } from "lucide-react";

// Login- und Registrier-Seite (wird gezeigt, solange niemand eingeloggt ist).
export default function AuthView({ onLogin, onRegister }) {
  const [modus, setModus] = useState("login"); // "login" oder "registrieren"
  const [fehler, setFehler] = useState("");
  const [busy, setBusy] = useState(false);

  async function absenden(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setFehler("");
    setBusy(true);
    try {
      if (modus === "login") {
        await onLogin(form.get("email"), form.get("passwort"));
      } else {
        await onRegister({
          email: form.get("email"),
          name: form.get("name"),
          rolle: form.get("rolle"),
          passwort: form.get("passwort")
        });
      }
    } catch (error) {
      setFehler(error.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-shell">
      <section className="auth-card">
        <p className="eyebrow">Virtual College</p>
        <h1>Digitaler Campus</h1>
        <p className="muted">Bitte melde dich an, um den Campus zu nutzen.</p>

        <div className="auth-switch">
          <button type="button" className={modus === "login" ? "active" : ""} onClick={() => { setModus("login"); setFehler(""); }}>
            <LogIn size={15} /> Login
          </button>
          <button type="button" className={modus === "registrieren" ? "active" : ""} onClick={() => { setModus("registrieren"); setFehler(""); }}>
            <UserPlus size={15} /> Registrieren
          </button>
        </div>

        <form className="settings-form" onSubmit={absenden}>
          {modus === "registrieren" && (
            <>
              <label>
                Name
                <input name="name" placeholder="Vor- und Nachname" required />
              </label>
              <label>
                Rolle
                {/* Admin ist bewusst NICHT waehlbar - den gibt es nur als
                    eingerichtetes Demo-Konto (admin@fh-dortmund.de). */}
                <select name="rolle" defaultValue="Student">
                  <option>Student</option>
                  <option>Professor</option>
                </select>
              </label>
            </>
          )}
          <label>
            E-Mail
            <input name="email" type="email" placeholder="name@fh-dortmund.de" required />
          </label>
          <label>
            Passwort
            <input name="passwort" type="password" placeholder="mindestens 6 Zeichen" required minLength={6} />
          </label>

          {fehler && <p className="auth-error">{fehler}</p>}

          <button className="primary-action" type="submit" disabled={busy}>
            {modus === "login" ? <LogIn size={16} /> : <UserPlus size={16} />}
            {modus === "login" ? "Einloggen" : "Konto erstellen"}
          </button>
        </form>

        <p className="muted small-hint">Demo-Konto zum Ausprobieren: <b>student@fh-dortmund.de</b> / <b>passwort123</b></p>
      </section>
    </div>
  );
}
