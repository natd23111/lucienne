package Project;

import java.util.HashMap;

public class Inventory implements Interactable {
    private HashMap<String, Integer> items = new HashMap<>();
    private Player player;

    public Inventory(Player player) {
        this.player = player;
    }

    @Override
    public void buyItem(String itemName, int cost) {
        if (player.getScore() >= cost) {
            // Currency is handled by the Player/Shop logic, 
            // we just add the item to the map here.
            items.put(itemName, items.getOrDefault(itemName, 0) + 1);
            System.out.println("Bought: " + itemName);
        }
    }

    @Override
    public void useItem(String itemName) {
        if (items.getOrDefault(itemName, 0) > 0) {
            items.put(itemName, items.get(itemName) - 1);
            System.out.println("Used: " + itemName);
        }
    }

    public int getKnowledgePoints() {
        return player.getScore();
    }
}