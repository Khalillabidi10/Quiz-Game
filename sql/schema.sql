-- ============================================================
-- Interactive Quiz System — Database Schema (SQLite)
-- ============================================================
-- Run this to manually create the database structure.
-- Note: DatabaseConnection.java auto-creates these tables,
-- so this file is for reference/documentation only.
-- ============================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL
);

-- Questions table (base info shared by all question types)
CREATE TABLE IF NOT EXISTS questions (
    id INTEGER PRIMARY KEY,
    type TEXT NOT NULL CHECK(type IN ('MCQ', 'TF')),
    question_text TEXT NOT NULL,
    category TEXT NOT NULL,
    time_limit_seconds INTEGER DEFAULT 15
);

-- Multiple-choice answer options
CREATE TABLE IF NOT EXISTS mcq_choices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL,
    choice_text TEXT NOT NULL,
    is_correct INTEGER DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- True/False answers
CREATE TABLE IF NOT EXISTS tf_answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL UNIQUE,
    correct_answer INTEGER NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

-- Score history
CREATE TABLE IF NOT EXISTS scores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    total_questions INTEGER NOT NULL,
    correct_answers INTEGER NOT NULL,
    time_taken_ms INTEGER NOT NULL,
    created_at TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ── Indexes for performance ─────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_mcq_question ON mcq_choices(question_id);
CREATE INDEX IF NOT EXISTS idx_scores_user ON scores(user_id);
CREATE INDEX IF NOT EXISTS idx_questions_category ON questions(category);
