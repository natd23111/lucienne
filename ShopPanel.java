package Project;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShopPanel extends BaseGamePanel {
    private Inventory inventory;
    private ProgressManager progressManager;
    private VillagePanel villagePanel;
    private JPanel shopContainer;
    private JLabel kpLabel;
    private List<ShopItem> shopItems; // To hold items for sorting
    private boolean sortByName = true; // Default sort order

    public ShopPanel(CardLayout cardLayout, JPanel mainPanel, Player player, Inventory inventory, VillagePanel villagePanel) {
        super(cardLayout, mainPanel, player);
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
        
        shopItems = new ArrayList<>();
        shopItems.add(new ShopItem("Memory Charm", 50));
        shopItems.add(new ShopItem("Knowledge Potion", 30));
        shopItems.add(new ShopItem("Sage's Scroll", 100));

        displayShopItems(); // Initial display

        add(new JScrollPane(shopContainer), BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(2, 1, 5, 5));
        kpLabel = new JLabel("Current KP: " + player.getScore(), SwingConstants.CENTER);
        JButton backBtn = new JButton("Back to Village");
        backBtn.addActionListener(e -> {
            cardLayout.show(mainPanel, "Village");
            villagePanel.requestFocusInWindow();
        });
        
        footer.add(kpLabel);
        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    // Inner class to represent a shop item
    private static class ShopItem {
        String name;
        int price;

        public ShopItem(String name, int price) {
            this.name = name;
            this.price = price;
        }

        public String getName() { return name; }
        public int getPrice() { return price; }
    }

    private void displayShopItems() {
        shopContainer.removeAll();

        // Add sorting controls
        JPanel sortPanel = new JPanel();
        JButton sortByNameBtn = new JButton("Sort by Name");
        sortByNameBtn.addActionListener(e -> {
            sortByName = true;
            sortAndRedisplayItems();
        });
        JButton sortByPriceBtn = new JButton("Sort by Price");
        sortByPriceBtn.addActionListener(e -> {
            sortByName = false;
            sortAndRedisplayItems();
        });
        sortPanel.add(sortByNameBtn);
        sortPanel.add(sortByPriceBtn);
        shopContainer.add(sortPanel);
        shopContainer.add(Box.createRigidArea(new Dimension(0, 10)));

        // Sort items before displaying
        sortAndRedisplayItems();
    }

    private void sortAndRedisplayItems() {
        // Remove only item buttons, keep sort panel
        for (Component comp : shopContainer.getComponents()) {
            if (comp instanceof JButton || comp instanceof Box.Filler) { // Assuming Box.Filler is for rigid areas
                shopContainer.remove(comp);
            }
        }

        if (sortByName) {
            Collections.sort(shopItems, Comparator.comparing(ShopItem::getName));
        } else {
            Collections.sort(shopItems, Comparator.comparingInt(ShopItem::getPrice));
        }

        for (ShopItem item : shopItems) {
            addShopItemButton(item.getName(), item.getPrice());
        }
        shopContainer.revalidate();
        shopContainer.repaint();
    }

    private void addShopItemButton(String itemName, int price) {
        JButton buyButton = new JButton(itemName + " - " + price + " KP"); // Use a final variable for lambda
        buyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        buyButton.addActionListener(e -> {
            if (player.getScore() >= price) {
                player.setScore(player.getScore() - price);
                inventory.buyItem(itemName, price);
                kpLabel.setText("Current KP: " + player.getScore());
                villagePanel.updateDisplay();
                JOptionPane.showMessageDialog(this, "Purchased " + itemName + "!");
                try {
                    progressManager.saveProgress(player);
                } catch (GameDataException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving game: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Not enough Knowledge Points!");
            }
        });
        
        shopContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        shopContainer.add(buyButton);
    }
}