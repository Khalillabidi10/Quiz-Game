package com.quizsystem.model;

/**
 * ============================================================
 * TrueFalseQuestion — True/False Question implementation.
 * ============================================================
 * 
 * POLYMORPHISM IN ACTION:
 * This class and MCQuestion both extend Question, but they behave
 * differently when you call isCorrect() or getChoicesDisplay().
 * 
 * Consider this code:
 *   List<Question> questions = loadQuestions();
 *   for (Question q : questions) {
 *       System.out.println(q.getChoicesDisplay()); // polymorphic call!
 *   }
 * 
 * For MCQuestion → prints "A) ... B) ... C) ... D) ..."
 * For TrueFalseQuestion → prints "True / False"
 * 
 * The caller doesn't need to use instanceof or type-check.
 * This is the power of polymorphism — one interface, multiple behaviors.
 */
public class TrueFalseQuestion extends Question {

    // ── Field specific to T/F ───────────────────────────────────
    private final boolean correctAnswer;

    // ── Constructor ─────────────────────────────────────────────
    /**
     * Creates a new true/false question.
     *
     * @param id                unique identifier
     * @param text              the statement to evaluate (e.g., "The Earth is round.")
     * @param correctAnswer     true if the statement is true, false otherwise
     * @param category          question category
     * @param timeLimitSeconds  time limit in seconds (0 = unlimited)
     */
    public TrueFalseQuestion(int id, String text, boolean correctAnswer,
                             Category category, int timeLimitSeconds) {
        super(id, text, category, timeLimitSeconds);
        this.correctAnswer = correctAnswer;
    }

    // ── Getter ──────────────────────────────────────────────────

    public boolean getCorrectAnswer() {
        return correctAnswer;
    }

    // ── Abstract method implementations ─────────────────────────

    /**
     * Checks if the user's answer matches.
     * Accepts: "true", "t", "1", "yes" for TRUE
     *          "false", "f", "0", "no"  for FALSE
     * Case-insensitive.
     *
     * @param answer the user's answer
     * @return true if the answer matches correctAnswer
     */
    @Override
    public boolean isCorrect(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return false;
        }

        String trimmed = answer.trim().toLowerCase();

        boolean userSaidTrue = trimmed.equals("true") 
                            || trimmed.equals("t")
                            || trimmed.equals("1")
                            || trimmed.equals("yes");

        boolean userSaidFalse = trimmed.equals("false") 
                             || trimmed.equals("f")
                             || trimmed.equals("0")
                             || trimmed.equals("no");

        if (userSaidTrue) return correctAnswer;
        if (userSaidFalse) return !correctAnswer;

        return false; // unrecognized input
    }

    /**
     * Simple display: "  True / False"
     */
    @Override
    public String getChoicesDisplay() {
        return "  True / False";
    }

    @Override
    public String getCorrectAnswerDisplay() {
        return correctAnswer ? "True" : "False";
    }

    /**
     * Serializes to pipe-delimited format:
     * TF|id|text|CATEGORY|timeLimit|true/false
     */
    @Override
    public String toFileString() {
        return String.format("TF|%d|%s|%s|%d|%s",
            getId(), getText(), getCategory().name(),
            getTimeLimitSeconds(), correctAnswer);
    }

    /**
     * Serializes to CSV format.
     */
    @Override
    public String toCsvString() {
        return String.format("TF,%d,\"%s\",%s,%d,,%s",
            getId(), getText().replace("\"", "\"\""),
            getCategory().name(), getTimeLimitSeconds(),
            correctAnswer);
    }
}
