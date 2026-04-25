package com.quizsystem.repository;

import com.quizsystem.model.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CsvQuestionRepository — Reads/writes questions to a .csv file.
 *
 * CSV FORMAT (with header):
 *   type,id,text,category,timeLimit,choices,correctAnswer
 *   MCQ,1,"What is 2+2?",MATH,15,"3;4;5;6",1
 *   TF,2,"The sky is blue.",SCIENCE,10,,true
 *
 * Handles quoted fields containing commas.
 */
public class CsvQuestionRepository implements QuestionRepository<Question> {

    private static final String HEADER = "type,id,text,category,timeLimit,choices,correctAnswer";
    private final String filePath;
    private final List<Question> cache = new ArrayList<>();

    public CsvQuestionRepository(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }

    @Override
    public List<Question> findAll() { return new ArrayList<>(cache); }

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
    public int count() { return cache.size(); }

    // ── File I/O ────────────────────────────────────────────────

    private void loadFromFile() {
        cache.clear();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("[CsvRepo] File not found: " + filePath + " — starting empty.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (isHeader) { isHeader = false; continue; } // skip header
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Question q = parseCsvLine(line);
                    if (q != null) cache.add(q);
                } catch (Exception e) {
                    System.err.println("[CsvRepo] Error parsing line " + lineNum + ": " + e.getMessage());
                }
            }
            System.out.println("[CsvRepo] Loaded " + cache.size() + " questions from " + filePath);
        } catch (IOException e) {
            System.err.println("[CsvRepo] Error reading file: " + e.getMessage());
        }
    }

    private void writeToFile() {
        File file = new File(filePath);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);
            writer.newLine();
            for (Question q : cache) {
                writer.write(q.toCsvString());
                writer.newLine();
            }
            System.out.println("[CsvRepo] Saved " + cache.size() + " questions to " + filePath);
        } catch (IOException e) {
            System.err.println("[CsvRepo] Error writing file: " + e.getMessage());
        }
    }

    /**
     * Parses a CSV line, handling quoted fields.
     * Uses a simple state machine to handle commas within quotes.
     */
    static Question parseCsvLine(String line) {
        List<String> fields = splitCsvLine(line);
        if (fields.size() < 7) return null;

        String type = fields.get(0).trim().toUpperCase();
        int id = Integer.parseInt(fields.get(1).trim());
        String text = fields.get(2).trim();
        Category cat = Category.fromString(fields.get(3).trim());
        int timeLimit = Integer.parseInt(fields.get(4).trim());

        if (type.equals("MCQ")) {
            String choicesStr = fields.get(5).trim();
            List<String> choices = Arrays.asList(choicesStr.split(";"));
            int correctIdx = Integer.parseInt(fields.get(6).trim());
            return new MCQuestion(id, text, choices, correctIdx, cat, timeLimit);
        } else if (type.equals("TF")) {
            boolean answer = Boolean.parseBoolean(fields.get(6).trim());
            return new TrueFalseQuestion(id, text, answer, cat, timeLimit);
        }
        return null;
    }

    /** Splits a CSV line respecting quoted fields. */
    static List<String> splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); // escaped quote
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
