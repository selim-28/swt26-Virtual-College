# Burndown-Chart – Virtual College

## Zweck
Das Burndown-Chart visualisiert pro Sprint, wie viel Arbeit (in **Story Points**) noch offen ist. Es macht früh sichtbar, ob das Team im Plan liegt oder ob nachgesteuert werden muss.

## Achsen

- **X-Achse:** Tage des Sprints (Tag 1 bis letzter Sprinttag)
- **Y-Achse:** verbleibende Story Points (Summe aller offenen Stories im Sprint)

## Linien

| Linie | Bedeutung |
|-------|-----------|
| **Ideal-Linie** | Gerade Linie von Start-Punkten auf Tag 1 bis 0 am Sprint-Ende. |
| **Ist-Linie**   | Tatsächlicher Fortschritt – wird täglich aktualisiert (z. B. nach dem Daily). |

## ASCII-Vorlage

```text
Story Points
   80 |* 
   70 |  *
   60 |    \
   50 |      *  Ist
   40 |        \           *
   30 |          *       /
   20 |            *  /
   10 |              X    Ideal
    0 |__________________________
       1  2  3  4  5  6  7  8   Tag
```

## Beispiel: Sprint 1 (Vorschlag)

Stories: US-1.1, US-1.4, US-2.1, US-4.1, US-6.1

| Story | Schätzung (SP) |
|-------|:--------------:|
| US-1.1 Stundenplan ansehen           | 5 |
| US-1.4 Aufgaben verwalten            | 5 |
| US-2.1 Tutorium anlegen              | 5 |
| US-4.1 Direktnachricht senden        | 8 |
| US-6.1 Übersichts-Dashboard          | 5 |
| **Summe**                            | **28** |

Daraus ergibt sich ein Ideal-Burndown von 28 → 0 SP über die Sprint-Dauer.

## Datenerfassung

| Tag | Ideal (SP) | Ist (SP) | Bemerkung |
|----:|:----------:|:--------:|-----------|
| 1   | 28         | 28       | Sprint-Start |
| 2   | 24         |          |  |
| 3   | 20         |          |  |
| 4   | 16         |          |  |
| 5   | 12         |          |  |
| 6   |  8         |          |  |
| 7   |  4         |          |  |
| 8   |  0         |          | Sprint-Ende |

> Wir aktualisieren den Ist-Wert jeden Werktag im Daily Stand-up.

## Werkzeug
Wir tragen die Werte in eine Tabelle (Google Sheets / Excel) ein und erzeugen daraus automatisch das Liniendiagramm. Die Tabelle wird im Repo unter `docs/sprints/burndown-sprintN.csv` versioniert (wird ab Praktikum 4 angelegt).

## Lese-Hinweise

- **Ist-Linie über Ideal-Linie** → wir sind hinter dem Plan. Ursachen prüfen (zu viel eingeplant? Blocker? Krankheit?).
- **Ist-Linie unter Ideal-Linie** → wir sind schneller als geplant. Ggf. weitere Story aus Backlog ziehen.
- **Ist-Linie flach** → keine Story wurde abgeschlossen. Achtung auf zu große Stories oder Blocker.
