package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoryPanel extends BaseGamePanel {
    private Map<String, Scene> storyScenes;
    private ProgressManager progressManager;
    private VillagePanel villagePanel;

    private String fullNarrative = "";
    private int charIndex = 0;
    private boolean textComplete = false;
    private Timer typewriterTimer;
    private Timer transitionTimer;
    private float transitionAlpha = 0f;

    private Scene currentScene;
    private List<JButton> choiceButtons = new ArrayList<>();

    private static final int MARGIN = 12;
    private static final int BACKDROP_TOP = 50;
    private static final int BACKDROP_BOT_PAD = 24;
    private static final int TEXT_PAD_X = 14;
    private static final int TEXT_TOP = 78;
    private static final int BTN_GAP = 8;
    private static final int BTN_PAD_X = 18;
    private static final int BTN_PAD_BOT = 12;
    private static final Font TEXT_FONT = new Font("Serif", Font.PLAIN, 14);
    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 16);
    private static final Font BTN_FONT = new Font("Serif", Font.BOLD, 14);
    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color TEXT_COLOR = new Color(220, 220, 240);

    public StoryPanel(CardLayout cardLayout, JPanel mainPanel, Player player,
            Map<String, Scene> scenes, VillagePanel villagePanel) {
        super(cardLayout, mainPanel, player);
        this.storyScenes = scenes;
        this.villagePanel = villagePanel;
        this.progressManager = new ProgressManager();

        setLayout(null);
        setOpaque(false);
        setBackgroundImage("assets/village_bg.png");

        typewriterTimer = new Timer(28, e -> {
            if (charIndex < fullNarrative.length()) {
                charIndex++;
                repaint();
            } else if (!textComplete) {
                textComplete = true;
                typewriterTimer.stop();
                showChoiceButtons();
            }
        });

        transitionTimer = new Timer(16, e -> {
            transitionAlpha = Math.max(0, transitionAlpha - 15f);
            repaint();
            if (transitionAlpha <= 0) {
                transitionTimer.stop();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                skipTyping();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE
                        || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    skipTyping();
                }
            }
        });

        displayScene(player.getCurrentStoryScene());
    }

    private int backdropW() {
        int w = getWidth() - MARGIN * 2;
        return Math.max(w, 280);
    }

    private int backdropH() {
        int h = getHeight() - BACKDROP_TOP - BACKDROP_BOT_PAD;
        return Math.max(h, 300);
    }

    private int textW() {
        return backdropW() - TEXT_PAD_X * 2;
    }

    private void skipTyping() {
        if (!textComplete && typewriterTimer.isRunning()) {
            charIndex = fullNarrative.length();
            textComplete = true;
            typewriterTimer.stop();
            showChoiceButtons();
            repaint();
        }
    }

    private void displayScene(String sceneId) {
        typewriterTimer.stop();

        currentScene = storyScenes.get(sceneId);
        if (currentScene == null) {
            cardLayout.show(mainPanel, "Village");
            villagePanel.requestFocusInWindow();
            return;
        }

        player.setCurrentStoryScene(sceneId);
        clearChoiceButtons();

        fullNarrative = currentScene.getNarrativeText() != null
                ? currentScene.getNarrativeText() : "";
        charIndex = 0;
        textComplete = false;

        transitionAlpha = 255f;
        transitionTimer.start();
        typewriterTimer.start();
        requestFocusInWindow();
        repaint();
    }

    private void clearChoiceButtons() {
        for (JButton btn : choiceButtons) {
            remove(btn);
        }
        choiceButtons.clear();
    }

    private int getChoiceReserve() {
        if (currentScene == null)
            return 0;
        List<String> choiceTexts = getChoiceTexts();
        int total = 0;
        for (String text : choiceTexts) {
            total += buttonHeight(text) + BTN_GAP;
        }
        if (!choiceTexts.isEmpty())
            total -= BTN_GAP;
        return total + BTN_PAD_BOT * 2;
    }

    private List<String> getChoiceTexts() {
        List<String> texts = new ArrayList<>();
        if (currentScene == null)
            return texts;
        if (currentScene.isEnding()) {
            texts.add("Begin Your Adventure");
        } else {
            for (Scene.Choice c : currentScene.getChoices()) {
                texts.add(c.getChoiceText());
            }
        }
        return texts;
    }

    private int buttonHeight(String text) {
        int btnW = backdropW() - BTN_PAD_X * 2;
        FontMetrics fm = getFontMetrics(BTN_FONT);
        String wrapped = wrapAsHtml(text, btnW);
        int lineCount = 1;
        for (int i = 0; i < wrapped.length(); i++) {
            if (wrapped.charAt(i) == '\n')
                lineCount++;
        }
        return Math.max(38, lineCount * fm.getHeight() + 10);
    }

    private String wrapAsHtml(String text, int maxWidth) {
        FontMetrics fm = getFontMetrics(BTN_FONT);
        String[] words = text.split(" ");
        StringBuilder html = new StringBuilder();
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth - 12) {
                if (line.length() > 0)
                    line.append(" ");
                line.append(word);
            } else {
                if (html.length() > 0)
                    html.append("<br>");
                html.append(line);
                line = new StringBuilder(word);
            }
        }
        if (html.length() > 0)
            html.append("<br>");
        html.append(line);

        return "<html><div style='text-align:center;'>" + html.toString() + "</div></html>";
    }

    private int getTextBottom() {
        return BACKDROP_TOP + backdropH() - getChoiceReserve() - 6;
    }

    private void showChoiceButtons() {
        clearChoiceButtons();
        if (currentScene == null)
            return;

        int btnX = MARGIN + BTN_PAD_X;
        int btnW = backdropW() - BTN_PAD_X * 2;

        if (currentScene.isEnding()) {
            int btnH = buttonHeight("Begin Your Adventure");
            int btnY = BACKDROP_TOP + backdropH() - BTN_PAD_BOT - btnH;
            JButton btn = createStyledButton("Begin Your Adventure", btnW);
            btn.setBounds(btnX, btnY, btnW, btnH);
            btn.addActionListener(e -> {
                try {
                    progressManager.saveProgress(player);
                } catch (GameDataException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving game: " + ex.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                }
                cardLayout.show(mainPanel, "Village");
                villagePanel.requestFocusInWindow();
            });
            add(btn);
            choiceButtons.add(btn);
        } else {
            List<Scene.Choice> choices = currentScene.getChoices();
            int y = BACKDROP_TOP + backdropH() - BTN_PAD_BOT;
            List<Integer> heights = new ArrayList<>();
            int totalH = 0;
            for (Scene.Choice c : choices) {
                int h = buttonHeight(c.getChoiceText());
                heights.add(h);
                totalH += h + BTN_GAP;
            }
            if (!heights.isEmpty())
                totalH -= BTN_GAP;
            y -= totalH;

            int idx = 0;
            for (Scene.Choice choice : choices) {
                String text = choice.getChoiceText();
                int h = heights.get(idx);
                JButton btn = createStyledButton(text, btnW);
                btn.setBounds(btnX, y, btnW, h);
                final String nextId = choice.getNextSceneId();
                final String sceneId = currentScene.getSceneId();
                final int choiceIdx = idx;
                btn.addActionListener(e -> {
                    player.recordChoice(sceneId, choiceIdx);
                    displayScene(nextId);
                });
                add(btn);
                choiceButtons.add(btn);
                y += h + BTN_GAP;
                idx++;
            }
        }
        revalidate();
        repaint();
    }

    private JButton createStyledButton(String text, int btnW) {
        JButton btn = new JButton(wrapAsHtml(text, btnW));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(GOLD);
        btn.setFont(BTN_FONT);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(GOLD);
            }
        });
        return btn;
    }

    public void resetToCurrent() {
        displayScene(player.getCurrentStoryScene());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBackdrop(g);
        drawSceneTitle(g);
        drawChoiceSeparator(g);
        drawTypewriterText(g);
        drawTransitionOverlay(g);
    }

    private void drawBackdrop(Graphics g) {
        int bw = backdropW();
        int bh = backdropH();
        if (bw <= 0 || bh <= 0)
            return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(10, 10, 30, 195));
        g2.fillRoundRect(MARGIN, BACKDROP_TOP, bw, bh, 18, 18);
        g2.setColor(new Color(255, 215, 0, 70));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(MARGIN, BACKDROP_TOP, bw, bh, 18, 18);
        g2.dispose();
    }

    private void drawSceneTitle(Graphics g) {
        if (currentScene == null)
            return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(GOLD);
        g2.setFont(TITLE_FONT);

        String title = formatSceneTitle(currentScene.getSceneId());
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(title);
        g2.drawString(title, (getWidth() - tw) / 2, BACKDROP_TOP - 14);
        g2.dispose();
    }

    private void drawChoiceSeparator(Graphics g) {
        if (textComplete && currentScene != null) {
            int sepY = getTextBottom() + 4;
            g.setColor(new Color(255, 215, 0, 50));
            g.drawLine(MARGIN + 24, sepY,
                    MARGIN + backdropW() - 24, sepY);
        }
    }

    private String formatSceneTitle(String sceneId) {
        switch (sceneId) {
            case "jeff_bedroom":
                return "--- Jeff's Bedroom ---";
            case "intro_arrival":
                return "--- The Shores of Lucienne ---";
            case "path_scholar":
                return "--- Path of the Scholar ---";
            case "path_protector":
                return "--- Path of the Protector ---";
            case "journey_start":
                return "--- A New Beginning ---";
            default:
                return "--- " + sceneId.replace('_', ' ') + " ---";
        }
    }

    private void drawTypewriterText(Graphics g) {
        if (fullNarrative.isEmpty())
            return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(TEXT_FONT);
        g2.setColor(TEXT_COLOR);
        FontMetrics fm = g2.getFontMetrics();
        int lineH = fm.getHeight();
        int textBottom = getTextBottom();
        int textX = MARGIN + TEXT_PAD_X;
        int textW = textW();

        String visible = fullNarrative.substring(0,
                Math.min(charIndex, fullNarrative.length()));
        String[] rawLines = visible.split("\n", -1);

        int y = TEXT_TOP;
        int cursorLineX = textX;
        int cursorLineY = y;

        lineLoop:
        for (int li = 0; li < rawLines.length; li++) {
            List<String> wrapped = wrapText(rawLines[li], fm, textW);
            for (int wi = 0; wi < wrapped.size(); wi++) {
                if (y + lineH > textBottom)
                    break lineLoop;
                String wl = wrapped.get(wi);
                g2.drawString(wl, textX, y);

                if (li == rawLines.length - 1 && wi == wrapped.size() - 1) {
                    cursorLineX = textX + fm.stringWidth(wl);
                    cursorLineY = y;
                }
                y += lineH;
            }
        }

        if (!textComplete && typewriterTimer.isRunning()
                && charIndex < fullNarrative.length()) {
            g2.setColor(GOLD);
            g2.fillRect(cursorLineX, cursorLineY - lineH + fm.getAscent() + 2, 7, 3);
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
                if (line.length() > 0)
                    line.append(" ");
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
                        if (fm.stringWidth(part.toString() + c) > maxWidth
                                && part.length() > 0) {
                            lines.add(part.toString());
                            part = new StringBuilder();
                        }
                        part.append(c);
                    }
                    if (part.length() > 0)
                        line.append(part.toString());
                }
            }
        }

        if (line.length() > 0)
            lines.add(line.toString());

        return lines;
    }

    private void drawTransitionOverlay(Graphics g) {
        if (transitionAlpha > 0) {
            g.setColor(new Color(0, 0, 0, (int) transitionAlpha));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
