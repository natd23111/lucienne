package Project;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class LucienneGame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Player player;
    private ProgressManager progressManager;

    public LucienneGame() {
        // Load saved progress
        progressManager = new ProgressManager();
        player = new Player("Jeff"); // Default player name
        progressManager.loadProgress(player);

        setTitle("Lucienne: Quest for Quality Education");
        setSize(360, 640); // Smartphone resolution [cite: 67]
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        BattleEngine engine = new BattleEngine();
        Inventory inventory = new Inventory(player);
        StoryParser storyParser = new StoryParser();
        Map<String, Scene> scenes = storyParser.parseStory();

        VillagePanel villagePanel = new VillagePanel(cardLayout, mainPanel, player);
        BattlePanel battlePanel = new BattlePanel(cardLayout, mainPanel, player, engine, villagePanel);
        ShopPanel shopPanel = new ShopPanel(cardLayout, mainPanel, player, inventory, villagePanel);
        StoryPanel storyPanel = new StoryPanel(cardLayout, mainPanel, player, scenes);

        // Add Screens here
        mainPanel.add(createWelcomeScreen(), "Welcome");
        mainPanel.add(villagePanel, "Village");

        // Registering game screens
        mainPanel.add(new LearningPanel(cardLayout, mainPanel, battlePanel), "KnowledgeGarden");
        mainPanel.add(battlePanel, "BattleGround");
        mainPanel.add(shopPanel, "VillageShop");
        mainPanel.add(storyPanel, "Story");
        
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createWelcomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Welcome to Lucienne", SwingConstants.CENTER);
        JButton startBtn = new JButton("Start Journey");
        
        // This is how you switch screens
        startBtn.addActionListener(e -> cardLayout.show(mainPanel, "Story"));
        
        panel.add(label, BorderLayout.CENTER);
        panel.add(startBtn, BorderLayout.SOUTH);
        return panel;
    }

    public static void main(String[] args) {
        // Run from command line as required [cite: 99]
        SwingUtilities.invokeLater(() -> new LucienneGame());
    }
}