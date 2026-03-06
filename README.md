# Live Football Score Board and Summary Library

A simple in-memory library that tracks ongoing football matches and their scores.

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

# Generate functional test report (written to target/test-report.md)
mvn test -Dtest="TestReportGenerator"
```

## Test Report

Running `mvn test -Dtest="TestReportGenerator"` generates a markdown report at `target/test-report.md` containing:

- **Test Scenario** — what is being tested
- **Input** — the data and parameters used
- **Expected** — the expected behavior or output
- **Actual** — what the code actually produced
- **Result** — PASS or FAIL

The report covers **27 functional test scenarios** across all four operations, organized as follows:

### Start Match (9 scenarios)

| # | Scenario |
|---|----------|
| 1 | Start a new match with initial score 0-0 |
| 2 | Reject duplicate match (same teams) |
| 3 | Reject if home team is already playing |
| 4 | Reject if away team is already playing |
| 5 | Reject null home team |
| 6 | Reject null away team |
| 7 | Reject empty home team |
| 8 | Reject blank away team |
| 9 | Reject same team as home and away |

### Update Score (4 scenarios)

| # | Scenario |
|---|----------|
| 10 | Update score with absolute values |
| 11 | Reject update for non-existent match |
| 12 | Reject negative home score |
| 13 | Reject negative away score |

### Finish Match (3 scenarios)

| # | Scenario |
|---|----------|
| 14 | Remove match from scoreboard |
| 15 | Reject finish for non-existent match |
| 16 | Team can start a new match after finishing |

### Get Summary (11 scenarios)

| # | Scenario |
|---|----------|
| 17 | Empty scoreboard returns empty list |
| 18 | Order by total score descending (2 matches) |
| 19 | Equal totals — most recently started first (2 matches) |
| 20 | Full 5-match example from requirements spec |
| 21 | 12-match league with distinct totals (12 down to 1) |
| 22 | Multiple tie groups — recency tie-break within each group |
| 23 | All 12 matches at 0-0 — ordered purely by most recently started |
| 24 | Start 12, finish 4 — summary shows only remaining 8 in correct order |
| 25 | Score updates dynamically flip summary ordering |
| 26 | High-scoring blowout (10-0) vs five low-scoring ties (1-1) |
| 27 | Start 12, finish 11 — single match remaining |

Scenarios 21–27 simulate a **12-country World Cup league** using teams from Brazil, Germany, Argentina, France, England, Spain, Netherlands, Italy, Portugal, Uruguay, Belgium, and Croatia to exercise large-scale sorting, multi-level tie-breaking, partial finishes, dynamic re-ordering, and edge cases with extreme score distributions.

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
