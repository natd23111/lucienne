package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LearningPanel extends BaseGamePanel {
    private BattlePanel battlePanel;
    private VillagePanel villagePanel;
    private QuestionParser parser;

    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color DARK_BG = new Color(8, 6, 28);

    public LearningPanel(CardLayout cardLayout, JPanel mainPanel, Player player,
            BattlePanel battlePanel, VillagePanel villagePanel) {
        super(cardLayout, mainPanel, player);
        this.battlePanel = battlePanel;
        this.villagePanel = villagePanel;
        this.parser = new QuestionParser();

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(DARK_BG);

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        JLabel titleLabel = new JLabel("Knowledge Garden", SwingConstants.CENTER);
        titleLabel.setForeground(GOLD);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        headerPanel.add(titleLabel);

        JLabel subtitle = new JLabel("<html><div style='text-align:center;'>"
                + "Study SDG 4 safely. Score 80%+ to master a topic.</div></html>",
                SwingConstants.CENTER);
        subtitle.setForeground(new Color(160, 150, 200));
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 12));
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        JPanel categoryContainer = new JPanel();
        categoryContainer.setLayout(new BoxLayout(categoryContainer, BoxLayout.Y_AXIS));
        categoryContainer.setOpaque(false);

        Map<String, List<Question>> questionsMap;
        try {
            questionsMap = parser.parse();
        } catch (GameDataException e) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + e.getMessage());
            questionsMap = new HashMap<>();
        }
        final Map<String, List<Question>> categorizedQuestions = questionsMap;

        for (String category : categorizedQuestions.keySet()) {
            final String categoryName = category;
            boolean mastered = player.isCategoryMastered(categoryName);

            String html = buildCategoryHtml(categoryName, mastered);
            JButton rowBtn = new JButton(html);
            rowBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            rowBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
            rowBtn.setMinimumSize(new Dimension(280, 55));
            rowBtn.setFont(new Font("Serif", Font.BOLD, 13));
            rowBtn.setForeground(mastered ? new Color(128, 224, 128) : GOLD);
            rowBtn.setHorizontalAlignment(SwingConstants.CENTER);
            rowBtn.setOpaque(false);
            rowBtn.setContentAreaFilled(false);
            rowBtn.setBorderPainted(false);
            rowBtn.setFocusPainted(false);
            rowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            rowBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            rowBtn.addActionListener(e -> {
                List<Question> questionsForCategory = categorizedQuestions.get(categoryName);
                battlePanel.setReturnScreen("Village");
                battlePanel.startLearningBattle(questionsForCategory, categoryName);
                cardLayout.show(mainPanel, "BattleQuiz");
            });

            rowBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    rowBtn.setForeground(Color.WHITE);
                }
                public void mouseExited(MouseEvent e) {
                    rowBtn.setForeground(mastered ? new Color(128, 224, 128) : GOLD);
                }
            });

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(60, 50, 100));
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

            categoryContainer.add(Box.createRigidArea(new Dimension(0, 6)));
            categoryContainer.add(rowBtn);
            categoryContainer.add(Box.createRigidArea(new Dimension(0, 4)));
            categoryContainer.add(sep);
        }

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JScrollPane scrollPane = new JScrollPane(categoryContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerWrap.add(scrollPane, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        JButton backBtn = new JButton("<html><div style='text-align:center;'>Return to Village</div></html>");
        backBtn.setFont(new Font("Serif", Font.BOLD, 13));
        backBtn.setForeground(new Color(180, 150, 120));
        backBtn.setHorizontalAlignment(SwingConstants.CENTER);
        backBtn.setOpaque(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            cardLayout.show(mainPanel, "Village");
            villagePanel.requestFocusInWindow();
        });
        backBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { backBtn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { backBtn.setForeground(new Color(180, 150, 120)); }
        });

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private String buildCategoryHtml(String name, boolean mastered) {
        String display = name;
        if (mastered) {
            display = "<font color='#80e080'>" + name + "</font>";
        }
        String status = mastered ? "  [MASTERED]" : "";
        return "<html><div style='text-align:center;padding:4px 0;'>"
                + display + status + "</div></html>";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) { g2.dispose(); return; }

        GradientPaint grad = new GradientPaint(0, 0, new Color(10, 8, 35),
                0, h, new Color(20, 15, 50));
        g2.setPaint(grad);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(30, 25, 60, 35));
        for (int i = 0; i < 8; i++) {
            int x = (i * 97 + 31) % w;
            int y = (i * 73 + 17) % h;
            g2.fillOval(x, y, 50 + i * 5, 30 + i * 3);
        }

        drawTopDivider(g2, w);

        g2.dispose();
    }

    private void drawTopDivider(Graphics2D g2, int w) {
        g2.setColor(new Color(255, 215, 0, 40));
        g2.setStroke(new BasicStroke(1));
        int y = 68;
        g2.drawLine(40, y, w - 40, y);
    }
}
