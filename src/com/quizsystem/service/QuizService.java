package com.quizsystem.service;

import com.quizsystem.model.Category;
import com.quizsystem.model.Question;
import com.quizsystem.model.Quiz;
import java.util.List;

/**
 * QuizService — Generic interface for quiz business logic.
 *
 * GENERICS: <T extends Question> ensures type safety.
 * The service works with any Question subtype while the compiler
 * enforces that you can't accidentally mix incompatible types.
 */
public interface QuizService<T extends Question> {

    /** Loads all available questions. */
    List<T> loadQuestions();

    /** Loads questions filtered by category. */
    List<T> getQuestionsByCategory(Category category);

    /** Creates a quiz with optional category filter and question limit. */
    Quiz createQuiz(String title, Category category, int maxQuestions);

    /** Creates a quiz with all categories. */
    Quiz createQuiz(String title, int maxQuestions);

    /** Adds a new question to the question bank. */
    void addQuestion(T question);

    /** Returns all available categories that have questions. */
    List<Category> getAvailableCategories();

    /** Returns total question count. */
    int getQuestionCount();
}
