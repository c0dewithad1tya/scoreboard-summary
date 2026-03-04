package com.sportradar.scoreboard;

import org.junit.jupiter.api.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@DisplayName("Test Report Generator")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestReportGenerator {

    private static PrintWriter report;
    private Scoreboard scoreboard;

    @BeforeAll
    static void openReport() throws IOException {
        report = new PrintWriter(new FileWriter("target/test-report.md"));
        report.println("# Live Football World Cup Score Board — Test Report");
        report.println();
        report.println("| # | Category | Test Scenario | Input | Expected | Actual | Result |");
        report.println("|---|----------|--------------|-------|----------|--------|--------|");
    }

    @AfterAll
    static void closeReport() {
        report.println();
        report.println("---");
        report.println("*Report generated automatically from test suite.*");
        report.flush();
        report.close();
        System.out.println("\n>>> Report written to target/test-report.md");
    }

    @BeforeEach
    void setUp() {
        scoreboard = new Scoreboard();
    }

    // ===================== START MATCH =====================

    @Test
    @Order(1)
    void report_startMatch_addsWithZeroScore() {
        scoreboard.startMatch("Mexico", "Canada");
        Match m = scoreboard.getSummary().get(0);
        String actual = m.toString();
        String expected = "Mexico 0 - Canada 5".replace("5", "0");

        row(1, "Start Match", "Start a new match",
                "homeTeam=Mexico, awayTeam=Canada",
                "Match added with score 0-0",
                actual,
                actual.equals("Mexico 0 - Canada 0"));
    }

    @Test
    @Order(2)
    void report_startMatch_rejectsDuplicate() {
        scoreboard.startMatch("Mexico", "Canada");
        String actual = captureException(() -> scoreboard.startMatch("Mexico", "Canada"));

        row(2, "Start Match", "Reject duplicate match",
                "Start Mexico vs Canada twice",
                "IllegalArgumentException thrown",
                actual,
                actual != null);
    }

    @Test
    @Order(3)
    void report_startMatch_rejectsTeamAlreadyPlaying() {
        scoreboard.startMatch("Mexico", "Canada");
        String actual1 = captureException(() -> scoreboard.startMatch("Mexico", "Brazil"));
        String actual2 = captureException(() -> scoreboard.startMatch("Brazil", "Canada"));

        row(3, "Start Match", "Reject if home team already playing",
                "Mexico vs Canada active, then start Mexico vs Brazil",
                "IllegalArgumentException thrown",
                actual1,
                actual1 != null);

        row(4, "Start Match", "Reject if away team already playing",
                "Mexico vs Canada active, then start Brazil vs Canada",
                "IllegalArgumentException thrown",
                actual2,
                actual2 != null);
    }

    @Test
    @Order(4)
    void report_startMatch_rejectsNullOrBlank() {
        String r1 = captureException(() -> scoreboard.startMatch(null, "Canada"));
        String r2 = captureException(() -> scoreboard.startMatch("Mexico", null));
        String r3 = captureException(() -> scoreboard.startMatch("", "Canada"));
        String r4 = captureException(() -> scoreboard.startMatch("Mexico", "  "));

        row(5, "Start Match", "Reject null home team",
                "homeTeam=null, awayTeam=Canada", "IllegalArgumentException", r1, r1 != null);
        row(6, "Start Match", "Reject null away team",
                "homeTeam=Mexico, awayTeam=null", "IllegalArgumentException", r2, r2 != null);
        row(7, "Start Match", "Reject empty home team",
                "homeTeam=\"\", awayTeam=Canada", "IllegalArgumentException", r3, r3 != null);
        row(8, "Start Match", "Reject blank away team",
                "homeTeam=Mexico, awayTeam=\"  \"", "IllegalArgumentException", r4, r4 != null);
    }

    @Test
    @Order(5)
    void report_startMatch_rejectsSameTeam() {
        String actual = captureException(() -> scoreboard.startMatch("Mexico", "Mexico"));

        row(9, "Start Match", "Reject same team as home and away",
                "homeTeam=Mexico, awayTeam=Mexico",
                "IllegalArgumentException thrown",
                actual,
                actual != null);
    }

    // ===================== UPDATE SCORE =====================

    @Test
    @Order(6)
    void report_updateScore_updatesCorrectly() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.updateScore("Mexico", "Canada", 0, 5);
        Match m = scoreboard.getSummary().get(0);
        String actual = m.toString();

        row(10, "Update Score", "Update score with absolute values",
                "Start Mexico vs Canada, then update to 0-5",
                "Mexico 0 - Canada 5",
                actual,
                m.getHomeScore() == 0 && m.getAwayScore() == 5);
    }

    @Test
    @Order(7)
    void report_updateScore_rejectsNonExistent() {
        String actual = captureException(() -> scoreboard.updateScore("Mexico", "Canada", 1, 0));

        row(11, "Update Score", "Reject update for non-existent match",
                "Update Mexico vs Canada (never started)",
                "IllegalArgumentException thrown",
                actual,
                actual != null);
    }

    @Test
    @Order(8)
    void report_updateScore_rejectsNegative() {
        scoreboard.startMatch("Mexico", "Canada");
        String r1 = captureException(() -> scoreboard.updateScore("Mexico", "Canada", -1, 0));
        String r2 = captureException(() -> scoreboard.updateScore("Mexico", "Canada", 0, -1));

        row(12, "Update Score", "Reject negative home score",
                "homeScore=-1, awayScore=0", "IllegalArgumentException", r1, r1 != null);
        row(13, "Update Score", "Reject negative away score",
                "homeScore=0, awayScore=-1", "IllegalArgumentException", r2, r2 != null);
    }

    // ===================== FINISH MATCH =====================

    @Test
    @Order(9)
    void report_finishMatch_removesMatch() {
        scoreboard.startMatch("Mexico", "Canada");
        int before = scoreboard.getSummary().size();
        scoreboard.finishMatch("Mexico", "Canada");
        int after = scoreboard.getSummary().size();

        row(14, "Finish Match", "Remove match from scoreboard",
                "Start Mexico vs Canada, then finish it",
                "Scoreboard size: 1 → 0",
                "Scoreboard size: " + before + " → " + after,
                after == 0);
    }

    @Test
    @Order(10)
    void report_finishMatch_rejectsNonExistent() {
        String actual = captureException(() -> scoreboard.finishMatch("Mexico", "Canada"));

        row(15, "Finish Match", "Reject finish for non-existent match",
                "Finish Mexico vs Canada (never started)",
                "IllegalArgumentException thrown",
                actual,
                actual != null);
    }

    @Test
    @Order(11)
    void report_finishMatch_teamCanPlayAgain() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.finishMatch("Mexico", "Canada");
        scoreboard.startMatch("Mexico", "Brazil");
        Match m = scoreboard.getSummary().get(0);
        String actual = m.toString();

        row(16, "Finish Match", "Team can start new match after finishing",
                "Start Mexico vs Canada → Finish → Start Mexico vs Brazil",
                "Mexico 0 - Brazil 0",
                actual,
                actual.equals("Mexico 0 - Brazil 0"));
    }

    // ===================== GET SUMMARY =====================

    @Test
    @Order(12)
    void report_getSummary_emptyWhenNoMatches() {
        List<Match> summary = scoreboard.getSummary();

        row(17, "Get Summary", "Empty scoreboard returns empty list",
                "(no matches started)",
                "Empty list (size=0)",
                "Empty list (size=" + summary.size() + ")",
                summary.isEmpty());
    }

    @Test
    @Order(13)
    void report_getSummary_orderByTotalScore() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.startMatch("Spain", "Brazil");
        scoreboard.updateScore("Mexico", "Canada", 0, 5);
        scoreboard.updateScore("Spain", "Brazil", 10, 2);

        List<Match> summary = scoreboard.getSummary();
        String actual = formatSummary(summary);

        row(18, "Get Summary", "Order by total score descending",
                "Mexico 0-5 (total 5), Spain 10-2 (total 12)",
                "1. Spain 10-Brazil 2, 2. Mexico 0-Canada 5",
                actual,
                summary.get(0).getHomeTeam().equals("Spain"));
    }

    @Test
    @Order(14)
    void report_getSummary_orderByRecencyOnTie() {
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.startMatch("Spain", "Brazil");
        scoreboard.updateScore("Mexico", "Canada", 0, 5);
        scoreboard.updateScore("Spain", "Brazil", 3, 2);

        List<Match> summary = scoreboard.getSummary();
        String actual = formatSummary(summary);

        row(19, "Get Summary", "Equal totals → most recently started first",
                "Mexico 0-5 (total 5, started 1st), Spain 3-2 (total 5, started 2nd)",
                "1. Spain 3-Brazil 2, 2. Mexico 0-Canada 5",
                actual,
                summary.get(0).getHomeTeam().equals("Spain"));
    }

    @Test
    @Order(15)
    void report_getSummary_fullRequirementsExample() {
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
        String actual = formatSummary(summary);

        String expected = "1. Uruguay 6 - Italy 6, " +
                "2. Spain 10 - Brazil 2, " +
                "3. Mexico 0 - Canada 5, " +
                "4. Argentina 3 - Australia 1, " +
                "5. Germany 2 - France 2";

        boolean pass = summary.get(0).getHomeTeam().equals("Uruguay")
                && summary.get(1).getHomeTeam().equals("Spain")
                && summary.get(2).getHomeTeam().equals("Mexico")
                && summary.get(3).getHomeTeam().equals("Argentina")
                && summary.get(4).getHomeTeam().equals("Germany");

        row(20, "Get Summary", "Full example from requirements spec",
                "5 matches started in order a-e with various scores",
                expected,
                actual,
                pass);
    }

    // ===================== HELPERS =====================

    private void row(int num, String category, String scenario, String input,
                     String expected, String actual, boolean pass) {
        report.printf("| %d | %s | %s | %s | %s | %s | %s |%n",
                num, category, scenario,
                escape(input), escape(expected), escape(actual),
                pass ? "PASS" : "FAIL");
    }

    private String captureException(Runnable action) {
        try {
            action.run();
            return null;
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private String formatSummary(List<Match> summary) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < summary.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append((i + 1)).append(". ").append(summary.get(i));
        }
        return sb.toString();
    }

    private String escape(String text) {
        return text.replace("|", "\\|").replace("\n", " ");
    }
}
