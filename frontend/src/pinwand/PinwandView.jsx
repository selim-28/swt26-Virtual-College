// Modul "pinwand": Beitraege anzeigen, erstellen, liken (Observer-Muster im
// Backend); der Admin darf zusaetzlich loeschen (verwalten).

import { Heart, Send, Trash2 } from "lucide-react";
import { kurzZeit } from "../gemeinsam/format.js";
import { SectionTitle } from "../gemeinsam/Bausteine.jsx";

// Pinwand-Seite mit echtem Speichern und Likes.
// Der Admin kann Beitraege zusaetzlich LOESCHEN (verwalten).
export default function PinwandView({ posts, onCreate, onLike, istAdmin, onDelete }) {
  return (
    <section className="page-grid pinwand-grid">
      <div className="workspace">
        <SectionTitle kicker="Observer-Muster" title="Virtuelle Pinwand" />
        <form className="post-form" onSubmit={onCreate}>
          <div className="form-row">
            <label>
              Kategorie
              <select name="kategorie" defaultValue="Campus">
                <option>Campus</option>
                <option>Wartung</option>
                <option>Lerngruppe</option>
                <option>Betreuung</option>
              </select>
            </label>
            <label>
              Titel
              <input name="titel" placeholder="Kurzer Titel" required />
            </label>
          </div>
          <label>
            Inhalt
            <textarea name="inhalt" placeholder="Was soll die Campus-Community wissen?" required />
          </label>
          <button className="primary-action" type="submit">
            <Send size={16} />
            Post speichern
          </button>
        </form>
      </div>

      <div className="post-list">
        {posts.map((post) => (
          <article className="post-card" key={post.id}>
            <span className="badge">{post.kategorie}</span>
            <h3>{post.titel}</h3>
            <p>{post.inhalt}</p>
            <footer>
              <span>{post.autor} - {post.rolle} - {kurzZeit(post.erstelltAm)}</span>
              <span className="post-actions">
                {istAdmin && (
                  <button
                    type="button"
                    className="icon-button danger"
                    title="Post loeschen (Admin)"
                    aria-label={`Post ${post.titel} loeschen`}
                    onClick={() => onDelete(post.id)}
                  >
                    <Trash2 size={15} />
                  </button>
                )}
                <button type="button" className="like-button" onClick={() => onLike(post.id)}>
                  <Heart size={15} />
                  {post.likes}
                </button>
              </span>
            </footer>
          </article>
        ))}
      </div>
    </section>
  );
}
