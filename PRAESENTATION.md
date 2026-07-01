# 🎤 Präsentations-Spickzettel – Virtual College (SWT2, Praktikum 4)

Dieser Zettel ist die **Kurzfassung** für die Abschlusspräsentation. Die
ausführliche Erklärung mit allen Diagrammen steht in
[docs/erklaerung.html](docs/erklaerung.html) (im Browser öffnen).

---

## ⭐ Offizielle Pflicht-Inhalte (1:1 aus den Vorlesungsfolien)

Die Folie *„Arbeitsergebnisse für die Abschlusspräsentation"* verlangt genau das hier.
Spalte „Stand" zeigt, was schon fertig ist und was ihr noch vorbereiten müsst:

| Pflicht-Inhalt (Folie) | Stand | Wo / To-do |
|------------------------|-------|------------|
| **Wrapper – Folie 1:** Gruppenthema als Teaser mit **2–3 Frontend-Auszügen** | 🟡 vorbereiten | Screenshots der React-App (`localhost:8090`) machen |
| **Wrapper – Folie 2:** Team mit **aktuellen Bildern, Namen, Matrikelnummern** | 🔴 offen | Team-Folie selbst erstellen |
| **Technologiestack** beschreiben + **bei DB begründen** | 🟢 da | Stack siehe unten; DBMS-Begründung siehe Abschnitt 6 |
| **Verteilungsdiagramm** inkl. **Zoom-In in die Artefakte** (Klassendiagramme + Code) | 🟢 da | erklaerung.html Kap. 2 (Verteilung) + Kap. 13 (Klassendiagramme) |
| → Fokus **Businesslogik mit Entwurfsmustern** | 🟢 da | Factory / Adapter / Observer – Abschnitt 2 |
| → Fokus **Kommunikation** | 🟢 da | synchron REST (HTTP) + asynchron MQTT über Mosquitto |
| → Fokus **Persistenz** | 🟢 da | JPA → MariaDB, Tabellen live abfragbar (Abschnitt 3) |
| **Projektdoku:** Rollen-/Aufgabenverteilung | 🔴 offen | Abschnitt 5 ausfüllen |
| **Projektdoku:** Umsetzungsplanung über **3 Sprints** + Abweichungen | 🟡 vorbereiten | Aus eurem Kanban-Board/Burndown ableiten |
| **Reflexion** (Architektur & Entwicklungspipeline) | 🟡 vorbereiten | Was lief gut, was würdet ihr anders machen |
| **Jedes Teammitglied stellt seinen Verantwortungsbereich vor** | 🔴 offen | Rollen in Abschnitt 5 festlegen |

> **Projekt-Anforderungen der Vorlesung – alle erfüllt:** ≥2 serverseitige Komponenten ✓ ·
> ein Teil simuliert ein externes System (`backend_send`) ✓ · **synchrone** (REST) **und
> asynchrone** (MQTT) Kommunikation ✓ · Message-Broker für async (Mosquitto) ✓ ·
> Daten persistieren (MariaDB) ✓ · Kommunikationsschnittstellen dokumentiert ✓.

### Benotung (Folie „Teams und Prüfung") – darauf zahlt jede Aktion ein

Es gibt **keine schriftliche Klausur**: Die Projektarbeit + diese Präsentation **ist** die
Prüfung (Team- **und** Einzelnote). Drei Phasen:

| Phase | Gewicht | Unser Stand |
|-------|---------|-------------|
| Architekturmodellierung (Diagramme, Muster) | **20 %** | weitgehend fertig |
| Projektmanagement (3-Sprint-Planung, Rollen, Abweichungen) | **30 %** | noch zu dokumentieren |
| Implementierung **und Test** | **50 %** | Code steht; **eigene Tests fehlen noch** |

→ Die größten offenen Punkte: **Projektdoku (30 %)** und **Tests** (Teil der 50 %).
Details + Prioritätenliste: siehe [docs/erklaerung.html](docs/erklaerung.html), **Kapitel 19**.

---

## 0. Vorbereitung (5 Minuten vor der Präsentation)

```bash
# Im Projekt-Hauptordner – einmal alles starten:
docker compose up --build -d

# Warten bis alle fünf Container laufen, dann im Browser:
#   http://localhost:8090
```

**Einloggen** mit dem Demo-Konto `student@fh-dortmund.de` / `passwort123`
(oder live ein neues Konto registrieren). Danach einmal durchklicken, damit
Testdaten da sind: eine Reservierung anlegen, einen Pinwand-Post erstellen,
Simulation starten. Danach ist die DB gefüllt.

---

## 1. Empfohlener Ablauf (roter Faden)

| # | Schritt | Was zeigen / sagen |
|---|---------|--------------------|
| 1 | **Folien: Projektidee** | Digitaler 2D-Campus der FH Dortmund: Karte, Räume, Mensa, ÖPNV, Pinwand. |
| 2 | **Verteilungsdiagramm** | 6 Knoten: Browser, Frontend (Nginx), 2 Backends (Spring Boot), MariaDB, Mosquitto. Wer redet über HTTP / JDBC / MQTT. → *erklaerung.html Kap. 2* |
| 3 | **OOD-Klassendiagramme** | `backend_receive` (Observer + Adapter) und `backend_send` (Factory). Diagramm = Bauplan im Code. → *Kap. 13* |
| 4 | **Die 3 Entwurfsmuster** | Pro Muster: **Name, Motivation, Umsetzung** (Tabelle unten). |
| 5 | **Live-Demo (Docker)** | `http://localhost:8090`: **einloggen** → Straße/Satellit → Raum wählen → Standort + Fußroute mit Pfeilen → reservieren → Simulation starten → VRR-Abfahrten. |
| 6 | **Datenbank-Nachweis** | Adminer öffnen (`http://localhost:8091`) oder Konsole (Befehle unten): Reservierung, Post und Mensa-Historie liegen wirklich in der DB. |
| 7 | **Abschluss** | Architektur steht, Entwurf ist umgesetzt. Echte Campus-/VRR-Daten sind integriert; nächste Schritte sind JUnit-Tests und produktive Service-Verträge. |

---

## 2. Die drei Entwurfsmuster (auswendig je 1 Satz)

| Kategorie | Muster | Motivation | Umsetzung im Code |
|-----------|--------|------------|-------------------|
| Erzeugung | **Factory Method** | Verschiedene Fremddaten erzeugen ohne `if`-Ketten. | `DatenGenerator.erzeugeDaten()`, je Datenart eine Unterklasse (`backend_send/simulation`). |
| Struktur | **Adapter** | Rohes MQTT-JSON in ein sauberes `Speiseplan`-Objekt übersetzen. | `MqttDatenAdapter` erfüllt `SpeiseplanQuelle` (`backend_receive/mensa`). |
| Verhalten | **Observer** | Bei neuem Post alle abonnierten Nutzer benachrichtigen. | `Pinwand` benachrichtigt alle `Beobachter` (`backend_receive/pinwand` + `nutzer`). |

> Singleton ist laut Aufgabe **verboten** – haben wir bewusst nicht als Pflichtmuster gewählt.

---

## 3. Live-Demo & Test-Befehle (zum Kopieren)

```bash
# Campus-Karte lesen
curl http://localhost:8090/api/campus/karte

# Raum reservieren
curl -X POST http://localhost:8090/api/campus/reservierungen \
  -H 'Content-Type: application/json' \
  -d '{"raumId":"g40-labor105","reserviertVon":"Test Student","von":"12:00","bis":"13:00"}'

# Pinwand-Post anlegen
curl -X POST http://localhost:8090/api/pinwand/posts \
  -H 'Content-Type: application/json' \
  -d '{"autor":"Test Student","rolle":"Student","kategorie":"Lerngruppe","titel":"React-Test","inhalt":"Pinwand-Test"}'

# Simulation starten: backend_send -> MQTT -> backend_receive -> MariaDB
curl -X POST http://localhost:8090/api/simulation/start
curl http://localhost:8090/api/mensa/historie
curl http://localhost:8090/api/transit/abfahrten
```

### Datenbank zeigen (der „Beweis")

**Am einfachsten – Adminer im Browser:** `http://localhost:8091` öffnen, einloggen
(Server `mariadb`, `vcuser` / `vcpass`, DB `virtualcollege`) und die Tabellen
`gespeicherte_reservierung`, `gespeicherter_pinwand_post`, `gespeichertes_gericht`,
`benutzerkonto` anklicken. Sehr überzeugend für die Präsentation.

**Alternativ – Konsole:**

```bash
docker compose exec -T mariadb mariadb -u vcuser -pvcpass virtualcollege -e 'SHOW TABLES'

docker compose exec -T mariadb mariadb -u vcuser -pvcpass virtualcollege \
  -e 'SELECT id, name, preis, empfangen_am FROM gespeichertes_gericht ORDER BY id DESC LIMIT 5'
```

---

## 4. Mögliche Prüfungsfragen (Kurzantworten)

| Frage | Antwort |
|-------|---------|
| Warum zwei Backends? | `backend_receive` = unsere App-Logik. `backend_send` = simuliertes Fremdsystem (ÖPNV/Studierendenwerk). |
| Warum MQTT statt direktem Aufruf? | Entkopplung: Sender und Empfänger kennen sich nicht, kommunizieren zeitversetzt über den Broker (publish/subscribe). |
| Warum Nginx? | Frontend Web Server aus dem Diagramm: liefert React aus und leitet `/api/...` an die Backends weiter. |
| Modulith / Vertical Slicing? | Ein Backend, aber pro Thema ein eigenes Paket (campus, pinwand, mensa, …) statt technischer Schichten. |
| Verteilungs- vs. Klassendiagramm? | Verteilungsdiagramm = Hardware/Server. Klassendiagramm = Bauplan im Code. Nicht verwechseln! |
| Wie sicher ist euer Login? | Passwörter werden mit **PBKDF2 + Salt gehasht** (nie im Klartext), nach Login ein Token. Demo-Niveau; produktiv: Spring Security + JWT + HTTPS. |

---

## 5. Rollenverteilung im Team (vor der Präsentation ausfüllen)

Pflicht laut Folie: **jedes Teammitglied stellt seinen Verantwortungsbereich vor.**

| Teil | Wer erklärt es |
|------|----------------|
| Wrapper (Teaser + Team-Folie) | _________ |
| Technologiestack + Verteilungsdiagramm | _________ |
| OOD-Klassendiagramme + die 3 Entwurfsmuster | _________ |
| Kommunikation (REST + MQTT) | _________ |
| Persistenz + Live-Demo + Datenbank-Nachweis | _________ |

---

## 6. Technologiestack & DBMS-Begründung (Folien-Pflicht)

**Stack:** React/Vite + Nginx (Frontend) · Java 21 + Spring Boot (2 Backends) ·
Spring Integration / Eclipse Paho (MQTT) · Mosquitto (Message-Broker) ·
Spring Data JPA (Persistenz) · **H2** (lokal) / **MariaDB** (Docker/Produktion) ·
**Authentifizierung** (Login/Registrierung, Passwort-Hashing mit PBKDF2 + Salt) · Docker Compose.

**Warum diese Datenbank?** (das fragt die Folie *„DBMS-Auswahl begründen"* ab)

- **Relationale DB**, weil unsere Daten klar strukturiert und verknüpft sind
  (Reservierungen, Pinwand-Posts, Mensa-Gerichte) – kein Bedarf für NoSQL.
- **MariaDB** in Produktion: frei/open-source, MySQL-kompatibel, leichtgewichtig,
  als fertiges Docker-Image verfügbar – genau die Aufteilung, die auch die
  Vorlesung empfiehlt: **„H2 für Entwicklung, MariaDB für Produktion"**.
- **H2 im Arbeitsspeicher** für die lokale Entwicklung: keine Installation nötig,
  das Backend läuft sofort. Umschaltung auf MariaDB rein über Umgebungsvariablen
  in `docker-compose.yml` – der Code bleibt gleich (Spring Data JPA abstrahiert).

---

*Stand: Praktikum 4 – Klassen, MQTT, Datenbank und React-Frontend 1.0 umgesetzt &
per Docker testbar. Pflicht-Inhalte oben aus den SWT2-Folien (SoSe 2026) abgeglichen.*
