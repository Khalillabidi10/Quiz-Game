package com.quizsystem.repository;

import com.quizsystem.model.*;
import com.quizsystem.util.DatabaseConnection;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JdbcQuestionRepository — Database-backed question storage.
 * Uses SQLite via JDBC PreparedStatements (prevents SQL injection).
 */
public class JdbcQuestionRepository implements QuestionRepository<Question> {

    private final DatabaseConnection dbConn;

    public JdbcQuestionRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    @Override
    public List<Question> findAll() {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT id, type, question_text, category, time_limit_seconds FROM questions";
        try (Connection conn = dbConn.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Question q = mapQuestion(conn, rs);
                if (q != null) questions.add(q);
            }
        } catch (SQLException e) {
            System.err.println("[JdbcRepo] Error loading questions: " + e.getMessage());
        }
        return questions;
    }

    @Override
    public Optional<Question> findById(int id) {
        String sql = "SELECT id, type, question_text, category, time_limit_seconds FROM questions WHERE id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(mapQuestion(conn, rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcRepo] Error finding question: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Question> findByCategory(Category category) {
        return findAll().stream()
            .filter(q -> q.getCategory() == category)
            .collect(Collectors.toList());
    }

    @Override
    public void save(Question question) {
        try {
            Connection conn = dbConn.getConnection();
            // Delete existing first (simple upsert)
            deleteById(question.getId());

            String sql = "INSERT INTO questions (id, type, question_text, category, time_limit_seconds) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, question.getId());
                ps.setString(2, question instanceof MCQuestion ? "MCQ" : "TF");
                ps.setString(3, question.getText());
                ps.setString(4, question.getCategory().name());
                ps.setInt(5, question.getTimeLimitSeconds());
                ps.executeUpdate();
            }

            // Save type-specific data
            if (question instanceof MCQuestion) {
                saveMcqChoices(conn, (MCQuestion) question);
            } else if (question instanceof TrueFalseQuestion) {
                saveTfAnswer(conn, (TrueFalseQuestion) question);
            }
        } catch (SQLException e) {
            System.err.println("[JdbcRepo] Error saving question: " + e.getMessage());
        }
    }

    @Override
    public void saveAll(List<Question> questions) {
        for (Question q : questions) save(q);
    }

    @Override
    public boolean deleteById(int id) {
        try {
            Connection conn = dbConn.getConnection();
            // Delete child records first (referential integrity)
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM mcq_choices WHERE question_id = ?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tf_answers WHERE question_id = ?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM questions WHERE id = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[JdbcRepo] Error deleting question: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int count() {
        try (Connection conn = dbConn.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    // ── Private helpers ─────────────────────────────────────────

    private Question mapQuestion(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String type = rs.getString("type");
        String text = rs.getString("question_text");
        Category cat = Category.fromString(rs.getString("category"));
        int timeLimit = rs.getInt("time_limit_seconds");

        if ("MCQ".equals(type)) {
            return loadMcq(conn, id, text, cat, timeLimit);
        } else if ("TF".equals(type)) {
            return loadTf(conn, id, text, cat, timeLimit);
        }
        return null;
    }

    private MCQuestion loadMcq(Connection conn, int id, String text, Category cat, int timeLimit) throws SQLException {
        List<String> choices = new ArrayList<>();
        int correctIdx = 0;
        String sql = "SELECT choice_text, is_correct FROM mcq_choices WHERE question_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                int idx = 0;
                while (rs.next()) {
                    choices.add(rs.getString("choice_text"));
                    if (rs.getInt("is_correct") == 1) correctIdx = idx;
                    idx++;
                }
            }
        }
        if (choices.isEmpty()) return null;
        return new MCQuestion(id, text, choices, correctIdx, cat, timeLimit);
    }

    private TrueFalseQuestion loadTf(Connection conn, int id, String text, Category cat, int timeLimit) throws SQLException {
        String sql = "SELECT correct_answer FROM tf_answers WHERE question_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TrueFalseQuestion(id, text, rs.getInt("correct_answer") == 1, cat, timeLimit);
                }
            }
        }
        return null;
    }

    private void saveMcqChoices(Connection conn, MCQuestion mcq) throws SQLException {
        String sql = "INSERT INTO mcq_choices (question_id, choice_text, is_correct) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            List<String> choices = mcq.getChoices();
            for (int i = 0; i < choices.size(); i++) {
                ps.setInt(1, mcq.getId());
                ps.setString(2, choices.get(i));
                ps.setInt(3, i == mcq.getCorrectIndex() ? 1 : 0);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void saveTfAnswer(Connection conn, TrueFalseQuestion tf) throws SQLException {
        String sql = "INSERT INTO tf_answers (question_id, correct_answer) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tf.getId());
            ps.setInt(2, tf.getCorrectAnswer() ? 1 : 0);
            ps.executeUpdate();
        }
    }
}
