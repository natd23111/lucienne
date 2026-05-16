package Project;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private String name;
    private int score;
    private String currentStoryScene;
    private Map<String, Integer> storyChoices;
    private Map<String, Integer> inventory;
    private boolean hasSagesScroll;
    private int xp;
    private int level;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.currentStoryScene = "jeff_bedroom";
        this.storyChoices = new HashMap<>();
        this.inventory = new HashMap<>();
        this.hasSagesScroll = false;
        this.xp = 0;
        this.level = 1;
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public void setScore(int score) {
        if (score < 0) {
            this.score = 0;
            System.err.println("Attempted to set score to a negative value. Score set to 0.");
        } else {
            this.score = score;
        }
    }
    public void addScore(int points) { this.score += points; }

    public int getXp() { return xp; }
    public void addXp(int amount) {
        this.xp += amount;
        int xpPerLevel = 15;
        while (this.xp >= xpPerLevel) {
            this.xp -= xpPerLevel;
            this.level++;
        }
    }
    public int getLevel() { return level; }
    public void setXp(int xp) { this.xp = xp; }
    public void setLevel(int level) { this.level = level; }

    public Map<String, Integer> getInventory() { return inventory; }
    public int getItemCount(String itemName) { return inventory.getOrDefault(itemName, 0); }
    public void addItem(String itemName) { inventory.merge(itemName, 1, Integer::sum); }

    public boolean useItem(String itemName) {
        int count = inventory.getOrDefault(itemName, 0);
        if (count > 0) {
            inventory.put(itemName, count - 1);
            if (inventory.get(itemName) == 0) inventory.remove(itemName);
            return true;
        }
        return false;
    }

    public boolean hasSagesScroll() { return hasSagesScroll; }
    public void setSagesScroll(boolean flag) { this.hasSagesScroll = flag; }

    public String getCurrentStoryScene() { return currentStoryScene; }
    public void setCurrentStoryScene(String sceneId) { this.currentStoryScene = sceneId; }
    public Map<String, Integer> getStoryChoices() { return storyChoices; }
    public void recordChoice(String sceneId, int choiceIndex) { storyChoices.put(sceneId, choiceIndex); }
}
