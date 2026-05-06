# Kanban-Board – Virtual College

## Zweck
Das Kanban-Board ist unser zentrales Werkzeug für die Sprintplanung. Jede User Story (siehe [`user-stories.md`](./user-stories.md)) wird als Karte abgebildet und durch die Spalten von links nach rechts gezogen.

## Spalten

| Spalte | Bedeutung |
|--------|-----------|
| **Backlog**     | Alle erfassten Stories. Noch nicht eingeplant. |
| **To Do**       | Für den aktuellen Sprint ausgewählt; höchste Priorität oben. |
| **In Progress** | Wird aktiv bearbeitet. Höchstens 1 aktive Story pro Person (WIP-Limit). |
| **Review**      | Implementiert, wartet auf Code Review / PR-Approval. |
| **Done**        | Akzeptanzkriterien erfüllt, in `main` gemerged. |

## Karten-Format

Jede Karte enthält:

- **Titel** (z. B. „US-1.1 Stundenplan ansehen")
- **Kurzbeschreibung** im Story-Format
- **Akzeptanzkriterien** (Definition of Done)
- **Verantwortliche\*r**
- **Story Points** (Schätzung 1, 2, 3, 5, 8)
- **Sprint-Label** (Sprint 1 / 2 / 3)
- **Epic-Label** (E1 … E7)

## ASCII-Skizze des Boards

```text
+------------+------------+--------------+------------+------------+
|  Backlog   |   To Do    | In Progress  |  Review    |   Done     |
+------------+------------+--------------+------------+------------+
| US-3.3     | US-1.1     | US-2.1       | US-1.4     | US-1.2     |
| US-4.3     | US-2.2     | US-6.1       |            |            |
| US-5.2     | US-4.1     |              |            |            |
| US-6.3     | US-7.1     |              |            |            |
| US-7.3     |            |              |            |            |
| ...        |            |              |            |            |
+------------+------------+--------------+------------+------------+
```

## Werkzeug
Wir setzen das Kanban-Board in **GitHub Projects** um (alternativ Conceptboard / Miro). Vorteile von GitHub Projects:

- direkt mit den Issue-Templates verknüpft (`.github/ISSUE_TEMPLATE/user_story.md`)
- automatisches Verschieben durch Pull Requests möglich
- für alle Teammitglieder ohne Zusatztool zugänglich

## Arbeitsweise

1. **Story-Erstellung:** Jede User Story wird als GitHub Issue mit dem Template `User Story` angelegt und bekommt das Label `userstory` sowie ein Epic-Label (`epic-E1` … `epic-E7`).
2. **Sprint-Planung:** Zu Beginn jedes Sprints ziehen wir Stories vom Backlog in *To Do*.
3. **Bearbeitung:** Eine Person zieht ihre Story in *In Progress* und arbeitet auf einem eigenen Branch.
4. **Review:** Pull Request öffnen → Karte automatisch in *Review*.
5. **Abnahme:** Nach Merge prüft Story-Owner die Akzeptanzkriterien → Karte in *Done*.

## WIP-Limits

- *In Progress*: max. 1 Story pro Person (vermeidet Multitasking)
- *Review*: max. 3 offene PRs (zwingt zum Review statt Neu-Anfangen)
