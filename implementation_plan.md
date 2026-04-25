# 🎯 Interactive Quiz System — Implementation Plan

A complete Java project following clean layered architecture, OOP best practices, generics, file & database persistence, and a JavaFX GUI.

---

## 1. Architecture Overview

```mermaid
graph TB
    subgraph UI Layer
        A[ConsoleUI]
        B[QuizFxApp - JavaFX]
    end

    subgraph Service Layer
        C[QuizService ‹interface›]
        D[QuizServiceImpl]
        E[ScoreService ‹interface›]
        F[ScoreServiceImpl]
        G[TimerService]
    end

    subgraph Repository Layer
        H[QuestionRepository ‹interface›]
        I[TxtQuestionRepository]
        J[CsvQuestionRepository]
        K[JdbcQuestionRepository]
        L[UserRepository ‹interface›]
        M[JdbcUserRepository]
        N[ScoreRepository ‹interface›]
        O[JdbcScoreRepository]
    end

    subgraph Model Layer
        P["Question ‹abstract›"]
        Q[MCQuestion]
        R[TrueFalseQuestion]
        S[User]
        T[Score]
        U[Quiz]
        V["Category ‹enum›"]
    end

    A --> C
    A --> E
    B --> C
    B --> E
    C --> D
    E --> F
    D --> H
    D --> G
    F --> N
    F --> L
    H --> I
    H --> J
    H --> K
    N --> O
    L --> M
    D --> P
    P --> Q
    P --> R
```

---

## 2. Class Diagram

```mermaid
classDiagram
    class Question {
        <<abstract>>
        -int id
        -String text
        -Category category
        -int timeLimitSeconds
        +getId() int
        +getText() String
        +getCategory() Category
        +getTimeLimitSeconds() int
        +isCorrect(String answer) boolean*
        +getChoicesDisplay() String*
        +toFileString() String*
    }

    class MCQuestion {
        -List~String~ choices
        -int correctIndex
        +MCQuestion(int, String, List~String~, int, Category, int)
        +isCorrect(String answer) boolean
        +getChoicesDisplay() String
        +getCorrectAnswer() String
        +toFileString() String
    }

    class TrueFalseQuestion {
        -boolean correctAnswer
        +TrueFalseQuestion(int, String, boolean, Category, int)
        +isCorrect(String answer) boolean
        +getChoicesDisplay() String
        +toFileString() String
    }

    class User {
        -int id
        -String username
        +User(int, String)
        +getId() int
        +getUsername() String
        +equals(Object) boolean
        +hashCode() int
    }

    class Score {
        -User user
        -int totalQuestions
        -int correctAnswers
        -long timeTakenMs
        -LocalDateTime timestamp
        +getPercentage() double
        +toString() String
    }

    class Quiz {
        -List~Question~ questions
        -String title
        -Category category
        +Quiz(String, Category, List~Question~)
        +getShuffledQuestions() List~Question~
        +getQuestionCount() int
    }

    class Category {
        <<enumeration>>
        SCIENCE
        HISTORY
        MATH
        PROGRAMMING
        GENERAL
    }

    class QuizService~T extends Question~ {
        <<interface>>
        +loadQuestions() List~T~
        +startQuiz(User user) Score
        +getQuestionsByCategory(Category cat) List~T~
    }

    class ScoreService {
        <<interface>>
        +recordScore(Score score) void
        +getScoresForUser(User user) List~Score~
        +getLeaderboard() Map~User, Score~
    }

    class QuestionRepository~T extends Question~ {
        <<interface>>
        +findAll() List~T~
        +findById(int id) Optional~T~
        +save(T question) void
        +saveAll(List~T~ questions) void
        +findByCategory(Category cat) List~T~
    }

    Question <|-- MCQuestion
    Question <|-- TrueFalseQuestion
    Question --> Category
    Quiz --> Question
    Quiz --> Category
    Score --> User
    QuizService ..> Question
    QuizService ..> Score
    ScoreService ..> Score
    QuestionRepository ..> Question
```

---

## 3. Database Schema (ERD)

```mermaid
erDiagram
    USERS {
        int id PK
        varchar username UK
    }

    QUESTIONS {
        int id PK
        varchar type
        text question_text
        varchar category
        int time_limit_seconds
    }

    MCQ_CHOICES {
        int id PK
        int question_id FK
        varchar choice_text
        boolean is_correct
    }

    TF_ANSWERS {
        int id PK
        int question_id FK
        boolean correct_answer
    }

    SCORES {
        int id PK
        int user_id FK
        int total_questions
        int correct_answers
        bigint time_taken_ms
        timestamp created_at
    }

    USERS ||--o{ SCORES : "has"
    QUESTIONS ||--o{ MCQ_CHOICES : "has"
    QUESTIONS ||--o| TF_ANSWERS : "has"
```

---

## 4. Project Structure

```
d:\Quiz Game\
├── src\
│   └── com\
│       └── quizsystem\
│           ├── model\
│           │   ├── Question.java          (abstract base)
│           │   ├── MCQuestion.java         (multiple-choice)
│           │   ├── TrueFalseQuestion.java  (true/false)
│           │   ├── User.java
│           │   ├── Score.java
│           │   ├── Quiz.java
│           │   └── Category.java           (enum)
│           ├── service\
│           │   ├── QuizService.java        (generic interface)
│           │   ├── QuizServiceImpl.java
│           │   ├── ScoreService.java       (interface)
│           │   ├── ScoreServiceImpl.java
│           │   └── TimerService.java
│           ├── repository\
│           │   ├── QuestionRepository.java (generic interface)
│           │   ├── TxtQuestionRepository.java
│           │   ├── CsvQuestionRepository.java
│           │   ├── JdbcQuestionRepository.java
│           │   ├── UserRepository.java     (interface)
│           │   ├── JdbcUserRepository.java
│           │   ├── ScoreRepository.java    (interface)
│           │   └── JdbcScoreRepository.java
│           ├── ui\
│           │   ├── ConsoleUI.java          (CLI interface)
│           │   └── QuizFxApp.java          (JavaFX GUI)
│           ├── util\
│           │   └── DatabaseConnection.java (JDBC singleton)
│           └── Main.java                   (entry point)
├── data\
│   ├── questions.txt
│   └── questions.csv
├── sql\
│   └── schema.sql
├── lib\
│   └── (JDBC driver JAR if needed)
├── compile.bat
├── run.bat
└── README.md
```

---

## 5. Proposed Changes — File by File

### Model Layer

#### [NEW] Question.java
- Abstract class with fields: `id`, `text`, `category`, `timeLimitSeconds`
- Abstract methods: `isCorrect(String)`, `getChoicesDisplay()`, `toFileString()`
- Demonstrates **abstraction** and **encapsulation**

#### [NEW] MCQuestion.java
- Extends `Question` — stores `List<String> choices` and `int correctIndex`
- Overrides abstract methods — demonstrates **inheritance** and **polymorphism**

#### [NEW] TrueFalseQuestion.java
- Extends `Question` — stores `boolean correctAnswer`
- Alternative question type showing polymorphism in action

#### [NEW] User.java
- Simple POJO with `id` and `username`
- Overrides `equals()` and `hashCode()` for use as `Map` key

#### [NEW] Score.java
- Tracks `user`, `totalQuestions`, `correctAnswers`, `timeTakenMs`, `timestamp`
- `getPercentage()` utility method

#### [NEW] Quiz.java
- Aggregates a list of questions with a title and category
- `getShuffledQuestions()` — uses `Collections.shuffle()` for randomization

#### [NEW] Category.java
- Enum: `SCIENCE`, `HISTORY`, `MATH`, `PROGRAMMING`, `GENERAL`

---

### Repository Layer

#### [NEW] QuestionRepository.java
- Generic interface: `QuestionRepository<T extends Question>`
- Methods: `findAll()`, `findById()`, `save()`, `saveAll()`, `findByCategory()`

#### [NEW] TxtQuestionRepository.java
- Reads/writes questions to `data/questions.txt` using `BufferedReader`/`BufferedWriter`
- Custom delimited format: `TYPE|ID|TEXT|CATEGORY|TIME|CHOICES|CORRECT`

#### [NEW] CsvQuestionRepository.java
- Reads/writes questions to `data/questions.csv`
- Standard CSV with header row
- Handles escaping commas in question text

#### [NEW] JdbcQuestionRepository.java
- Full CRUD via JDBC `PreparedStatement`
- Uses `DatabaseConnection` singleton for connection management

#### [NEW] UserRepository.java / JdbcUserRepository.java
- Interface + JDBC implementation for user persistence

#### [NEW] ScoreRepository.java / JdbcScoreRepository.java
- Interface + JDBC implementation for score persistence

---

### Service Layer

#### [NEW] QuizService.java
- Generic interface: `QuizService<T extends Question>`
- Methods: `loadQuestions()`, `startQuiz(User)`, `getQuestionsByCategory(Category)`

#### [NEW] QuizServiceImpl.java
- Core quiz logic: load questions, filter by category, run quiz, calculate score
- Delegates persistence to repository layer

#### [NEW] ScoreService.java / ScoreServiceImpl.java
- Score recording and leaderboard generation
- `getLeaderboard()` returns `Map<User, Score>`

#### [NEW] TimerService.java
- Per-question countdown timer using `ScheduledExecutorService`
- Callbacks for time-up events

---

### UI Layer

#### [NEW] ConsoleUI.java
- Full console-based quiz experience with `Scanner`
- Menu: Create Quiz, Take Quiz, View Scores, Manage Questions
- Colored console output where supported

#### [NEW] QuizFxApp.java
- JavaFX Application with scenes:
  - **Welcome** — username entry + category selection
  - **Quiz** — question display, choices (radio buttons), timer bar
  - **Results** — score summary, leaderboard table

---

### Utility

#### [NEW] DatabaseConnection.java
- Singleton pattern for JDBC connection
- Uses SQLite (zero-config, file-based — no server needed)
- Auto-creates tables on first run

---

### Data Files

#### [NEW] data/questions.txt
- 15+ sample questions across all categories in pipe-delimited format

#### [NEW] data/questions.csv
- Same questions in CSV format with header row

#### [NEW] sql/schema.sql
- DDL statements for all tables (SQLite syntax)

---

### Build & Run

#### [NEW] compile.bat
- Compiles all `.java` files with classpath setup

#### [NEW] run.bat
- Runs `Main` class with correct classpath

#### [NEW] README.md
- Setup instructions, architecture explanation, improvement suggestions

---

## 6. Key Design Decisions

| Decision | Rationale |
|---|---|
| **SQLite over MySQL/PostgreSQL** | Zero configuration — just a `.jar` file. Perfect for a learning project. |
| **Generic interfaces** | `QuestionRepository<T extends Question>` shows bounded type parameters in practice |
| **Repository pattern** | Cleanly separates data access from business logic. Easy to swap file ↔ DB storage |
| **Abstract `Question` class** | Enables polymorphism — `MCQuestion` and `TrueFalseQuestion` share a common API |
| **Enum for Category** | Type-safe, prevents invalid categories, easy to extend |
| **Singleton DB connection** | Simple resource management; appropriate for this project's scope |
| **Console + JavaFX** | Console-first for easy testing; JavaFX as a bonus polished UI |

---

## 7. Open Questions

> [!IMPORTANT]
> **JavaFX Availability**: JavaFX was removed from the JDK starting with Java 11. Do you have JavaFX SDK installed, or would you prefer I use **Swing** instead? (The console UI works regardless.)

> [!NOTE]
> **Database**: I'll use **SQLite** (via `sqlite-jdbc` JAR) so there's no need to install a database server. The database file will live at `data/quiz.db`. Is that acceptable?

> [!NOTE]
> **Build System**: I'll provide simple `compile.bat` and `run.bat` scripts. Would you prefer a **Maven** or **Gradle** project instead?

---

## 8. Verification Plan

### Automated Tests
- Compile all source files and verify zero errors
- Run the console UI end-to-end: create user → take quiz → view scores
- Verify `.txt` and `.csv` file read/write (data survives restart)
- Verify SQLite DB creation and CRUD operations

### Manual Verification
- Demonstrate full console session with sample output
- Launch JavaFX GUI (if available) and walk through quiz flow
- Verify question randomization produces different ordering
- Verify timer functionality (question auto-skips on timeout)

---

## 9. Suggested Improvements (Post-MVP)

1. **Difficulty levels** — Easy/Medium/Hard with weighted scoring
2. **Multi-player mode** — Competitive quiz sessions
3. **REST API** — Spring Boot backend for web-based quizzes
4. **Question import** — Load questions from JSON/XML
5. **Analytics** — Track which categories a user struggles with
6. **Sound effects** — Audio feedback for correct/incorrect answers in JavaFX
7. **Internationalization (i18n)** — Support multiple languages
