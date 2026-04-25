package com.quizsystem.repository;

import com.quizsystem.model.Category;
import com.quizsystem.model.Question;
import java.util.List;
import java.util.Optional;

/**
 * QuestionRepository — Generic interface for question persistence.
 *
 * GENERICS:
 * The type parameter <T extends Question> means this interface works
 * with ANY subclass of Question (MCQuestion, TrueFalseQuestion, etc.)
 * while still providing type safety.
 *
 * REPOSITORY PATTERN:
 * Separates data access logic from business logic. The service layer
 * calls repository methods without knowing if data comes from a file,
 * CSV, database, or even a web API.
 */
public interface QuestionRepository<T extends Question> {

    /** Returns all stored questions. */
    List<T> findAll();

    /** Finds a question by its unique ID. */
    Optional<T> findById(int id);

    /** Finds all questions in a given category. */
    List<T> findByCategory(Category category);

    /** Saves a single question (insert or update). */
    void save(T question);

    /** Saves multiple questions at once. */
    void saveAll(List<T> questions);

    /** Deletes a question by ID. Returns true if found and deleted. */
    boolean deleteById(int id);

    /** Returns the total count of stored questions. */
    int count();
}
