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
        try {
            progressManager.loadProgress(player);
        } catch (GameDataException e) {
            JOptionPane.showMessageDialog(this, "Error loading game progress: " + e.getMessage() + "\nStarting a new game.", "Load Error", JOptionPane.WARNING_MESSAGE);
        }

        setTitle("Lucienne: Quest for Quality Education");
        setSize(360, 640); // Smartphone resolution [cite: 67]
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        BattleEngine engine = new BattleEngine();
        Inventory inventory = new Inventory(player);

        // Use ResourceParser interface
        ResourceParser<Scene> storyParser = new StoryParser();
        Map<String, Scene> scenes = new HashMap<>();
        try {
            scenes = storyParser.parse();
        } catch (GameDataException e) {
            JOptionPane.showMessageDialog(this, "Error loading story data: " + e.getMessage() + "\nGame may not function correctly.", "Data Error", JOptionPane.ERROR_MESSAGE);
        }
        
        ResourceParser<List<Question>> questionParser = new QuestionParser();
        try {
            questionParser.parse(); // Parse questions at startup to catch errors early
        } catch (GameDataException e) {
            JOptionPane.showMessageDialog(this, "Error loading question data: " + e.getMessage() + "\nQuizzes may not function correctly.", "Data Error", JOptionPane.ERROR_MESSAGE);
        }

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