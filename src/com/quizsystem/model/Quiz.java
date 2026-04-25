package com.quizsystem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quiz — Aggregates questions into a playable quiz session.
 * Supports shuffling and category filtering.
 */
public class Quiz {

    private final String title;
    private final Category category;
    private final List<Question> questions;

    public Quiz(String title, Category category, List<Question> questions) {
        this.title = title;
        this.category = category;
        this.questions = new ArrayList<>(questions);
    }

    public String getTitle() { return title; }
    public Category getCategory() { return category; }
    public int getQuestionCount() { return questions.size(); }
    public List<Question> getQuestions() { return Collections.unmodifiableList(questions); }

    /**
     * Returns a new list with questions in random order.
     * Original list is NOT modified (defensive copy pattern).
     */
    public List<Question> getShuffledQuestions() {
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * Filters questions by category.
     */
    public List<Question> getQuestionsByCategory(Category cat) {
        return questions.stream()
            .filter(q -> q.getCategory() == cat)
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("Quiz: '%s' [%s] — %d questions",
            title, category.getDisplayName(), questions.size());
    }
}
