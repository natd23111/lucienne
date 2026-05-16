package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CodexPanel extends BaseGamePanel {
    private VillagePanel villagePanel;

    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color DARK_BG = new Color(8, 6, 28);
    private static final Color CARD_BG = new Color(12, 10, 34, 210);
    private static final Color GOLD_DIM = new Color(255, 215, 0, 50);

    public CodexPanel(CardLayout cardLayout, JPanel mainPanel, Player player,
            VillagePanel villagePanel) {
        super(cardLayout, mainPanel, player);
        this.villagePanel = villagePanel;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(DARK_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 10, 16));
        JLabel title = new JLabel("Codex of Knowledge", SwingConstants.CENTER);
        title.setForeground(GOLD);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        List<String> scrolls = player.getCollectedScrolls();
        if (scrolls.isEmpty()) {
            JLabel emptyLabel = new JLabel("No scrolls collected yet. Explore the village!",
                    SwingConstants.CENTER);
            emptyLabel.setForeground(new Color(140, 130, 170));
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(Box.createVerticalGlue());
            content.add(emptyLabel);
            content.add(Box.createVerticalGlue());
        } else {
            for (int i = 0; i < scrolls.size(); i++) {
                JPanel card = createFactCard(i + 1, scrolls.get(i));
                content.add(Box.createRigidArea(new Dimension(0, 6)));
                content.add(card);
            }
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(6, 16, 10, 16));
        centerWrap.add(scrollPane, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        JButton backBtn = new JButton("<html><div style='text-align:center;'>Return to Village</div></html>");
        backBtn.setFont(new Font("Serif", Font.BOLD, 13));
        backBtn.setForeground(GOLD);
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
            public void mouseExited(MouseEvent e) { backBtn.setForeground(GOLD); }
        });

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createFactCard(int number, String fact) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(GOLD_DIM);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(320, 70));
        card.setMaximumSize(new Dimension(320, 100));
        card.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel numLabel = new JLabel("Scroll #" + number);
        numLabel.setForeground(GOLD);
        numLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        numLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(numLabel);

        JLabel factLabel = new JLabel("<html><div style='color:#c0c0d0;width:280px;'>"
                + fact + "</div></html>");
        factLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        factLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(factLabel);

        return card;
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

        int headerBottom = 54;
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, w, headerBottom);
        g2.setColor(GOLD_DIM);
        g2.drawLine(40, headerBottom, w - 40, headerBottom);

        g2.dispose();
    }
}
