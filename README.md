# Live Football World Cup Score Board

A simple in-memory library that tracks ongoing football matches and their scores during the World Cup.

## Features

- **Start a match** — begins a new match with an initial score of 0–0
- **Update score** — sets absolute home and away scores for an ongoing match
- **Finish a match** — removes a completed match from the scoreboard
- **Get summary** — returns all ongoing matches ordered by total score (descending), with ties broken by most recently started

## Prerequisites

- Java 17+
- Maven 3.8+

## Build & Test

```bash
# Run all tests
mvn test

# Build the project
mvn clean package

# Run a single test
mvn test -Dtest="ScoreboardTest#getSummary_shouldMatchExampleFromRequirements"
```

## Usage

```java
Scoreboard scoreboard = new Scoreboard();

scoreboard.startMatch("Mexico", "Canada");
scoreboard.startMatch("Spain", "Brazil");

scoreboard.updateScore("Mexico", "Canada", 0, 5);
scoreboard.updateScore("Spain", "Brazil", 10, 2);

List<Match> summary = scoreboard.getSummary();
// 1. Spain 10 - Brazil 2
// 2. Mexico 0 - Canada 5

scoreboard.finishMatch("Mexico", "Canada");
```

## Assumptions

- Team names are case-insensitive (e.g., "mexico" and "Mexico" are treated as the same team)
- A team cannot play in two matches simultaneously
- Scores are absolute values (not incremental) and must be non-negative
- The same matchup (home vs away) cannot be started twice without finishing the first one
- Summary ordering uses insertion order to determine recency (no timestamps needed)

## Design Decisions

- **In-memory storage** using `ArrayList` — simple and sufficient for the library's scope
- **No external dependencies** beyond JUnit 5 for testing
- **TDD approach** — all functionality was driven by tests written before implementation
- **Immutable summary** — `getSummary()` returns an unmodifiable copy to prevent external mutation
