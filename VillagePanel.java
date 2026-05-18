package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class VillagePanel extends BaseGamePanel {
    private Hero hero;
    private Image scrollIcon;

    private Timer gameLoop;
    private boolean[] keys = new boolean[256];
    private boolean ePressed = false;

    private List<FloatingFragment> fragments = new ArrayList<>();
    private List<InteractionZone> zones = new ArrayList<>();
    private List<CollectParticle> particles = new ArrayList<>();
    private Random random = new Random();

    private String promptText = null;
    private SoundManager soundManager;

    private List<NPC> npcs = new ArrayList<>();
    private List<String> scrollFacts = new ArrayList<>();

    public VillagePanel(CardLayout cardLayout, JPanel mainPanel, Player player, SoundManager soundManager) {
        super(cardLayout, mainPanel, player);
        this.soundManager = soundManager;

        setLayout(null);
        setOpaque(false);

        setBackgroundImage("assets/village_bg.png");
        Image heroSprite = new ImageIcon("assets/jeff_sprite.png").getImage();
        Image spriteSheet = new ImageIcon("assets/jeff_spritesheet.png").getImage();
        scrollIcon = new ImageIcon("assets/icon_scroll.png").getImage();
        hero = new Hero(spriteSheet, heroSprite, 180, 320, 48, 64, 3);

        zones.add(new InteractionZone(30, 100, 140, 100, "KnowledgeGarden", "Explore Knowledge Garden"));
        zones.add(new InteractionZone(190, 100, 140, 100, "BattleGround", "Enter Battle Ground"));
        zones.add(new InteractionZone(30, 400, 140, 100, "VillageShop", "Visit Village Shop"));
        zones.add(new InteractionZone(190, 400, 140, 100, null, "Save Game"));
        zones.add(new InteractionZone(300, 540, 50, 50, "Codex", "Open Codex"));

        loadNPCs();
        loadScrollFacts();

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

    private void clearInputState() {
        for (int i = 0; i < keys.length; i++) {
            keys[i] = false;
        }
        ePressed = false;
    }

    private void updateGame() {
        hero.update(
            keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP],
            keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN],
            keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT],
            keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT],
            getWidth(), getHeight()
        );

        promptText = null;
        Rectangle heroRect = new Rectangle(hero.getX(), hero.getY(), hero.getWidth(), hero.getHeight());
        for (InteractionZone zone : zones) {
            if (heroRect.intersects(zone.bounds)) {
                promptText = zone.prompt;
                break;
            }
        }

        if (promptText == null) {
            for (NPC npc : npcs) {
                Rectangle npcRect = new Rectangle(npc.getX(), npc.getY(), 48, 64);
                if (heroRect.intersects(npcRect)) {
                    promptText = "Press E to talk to " + npc.getName();
                    break;
                }
            }
        }

        if (ePressed) {
            ePressed = false;
            boolean handled = false;
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
                            clearInputState();
                            return;
                        }
                        JOptionPane.showMessageDialog(this,
                                "Progress Saved to savegame.txt!");
                        clearInputState();
                    }
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                for (NPC npc : npcs) {
                    Rectangle npcRect = new Rectangle(npc.getX(), npc.getY(), 48, 64);
                    if (heroRect.intersects(npcRect)) {
                        showNPCDialogue(npc);
                        handled = true;
                        break;
                    }
                }
            }
        }

        Iterator<FloatingFragment> it = fragments.iterator();
        while (it.hasNext()) {
            FloatingFragment f = it.next();
            f.update();

            double fdx = hero.getCenterX() - f.x;
            double fdy = hero.getCenterY() - f.y;
            if (Math.sqrt(fdx * fdx + fdy * fdy) < 40) {
                player.addScore(5);
                soundManager.playCollect();
                spawnParticles(f.x, f.y);
                it.remove();
                String fact = getNextScrollFact();
                if (fact != null) {
                    player.addCollectedScroll(fact);
                    JOptionPane.showMessageDialog(this,
                            "Knowledge Scroll Collected!\n\n" + fact,
                            "Scroll of Knowledge", JOptionPane.INFORMATION_MESSAGE);
                    clearInputState();
                }
            }
        }

        for (Iterator<CollectParticle> pi = particles.iterator(); pi.hasNext();) {
            CollectParticle p = pi.next();
            p.update();
            if (p.life <= 0) pi.remove();
        }
    }

    public void updateDisplay() {
    }

    private void spawnParticles(double x, double y) {
        for (int i = 0; i < 8; i++) {
            particles.add(new CollectParticle(x, y));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (FloatingFragment f : fragments) {
            f.draw(g);
        }

        for (CollectParticle p : particles) {
            p.draw(g);
        }

        drawHero(g);

        for (NPC npc : npcs) {
            drawNPC(g, npc);
        }

        if (promptText != null) {
            drawPrompt(g);
        }

        drawHUD(g);
    }

    private void drawHero(Graphics g) {
        hero.draw(g, this);
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

        int bx = hero.getCenterX() - textW / 2 - 8;
        int by = hero.getY() - textH - 12;

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

    private void loadNPCs() {
        try (BufferedReader reader = new BufferedReader(new FileReader("npcs.txt"))) {
            String name = null;
            int x = 0, y = 0;
            String sprite = null;
            List<String> dialogue = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("NAME:")) {
                    name = line.substring(5).trim();
                } else if (line.startsWith("X:")) {
                    x = Integer.parseInt(line.substring(2).trim());
                } else if (line.startsWith("Y:")) {
                    y = Integer.parseInt(line.substring(2).trim());
                } else if (line.startsWith("SPRITE:")) {
                    sprite = "assets/" + line.substring(7).trim();
                } else if (line.startsWith("DIALOGUE:")) {
                    dialogue.add(line.substring(9).trim());
                } else if (line.equals("END") && name != null && sprite != null) {
                    NPC npc = new NPC(name, x, y, sprite);
                    for (String d : dialogue) npc.addDialogue(d);
                    npcs.add(npc);
                    name = null; sprite = null;
                    dialogue.clear();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load NPCs: " + e.getMessage());
        }
    }

    private void loadScrollFacts() {
        try (BufferedReader reader = new BufferedReader(new FileReader("scrolls.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    scrollFacts.add(line);
                }
            }
            Collections.shuffle(scrollFacts);
        } catch (IOException e) {
            System.err.println("Could not load scroll facts: " + e.getMessage());
        }
    }

    private String getNextScrollFact() {
        if (scrollFacts.isEmpty()) {
            scrollFacts.add("Knowledge is the most powerful weapon to change the world.");
        }
        int collected = player.getCollectedScrolls().size();
        if (collected >= scrollFacts.size()) {
            return scrollFacts.get(random.nextInt(scrollFacts.size()));
        }
        return scrollFacts.get(collected);
    }

    private void showNPCDialogue(NPC npc) {
        if (npc.getDialogue().isEmpty()) return;
        StringBuilder msg = new StringBuilder();
        for (String line : npc.getDialogue()) {
            if (msg.length() > 0) msg.append("\n\n");
            msg.append(line);
        }
        JOptionPane.showMessageDialog(this, msg.toString(),
                npc.getName(), JOptionPane.PLAIN_MESSAGE);
        clearInputState();
    }

    private void drawNPC(Graphics g, NPC npc) {
        Image sprite = new ImageIcon(npc.getSpritePath()).getImage();
        int npcW = 48;
        int npcH = 64;
        if (sprite.getWidth(null) > 0) {
            g.drawImage(sprite, npc.getX(), npc.getY(), npcW, npcH, null);
        } else {
            g.setColor(new Color(150, 120, 80));
            g.fillRect(npc.getX(), npc.getY(), npcW, npcH);
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(new Color(0, 0, 0, 140));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(npc.getName());
        g.fillRect(npc.getX() + npcW / 2 - tw / 2 - 3, npc.getY() - 14, tw + 6, 14);
        g.setColor(new Color(255, 215, 0));
        g.drawString(npc.getName(), npc.getX() + npcW / 2 - tw / 2, npc.getY() - 3);
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
            if (scrollIcon != null) {
                g.drawImage(scrollIcon, (int) x - 12, (int) y - 12, 64, 64, null);
            } else {
                Color gold = new Color(255, 215, 0, 180);
                Color glow = new Color(255, 255, 100, 100);
                g.setColor(glow);
                g.fillOval((int) x - 10, (int) y - 10, 20, 20);
                g.setColor(gold);
                g.fillOval((int) x - 6, (int) y - 6, 12, 12);
            }
        }
    }

    private class CollectParticle {
        double x, y;
        double vx, vy;
        int life;
        int maxLife;

        CollectParticle(double x, double y) {
            this.x = x;
            this.y = y;
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 1.5 + random.nextDouble() * 3;
            this.vx = Math.cos(angle) * speed;
            this.vy = Math.sin(angle) * speed - 2;
            this.life = 20 + random.nextInt(20);
            this.maxLife = this.life;
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.1;
            life--;
        }

        void draw(Graphics g) {
            float alpha = (float) life / maxLife;
            g.setColor(new Color(255, 215, 0, (int) (200 * alpha)));
            int size = (int) (4 * alpha + 2);
            g.fillOval((int) x, (int) y, size, size);
        }
    }
}
