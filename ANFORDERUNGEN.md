# Virtual College – Epics und User Stories

Diese Datei ist das Product Backlog des Projekts.
Sie beschreibt die aktuell vereinbarten Epics, User Stories und Akzeptanzkriterien.

## Arbeitsregeln für die Weiterentwicklung

- Vor Änderungen zuerst prüfen, welche Anforderungen bereits umgesetzt sind.
- Bestehende Funktionen nicht ohne ausdrücklichen Auftrag entfernen oder grundlegend verändern.
- Akzeptanzkriterien als Grundlage für Implementierung und Tests verwenden.
- Änderungen möglichst einer User Story zuordnen und die Issue-Nummer nennen.
- Die GitHub-Issues im Repository `selim-28/swt26-Virtual-College` sind die zugehörigen Kanban-Karten.
- Ein offenes Kontrollkästchen bedeutet eine prüfbare Anforderung, nicht automatisch, dass die Funktion fehlt.
- Architekturgrenzen respektieren: React/Nginx im Frontend, `backend_receive` als Hauptbackend,
  `backend_send` als simuliertes Fremdsystem, MQTT/Mosquitto für asynchrone Kommunikation
  und MariaDB für Persistenz.

---

## EPIC 1 – Benutzerkonto und Anmeldung

**GitHub-Issue:** #19

**Ziel:** Nutzer sollen Virtual College mit einem persönlichen und geschützten Benutzerkonto verwenden können.

### US 1.1 – Benutzerkonto registrieren

**GitHub-Issue:** #20

Als neuer Nutzer möchte ich ein Benutzerkonto mit Name, E-Mail-Adresse, Rolle und Passwort erstellen,
damit ich Virtual College verwenden kann.

**Akzeptanzkriterien:**

- [ ] Alle Pflichtfelder müssen ausgefüllt sein.
- [ ] Eine bereits verwendete E-Mail-Adresse wird abgelehnt.
- [ ] Das Passwort wird nicht im Klartext gespeichert.
- [ ] Nach erfolgreicher Registrierung kann sich der Nutzer anmelden.

### US 1.2 – Mit Benutzerkonto anmelden

**GitHub-Issue:** #21

Als registrierter Nutzer möchte ich mich mit meiner E-Mail-Adresse und meinem Passwort anmelden,
damit ich Zugriff auf die Funktionen der Anwendung erhalte.

**Akzeptanzkriterien:**

- [ ] Gültige Zugangsdaten führen zur Anmeldung.
- [ ] Bei falschen Zugangsdaten erscheint eine verständliche Fehlermeldung.
- [ ] Nach der Anmeldung werden Name und Rolle des Nutzers angezeigt.
- [ ] Geschützte Funktionen sind erst nach der Anmeldung erreichbar.

### US 1.3 – Vom Benutzerkonto abmelden

**GitHub-Issue:** #22

Als angemeldeter Nutzer möchte ich mich abmelden, damit mein Konto auf einem gemeinsam verwendeten
Gerät geschützt bleibt.

**Akzeptanzkriterien:**

- [ ] Die Sitzung wird beendet.
- [ ] Der gespeicherte Zugangstoken wird entfernt.
- [ ] Anschließend wird wieder die Anmeldeseite angezeigt.
- [ ] Geschützte Funktionen sind danach nicht mehr erreichbar.

---

## EPIC 2 – Digitaler Campus und Navigation

**GitHub-Issue:** #9

**Ziel:** Studierende sollen sich auf dem Campus orientieren, Räume finden und einen Fußweg zu ihrem Ziel erhalten.

### US 2.1 – Campuskarte anzeigen und Raum auswählen

**GitHub-Issue:** #3

Als Student im ersten Semester möchte ich eine interaktive Campuskarte verwenden und einen Raum auswählen,
damit ich mich orientieren und Informationen zu meinem Ziel erhalten kann.

**Akzeptanzkriterien:**

- [ ] Gebäude und Räume werden auf der Karte angezeigt.
- [ ] Die Karte kann vergrößert und verkleinert werden.
- [ ] Zwischen Straßen- und Satellitenansicht kann gewechselt werden.
- [ ] Ein ausgewählter Raum wird hervorgehoben.
- [ ] Raumname, Veranstaltungen und Reservierungen werden angezeigt.
- [ ] Bei Ausfall des Kartendienstes bleibt eine Ersatzkarte nutzbar.

### US 2.2 – Eigenen Standort auf der Karte anzeigen

**GitHub-Issue:** #7

Als Student möchte ich nach meiner Zustimmung meinen aktuellen Standort auf der Campuskarte sehen,
damit ich meine Position auf dem Campus erkenne.

**Akzeptanzkriterien:**

- [ ] Die Anwendung fragt vorab nach der Standortfreigabe.
- [ ] Ohne Zustimmung wird kein Standort verwendet.
- [ ] Der eigene Standort wird auf der Karte markiert.
- [ ] Der Standort wird nicht dauerhaft im Backend gespeichert.
- [ ] Fehler bei der Standortermittlung werden verständlich angezeigt.

### US 2.3 – Fußweg zu einem Raum berechnen

**GitHub-Issue:** #23

Als Student möchte ich den Fußweg von meinem Standort zu einem ausgewählten Raum sehen,
damit ich den Raum schnell finde.

**Akzeptanzkriterien:**

- [ ] Startpunkt ist der freigegebene aktuelle Standort.
- [ ] Zielpunkt ist der ausgewählte Raum.
- [ ] Die Route wird auf der Karte dargestellt.
- [ ] Entfernung und ungefähre Dauer werden angezeigt.
- [ ] Richtungspfeile verdeutlichen den Weg.
- [ ] Fehler des Routingdienstes werden verständlich angezeigt.

---

## EPIC 3 – Raumverwaltung und Reservierung

**GitHub-Issue:** #10

**Ziel:** Studierende sollen Raumbelegungen einsehen und freie Räume für einen gewünschten Zeitraum reservieren können.

### US 3.1 – Raumbelegung einsehen

**GitHub-Issue:** #6

Als Student möchte ich Veranstaltungen und Reservierungen eines Raumes sehen,
damit ich erkennen kann, wann der Raum belegt ist.

**Akzeptanzkriterien:**

- [ ] Veranstaltungen werden mit ihrer Uhrzeit angezeigt.
- [ ] Vorhandene Reservierungen werden angezeigt.
- [ ] Die Informationen gehören zum aktuell ausgewählten Raum.
- [ ] Freie Zeiträume können aus der Übersicht abgeleitet werden.

### US 3.2 – Raum reservieren

**GitHub-Issue:** #24

Als Student möchte ich einen freien Raum für einen Zeitraum reservieren,
damit ich ihn beispielsweise für eine Lerngruppe nutzen kann.

**Akzeptanzkriterien:**

- [ ] Vor der Reservierung muss ein Raum ausgewählt sein.
- [ ] Beginn und Ende müssen angegeben werden.
- [ ] Die Reservierung wird dem angemeldeten Nutzer zugeordnet.
- [ ] Nach erfolgreicher Reservierung erscheint sie in der Raumübersicht.
- [ ] Die Reservierung wird dauerhaft in der Datenbank gespeichert.

---

## EPIC 4 – Virtuelle Pinwand

**GitHub-Issue:** #11

**Ziel:** Campus-Mitglieder sollen Informationen veröffentlichen, lesen und durch Likes oder
Benachrichtigungen miteinander interagieren können.

### US 4.1 – Pinwand-Beiträge anzeigen und erstellen

**GitHub-Issue:** #8

Als angemeldeter Nutzer möchte ich Pinwand-Beiträge lesen und einen eigenen Beitrag erstellen,
damit ich Informationen mit der Campus-Community austauschen kann.

**Akzeptanzkriterien:**

- [ ] Autor, Rolle, Kategorie, Titel und Inhalt eines Beitrags werden angezeigt.
- [ ] Kategorie, Titel und Inhalt sind beim Erstellen Pflichtfelder.
- [ ] Autor und Rolle werden aus dem angemeldeten Konto übernommen.
- [ ] Ein neuer Beitrag erscheint anschließend auf der Pinwand.
- [ ] Der Beitrag wird dauerhaft in der Datenbank gespeichert.

### US 4.2 – Pinwand-Beitrag liken

**GitHub-Issue:** #25

Als Nutzer möchte ich einen hilfreichen Pinwand-Beitrag liken,
damit ich Zustimmung oder Interesse ausdrücken kann.

**Akzeptanzkriterien:**

- [ ] Ein Beitrag kann über eine sichtbare Schaltfläche gelikt werden.
- [ ] Der Like-Zähler wird unmittelbar aktualisiert.
- [ ] Die Änderung wird dauerhaft gespeichert.
- [ ] Ein Fehler wird verständlich angezeigt und verfälscht den Zähler nicht.

### US 4.3 – Über neue Pinwand-Beiträge benachrichtigt werden

**GitHub-Issue:** #26

Als Beobachter der Pinwand möchte ich bei einem neuen Beitrag benachrichtigt werden,
damit ich keine wichtigen Informationen verpasse.

**Akzeptanzkriterien:**

- [ ] Registrierte Beobachter werden über einen neuen Beitrag informiert.
- [ ] Die Benachrichtigung enthält Informationen zum Beitrag.
- [ ] Nicht registrierte Beobachter erhalten keine Benachrichtigung.
- [ ] Das Benachrichtigen erfolgt automatisch nach dem Erstellen eines Beitrags.

---

## EPIC 5 – Mensa-Informationen

**GitHub-Issue:** #12

**Ziel:** Studierende sollen aktuelle Mensagerichte und bereits empfangene Mensadaten zentral abrufen können.

### US 5.1 – Aktuellen Mensa-Speiseplan anzeigen

**GitHub-Issue:** #5

Als Student möchte ich den aktuellen Speiseplan sehen, damit ich meine Mahlzeit planen kann.

**Akzeptanzkriterien:**

- [ ] Verfügbare Gerichte werden angezeigt.
- [ ] Zu jedem Gericht wird der Preis angezeigt.
- [ ] Die Datenquelle ist erkennbar.
- [ ] Neu empfangene Fremddaten werden automatisch verarbeitet.
- [ ] Ein Ausfall externer Daten bringt die Anwendung nicht zum Absturz.

### US 5.2 – Mensa-Historie anzeigen

**GitHub-Issue:** #27

Als Nutzer möchte ich bereits empfangene Mensadaten sehen,
damit ich die Speicherung und Entwicklung der Daten nachvollziehen kann.

**Akzeptanzkriterien:**

- [ ] Gespeicherte Gerichte werden mit Preis und Empfangszeit angezeigt.
- [ ] Die Gesamtzahl der gespeicherten Einträge ist sichtbar.
- [ ] Die angezeigten Daten stammen aus der Datenbank.
- [ ] Neue empfangene Gerichte erscheinen anschließend in der Historie.

---

## EPIC 6 – Öffentlicher Nahverkehr

**GitHub-Issue:** #28

**Ziel:** Studierende sollen aktuelle Abfahrtsinformationen für den Campus erhalten und auch bei
Ausfall des externen Verkehrsdienstes eine nutzbare Rückmeldung bekommen.

### US 6.1 – Aktuelle ÖPNV-Abfahrten anzeigen

**GitHub-Issue:** #29

Als Student möchte ich aktuelle Abfahrten an der Haltestelle Dortmund Universität S sehen,
damit ich meine Weiterfahrt planen kann.

**Akzeptanzkriterien:**

- [ ] Linie, Ziel und Abfahrtszeit werden angezeigt.
- [ ] Die verwendete Haltestelle ist sichtbar.
- [ ] Die Daten werden über das simulierte Fremdsystem bereitgestellt.
- [ ] Neu empfangene Abfahrten können ohne Seitenneustart geladen werden.

### US 6.2 – Ersatzdaten bei Ausfall des Verkehrsdienstes verwenden

**GitHub-Issue:** #30

Als Nutzer möchte ich auch bei einem Ausfall des externen Verkehrsdienstes Informationen erhalten,
damit die Anwendung weiterhin verwendbar und demonstrierbar bleibt.

**Akzeptanzkriterien:**

- [ ] Bei einem Ausfall werden Ersatzdaten bereitgestellt.
- [ ] Ersatzdaten sind eindeutig als Offline-Daten gekennzeichnet.
- [ ] Der Ausfall bringt weder Frontend noch Backends zum Absturz.
- [ ] Die Anwendung informiert verständlich über die eingeschränkte Datenquelle.

---

## EPIC 7 – Fremdsystem und asynchrone Datenübertragung

**GitHub-Issue:** #31

**Ziel:** Externe Mensa-, ÖPNV- und Auslastungsdaten sollen simuliert, über MQTT übertragen und
vom Hauptsystem verarbeitet werden.

### US 7.1 – Fremddatensimulation starten

**GitHub-Issue:** #32

Als Nutzer möchte ich die Fremddatensimulation starten,
damit neue Mensa-, ÖPNV- und Auslastungsdaten erzeugt werden.

**Akzeptanzkriterien:**

- [ ] Die Simulation kann über die Benutzeroberfläche gestartet werden.
- [ ] Mensa-, ÖPNV- und Auslastungsdaten werden erzeugt.
- [ ] Der Nutzer erhält eine verständliche Rückmeldung.
- [ ] Ein erneuter Start ist möglich, ohne die Anwendung neu zu starten.

### US 7.2 – Daten asynchron über MQTT übertragen

**GitHub-Issue:** #33

Als Systembetreiber möchte ich Fremddaten über einen Message-Broker übertragen,
damit Sender und Empfänger voneinander entkoppelt sind.

**Akzeptanzkriterien:**

- [ ] Das sendende Backend veröffentlicht die Daten über MQTT.
- [ ] Mosquitto dient als Message-Broker.
- [ ] Das Hauptbackend empfängt Nachrichten über das vereinbarte Topic.
- [ ] Ein vorübergehend nicht erreichbarer Empfänger führt nicht zum Absturz des Senders.
- [ ] Verbindungsfehler werden protokolliert.

### US 7.3 – Empfangene Fremddaten verarbeiten und speichern

**GitHub-Issue:** #34

Als System möchte ich empfangene Rohdaten in interne Datenobjekte umwandeln,
damit sie im Frontend verwendet und dauerhaft gespeichert werden können.

**Akzeptanzkriterien:**

- [ ] MQTT-Nachrichten werden als JSON empfangen.
- [ ] Rohdaten werden geprüft und in interne Objekte umgewandelt.
- [ ] Gültige Daten werden über REST bereitgestellt.
- [ ] Persistierbare Daten werden in MariaDB gespeichert.
- [ ] Ungültige Daten führen nicht zum Absturz des Systems.

---

## EPIC 8 – Betrieb und Persistenz

**GitHub-Issue:** #35

**Ziel:** Das Gesamtsystem soll reproduzierbar gestartet werden und wichtige Nutzungs- und
Fremddaten dauerhaft speichern.

### US 8.1 – Gesamtsystem mit Docker starten

**GitHub-Issue:** #36

Als Entwickler oder Prüfer möchte ich das gesamte Projekt mit einem Befehl starten,
damit ich die Anwendung ohne manuelle Einrichtung testen kann.

**Akzeptanzkriterien:**

- [ ] Frontend, beide Backends, Mosquitto, MariaDB und Adminer werden gestartet.
- [ ] Die Dienste können im Docker-Netzwerk miteinander kommunizieren.
- [ ] Das Frontend ist über Port 8090 erreichbar.
- [ ] Das Hauptbackend wartet auf eine betriebsbereite Datenbank.
- [ ] Das System kann gemeinsam wieder beendet werden.

### US 8.2 – Daten dauerhaft speichern

**GitHub-Issue:** #37

Als Nutzer möchte ich, dass wichtige Daten dauerhaft gespeichert werden,
damit sie nach einem Neustart weiterhin vorhanden sind.

**Akzeptanzkriterien:**

- [ ] Benutzerkonten werden gespeichert.
- [ ] Raumreservierungen werden gespeichert.
- [ ] Pinwand-Beiträge und Likes werden gespeichert.
- [ ] Empfangene Mensadaten werden gespeichert.
- [ ] Die Daten bleiben nach einem Container-Neustart erhalten.

### US 8.3 – Gespeicherte Daten einsehen

**GitHub-Issue:** #38

Als Entwickler oder Prüfer möchte ich die gespeicherten Daten über eine Weboberfläche einsehen,
damit ich die Persistenz nachweisen kann.

**Akzeptanzkriterien:**

- [ ] Adminer ist über den Browser erreichbar.
- [ ] Eine Anmeldung an MariaDB ist möglich.
- [ ] Tabellen und gespeicherte Datensätze können angezeigt werden.
- [ ] Benutzerkonten, Reservierungen, Pinwand-Posts und Mensadaten sind nachvollziehbar.

---

## Übersicht

| Epic | User Stories | GitHub-Issue |
|---|---:|---:|
| Benutzerkonto und Anmeldung | 3 | #19 |
| Digitaler Campus und Navigation | 3 | #9 |
| Raumverwaltung und Reservierung | 2 | #10 |
| Virtuelle Pinwand | 3 | #11 |
| Mensa-Informationen | 2 | #12 |
| Öffentlicher Nahverkehr | 2 | #28 |
| Fremdsystem und asynchrone Datenübertragung | 3 | #31 |
| Betrieb und Persistenz | 3 | #35 |

**Gesamt:** 8 Epics und 21 User Stories.
