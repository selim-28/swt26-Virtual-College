# User Stories – Virtual College

> Format: **„Als [Nutzer\*in] möchte ich [Funktion], damit [Nutzen]."**
>
> Eigenschaften: klein und in wenigen Tagen umsetzbar · klar verständlich · echter Mehrwert für Nutzer\*innen.

Die Stories sind nach Epic gruppiert (siehe [`epics.md`](./epics.md)). Jede Story trägt eine ID (`US-<Epic>.<Nr>`) und Akzeptanzkriterien (Definition of Done).

---

## E1 – Digitale Studienorganisation

### US-1.1 – Stundenplan ansehen
**Als** Studierende\*r **möchte ich** meinen Stundenplan digital einsehen, **damit** ich weiß, wann meine Vorlesungen stattfinden.

**DoD**
- [ ] Tages-, Wochen- und Semesteransicht verfügbar
- [ ] Vorlesung zeigt Titel, Raum, Dozent\*in, Uhrzeit
- [ ] Funktioniert auf Desktop und mobil

### US-1.2 – Stundenplan importieren
**Als** Studierende\*r **möchte ich** meinen Stundenplan aus dem FH-System importieren, **damit** ich ihn nicht manuell pflegen muss.

**DoD**
- [ ] Import per ICS/Datei oder API
- [ ] Doppelte Einträge werden erkannt

### US-1.3 – Vorlesungen filtern
**Als** Studierende\*r **möchte ich** Vorlesungen nach Tag oder Modul filtern, **damit** ich relevante Termine schneller finde.

**DoD**
- [ ] Filter nach Tag, Modul, Dozent\*in
- [ ] Filter sind kombinierbar

### US-1.4 – Aufgaben verwalten
**Als** Studierende\*r **möchte ich** Aufgaben mit Fristen anlegen und abhaken, **damit** ich nichts vergesse.

**DoD**
- [ ] Aufgabe anlegen, bearbeiten, löschen
- [ ] Status: offen / in Arbeit / erledigt
- [ ] Fristanzeige farblich (grün/gelb/rot)

---

## E2 – Tutorien-Verwaltung

### US-2.1 – Tutorium anlegen
**Als** Tutor\*in **möchte ich** ein Tutorium mit Termin, Raum und Modul anlegen, **damit** sich Studierende anmelden können.

**DoD**
- [ ] Pflichtfelder validiert
- [ ] Tutorium erscheint in Suche der Studierenden

### US-2.2 – Anmeldungen verwalten
**Als** Tutor\*in **möchte ich** sehen, wer angemeldet ist, **damit** ich die Gruppengröße im Blick behalte.

**DoD**
- [ ] Teilnehmerliste mit Name, Matrikel, Status
- [ ] Maximalteilnehmerzahl konfigurierbar

### US-2.3 – Schnell-Feedback geben
**Als** Tutor\*in **möchte ich** vorgefertigte Rückmeldungen mit einem Klick versenden, **damit** ich häufige Antworten nicht jedes Mal neu formuliere.

**DoD**
- [ ] Mindestens 5 Standard-Vorlagen
- [ ] Eigene Vorlagen anlegen
- [ ] Versand an einzelne Studierende oder ganze Gruppe

### US-2.4 – Aufgabenliste der/des Tutor*in
**Als** Tutor\*in **möchte ich** eine Tagesübersicht meiner Aufgaben sehen, **damit** ich weiß, was heute zu erledigen ist.

**DoD**
- [ ] Heutige Termine, offene Anfragen, fällige Aufgaben auf einer Seite

---

## E3 – Peer-to-Peer-Lerngruppen

### US-3.1 – Lerngruppe finden
**Als** Studierende\*r **möchte ich** Lerngruppen zu meinen Modulen finden, **damit** ich gemeinsam mit anderen lernen kann.

**DoD**
- [ ] Suche nach Modul, Thema, Termin
- [ ] Gruppen-Detailseite mit Mitgliedern und nächstem Termin

### US-3.2 – Lerngruppe gründen
**Als** Studierende\*r **möchte ich** eine eigene Lerngruppe gründen, **damit** ich Mitlernende für ein Thema finde.

**DoD**
- [ ] Modul, Thema, Treffpunkt (digital/physisch), Maximalgröße angebbar
- [ ] Gruppe sofort sichtbar in der Suche

### US-3.3 – Gemeinsames Whiteboard
**Als** Lerngruppen-Mitglied **möchte ich** ein gemeinsames Whiteboard nutzen, **damit** wir online zusammen lernen können.

**DoD**
- [ ] Mehrere Personen zeichnen gleichzeitig
- [ ] Inhalte bleiben für die Gruppe gespeichert

### US-3.4 – Tutor\*in bei Bedarf hinzuziehen
**Als** Lerngruppen-Mitglied **möchte ich** einen Tutor / eine Tutorin zur Gruppe einladen, **damit** wir bei Bedarf fachliche Hilfe bekommen.

**DoD**
- [ ] Einladung per Klick aus Gruppe
- [ ] Tutor\*in sieht Anfrage in eigenem Dashboard

---

## E4 – Kommunikation zwischen den Zielgruppen

### US-4.1 – Direktnachricht senden
**Als** Studierende\*r **möchte ich** eine Tutor\*in direkt anschreiben, **damit** ich schnell eine Frage stellen kann.

**DoD**
- [ ] 1:1-Chat mit Lese-/Antwortstatus
- [ ] Nachrichten bleiben dauerhaft sichtbar

### US-4.2 – Gruppen-Chat
**Als** Tutor\*in **möchte ich** einen Gruppenchat mit allen Tutoriums-Teilnehmenden haben, **damit** Ankündigungen alle erreichen.

**DoD**
- [ ] Tutor\*in kann Nachricht an gesamte Gruppe senden
- [ ] Studierende können antworten; Tutor\*in kann Nur-Lesen-Modus aktivieren

### US-4.3 – Video-Call starten
**Als** Tutor\*in **möchte ich** einen Video-Call direkt aus einem Tutoriums-Termin starten, **damit** Online-Tutorien ohne externe Tools funktionieren.

**DoD**
- [ ] Call-Link wird beim Termin angezeigt
- [ ] Beitritt mit Mikro/Kamera-Toggle

---

## E5 – Materialien- und Dokumentenmanagement

### US-5.1 – Materialien hochladen
**Als** Tutor\*in **möchte ich** Lernunterlagen zu einem Tutorium hochladen, **damit** Studierende sie vor- und nachbereiten können.

**DoD**
- [ ] PDF/Bild/ZIP bis 50 MB
- [ ] Zugriff auf Tutoriums-Teilnehmende beschränkbar

### US-5.2 – Materialien herunterladen
**Als** Studierende\*r **möchte ich** Materialien meiner Tutorien herunterladen, **damit** ich sie offline nutzen kann.

**DoD**
- [ ] Einzel- und Sammeldownload (ZIP)
- [ ] Letzter Aktualisierungszeitpunkt sichtbar

---

## E6 – Vereinfachte digitale Verwaltung für Mitarbeitende

### US-6.1 – Übersichts-Dashboard für Mitarbeitende
**Als** Mitarbeiter\*in (z. B. Ralf Becker) **möchte ich** ein klares Dashboard mit großen Buttons, **damit** ich ohne Stress meine täglichen Aufgaben finde.

**DoD**
- [ ] Maximal 6 Kachel auf der Startseite
- [ ] Große Schrift, hoher Kontrast, einfache Sprache

### US-6.2 – Formulare mit Auto-Vorausfüllung
**Als** Mitarbeiter\*in **möchte ich**, dass bekannte Felder (Name, Abteilung, …) automatisch ausgefüllt werden, **damit** ich nicht jedes Mal alles neu eintippe.

**DoD**
- [ ] Profil-Daten werden automatisch übernommen
- [ ] Felder bleiben editierbar

### US-6.3 – Anfragen-Eingang
**Als** Mitarbeiter\*in **möchte ich** Anfragen von Studierenden gebündelt sehen und beantworten können, **damit** keine E-Mail untergeht.

**DoD**
- [ ] Liste mit Status (neu / in Bearbeitung / beantwortet)
- [ ] Vorlagen-Antworten verfügbar

---

## E7 – Benachrichtigungen und Erinnerungen

### US-7.1 – Erinnerung vor Vorlesung
**Als** Studierende\*r **möchte ich** Push-Benachrichtigungen vor Vorlesungen erhalten, **damit** ich pünktlich bin.

**DoD**
- [ ] Vorlaufzeit konfigurierbar (5 / 15 / 30 min)
- [ ] Pro Vorlesung deaktivierbar

### US-7.2 – Erinnerung an Aufgabe
**Als** Studierende\*r **möchte ich** an Aufgaben mit Frist erinnert werden, **damit** ich keine Abgabe verpasse.

**DoD**
- [ ] Standard-Erinnerung 24 h vor Frist
- [ ] Konfigurierbar pro Aufgabe

### US-7.3 – Hilfe-Anzeige für Tutor\*in
**Als** Tutor\*in **möchte ich** sehen, wenn ein\*e Studierende\*r mit einer Aufgabe hängt (farbiger Punkt), **damit** ich gezielt helfen kann.

**DoD**
- [ ] Status grün/gelb/rot je nach Bearbeitungszeit
- [ ] Klick öffnet Detailansicht

---

## Übersicht Story → Sprint (Vorschlag)

> Wird im Praktikum 4 (Sprint-Planung) finalisiert. Hier nur grobe Einschätzung als Diskussionsgrundlage.

| Sprint | Stories (Vorschlag) |
|--------|---------------------|
| Sprint 1 | US-1.1, US-1.4, US-2.1, US-4.1, US-6.1 |
| Sprint 2 | US-1.2, US-1.3, US-2.2, US-3.1, US-3.2, US-5.1, US-5.2, US-7.1 |
| Sprint 3 | US-2.3, US-2.4, US-3.3, US-3.4, US-4.2, US-4.3, US-6.2, US-6.3, US-7.2, US-7.3 |
