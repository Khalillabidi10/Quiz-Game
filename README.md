# 🎯 Interactive Quiz System

A complete Java quiz application with clean layered architecture, file & database persistence, console and GUI interfaces, timers, and category-based question management.

---

## 📦 Project Structure

```
Quiz Game/
├── src/com/quizsystem/
│   ├── model/           ← Data classes (Question, User, Score, etc.)
│   ├── service/         ← Business logic (QuizService, ScoreService, TimerService)
│   ├── repository/      ← Data access (TXT, CSV, JDBC implementations)
│   ├── ui/              ← User interfaces (Console, Swing GUI)
│   ├── util/            ← Utilities (DatabaseConnection)
│   └── Main.java        ← Entry point
├── data/
│   ├── questions.txt    ← Question bank (pipe-delimited)
│   └── questions.csv    ← Question bank (CSV format)
├── sql/
│   └── schema.sql       ← Database schema reference
├── compile.bat          ← Compile script
├── run.bat              ← Run script
└── README.md            ← This file
```

---

## 🚀 How to Run

### Prerequisites
- **Java JDK 8+** installed and `javac`/`java` on your PATH

### Compile
```batch
compile.bat
```

### Run (Console Mode)
```batch
run.bat
```

### Run (GUI Mode)
```batch
run.bat --gui
```

### Run with CSV storage
```batch
run.bat --csv
```

### Run with SQLite database
1. Download [sqlite-jdbc JAR](https://github.com/xerial/sqlite-jdbc/releases)
2. Place it in `lib/sqlite-jdbc.jar`
3. Run:
```batch
run.bat --db
```

---

## 🏗️ Architecture

```
┌─────────────┐     ┌─────────────┐
│  ConsoleUI  │     │ SwingGUI    │       ← UI Layer
└──────┬──────┘     └──────┬──────┘
       │                   │
       ▼                   ▼
┌──────────────────────────────────┐
│  QuizService  │  ScoreService   │       ← Service Layer
└──────┬────────┴────────┬────────┘
       │                 │
       ▼                 ▼
┌──────────────────────────────────┐
│  TxtRepo │ CsvRepo │ JdbcRepo  │       ← Repository Layer
└──────────┴──────────┴───────────┘
       │
       ▼
┌──────────────────────────────────┐
│  Question │ User │ Score │ Quiz │       ← Model Layer
└──────────────────────────────────┘
```

---

## 🧱 OOP Concepts Demonstrated

| Concept | Where |
|---|---|
| **Abstraction** | `Question` is abstract — cannot be instantiated directly |
| **Encapsulation** | All fields are `private`, accessed via getters |
| **Inheritance** | `MCQuestion` and `TrueFalseQuestion` extend `Question` |
| **Polymorphism** | `isCorrect()` behaves differently for MCQ vs T/F |
| **Interfaces** | `QuizService`, `QuestionRepository`, `ScoreService` |
| **Generics** | `QuestionRepository<T extends Question>` |
| **Enums** | `Category` with display names and safe parsing |

---

## ✨ Features

- ✅ Multiple-choice and True/False questions
- ✅ 5 categories: Science, History, Math, Programming, General
- ✅ Per-question countdown timer
- ✅ Question randomization
- ✅ Score tracking with grades (A+ through F)
- ✅ Leaderboard
- ✅ File persistence (.txt and .csv)
- ✅ Optional SQLite database
- ✅ Console and GUI interfaces
- ✅ Add questions at runtime

---

## 💡 Suggested Improvements

1. **Difficulty levels** — Easy/Medium/Hard with weighted scoring
2. **Multiplayer mode** — Competitive quiz sessions
3. **REST API** — Spring Boot backend for web quizzes
4. **JSON/XML import** — More question formats
5. **Analytics** — Track weak categories per user
6. **Sound effects** — Audio feedback in GUI
7. **i18n** — Multi-language support
8. **Unit tests** — JUnit test suite for services and repositories
