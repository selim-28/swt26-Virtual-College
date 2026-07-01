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
# Backend bauen und pruefen
./mvnw verify

# Frontend bauen und pruefen
cd frontend
npm run build
```

End-to-End ueber das Frontend:

```bash
# Campus-Karte lesen
curl http://localhost:8090/api/campus/karte

# Raumreservierung speichern
curl -X POST http://localhost:8090/api/campus/reservierungen \
  -H 'Content-Type: application/json' \
  -d '{"raumId":"g40-labor105","reserviertVon":"Test Student","von":"12:00","bis":"13:00"}'

# Pinwand-Post speichern
curl -X POST http://localhost:8090/api/pinwand/posts \
  -H 'Content-Type: application/json' \
  -d '{"autor":"Test Student","rolle":"Student","kategorie":"Lerngruppe","titel":"React-Test","inhalt":"Pinwand-Test"}'

# Simulation starten: backend_send -> MQTT -> backend_receive -> MariaDB
curl -X POST http://localhost:8090/api/simulation/start
curl http://localhost:8090/api/mensa/historie

# Danach den letzten via MQTT empfangenen VRR-Snapshot lesen
curl http://localhost:8090/api/transit/abfahrten
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
                      auth/        Login/Registrierung, Passwort-Hashing (PBKDF2)
                      mensa/       Speiseplan + MQTT-Empfang (Adapter)
                      transit/     via MQTT empfangene VRR-Abfahrten
                      persistenz/  JPA-Entities + Repositories (Datenbank)
backend_send/       Simuliertes Fremdsystem (Port 8084), Paket:
                      simulation/  DatenGenerator (Factory) + MqttSender (publish)
frontend/           React/Vite-App plus Nginx-Proxy (Port 8090)
mosquitto/          MQTT-Broker-Konfiguration
docs/erklaerung.html Konzepte + Diagramme + Klausur/Aufgaben
docs/code-verstehen.html Rundgang durch den echten Code (was liegt wo, wie fliesst es)
PRAESENTATION.md    Spickzettel + Ablauf fuer die Abschlusspraesentation
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

Zwei Lern-Dokumente, die zusammengehoeren:

- [docs/erklaerung.html](docs/erklaerung.html) — **Konzepte & Klausur**: Verteilungsdiagramm,
  OOD-Klassendiagramme, die drei Entwurfsmuster Factory/Adapter/Observer als Theorie.
- [docs/code-verstehen.html](docs/code-verstehen.html) — **der echte Code**: Architektur,
  Ordnerstruktur, drei Datenfluss-Szenarien und jede Klasse Paket fuer Paket erklaert.
  Ideal, um komplett zu verstehen, was im Projekt steckt.

> **Hinweis Tests:** Build- und End-to-End-Tests (`./mvnw verify`, `npm run build`,
> die curl-Aufrufe oben) laufen erfolgreich. Eigene JUnit-Fachtests gibt es noch
> nicht – das ist die naechste sinnvolle Politur, aber keine Voraussetzung fuer die
> Praesentation.

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
