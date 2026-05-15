package Project;

import javax.swing.*;
import java.awt.*;

public abstract class BaseGamePanel extends JPanel {
    protected CardLayout cardLayout;
    protected JPanel mainPanel;
    protected Player player;
    protected Image backgroundImage;

    public BaseGamePanel(CardLayout cardLayout, JPanel mainPanel, Player player) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.player = player;
        setOpaque(false); // Allows background images to show through
    }

    protected void setBackgroundImage(String fileName) {
        this.backgroundImage = new ImageIcon(fileName).getImage();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}