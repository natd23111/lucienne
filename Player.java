package Project;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private String name;
    private int score;
    private String currentStoryScene;
    private Map<String, Integer> storyChoices;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.currentStoryScene = "intro_arrival"; // Default starting scene
        this.storyChoices = new HashMap<>();
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public void setScore(int score) {
        if (score < 0) {
            this.score = 0; // Prevent negative scores
            System.err.println("Attempted to set score to a negative value. Score set to 0.");
        } else {
            this.score = score;
        }
    }
    public void addScore(int points) { this.score += points; }

    public String getCurrentStoryScene() { return currentStoryScene; }
    public void setCurrentStoryScene(String sceneId) { this.currentStoryScene = sceneId; }
    public Map<String, Integer> getStoryChoices() { return storyChoices; }
    public void recordChoice(String sceneId, int choiceIndex) { storyChoices.put(sceneId, choiceIndex); }
}
