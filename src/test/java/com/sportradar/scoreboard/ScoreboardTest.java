package com.sportradar.scoreboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreboardTest {

    private Scoreboard scoreboard;

    @BeforeEach
    void setUp() {
        scoreboard = new Scoreboard();
    }

    // --- Start Match ---

    @Test
    void startMatch_shouldAddMatchWithZeroZeroScore() {
        scoreboard.startMatch("Mexico", "Canada");

        List<Match> summary = scoreboard.getSummary();
        assertEquals(1, summary.size());
        assertEquals("Mexico", summary.get(0).getHomeTeam());
        assertEquals("Canada", summary.get(0).getAwayTeam());
        assertEquals(0, summary.get(0).getHomeScore());
        assertEquals(0, summary.get(0).getAwayScore());
    }

    @Test
    void startMatch_shouldNotAllowDuplicateMatch() {
        scoreboard.startMatch("Mexico", "Canada");

        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.startMatch("Mexico", "Canada"));
    }

    @Test
    void startMatch_shouldNotAllowTeamAlreadyPlaying() {
        scoreboard.startMatch("Mexico", "Canada");

        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.startMatch("Mexico", "Brazil"));
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.startMatch("Brazil", "Canada"));
    }

    @Test
    void startMatch_shouldNotAllowNullOrBlankTeamNames() {
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.startMatch(null, "Canada"));
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.startMatch("Mexico", null));
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.startMatch("", "Canada"));
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.startMatch("Mexico", "  "));
    }

    @Test
    void startMatch_shouldNotAllowSameTeamAsHomeAndAway() {
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.startMatch("Mexico", "Mexico"));
    }

    // --- Update Score ---

    @Test
    void updateScore_shouldUpdateMatchScore() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.updateScore("Mexico", "Canada", 0, 5);

        Match match = scoreboard.getSummary().get(0);
        assertEquals(0, match.getHomeScore());
        assertEquals(5, match.getAwayScore());
    }

    @Test
    void updateScore_shouldThrowForNonExistentMatch() {
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.updateScore("Mexico", "Canada", 1, 0));
    }

    @Test
    void updateScore_shouldNotAllowNegativeScores() {
        scoreboard.startMatch("Mexico", "Canada");

        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.updateScore("Mexico", "Canada", -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.updateScore("Mexico", "Canada", 0, -1));
    }

    // --- Finish Match ---

    @Test
    void finishMatch_shouldRemoveMatchFromScoreboard() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.finishMatch("Mexico", "Canada");

        assertTrue(scoreboard.getSummary().isEmpty());
    }

    @Test
    void finishMatch_shouldThrowForNonExistentMatch() {
        assertThrows(IllegalArgumentException.class,
                () -> scoreboard.finishMatch("Mexico", "Canada"));
    }

    @Test
    void finishMatch_shouldAllowTeamToStartNewMatchAfter() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.finishMatch("Mexico", "Canada");
        scoreboard.startMatch("Mexico", "Brazil");

        assertEquals(1, scoreboard.getSummary().size());
        assertEquals("Brazil", scoreboard.getSummary().get(0).getAwayTeam());
    }

    // --- Get Summary (ordering) ---

    @Test
    void getSummary_shouldReturnEmptyListWhenNoMatches() {
        assertTrue(scoreboard.getSummary().isEmpty());
    }

    @Test
    void getSummary_shouldOrderByTotalScoreDescending() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.startMatch("Spain", "Brazil");

        scoreboard.updateScore("Mexico", "Canada", 0, 5);   // total 5
        scoreboard.updateScore("Spain", "Brazil", 10, 2);    // total 12

        List<Match> summary = scoreboard.getSummary();
        assertEquals("Spain", summary.get(0).getHomeTeam());
        assertEquals("Mexico", summary.get(1).getHomeTeam());
    }

    @Test
    void getSummary_shouldOrderByMostRecentlyStartedWhenTotalScoreEqual() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.startMatch("Spain", "Brazil");

        scoreboard.updateScore("Mexico", "Canada", 0, 5);   // total 5
        scoreboard.updateScore("Spain", "Brazil", 3, 2);     // total 5

        List<Match> summary = scoreboard.getSummary();
        // Spain started later, so it comes first when totals are equal
        assertEquals("Spain", summary.get(0).getHomeTeam());
        assertEquals("Mexico", summary.get(1).getHomeTeam());
    }

    @Test
    void getSummary_shouldMatchExampleFromRequirements() {
        // Start matches in order a-e
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.startMatch("Spain", "Brazil");
        scoreboard.startMatch("Germany", "France");
        scoreboard.startMatch("Uruguay", "Italy");
        scoreboard.startMatch("Argentina", "Australia");

        // Update scores
        scoreboard.updateScore("Mexico", "Canada", 0, 5);
        scoreboard.updateScore("Spain", "Brazil", 10, 2);
        scoreboard.updateScore("Germany", "France", 2, 2);
        scoreboard.updateScore("Uruguay", "Italy", 6, 6);
        scoreboard.updateScore("Argentina", "Australia", 3, 1);

        List<Match> summary = scoreboard.getSummary();

        assertEquals(5, summary.size());
        assertEquals("Uruguay", summary.get(0).getHomeTeam());    // 12, started 4th
        assertEquals("Spain", summary.get(1).getHomeTeam());      // 12, started 2nd
        assertEquals("Mexico", summary.get(2).getHomeTeam());     // 5
        assertEquals("Argentina", summary.get(3).getHomeTeam());  // 4, started 5th
        assertEquals("Germany", summary.get(4).getHomeTeam());    // 4, started 3rd
    }
}
