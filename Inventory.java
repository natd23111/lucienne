import java.util.HashMap;

public class Inventory implements Interactable {
    private HashMap<String, Integer> items = new HashMap<>();
    private int knowledgePoints = 100; // Starting currency

    @Override
    public void buyItem(String itemName, int cost) {
        if (knowledgePoints >= cost) {
            knowledgePoints -= cost;
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
}