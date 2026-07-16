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
| **Projektdoku:** Rollen-/Aufgabenverteilung | 🟡 fast fertig | Abschnitt 5: Tabelle + Zeig-Aufgaben stehen – nur **Namen eintragen** |
| **Projektdoku:** Umsetzungsplanung über **3 Sprints** + Abweichungen | 🟡 Gerüst da | Burndown + Datenbasis: *erklaerung.html Kap. 28.c*; Abweichungen: Abschnitt 8 – beim Team-Abend verfeinern |
| **Reflexion** (Architektur & Entwicklungspipeline) | 🟢 da | Fertiger Text in **Abschnitt 8** – nur als Folie übernehmen |
| **Jedes Teammitglied stellt seinen Verantwortungsbereich vor** | 🟡 fast fertig | 6 Bereiche in Abschnitt 5 definiert – Namen zuordnen + Drill üben |

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
| Projektmanagement (3-Sprint-Planung, Rollen, Abweichungen) | **30 %** | 🟡 Burndown ✅ (Kap. 28.c) · Board bereinigt ✅ · Reflexion ✅ (§8) – Rest: Namen in §5, Sprint-Rückblick, Milestones |
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

**API-Doku live zeigen (OpenAPI/Swagger, seit 17.07.):**
`http://localhost:8090/swagger-ui.html` öffnen → über **Authorize** den Token
aus Schritt 1 eintragen → beliebigen geschützten Endpunkt direkt ausführen.
(Direktzugriff ohne Nginx: `localhost:8083` für receive, `localhost:8084` für send.)

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

## 5. Rollenverteilung im Team (Namen eintragen – Rest ist vorbereitet)

Pflicht laut Folie: **jedes Teammitglied stellt seinen Verantwortungsbereich vor.**
Die sechs Bereiche entsprechen 1:1 dem Sprecherplan der Folien (praesentation/).
Pro Bereich steht dabei, was du **live zeigen können musst** (Prüfer sagen gern
„zeigen Sie mal") und die wahrscheinlichste Nachfrage.

> **Namens-VORSCHLAG (frei tauschen – beim Team-Abend bestätigen!):** abgeleitet aus
> den GitHub-Beteiligten; wer einen Bereich lieber übernimmt, einfach umtragen.
> Bei 5 Personen übernimmt Sprecher:in 1 zusätzlich Bereich 6 (Einstieg + Abschluss-Demo passt gut zusammen).

| # | Bereich (= Folien-Block) | Name (Vorschlag) | Musst du live zeigen können | Typische Nachfrage → Antwort steht in |
|---|--------------------------|------|------------------------------|----------------------------------------|
| 1 | Einstieg: Projektidee, Teaser, Systemkontext | Adam | App-Rundflug in 60 s (`localhost:8090`): Karte → Raum → Pinwand | „Wer sind die Akteure?" → Folie Systemkontext |
| 2 | Projektmanagement: Scrum, 3 Sprints, Kanban, Burndown, Abweichungen | Selim | GitHub-Projects-Board „Virtual College – Kanban" + Burndown (erklaerung.html Kap. 28.c) | „Was lesen Sie aus dem Burndown ab?" → Kap. 28.c + Abschnitt 8 |
| 3 | Stack-Entscheidungen + DBMS-Begründung | Mhamed | `docker-compose.yml` öffnen und die 6 Dienste nennen | „Warum MariaDB statt H2/Postgres?" → Abschnitt 6 |
| 4 | Architektur: Verteilungsdiagramm, Modulith, REST + MQTT, Docker, CI | Sif-Eddine | Verteilungsdiagramm (Kap. 2) + einen grünen PR-Check + Swagger (`/swagger-ui.html`) | „Was passiert, wenn Mosquitto ausfällt?" → Kap. 7/27 |
| 5 | OOD + die 3 Entwurfsmuster + JWT/Rollen + Compliance | Onur | `MqttDatenAdapter.java` öffnen und den Adapter am Code erklären; 403-Test nennen | „Warum kein Singleton?" → Abschnitt 4 |
| 6 | Epics-Ergebnis, Live-Demo, DB-Nachweis, 97 Tests | 6. Person / sonst Adam | Demo-Ablauf aus Abschnitt 1; `./mvnw test` grün; Adminer | „Zeigen Sie die Reservierung in der DB" → Abschnitt 3 |

### Muster-Drill – Pflicht für ALLE (nicht nur für Bereich 5!)

Praktikum 4 wörtlich: **jedes** Team-Mitglied muss für **jedes** Muster
Name → Motivation → Umsetzung erklären können. Lernbasis: Tabelle in Abschnitt 2
(je Muster ein Satz pro Spalte) + **`docs/code-lesen.html`** (die Muster-Dateien
Zeile für Zeile erklärt, mit Lese-Fahrplan pro Bereich). Selbsttest zu zweit, 5 Minuten:

1. „Erkläre Factory Method, als wäre ich im 1. Semester." (Erzeugung, `backend_send`)
2. „Warum heißt der `MqttDatenAdapter` Adapter und nicht einfach Parser?" (Struktur)
3. „Zeig mir den Observer LIVE." (Verhalten: 2. Browserfenster, Post erstellen, 🔔)
4. Bonus-Frage der Profs: „Welche Muster nutzt ihr **noch**, ohne sie gezeichnet zu
   haben?" → Dependency Injection (Spring-Kern), Repository (Spring Data JPA), MVC
   (Controller), Front Controller (DispatcherServlet), Publish/Subscribe (MQTT –
   der „große Bruder" unseres Observers).

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

## 8. Reflexion (Pflichtpunkt der Abschluss-Folie) – fertig vorbereitet 🪞

Die Folie verlangt ausdrücklich **Reflexion** und **„Abweichungen von der Planung
und warum"**. Stärkste Strategie: die eigenen Schwächen **selbst benennen, bevor
die Prüfer sie finden** – jede mit einer konkreten Konsequenz. Sprecher:in von
Bereich 2 trägt das vor, alle kennen es.

**Was gut lief (je 1 Satz):**

- **Architektur trägt:** Modulith mit Vertical Slicing – neue Epics (z. B. Auth)
  ließen sich als eigenes Paket ergänzen, ohne Bestehendes umzubauen.
- **Ein-Befehl-Betrieb:** `docker compose up` startet alle sechs Dienste – die
  Demo ist reproduzierbar, kein „läuft nur auf meinem Rechner".
- **Absicherung am Ende stark:** 97 automatische Tests inkl. Rollen-Rechten über
  den echten HTTP-Stack und provoziertem VRR-Ausfall.

**Was wir gelernt haben (ehrlich, mit Konsequenz):**

1. **Zu große Commits:** Der Endstand kam in einem Sammel-Commit („alle 8 Epics")
   – das hat uns unterwegs Code-Reviews und CI-Feedback gekostet.
   → *Konsequenz (seit 16.07. aktiv): kleine Feature-Branches + PR mit grünen
   Checks für jede Änderung – PR #40 und #41 sind die ersten Beispiele.*
2. **Tests kamen zu spät:** Lange stand das Projekt bei 0 Tests; beim Auth-Umbau
   fehlte das Sicherheitsnetz. → *Konsequenz: „Definition of Done" mit
   Test-Pflicht pro Story – Tests entstehen mit dem Feature, nicht danach.*
3. **Board lief der Realität hinterher:** 18 fertige Stories standen noch auf
   „offen"; unser Burndown (erklaerung.html Kap. 28.c) zeigt die Klippe ehrlich.
   → *Konsequenz: Issues werden beim Merge geschlossen, Commits nennen die
   Issue-Nummer.*
4. **Frontend war lange eine einzige Datei:** `App.jsx` (≈1160 Zeilen) widersprach
   unserem eigenen Backend-Prinzip „lose Kopplung, hohe Kohäsion".
   → *Konsequenz (am 17.07. umgesetzt): das Frontend ist jetzt wie das Backend
   nach Fachmodulen geschnitten (`auth/`, `campus/`, `mensa/`, `pinwand/`,
   `transit/`, `einstellungen/`, `gemeinsam/`) – `App.jsx` hält nur noch Zustand
   und Verdrahtung; alle 27 Tests blieben grün. Wir haben unsere eigene
   Reflexion noch vor der Präsentation eingelöst.*

**Schluss-Satz:** „Fachlich haben wir alles erreicht – gelernt haben wir am
meisten über den *Prozess*: Kleine, geprüfte Schritte sind kein Overhead,
sondern das, was Teamarbeit nachweisbar macht."

---

*Stand: alle 8 Epics umgesetzt und getestet (97 Tests) – 3 Rollen (Student/
Professor/Admin), 6 echte FH-Räume (Emil-Figge-Str., Schwerpunkt Informatik), Observer LIVE, Polling,
MQTT, MariaDB, Docker. Pflicht-Inhalte + Praktika 1–4 + Compliance aus
`SWT2ALLIN7.pdf` abgeglichen; Praktikums-Nachweis: erklaerung.html Kap. 28.*
