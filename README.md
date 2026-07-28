# Virtual College

Virtual College ist unser Projekt für Softwaretechnik 2: ein digitaler 2D-Campus für die
FH Dortmund. Studierende sollen sich auf dem Campus zurechtfinden, Räume reservieren,
sich über eine Pinnwand austauschen und nebenbei sehen, was es in der Mensa gibt und wann
die nächste Bahn fährt.

Uns war wichtig, dass das nicht nur eine hübsche Oberfläche ist, sondern eine echte
verteilte Anwendung. Die Kette sieht so aus:

```text
Browser (React)  ->  Nginx  ->  backend_receive (Spring Boot)  ->  MariaDB
                                        ^
                                        | MQTT (Mosquitto)
                                        |
                                backend_send (simuliertes Fremdsystem)
```

`backend_receive` ist der Application Server, mit dem das Frontend spricht.
`backend_send` spielt das Fremdsystem (ÖPNV / Studierendenwerk) und schickt seine Daten
asynchron über MQTT — die beiden Backends kennen sich also nicht direkt, sondern nur über
den Broker. Persistiert wird in MariaDB; ohne Docker läuft stattdessen eine H2-Datenbank
im Arbeitsspeicher, damit man das Backend auch mal schnell allein starten kann.

Umgesetzt sind alle acht Epics aus [ANFORDERUNGEN.md](ANFORDERUNGEN.md): Login und
Registrierung, echte Campus-Karte mit Straßen- und Satellitenansicht, Browser-Standort,
Fußweg mit Richtungspfeilen, Raumreservierung mit Rollenrechten, Pinnwand mit Likes und
Benachrichtigungen, Mensa-Speiseplan, VRR-Abfahrten, Simulation und Datenbank-Historie.

> [!NOTE]
> Die App liegt komplett hinter einem Login. Zum Ausprobieren gibt es ein Demo-Konto:
> `student@fh-dortmund.de` / `passwort123`. Man kann sich natürlich auch selbst
> registrieren.

## Team

Team Leader: Mahmut Selim Kilicaslan

Members: Sif-Eddine Khayati, Onur Kizil, Adam Nachit, Mhamed Mokhtar

Die Verantwortungsbereiche haben wir so aufgeteilt:

| Bereich | Wer |
|---------|-----|
| Projektidee, Systemkontext, Demo | Adam Nachit |
| Projektmanagement: Scrum, Sprints, Kanban-Board | Mahmut Selim Kilicaslan |
| Technologie-Stack und DBMS-Entscheidung | Mhamed Mokhtar |
| Architektur: Verteilung, REST + MQTT, Docker, CI | Sif-Eddine Khayati |
| OOD, Entwurfsmuster, JWT/Rollen, Compliance | Onur Kizil |

## Quickstart

Der schnellste Weg ist Docker — damit braucht man weder Java noch Node lokal.
Im Projekt-Hauptordner:

```bash
docker compose up --build
```

Danach im Browser öffnen:

```text
http://localhost:8090
```

Alles wieder stoppen:

```bash
docker compose down
```

Docker startet dabei sechs Container:

```text
frontend          React/Vite, gebaut und von Nginx ausgeliefert   Port 8090
backend_receive   Spring-Boot-Hauptbackend                        Port 8083
backend_send      simuliertes Fremdsystem                         Port 8084
mosquitto         MQTT-Broker                                     Port 1883
mariadb           Datenbank                                       Port 3306
adminer           Datenbank im Browser ansehen (optional)         Port 8091
```

Wer lieber ohne Compose baut, kann ein einzelnes Backend-Image auch direkt bauen:

```bash
docker build -f Dockerfile.java --build-arg MODULE=backend_receive .
```

## Prerequisites

- Docker und Docker Compose — reicht allein für den Quickstart
- Java 21, falls die Backends ohne Docker laufen sollen
- Node.js 20 oder neuer, falls am Frontend entwickelt wird
- Maven muss **nicht** installiert werden, der Maven Wrapper (`./mvnw`) liegt im Repo
- Für Karte, Fußweg und VRR-Abfahrten braucht die App eine Internetverbindung
  (OpenStreetMap, Esri, OSRM, VRR-EFA). Ohne Netz greifen dokumentierte Fallbacks.

## Installation and Setup

1. Repository klonen.
2. Den Projekt-Hauptordner als Maven-Projekt in der IDE öffnen — die Wurzel-`pom.xml`
   klammert `backend_receive` und `backend_send` zusammen.
3. Einmal `docker compose up --build` laufen lassen und prüfen, ob
   `http://localhost:8090` erscheint.
4. Mit dem Demo-Konto anmelden oder ein eigenes Konto registrieren.
5. Für die Entwicklung ohne Docker: Mosquitto und (optional) MariaDB starten,
   sonst fällt `backend_receive` auf H2 zurück und die MQTT-Verbindung schlägt fehl.

> [!IMPORTANT]
> `app.jwt.secret` in `backend_receive/src/main/resources/application.properties` hat
> einen Standardwert, der ausdrücklich nur für die Entwicklung gedacht ist. In einem
> echten Betrieb muss er über die Umgebungsvariable `APP_JWT_SECRET` gesetzt werden.

## Running the Project

### Alles zusammen

```bash
docker compose up --build
```

### Nur die Backends, ohne Docker

```bash
./mvnw -pl backend_receive spring-boot:run   # Login, Campus, Pinnwand, Mensa (8083)
./mvnw -pl backend_send spring-boot:run      # Fremdsystem/Simulation (8084)
```

Ein MQTT-Broker muss dafür erreichbar sein, sonst funktioniert die Simulation nicht:

```bash
docker compose up mosquitto
```

### Frontend im Dev-Modus

```bash
cd frontend
npm install
npm run dev      # http://localhost:8090
```

Der Vite-Dev-Server übernimmt dabei die Rolle von Nginx und leitet `/api/simulation`
an Port 8084 und alles andere unter `/api` an Port 8083 weiter.

### Tests

```bash
./mvnw verify                      # kompletter Build inkl. aller Backend-Tests
./mvnw -pl backend_receive test    # nur das Hauptbackend
cd frontend && npm test            # Node-eigener Test-Runner, ohne Zusatzbibliothek
```

Insgesamt sichern **97 automatische Tests** das Projekt ab: 60 in `backend_receive`
(Auth/JWT, Rollenrechte über den echten HTTP-Stack, Reservierungsregeln, Persistenz,
Adapter, Observer), 10 in `backend_send` (Factory, MQTT-Publish, provozierter
VRR-Ausfall) und 27 im Frontend.

### Ein Durchlauf per curl

Die API ist seit Spring Security + JWT geschützt — also erst einloggen, dann das Token
als `Authorization: Bearer` mitschicken:

```bash
# 1) Einloggen und Token merken
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@fh-dortmund.de","passwort":"passwort123"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

# 2) Campus-Karte lesen
curl http://localhost:8090/api/campus/karte -H "Authorization: Bearer $TOKEN"

# 3) Raum reservieren
curl -X POST http://localhost:8090/api/campus/reservierungen \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"raumId":"g40-labor105","reserviertVon":"Test Student","von":"12:00","bis":"13:00"}'

# 4) Pinnwand-Post anlegen
curl -X POST http://localhost:8090/api/pinwand/posts \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"autor":"Test Student","rolle":"Student","kategorie":"Lerngruppe","titel":"React-Test","inhalt":"Pinwand-Test"}'

# 5) Simulation starten: backend_send -> MQTT -> backend_receive -> MariaDB
curl -X POST http://localhost:8090/api/simulation/start
curl http://localhost:8090/api/mensa/historie -H "Authorization: Bearer $TOKEN"

# 6) Den zuletzt über MQTT empfangenen VRR-Snapshot lesen
curl http://localhost:8090/api/transit/abfahrten -H "Authorization: Bearer $TOKEN"
```

### In die Datenbank schauen

Am bequemsten über Adminer unter `http://localhost:8091`:

```text
System:    MySQL / MariaDB
Server:    mariadb
Benutzer:  vcuser
Passwort:  vcpass
Datenbank: virtualcollege
```

Interessante Tabellen sind `benutzerkonto`, `gespeicherte_reservierung`,
`gespeicherter_pinwand_post` und `gespeichertes_gericht`. Wer die Konsole bevorzugt:

```bash
docker compose exec -T mariadb mariadb -u vcuser -pvcpass virtualcollege -e 'SHOW TABLES'

docker compose exec -T mariadb mariadb -u vcuser -pvcpass virtualcollege \
  -e 'SELECT id, raum_name, reserviert_von, von, bis, status FROM gespeicherte_reservierung ORDER BY id DESC LIMIT 5'
```

## Project Structure

```text
swt26-Virtual-College/
├── pom.xml                     # Eltern-POM, klammert beide Backend-Module
├── backend_receive/            # Application Server (Port 8083)
│   ├── pom.xml
│   └── src/main/java/swt2/
│       ├── campus/             # Karte, Räume, Reservierungen (Fassade)
│       ├── pinwand/            # Posts, Likes, Benachrichtigungen (Observer)
│       ├── nutzer/             # Nutzer + Student/Betreuer/Verwaltung (Vererbung)
│       ├── auth/               # Login, Spring Security + JWT, PBKDF2-Hashing
│       ├── mensa/              # Speiseplan + MQTT-Empfang (Adapter)
│       ├── transit/            # per MQTT empfangene VRR-Abfahrten
│       └── persistenz/         # JPA-Entities und Repositories
├── backend_send/               # simuliertes Fremdsystem (Port 8084)
│   ├── pom.xml
│   └── src/main/java/swt2/
│       └── simulation/         # DatenGenerator (Factory) + MqttSender
├── frontend/                   # React/Vite-App, im Container hinter Nginx (Port 8090)
├── mosquitto/                  # Konfiguration des MQTT-Brokers
├── docker-compose.yml          # Ein-Knopf-Start für alle Container
├── Dockerfile.java             # Container-Build für ein ausgewähltes Maven-Modul
├── ANFORDERUNGEN.md            # Product Backlog: Epics, User Stories, Akzeptanzkriterien
├── .github/ISSUE_TEMPLATE/     # Vorlagen für User Stories und Bug Reports
├── .github/PULL_REQUEST_TEMPLATE.md
└── .github/workflows/          # Pre-Merge-Checks und Release
```

## Karte, Navigation und Live-ÖPNV

Ein paar Dinge, die bewusst gegen echte Dienste laufen und deshalb Erklärung verdienen:

- `CampusService` liefert die realen WGS84-Koordinaten des Campus
  Emil-Figge-Straße 38–44.
- Leaflet zeigt wahlweise OpenStreetMap oder Esri World Imagery; die Quellenangabe
  bleibt sichtbar.
- „Meine Position" nutzt ausschließlich die Browser-Geolocation und erst nach
  Zustimmung. Die Position wird **nicht** im Backend gespeichert.
- „Fußweg zum Raum" fragt einen öffentlichen OSRM-Fußgänger-Router an und zeichnet
  Route, Distanz, Dauer und Richtungspfeile.
- `backend_send` holt beim Start der Simulation den VRR-EFA-Abfahrtsmonitor für
  „Dortmund Universität S". Der Datensatz läuft dann über Factory → MQTT →
  `backend_receive` → REST. Ist VRR nicht erreichbar, erscheint ein klar
  gekennzeichneter Offline-Fallback.
- Fällt das Leaflet-CDN aus, bleibt die lokale Campus-Skizze als Fallback bedienbar.

Diese externen Dienste sind Best-Effort und ohne Verfügbarkeitsgarantie. Für eine
produktive App würden Tiles und Routing über vertraglich abgesicherte oder selbst
betriebene Dienste laufen.

## Software Development Lifecycle

So arbeiten wir im Repository:

1. Jede Aufgabe entsteht als GitHub-Issue aus den Vorlagen in `.github/ISSUE_TEMPLATE/`
   und landet auf dem Kanban-Board „Virtual College".
2. Umfang, Akzeptanzkriterien und Zuständigkeit klären wir im Issue, nicht im Chat.
   Die Akzeptanzkriterien stehen zusätzlich in [ANFORDERUNGEN.md](ANFORDERUNGEN.md).
3. Entwickelt wird auf einem Branch pro Issue. Vor dem Push muss `./mvnw verify`
   grün sein.
4. Pull Request mit der PR-Vorlage öffnen, Review abwarten, Feedback einarbeiten.
5. Merge nach `main` erst, wenn die Pre-Merge-Checks durch sind — der Workflow baut
   alle Maven-Module und zusätzlich die deploybaren Container-Images.
6. Der Release-Workflow paketiert anschließend die Module, die sich fürs
   Container-Publishing angemeldet haben.

Bestehende Funktionen ändern wir nicht nebenbei mit: Wenn etwas Umgebautes nicht zur
User Story gehört, wird ein eigenes Issue daraus.

## Adding More Modules

1. Neuen Modulordner mit eigener `pom.xml` anlegen.
2. Das Modul in der Wurzel-`pom.xml` unter `<modules>` eintragen.
3. `./mvnw verify` im Hauptordner laufen lassen.
4. Wenn es ein deploybarer Dienst ist, muss dabei ein startfähiges Spring-Boot-Jar
   herauskommen.
5. Soll das Modul auch in Docker laufen, einen Service in `docker-compose.yml`
   ergänzen — `Dockerfile.java` baut jedes Modul über `--build-arg MODULE=<name>`.

Soll `release.yaml` ein Container-Image veröffentlichen, gehört diese Eigenschaft in die
`pom.xml` des Moduls:

```xml
<properties>
    <container.image.enabled>true</container.image.enabled>
</properties>
```

Der Release-Workflow findet die deploybaren Module über die Maven-Metadaten, es muss
also keine Ordnerliste in GitHub Actions gepflegt werden. Aktuell hat sich noch keines
unserer Module dafür angemeldet — wir bauen die Images über `docker-compose.yml`.

