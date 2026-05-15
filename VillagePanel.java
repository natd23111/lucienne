package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class VillagePanel extends BaseGamePanel {
    private Image heroSprite;
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
    private boolean ePressed = false;

    private List<FloatingFragment> fragments = new ArrayList<>();
    private List<InteractionZone> zones = new ArrayList<>();
    private Random random = new Random();

    private String promptText = null;

    public VillagePanel(CardLayout cardLayout, JPanel mainPanel, Player player) {
        super(cardLayout, mainPanel, player);

        setLayout(null);
        setOpaque(false);

        setBackgroundImage("assets/village_bg.png");
        heroSprite = new ImageIcon("assets/jeff_sprite.png").getImage();
        spriteSheet = new ImageIcon("assets/jeff_spritesheet.png").getImage();

        zones.add(new InteractionZone(30, 100, 140, 100, "KnowledgeGarden", "Explore Knowledge Garden"));
        zones.add(new InteractionZone(190, 100, 140, 100, "BattleGround", "Enter Battle Ground"));
        zones.add(new InteractionZone(30, 400, 140, 100, "VillageShop", "Visit Village Shop"));
        zones.add(new InteractionZone(190, 400, 140, 100, null, "Save Game"));

        gameLoop = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code >= 0 && code < keys.length) {
                    keys[code] = true;
                }
                if (code == KeyEvent.VK_E) {
                    ePressed = true;
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

        for (int i = 0; i < 8; i++) {
            fragments.add(new FloatingFragment(
                    random.nextInt(300), random.nextInt(500)));
        }

        gameLoop.start();
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
        } else {
            animFrame = 0;
            animTick = 0;
        }

        heroX = Math.max(0, Math.min(heroX, getWidth() - heroW));
        heroY = Math.max(0, Math.min(heroY, getHeight() - heroH));

        promptText = null;
        Rectangle heroRect = new Rectangle(heroX, heroY, heroW, heroH);
        for (InteractionZone zone : zones) {
            if (heroRect.intersects(zone.bounds)) {
                promptText = zone.prompt;
                break;
            }
        }

        if (ePressed) {
            ePressed = false;
            for (InteractionZone zone : zones) {
                if (heroRect.intersects(zone.bounds)) {
                    if (zone.targetScreen != null) {
                        cardLayout.show(mainPanel, zone.targetScreen);
                    } else {
                        try {
                            new ProgressManager().saveProgress(player);
                        } catch (GameDataException ex) {
                            JOptionPane.showMessageDialog(this,
                                    "Error saving game: " + ex.getMessage(),
                                    "Save Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        JOptionPane.showMessageDialog(this,
                                "Progress Saved to savegame.txt!");
                    }
                    break;
                }
            }
        }

        Iterator<FloatingFragment> it = fragments.iterator();
        while (it.hasNext()) {
            FloatingFragment f = it.next();
            f.update();

            double fdx = (heroX + heroW / 2.0) - f.x;
            double fdy = (heroY + heroH / 2.0) - f.y;
            if (Math.sqrt(fdx * fdx + fdy * fdy) < 40) {
                player.addScore(5);
                it.remove();
            }
        }
    }

    public void updateDisplay() {
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (FloatingFragment f : fragments) {
            f.draw(g);
        }

        drawHero(g);

        if (promptText != null) {
            drawPrompt(g);
        }

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

        if (heroSprite != null) {
            g.drawImage(heroSprite, heroX, heroY, heroW, heroH, this);
            return;
        }

        g.setColor(Color.BLUE);
        g.fillRect(heroX, heroY, heroW, heroH);
        g.setColor(Color.WHITE);
        String[] dirLabels = { "D", "L", "R", "U" };
        g.drawString(dirLabels[facingDir], heroX + heroW / 2 - 4, heroY + heroH / 2 + 4);
    }

    private void drawPrompt(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font = new Font("SansSerif", Font.BOLD, 14);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(promptText);
        int textH = fm.getHeight();

        int bx = heroX + heroW / 2 - textW / 2 - 8;
        int by = heroY - textH - 12;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(bx, by, textW + 16, textH + 8, 8, 8);
        g2.setColor(Color.WHITE);
        g2.drawString(promptText, bx + 8, by + textH - 2);
        g2.dispose();
    }

    private void drawHUD(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int hudH = 40;
        g2.setColor(new Color(20, 20, 40, 180));
        g2.fillRect(0, 0, getWidth(), hudH);
        g2.setColor(new Color(255, 215, 0, 100));
        g2.drawLine(0, hudH, getWidth(), hudH);

        Font font = new Font("SansSerif", Font.BOLD, 14);
        g2.setFont(font);
        g2.setColor(Color.WHITE);

        String nameStr = player.getName();
        String kpStr = "KP: " + player.getScore();

        g2.drawString(nameStr, 12, 24);
        FontMetrics fm = g2.getFontMetrics();
        int kpW = fm.stringWidth(kpStr);
        g2.drawString(kpStr, getWidth() - kpW - 12, 24);

        g2.dispose();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    private static class InteractionZone {
        Rectangle bounds;
        String targetScreen;
        String prompt;

        InteractionZone(int x, int y, int w, int h, String targetScreen, String prompt) {
            this.bounds = new Rectangle(x, y, w, h);
            this.targetScreen = targetScreen;
            this.prompt = "Press E to " + prompt;
        }
    }

    private class FloatingFragment {
        double x, y;
        double baseY;
        double phase;

        FloatingFragment(double x, double y) {
            this.x = x;
            this.y = y;
            this.baseY = y;
            this.phase = random.nextDouble() * Math.PI * 2;
        }

        void update() {
            phase += 0.05;
            y = baseY + Math.sin(phase) * 10;
        }

        void draw(Graphics g) {
            Color gold = new Color(255, 215, 0, 180);
            Color glow = new Color(255, 255, 100, 100);
            g.setColor(glow);
            g.fillOval((int) x - 10, (int) y - 10, 20, 20);
            g.setColor(gold);
            g.fillOval((int) x - 6, (int) y - 6, 12, 12);
        }
    }
}
