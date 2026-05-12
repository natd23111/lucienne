package Project;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class LearningPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private QuestionParser parser;

    public LearningPanel(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
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
        Map<String, List<Question>> categorizedQuestions = parser.parseQuestions();

        for (String category : categorizedQuestions.keySet()) {
            JButton categoryBtn = new JButton(category);
            categoryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            categoryBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            
            categoryBtn.addActionListener(e -> {
                List<Question> questionsForCategory = categorizedQuestions.get(category);
                // Transition logic: You can now pass 'questionsForCategory' to the BattlePanel
                JOptionPane.showMessageDialog(this, "Studying: " + category + "\nQuestions found: " + questionsForCategory.size());
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