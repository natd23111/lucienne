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
    private SoundManager soundManager;
    private JPanel shopContainer;
    private JLabel kpLabel;
    private List<ShopItem> shopItems;
    private boolean sortByName = true;

    public ShopPanel(CardLayout cardLayout, JPanel mainPanel, Player player, Inventory inventory,
            VillagePanel villagePanel, SoundManager soundManager) {
        super(cardLayout, mainPanel, player);
        this.inventory = inventory;
        this.progressManager = new ProgressManager();
        this.villagePanel = villagePanel;
        this.soundManager = soundManager;

        setBackgroundImage("assets/shop_bg.png");
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Village Shop", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        shopContainer = new JPanel();
        shopContainer.setLayout(new BoxLayout(shopContainer, BoxLayout.Y_AXIS));

        shopItems = new ArrayList<>();
        shopItems.add(new ShopItem("Memory Charm", 50,
                "Eliminates 2 wrong answers in battle", "assets/icon_charm.png"));
        shopItems.add(new ShopItem("Knowledge Potion", 30,
                "Auto-corrects one quiz question", "assets/icon_potion.png"));
        shopItems.add(new ShopItem("Sage's Scroll", 100,
                "+2 bonus KP per correct answer (permanent)", "assets/icon_sages_scroll.png"));

        displayShopItems();

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

    private static class ShopItem {
        String name;
        int price;
        String description;
        String iconPath;

        ShopItem(String name, int price, String description, String iconPath) {
            this.name = name;
            this.price = price;
            this.description = description;
            this.iconPath = iconPath;
        }

        String getName() { return name; }
        int getPrice() { return price; }
        String getDescription() { return description; }
        String getIconPath() { return iconPath; }
    }

    private void displayShopItems() {
        sortAndRedisplayItems();
    }

    private void sortAndRedisplayItems() {
        Component[] components = shopContainer.getComponents();
        for (Component comp : components) {
            shopContainer.remove(comp);
        }

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

        if (sortByName) {
            Collections.sort(shopItems, Comparator.comparing(ShopItem::getName));
        } else {
            Collections.sort(shopItems, Comparator.comparingInt(ShopItem::getPrice));
        }

        for (ShopItem item : shopItems) {
            addShopItemCard(item);
        }
        shopContainer.revalidate();
        shopContainer.repaint();
    }

    private void addShopItemCard(ShopItem item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 120), 1));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        card.setOpaque(false);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        header.setOpaque(false);

        ImageIcon icon = new ImageIcon(item.getIconPath());
        Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaled));
        header.add(iconLabel);

        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Serif", Font.BOLD, 14));
        header.add(nameLabel);
        card.add(header);

        JLabel descLabel = new JLabel("<html><div style='text-align:center;color:#aaa;'>"
                + item.getDescription() + "</div></html>");
        descLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String ownedText = "";
        if (item.getName().equals("Sage's Scroll") && player.hasSagesScroll()) {
            ownedText = " [OWNED]";
        } else if (player.getItemCount(item.getName()) > 0) {
            ownedText = " (x" + player.getItemCount(item.getName()) + " owned)";
        }

        JButton buyButton = new JButton(item.getPrice() + " KP" + ownedText);
        buyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        buyButton.addActionListener(e -> {
            if (item.getName().equals("Sage's Scroll") && player.hasSagesScroll()) {
                JOptionPane.showMessageDialog(this, "You already own the Sage's Scroll!");
                return;
            }
            if (player.getScore() >= item.getPrice()) {
                player.setScore(player.getScore() - item.getPrice());
                inventory.buyItem(item.getName(), item.getPrice());
                kpLabel.setText("Current KP: " + player.getScore());
                villagePanel.updateDisplay();
                soundManager.playPurchase();
                JOptionPane.showMessageDialog(this, "Purchased " + item.getName() + "!");
                try {
                    progressManager.saveProgress(player);
                } catch (GameDataException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving game: " + ex.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                }
                sortAndRedisplayItems();
            } else {
                JOptionPane.showMessageDialog(this, "Not enough Knowledge Points!");
            }
        });

        card.add(nameLabel);
        card.add(descLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(buyButton);

        shopContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        shopContainer.add(card);
    }
}