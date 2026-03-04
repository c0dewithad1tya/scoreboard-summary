package com.sportradar.scoreboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Scoreboard")
class ScoreboardTest {

    private Scoreboard scoreboard;

    @BeforeEach
    void setUp() {
        scoreboard = new Scoreboard();
    }

    @Nested
    @DisplayName("Start Match")
    class StartMatch {

        @Test
        @DisplayName("Should add a new match with initial score 0-0")
        void shouldAddMatchWithZeroZeroScore() {
            scoreboard.startMatch("Mexico", "Canada");

            List<Match> summary = scoreboard.getSummary();
            assertEquals(1, summary.size());

            Match match = summary.get(0);
            System.out.println("Started: " + match);
            assertEquals("Mexico", match.getHomeTeam());
            assertEquals("Canada", match.getAwayTeam());
            assertEquals(0, match.getHomeScore());
            assertEquals(0, match.getAwayScore());
        }

        @Test
        @DisplayName("Should reject duplicate match (same home and away teams)")
        void shouldNotAllowDuplicateMatch() {
            scoreboard.startMatch("Mexico", "Canada");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> scoreboard.startMatch("Mexico", "Canada"));
            System.out.println("Rejected duplicate: " + ex.getMessage());
        }

        @Test
        @DisplayName("Should reject match if a team is already playing")
        void shouldNotAllowTeamAlreadyPlaying() {
            scoreboard.startMatch("Mexico", "Canada");

            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                    () -> scoreboard.startMatch("Mexico", "Brazil"));
            System.out.println("Rejected (home team busy): " + ex1.getMessage());

            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                    () -> scoreboard.startMatch("Brazil", "Canada"));
            System.out.println("Rejected (away team busy): " + ex2.getMessage());
        }

        @Test
        @DisplayName("Should reject null or blank team names")
        void shouldNotAllowNullOrBlankTeamNames() {
            assertThrows(IllegalArgumentException.class, () -> scoreboard.startMatch(null, "Canada"));
            assertThrows(IllegalArgumentException.class, () -> scoreboard.startMatch("Mexico", null));
            assertThrows(IllegalArgumentException.class, () -> scoreboard.startMatch("", "Canada"));
            assertThrows(IllegalArgumentException.class, () -> scoreboard.startMatch("Mexico", "  "));
            System.out.println("All null/blank team name inputs correctly rejected");
        }

        @Test
        @DisplayName("Should reject match where home and away team are the same")
        void shouldNotAllowSameTeamAsHomeAndAway() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> scoreboard.startMatch("Mexico", "Mexico"));
            System.out.println("Rejected same team: " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Update Score")
    class UpdateScore {

        @Test
        @DisplayName("Should update home and away scores with absolute values")
        void shouldUpdateMatchScore() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.updateScore("Mexico", "Canada", 0, 5);

            Match match = scoreboard.getSummary().get(0);
            System.out.println("Updated: " + match);
            assertEquals(0, match.getHomeScore());
            assertEquals(5, match.getAwayScore());
        }

        @Test
        @DisplayName("Should throw when updating score for a non-existent match")
        void shouldThrowForNonExistentMatch() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> scoreboard.updateScore("Mexico", "Canada", 1, 0));
            System.out.println("Rejected non-existent match: " + ex.getMessage());
        }

        @Test
        @DisplayName("Should reject negative score values")
        void shouldNotAllowNegativeScores() {
            scoreboard.startMatch("Mexico", "Canada");

            assertThrows(IllegalArgumentException.class,
                    () -> scoreboard.updateScore("Mexico", "Canada", -1, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> scoreboard.updateScore("Mexico", "Canada", 0, -1));
            System.out.println("Negative scores correctly rejected");
        }
    }

    @Nested
    @DisplayName("Finish Match")
    class FinishMatch {

        @Test
        @DisplayName("Should remove the match from the scoreboard")
        void shouldRemoveMatchFromScoreboard() {
            scoreboard.startMatch("Mexico", "Canada");
            System.out.println("Before finish: " + scoreboard.getSummary().size() + " match(es)");

            scoreboard.finishMatch("Mexico", "Canada");
            System.out.println("After finish: " + scoreboard.getSummary().size() + " match(es)");

            assertTrue(scoreboard.getSummary().isEmpty());
        }

        @Test
        @DisplayName("Should throw when finishing a non-existent match")
        void shouldThrowForNonExistentMatch() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> scoreboard.finishMatch("Mexico", "Canada"));
            System.out.println("Rejected non-existent finish: " + ex.getMessage());
        }

        @Test
        @DisplayName("Should allow a team to start a new match after finishing the previous one")
        void shouldAllowTeamToStartNewMatchAfter() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.finishMatch("Mexico", "Canada");
            scoreboard.startMatch("Mexico", "Brazil");

            Match match = scoreboard.getSummary().get(0);
            System.out.println("New match after finish: " + match);
            assertEquals(1, scoreboard.getSummary().size());
            assertEquals("Brazil", match.getAwayTeam());
        }
    }

    @Nested
    @DisplayName("Get Summary")
    class GetSummary {

        @Test
        @DisplayName("Should return empty list when no matches are in progress")
        void shouldReturnEmptyListWhenNoMatches() {
            List<Match> summary = scoreboard.getSummary();
            System.out.println("Summary with no matches: " + summary);
            assertTrue(summary.isEmpty());
        }

        @Test
        @DisplayName("Should order matches by total score in descending order")
        void shouldOrderByTotalScoreDescending() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Spain", "Brazil");

            scoreboard.updateScore("Mexico", "Canada", 0, 5);   // total 5
            scoreboard.updateScore("Spain", "Brazil", 10, 2);    // total 12

            List<Match> summary = scoreboard.getSummary();
            printSummary(summary);

            assertEquals("Spain", summary.get(0).getHomeTeam());
            assertEquals("Mexico", summary.get(1).getHomeTeam());
        }

        @Test
        @DisplayName("Should order by most recently started match when total scores are equal")
        void shouldOrderByMostRecentlyStartedWhenTotalScoreEqual() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Spain", "Brazil");

            scoreboard.updateScore("Mexico", "Canada", 0, 5);   // total 5
            scoreboard.updateScore("Spain", "Brazil", 3, 2);     // total 5

            List<Match> summary = scoreboard.getSummary();
            printSummary(summary);

            assertEquals("Spain", summary.get(0).getHomeTeam());
            assertEquals("Mexico", summary.get(1).getHomeTeam());
        }

        @Test
        @DisplayName("Should match the expected output from the requirements example")
        void shouldMatchExampleFromRequirements() {
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Spain", "Brazil");
            scoreboard.startMatch("Germany", "France");
            scoreboard.startMatch("Uruguay", "Italy");
            scoreboard.startMatch("Argentina", "Australia");

            scoreboard.updateScore("Mexico", "Canada", 0, 5);
            scoreboard.updateScore("Spain", "Brazil", 10, 2);
            scoreboard.updateScore("Germany", "France", 2, 2);
            scoreboard.updateScore("Uruguay", "Italy", 6, 6);
            scoreboard.updateScore("Argentina", "Australia", 3, 1);

            List<Match> summary = scoreboard.getSummary();

            System.out.println("\n=== Live Football World Cup Score Board ===");
            printSummary(summary);
            System.out.println("============================================");

            assertEquals(5, summary.size());
            assertEquals("Uruguay", summary.get(0).getHomeTeam());
            assertEquals("Spain", summary.get(1).getHomeTeam());
            assertEquals("Mexico", summary.get(2).getHomeTeam());
            assertEquals("Argentina", summary.get(3).getHomeTeam());
            assertEquals("Germany", summary.get(4).getHomeTeam());
        }
    }

    private void printSummary(List<Match> summary) {
        for (int i = 0; i < summary.size(); i++) {
            System.out.println((i + 1) + ". " + summary.get(i));
        }
    }
}
