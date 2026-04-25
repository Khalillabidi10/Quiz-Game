package com.quizsystem.repository;

import com.quizsystem.model.Score;
import com.quizsystem.model.User;
import java.util.List;
import java.util.Map;

/**
 * ScoreRepository — Interface for score persistence.
 */
public interface ScoreRepository {
    void save(Score score);
    List<Score> findByUser(User user);
    List<Score> findAll();
    Map<User, Score> getTopScores();
}
