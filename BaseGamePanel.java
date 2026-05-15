package Project;

import javax.swing.*;
import java.awt.*;

public abstract class BaseGamePanel extends JPanel {
    protected CardLayout cardLayout;
    protected JPanel mainPanel;
    protected Player player;

    public BaseGamePanel(CardLayout cardLayout, JPanel mainPanel, Player player) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.player = player;
    }
}