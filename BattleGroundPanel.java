package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BattleGroundPanel extends BaseGamePanel {
    private VillagePanel villagePanel;
    private BattlePanel battlePanel;
    private QuestionParser questionParser;
    private SoundManager soundManager;

    private Hero hero;

    private Timer gameLoop;
    private boolean[] keys = new boolean[256];

    private int stepCount = 0;
    private int encounterThreshold;
    private Random random = new Random();

    private List<Question> allQuestions;
    private boolean questionsLoaded;

    // Overlay state for encounter messages
    private boolean overlayActive = false;
    private String overlayTitle = "";
    private String overlayFullText = "";
    private int overlayCharIndex = 0;
    private boolean overlayTextComplete = false;
    private Timer overlayTimer;
    private Runnable overlayCallback;

    public BattleGroundPanel(CardLayout cardLayout, JPanel mainPanel, Player player,
            VillagePanel villagePanel, BattlePanel battlePanel,
            SoundManager soundManager, QuestionParser questionParser) {
        super(cardLayout, mainPanel, player);
        this.villagePanel = villagePanel;
        this.battlePanel = battlePanel;
        this.soundManager = soundManager;
        this.questionParser = questionParser;

        setLayout(null);
        setOpaque(false);

        setBackgroundImage("assets/battleground_bg.png");
        Image spriteSheet = new ImageIcon("assets/jeff_wizard_spritesheet.png").getImage();
        hero = new Hero(spriteSheet, null, 180, 320, 48, 64, 3);
        encounterThreshold = 35 + random.nextInt(16);

        gameLoop = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                activate();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                deactivate();
            }
        });

        overlayTimer = new Timer(28, e -> {
            if (overlayCharIndex < overlayFullText.length()) {
                overlayCharIndex++;
                repaint();
            } else if (!overlayTextComplete) {
                overlayTextComplete = true;
                overlayTimer.stop();
                repaint();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (overlayActive) {
                    if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_E) {
                        if (!overlayTextComplete && overlayTimer.isRunning()) {
                            skipOverlayTyping();
                        } else {
                            dismissOverlay();
                        }
                    }
                    return;
                }
                if (code >= 0 && code < keys.length) {
                    keys[code] = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code >= 0 && code < keys.length) {
                    keys[code] = false;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (overlayActive) {
                    if (!overlayTextComplete && overlayTimer.isRunning()) {
                        skipOverlayTyping();
                    } else {
                        dismissOverlay();
                    }
                }
            }
        });

        loadQuestions();
    }

    private void loadQuestions() {
        try {
            Map<String, List<Question>> categorized = questionParser.parse();
            allQuestions = new ArrayList<>();
            for (List<Question> catQuestions : categorized.values()) {
                allQuestions.addAll(catQuestions);
            }
            questionsLoaded = !allQuestions.isEmpty();
        } catch (GameDataException e) {
            questionsLoaded = false;
        }
    }

    private void updateGame() {
        if (overlayActive) {
            hero.resetAnimation();
            return;
        }

        hero.update(
            keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP],
            keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN],
            keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT],
            keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT],
            getWidth(), getHeight()
        );

        if (hero.isMoving()) {
            stepCount++;
            if (stepCount >= encounterThreshold) {
                triggerEncounter();
            }
        }
    }

    private void clearInputState() {
        for (int i = 0; i < keys.length; i++) {
            keys[i] = false;
        }
    }

    private void triggerEncounter() {
        deactivate();
        stepCount = 0;
        encounterThreshold = 35 + random.nextInt(16);

        if (!questionsLoaded) {
            loadQuestions();
        }
        if (!questionsLoaded) {
            activate();
            return;
        }

        List<Question> temp = new ArrayList<>(allQuestions);
        Collections.shuffle(temp);
        if (temp.size() > 5) {
            temp = new ArrayList<>(temp.subList(0, 5));
        }
        final List<Question> battleQuestions = temp;

        String[] enemies = { "Fractured Geometry", "Corrupted Literacy",
                "Faded Science", "Twisted History", "Shadow of Ignorance",
                "Fractured Memory", "Dark Calculation", "Lost Knowledge" };
        final String enemyName = enemies[random.nextInt(enemies.length)];

        soundManager.playDamage();
        showEncounterOverlay("Encounter!",
                "An Evil Memory Fragment materializes from the mist!\n\n\""
                        + enemyName + " appears before you...\"", () -> {
            battlePanel.setReturnScreen("BattleGround");
            battlePanel.startBattle(battleQuestions, enemyName);
            cardLayout.show(mainPanel, "BattleQuiz");
        });
    }

    private void activate() {
        stepCount = 0;
        hero.resetAnimation();
        for (int i = 0; i < keys.length; i++) keys[i] = false;
        gameLoop.start();
        requestFocusInWindow();
    }

    private void deactivate() {
        gameLoop.stop();
        hero.resetAnimation();
        for (int i = 0; i < keys.length; i++) keys[i] = false;
        if (overlayActive) {
            overlayTimer.stop();
            overlayActive = false;
            overlayCallback = null;
        }
    }

    private void showEncounterOverlay(String title, String text, Runnable onDismiss) {
        overlayActive = true;
        overlayTitle = title;
        overlayFullText = text;
        overlayCharIndex = 0;
        overlayTextComplete = false;
        overlayCallback = onDismiss;
        overlayTimer.start();
        repaint();
    }

    private void skipOverlayTyping() {
        overlayCharIndex = overlayFullText.length();
        overlayTextComplete = true;
        overlayTimer.stop();
        repaint();
    }

    private void dismissOverlay() {
        overlayActive = false;
        overlayTimer.stop();
        repaint();
        if (overlayCallback != null) {
            Runnable cb = overlayCallback;
            overlayCallback = null;
            cb.run();
        }
    }

    private void drawOverlay(Graphics g) {
        if (!overlayActive) return;

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        g.setColor(new Color(0, 0, 0, 245));
        g.fillRect(0, 0, w, h);

        int margin = 20;
        int top = 160;
        int bw = w - margin * 2;
        int bh = 180;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(new Color(10, 10, 30, 230));
        g2.fillRoundRect(margin, top, bw, bh, 18, 18);
        g2.setColor(new Color(255, 215, 0, 90));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(margin, top, bw, bh, 18, 18);

        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        FontMetrics fmTitle = g2.getFontMetrics();
        int tw = fmTitle.stringWidth(overlayTitle);
        g2.drawString(overlayTitle, (w - tw) / 2, top - 12);

        g2.setFont(new Font("Serif", Font.PLAIN, 14));
        g2.setColor(new Color(220, 220, 240));
        FontMetrics fm = g2.getFontMetrics();
        int lineH = fm.getHeight();
        int textX = margin + 16;
        int maxTextW = bw - 32;
        int textBottom = top + bh - 28;

        String visible = overlayFullText.substring(0, Math.min(overlayCharIndex, overlayFullText.length()));
        String[] rawLines = visible.split("\n", -1);

        int y = top + 32;
        lineLoop:
        for (int li = 0; li < rawLines.length; li++) {
            List<String> wrapped = wrapOverlayText(rawLines[li], fm, maxTextW);
            for (int wi = 0; wi < wrapped.size(); wi++) {
                if (y + lineH > textBottom) break lineLoop;
                g2.drawString(wrapped.get(wi), textX, y);
                y += lineH;
            }
        }

        if (!overlayTextComplete && overlayTimer.isRunning() && overlayCharIndex < overlayFullText.length()) {
            g2.setColor(new Color(255, 215, 0));
            g2.fillRect(textX, y - lineH + fm.getAscent() + 2, 7, 3);
        }

        if (overlayTextComplete) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(180, 160, 140));
            String hint = "Press Space, Enter or Click to continue";
            FontMetrics fmHint = g2.getFontMetrics();
            int hw = fmHint.stringWidth(hint);
            g2.drawString(hint, (w - hw) / 2, top + bh - 10);
        }

        g2.dispose();
    }

    private List<String> wrapOverlayText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split(" ", -1);
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth) {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            } else {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
                if (fm.stringWidth(word) <= maxWidth) {
                    line.append(word);
                } else {
                    StringBuilder part = new StringBuilder();
                    for (int i = 0; i < word.length(); i++) {
                        char c = word.charAt(i);
                        if (fm.stringWidth(part.toString() + c) > maxWidth && part.length() > 0) {
                            lines.add(part.toString());
                            part = new StringBuilder();
                        }
                        part.append(c);
                    }
                    if (part.length() > 0) line.append(part.toString());
                }
            }
        }

        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        activate();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawHero(g);
        drawHUD(g);
        drawOverlay(g);
    }

    private void drawHero(Graphics g) {
        hero.draw(g, this);
    }

    private void drawHUD(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int hudH = 50;
        g2.setColor(new Color(20, 10, 40, 200));
        g2.fillRect(0, 0, getWidth(), hudH);
        g2.setColor(new Color(180, 80, 180, 100));
        g2.drawLine(0, hudH, getWidth(), hudH);

        Font font = new Font("SansSerif", Font.BOLD, 13);
        g2.setFont(font);
        g2.setColor(Color.WHITE);

        String nameStr = player.getName();
        String kpStr = "KP: " + player.getScore();
        String lvlStr = "Lv." + player.getLevel();
        String xpStr = "XP: " + player.getXp() + "/15";

        g2.drawString(nameStr, 12, 16);
        g2.drawString(lvlStr, 12, 38);

        FontMetrics fm = g2.getFontMetrics();
        int kpW = fm.stringWidth(kpStr);
        int xpW = fm.stringWidth(xpStr);
        g2.drawString(kpStr, getWidth() - kpW - 12, 16);
        g2.drawString(xpStr, getWidth() - xpW - 12, 38);

        int xpBarX = getWidth() - 120;
        int xpBarY = 42;
        int xpBarW = 108;
        int xpBarH = 5;
        g2.setColor(new Color(40, 20, 60));
        g2.fillRect(xpBarX, xpBarY, xpBarW, xpBarH);
        g2.setColor(new Color(180, 100, 255));
        int filledW = (int) ((double) player.getXp() / 15 * xpBarW);
        g2.fillRect(xpBarX, xpBarY, Math.min(filledW, xpBarW), xpBarH);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(360, 640);
    }
}
