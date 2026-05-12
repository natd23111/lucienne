package Project;

import javax.swing.*;
import java.awt.*;

public class ShopPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Player player;
    private Inventory inventory;
    private ProgressManager progressManager;
    private VillagePanel villagePanel;
    private JPanel shopContainer;
    private JLabel kpLabel;

    public ShopPanel(CardLayout cardLayout, JPanel mainPanel, Player player, Inventory inventory, VillagePanel villagePanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.player = player;
        this.inventory = inventory;
        this.progressManager = new ProgressManager();
        this.villagePanel = villagePanel;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Village Shop", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        shopContainer = new JPanel();
        shopContainer.setLayout(new BoxLayout(shopContainer, BoxLayout.Y_AXIS));
        
        setupShopItem("Memory Charm", 50);
        setupShopItem("Knowledge Potion", 30);
        setupShopItem("Sage's Scroll", 100);

        add(new JScrollPane(shopContainer), BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(2, 1, 5, 5));
        kpLabel = new JLabel("Current KP: " + player.getScore(), SwingConstants.CENTER);
        JButton backBtn = new JButton("Back to Village");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Village"));
        
        footer.add(kpLabel);
        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void setupShopItem(String itemName, int price) {
        JButton buyButton = new JButton(itemName + " - " + price + " KP");
        buyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        buyButton.addActionListener(e -> {
            if (player.getScore() >= price) {
                player.setScore(player.getScore() - price); // Deduct once here
                inventory.buyItem(itemName, price);
                kpLabel.setText("Current KP: " + player.getScore());
                villagePanel.updateDisplay();
                JOptionPane.showMessageDialog(this, "Purchased " + itemName + "!");
                progressManager.saveScore(player.getName(), player.getScore());
            } else {
                JOptionPane.showMessageDialog(this, "Not enough Knowledge Points!");
            }
        });
        
        shopContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        shopContainer.add(buyButton);
    }
}