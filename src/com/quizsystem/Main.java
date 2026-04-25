package com.quizsystem;

import com.quizsystem.model.*;
import com.quizsystem.repository.*;
import com.quizsystem.service.*;
import com.quizsystem.ui.*;

/**
 * ============================================================
 * Main — Entry point for the Interactive Quiz System.
 * ============================================================
 * 
 * ARCHITECTURE FLOW:
 *   Main creates → Repository → Service → UI
 *   Each layer only depends on the layer below it.
 * 
 * STORAGE MODES:
 *   1. File-based (default) — uses questions.txt
 *   2. CSV-based — uses questions.csv
 *   3. Database — uses SQLite (if driver is available)
 * 
 * LAUNCH MODES:
 *   --gui    → Launch the Swing GUI
 *   --csv    → Use CSV file instead of TXT
 *   (default) → Console mode with TXT file
 */
public class Main {

    public static void main(String[] args) {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════╗");
        System.out.println("  ║   Interactive Quiz System v1.0      ║");
        System.out.println("  ╠══════════════════════════════════════╣");

        // Parse command-line arguments
        boolean useGui = hasArg(args, "--gui");
        boolean useCsv = hasArg(args, "--csv");
        boolean useDb  = hasArg(args, "--db");

        // ── 1. Create Repository ────────────────────────────────
        QuestionRepository<Question> questionRepo;

        if (useDb && com.quizsystem.util.DatabaseConnection.isAvailable()) {
            System.out.println("  ║   Storage: SQLite Database           ║");
            com.quizsystem.util.DatabaseConnection dbConn = 
                com.quizsystem.util.DatabaseConnection.getInstance("data/quiz.db");
            questionRepo = new JdbcQuestionRepository(dbConn);
        } else if (useCsv) {
            System.out.println("  ║   Storage: CSV File                  ║");
            questionRepo = new CsvQuestionRepository("data/questions.csv");
        } else {
            System.out.println("  ║   Storage: TXT File                  ║");
            questionRepo = new TxtQuestionRepository("data/questions.txt");
        }

        // ── 2. Create Services ──────────────────────────────────
        QuizService<Question> quizService = new QuizServiceImpl(questionRepo);
        ScoreService scoreService = new ScoreServiceImpl();

        System.out.println("  ║   Questions loaded: " + 
            String.format("%-17d", quizService.getQuestionCount()) + "║");

        // ── 3. Launch UI ────────────────────────────────────────
        if (useGui) {
            System.out.println("  ║   Mode: Graphical (Swing)            ║");
            System.out.println("  ╚══════════════════════════════════════╝");
            QuizSwingApp.launch(quizService, scoreService);
        } else {
            System.out.println("  ║   Mode: Console                      ║");
            System.out.println("  ╚══════════════════════════════════════╝");
            ConsoleUI consoleUI = new ConsoleUI(quizService, scoreService);
            consoleUI.start();
        }
    }

    private static boolean hasArg(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(flag)) return true;
        }
        return false;
    }
}
