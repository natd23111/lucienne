package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ShopPanel extends BaseGamePanel {
    private Inventory inventory;
    private ProgressManager progressManager;
    private VillagePanel villagePanel;
    private SoundManager soundManager;
    private JPanel shopContainer;
    private JLabel kpLabel;
    private List<ShopItem> shopItems;

    private static final Color GOLD = new Color(255, 215, 0);
    private static final Color CARD_BG = new Color(12, 10, 34, 210);
    private static final Color GOLD_DIM = new Color(255, 215, 0, 50);

    public ShopPanel(CardLayout cardLayout, JPanel mainPanel, Player player, Inventory inventory,
            VillagePanel villagePanel, SoundManager soundManager) {
        super(cardLayout, mainPanel, player);
        this.inventory = inventory;
        this.progressManager = new ProgressManager();
        this.villagePanel = villagePanel;
        this.soundManager = soundManager;

        setBackgroundImage("assets/shop_bg.png");
        setLayout(new BorderLayout());
        setOpaque(true);

        add(createHeader(), BorderLayout.NORTH);

        shopContainer = new JPanel();
        shopContainer.setLayout(new BoxLayout(shopContainer, BoxLayout.Y_AXIS));
        shopContainer.setOpaque(false);

        shopItems = new ArrayList<>();
        shopItems.add(new ShopItem("Memory Charm", 50,
                "Eliminates 2 wrong answers in battle", "assets/icon_charm.png"));
        shopItems.add(new ShopItem("Knowledge Potion", 30,
                "Auto-corrects one quiz question", "assets/icon_potion.png"));
        shopItems.add(new ShopItem("Sage's Scroll", 100,
                "+2 bonus KP per correct answer", "assets/icon_sages_scroll.png"));

        displayShopItems();

        JScrollPane scrollPane = new JScrollPane(shopContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        centerWrap.add(scrollPane, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 10, 16));

        JLabel title = new JLabel("Village Shop", SwingConstants.CENTER);
        title.setForeground(GOLD);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        header.add(title, BorderLayout.CENTER);

        JLabel kpTop = new JLabel("<html><b>KP: " + player.getScore() + "</b></html>", SwingConstants.RIGHT);
        kpTop.setForeground(GOLD);
        kpTop.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.add(kpTop, BorderLayout.EAST);
        this.kpLabel = kpTop;

        return header;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));

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
        footer.add(backBtn);

        return footer;
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
        shopContainer.removeAll();

        for (ShopItem item : shopItems) {
            addShopItemCard(item);
        }
        shopContainer.revalidate();
        shopContainer.repaint();
    }

    private void addShopItemCard(ShopItem item) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(GOLD_DIM);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(320, 130));
        card.setMaximumSize(new Dimension(320, 130));
        card.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);

        ImageIcon icon = new ImageIcon(item.getIconPath());
        Image scaled = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaled));
        header.add(iconLabel);

        JLabel nameLabel = new JLabel("<html><div style='text-align:left;width:150px;'>"
                + item.getName() + "</div></html>");
        nameLabel.setForeground(GOLD);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 15));
        header.add(nameLabel);

        JLabel priceLabel = new JLabel(item.getPrice() + " KP");
        priceLabel.setForeground(new Color(200, 180, 140));
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.add(priceLabel);

        card.add(header);

        JLabel descLabel = new JLabel("<html><div style='text-align:left;color:#b0a8c0;padding:2px 0 2px 10px;width:290px;'>"
                + item.getDescription() + "</div></html>");
        descLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLabel);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);

        String ownedText = "";
        final boolean canBuy;
        if (item.getName().equals("Sage's Scroll") && player.hasSagesScroll()) {
            ownedText = " [OWNED]";
            canBuy = false;
        } else if (player.getItemCount(item.getName()) > 0) {
            ownedText = " (x" + player.getItemCount(item.getName()) + " owned)";
            canBuy = true;
        } else {
            canBuy = true;
        }

        JButton buyButton = new JButton(canBuy ? "Buy" + ownedText : ownedText.trim());
        buyButton.setFont(new Font("Serif", Font.BOLD, 12));
        buyButton.setForeground(canBuy ? new Color(140, 220, 140) : new Color(120, 120, 120));
        buyButton.setOpaque(false);
        buyButton.setContentAreaFilled(false);
        buyButton.setBorderPainted(false);
        buyButton.setFocusPainted(false);
        buyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buyButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (canBuy) buyButton.setForeground(Color.WHITE);
            }
            public void mouseExited(MouseEvent e) {
                buyButton.setForeground(canBuy ? new Color(140, 220, 140) : new Color(120, 120, 120));
            }
        });

        buyButton.addActionListener(e -> {
            if (!canBuy) {
                JOptionPane.showMessageDialog(this, "You already own the Sage's Scroll!");
                return;
            }
            if (player.getScore() >= item.getPrice()) {
                player.setScore(player.getScore() - item.getPrice());
                inventory.buyItem(item.getName(), item.getPrice());
                kpLabel.setText("<html><b>KP: " + player.getScore() + "</b></html>");
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

        btnRow.add(buyButton);
        card.add(btnRow);

        shopContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        shopContainer.add(card);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) { g2.dispose(); return; }

        int headerBottom = 55;
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, w, headerBottom);
        g2.setColor(GOLD_DIM);
        g2.drawLine(40, headerBottom, w - 40, headerBottom);

        g2.dispose();
    }
}
