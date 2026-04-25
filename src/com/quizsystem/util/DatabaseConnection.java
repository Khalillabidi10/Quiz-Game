package com.quizsystem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseConnection — Singleton for JDBC connection management.
 *
 * Uses SQLite (file-based, zero-config database).
 * Auto-creates tables on first connection.
 *
 * SINGLETON PATTERN:
 * Only one instance exists. Call DatabaseConnection.getInstance()
 * from anywhere to get the shared connection manager.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private final String dbPath;

    private DatabaseConnection(String dbPath) {
        this.dbPath = dbPath;
    }

    /** Gets the singleton instance, creating it if needed. */
    public static synchronized DatabaseConnection getInstance(String dbPath) {
        if (instance == null) {
            instance = new DatabaseConnection(dbPath);
        }
        return instance;
    }

    /** Gets (or creates) the JDBC connection. */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                initTables();
                System.out.println("[DB] Connected to SQLite: " + dbPath);
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found. "
                    + "Add sqlite-jdbc JAR to classpath.", e);
            }
        }
        return connection;
    }

    /** Creates tables if they don't exist. */
    private void initTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT UNIQUE NOT NULL" +
                ")");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS questions (" +
                "  id INTEGER PRIMARY KEY," +
                "  type TEXT NOT NULL," +
                "  question_text TEXT NOT NULL," +
                "  category TEXT NOT NULL," +
                "  time_limit_seconds INTEGER DEFAULT 15" +
                ")");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS mcq_choices (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  question_id INTEGER NOT NULL," +
                "  choice_text TEXT NOT NULL," +
                "  is_correct INTEGER DEFAULT 0," +
                "  FOREIGN KEY (question_id) REFERENCES questions(id)" +
                ")");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tf_answers (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  question_id INTEGER NOT NULL UNIQUE," +
                "  correct_answer INTEGER NOT NULL," +
                "  FOREIGN KEY (question_id) REFERENCES questions(id)" +
                ")");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS scores (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER NOT NULL," +
                "  total_questions INTEGER NOT NULL," +
                "  correct_answers INTEGER NOT NULL," +
                "  time_taken_ms INTEGER NOT NULL," +
                "  created_at TEXT DEFAULT (datetime('now'))," +
                "  FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")");
        }
    }

    /** Closes the connection. */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }

    /** Checks if the SQLite driver is available. */
    public static boolean isAvailable() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
