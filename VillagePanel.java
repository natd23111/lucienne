package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    // Dialogue overlay state
    private boolean inDialogue = false;
    private List<String> dialogueLines = new ArrayList<>();
    private int dialogueIndex = 0;
    private String dialogueFullText = "";
    private int dialogueCharIndex = 0;
    private boolean dialogueTextComplete = false;
    private Timer dialogueTimer;
    private String dialogueNpcName = "";

    // Scroll collection overlay state
    private boolean scrollOverlayActive = false;
    private String scrollOverlayTitle = "";
    private String scrollOverlayFullText = "";
    private int scrollOverlayCharIndex = 0;
    private boolean scrollOverlayTextComplete = false;
    private Timer scrollOverlayTimer;

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

        loadNPCs();
        loadZones();
        loadScrollFacts();

        gameLoop = new Timer(16, e -> {
            updateGame();
            repaint();
        });

        dialogueTimer = new Timer(28, e -> {
            if (dialogueCharIndex < dialogueFullText.length()) {
                dialogueCharIndex++;
                repaint();
            } else if (!dialogueTextComplete) {
                dialogueTextComplete = true;
                dialogueTimer.stop();
                repaint();
            }
        });

        scrollOverlayTimer = new Timer(28, e -> {
            if (scrollOverlayCharIndex < scrollOverlayFullText.length()) {
                scrollOverlayCharIndex++;
                repaint();
            } else if (!scrollOverlayTextComplete) {
                scrollOverlayTextComplete = true;
                scrollOverlayTimer.stop();
                repaint();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (inDialogue) {
                    if (code == KeyEvent.VK_E || code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                        if (!dialogueTextComplete && dialogueTimer.isRunning()) {
                            skipDialogueTyping();
                        } else {
                            advanceDialogue();
                        }
                    }
                    return;
                }
                if (scrollOverlayActive) {
                    if (code == KeyEvent.VK_E || code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                        if (!scrollOverlayTextComplete && scrollOverlayTimer.isRunning()) {
                            skipScrollOverlayTyping();
                        } else {
                            dismissScrollOverlay();
                        }
                    }
                    return;
                }
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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (inDialogue) {
                    if (!dialogueTextComplete && dialogueTimer.isRunning()) {
                        skipDialogueTyping();
                    } else {
                        advanceDialogue();
                    }
                } else if (scrollOverlayActive) {
                    if (!scrollOverlayTextComplete && scrollOverlayTimer.isRunning()) {
                        skipScrollOverlayTyping();
                    } else {
                        dismissScrollOverlay();
                    }
                }
            }
        });

        int heroStartCX = hero.getCenterX();
        int heroStartCY = hero.getCenterY();
        for (int i = 0; i < 3; i++) {
            int fx, fy;
            do {
                fx = 20 + random.nextInt(320);
                fy = 60 + random.nextInt(560);
            } while (Math.hypot(fx - heroStartCX, fy - heroStartCY) < 120);
            fragments.add(new FloatingFragment(fx, fy));
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
        if (inDialogue) {
            // Pause hero animation while in dialogue
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
                        startNPCDialogue(npc);
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
                    showScrollOverlay("Scroll of Knowledge",
                            "Knowledge Scroll Collected!\n\n" + fact);
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

        if (inDialogue) {
            drawDialogueOverlay(g);
        }

        if (scrollOverlayActive) {
            drawScrollOverlay(g);
        }
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

        int bx = (getWidth() - textW) / 2 - 8;
        int by = getHeight() - textH - 24;

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

    private void loadZones() {
        try (BufferedReader reader = new BufferedReader(new FileReader("zones.txt"))) {
            int x = 0, y = 0, w = 0, h = 0;
            String target = null;
            String prompt = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("X:")) {
                    x = Integer.parseInt(line.substring(2).trim());
                } else if (line.startsWith("Y:")) {
                    y = Integer.parseInt(line.substring(2).trim());
                } else if (line.startsWith("W:")) {
                    w = Integer.parseInt(line.substring(2).trim());
                } else if (line.startsWith("H:")) {
                    h = Integer.parseInt(line.substring(2).trim());
                } else if (line.startsWith("TARGET:")) {
                    String val = line.substring(7).trim();
                    target = (val.equalsIgnoreCase("none") || val.isEmpty()) ? null : val;
                } else if (line.startsWith("PROMPT:")) {
                    prompt = line.substring(7).trim();
                } else if (line.equals("END") && prompt != null) {
                    zones.add(new InteractionZone(x, y, w, h, target, prompt));
                    target = null;
                    prompt = null;
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Could not load zones: " + e.getMessage());
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

    private void startNPCDialogue(NPC npc) {
        if (npc.getDialogue().isEmpty()) return;
        inDialogue = true;
        dialogueLines = new ArrayList<>(npc.getDialogue());
        dialogueIndex = 0;
        dialogueNpcName = npc.getName();
        loadDialogueLine();
        clearInputState();
    }

    private void loadDialogueLine() {
        if (dialogueIndex >= dialogueLines.size()) {
            finishDialogue();
            return;
        }
        dialogueFullText = dialogueLines.get(dialogueIndex);
        dialogueCharIndex = 0;
        dialogueTextComplete = false;
        dialogueTimer.start();
        repaint();
    }

    private void skipDialogueTyping() {
        dialogueCharIndex = dialogueFullText.length();
        dialogueTextComplete = true;
        dialogueTimer.stop();
        repaint();
    }

    private void advanceDialogue() {
        dialogueIndex++;
        loadDialogueLine();
    }

    private void finishDialogue() {
        inDialogue = false;
        dialogueTimer.stop();
        dialogueLines.clear();
        dialogueNpcName = "";
        clearInputState();
        repaint();
    }

    private void showScrollOverlay(String title, String text) {
        scrollOverlayActive = true;
        scrollOverlayTitle = title;
        scrollOverlayFullText = text;
        scrollOverlayCharIndex = 0;
        scrollOverlayTextComplete = false;
        scrollOverlayTimer.start();
        clearInputState();
        repaint();
    }

    private void skipScrollOverlayTyping() {
        scrollOverlayCharIndex = scrollOverlayFullText.length();
        scrollOverlayTextComplete = true;
        scrollOverlayTimer.stop();
        repaint();
    }

    private void dismissScrollOverlay() {
        scrollOverlayActive = false;
        scrollOverlayTimer.stop();
        clearInputState();
        repaint();
    }

    private static final int DIALOGUE_MARGIN = 14;
    private static final int DIALOGUE_TOP = 420;
    private static final int DIALOGUE_PAD_X = 16;
    private static final int DIALOGUE_TEXT_TOP = 448;
    private static final Font DIALOGUE_FONT = new Font("Serif", Font.PLAIN, 14);
    private static final Font DIALOGUE_TITLE_FONT = new Font("Serif", Font.BOLD, 14);
    private static final Font DIALOGUE_HINT_FONT = new Font("SansSerif", Font.PLAIN, 11);
    private static final Color DIALOGUE_GOLD = new Color(255, 215, 0);
    private static final Color DIALOGUE_TEXT_COLOR = new Color(220, 220, 240);

    private void drawDialogueOverlay(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Dim the rest of the screen slightly
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRect(0, 0, w, h);

        // Backdrop
        int bw = w - DIALOGUE_MARGIN * 2;
        int bh = h - DIALOGUE_TOP - 20;
        if (bw <= 0 || bh <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw rounded backdrop
        g2.setColor(new Color(10, 10, 30, 220));
        g2.fillRoundRect(DIALOGUE_MARGIN, DIALOGUE_TOP, bw, bh, 18, 18);
        g2.setColor(new Color(255, 215, 0, 90));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(DIALOGUE_MARGIN, DIALOGUE_TOP, bw, bh, 18, 18);

        // Draw NPC name title
        g2.setColor(DIALOGUE_GOLD);
        g2.setFont(DIALOGUE_TITLE_FONT);
        String title = "--- " + dialogueNpcName + " ---";
        FontMetrics fmTitle = g2.getFontMetrics();
        int tw = fmTitle.stringWidth(title);
        g2.drawString(title, (w - tw) / 2, DIALOGUE_TOP - 10);

        // Draw typewriter text
        if (!dialogueFullText.isEmpty()) {
            g2.setFont(DIALOGUE_FONT);
            g2.setColor(DIALOGUE_TEXT_COLOR);
            FontMetrics fm = g2.getFontMetrics();
            int lineH = fm.getHeight();
            int textX = DIALOGUE_MARGIN + DIALOGUE_PAD_X;
            int maxTextW = bw - DIALOGUE_PAD_X * 2;
            int textBottom = DIALOGUE_TOP + bh - 28;

            String visible = dialogueFullText.substring(0, Math.min(dialogueCharIndex, dialogueFullText.length()));
            String[] rawLines = visible.split("\n", -1);

            int y = DIALOGUE_TEXT_TOP;
            lineLoop:
            for (int li = 0; li < rawLines.length; li++) {
                List<String> wrapped = wrapText(rawLines[li], fm, maxTextW);
                for (int wi = 0; wi < wrapped.size(); wi++) {
                    if (y + lineH > textBottom) break lineLoop;
                    g2.drawString(wrapped.get(wi), textX, y);
                    y += lineH;
                }
            }

            // Cursor
            if (!dialogueTextComplete && dialogueTimer.isRunning() && dialogueCharIndex < dialogueFullText.length()) {
                g2.setColor(DIALOGUE_GOLD);
                g2.fillRect(textX, y - lineH + fm.getAscent() + 2, 7, 3);
            }
        }

        // Hint
        if (dialogueTextComplete) {
            g2.setFont(DIALOGUE_HINT_FONT);
            g2.setColor(new Color(180, 160, 140));
            String hint = "Press E, Space, Enter or Click to continue";
            FontMetrics fmHint = g2.getFontMetrics();
            int hw = fmHint.stringWidth(hint);
            g2.drawString(hint, (w - hw) / 2, DIALOGUE_TOP + bh - 10);
        }

        g2.dispose();
    }

    private void drawScrollOverlay(Graphics g) {
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
        int tw = fmTitle.stringWidth(scrollOverlayTitle);
        g2.drawString(scrollOverlayTitle, (w - tw) / 2, top - 12);

        g2.setFont(new Font("Serif", Font.PLAIN, 14));
        g2.setColor(new Color(220, 220, 240));
        FontMetrics fm = g2.getFontMetrics();
        int lineH = fm.getHeight();
        int textX = margin + 16;
        int maxTextW = bw - 32;
        int textBottom = top + bh - 28;

        String visible = scrollOverlayFullText.substring(0, Math.min(scrollOverlayCharIndex, scrollOverlayFullText.length()));
        String[] rawLines = visible.split("\n", -1);

        int y = top + 32;
        lineLoop:
        for (int li = 0; li < rawLines.length; li++) {
            List<String> wrapped = wrapText(rawLines[li], fm, maxTextW);
            for (int wi = 0; wi < wrapped.size(); wi++) {
                if (y + lineH > textBottom) break lineLoop;
                g2.drawString(wrapped.get(wi), textX, y);
                y += lineH;
            }
        }

        if (!scrollOverlayTextComplete && scrollOverlayTimer.isRunning() && scrollOverlayCharIndex < scrollOverlayFullText.length()) {
            g2.setColor(new Color(255, 215, 0));
            g2.fillRect(textX, y - lineH + fm.getAscent() + 2, 7, 3);
        }

        if (scrollOverlayTextComplete) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(180, 160, 140));
            String hint = "Press E, Space, Enter or Click to continue";
            FontMetrics fmHint = g2.getFontMetrics();
            int hw = fmHint.stringWidth(hint);
            g2.drawString(hint, (w - hw) / 2, top + bh - 10);
        }

        g2.dispose();
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
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
