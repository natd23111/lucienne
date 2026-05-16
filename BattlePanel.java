package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattlePanel extends BaseGamePanel {
    private BattleEngine battleEngine;
    private ProgressManager progressManager;
    private VillagePanel villagePanel;
    private SoundManager soundManager;
    private Inventory inventory;

    private JLabel questionLabel;
    private JPanel optionsPanel;
    private JPanel bottomPanel;
    private List<Question> currentQuestions;
    private int currentQuestionIndex;
    private int correctAnswersCount;
    private boolean scoreMultiplier = true;

    private String enemyName;
    private int enemyX, enemyY;
    private double enemyPhase;
    private double enemyRotate;
    private Timer animTimer;
    private Random random = new Random();
    private String returnScreen = "Village";

    private Color flashColor = null;
    private Timer flashTimer;

    private List<Integer> hiddenOptionIndices = new ArrayList<>();

    public BattlePanel(CardLayout cardLayout, JPanel mainPanel, Player player, BattleEngine engine,
            VillagePanel villagePanel, SoundManager soundManager, Inventory inventory) {
        super(cardLayout, mainPanel, player);
        this.battleEngine = engine;
        this.progressManager = new ProgressManager();
        this.villagePanel = villagePanel;
        this.soundManager = soundManager;
        this.inventory = inventory;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        questionLabel = new JLabel("Prepare for Battle!", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Serif", Font.BOLD, 16));
        add(questionLabel, BorderLayout.NORTH);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);
        add(optionsPanel, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        JButton fleeBtn = new JButton("Flee to Village");
        fleeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        fleeBtn.addActionListener(e -> {
            animTimer.stop();
            cardLayout.show(mainPanel, "Village");
            villagePanel.requestFocusInWindow();
        });
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        bottomPanel.add(fleeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

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
        this.currentQuestions = questions;
        this.currentQuestionIndex = 0;
        this.correctAnswersCount = 0;
        this.hiddenOptionIndices.clear();

        if (enemy != null) {
            this.enemyName = enemy;
        } else {
            String[] enemies = { "Fractured Geometry", "Corrupted Literacy",
                    "Faded Science", "Twisted History", "Shadow of Ignorance" };
            this.enemyName = enemies[random.nextInt(enemies.length)];
        }

        enemyX = 300;
        enemyY = 180;
        enemyPhase = 0;
        enemyRotate = 0;
        animTimer.start();
        showNextQuestion();
    }

    private void showNextQuestion() {
        optionsPanel.removeAll();
        hiddenOptionIndices.clear();

        if (currentQuestionIndex < currentQuestions.size()) {
            Question q = currentQuestions.get(currentQuestionIndex);
            questionLabel.setText("<html><div style='text-align: center;'>"
                    + q.getQuestionText() + "</div></html>");

            List<String> options = q.getOptions();
            if (options.isEmpty()) {
                options = java.util.Arrays.asList("True", "False");
            }

            addItemButton();

            for (int i = 0; i < options.size(); i++) {
                final String option = options.get(i);
                final int idx = i;
                JButton btn = new JButton(option);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                btn.addActionListener(e -> handleAnswer(q, option, idx));
                btn.setName("opt-" + i);
                optionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                optionsPanel.add(btn);
            }

            if (!inventory.hasSagesScroll()) {
                scoreMultiplier = false;
            }
            if (player.hasSagesScroll()) {
                scoreMultiplier = true;
            }
        } else {
            finishBattle();
        }
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void addItemButton() {
        int potionCount = inventory.getKnowledgePotionCount();
        int charmCount = inventory.getMemoryCharmCount();
        if (potionCount == 0 && charmCount == 0) return;

        JPanel itemBar = new JPanel();
        itemBar.setOpaque(false);

        if (potionCount > 0) {
            JButton potionBtn = new JButton("Potion (x" + potionCount + ")");
            potionBtn.addActionListener(e -> useKnowledgePotion());
            itemBar.add(potionBtn);
        }
        if (charmCount > 0) {
            JButton charmBtn = new JButton("Charm (x" + charmCount + ")");
            charmBtn.addActionListener(e -> useMemoryCharm());
            itemBar.add(charmBtn);
        }

        itemBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        optionsPanel.add(itemBar);
    }

    private void useKnowledgePotion() {
        if (inventory.useKnowledgePotion()) {
            Question q = currentQuestions.get(currentQuestionIndex);
            correctAnswersCount++;
            addScoreToPlayer();
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

            Component[] comps = optionsPanel.getComponents();
            for (Component comp : comps) {
                if (comp instanceof JButton && ((JButton) comp).getName() != null
                        && ((JButton) comp).getName().startsWith("opt-")) {
                    int idx = Integer.parseInt(((JButton) comp).getName().substring(4));
                    if (hiddenOptionIndices.contains(idx)) {
                        ((JButton) comp).setEnabled(false);
                        ((JButton) comp).setText("--");
                        ((JButton) comp).setBackground(new Color(60, 20, 20));
                    }
                }
            }
        }
    }

    private void handleAnswer(Question q, String selected, int optionIndex) {
        if (hiddenOptionIndices.contains(optionIndex)) return;

        if (q.checkAnswer(selected)) {
            correctAnswersCount++;
            addScoreToPlayer();
            flashCorrect();
            soundManager.playCorrect();
            JOptionPane.showMessageDialog(this, "Correct! The fragment weakens...");
        } else {
            flashWrong();
            soundManager.playWrong();
            JOptionPane.showMessageDialog(this, "Incorrect. The fragment grows stronger...");
        }
        currentQuestionIndex++;
        showNextQuestion();
    }

    private void addScoreToPlayer() {
        int points = player.hasSagesScroll() ? 12 : 10;
        player.addScore(points);
        player.addXp(3);
    }

    private void flashCorrect() {
        flashColor = new Color(0, 255, 0, 60);
        if (flashTimer != null) flashTimer.stop();
        flashTimer = new Timer(200, e -> { flashColor = null; repaint(); });
        flashTimer.setRepeats(false);
        flashTimer.start();
        repaint();
    }

    private void flashWrong() {
        flashColor = new Color(255, 0, 0, 60);
        if (flashTimer != null) flashTimer.stop();
        flashTimer = new Timer(200, e -> { flashColor = null; repaint(); });
        flashTimer.setRepeats(false);
        flashTimer.start();
        repaint();
    }

    private void finishBattle() {
        animTimer.stop();
        String message = battleEngine.getMotivationalMessage(correctAnswersCount, currentQuestions.size());
        soundManager.playVictory();
        JOptionPane.showMessageDialog(this,
                "Battle Result: " + correctAnswersCount + "/" + currentQuestions.size()
                        + "\n" + message + "\nLevel: " + player.getLevel()
                        + " | XP: " + player.getXp() + "/15");
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawEnemy(g);

        if (enemyName != null && currentQuestionIndex < currentQuestions.size()) {
            drawEnemyName(g);
        }

        if (flashColor != null) {
            g.setColor(flashColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void drawEnemy(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double floatY = Math.sin(enemyPhase) * 8;

        int cx = (int) enemyX;
        int cy = (int) (enemyY + floatY);
        int size = 40;

        g2.translate(cx, cy);
        g2.rotate(enemyRotate);

        RadialGradientPaint glowPaint = new RadialGradientPaint(
                0, 0, size + 10,
                new float[] { 0.0f, 0.5f, 1.0f },
                new Color[] {
                        new Color(180, 60, 180, 80),
                        new Color(100, 20, 100, 40),
                        new Color(0, 0, 0, 0)
                });
        g2.setPaint(glowPaint);
        g2.fillOval(-size - 10, -size - 10, (size + 10) * 2, (size + 10) * 2);

        Color darkPurple = new Color(60, 20, 80);
        Color edgePurple = new Color(150, 50, 180);

        int[] xs = { 0, size, size / 2, -size / 3, -size, -size / 4 };
        int[] ys = { -size, -size / 3, size / 2, size, size / 4, -size / 2 };
        Polygon frag = new Polygon(xs, ys, xs.length);

        g2.setColor(darkPurple);
        g2.fillPolygon(frag);
        g2.setColor(edgePurple);
        g2.setStroke(new BasicStroke(2));
        g2.drawPolygon(frag);

        int eyeSize = 6;
        g2.setColor(Color.WHITE);
        g2.fillOval(-10, -6, eyeSize * 2, eyeSize * 2);
        g2.fillOval(-eyeSize, -6, eyeSize * 2, eyeSize * 2);
        g2.setColor(Color.BLACK);
        g2.fillOval(-8, -4, eyeSize, eyeSize);
        g2.fillOval(-eyeSize + 2, -4, eyeSize, eyeSize);

        g2.dispose();
    }

    private void drawEnemyName(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Serif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(enemyName);
        int tx = enemyX - tw / 2;
        int ty = enemyY + 55;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(tx - 6, ty - fm.getAscent(), tw + 12, fm.getHeight() + 4, 6, 6);
        g2.setColor(new Color(255, 100, 100));
        g2.drawString(enemyName, tx, ty);
        g2.dispose();
    }
}