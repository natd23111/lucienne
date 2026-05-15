package Project;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BattlePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Player player;
    private BattleEngine battleEngine;
    private ProgressManager progressManager;
    private VillagePanel villagePanel;
    
    private JLabel questionLabel;
    private JPanel optionsPanel;
    private List<Question> currentQuestions;
    private int currentQuestionIndex;
    private int correctAnswersCount;

    public BattlePanel(CardLayout cardLayout, JPanel mainPanel, Player player, BattleEngine engine, VillagePanel villagePanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.player = player;
        this.battleEngine = engine;
        this.progressManager = new ProgressManager();
        this.villagePanel = villagePanel;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        questionLabel = new JLabel("Prepare for Battle!", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Serif", Font.BOLD, 18));
        add(questionLabel, BorderLayout.NORTH);

        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        add(optionsPanel, BorderLayout.CENTER);

        JButton fleeBtn = new JButton("Flee to Village");
        fleeBtn.addActionListener(e -> cardLayout.show(mainPanel, "Village"));
        add(fleeBtn, BorderLayout.SOUTH);
    }

    public void startBattle(List<Question> questions) {
        this.currentQuestions = questions;
        this.currentQuestionIndex = 0;
        this.correctAnswersCount = 0;
        showNextQuestion();
    }

    private void showNextQuestion() {
        optionsPanel.removeAll();
        if (currentQuestionIndex < currentQuestions.size()) {
            Question q = currentQuestions.get(currentQuestionIndex);
            questionLabel.setText("<html><div style='text-align: center;'>" + q.getQuestionText() + "</div></html>");

            List<String> options = q.getOptions();
            if (options.isEmpty()) { 
                options = java.util.Arrays.asList("True", "False");
            }

            for (String option : options) {
                JButton btn = new JButton(option);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                btn.addActionListener(e -> handleAnswer(q, option));
                
                optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                optionsPanel.add(btn);
            }
        } else {
            finishBattle();
        }
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void handleAnswer(Question q, String selected) {
        if (q.checkAnswer(selected)) {
            correctAnswersCount++;
            player.addScore(10);
            JOptionPane.showMessageDialog(this, "Correct! Knowledge fragment restored.");
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect. The fragment grows stronger...");
        }
        currentQuestionIndex++;
        showNextQuestion();
    }

    private void finishBattle() {
        String message = battleEngine.getMotivationalMessage(correctAnswersCount, currentQuestions.size());
        JOptionPane.showMessageDialog(this, "Battle Result: " + correctAnswersCount + "/" + currentQuestions.size() + "\n" + message);
        progressManager.saveProgress(player);
        villagePanel.updateDisplay();
        cardLayout.show(mainPanel, "Village");
    }
}