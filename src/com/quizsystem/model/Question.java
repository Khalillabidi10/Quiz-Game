package com.quizsystem.model;

/**
 * ============================================================
 * Question — Abstract base class for all question types.
 * ============================================================
 * 
 * OOP CONCEPTS DEMONSTRATED:
 * 
 * 1. ABSTRACTION — This class defines *what* a question can do
 *    (isCorrect, getChoicesDisplay, etc.) without specifying *how*.
 *    Subclasses like MCQuestion and TrueFalseQuestion provide the "how".
 * 
 * 2. ENCAPSULATION — All fields are private. Access is controlled
 *    through getters. This protects the internal state from being
 *    modified in unexpected ways.
 * 
 * 3. INHERITANCE — MCQuestion and TrueFalseQuestion extend this class,
 *    inheriting common fields (id, text, category, timeLimitSeconds)
 *    and adding their own specialized behavior.
 * 
 * WHY ABSTRACT?
 * - A "Question" by itself is incomplete — it doesn't know its choices
 *   or how to check answers. Only concrete subclasses are meaningful.
 * - Making it abstract prevents direct instantiation:
 *   `new Question(...)` → compile error ✓
 * - It serves as a CONTRACT that all question types must fulfill.
 * 
 * DESIGN PATTERN: Template Method
 * - The toString() method uses getChoicesDisplay() and getCorrectAnswerDisplay(),
 *   which are abstract. Subclasses fill in the details. This is the
 *   Template Method pattern in action.
 */
public abstract class Question {

    // ── Private fields (Encapsulation) ──────────────────────────
    private int id;
    private String text;
    private Category category;
    private int timeLimitSeconds;

    // ── Constructor ─────────────────────────────────────────────
    /**
     * Constructs a new Question.
     *
     * @param id                unique identifier for this question
     * @param text              the question text displayed to the user
     * @param category          the topic category (e.g., SCIENCE, HISTORY)
     * @param timeLimitSeconds  seconds allowed to answer (0 = unlimited)
     */
    public Question(int id, String text, Category category, int timeLimitSeconds) {
        this.id = id;
        this.text = text;
        this.category = category;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    // ── Getters (no setters → immutable after construction) ─────

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Category getCategory() {
        return category;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    // ── Abstract methods (subclasses MUST implement) ────────────

    /**
     * Checks whether the given answer is correct.
     * 
     * POLYMORPHISM: The caller doesn't need to know if this is an
     * MCQuestion or TrueFalseQuestion — they just call isCorrect().
     *
     * @param answer the user's answer (e.g., "A", "true")
     * @return true if the answer is correct
     */
    public abstract boolean isCorrect(String answer);

    /**
     * Returns a formatted string showing the available choices.
     * For MCQ: "A) Paris  B) London  C) Berlin  D) Madrid"
     * For T/F: "True / False"
     *
     * @return formatted choices string
     */
    public abstract String getChoicesDisplay();

    /**
     * Returns the correct answer in a human-readable format.
     *
     * @return the correct answer as a display string
     */
    public abstract String getCorrectAnswerDisplay();

    /**
     * Serializes this question to a pipe-delimited string for file storage.
     * Format: TYPE|ID|TEXT|CATEGORY|TIME_LIMIT|...type-specific fields...
     *
     * @return the serialized string representation
     */
    public abstract String toFileString();

    /**
     * Serializes this question to a CSV row.
     * Fields containing commas are quoted.
     *
     * @return the CSV row string
     */
    public abstract String toCsvString();

    // ── Common methods ──────────────────────────────────────────

    /**
     * Template Method: uses abstract methods to build a complete
     * string representation. Subclasses don't need to override this.
     */
    @Override
    public String toString() {
        return String.format(
            "[Q%d | %s | %ds] %s\n%s",
            id, category.getDisplayName(), timeLimitSeconds,
            text, getChoicesDisplay()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return id == question.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
