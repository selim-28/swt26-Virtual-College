# Virtual College

Virtual College ist eine SWT2-Demo fuer einen digitalen 2D-Campus. Das Projekt zeigt nicht nur Oberflaeche, sondern eine echte verteilte Kette:

Browser/React -> Nginx -> Spring Boot Backends -> MQTT/Mosquitto -> MariaDB.

Die aktuelle Version setzt den Kanban-Entwurf als benutzbare Web-App um: Login/Registrierung, echte 2D-Campus-Karte mit Straßen-/Satellitenansicht, Browser-Standort, Fußweg mit Richtungspfeilen, Raumreservierung, Pinwand, Likes, Mensa, VRR-Abfahrten, Simulation und Datenbank-Historie.

> **Anmeldung:** Die App ist hinter einem Login. Demo-Konto zum Ausprobieren:
> `student@fh-dortmund.de` / `passwort123` (oder ein eigenes Konto registrieren).

## Schnellstart mit Docker

Im Projekt-Hauptordner:

```bash
docker compose up --build
```

Danach im Browser oeffnen:

```text
http://localhost:8090
```

Docker startet diese Services:

```text
frontend         React/Vite gebaut und von Nginx ausgeliefert, Port 8090
backend_receive Spring Boot Hauptbackend, Port 8083
backend_send    Simuliertes Fremdsystem, Port 8084
mosquitto       MQTT-Broker, Port 1883
mariadb         Datenbank, Port 3306
adminer         Datenbank im Browser ansehen (optional), Port 8091
```

Stoppen:

```bash
docker compose down
```

## Wichtige Tests

```bash
# Backend: bauen + automatische Tests (u. a. 22 Auth-/Security-Tests, EPIC 1)
./mvnw -pl backend_receive test

# Backend: kompletter Build inkl. Tests
./mvnw verify

# Frontend: automatische Unit-Tests (Karten-Helfer, EPIC 2) – ohne Zusatzbibliothek
cd frontend
npm test          # nutzt Nodes eingebauten Runner (node --test), 12 Tests

# Frontend: Produktions-Build
npm run build
```

End-to-End ueber das Frontend (die API-Endpunkte sind seit Spring Security + JWT
geschuetzt – zuerst einloggen, dann das Token als `Authorization: Bearer` mitschicken):

```bash
# 1) Einloggen und Token merken (Demo-Konto)
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"student@fh-dortmund.de","passwort":"passwort123"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

# 2) Campus-Karte lesen (mit Token)
curl http://localhost:8090/api/campus/karte -H "Authorization: Bearer $TOKEN"

# 3) Raumreservierung speichern
curl -X POST http://localhost:8090/api/campus/reservierungen \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"raumId":"g40-labor105","reserviertVon":"Test Student","von":"12:00","bis":"13:00"}'

# 4) Pinwand-Post speichern
curl -X POST http://localhost:8090/api/pinwand/posts \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"autor":"Test Student","rolle":"Student","kategorie":"Lerngruppe","titel":"React-Test","inhalt":"Pinwand-Test"}'

# 5) Simulation starten: backend_send -> MQTT -> backend_receive -> MariaDB
curl -X POST http://localhost:8090/api/simulation/start
curl http://localhost:8090/api/mensa/historie -H "Authorization: Bearer $TOKEN"

# 6) Danach den letzten via MQTT empfangenen VRR-Snapshot lesen
curl http://localhost:8090/api/transit/abfahrten -H "Authorization: Bearer $TOKEN"
```

## Datenbank Anzeigen

### Einfach: im Browser mit Adminer (empfohlen)

Kein Konsolen-Befehl noetig. Nach `docker compose up --build` im Browser oeffnen:

```text
http://localhost:8091
```

Login:

```text
System:    MySQL / MariaDB
Server:    mariadb
Benutzer:  vcuser
Passwort:  vcpass
Datenbank: virtualcollege
```

Dann links auf eine Tabelle klicken (`benutzerkonto`, `gespeicherte_reservierung`,
`gespeicherter_pinwand_post`, `gespeichertes_gericht`) und die Zeilen ansehen –
oder oben eigene SQL-Abfragen ausfuehren.

### Alternativ: ueber die Konsole

```bash
# Tabellen anzeigen
docker compose exec -T mariadb mariadb -u vcuser -pvcpass virtualcollege -e 'SHOW TABLES'

# Reservierungen
docker compose exec -T mariadb mariadb -u vcuser -pvcpass virtualcollege \
  -e 'SELECT id, raum_name, reserviert_von, von, bis, status FROM gespeicherte_reservierung ORDER BY id DESC LIMIT 5'

# Pinwand
docker compose exec -T mariadb mariadb -u vcuser -pvcpass virtualcollege \
  -e 'SELECT id, autor, rolle, kategorie, titel, likes FROM gespeicherter_pinwand_post ORDER BY id DESC LIMIT 5'

# Mensa-Historie
docker compose exec -T mariadb mariadb -u vcuser -pvcpass virtualcollege \
  -e 'SELECT id, name, preis, empfangen_am FROM gespeichertes_gericht ORDER BY id DESC LIMIT 5'
```

## Projektstruktur

```text
backend_receive/    Hauptbackend (Port 8083), Pakete:
                      campus/      Campus-Karte, Raeume, Reservierungen (Fassade)
                      pinwand/     Posts, Likes, Benachrichtigungen (Observer)
                      nutzer/      Nutzer + Student/Betreuer/Verwaltung (Vererbung)
                      auth/        Login/Registrierung, Spring Security + JWT, Passwort-Hashing (PBKDF2)
                      mensa/       Speiseplan + MQTT-Empfang (Adapter)
                      transit/     via MQTT empfangene VRR-Abfahrten
                      persistenz/  JPA-Entities + Repositories (Datenbank)
backend_send/       Simuliertes Fremdsystem (Port 8084), Paket:
                      simulation/  DatenGenerator (Factory) + MqttSender (publish)
frontend/           React/Vite-App plus Nginx-Proxy (Port 8090)
mosquitto/          MQTT-Broker-Konfiguration
docs/erklaerung.html DIE komplette Doku in EINER Datei: Konzepte, Diagramme,
                     Praktikums-Nachweis, Compliance UND Code-Rundgang (Block E)
PRAESENTATION.md    Spickzettel + Ablauf fuer die Abschlusspraesentation
unterlagen/         Lernmaterial & Alt-Dateien (Folien-PDF, Kanban-PDFs, altes OOD)
docker-compose.yml  Ein-Knopf-Start: 5 App-Container + Adminer (DB-Ansicht)
```

## Echte Karte, Navigation und Live-ÖPNV

- `CampusService` liefert reale WGS84-Koordinaten des Campus Emil-Figge-Straße 38–44.
- Leaflet zeigt OpenStreetMap oder Esri World Imagery; die Quellenangabe bleibt sichtbar.
- „Meine Position“ nutzt ausschließlich die Browser-Geolocation nach Zustimmung. Die Position wird nicht im Backend gespeichert.
- „Fußweg zum Raum“ nutzt einen öffentlichen OSRM-Fußgänger-Router und zeichnet Route, Distanz, Dauer und Richtungspfeile.
- `backend_send` fragt beim Start der Simulation den VRR-EFA-Abfahrtsmonitor für „Dortmund Universität S“ ab. Danach läuft der Datensatz über Factory → MQTT → `backend_receive` → REST. Ist VRR nicht erreichbar, erscheint ein klar gekennzeichneter Offline-Fallback.
- Ist das Leaflet-CDN nicht erreichbar, bleibt die bisherige lokale Campus-Skizze als Fallback bedienbar.

Externe Dienste benötigen Internet und sind Best-Effort-Dienste ohne Verfügbarkeitsgarantie. Für eine produktive App würden Tiles und Routing über vertraglich abgesicherte bzw. selbst betriebene Dienste laufen.

Die komplette Lern-Dokumentation steht in **einer** Datei:

- [docs/erklaerung.html](docs/erklaerung.html) — in 5 Bloecken: **A** Konzepte
  (Verteilungsdiagramm, MQTT, REST, …) · **B** Praktikums-Stand · **C** Betrieb &
  Umsetzung (Docker, Auth/Rollen, Karte) · **D** Nachweise (97 Tests, alle 8 EPICs,
  Praktikums-Aufgaben 1–4, **Compliance**) · **E** Code-Rundgang fuer Einsteiger
  (Ordnerbaum, 4 Datenfluesse, jede Klasse Paket fuer Paket, Glossar).
  (`docs/code-verstehen.html` leitet nur noch dorthin weiter.)

> **Tests:** **97 automatische Tests** sichern das Projekt ab – 60 in
> `backend_receive`, 10 in `backend_send` (`./mvnw test`) und 27 im Frontend
> (`cd frontend && npm test`). Details: erklaerung.html, Kapitel 21.

## Praesentationsfluss

1. Folien: Verteilungsdiagramm und OOD kurz erklaeren.
2. `docker compose up --build` starten.
3. `http://localhost:8090` zeigen.
4. Campus-Karte oeffnen und einen Raum reservieren.
5. Pinwand-Post erstellen und liken.
6. Live-Daten oeffnen und Simulation starten.
7. Datenbank zeigen: entweder Adminer (`http://localhost:8091`) oder direkt per Konsole.

Den kompakten Spickzettel mit Ablauf, Mustern und Pruefungsfragen findest du in
[PRAESENTATION.md](PRAESENTATION.md). Die ausfuehrliche Erklaerung steht in
[docs/erklaerung.html](docs/erklaerung.html), besonders Kapitel 15 bis 17.
