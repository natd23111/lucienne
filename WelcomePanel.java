package Project;

import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends BaseGamePanel {
    private Image titleImage;
    private VillagePanel villagePanel;
    private StoryPanel storyPanel;

    public WelcomePanel(CardLayout cardLayout, JPanel mainPanel, Player player,
            VillagePanel villagePanel, StoryPanel storyPanel) {
        super(cardLayout, mainPanel, player);
        this.villagePanel = villagePanel;
        this.storyPanel = storyPanel;

        setLayout(null);
        setOpaque(false);

        setBackgroundImage("assets/village_bg.png");
        titleImage = new ImageIcon("assets/Lucienne_Screen_Title.png").getImage();

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(3, 1, 0, 18));
        menuPanel.setOpaque(false);
        menuPanel.setBounds(40, 340, 280, 230);

        menuPanel.add(createMenuButton("New Quest", e -> {
            player.setScore(0);
            player.setCurrentStoryScene("intro_arrival");
            storyPanel.resetToCurrent();
            cardLayout.show(mainPanel, "Story");
        }));

        menuPanel.add(createMenuButton("Continue Quest", e -> {
            cardLayout.show(mainPanel, "Village");
            villagePanel.requestFocusInWindow();
        }));

        menuPanel.add(createMenuButton("Exit Kingdom", e -> System.exit(0)));

        add(menuPanel);
    }

    private JButton createMenuButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(new Color(255, 215, 0));
        btn.setFont(new Font("Serif", Font.BOLD, 24));
        btn.setFocusPainted(false);
        btn.addActionListener(action);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setForeground(new Color(255, 215, 0));
            }
        });

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawTitleImage(g);
        drawMenuBackdrop(g);
    }

    private void drawTitleImage(Graphics g) {
        if (titleImage != null) {
            int tw = titleImage.getWidth(this);
            int th = titleImage.getHeight(this);
            if (tw > 0 && th > 0) {
                int targetW = Math.min(tw, getWidth() - 60);
                int targetH = (int) ((double) targetW / tw * th);
                int tx = (getWidth() - targetW) / 2;
                int ty = 60;
                g.drawImage(titleImage, tx, ty, targetW, targetH, this);
            }
        }
    }

    private void drawMenuBackdrop(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int bx = 30;
        int by = 330;
        int bw = getWidth() - 60;
        int bh = 250;

        g2.setColor(new Color(10, 10, 30, 180));
        g2.fillRoundRect(bx, by, bw, bh, 20, 20);
        g2.setColor(new Color(255, 215, 0, 80));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(bx, by, bw, bh, 20, 20);
        g2.dispose();
    }
}
