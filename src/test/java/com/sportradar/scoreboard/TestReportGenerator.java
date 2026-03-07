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

    // ===================== GET SUMMARY — 12-COUNTRY LEAGUE EDGE CASES =====================

    @Test
    @Order(16)
    void report_getSummary_twelveMatchLeagueDistinctTotals() {
        scoreboard.startMatch("Brazil", "Germany");       // 1st
        scoreboard.startMatch("Argentina", "France");     // 2nd
        scoreboard.startMatch("England", "Spain");        // 3rd
        scoreboard.startMatch("Netherlands", "Italy");    // 4th
        scoreboard.startMatch("Portugal", "Uruguay");     // 5th
        scoreboard.startMatch("Belgium", "Croatia");      // 6th
        scoreboard.startMatch("Mexico", "Canada");        // 7th
        scoreboard.startMatch("Japan", "South Korea");    // 8th
        scoreboard.startMatch("USA", "Australia");        // 9th
        scoreboard.startMatch("Switzerland", "Denmark");  // 10th
        scoreboard.startMatch("Colombia", "Chile");       // 11th
        scoreboard.startMatch("Serbia", "Poland");        // 12th

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
        String actual = formatSummary(summary);

        boolean pass = summary.size() == 12
                && summary.get(0).getHomeTeam().equals("Brazil")
                && summary.get(1).getHomeTeam().equals("Argentina")
                && summary.get(2).getHomeTeam().equals("England")
                && summary.get(3).getHomeTeam().equals("Netherlands")
                && summary.get(4).getHomeTeam().equals("Portugal")
                && summary.get(5).getHomeTeam().equals("Belgium")
                && summary.get(6).getHomeTeam().equals("Mexico")
                && summary.get(7).getHomeTeam().equals("Japan")
                && summary.get(8).getHomeTeam().equals("USA")
                && summary.get(9).getHomeTeam().equals("Switzerland")
                && summary.get(10).getHomeTeam().equals("Colombia")
                && summary.get(11).getHomeTeam().equals("Serbia");

        row(21, "Get Summary", "12 matches with distinct totals → descending order",
                "12 matches, totals 12 down to 1",
                "Brazil(12), Argentina(11), ..., Serbia(1)",
                actual, pass);
    }

    @Test
    @Order(17)
    void report_getSummary_multipleTiesAtDifferentLevels() {
        // Group A: total 8 — started 1st, 2nd, 3rd → tie-break: 3rd, 2nd, 1st
        scoreboard.startMatch("Brazil", "Germany");       // 1st, total 8
        scoreboard.startMatch("Argentina", "France");     // 2nd, total 8
        scoreboard.startMatch("England", "Spain");        // 3rd, total 8

        // Group B: total 5 — started 4th, 5th, 6th → tie-break: 6th, 5th, 4th
        scoreboard.startMatch("Netherlands", "Italy");    // 4th, total 5
        scoreboard.startMatch("Portugal", "Uruguay");     // 5th, total 5
        scoreboard.startMatch("Belgium", "Croatia");      // 6th, total 5

        // Group C: total 2 — started 7th, 8th, 9th → tie-break: 9th, 8th, 7th
        scoreboard.startMatch("Mexico", "Canada");        // 7th, total 2
        scoreboard.startMatch("Japan", "South Korea");    // 8th, total 2
        scoreboard.startMatch("USA", "Australia");        // 9th, total 2

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
        String actual = formatSummary(summary);

        boolean pass = summary.size() == 9
                // Group A (total 8): most recent first → England, Argentina, Brazil
                && summary.get(0).getHomeTeam().equals("England")
                && summary.get(1).getHomeTeam().equals("Argentina")
                && summary.get(2).getHomeTeam().equals("Brazil")
                // Group B (total 5): most recent first → Belgium, Portugal, Netherlands
                && summary.get(3).getHomeTeam().equals("Belgium")
                && summary.get(4).getHomeTeam().equals("Portugal")
                && summary.get(5).getHomeTeam().equals("Netherlands")
                // Group C (total 2): most recent first → USA, Japan, Mexico
                && summary.get(6).getHomeTeam().equals("USA")
                && summary.get(7).getHomeTeam().equals("Japan")
                && summary.get(8).getHomeTeam().equals("Mexico");

        row(22, "Get Summary", "Multiple tie groups → recency tie-break within each",
                "3 groups of 3 at totals 8, 5, 2",
                "England, Argentina, Brazil, Belgium, Portugal, Netherlands, USA, Japan, Mexico",
                actual, pass);
    }

    @Test
    @Order(18)
    void report_getSummary_allMatchesTiedAtZeroZero() {
        scoreboard.startMatch("Brazil", "Germany");       // 1st
        scoreboard.startMatch("Argentina", "France");     // 2nd
        scoreboard.startMatch("England", "Spain");        // 3rd
        scoreboard.startMatch("Netherlands", "Italy");    // 4th
        scoreboard.startMatch("Portugal", "Uruguay");     // 5th
        scoreboard.startMatch("Belgium", "Croatia");      // 6th
        scoreboard.startMatch("Mexico", "Canada");        // 7th
        scoreboard.startMatch("Japan", "South Korea");    // 8th
        scoreboard.startMatch("USA", "Australia");        // 9th
        scoreboard.startMatch("Switzerland", "Denmark");  // 10th
        scoreboard.startMatch("Colombia", "Chile");       // 11th
        scoreboard.startMatch("Serbia", "Poland");        // 12th

        List<Match> summary = scoreboard.getSummary();
        String actual = formatSummary(summary);

        // All 0-0 → purely reverse start order (most recent first)
        boolean pass = summary.size() == 12
                && summary.get(0).getHomeTeam().equals("Serbia")
                && summary.get(1).getHomeTeam().equals("Colombia")
                && summary.get(2).getHomeTeam().equals("Switzerland")
                && summary.get(3).getHomeTeam().equals("USA")
                && summary.get(4).getHomeTeam().equals("Japan")
                && summary.get(5).getHomeTeam().equals("Mexico")
                && summary.get(6).getHomeTeam().equals("Belgium")
                && summary.get(7).getHomeTeam().equals("Portugal")
                && summary.get(8).getHomeTeam().equals("Netherlands")
                && summary.get(9).getHomeTeam().equals("England")
                && summary.get(10).getHomeTeam().equals("Argentina")
                && summary.get(11).getHomeTeam().equals("Brazil");

        row(23, "Get Summary", "All 12 matches at 0-0 → ordered by most recently started",
                "12 matches, all 0-0",
                "Serbia(12th), Colombia(11th), ..., Brazil(1st)",
                actual, pass);
    }

    @Test
    @Order(19)
    void report_getSummary_afterSomeMatchesFinished() {
        scoreboard.startMatch("Brazil", "Germany");       // 1st
        scoreboard.startMatch("Argentina", "France");     // 2nd
        scoreboard.startMatch("England", "Spain");        // 3rd
        scoreboard.startMatch("Netherlands", "Italy");    // 4th
        scoreboard.startMatch("Portugal", "Uruguay");     // 5th
        scoreboard.startMatch("Belgium", "Croatia");      // 6th
        scoreboard.startMatch("Mexico", "Canada");        // 7th
        scoreboard.startMatch("Japan", "South Korea");    // 8th
        scoreboard.startMatch("USA", "Australia");        // 9th
        scoreboard.startMatch("Switzerland", "Denmark");  // 10th
        scoreboard.startMatch("Colombia", "Chile");       // 11th
        scoreboard.startMatch("Serbia", "Poland");        // 12th

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

        // Finish 4 matches
        scoreboard.finishMatch("Brazil", "Germany");
        scoreboard.finishMatch("England", "Spain");
        scoreboard.finishMatch("Belgium", "Croatia");
        scoreboard.finishMatch("USA", "Australia");

        List<Match> summary = scoreboard.getSummary();
        String actual = formatSummary(summary);

        boolean pass = summary.size() == 8
                && summary.get(0).getHomeTeam().equals("Argentina")    // total 11
                && summary.get(1).getHomeTeam().equals("Netherlands")  // total 9
                && summary.get(2).getHomeTeam().equals("Portugal")     // total 8
                && summary.get(3).getHomeTeam().equals("Mexico")       // total 6
                && summary.get(4).getHomeTeam().equals("Japan")        // total 5
                && summary.get(5).getHomeTeam().equals("Switzerland")  // total 3
                && summary.get(6).getHomeTeam().equals("Colombia")     // total 2
                && summary.get(7).getHomeTeam().equals("Serbia");      // total 1

        row(24, "Get Summary", "Start 12, finish 4 → summary shows remaining 8",
                "12 matches started, 4 finished (Brazil, England, Belgium, USA)",
                "Argentina(11), Netherlands(9), Portugal(8), Mexico(6), Japan(5), Switzerland(3), Colombia(2), Serbia(1)",
                actual, pass);
    }

    @Test
    @Order(20)
    void report_getSummary_scoreUpdatesChangeOrdering() {
        scoreboard.startMatch("Brazil", "Germany");       // 1st
        scoreboard.startMatch("Argentina", "France");     // 2nd
        scoreboard.startMatch("England", "Spain");        // 3rd
        scoreboard.startMatch("Netherlands", "Italy");    // 4th
        scoreboard.startMatch("Portugal", "Uruguay");     // 5th
        scoreboard.startMatch("Belgium", "Croatia");      // 6th

        // Initial scores
        scoreboard.updateScore("Brazil", "Germany", 1, 0);         // total 1
        scoreboard.updateScore("Argentina", "France", 0, 1);       // total 1
        scoreboard.updateScore("England", "Spain", 0, 0);          // total 0
        scoreboard.updateScore("Netherlands", "Italy", 2, 0);      // total 2
        scoreboard.updateScore("Portugal", "Uruguay", 0, 0);       // total 0
        scoreboard.updateScore("Belgium", "Croatia", 3, 0);        // total 3

        List<Match> before = scoreboard.getSummary();
        String actualBefore = formatSummary(before);

        // Verify initial order: Belgium(3), Netherlands(2), Argentina(1,2nd), Brazil(1,1st), Belgium(6th)...
        boolean passBefore = before.get(0).getHomeTeam().equals("Belgium")
                && before.get(1).getHomeTeam().equals("Netherlands");

        // Update scores to flip ordering
        scoreboard.updateScore("England", "Spain", 5, 5);          // total 10 — was 0
        scoreboard.updateScore("Portugal", "Uruguay", 4, 4);       // total 8 — was 0
        scoreboard.updateScore("Brazil", "Germany", 1, 0);         // total 1 — stays
        scoreboard.updateScore("Belgium", "Croatia", 3, 0);        // total 3 — stays

        List<Match> after = scoreboard.getSummary();
        String actualAfter = formatSummary(after);

        boolean passAfter = after.get(0).getHomeTeam().equals("England")      // total 10
                && after.get(1).getHomeTeam().equals("Portugal")              // total 8
                && after.get(2).getHomeTeam().equals("Belgium");              // total 3

        row(25, "Get Summary", "Score updates flip ordering",
                "6 matches; initial order Belgium top; then England & Portugal score big",
                "Before: Belgium(3) top → After: England(10) top",
                "Before: " + actualBefore + " / After: " + actualAfter,
                passBefore && passAfter);
    }

    @Test
    @Order(21)
    void report_getSummary_highScoringBlowoutVsLowScoringTies() {
        // One huge blowout
        scoreboard.startMatch("Brazil", "Germany");       // 1st
        // Several low-scoring ties
        scoreboard.startMatch("Argentina", "France");     // 2nd
        scoreboard.startMatch("England", "Spain");        // 3rd
        scoreboard.startMatch("Netherlands", "Italy");    // 4th
        scoreboard.startMatch("Portugal", "Uruguay");     // 5th
        scoreboard.startMatch("Belgium", "Croatia");      // 6th

        scoreboard.updateScore("Brazil", "Germany", 10, 0);        // total 10 (blowout)
        scoreboard.updateScore("Argentina", "France", 1, 1);       // total 2
        scoreboard.updateScore("England", "Spain", 1, 1);          // total 2
        scoreboard.updateScore("Netherlands", "Italy", 1, 1);      // total 2
        scoreboard.updateScore("Portugal", "Uruguay", 1, 1);       // total 2
        scoreboard.updateScore("Belgium", "Croatia", 1, 1);        // total 2

        List<Match> summary = scoreboard.getSummary();
        String actual = formatSummary(summary);

        // Brazil on top, then the 5 ties in reverse start order
        boolean pass = summary.size() == 6
                && summary.get(0).getHomeTeam().equals("Brazil")
                && summary.get(1).getHomeTeam().equals("Belgium")
                && summary.get(2).getHomeTeam().equals("Portugal")
                && summary.get(3).getHomeTeam().equals("Netherlands")
                && summary.get(4).getHomeTeam().equals("England")
                && summary.get(5).getHomeTeam().equals("Argentina");

        row(26, "Get Summary", "One blowout (10-0) vs five 1-1 ties → blowout first, ties by recency",
                "Brazil 10-0 (total 10), 5 matches at 1-1 (total 2 each)",
                "Brazil, Belgium, Portugal, Netherlands, England, Argentina",
                actual, pass);
    }

    @Test
    @Order(22)
    void report_getSummary_singleMatchRemainingAfterFinishing11() {
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

        // Finish all except Portugal vs Uruguay
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
        String actual = formatSummary(summary);

        boolean pass = summary.size() == 1
                && summary.get(0).getHomeTeam().equals("Portugal")
                && summary.get(0).getHomeScore() == 3
                && summary.get(0).getAwayScore() == 2;

        row(27, "Get Summary", "Start 12, finish 11 → single match remaining",
                "12 matches started, 11 finished, Portugal 3-Uruguay 2 remains",
                "1. Portugal 3 - Uruguay 2",
                actual, pass);
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
