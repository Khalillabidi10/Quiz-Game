package com.quizsystem.repository;

import com.quizsystem.model.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TxtQuestionRepository — Reads/writes questions to a .txt file.
 *
 * FILE FORMAT (pipe-delimited):
 *   MCQ|1|What is 2+2?|MATH|15|3;4;5;6|1
 *   TF|2|The sky is blue.|SCIENCE|10|true
 *
 * Fields: TYPE|ID|TEXT|CATEGORY|TIME_LIMIT|CHOICES_OR_ANSWER|CORRECT_INDEX
 */
public class TxtQuestionRepository implements QuestionRepository<Question> {

    private final String filePath;
    private final List<Question> cache = new ArrayList<>();

    public TxtQuestionRepository(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }

    // ── Interface implementations ───────────────────────────────

    @Override
    public List<Question> findAll() {
        return new ArrayList<>(cache);
    }

    @Override
    public Optional<Question> findById(int id) {
        return cache.stream().filter(q -> q.getId() == id).findFirst();
    }

    @Override
    public List<Question> findByCategory(Category category) {
        return cache.stream()
            .filter(q -> q.getCategory() == category)
            .collect(Collectors.toList());
    }

    @Override
    public void save(Question question) {
        // Remove existing question with same ID, then add
        cache.removeIf(q -> q.getId() == question.getId());
        cache.add(question);
        writeToFile();
    }

    @Override
    public void saveAll(List<Question> questions) {
        cache.clear();
        cache.addAll(questions);
        writeToFile();
    }

    @Override
    public boolean deleteById(int id) {
        boolean removed = cache.removeIf(q -> q.getId() == id);
        if (removed) writeToFile();
        return removed;
    }

    @Override
    public int count() {
        return cache.size();
    }

    // ── File I/O ────────────────────────────────────────────────

    private void loadFromFile() {
        cache.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("[TxtRepo] File not found: " + filePath + " — starting empty.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue; // skip comments

                try {
                    Question q = parseLine(line);
                    if (q != null) cache.add(q);
                } catch (Exception e) {
                    System.err.println("[TxtRepo] Error parsing line " + lineNum + ": " + e.getMessage());
                }
            }
            System.out.println("[TxtRepo] Loaded " + cache.size() + " questions from " + filePath);
        } catch (IOException e) {
            System.err.println("[TxtRepo] Error reading file: " + e.getMessage());
        }
    }

    private void writeToFile() {
        File file = new File(filePath);
        file.getParentFile().mkdirs(); // ensure directory exists

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# Interactive Quiz System — Question Bank");
            writer.newLine();
            writer.write("# Format: TYPE|ID|TEXT|CATEGORY|TIME_LIMIT|CHOICES|CORRECT_INDEX");
            writer.newLine();
            writer.newLine();

            for (Question q : cache) {
                writer.write(q.toFileString());
                writer.newLine();
            }
            System.out.println("[TxtRepo] Saved " + cache.size() + " questions to " + filePath);
        } catch (IOException e) {
            System.err.println("[TxtRepo] Error writing file: " + e.getMessage());
        }
    }

    /**
     * Parses a pipe-delimited line into a Question object.
     */
    static Question parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        String type = parts[0].trim().toUpperCase();

        if (type.equals("MCQ") && parts.length >= 7) {
            int id = Integer.parseInt(parts[1].trim());
            String text = parts[2].trim();
            Category cat = Category.fromString(parts[3].trim());
            int timeLimit = Integer.parseInt(parts[4].trim());
            List<String> choices = Arrays.asList(parts[5].trim().split(";"));
            int correctIdx = Integer.parseInt(parts[6].trim());
            return new MCQuestion(id, text, choices, correctIdx, cat, timeLimit);
        } else if (type.equals("TF") && parts.length >= 6) {
            int id = Integer.parseInt(parts[1].trim());
            String text = parts[2].trim();
            Category cat = Category.fromString(parts[3].trim());
            int timeLimit = Integer.parseInt(parts[4].trim());
            boolean answer = Boolean.parseBoolean(parts[5].trim());
            return new TrueFalseQuestion(id, text, answer, cat, timeLimit);
        }
        System.err.println("[TxtRepo] Unknown question type: " + type);
        return null;
    }
}
