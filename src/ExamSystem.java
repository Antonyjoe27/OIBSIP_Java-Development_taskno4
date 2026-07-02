import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * TASK 4 — Online Examination System
 * Stack  : Java Swing, CardLayout, javax.swing.Timer
 * No DB needed; credentials and questions are hard-coded (easy to extend).
 */
public class ExamSystem extends JFrame {

    // ── Colours ───────────────────────────────────────────────────────────────
    static final Color BG      = new Color(24, 24, 37);
    static final Color CARD_BG = new Color(36, 36, 54);
    static final Color ACCENT  = new Color(137, 180, 250);
    static final Color GREEN   = new Color(166, 227, 161);
    static final Color RED     = new Color(243, 139, 168);
    static final Color YELLOW  = new Color(249, 226, 175);
    static final Color TEXT    = new Color(205, 214, 244);
    static final Color MUTED   = new Color(127, 132, 156);

    // ── Credentials (extend as needed) ───────────────────────────────────────
    static final Map<String, String> USERS = Map.of("alice", "alice123", "bob", "bob456");

    // ── Question bank ─────────────────────────────────────────────────────────
    static final String[][] QUESTIONS = {
        {"Which keyword is used to create a class in Java?",
            "class", "struct", "define", "object", "A"},
        {"What is the default value of an int in Java?",
            "null", "0", "-1", "undefined", "B"},
        {"Which collection maintains insertion order?",
            "HashSet", "TreeSet", "LinkedList", "PriorityQueue", "C"},
        {"Which access modifier is visible everywhere?",
            "private", "protected", "default", "public", "D"},
        {"What does JVM stand for?",
            "Java Virtual Machine", "Java Verified Method",
            "Just Var Memory", "Java Visual Manager", "A"},
        {"Which method is the entry point of a Java program?",
            "start()", "run()", "main()", "init()", "C"},
        {"Which of these is not a primitive type?",
            "int", "char", "String", "boolean", "C"},
        {"What is the result of 10 % 3?",
            "3", "0", "1", "2", "C"},
        {"Which loop always executes at least once?",
            "for", "while", "do-while", "enhanced for", "C"},
        {"What does 'final' keyword prevent?",
            "Compilation", "Overriding/modification",
            "Instantiation", "Garbage collection", "B"},
    };

    static final int EXAM_SECONDS = 10 * 60; // 10 minutes

    // ── State ─────────────────────────────────────────────────────────────────
    private String loggedInUser;
    private String displayName;
    private int[]  answers;      // -1 = unanswered, 0-3 = option index
    private int    currentQ;
    private int    timeLeft;
    private javax.swing.Timer countdownTimer;
    private long   examStartMs;

    // ── Card layout ───────────────────────────────────────────────────────────
    private CardLayout cards;
    private JPanel     cardPanel;

    // ── Login widgets ─────────────────────────────────────────────────────────
    private JTextField     tfLoginUser;
    private JPasswordField pfLoginPass;

    // ── Profile widgets ───────────────────────────────────────────────────────
    private JTextField     tfDisplayName;
    private JPasswordField pfNewPass;

    // ── Exam widgets ──────────────────────────────────────────────────────────
    private JLabel         lblQNum, lblQText, lblTimer, lblExamUser;
    private JRadioButton[] radios = new JRadioButton[4];
    private ButtonGroup    btnGroup;
    private JButton        btnPrev, btnNext, btnSubmit;
    private JPanel         questionPanel;

    // ── Result widgets ────────────────────────────────────────────────────────
    private JLabel  lblResultScore, lblResultTime;
    private JTextArea taResultBreakdown;

    // ═════════════════════════════════════════════════════════════════════════
    public ExamSystem() {
        super("📝 Online Exam System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(640, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (countdownTimer != null && countdownTimer.isRunning()) {
                    int r = JOptionPane.showConfirmDialog(ExamSystem.this,
                        "Are you sure you want to quit the exam?",
                        "Quit?", JOptionPane.YES_NO_OPTION);
                    if (r == JOptionPane.YES_OPTION) System.exit(0);
                } else System.exit(0);
            }
        });

        cards     = new CardLayout();
        cardPanel = new JPanel(cards);
        cardPanel.setBackground(BG);
        cardPanel.add(buildLogin(),   "LOGIN");
        cardPanel.add(buildProfile(), "PROFILE");
        cardPanel.add(buildExam(),    "EXAM");
        cardPanel.add(buildResult(),  "RESULT");
        add(cardPanel);
        cards.show(cardPanel, "LOGIN");
    }

    // ══════════════════════════════ LOGIN ════════════════════════════════════
    private JPanel buildLogin() {
        JPanel p = card(new GridBagLayout());
        GridBagConstraints g = gbc();
        g.gridy = 0; p.add(lbl("📝 Online Examination Portal", 20, Font.BOLD, ACCENT), g);
        g.gridy = 1; p.add(lbl("Sign in to take the exam", 13, Font.ITALIC, MUTED), g);
        g.gridy = 2; p.add(Box.createVerticalStrut(20), g);

        tfLoginUser = field(18); pfLoginPass = new JPasswordField(18); styleField(pfLoginPass);
        g.gridy = 3; p.add(row("Username:", tfLoginUser), g);
        g.gridy = 4; p.add(row("Password:", pfLoginPass), g);
        g.gridy = 5; p.add(Box.createVerticalStrut(10), g);

        JButton btn = btn("Login →", ACCENT);
        g.gridy = 6; p.add(btn, g);
        btn.addActionListener(e -> doLogin());
        pfLoginPass.addActionListener(e -> doLogin());

        JLabel hint = lbl("Demo: alice / alice123  or  bob / bob456", 11, Font.ITALIC, MUTED);
        g.gridy = 7; p.add(hint, g);
        return p;
    }

    private void doLogin() {
        String u = tfLoginUser.getText().trim();
        String pw = new String(pfLoginPass.getPassword());
        if (USERS.getOrDefault(u, "").equals(pw)) {
            loggedInUser = u;
            displayName  = u;
            tfDisplayName.setText(u);
            pfNewPass.setText("");
            pfLoginPass.setText("");
            cards.show(cardPanel, "PROFILE");
        } else JOptionPane.showMessageDialog(this, "❌ Invalid credentials.");
    }

    // ══════════════════════════════ PROFILE ══════════════════════════════════
    private JPanel buildProfile() {
        JPanel p = card(new GridBagLayout());
        GridBagConstraints g = gbc();
        g.gridy = 0; p.add(lbl("👤 Profile Setup", 18, Font.BOLD, ACCENT), g);
        g.gridy = 1; p.add(lbl("Update your name/password before starting", 13, Font.PLAIN, MUTED), g);
        g.gridy = 2; p.add(Box.createVerticalStrut(14), g);

        tfDisplayName = field(18); pfNewPass = new JPasswordField(18); styleField(pfNewPass);
        g.gridy = 3; p.add(row("Display Name:", tfDisplayName), g);
        g.gridy = 4; p.add(row("New Password:", pfNewPass), g);
        g.gridy = 5; p.add(lbl("(leave blank to keep current)", 11, Font.ITALIC, MUTED), g);
        g.gridy = 6; p.add(Box.createVerticalStrut(14), g);

        JButton btnStart = btn("Start Exam ▶", GREEN);
        g.gridy = 7; p.add(btnStart, g);
        btnStart.addActionListener(e -> startExam());
        return p;
    }

    private void startExam() {
        displayName = tfDisplayName.getText().trim();
        if (displayName.isEmpty()) displayName = loggedInUser;
        String np = new String(pfNewPass.getPassword());
        // In a real system you would update the DB here
        answers   = new int[QUESTIONS.length];
        Arrays.fill(answers, -1);
        currentQ  = 0;
        timeLeft  = EXAM_SECONDS;
        examStartMs = System.currentTimeMillis();

        loadQuestion();
        lblExamUser.setText("Candidate: " + displayName);
        cards.show(cardPanel, "EXAM");
        startTimer();
    }

    // ══════════════════════════════ EXAM ═════════════════════════════════════
    private JPanel buildExam() {
        JPanel outer = card(new BorderLayout(10, 10));
        outer.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        lblExamUser = lbl("", 13, Font.PLAIN, MUTED);
        lblTimer    = lbl("⏱ 10:00", 15, Font.BOLD, YELLOW);
        top.add(lblExamUser, BorderLayout.WEST);
        top.add(lblTimer,    BorderLayout.EAST);
        outer.add(top, BorderLayout.NORTH);

        // Question card
        questionPanel = new JPanel(new GridBagLayout());
        questionPanel.setBackground(CARD_BG);
        questionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            new EmptyBorder(16, 16, 16, 16)));
        GridBagConstraints g = gbc();

        lblQNum  = lbl("", 12, Font.PLAIN, MUTED);
        lblQText = lbl("", 15, Font.BOLD, TEXT);
        lblQText.setPreferredSize(new Dimension(520, 50));

        g.gridy = 0; questionPanel.add(lblQNum,  g);
        g.gridy = 1; questionPanel.add(lblQText, g);
        g.gridy = 2; questionPanel.add(Box.createVerticalStrut(10), g);

        btnGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            radios[i] = new JRadioButton();
            radios[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
            radios[i].setForeground(TEXT); radios[i].setOpaque(false);
            radios[i].setBackground(CARD_BG);
            btnGroup.add(radios[i]);
            g.gridy = 3 + i; questionPanel.add(radios[i], g);
        }
        outer.add(questionPanel, BorderLayout.CENTER);

        // Nav bar
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        nav.setOpaque(false);
        btnPrev   = btn("◀ Previous", MUTED);
        btnNext   = btn("Next ▶", ACCENT);
        btnSubmit = btn("✔ Submit Exam", GREEN);

        btnPrev.addActionListener(e -> navigate(-1));
        btnNext.addActionListener(e -> navigate(1));
        btnSubmit.addActionListener(e -> confirmSubmit());

        nav.add(btnPrev); nav.add(btnNext); nav.add(btnSubmit);
        outer.add(nav, BorderLayout.SOUTH);
        return outer;
    }

    private void loadQuestion() {
        saveCurrentAnswer();
        String[] q = QUESTIONS[currentQ];
        lblQNum.setText("Question " + (currentQ + 1) + " of " + QUESTIONS.length);
        lblQText.setText("<html><body style='width:500px'>" + q[0] + "</body></html>");
        for (int i = 0; i < 4; i++) {
            radios[i].setText((char)('A' + i) + ".  " + q[i + 1]);
        }
        // Restore saved answer
        btnGroup.clearSelection();
        if (answers[currentQ] >= 0) radios[answers[currentQ]].setSelected(true);

        btnPrev.setEnabled(currentQ > 0);
        btnNext.setEnabled(currentQ < QUESTIONS.length - 1);
        btnSubmit.setVisible(currentQ == QUESTIONS.length - 1);
    }

    private void saveCurrentAnswer() {
        for (int i = 0; i < 4; i++) {
            if (radios[i].isSelected()) { answers[currentQ] = i; return; }
        }
    }

    private void navigate(int dir) {
        saveCurrentAnswer();
        currentQ = Math.max(0, Math.min(QUESTIONS.length - 1, currentQ + dir));
        loadQuestion();
    }

    private void startTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            int m = timeLeft / 60, s = timeLeft % 60;
            lblTimer.setText(String.format("⏱ %02d:%02d", m, s));
            lblTimer.setForeground(timeLeft <= 60 ? RED : YELLOW);
            if (timeLeft <= 0) {
                countdownTimer.stop();
                JOptionPane.showMessageDialog(this, "⏰ Time's up! Auto-submitting.");
                submitExam();
            }
        });
        countdownTimer.start();
    }

    private void confirmSubmit() {
        saveCurrentAnswer();
        int unanswered = (int) Arrays.stream(answers).filter(a -> a == -1).count();
        String msg = unanswered > 0
            ? unanswered + " question(s) unanswered. Submit anyway?"
            : "Submit the exam now?";
        int r = JOptionPane.showConfirmDialog(this, msg, "Submit?", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) submitExam();
    }

    private void submitExam() {
        if (countdownTimer != null) countdownTimer.stop();
        long elapsed = (System.currentTimeMillis() - examStartMs) / 1000;

        int score = 0;
        StringBuilder bd = new StringBuilder();
        for (int i = 0; i < QUESTIONS.length; i++) {
            String[] q     = QUESTIONS[i];
            int correctIdx = q[5].charAt(0) - 'A';
            boolean right  = answers[i] == correctIdx;
            if (right) score++;
            bd.append(String.format("Q%2d. %s%n     Your: %s | Correct: %s%n%n",
                i + 1,
                right ? "✅" : "❌",
                answers[i] >= 0 ? String.valueOf((char)('A' + answers[i])) : "—",
                q[5]));
        }

        lblResultScore.setText("Score: " + score + " / " + QUESTIONS.length
            + "   (" + (int)(100.0 * score / QUESTIONS.length) + "%)");
        int m = (int)(elapsed / 60), s = (int)(elapsed % 60);
        lblResultTime.setText("Time taken: " + String.format("%02d:%02d", m, s));
        taResultBreakdown.setText(bd.toString());
        taResultBreakdown.setCaretPosition(0);
        cards.show(cardPanel, "RESULT");
    }

    // ══════════════════════════════ RESULT ═══════════════════════════════════
    private JPanel buildResult() {
        JPanel outer = card(new BorderLayout(10, 10));
        outer.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel top = new JPanel(new GridLayout(3, 1, 4, 4));
        top.setOpaque(false);
        top.add(lbl("🏁 Exam Results", 20, Font.BOLD, ACCENT));
        lblResultScore = lbl("", 16, Font.BOLD, GREEN);
        lblResultTime  = lbl("", 13, Font.PLAIN, MUTED);
        top.add(lblResultScore); top.add(lblResultTime);
        outer.add(top, BorderLayout.NORTH);

        taResultBreakdown = new JTextArea();
        taResultBreakdown.setEditable(false);
        taResultBreakdown.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taResultBreakdown.setBackground(CARD_BG); taResultBreakdown.setForeground(TEXT);
        taResultBreakdown.setBorder(new EmptyBorder(8, 8, 8, 8));
        outer.add(new JScrollPane(taResultBreakdown), BorderLayout.CENTER);

        JButton btnLogout = btn("🚪 Logout", RED);
        btnLogout.addActionListener(e -> {
            loggedInUser = null; displayName = null;
            tfLoginUser.setText(""); 
            cards.show(cardPanel, "LOGIN");
        });
        outer.add(btnLogout, BorderLayout.SOUTH);
        return outer;
    }

    // ══════════════════════════════ HELPERS ══════════════════════════════════
    private JPanel card(LayoutManager lm) {
        JPanel p = new JPanel(lm); p.setBackground(BG); return p;
    }
    private JLabel lbl(String t, int sz, int style, Color fg) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", style, sz)); l.setForeground(fg); return l;
    }
    private JTextField field(int c) {
        JTextField tf = new JTextField(c); styleField(tf); return tf;
    }
    private void styleField(JTextField tf) {
        tf.setBackground(new Color(49, 50, 68)); tf.setForeground(TEXT);
        tf.setCaretColor(TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1), new EmptyBorder(4, 6, 4, 6)));
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }
    private JButton btn(String t, Color bg) {
        JButton b = new JButton(t); b.setBackground(bg); b.setForeground(BG);
        b.setFont(new Font("SansSerif", Font.BOLD, 13)); b.setFocusPainted(false);
        b.setBorderPainted(false); b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }
    private JPanel row(String lbl, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel l = lbl(lbl, 13, Font.PLAIN, TEXT);
        l.setPreferredSize(new Dimension(120, 24));
        p.add(l); p.add(field); return p;
    }
    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6); g.gridx = 0;
        g.anchor = GridBagConstraints.CENTER; return g;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExamSystem().setVisible(true));
    }
}
