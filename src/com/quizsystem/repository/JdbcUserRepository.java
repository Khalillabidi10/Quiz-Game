package com.quizsystem.repository;

import com.quizsystem.model.User;
import com.quizsystem.util.DatabaseConnection;
import java.sql.*;
import java.util.*;

/**
 * JdbcUserRepository — Database-backed user storage.
 */
public class JdbcUserRepository implements UserRepository {

    private final DatabaseConnection dbConn;

    public JdbcUserRepository(DatabaseConnection dbConn) {
        this.dbConn = dbConn;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection conn = dbConn.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username FROM users")) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username")));
            }
        } catch (SQLException e) {
            System.err.println("[UserRepo] Error: " + e.getMessage());
        }
        return users;
    }

    @Override
    public Optional<User> findById(int id) {
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, username FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(new User(rs.getInt("id"), rs.getString("username")));
            }
        } catch (SQLException e) { /* ignore */ }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, username FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(new User(rs.getInt("id"), rs.getString("username")));
            }
        } catch (SQLException e) { /* ignore */ }
        return Optional.empty();
    }

    @Override
    public User save(User user) {
        // Try to find existing user first
        Optional<User> existing = findByUsername(user.getUsername());
        if (existing.isPresent()) return existing.get();

        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO users (username) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), user.getUsername());
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserRepo] Error saving user: " + e.getMessage());
        }
        return user;
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
}
