package com.quizsystem.service;

import com.quizsystem.model.Score;
import com.quizsystem.model.User;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ScoreServiceImpl — In-memory score tracking with Map<User, Score>.
 *
 * Stores ALL scores in a list, and maintains a leaderboard map.
 * Optionally delegates to a ScoreRepository for database persistence.
 */
public class ScoreServiceImpl implements ScoreService {

    // Map<User, Score> — tracks BEST score per user (as required)
    private final Map<User, Score> leaderboard = new LinkedHashMap<>();

    // Full history of all scores
    private final List<Score> allScores = new ArrayList<>();

    @Override
    public void recordScore(Score score) {
        allScores.add(score);

        // Update leaderboard: keep the BEST score per user
        Score existing = leaderboard.get(score.getUser());
        if (existing == null || score.getPercentage() > existing.getPercentage()) {
            leaderboard.put(score.getUser(), score);
        }

        System.out.println("[ScoreService] Recorded score for " + score.getUser().getUsername()
            + ": " + score.getCorrectAnswers() + "/" + score.getTotalQuestions());
    }

    @Override
    public List<Score> getScoresForUser(User user) {
        return allScores.stream()
            .filter(s -> s.getUser().equals(user))
            .collect(Collectors.toList());
    }

    @Override
    public Map<User, Score> getLeaderboard() {
        // Return sorted by percentage (descending)
        return leaderboard.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue().getPercentage(), a.getValue().getPercentage()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    @Override
    public List<Score> getAllScores() {
        return new ArrayList<>(allScores);
    }
}
