package com.quizsystem.ui;

import com.quizsystem.model.*;
import com.quizsystem.service.*;
import java.util.*;

/**
 * ConsoleUI — Full interactive console interface for the quiz system.
 *
 * Features: Take quiz, view scores, manage questions, category filter.
 */
public class ConsoleUI {

    private final QuizService<Question> quizService;
    private final ScoreService scoreService;
    private final TimerService timerService;
    private final Scanner scanner;
    private User currentUser;
    private int nextUserId = 1;

    public ConsoleUI(QuizService<Question> quizService, ScoreService scoreService) {
        this.quizService = quizService;
        this.scoreService = scoreService;
        this.timerService = new TimerService();
        this.scanner = new Scanner(System.in);
    }

    /** Starts the console application main loop. */
    public void start() {
        printBanner();
        login();
        mainMenu();
    }

    // ── Banner & Login ──────────────────────────────────────────

    private void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║          INTERACTIVE QUIZ SYSTEM v1.0           ║");
        System.out.println("║         ─── Test Your Knowledge! ───           ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private void login() {
        System.out.print("  Enter your username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) username = "Player";
        currentUser = new User(nextUserId++, username);
        System.out.println("\n  Welcome, " + currentUser.getUsername() + "! Let's get started.\n");
    }

    // ── Main Menu ───────────────────────────────────────────────

    private void mainMenu() {
        while (true) {
            System.out.println("┌──────────────────────────────────────┐");
            System.out.println("│           MAIN MENU                  │");
            System.out.println("├──────────────────────────────────────┤");
            System.out.println("│  1) Take a Quiz                     │");
            System.out.println("│  2) View My Scores                  │");
            System.out.println("│  3) View Leaderboard                │");
            System.out.println("│  4) Add a Question                  │");
            System.out.println("│  5) View All Questions               │");
            System.out.println("│  6) Switch User                     │");
            System.out.println("│  7) Exit                            │");
            System.out.println("└──────────────────────────────────────┘");
            System.out.print("  Choose an option (1-7): ");

            String choice = scanner.nextLine().trim();
            System.out.println();

            switch (choice) {
                case "1": takeQuiz(); break;
                case "2": viewMyScores(); break;
                case "3": viewLeaderboard(); break;
                case "4": addQuestion(); break;
                case "5": viewAllQuestions(); break;
                case "6": login(); break;
                case "7":
                    System.out.println("  Thanks for playing, " + currentUser.getUsername() + "! Goodbye!");
                    return;
                default:
                    System.out.println("  Invalid option. Please try again.\n");
            }
        }
    }

    // ── Take Quiz ───────────────────────────────────────────────

    private void takeQuiz() {
        if (quizService.getQuestionCount() == 0) {
            System.out.println("  No questions available. Add some first!\n");
            return;
        }

        // Category selection
        Category selectedCat = selectCategory();

        // Question count
        System.out.print("  How many questions? (0 = all): ");
        int maxQ = readInt(0);

        // Create quiz
        Quiz quiz = quizService.createQuiz("Quiz", selectedCat, maxQ);
        if (quiz.getQuestionCount() == 0) {
            System.out.println("  No questions found for this category.\n");
            return;
        }

        System.out.println("\n  ═══ " + quiz.getTitle() + " ═══");
        System.out.println("  " + quiz.getQuestionCount() + " questions. Good luck!\n");

        // Run quiz
        List<Question> questions = quiz.getShuffledQuestions();
        int correct = 0;
        long totalStartTime = System.currentTimeMillis();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("  ─── Question " + (i + 1) + " of " + questions.size() + " ───");
            System.out.println("  [" + q.getCategory().getDisplayName() + "] " + q.getText());
            System.out.println(q.getChoicesDisplay());

            if (q.getTimeLimitSeconds() > 0) {
                System.out.println("  ⏱ Time limit: " + q.getTimeLimitSeconds() + " seconds");
            }

            // Start timer
            final boolean[] timedOut = {false};
            timerService.start(q.getTimeLimitSeconds(), () -> {
                timedOut[0] = true;
                System.out.println("\n  ⏰ TIME'S UP!");
            });

            // Get answer
            System.out.print("  Your answer: ");
            String answer = scanner.nextLine().trim();
            timerService.stop();

            if (timedOut[0]) {
                System.out.println("  ❌ Too slow! The answer was: " + q.getCorrectAnswerDisplay());
            } else if (q.isCorrect(answer)) {
                correct++;
                System.out.println("  ✅ Correct!");
            } else {
                System.out.println("  ❌ Wrong! The answer was: " + q.getCorrectAnswerDisplay());
            }
            System.out.println();
        }

        long totalTime = System.currentTimeMillis() - totalStartTime;

        // Record and display score
        Score score = new Score(currentUser, questions.size(), correct, totalTime);
        scoreService.recordScore(score);

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║              QUIZ COMPLETE!                     ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║  Player:   %-36s ║%n", currentUser.getUsername());
        System.out.printf("║  Score:    %d / %d (%.1f%%)%s║%n",
            correct, questions.size(), score.getPercentage(),
            " ".repeat(Math.max(1, 28 - String.format("%d / %d (%.1f%%)", correct, questions.size(), score.getPercentage()).length())));
        System.out.printf("║  Grade:    %-36s ║%n", score.getGrade());
        System.out.printf("║  Time:     %-36s ║%n", score.getFormattedTime());
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
    }

    private Category selectCategory() {
        List<Category> cats = quizService.getAvailableCategories();
        System.out.println("  Select a category:");
        System.out.println("  0) All Categories");
        for (int i = 0; i < cats.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + cats.get(i).getDisplayName());
        }
        System.out.print("  Choice: ");
        int choice = readInt(0);
        if (choice >= 1 && choice <= cats.size()) {
            return cats.get(choice - 1);
        }
        return null; // all categories
    }

    // ── View Scores ─────────────────────────────────────────────

    private void viewMyScores() {
        List<Score> scores = scoreService.getScoresForUser(currentUser);
        if (scores.isEmpty()) {
            System.out.println("  No scores yet. Take a quiz first!\n");
            return;
        }
        System.out.println("  ═══ Scores for " + currentUser.getUsername() + " ═══");
        for (int i = 0; i < scores.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + scores.get(i));
        }
        System.out.println();
    }

    private void viewLeaderboard() {
        Map<User, Score> board = scoreService.getLeaderboard();
        if (board.isEmpty()) {
            System.out.println("  Leaderboard is empty. Be the first to play!\n");
            return;
        }
        System.out.println("  ═══ LEADERBOARD ═══");
        System.out.printf("  %-5s %-20s %-10s %-8s%n", "Rank", "Player", "Score", "Grade");
        System.out.println("  " + "─".repeat(45));
        int rank = 1;
        for (Map.Entry<User, Score> entry : board.entrySet()) {
            Score s = entry.getValue();
            System.out.printf("  %-5d %-20s %d/%-7d %-8s%n",
                rank++, entry.getKey().getUsername(),
                s.getCorrectAnswers(), s.getTotalQuestions(), s.getGrade());
        }
        System.out.println();
    }

    // ── Add Question ────────────────────────────────────────────

    private void addQuestion() {
        System.out.println("  ═══ Add a New Question ═══");
        System.out.println("  1) Multiple Choice");
        System.out.println("  2) True/False");
        System.out.print("  Type: ");
        String type = scanner.nextLine().trim();

        int id = quizService.getQuestionCount() + 100; // simple ID generation

        System.out.print("  Question text: ");
        String text = scanner.nextLine().trim();

        // Category
        System.out.print("  Category (SCIENCE/HISTORY/MATH/PROGRAMMING/GENERAL): ");
        Category cat = Category.fromString(scanner.nextLine().trim());

        System.out.print("  Time limit (seconds, 0=unlimited): ");
        int time = readInt(15);

        if ("1".equals(type)) {
            List<String> choices = new ArrayList<>();
            System.out.println("  Enter choices (empty line to finish, min 2):");
            for (int i = 0; i < 6; i++) {
                char letter = (char) ('A' + i);
                System.out.print("  " + letter + ") ");
                String choice = scanner.nextLine().trim();
                if (choice.isEmpty() && choices.size() >= 2) break;
                if (!choice.isEmpty()) choices.add(choice);
            }
            System.out.print("  Correct answer (A/B/C/...): ");
            String correct = scanner.nextLine().trim().toUpperCase();
            int idx = correct.charAt(0) - 'A';
            quizService.addQuestion(new MCQuestion(id, text, choices, idx, cat, time));
        } else {
            System.out.print("  Correct answer (true/false): ");
            boolean answer = scanner.nextLine().trim().equalsIgnoreCase("true");
            quizService.addQuestion(new TrueFalseQuestion(id, text, answer, cat, time));
        }

        System.out.println("  Question added successfully!\n");
    }

    // ── View Questions ──────────────────────────────────────────

    private void viewAllQuestions() {
        List<Question> questions = quizService.loadQuestions();
        if (questions.isEmpty()) {
            System.out.println("  No questions in the bank.\n");
            return;
        }
        System.out.println("  ═══ Question Bank (" + questions.size() + " questions) ═══\n");
        for (Question q : questions) {
            System.out.println("  " + q.toString().replace("\n", "\n  "));
            System.out.println("  Answer: " + q.getCorrectAnswerDisplay());
            System.out.println();
        }
    }

    // ── Utility ─────────────────────────────────────────────────

    private int readInt(int defaultVal) {
        try {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) return defaultVal;
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
