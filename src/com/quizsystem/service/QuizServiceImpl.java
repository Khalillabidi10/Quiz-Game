package com.quizsystem.service;

import com.quizsystem.model.*;
import com.quizsystem.repository.QuestionRepository;
import java.util.*;
import java.util.stream.Collectors;

/**
 * QuizServiceImpl — Core quiz business logic.
 *
 * DEPENDENCY INJECTION (manual):
 * The repository is passed in via the constructor, not created internally.
 * This makes the service testable and flexible — you can inject a
 * TxtQuestionRepository, CsvQuestionRepository, or JdbcQuestionRepository.
 */
public class QuizServiceImpl implements QuizService<Question> {

    private final QuestionRepository<Question> questionRepo;

    /** Constructor injection — the caller decides which repo to use. */
    public QuizServiceImpl(QuestionRepository<Question> questionRepo) {
        this.questionRepo = questionRepo;
    }

    @Override
    public List<Question> loadQuestions() {
        return questionRepo.findAll();
    }

    @Override
    public List<Question> getQuestionsByCategory(Category category) {
        return questionRepo.findByCategory(category);
    }

    @Override
    public Quiz createQuiz(String title, Category category, int maxQuestions) {
        List<Question> questions;
        if (category == null) {
            questions = questionRepo.findAll();
        } else {
            questions = questionRepo.findByCategory(category);
        }

        // Shuffle for randomization
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);

        // Limit question count
        if (maxQuestions > 0 && maxQuestions < shuffled.size()) {
            shuffled = shuffled.subList(0, maxQuestions);
        }

        String catName = category != null ? category.getDisplayName() : "Mixed";
        return new Quiz(title + " (" + catName + ")", category, shuffled);
    }

    @Override
    public Quiz createQuiz(String title, int maxQuestions) {
        return createQuiz(title, null, maxQuestions);
    }

    @Override
    public void addQuestion(Question question) {
        questionRepo.save(question);
    }

    @Override
    public List<Category> getAvailableCategories() {
        List<Question> all = questionRepo.findAll();
        return all.stream()
            .map(Question::getCategory)
            .distinct()
            .sorted(Comparator.comparing(Category::getDisplayName))
            .collect(Collectors.toList());
    }

    @Override
    public int getQuestionCount() {
        return questionRepo.count();
    }
}
