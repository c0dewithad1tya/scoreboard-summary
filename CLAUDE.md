# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Live Football World Cup Score Board — a simple in-memory library (not a REST API, CLI, or web service). Sportradar coding exercise.

## Scoreboard Operations

1. **Start match** — takes home team and away team, initial score 0-0
2. **Update score** — receives absolute home and away scores
3. **Finish match** — removes match from scoreboard
4. **Get summary** — matches ordered by total score (descending); ties broken by most recently started first

## Key Design Constraints

- In-memory storage only (use collections)
- TDD approach — write tests first, commit history matters
- SOLID principles, Clean Code, OO design
- Keep it simple — simplest working solution with edge cases handled

## Summary Sort Example

Given matches started in order a-e with scores: Mexico 0-Canada 5, Spain 10-Brazil 2, Germany 2-France 2, Uruguay 6-Italy 6, Argentina 3-Australia 1 — summary is:
1. Uruguay 6 - Italy 6 (total 12, started 4th)
2. Spain 10 - Brazil 2 (total 12, started 2nd)
3. Mexico 0 - Canada 5 (total 5)
4. Argentina 3 - Australia 1 (total 4)
5. Germany 2 - France 2 (total 4, started 3rd — before Argentina)
