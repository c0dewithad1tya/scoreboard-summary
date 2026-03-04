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
        @Test
        @DisplayName("12-match league with distinct totals should sort in descending order")
        void shouldOrderTwelveMatchesWithDistinctTotals() {
            scoreboard.startMatch("Brazil", "Germany");
            scoreboard.startMatch("Argentina", "France");
            scoreboard.startMatch("England", "Spain");
            scoreboard.startMatch("Netherlands", "Italy");
            scoreboard.startMatch("Portugal", "Uruguay");
            scoreboard.startMatch("Belgium", "Croatia");
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Japan", "South Korea");
            scoreboard.startMatch("USA", "Australia");
            scoreboard.startMatch("Switzerland", "Denmark");
            scoreboard.startMatch("Colombia", "Chile");
            scoreboard.startMatch("Serbia", "Poland");

            scoreboard.updateScore("Brazil", "Germany", 7, 5);         // total 12
            scoreboard.updateScore("Argentina", "France", 5, 6);       // total 11
            scoreboard.updateScore("England", "Spain", 4, 6);          // total 10
            scoreboard.updateScore("Netherlands", "Italy", 5, 4);      // total 9
            scoreboard.updateScore("Portugal", "Uruguay", 3, 5);       // total 8
            scoreboard.updateScore("Belgium", "Croatia", 4, 3);        // total 7
            scoreboard.updateScore("Mexico", "Canada", 2, 4);          // total 6
            scoreboard.updateScore("Japan", "South Korea", 3, 2);      // total 5
            scoreboard.updateScore("USA", "Australia", 2, 2);          // total 4
            scoreboard.updateScore("Switzerland", "Denmark", 1, 2);    // total 3
            scoreboard.updateScore("Colombia", "Chile", 1, 1);         // total 2
            scoreboard.updateScore("Serbia", "Poland", 0, 1);          // total 1

            List<Match> summary = scoreboard.getSummary();
            printSummary(summary);

            assertEquals(12, summary.size());
            assertEquals("Brazil", summary.get(0).getHomeTeam());
            assertEquals("Argentina", summary.get(1).getHomeTeam());
            assertEquals("England", summary.get(2).getHomeTeam());
            assertEquals("Netherlands", summary.get(3).getHomeTeam());
            assertEquals("Portugal", summary.get(4).getHomeTeam());
            assertEquals("Belgium", summary.get(5).getHomeTeam());
            assertEquals("Mexico", summary.get(6).getHomeTeam());
            assertEquals("Japan", summary.get(7).getHomeTeam());
            assertEquals("USA", summary.get(8).getHomeTeam());
            assertEquals("Switzerland", summary.get(9).getHomeTeam());
            assertEquals("Colombia", summary.get(10).getHomeTeam());
            assertEquals("Serbia", summary.get(11).getHomeTeam());
        }

        @Test
        @DisplayName("Multiple tie groups should break ties by most recently started")
        void shouldBreakMultipleTieGroupsByRecency() {
            // Group A: total 8
            scoreboard.startMatch("Brazil", "Germany");       // 1st
            scoreboard.startMatch("Argentina", "France");     // 2nd
            scoreboard.startMatch("England", "Spain");        // 3rd
            // Group B: total 5
            scoreboard.startMatch("Netherlands", "Italy");    // 4th
            scoreboard.startMatch("Portugal", "Uruguay");     // 5th
            scoreboard.startMatch("Belgium", "Croatia");      // 6th
            // Group C: total 2
            scoreboard.startMatch("Mexico", "Canada");        // 7th
            scoreboard.startMatch("Japan", "South Korea");    // 8th
            scoreboard.startMatch("USA", "Australia");        // 9th

            scoreboard.updateScore("Brazil", "Germany", 5, 3);
            scoreboard.updateScore("Argentina", "France", 4, 4);
            scoreboard.updateScore("England", "Spain", 6, 2);
            scoreboard.updateScore("Netherlands", "Italy", 3, 2);
            scoreboard.updateScore("Portugal", "Uruguay", 2, 3);
            scoreboard.updateScore("Belgium", "Croatia", 1, 4);
            scoreboard.updateScore("Mexico", "Canada", 1, 1);
            scoreboard.updateScore("Japan", "South Korea", 0, 2);
            scoreboard.updateScore("USA", "Australia", 2, 0);

            List<Match> summary = scoreboard.getSummary();
            printSummary(summary);

            assertEquals(9, summary.size());
            // Group A (total 8): most recent first
            assertEquals("England", summary.get(0).getHomeTeam());
            assertEquals("Argentina", summary.get(1).getHomeTeam());
            assertEquals("Brazil", summary.get(2).getHomeTeam());
            // Group B (total 5): most recent first
            assertEquals("Belgium", summary.get(3).getHomeTeam());
            assertEquals("Portugal", summary.get(4).getHomeTeam());
            assertEquals("Netherlands", summary.get(5).getHomeTeam());
            // Group C (total 2): most recent first
            assertEquals("USA", summary.get(6).getHomeTeam());
            assertEquals("Japan", summary.get(7).getHomeTeam());
            assertEquals("Mexico", summary.get(8).getHomeTeam());
        }

        @Test
        @DisplayName("All 12 matches at 0-0 should order by most recently started")
        void shouldOrderAllZeroZeroByRecency() {
            scoreboard.startMatch("Brazil", "Germany");
            scoreboard.startMatch("Argentina", "France");
            scoreboard.startMatch("England", "Spain");
            scoreboard.startMatch("Netherlands", "Italy");
            scoreboard.startMatch("Portugal", "Uruguay");
            scoreboard.startMatch("Belgium", "Croatia");
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Japan", "South Korea");
            scoreboard.startMatch("USA", "Australia");
            scoreboard.startMatch("Switzerland", "Denmark");
            scoreboard.startMatch("Colombia", "Chile");
            scoreboard.startMatch("Serbia", "Poland");

            List<Match> summary = scoreboard.getSummary();
            printSummary(summary);

            assertEquals(12, summary.size());
            assertEquals("Serbia", summary.get(0).getHomeTeam());
            assertEquals("Colombia", summary.get(1).getHomeTeam());
            assertEquals("Switzerland", summary.get(2).getHomeTeam());
            assertEquals("USA", summary.get(3).getHomeTeam());
            assertEquals("Japan", summary.get(4).getHomeTeam());
            assertEquals("Mexico", summary.get(5).getHomeTeam());
            assertEquals("Belgium", summary.get(6).getHomeTeam());
            assertEquals("Portugal", summary.get(7).getHomeTeam());
            assertEquals("Netherlands", summary.get(8).getHomeTeam());
            assertEquals("England", summary.get(9).getHomeTeam());
            assertEquals("Argentina", summary.get(10).getHomeTeam());
            assertEquals("Brazil", summary.get(11).getHomeTeam());
        }

        @Test
        @DisplayName("Summary after finishing some matches should only show remaining matches")
        void shouldShowOnlyRemainingMatchesAfterFinishing() {
            scoreboard.startMatch("Brazil", "Germany");
            scoreboard.startMatch("Argentina", "France");
            scoreboard.startMatch("England", "Spain");
            scoreboard.startMatch("Netherlands", "Italy");
            scoreboard.startMatch("Portugal", "Uruguay");
            scoreboard.startMatch("Belgium", "Croatia");
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Japan", "South Korea");
            scoreboard.startMatch("USA", "Australia");
            scoreboard.startMatch("Switzerland", "Denmark");
            scoreboard.startMatch("Colombia", "Chile");
            scoreboard.startMatch("Serbia", "Poland");

            scoreboard.updateScore("Brazil", "Germany", 7, 5);
            scoreboard.updateScore("Argentina", "France", 5, 6);
            scoreboard.updateScore("England", "Spain", 4, 6);
            scoreboard.updateScore("Netherlands", "Italy", 5, 4);
            scoreboard.updateScore("Portugal", "Uruguay", 3, 5);
            scoreboard.updateScore("Belgium", "Croatia", 4, 3);
            scoreboard.updateScore("Mexico", "Canada", 2, 4);
            scoreboard.updateScore("Japan", "South Korea", 3, 2);
            scoreboard.updateScore("USA", "Australia", 2, 2);
            scoreboard.updateScore("Switzerland", "Denmark", 1, 2);
            scoreboard.updateScore("Colombia", "Chile", 1, 1);
            scoreboard.updateScore("Serbia", "Poland", 0, 1);

            scoreboard.finishMatch("Brazil", "Germany");
            scoreboard.finishMatch("England", "Spain");
            scoreboard.finishMatch("Belgium", "Croatia");
            scoreboard.finishMatch("USA", "Australia");

            List<Match> summary = scoreboard.getSummary();
            printSummary(summary);

            assertEquals(8, summary.size());
            assertEquals("Argentina", summary.get(0).getHomeTeam());
            assertEquals("Netherlands", summary.get(1).getHomeTeam());
            assertEquals("Portugal", summary.get(2).getHomeTeam());
            assertEquals("Mexico", summary.get(3).getHomeTeam());
            assertEquals("Japan", summary.get(4).getHomeTeam());
            assertEquals("Switzerland", summary.get(5).getHomeTeam());
            assertEquals("Colombia", summary.get(6).getHomeTeam());
            assertEquals("Serbia", summary.get(7).getHomeTeam());
        }

        @Test
        @DisplayName("Score updates should change summary ordering dynamically")
        void shouldReorderAfterScoreUpdates() {
            scoreboard.startMatch("Brazil", "Germany");
            scoreboard.startMatch("Argentina", "France");
            scoreboard.startMatch("England", "Spain");
            scoreboard.startMatch("Netherlands", "Italy");
            scoreboard.startMatch("Portugal", "Uruguay");
            scoreboard.startMatch("Belgium", "Croatia");

            scoreboard.updateScore("Brazil", "Germany", 1, 0);
            scoreboard.updateScore("Argentina", "France", 0, 1);
            scoreboard.updateScore("England", "Spain", 0, 0);
            scoreboard.updateScore("Netherlands", "Italy", 2, 0);
            scoreboard.updateScore("Portugal", "Uruguay", 0, 0);
            scoreboard.updateScore("Belgium", "Croatia", 3, 0);

            List<Match> before = scoreboard.getSummary();
            assertEquals("Belgium", before.get(0).getHomeTeam());
            assertEquals("Netherlands", before.get(1).getHomeTeam());

            // Update scores to flip ordering
            scoreboard.updateScore("England", "Spain", 5, 5);
            scoreboard.updateScore("Portugal", "Uruguay", 4, 4);

            List<Match> after = scoreboard.getSummary();
            printSummary(after);

            assertEquals("England", after.get(0).getHomeTeam());
            assertEquals("Portugal", after.get(1).getHomeTeam());
            assertEquals("Belgium", after.get(2).getHomeTeam());
        }

        @Test
        @DisplayName("High-scoring blowout vs multiple low-scoring ties")
        void shouldSortBlowoutAboveTiesCorrectly() {
            scoreboard.startMatch("Brazil", "Germany");
            scoreboard.startMatch("Argentina", "France");
            scoreboard.startMatch("England", "Spain");
            scoreboard.startMatch("Netherlands", "Italy");
            scoreboard.startMatch("Portugal", "Uruguay");
            scoreboard.startMatch("Belgium", "Croatia");

            scoreboard.updateScore("Brazil", "Germany", 10, 0);
            scoreboard.updateScore("Argentina", "France", 1, 1);
            scoreboard.updateScore("England", "Spain", 1, 1);
            scoreboard.updateScore("Netherlands", "Italy", 1, 1);
            scoreboard.updateScore("Portugal", "Uruguay", 1, 1);
            scoreboard.updateScore("Belgium", "Croatia", 1, 1);

            List<Match> summary = scoreboard.getSummary();
            printSummary(summary);

            assertEquals(6, summary.size());
            assertEquals("Brazil", summary.get(0).getHomeTeam());
            // Ties at total 2 — reverse start order
            assertEquals("Belgium", summary.get(1).getHomeTeam());
            assertEquals("Portugal", summary.get(2).getHomeTeam());
            assertEquals("Netherlands", summary.get(3).getHomeTeam());
            assertEquals("England", summary.get(4).getHomeTeam());
            assertEquals("Argentina", summary.get(5).getHomeTeam());
        }

        @Test
        @DisplayName("Single match remaining after finishing 11 of 12")
        void shouldShowSingleRemainingMatch() {
            scoreboard.startMatch("Brazil", "Germany");
            scoreboard.startMatch("Argentina", "France");
            scoreboard.startMatch("England", "Spain");
            scoreboard.startMatch("Netherlands", "Italy");
            scoreboard.startMatch("Portugal", "Uruguay");
            scoreboard.startMatch("Belgium", "Croatia");
            scoreboard.startMatch("Mexico", "Canada");
            scoreboard.startMatch("Japan", "South Korea");
            scoreboard.startMatch("USA", "Australia");
            scoreboard.startMatch("Switzerland", "Denmark");
            scoreboard.startMatch("Colombia", "Chile");
            scoreboard.startMatch("Serbia", "Poland");

            scoreboard.updateScore("Portugal", "Uruguay", 3, 2);

            scoreboard.finishMatch("Brazil", "Germany");
            scoreboard.finishMatch("Argentina", "France");
            scoreboard.finishMatch("England", "Spain");
            scoreboard.finishMatch("Netherlands", "Italy");
            scoreboard.finishMatch("Belgium", "Croatia");
            scoreboard.finishMatch("Mexico", "Canada");
            scoreboard.finishMatch("Japan", "South Korea");
            scoreboard.finishMatch("USA", "Australia");
            scoreboard.finishMatch("Switzerland", "Denmark");
            scoreboard.finishMatch("Colombia", "Chile");
            scoreboard.finishMatch("Serbia", "Poland");

            List<Match> summary = scoreboard.getSummary();
            printSummary(summary);

            assertEquals(1, summary.size());
            assertEquals("Portugal", summary.get(0).getHomeTeam());
            assertEquals(3, summary.get(0).getHomeScore());
            assertEquals(2, summary.get(0).getAwayScore());
        }
    }

    private void printSummary(List<Match> summary) {
        for (int i = 0; i < summary.size(); i++) {
            System.out.println((i + 1) + ". " + summary.get(i));
        }
    }
}
