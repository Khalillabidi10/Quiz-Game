package com.quizsystem.service;

import com.quizsystem.model.Score;
import com.quizsystem.model.User;
import java.util.List;
import java.util.Map;

/**
 * ScoreService — Interface for score tracking and leaderboard.
 */
public interface ScoreService {

    /** Records a quiz score. */
    void recordScore(Score score);

    /** Gets all scores for a specific user. */
    List<Score> getScoresForUser(User user);

    /** Gets the leaderboard: best score per user, sorted by percentage. */
    Map<User, Score> getLeaderboard();

    /** Gets all scores across all users. */
    List<Score> getAllScores();
}
