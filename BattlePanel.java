package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattlePanel extends BaseGamePanel {
    private BattleEngine battleEngine;
    private ProgressManager progressManager;
    private VillagePanel villagePanel;
    private SoundManager soundManager;
    private Inventory inventory;

    private List<Question> currentQuestions;
    private int currentQuestionIndex;
    private int correctAnswersCount;

    private String enemyName;
    private Image enemyImage;
    private int enemyX, enemyY;
    private double enemyPhase;
    private double enemyRotate;
    private Timer animTimer;
    private Random random = new Random();
    private String returnScreen = "Village";

    private boolean learningMode;
    private String learningCategory;

    private Color flashColor = null;
    private Timer flashTimer;

    private List<Integer> hiddenOptionIndices = new ArrayList<>();

    private JButton fleeBtn;
    private JLabel questionLabel;
    private JLabel progressLabel;
    private java.util.List<JButton> optionBtns = new ArrayList<>();
    private JPanel itemBar;
    private JButton potionBtn;
    private JButton charmBtn;

    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color DARK_BG = new Color(8, 6, 28);
    private static final Color CARD_BG = new Color(14, 12, 40, 210);
    private static final Color GOLD_DIM = new Color(255, 215, 0, 60);

    public BattlePanel(CardLayout cardLayout, JPanel mainPanel, Player player, BattleEngine engine,
            VillagePanel villagePanel, SoundManager soundManager, Inventory inventory) {
        super(cardLayout, mainPanel, player);
        this.battleEngine = engine;
        this.progressManager = new ProgressManager();
        this.villagePanel = villagePanel;
        this.soundManager = soundManager;
        this.inventory = inventory;

        enemyImage = new ImageIcon("assets/enemy_fragment.png").getImage();

        setLayout(null);
        setOpaque(true);
        setBackground(DARK_BG);

        questionLabel = new JLabel();
        questionLabel.setForeground(new Color(220, 220, 240));
        questionLabel.setFont(new Font("Serif", Font.BOLD, 15));
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        questionLabel.setVerticalAlignment(SwingConstants.CENTER);
        add(questionLabel);

        progressLabel = new JLabel("", SwingConstants.CENTER);
        progressLabel.setForeground(new Color(160, 140, 200));
        progressLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(progressLabel);

        itemBar = new JPanel();
        itemBar.setOpaque(false);
        itemBar.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        add(itemBar);

        fleeBtn = createStyledButton("Flee to Village");
        fleeBtn.addActionListener(e -> {
            animTimer.stop();
            cardLayout.show(mainPanel, "Village");
            villagePanel.requestFocusInWindow();
        });
        add(fleeBtn);

        animTimer = new Timer(30, e -> {
            enemyPhase += 0.05;
            enemyRotate += 0.02;
            repaint();
        });
    }

    public void setReturnScreen(String screen) { this.returnScreen = screen; }

    public void startBattle(List<Question> questions) {
        startBattle(questions, null);
    }

    public void startBattle(List<Question> questions, String enemy) {
        this.learningMode = false;
        this.learningCategory = null;
        initBattle(questions);

        if (enemy != null) {
            this.enemyName = enemy;
        } else {
            String[] enemies = { "Fractured Geometry", "Corrupted Literacy",
                    "Faded Science", "Twisted History", "Shadow of Ignorance" };
            this.enemyName = enemies[random.nextInt(enemies.length)];
        }

        enemyX = getWidth() / 4;
        enemyY = 130;
        enemyPhase = 0;
        enemyRotate = 0;
        animTimer.start();
        showNextQuestion();
    }

    public void startLearningBattle(List<Question> questions, String category) {
        this.learningMode = true;
        this.learningCategory = category;
        initBattle(questions);
        this.enemyName = null;
        animTimer.stop();
        showNextQuestion();
    }

    private void initBattle(List<Question> questions) {
        this.currentQuestions = questions;
        this.currentQuestionIndex = 0;
        this.correctAnswersCount = 0;
        this.hiddenOptionIndices.clear();
        clearOptionButtons();
    }

    private void clearOptionButtons() {
        for (JButton btn : optionBtns) remove(btn);
        optionBtns.clear();
    }

    private void showNextQuestion() {
        clearOptionButtons();
        hiddenOptionIndices.clear();
        refreshItemBar();

        if (currentQuestions == null || currentQuestionIndex >= currentQuestions.size()) {
            finishBattle();
            return;
        }

        Question q = currentQuestions.get(currentQuestionIndex);
        int total = currentQuestions.size();

        questionLabel.setText("<html><div style='text-align:center;'>"
                + q.getQuestionText() + "</div></html>");

        String progressText = learningMode
                ? "Study  \u2022  " + (currentQuestionIndex + 1) + " / " + total
                : enemyName + "  \u2022  " + (currentQuestionIndex + 1) + " / " + total;
        progressLabel.setText(progressText);

        List<String> raw = q.getOptions();
        final List<String> options;
        if (raw.isEmpty()) {
            options = java.util.Arrays.asList("True", "False");
        } else {
            options = raw;
        }

        int btnY = 320;
        int btnW = 280;
        int btnX = (getWidth() - btnW) / 2;

        for (int i = 0; i < options.size(); i++) {
            int btnH = 44;
            int y = btnY + i * (btnH + 6);

            JButton btn = new JButton("<html><div style='text-align:center;'>"
                    + options.get(i) + "</div></html>");
            btn.setName("opt-" + i);
            btn.setBounds(btnX, y, btnW, btnH);
            btn.setFont(new Font("Serif", Font.BOLD, 13));
            btn.setForeground(GOLD);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            final int idx = i;
            btn.addActionListener(e -> handleAnswer(q, options.get(idx), idx));

            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
                public void mouseExited(MouseEvent e) { btn.setForeground(GOLD); }
            });

            add(btn);
            optionBtns.add(btn);
        }

        if (!learningMode && player.isCategoryMastered(q.getCategory())) {
            applyMasteryHint(q);
        }

        layoutComponents();
        revalidate();
        repaint();
    }

    private void refreshItemBar() {
        itemBar.removeAll();
        int potionCount = inventory.getKnowledgePotionCount();
        int charmCount = inventory.getMemoryCharmCount();

        if (potionCount > 0) {
            potionBtn = new JButton("Potion x" + potionCount);
            potionBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            potionBtn.setForeground(new Color(100, 220, 140));
            potionBtn.setOpaque(false);
            potionBtn.setContentAreaFilled(false);
            potionBtn.setBorderPainted(false);
            potionBtn.setFocusPainted(false);
            potionBtn.addActionListener(e -> useKnowledgePotion());
            potionBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { potionBtn.setForeground(Color.WHITE); }
                public void mouseExited(MouseEvent e) { potionBtn.setForeground(new Color(100, 220, 140)); }
            });
            itemBar.add(potionBtn);
        }
        if (charmCount > 0) {
            charmBtn = new JButton("Charm x" + charmCount);
            charmBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            charmBtn.setForeground(new Color(200, 180, 100));
            charmBtn.setOpaque(false);
            charmBtn.setContentAreaFilled(false);
            charmBtn.setBorderPainted(false);
            charmBtn.setFocusPainted(false);
            charmBtn.addActionListener(e -> useMemoryCharm());
            charmBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { charmBtn.setForeground(Color.WHITE); }
                public void mouseExited(MouseEvent e) { charmBtn.setForeground(new Color(200, 180, 100)); }
            });
            itemBar.add(charmBtn);
        }
        itemBar.revalidate();
        itemBar.repaint();
    }

    private void layoutComponents() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        int cardX = 20;
        int cardW = w - 40;

        questionLabel.setBounds(cardX + 10, 100, cardW - 20, 70);

        progressLabel.setBounds(cardX, 22, cardW, 20);

        int itemY = learningMode ? 280 : 280;
        itemBar.setBounds(cardX, itemY, cardW, 30);

        fleeBtn.setBounds(cardX, h - 60, cardW, 38);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 13));
        btn.setForeground(new Color(200, 160, 140));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { btn.setForeground(new Color(200, 160, 140)); }
        });
        return btn;
    }

    private void applyMasteryHint(Question q) {
        List<String> options = q.getOptions();
        if (options.isEmpty() || options.size() <= 2) return;

        String correct = q.getCorrectAnswer();
        List<Integer> wrongIndices = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            if (!options.get(i).startsWith(correct + ")")
                    && !options.get(i).equalsIgnoreCase(correct)) {
                wrongIndices.add(i);
            }
        }
        if (wrongIndices.isEmpty()) return;

        int hintIdx = wrongIndices.get(random.nextInt(wrongIndices.size()));
        hiddenOptionIndices.add(hintIdx);

        for (JButton btn : optionBtns) {
            if (btn.getName() != null && btn.getName().equals("opt-" + hintIdx)) {
                btn.setEnabled(false);
                btn.setForeground(new Color(80, 60, 40));
                btn.setText("(" + btn.getText() + ")");
            }
        }
    }

    private void useKnowledgePotion() {
        if (inventory.useKnowledgePotion()) {
            Question q = currentQuestions.get(currentQuestionIndex);
            correctAnswersCount++;
            player.addScore(10);
            JOptionPane.showMessageDialog(this,
                    "Knowledge Potion used! " + q.getCorrectAnswer() + " is correct.");
            soundManager.playCorrect();
            currentQuestionIndex++;
            showNextQuestion();
        }
    }

    private void useMemoryCharm() {
        if (inventory.useMemoryCharm()) {
            Question q = currentQuestions.get(currentQuestionIndex);
            List<String> options = q.getOptions();
            if (options.isEmpty()) return;

            String correct = q.getCorrectAnswer();
            List<Integer> wrongIndices = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                if (!options.get(i).startsWith(correct + ")")
                        && !options.get(i).equalsIgnoreCase(correct)) {
                    wrongIndices.add(i);
                }
            }

            while (wrongIndices.size() > (options.size() - 2) && wrongIndices.size() > 1) {
                wrongIndices.remove(random.nextInt(wrongIndices.size()));
            }
            if (wrongIndices.size() > (options.size() - 2)) {
                wrongIndices.remove(wrongIndices.size() - 1);
            }

            hiddenOptionIndices.addAll(wrongIndices);
            soundManager.playCollect();

            for (JButton btn : optionBtns) {
                if (btn.getName() != null && btn.getName().startsWith("opt-")) {
                    int idx = Integer.parseInt(btn.getName().substring(4));
                    if (hiddenOptionIndices.contains(idx)) {
                        btn.setEnabled(false);
                        btn.setForeground(new Color(60, 20, 20));
                        btn.setText("--");
                    }
                }
            }
        }
    }

    private void handleAnswer(Question q, String selected, int optionIndex) {
        if (hiddenOptionIndices.contains(optionIndex)) return;

        if (q.checkAnswer(selected)) {
            correctAnswersCount++;
            int points = (!learningMode && player.hasSagesScroll()) ? 12 : 10;
            player.addScore(points);
            if (!learningMode) player.addXp(3);
            flashCorrect();
            soundManager.playCorrect();
            if (learningMode) {
                JOptionPane.showMessageDialog(this,
                        "Correct! \"" + q.getCorrectAnswer() + "\" is right.");
            } else {
                JOptionPane.showMessageDialog(this, "Correct! The fragment weakens...");
            }
        } else {
            flashWrong();
            soundManager.playWrong();
            if (!learningMode) {
                int penalty = Math.min(5, player.getScore());
                if (penalty > 0) player.setScore(player.getScore() - penalty);
                JOptionPane.showMessageDialog(this,
                        "Incorrect. The fragment grows stronger...\n(-" + penalty + " KP)");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Not quite. The correct answer is: " + q.getCorrectAnswer());
            }
        }
        currentQuestionIndex++;
        showNextQuestion();
    }

    private void flashCorrect() {
        flashColor = new Color(0, 255, 0, 50);
        if (flashTimer != null) flashTimer.stop();
        flashTimer = new Timer(200, e -> { flashColor = null; repaint(); });
        flashTimer.setRepeats(false);
        flashTimer.start();
        repaint();
    }

    private void flashWrong() {
        flashColor = new Color(255, 0, 0, 50);
        if (flashTimer != null) flashTimer.stop();
        flashTimer = new Timer(200, e -> { flashColor = null; repaint(); });
        flashTimer.setRepeats(false);
        flashTimer.start();
        repaint();
    }

    private void finishBattle() {
        animTimer.stop();
        int total = currentQuestions != null ? currentQuestions.size() : 0;

        if (learningMode && total > 0) {
            double pct = ((double) correctAnswersCount / total) * 100;
            if (pct >= 80 && learningCategory != null) {
                player.masterCategory(learningCategory);
                JOptionPane.showMessageDialog(this,
                        "Category Mastered: " + learningCategory + "!\n"
                                + "Score: " + correctAnswersCount + "/" + total
                                + "\n\nThis knowledge will now help you in the Battleground.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Study Result: " + correctAnswersCount + "/" + total
                                + "\n\nScore 80% or higher to master this category.");
            }
        } else {
            String message = battleEngine.getMotivationalMessage(correctAnswersCount, total);
            soundManager.playVictory();
            JOptionPane.showMessageDialog(this,
                    "Battle Result: " + correctAnswersCount + "/" + total
                            + "\n" + message + "\nLevel: " + player.getLevel()
                            + " | XP: " + player.getXp() + "/15");
        }

        try {
            progressManager.saveProgress(player);
        } catch (GameDataException ex) {
            JOptionPane.showMessageDialog(this, "Error saving game: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
        villagePanel.updateDisplay();
        cardLayout.show(mainPanel, returnScreen);
        if (returnScreen.equals("Village")) {
            villagePanel.requestFocusInWindow();
        }
        returnScreen = "Village";
        learningMode = false;
        learningCategory = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) { g2.dispose(); return; }

        drawBackground(g2, w, h);

        if (!learningMode) {
            drawEnemy(g2);
            drawEnemyHPBar(g2);
        }

        drawQuestionCard(g2, w);

        if (flashColor != null) {
            g2.setColor(flashColor);
            g2.fillRect(0, 0, w, h);
        }

        g2.dispose();
    }

    private void drawBackground(Graphics2D g2, int w, int h) {
        GradientPaint grad = new GradientPaint(0, 0, new Color(10, 8, 35),
                0, h, new Color(20, 15, 50));
        g2.setPaint(grad);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(30, 25, 60, 40));
        for (int i = 0; i < 8; i++) {
            int x = (i * 97 + 31) % w;
            int y = (i * 73 + 17) % h;
            g2.fillOval(x, y, 50 + i * 5, 30 + i * 3);
        }

        g2.setColor(new Color(255, 255, 255, 6));
        for (int i = 0; i < 30; i++) {
            int x = (i * 47 + 11) % w;
            int y = (i * 61 + 13) % h;
            g2.fillOval(x, y, 2, 2);
        }
    }

    private void drawQuestionCard(Graphics2D g2, int w) {
        int cardX = 18;
        int cardY = 50;
        int cardW = w - 36;
        int cardH = 145;

        g2.setColor(CARD_BG);
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 14, 14);
        g2.setColor(GOLD_DIM);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(cardX, cardY, cardW, cardH, 14, 14);
    }

    private void drawEnemy(Graphics2D g2) {
        double floatY = Math.sin(enemyPhase) * 8;
        int cx = enemyX;
        int cy = (int) (enemyY + floatY);
        int size = 55;

        g2.translate(cx, cy);
        g2.rotate(enemyRotate);

        RadialGradientPaint glow = new RadialGradientPaint(
                0, 0, size,
                new float[] { 0.0f, 0.5f, 1.0f },
                new Color[] {
                        new Color(180, 60, 180, 60),
                        new Color(100, 20, 100, 25),
                        new Color(0, 0, 0, 0)
                });
        g2.setPaint(glow);
        g2.fillOval(-size, -size, size * 2, size * 2);

        if (enemyImage != null) {
            g2.drawImage(enemyImage, -24, -24, 48, 48, null);
        } else {
            Color darkPurple = new Color(60, 20, 80);
            g2.setColor(darkPurple);
            Polygon frag = new Polygon(
                    new int[] { 0, 25, 12, -10, -25, -6 },
                    new int[] { -25, -8, 15, 25, 6, -12 }, 6);
            g2.fillPolygon(frag);
        }

        g2.rotate(-enemyRotate);
        g2.translate(-cx, -cy);

        g2.setFont(new Font("Serif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(enemyName);
        int tx = enemyX - tw / 2;
        int ty = enemyY + 38;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(tx - 6, ty - fm.getAscent(), tw + 12, fm.getHeight() + 4, 6, 6);
        g2.setColor(new Color(255, 120, 120));
        g2.drawString(enemyName, tx, ty);
    }

    private void drawEnemyHPBar(Graphics2D g2) {
        if (currentQuestions == null || currentQuestions.isEmpty()) return;
        int bx = enemyX - 60;
        int by = enemyY + 60;
        int bw = 120;
        int bh = 6;

        int remaining = currentQuestions.size() - currentQuestionIndex;
        double ratio = (double) remaining / currentQuestions.size();

        g2.setColor(new Color(40, 10, 20));
        g2.fillRoundRect(bx, by, bw, bh, 3, 3);

        Color barColor;
        if (ratio > 0.6) barColor = new Color(200, 60, 60);
        else if (ratio > 0.3) barColor = new Color(220, 140, 20);
        else barColor = new Color(220, 50, 20);

        g2.setColor(barColor);
        g2.fillRoundRect(bx, by, (int) (bw * ratio), bh, 3, 3);

        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(bx, by, bw, bh, 3, 3);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        layoutComponents();
    }
}
