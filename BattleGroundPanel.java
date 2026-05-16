package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

    private Image spriteSheet;
    private int heroX = 180;
    private int heroY = 320;
    private int heroW = 48;
    private int heroH = 64;
    private int facingDir = 0;
    private int animFrame = 0;
    private int animTick = 0;
    private boolean isMoving = false;

    private Timer gameLoop;
    private boolean[] keys = new boolean[256];

    private int stepCount = 0;
    private int encounterThreshold;
    private Random random = new Random();

    private List<Question> allQuestions;
    private boolean questionsLoaded;

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
        spriteSheet = new ImageIcon("assets/jeff_wizard_spritesheet.png").getImage();
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

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
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
        int prevX = heroX;
        int prevY = heroY;
        int speed = 3;

        if (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP])
            heroY -= speed;
        if (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN])
            heroY += speed;
        if (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT])
            heroX -= speed;
        if (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT])
            heroX += speed;

        int dx = heroX - prevX;
        int dy = heroY - prevY;
        isMoving = (dx != 0 || dy != 0);

        if (isMoving) {
            if (Math.abs(dx) >= Math.abs(dy)) {
                facingDir = (dx > 0) ? 2 : 1;
            } else {
                facingDir = (dy > 0) ? 0 : 3;
            }

            animTick++;
            if (animTick >= 8) {
                animTick = 0;
                animFrame = (animFrame + 1) % 4;
            }

            stepCount++;
            if (stepCount >= encounterThreshold) {
                triggerEncounter();
            }
        } else {
            animFrame = 0;
            animTick = 0;
        }

        heroX = Math.max(0, Math.min(heroX, getWidth() - heroW));
        heroY = Math.max(0, Math.min(heroY, getHeight() - heroH));
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

        List<Question> battleQuestions = new ArrayList<>(allQuestions);
        Collections.shuffle(battleQuestions);
        if (battleQuestions.size() > 5) {
            battleQuestions = battleQuestions.subList(0, 5);
        }

        String[] enemies = { "Fractured Geometry", "Corrupted Literacy",
                "Faded Science", "Twisted History", "Shadow of Ignorance",
                "Fractured Memory", "Dark Calculation", "Lost Knowledge" };
        String enemyName = enemies[random.nextInt(enemies.length)];

        soundManager.playDamage();
        JOptionPane.showMessageDialog(this,
                "An Evil Memory Fragment materializes from the mist!\n\n\""
                        + enemyName + " appears before you...\"");

        battlePanel.setReturnScreen("BattleGround");
        battlePanel.startBattle(battleQuestions, enemyName);
        cardLayout.show(mainPanel, "BattleQuiz");
    }

    private void activate() {
        stepCount = 0;
        isMoving = false;
        animFrame = 0;
        animTick = 0;
        for (int i = 0; i < keys.length; i++) keys[i] = false;
        gameLoop.start();
        requestFocusInWindow();
    }

    private void deactivate() {
        gameLoop.stop();
        isMoving = false;
        animFrame = 0;
        animTick = 0;
        for (int i = 0; i < keys.length; i++) keys[i] = false;
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
    }

    private void drawHero(Graphics g) {
        if (spriteSheet != null) {
            int sheetCols = 4;
            int sheetRows = 4;
            int fw = spriteSheet.getWidth(this) / sheetCols;
            int fh = spriteSheet.getHeight(this) / sheetRows;
            if (fw > 0 && fh > 0) {
                int sx = animFrame * fw;
                int sy = facingDir * fh;
                g.drawImage(spriteSheet, heroX, heroY, heroX + heroW, heroY + heroH,
                        sx, sy, sx + fw, sy + fh, this);
                return;
            }
        }

        g.setColor(Color.BLUE);
        g.fillRect(heroX, heroY, heroW, heroH);
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
