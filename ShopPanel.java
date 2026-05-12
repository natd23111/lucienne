package Project;

import javax.swing.*;
import java.awt.*;

public class ShopPanel extends JPanel {
    public ShopPanel(CardLayout cardLayout, JPanel mainPanel) {
        setLayout(new BorderLayout());
        add(new JLabel("Village Shop - Coming Soon!", SwingConstants.CENTER), BorderLayout.CENTER);
        JButton backBtn = new JButton("Back to Village");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Village"));
        add(backBtn, BorderLayout.SOUTH);
    }
}