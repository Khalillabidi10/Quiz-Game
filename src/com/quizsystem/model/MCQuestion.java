package com.quizsystem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ============================================================
 * MCQuestion — Multiple-Choice Question implementation.
 * ============================================================
 * 
 * INHERITANCE: Extends the abstract Question class, providing
 * concrete implementations for all abstract methods.
 * 
 * POLYMORPHISM: An MCQuestion can be treated as a Question anywhere
 * in the codebase. When you call question.isCorrect("B"), Java's
 * dynamic dispatch calls THIS class's version of isCorrect().
 * 
 * Example usage:
 *   Question q = new MCQuestion(1, "Capital of France?",
 *       List.of("Paris", "London", "Berlin", "Madrid"),
 *       0, Category.GENERAL, 15);
 *   
 *   q.isCorrect("A");  // → true  (Paris is at index 0 → "A")
 *   q.isCorrect("B");  // → false
 * 
 * ANSWER FORMAT:
 * The user answers with a letter: A, B, C, D, etc.
 * Internally, "A" maps to index 0, "B" to index 1, and so on.
 */
public class MCQuestion extends Question {

    // ── Fields specific to MCQ ──────────────────────────────────
    private final List<String> choices;    // e.g., ["Paris", "London", "Berlin", "Madrid"]
    private final int correctIndex;        // 0-based index into the choices list

    // ── Constructor ─────────────────────────────────────────────
    /**
     * Creates a new multiple-choice question.
     *
     * @param id                unique identifier
     * @param text              the question text
     * @param choices           list of answer choices (2-6 options)
     * @param correctIndex      0-based index of the correct choice
     * @param category          question category
     * @param timeLimitSeconds  time limit in seconds (0 = unlimited)
     * @throws IllegalArgumentException if correctIndex is out of bounds
     */
    public MCQuestion(int id, String text, List<String> choices,
                      int correctIndex, Category category, int timeLimitSeconds) {
        super(id, text, category, timeLimitSeconds);

        // Defensive copy — prevents external code from modifying our list
        this.choices = new ArrayList<>(choices);

        // Validate correctIndex
        if (correctIndex < 0 || correctIndex >= choices.size()) {
            throw new IllegalArgumentException(
                "correctIndex " + correctIndex + " is out of bounds for " 
                + choices.size() + " choices");
        }
        this.correctIndex = correctIndex;
    }

    // ── Getters ─────────────────────────────────────────────────

    /**
     * Returns an unmodifiable view of the choices.
     * This prevents callers from accidentally modifying the list.
     */
    public List<String> getChoices() {
        return Collections.unmodifiableList(choices);
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getCorrectAnswer() {
        return choices.get(correctIndex);
    }

    // ── Abstract method implementations ─────────────────────────

    /**
     * Checks if the user's answer letter matches the correct choice.
     * Accepts both "A" and "a" (case-insensitive).
     * Also accepts the 1-based number: "1", "2", etc.
     *
     * @param answer the user's answer (e.g., "A" or "1")
     * @return true if correct
     */
    @Override
    public boolean isCorrect(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return false;
        }

        String trimmed = answer.trim().toUpperCase();

        // Support letter-based answers: A=0, B=1, C=2, D=3, ...
        if (trimmed.length() == 1 && Character.isLetter(trimmed.charAt(0))) {
            int index = trimmed.charAt(0) - 'A';
            return index == correctIndex;
        }

        // Support number-based answers: 1=0, 2=1, 3=2, 4=3, ...
        try {
            int num = Integer.parseInt(trimmed);
            return (num - 1) == correctIndex;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Formats the choices with letter prefixes:
     *   A) Paris
     *   B) London
     *   C) Berlin
     *   D) Madrid
     */
    @Override
    public String getChoicesDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < choices.size(); i++) {
            char letter = (char) ('A' + i);
            sb.append("  ").append(letter).append(") ").append(choices.get(i));
            if (i < choices.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public String getCorrectAnswerDisplay() {
        char letter = (char) ('A' + correctIndex);
        return letter + ") " + choices.get(correctIndex);
    }

    /**
     * Serializes to pipe-delimited format:
     * MCQ|id|text|CATEGORY|timeLimit|choice1;choice2;...|correctIndex
     */
    @Override
    public String toFileString() {
        String choicesStr = String.join(";", choices);
        return String.format("MCQ|%d|%s|%s|%d|%s|%d",
            getId(), getText(), getCategory().name(),
            getTimeLimitSeconds(), choicesStr, correctIndex);
    }

    /**
     * Serializes to CSV format.
     * Text is quoted to handle commas within question text.
     */
    @Override
    public String toCsvString() {
        String choicesStr = String.join(";", choices);
        return String.format("MCQ,%d,\"%s\",%s,%d,\"%s\",%d",
            getId(), getText().replace("\"", "\"\""),
            getCategory().name(), getTimeLimitSeconds(),
            choicesStr, correctIndex);
    }
}
