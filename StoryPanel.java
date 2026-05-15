package Project;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class StoryPanel extends BaseGamePanel {
    private Map<String, Scene> storyScenes;
    private JTextArea narrativeArea;
    private JPanel choicePanel;
    private ProgressManager progressManager;
    private VillagePanel villagePanel;

    public StoryPanel(CardLayout cardLayout, JPanel mainPanel, Player player,
            Map<String, Scene> scenes, VillagePanel villagePanel) {
        super(cardLayout, mainPanel, player);
        this.storyScenes = scenes;
        this.villagePanel = villagePanel;
        this.progressManager = new ProgressManager();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        narrativeArea = new JTextArea();
        narrativeArea.setLineWrap(true);
        narrativeArea.setWrapStyleWord(true);
        narrativeArea.setEditable(false);
        narrativeArea.setFont(new Font("Serif", Font.ITALIC, 18));
        narrativeArea.setBackground(getBackground());
        
        JScrollPane scrollPane = new JScrollPane(narrativeArea);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        choicePanel = new JPanel();
        choicePanel.setLayout(new BoxLayout(choicePanel, BoxLayout.Y_AXIS));
        add(choicePanel, BorderLayout.SOUTH);

        displayScene(player.getCurrentStoryScene());
    }

    private void displayScene(String sceneId) {
        Scene scene = storyScenes.get(sceneId);
        if (scene == null) {
            cardLayout.show(mainPanel, "Village");
            villagePanel.requestFocusInWindow();
            return;
        }

        player.setCurrentStoryScene(sceneId);
        narrativeArea.setText(scene.getNarrativeText());
        choicePanel.removeAll();

        if (scene.isEnding()) {
            JButton endBtn = new JButton("Begin Your Adventure");
            endBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            endBtn.addActionListener(e -> {
                try {
                    progressManager.saveProgress(player);
                } catch (GameDataException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving game: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                }
                cardLayout.show(mainPanel, "Village");
                villagePanel.requestFocusInWindow();
            });
            choicePanel.add(endBtn);
        } else {
            int index = 0;
            for (Scene.Choice choice : scene.getChoices()) {
                final int choiceIdx = index++;
                JButton btn = new JButton(choice.getChoiceText());
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                btn.addActionListener(e -> {
                    player.recordChoice(sceneId, choiceIdx);
                    displayScene(choice.getNextSceneId());
                });
                choicePanel.add(btn);
                choicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        choicePanel.revalidate();
        choicePanel.repaint();
    }

    public void resetToCurrent() {
        displayScene(player.getCurrentStoryScene());
    }
}