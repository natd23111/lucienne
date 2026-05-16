package Project;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LucienneGame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Player player;
    private ProgressManager progressManager;

    public LucienneGame() {
        progressManager = new ProgressManager();
        player = new Player("Jeff");
        try {
            progressManager.loadProgress(player);
        } catch (GameDataException e) {
            JOptionPane.showMessageDialog(this, "Error loading game progress: " + e.getMessage()
                    + "\nStarting a new game.", "Load Error", JOptionPane.WARNING_MESSAGE);
        }

        setTitle("Lucienne: Quest for Quality Education");
        setSize(360, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        SoundManager soundManager = new SoundManager();
        BattleEngine engine = new BattleEngine();
        Inventory inventory = new Inventory(player);
        QuestionParser questionParser = new QuestionParser();

        ResourceParser<Scene> storyParser = new StoryParser();
        Map<String, Scene> scenes = new HashMap<>();
        try {
            scenes = storyParser.parse();
        } catch (GameDataException e) {
            JOptionPane.showMessageDialog(this, "Error loading story data: " + e.getMessage()
                    + "\nGame may not function correctly.", "Data Error", JOptionPane.ERROR_MESSAGE);
        }

        try {
            questionParser.parse();
        } catch (GameDataException e) {
            JOptionPane.showMessageDialog(this, "Error loading question data: " + e.getMessage()
                    + "\nQuizzes may not function correctly.", "Data Error", JOptionPane.ERROR_MESSAGE);
        }

        VillagePanel villagePanel = new VillagePanel(cardLayout, mainPanel, player, soundManager);
        BattlePanel battlePanel = new BattlePanel(cardLayout, mainPanel, player, engine,
                villagePanel, soundManager, inventory);
        BattleGroundPanel battleGroundPanel = new BattleGroundPanel(cardLayout, mainPanel, player,
                villagePanel, battlePanel, soundManager, questionParser);
        ShopPanel shopPanel = new ShopPanel(cardLayout, mainPanel, player, inventory,
                villagePanel, soundManager);
        StoryPanel storyPanel = new StoryPanel(cardLayout, mainPanel, player, scenes, villagePanel);
        CodexPanel codexPanel = new CodexPanel(cardLayout, mainPanel, player, villagePanel);
        WelcomePanel welcomePanel = new WelcomePanel(cardLayout, mainPanel, player,
                villagePanel, storyPanel);

        mainPanel.add(welcomePanel, "Welcome");
        mainPanel.add(villagePanel, "Village");
        mainPanel.add(new LearningPanel(cardLayout, mainPanel, player, battlePanel, villagePanel),
                "KnowledgeGarden");
        mainPanel.add(battleGroundPanel, "BattleGround");
        mainPanel.add(battlePanel, "BattleQuiz");
        mainPanel.add(shopPanel, "VillageShop");
        mainPanel.add(codexPanel, "Codex");
        mainPanel.add(storyPanel, "Story");

        add(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LucienneGame());
    }
}