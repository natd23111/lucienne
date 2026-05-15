package Project;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LearningPanel extends BaseGamePanel {
    private BattlePanel battlePanel;
    private QuestionParser parser;

    public LearningPanel(CardLayout cardLayout, JPanel mainPanel, Player player, BattlePanel battlePanel) {
        super(cardLayout, mainPanel, player);
        this.battlePanel = battlePanel;
        this.parser = new QuestionParser();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header for the Knowledge Garden
        JLabel titleLabel = new JLabel("Knowledge Garden", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // Container for category buttons
        JPanel categoryContainer = new JPanel();
        categoryContainer.setLayout(new BoxLayout(categoryContainer, BoxLayout.Y_AXIS));

        // Parse questions and create a button for each unique category
        Map<String, List<Question>> questionsMap;
        try {
            questionsMap = parser.parse();
        } catch (GameDataException e) {
            JOptionPane.showMessageDialog(this, "Error loading questions: " + e.getMessage());
            questionsMap = new HashMap<>();
        }
        final Map<String, List<Question>> categorizedQuestions = questionsMap;

        for (String category : categorizedQuestions.keySet()) {
            final String categoryName = category; // Explicitly capture for lambda
            JButton categoryBtn = new JButton(categoryName);
            categoryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            categoryBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            
            categoryBtn.addActionListener(e -> {
                List<Question> questionsForCategory = categorizedQuestions.get(categoryName);
                battlePanel.startBattle(questionsForCategory);
                cardLayout.show(mainPanel, "BattleGround");
            });

            categoryContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            categoryContainer.add(categoryBtn);
        }

        JScrollPane scrollPane = new JScrollPane(categoryContainer);
        add(scrollPane, BorderLayout.CENTER);

        JButton backBtn = new JButton("Back to Village");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Village"));
        add(backBtn, BorderLayout.SOUTH);
    }
}