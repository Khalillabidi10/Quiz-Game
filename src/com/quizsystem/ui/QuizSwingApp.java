package com.quizsystem.ui;

import com.quizsystem.model.*;
import com.quizsystem.service.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * QuizSwingApp — Modern dark-themed Swing GUI for the quiz system.
 */
public class QuizSwingApp extends JFrame {

    // ── Colors (Modern Dark Theme) ──────────────────────────────
    private static final Color BG_DARK      = new Color(0x1a, 0x1a, 0x2e);
    private static final Color BG_CARD      = new Color(0x16, 0x21, 0x3e);
    private static final Color ACCENT       = new Color(0x0f, 0x34, 0x60);
    private static final Color HIGHLIGHT    = new Color(0xe9, 0x45, 0x60);
    private static final Color SUCCESS      = new Color(0x00, 0xb8, 0x94);
    private static final Color WARNING      = new Color(0xfd, 0xcb, 0x6e);
    private static final Color TEXT_PRIMARY = new Color(0xf0, 0xf0, 0xf0);
    private static final Color TEXT_MUTED   = new Color(0x99, 0x99, 0xbb);

    // ── Fonts ───────────────────────────────────────────────────
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BTN    = new Font("Segoe UI", Font.BOLD, 15);

    // ── Services ────────────────────────────────────────────────
    private final QuizService<Question> quizService;
    private final ScoreService scoreService;

    // ── State ───────────────────────────────────────────────────
    private User currentUser;
    private List<Question> currentQuestions;
    private int currentQuestionIndex;
    private int correctCount;
    private long quizStartTime;
    private javax.swing.Timer countdownTimer;

    // ── UI Panels ───────────────────────────────────────────────
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    public QuizSwingApp(QuizService<Question> quizService, ScoreService scoreService) {
        this.quizService = quizService;
        this.scoreService = scoreService;
        initFrame();
        buildWelcomePanel();
        buildQuizPanel();
        buildResultsPanel();
        cardLayout.show(mainPanel, "welcome");
    }

    private void initFrame() {
        setTitle("Interactive Quiz System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);
        setResizable(false);
        mainPanel.setBackground(BG_DARK);
        setContentPane(mainPanel);
    }

    // ═════════════════════════════════════════════════════════════
    //  WELCOME PANEL
    // ═════════════════════════════════════════════════════════════

    private JTextField usernameField;
    private JComboBox<String> categoryCombo;
    private JSpinner questionCountSpinner;

    private void buildWelcomePanel() {
        JPanel panel = createGradientPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = styledLabel("Interactive Quiz System", FONT_TITLE, HIGHLIGHT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title, gbc);

        // Subtitle
        gbc.gridy = 1;
        JLabel subtitle = styledLabel("Test your knowledge across multiple categories!", FONT_SMALL, TEXT_MUTED);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(subtitle, gbc);

        // Spacer
        gbc.gridy = 2;
        panel.add(Box.createVerticalStrut(15), gbc);

        // Username
        gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(styledLabel("Username:", FONT_BODY, TEXT_PRIMARY), gbc);
        gbc.gridx = 1;
        usernameField = styledTextField("Player");
        panel.add(usernameField, gbc);

        // Category
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(styledLabel("Category:", FONT_BODY, TEXT_PRIMARY), gbc);
        gbc.gridx = 1;
        List<String> catNames = new ArrayList<>();
        catNames.add("All Categories");
        for (Category c : Category.values()) catNames.add(c.getDisplayName());
        categoryCombo = styledComboBox(catNames.toArray(new String[0]));
        panel.add(categoryCombo, gbc);

        // Question count
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(styledLabel("Questions:", FONT_BODY, TEXT_PRIMARY), gbc);
        gbc.gridx = 1;
        questionCountSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        questionCountSpinner.setFont(FONT_BODY);
        panel.add(questionCountSpinner, gbc);

        // Start Button
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 60, 8, 60);
        JButton startBtn = styledButton("START QUIZ", HIGHLIGHT);
        startBtn.addActionListener(e -> startQuiz());
        panel.add(startBtn, gbc);

        mainPanel.add(panel, "welcome");
    }

    private void startQuiz() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) username = "Player";
        currentUser = new User(1, username);

        int catIdx = categoryCombo.getSelectedIndex();
        Category cat = catIdx == 0 ? null : Category.values()[catIdx - 1];
        int maxQ = (int) questionCountSpinner.getValue();

        Quiz quiz = quizService.createQuiz("Quiz", cat, maxQ);
        if (quiz.getQuestionCount() == 0) {
            JOptionPane.showMessageDialog(this, "No questions available for this category!", 
                "Oops", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentQuestions = quiz.getShuffledQuestions();
        currentQuestionIndex = 0;
        correctCount = 0;
        quizStartTime = System.currentTimeMillis();
        showQuestion();
        cardLayout.show(mainPanel, "quiz");
    }

    // ═════════════════════════════════════════════════════════════
    //  QUIZ PANEL
    // ═════════════════════════════════════════════════════════════

    private JLabel questionNumberLabel;
    private JLabel questionTextLabel;
    private JLabel timerLabel;
    private JProgressBar timerBar;
    private JPanel choicesPanel;
    private ButtonGroup choiceGroup;
    private JButton submitBtn;
    private JLabel feedbackLabel;

    private void buildQuizPanel() {
        JPanel panel = createGradientPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Top bar: question number + timer
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        questionNumberLabel = styledLabel("Question 1 of 10", FONT_SMALL, TEXT_MUTED);
        timerLabel = styledLabel("⏱ 15s", FONT_BODY, WARNING);
        topBar.add(questionNumberLabel, BorderLayout.WEST);
        topBar.add(timerLabel, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Center: question + choices
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Timer bar
        timerBar = new JProgressBar(0, 100);
        timerBar.setValue(100);
        timerBar.setPreferredSize(new Dimension(600, 6));
        timerBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        timerBar.setForeground(SUCCESS);
        timerBar.setBackground(BG_CARD);
        timerBar.setBorderPainted(false);
        centerPanel.add(timerBar);
        centerPanel.add(Box.createVerticalStrut(15));

        // Question text
        questionTextLabel = styledLabel("Question text here", FONT_HEADER, TEXT_PRIMARY);
        questionTextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(questionTextLabel);
        centerPanel.add(Box.createVerticalStrut(20));

        // Choices
        choicesPanel = new JPanel();
        choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.Y_AXIS));
        choicesPanel.setOpaque(false);
        choicesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(choicesPanel);

        // Feedback
        centerPanel.add(Box.createVerticalStrut(10));
        feedbackLabel = styledLabel(" ", FONT_BODY, TEXT_MUTED);
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(feedbackLabel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Bottom: submit button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        submitBtn = styledButton("SUBMIT ANSWER", ACCENT);
        submitBtn.addActionListener(e -> submitAnswer());
        bottomPanel.add(submitBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "quiz");
    }

    private void showQuestion() {
        Question q = currentQuestions.get(currentQuestionIndex);
        questionNumberLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + currentQuestions.size()
            + "  |  " + q.getCategory().getDisplayName());
        questionTextLabel.setText("<html><body style='width:500px'>" + q.getText() + "</body></html>");
        feedbackLabel.setText(" ");
        submitBtn.setText("SUBMIT ANSWER");
        submitBtn.setEnabled(true);

        // Build choices
        choicesPanel.removeAll();
        choiceGroup = new ButtonGroup();

        if (q instanceof MCQuestion) {
            MCQuestion mcq = (MCQuestion) q;
            List<String> choices = mcq.getChoices();
            for (int i = 0; i < choices.size(); i++) {
                char letter = (char) ('A' + i);
                JRadioButton rb = styledRadioButton(letter + ")  " + choices.get(i));
                rb.setActionCommand(String.valueOf(letter));
                choiceGroup.add(rb);
                choicesPanel.add(rb);
                choicesPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JRadioButton rbTrue = styledRadioButton("True");
            rbTrue.setActionCommand("true");
            JRadioButton rbFalse = styledRadioButton("False");
            rbFalse.setActionCommand("false");
            choiceGroup.add(rbTrue);
            choiceGroup.add(rbFalse);
            choicesPanel.add(rbTrue);
            choicesPanel.add(Box.createVerticalStrut(5));
            choicesPanel.add(rbFalse);
        }

        choicesPanel.revalidate();
        choicesPanel.repaint();

        // Start timer
        startCountdown(q.getTimeLimitSeconds());
    }

    private void startCountdown(int seconds) {
        if (countdownTimer != null) countdownTimer.stop();
        if (seconds <= 0) {
            timerLabel.setText("⏱ No limit");
            timerBar.setValue(100);
            return;
        }

        final long deadline = System.currentTimeMillis() + seconds * 1000L;
        timerBar.setValue(100);
        timerBar.setForeground(SUCCESS);

        countdownTimer = new javax.swing.Timer(100, e -> {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                timerLabel.setText("⏱ TIME'S UP!");
                timerBar.setValue(0);
                feedbackLabel.setText("⏰ Time expired!");
                feedbackLabel.setForeground(HIGHLIGHT);
                submitBtn.setEnabled(false);
                // Auto-advance after 1.5s
                javax.swing.Timer delay = new javax.swing.Timer(1500, evt -> nextQuestion());
                delay.setRepeats(false);
                delay.start();
            } else {
                int secs = (int) (remaining / 1000) + 1;
                timerLabel.setText("⏱ " + secs + "s");
                int pct = (int) (remaining * 100 / (seconds * 1000L));
                timerBar.setValue(pct);
                if (pct < 30) timerBar.setForeground(HIGHLIGHT);
                else if (pct < 60) timerBar.setForeground(WARNING);
            }
        });
        countdownTimer.start();
    }

    private void submitAnswer() {
        if (countdownTimer != null) countdownTimer.stop();

        ButtonModel selected = choiceGroup.getSelection();
        if (selected == null) {
            feedbackLabel.setText("Please select an answer!");
            feedbackLabel.setForeground(WARNING);
            return;
        }

        String answer = selected.getActionCommand();
        Question q = currentQuestions.get(currentQuestionIndex);

        if (q.isCorrect(answer)) {
            correctCount++;
            feedbackLabel.setText("✅ Correct!");
            feedbackLabel.setForeground(SUCCESS);
        } else {
            feedbackLabel.setText("❌ Wrong! Answer: " + q.getCorrectAnswerDisplay());
            feedbackLabel.setForeground(HIGHLIGHT);
        }

        submitBtn.setText("NEXT →");
        submitBtn.removeActionListener(submitBtn.getActionListeners()[0]);
        submitBtn.addActionListener(e -> nextQuestion());
    }

    private void nextQuestion() {
        // Remove all listeners and re-add submitAnswer
        for (ActionListener al : submitBtn.getActionListeners()) {
            submitBtn.removeActionListener(al);
        }
        submitBtn.addActionListener(e -> submitAnswer());

        currentQuestionIndex++;
        if (currentQuestionIndex < currentQuestions.size()) {
            showQuestion();
        } else {
            showResults();
        }
    }

    // ═════════════════════════════════════════════════════════════
    //  RESULTS PANEL
    // ═════════════════════════════════════════════════════════════

    private JLabel resultScoreLabel;
    private JLabel resultGradeLabel;
    private JLabel resultTimeLabel;
    private JLabel resultPercentLabel;

    private void buildResultsPanel() {
        JPanel panel = createGradientPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel title = styledLabel("Quiz Complete!", FONT_TITLE, SUCCESS);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title, gbc);

        gbc.gridy = 1;
        panel.add(Box.createVerticalStrut(10), gbc);

        // Score
        gbc.gridy = 2;
        resultScoreLabel = styledLabel("Score: 0/0", FONT_HEADER, TEXT_PRIMARY);
        resultScoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(resultScoreLabel, gbc);

        // Percentage
        gbc.gridy = 3;
        resultPercentLabel = styledLabel("0%", FONT_TITLE, HIGHLIGHT);
        resultPercentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(resultPercentLabel, gbc);

        // Grade
        gbc.gridy = 4;
        resultGradeLabel = styledLabel("Grade: F", FONT_HEADER, WARNING);
        resultGradeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(resultGradeLabel, gbc);

        // Time
        gbc.gridy = 5;
        resultTimeLabel = styledLabel("Time: 0s", FONT_BODY, TEXT_MUTED);
        resultTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(resultTimeLabel, gbc);

        gbc.gridy = 6;
        panel.add(Box.createVerticalStrut(15), gbc);

        // Buttons
        gbc.gridy = 7; gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 20, 8, 5);
        JButton retryBtn = styledButton("TRY AGAIN", ACCENT);
        retryBtn.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));
        panel.add(retryBtn, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(8, 5, 8, 20);
        JButton exitBtn = styledButton("EXIT", HIGHLIGHT);
        exitBtn.addActionListener(e -> dispose());
        panel.add(exitBtn, gbc);

        mainPanel.add(panel, "results");
    }

    private void showResults() {
        if (countdownTimer != null) countdownTimer.stop();
        long totalTime = System.currentTimeMillis() - quizStartTime;
        Score score = new Score(currentUser, currentQuestions.size(), correctCount, totalTime);
        scoreService.recordScore(score);

        resultScoreLabel.setText("Score: " + correctCount + " / " + currentQuestions.size());
        resultPercentLabel.setText(String.format("%.0f%%", score.getPercentage()));
        resultGradeLabel.setText("Grade: " + score.getGrade());
        resultTimeLabel.setText("Time: " + score.getFormattedTime());

        // Color based on grade
        double pct = score.getPercentage();
        resultPercentLabel.setForeground(pct >= 70 ? SUCCESS : pct >= 50 ? WARNING : HIGHLIGHT);

        cardLayout.show(mainPanel, "results");
    }

    // ═════════════════════════════════════════════════════════════
    //  STYLED COMPONENTS
    // ═════════════════════════════════════════════════════════════

    private JPanel createGradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, 0, getHeight(), BG_CARD);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    private JLabel styledLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField(placeholder, 15);
        field.setFont(FONT_BODY);
        field.setBackground(BG_CARD);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return field;
    }

    private JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(BG_CARD);
        combo.setForeground(TEXT_PRIMARY);
        return combo;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JRadioButton styledRadioButton(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(FONT_BODY);
        rb.setForeground(TEXT_PRIMARY);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return rb;
    }

    /** Launches the GUI. */
    public static void launch(QuizService<Question> quizService, ScoreService scoreService) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new QuizSwingApp(quizService, scoreService).setVisible(true);
        });
    }
}
