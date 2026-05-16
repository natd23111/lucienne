package Project;

import java.util.HashMap;

public class Inventory implements Interactable {
    private Player player;

    public Inventory(Player player) {
        this.player = player;
    }

    @Override
    public void buyItem(String itemName, int cost) {
        if (player.getScore() >= cost) {
            if (itemName.equals("Sage's Scroll") && player.hasSagesScroll()) {
                System.out.println("Already own Sage's Scroll");
                return;
            }
            if (itemName.equals("Sage's Scroll")) {
                player.setSagesScroll(true);
            } else {
                player.addItem(itemName);
            }
            System.out.println("Bought: " + itemName);
        }
    }

    @Override
    public void useItem(String itemName) {
        player.useItem(itemName);
        System.out.println("Used: " + itemName);
    }

    public boolean useKnowledgePotion() { return player.useItem("Knowledge Potion"); }
    public boolean useMemoryCharm() { return player.useItem("Memory Charm"); }
    public int getKnowledgePotionCount() { return player.getItemCount("Knowledge Potion"); }
    public int getMemoryCharmCount() { return player.getItemCount("Memory Charm"); }
    public boolean hasSagesScroll() { return player.hasSagesScroll(); }

    public HashMap<String, Integer> getAllItems() { return new HashMap<>(player.getInventory()); }

    public int getKnowledgePoints() { return player.getScore(); }
}