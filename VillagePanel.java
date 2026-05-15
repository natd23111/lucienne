package Project;

import javax.swing.*;
import java.awt.*;

public class VillagePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Player player;
    private JLabel scoreLabel;

    public VillagePanel(CardLayout cardLayout, JPanel mainPanel, Player player) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.player = player;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Progress Visualization Header
        JPanel statsPanel = new JPanel(new GridLayout(2, 1));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Guardian Status"));
        
        JLabel nameLabel = new JLabel("Hero: " + player.getName());
        scoreLabel = new JLabel("Knowledge Points: " + player.getScore());
        
        statsPanel.add(nameLabel);
        statsPanel.add(scoreLabel);
        add(statsPanel, BorderLayout.NORTH);

        // Navigation Menu
        JPanel menuPanel = new JPanel(new GridLayout(4, 1, 10, 10));

        JButton knowledgeGardenBtn = new JButton("Explore Knowledge Garden");
        knowledgeGardenBtn.addActionListener(e -> cardLayout.show(mainPanel, "KnowledgeGarden"));
        menuPanel.add(knowledgeGardenBtn);

        JButton battleGroundBtn = new JButton("Enter Battle Ground");
        battleGroundBtn.addActionListener(e -> cardLayout.show(mainPanel, "BattleGround"));
        menuPanel.add(battleGroundBtn);

        JButton villageShopBtn = new JButton("Visit Village Shop");
        villageShopBtn.addActionListener(e -> cardLayout.show(mainPanel, "VillageShop"));
        menuPanel.add(villageShopBtn);

        JButton saveGameBtn = new JButton("Save Game");
        saveGameBtn.addActionListener(e -> {
            try {
                new ProgressManager().saveProgress(player);
            } catch (GameDataException ex) {
                JOptionPane.showMessageDialog(this, "Error saving game: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(this, "Progress Saved to savegame.txt!");
        });
        menuPanel.add(saveGameBtn);

        add(menuPanel, BorderLayout.CENTER);
    }

    // Refresh visual progress (call this when returning to the village)
    public void updateDisplay() {
        scoreLabel.setText("Knowledge Points: " + player.getScore());
    }
}