package com.sportradar.scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Scoreboard {

    private final List<Match> matches = new ArrayList<>();
    private long matchCounter = 0;

    public void startMatch(String homeTeam, String awayTeam) {
        validateTeamName(homeTeam, "Home team");
        validateTeamName(awayTeam, "Away team");

        if (homeTeam.trim().equalsIgnoreCase(awayTeam.trim())) {
            throw new IllegalArgumentException("Home and away teams must be different");
        }

        if (isTeamPlaying(homeTeam) || isTeamPlaying(awayTeam)) {
            throw new IllegalArgumentException("Team is already in an active match");
        }

        matches.add(new Match(homeTeam, awayTeam, ++matchCounter));
    }

    public void updateScore(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Scores cannot be negative");
        }

        Match match = findMatch(homeTeam, awayTeam);
        match.setScore(homeScore, awayScore);
    }

    public void finishMatch(String homeTeam, String awayTeam) {
        Match match = findMatch(homeTeam, awayTeam);
        matches.remove(match);
    }

    public List<Match> getSummary() {
        List<Match> sorted = new ArrayList<>(matches);
        sorted.sort(Comparator
                .comparingInt(Match::getTotalScore)
                .reversed()
                .thenComparingLong(m -> -m.getStartOrder()));
        return List.copyOf(sorted);
    }

    private void validateTeamName(String name, String label) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " name cannot be null or blank");
        }
    }

    private Match findMatch(String homeTeam, String awayTeam) {
        return matches.stream()
                .filter(m -> m.getHomeTeam().equalsIgnoreCase(homeTeam.trim())
                        && m.getAwayTeam().equalsIgnoreCase(awayTeam.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
    }

    private boolean isTeamPlaying(String team) {
        return matches.stream().anyMatch(m ->
                m.getHomeTeam().equalsIgnoreCase(team.trim())
                        || m.getAwayTeam().equalsIgnoreCase(team.trim()));
    }
}
