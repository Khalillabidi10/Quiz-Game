package com.quizsystem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Score — Records the result of a quiz attempt.
 * Immutable: all fields are final, no setters.
 */
public class Score {

    private final User user;
    private final int totalQuestions;
    private final int correctAnswers;
    private final long timeTakenMs;
    private final LocalDateTime timestamp;

    private static final DateTimeFormatter DISPLAY_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Score(User user, int totalQuestions, int correctAnswers, long timeTakenMs) {
        this.user = user;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.timeTakenMs = timeTakenMs;
        this.timestamp = LocalDateTime.now();
    }

    public Score(User user, int totalQuestions, int correctAnswers,
                 long timeTakenMs, LocalDateTime timestamp) {
        this.user = user;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.timeTakenMs = timeTakenMs;
        this.timestamp = timestamp;
    }

    public User getUser() { return user; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public long getTimeTakenMs() { return timeTakenMs; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /** Calculates percentage score (0.0 – 100.0). */
    public double getPercentage() {
        if (totalQuestions == 0) return 0.0;
        return (double) correctAnswers / totalQuestions * 100.0;
    }

    /** Formats time as "Xm Ys" or just "Ys". */
    public String getFormattedTime() {
        long totalSeconds = timeTakenMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes > 0) return String.format("%dm %ds", minutes, seconds);
        return String.format("%ds", seconds);
    }

    /** Returns letter grade based on percentage. */
    public String getGrade() {
        double pct = getPercentage();
        if (pct >= 90) return "A+";
        if (pct >= 80) return "A";
        if (pct >= 70) return "B";
        if (pct >= 60) return "C";
        if (pct >= 50) return "D";
        return "F";
    }

    @Override
    public String toString() {
        return String.format(
            "  Score: %d/%d (%.1f%%) | Grade: %s | Time: %s | Date: %s",
            correctAnswers, totalQuestions, getPercentage(),
            getGrade(), getFormattedTime(),
            timestamp.format(DISPLAY_FORMAT));
    }
}
