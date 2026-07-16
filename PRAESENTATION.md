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
| Architekturmodellierung (Diagramme, Muster) | **20 %** | 🟢 fertig – Verteilungsdiagramm (inkl. externe Dienste) + 2 OOD-Diagramme + Auth-Zusatzdiagramm, konsistent zum Code |
| Projektmanagement (3-Sprint-Planung, Rollen, Abweichungen) | **30 %** | 🟡 Team-Aufgabe: Milestones/Burndown finalisieren + Abschnitt 5 ausfüllen |
| Implementierung **und Test** | **50 %** | 🟢 alle 8 Epics umgesetzt; **97 automatische Tests** (60 backend_receive + 10 backend_send + 27 Frontend), alle grün |

→ Größter offener Punkt: **Projektdoku (30 %)** – Rollen + 3-Sprint-Rückblick.
**Praktikums-Nachweis** (jede Prof-Aufgabe → Antwort + Fundort): *erklaerung.html Kapitel 28*.

---

## 0. Vorbereitung (5 Minuten vor der Präsentation)

```bash
# Im Projekt-Hauptordner – einmal alles starten:
docker compose up --build -d

# Warten bis alle fünf Container laufen, dann im Browser:
#   http://localhost:8090
```

**Einloggen** – es gibt **drei Demo-Konten** (Passwort jeweils `passwort123`),
damit ihr alle Rollen zeigen könnt:

| Konto | Rolle | Zeigt in der Demo |
|-------|-------|-------------------|
| `student@fh-dortmund.de` | Student | Labor reservieren; **Hörsaal wird verweigert** |
| `prof@fh-dortmund.de` | Professor | darf zusätzlich den **Hörsaal** reservieren |
| `admin@fh-dortmund.de` | Admin | 🗑-Löschknöpfe an Reservierungen & Posts |

Danach einmal durchklicken, damit Testdaten da sind: Reservierung anlegen,
Pinwand-Post erstellen, Simulation starten. Danach ist die DB gefüllt.

---

## 1. Empfohlener Ablauf (roter Faden)

| # | Schritt | Was zeigen / sagen |
|---|---------|--------------------|
| 1 | **Folien: Projektidee** | Digitaler 2D-Campus der FH Dortmund: Karte, Räume, Mensa, ÖPNV, Pinwand. |
| 2 | **Verteilungsdiagramm** | 7 Knoten: Browser, Frontend (Nginx), 2 Backends (Spring Boot), MariaDB, Mosquitto + **Internet/Externe Dienste** (OSM/OSRM/VRR). Wer redet über HTTP / JDBC / MQTT / HTTPS. → *erklaerung.html Kap. 2* |
| 3 | **OOD-Klassendiagramme** | `backend_receive` (Observer + Adapter + Auth-Zusatzdiagramm) und `backend_send` (Factory). Diagramm = Bauplan im Code, **konsistent gehalten**. → *Kap. 13* |
| 4 | **Die 3 Entwurfsmuster** | Pro Muster: **Name, Motivation, Umsetzung** (Tabelle unten). Observer läuft **LIVE** (🔔). |
| 5 | **Live-Demo (Docker)** | `http://localhost:8090`: als **Student** einloggen → Karte (6 echte FH-Räume, Schwerpunkt Informatik) → Fußroute → Labor reservieren, Hörsaal wird abgelehnt → als **Prof** Hörsaal reservieren → als **Admin** löschen → Simulation → VRR-Abfahrten → 🔔-Meldung bei neuem Post (2. Browserfenster). |
| 6 | **Datenbank-Nachweis** | Adminer öffnen (`http://localhost:8091`) oder Konsole (Befehle unten): Reservierung, Post und Mensa-Historie liegen wirklich in der DB. |
| 7 | **Tests & Compliance** | **97 automatische Tests** (JUnit + Frontend) kurz zeigen (`./mvnw test`); Compliance-Frage beantworten (Abschnitt 7 unten / *erklaerung.html Kap. 29*). |
| 8 | **Abschluss** | Alle 8 Epics umgesetzt, getestet, dokumentiert. Reflexion: was lief gut / anders. |

---

## 2. Die drei Entwurfsmuster (auswendig je 1 Satz)

| Kategorie | Muster | Motivation | Umsetzung im Code |
|-----------|--------|------------|-------------------|
| Erzeugung | **Factory Method** | Verschiedene Fremddaten erzeugen ohne `if`-Ketten. | `DatenGenerator.erzeugeDaten()`, je Datenart eine Unterklasse (`backend_send/simulation`). |
| Struktur | **Adapter** | Rohes MQTT-JSON in ein sauberes `Speiseplan`-Objekt übersetzen. | `MqttDatenAdapter` erfüllt `SpeiseplanQuelle` (`backend_receive/mensa`). |
| Verhalten | **Observer** | Bei neuem Post alle abonnierten Nutzer benachrichtigen. | `Pinwand` (Subjekt) benachrichtigt alle `Beobachter` – **läuft LIVE**: `BenachrichtigungsSpeicher` ist als Beobachter registriert, abrufbar über `GET /api/pinwand/benachrichtigungen`, im Frontend als 🔔-Meldung. |

> Singleton ist laut Aufgabe **verboten** – haben wir bewusst nicht als Pflichtmuster gewählt.

---

## 3. Live-Demo & Test-Befehle (zum Kopieren)

> ⚠️ **Alles ist mit Spring Security geschützt** – ohne Token gibt es 401.
> Deshalb zuerst einloggen und den JWT in eine Variable legen:

```bash
# 1) Einloggen und Token merken (Student)
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@fh-dortmund.de","passwort":"passwort123"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2) Campus-Karte lesen (6 echte FH-Räume)
curl -s http://localhost:8090/api/campus/karte -H "Authorization: Bearer $TOKEN"

# 3) Raum reservieren (Innovation Lab, Haus 42)
curl -s -X POST http://localhost:8090/api/campus/reservierungen \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"raumId":"ef42-innovationlab","von":"12:00","bis":"13:00"}'
#    (reserviertVon ist egal - der Name kommt fälschungssicher aus dem Token!)

# 4) Pinwand-Post anlegen (Autor/Rolle kommen aus dem Token)
curl -s -X POST http://localhost:8090/api/pinwand/posts \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"kategorie":"Lerngruppe","titel":"React-Test","inhalt":"Pinwand-Test"}'

# 5) Observer-Beweis: die automatische Benachrichtigung abrufen (US 4.3)
curl -s http://localhost:8090/api/pinwand/benachrichtigungen -H "Authorization: Bearer $TOKEN"

# 6) Simulation starten: backend_send -> MQTT -> backend_receive -> MariaDB
curl -s -X POST http://localhost:8090/api/simulation/start -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8090/api/mensa/historie      -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8090/api/transit/abfahrten   -H "Authorization: Bearer $TOKEN"

# 7) Alle 97 Tests vorführen (belegt die 50%-Phase "Implementierung und Test")
./mvnw test && (cd frontend && npm test)
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
| Wie sicher ist euer Login? | Passwörter werden mit **PBKDF2 + Salt gehasht** (nie im Klartext). Die REST-Endpunkte sind mit **Spring Security + JWT** geschützt: Der Login liefert einen signierten Token (HS256, 24 h Gültigkeit), der bei jeder Anfrage als `Authorization: Bearer` geprüft wird; beim Logout wird der Token sofort gesperrt. Für echten Produktivbetrieb fehlt noch HTTPS. |
| Welche Rollen gibt es und was dürfen sie? | **Student** (Labore/Seminarräume reservieren), **Professor** (zusätzlich Hörsäle), **Admin** (verwalten: Reservierungen & Posts löschen – DELETE ist serverseitig auf `ROLE_ADMIN` beschränkt). Die Rolle steht im signierten JWT; Zuordnung von Buchungen/Posts kommt aus dem Token, nicht aus dem Request. Bewiesen im `RechteIntegrationTest`. |
| Wie habt ihr getestet? | **97 automatische Tests**: 60 in `backend_receive` (Auth/JWT, Rollen-Rechte über echten HTTP-Stack, Reservierungsregeln, Persistenz mit „Neustart"-Trick, Adapter, Observer), 10 in `backend_send` (Factory, MQTT-Publish, **VRR-Ausfall gezielt provoziert** → Offline-Fallback), 27 im Frontend (`node --test`, reine Funktionen). Ein Befehl: `./mvnw test`. |
| Was heißt Compliance für euer Projekt? | Kurzantwort in Abschnitt 7 unten – ausführlich mit DSGVO-Zerlegung, STRIDE, SBOM und AI Act in *erklaerung.html Kapitel 29*. |

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
**Authentifizierung** (Login/Registrierung, **Spring Security + JWT**, Passwort-Hashing mit PBKDF2 + Salt) · Docker Compose.

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

## 7. Compliance (wird in der Präsentation gefragt!) ⚖️

Aus den Vorlesungen „Compliance im Software Engineering I+II". **Kernsatz zum
Auswendiglernen:** *„Compliance heißt, regulatorische Regeln in technische und
organisatorische Anforderungen zu überführen – und die Einhaltung nachweisen zu
können. Das ist eine Engineering-Aufgabe, keine reine Rechtsfrage."*

**So setzen WIR das um (je 1 Satz):**

| Compliance-Baustein | Bei Virtual College |
|---------------------|---------------------|
| Personenbezogene Daten | Nur das Nötigste: E-Mail, Name, Rolle, Passwort-**Hash**. Der **Standort wird NIE gespeichert** (bleibt im Browser, nur nach Zustimmung – Privacy by Design/Default). |
| Rollen- & Berechtigungskonzept | 3 Rollen im **signierten JWT**; DELETE nur `ROLE_ADMIN`, Hörsäle nur Professor/Admin – serverseitig erzwungen und **im Test bewiesen** (403 für Studenten). |
| Betroffenenrechte (DSGVO-Auskunft) | Technisch sofort erfüllbar: `GET /api/auth/ich` + Adminer zeigen alle Daten eines Nutzers; Akteure/Pflichten/Fristen sauber zuordenbar. |
| Nachweisbarkeit (Accountability) | „Aus Umsetzung wird erst durch **Evidenz** prüfbare Compliance": Diagramme (Kap. 2/13), Rollenkonzept (Kap. 18), Logs, **97 Tests**, Adminer, diese Doku. |
| STRIDE-Bedrohungen | Spoofing→JWT-Signatur · Tampering→Manipulations-Test · Info-Disclosure→nur Hashes in der DB · Elevation→`hasRole("ADMIN")` · ehrlich offen: kein Rate-Limiting (DoS). |
| SBOM (CRA, Pflicht ab 09/2026) | `pom.xml` + `package.json` sind unsere versionierte „Zutatenliste"; `./mvnw dependency:tree` / `npm ls` listet alles auf. |
| AI Act | App enthält **kein KI-System** (keine automatisierten Entscheidungen über Personen, kein Scoring) → minimales Risiko. Der Code ist reviewt (PRs + Pre-Merge-Checks), durch 97 Tests abgesichert und in `docs/erklaerung.html` (Block E) erklärt – Verantwortung liegt beim Team. |

→ Ausführlich mit allen Tabellen und dem 30-Sekunden-Spickzettel:
*erklaerung.html Kapitel 29*.

---

*Stand: alle 8 Epics umgesetzt und getestet (97 Tests) – 3 Rollen (Student/
Professor/Admin), 6 echte FH-Räume (Emil-Figge-Str., Schwerpunkt Informatik), Observer LIVE, Polling,
MQTT, MariaDB, Docker. Pflicht-Inhalte + Praktika 1–4 + Compliance aus
`SWT2ALLIN7.pdf` abgeglichen; Praktikums-Nachweis: erklaerung.html Kap. 28.*
