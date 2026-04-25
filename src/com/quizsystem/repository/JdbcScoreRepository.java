package com.quizsystem.repository;

import com.quizsystem.model.Score;
import com.quizsystem.model.User;
import com.quizsystem.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * JdbcScoreRepository — Database-backed score storage.
 */
public class JdbcScoreRepository implements ScoreRepository {

    private final DatabaseConnection dbConn;
    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public JdbcScoreRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    @Override
    public void save(Score score) {
        String sql = "INSERT INTO scores (user_id, total_questions, correct_answers, time_taken_ms) VALUES (?,?,?,?)";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, score.getUser().getId());
            ps.setInt(2, score.getTotalQuestions());
            ps.setInt(3, score.getCorrectAnswers());
            ps.setLong(4, score.getTimeTakenMs());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ScoreRepo] Error saving score: " + e.getMessage());
        }
    }

    @Override
    public List<Score> findByUser(User user) {
        List<Score> scores = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM scores s JOIN users u ON s.user_id = u.id WHERE s.user_id = ? ORDER BY s.created_at DESC";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) scores.add(mapScore(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ScoreRepo] Error: " + e.getMessage());
        }
        return scores;
    }

    @Override
    public List<Score> findAll() {
        List<Score> scores = new ArrayList<>();
        String sql = "SELECT s.*, u.username FROM scores s JOIN users u ON s.user_id = u.id ORDER BY s.created_at DESC";
        try (Connection conn = dbConn.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) scores.add(mapScore(rs));
        } catch (SQLException e) {
            System.err.println("[ScoreRepo] Error: " + e.getMessage());
        }
        return scores;
    }

    @Override
    public Map<User, Score> getTopScores() {
        Map<User, Score> top = new LinkedHashMap<>();
        // Best score per user (highest percentage)
        String sql = "SELECT s.*, u.username FROM scores s JOIN users u ON s.user_id = u.id " +
                     "ORDER BY (CAST(s.correct_answers AS REAL) / s.total_questions) DESC";
        try (Connection conn = dbConn.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Score score = mapScore(rs);
                top.putIfAbsent(score.getUser(), score); // keep only best per user
            }
        } catch (SQLException e) {
            System.err.println("[ScoreRepo] Error: " + e.getMessage());
        }
        return top;
    }

    private Score mapScore(ResultSet rs) throws SQLException {
        User user = new User(rs.getInt("user_id"), rs.getString("username"));
        String dateStr = rs.getString("created_at");
        LocalDateTime ts = dateStr != null ? LocalDateTime.parse(dateStr, DB_FORMAT) : LocalDateTime.now();
        return new Score(user, rs.getInt("total_questions"),
            rs.getInt("correct_answers"), rs.getLong("time_taken_ms"), ts);
    }
}
