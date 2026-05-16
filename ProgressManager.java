package Project;

import java.io.*;
import java.util.Map;
import java.util.Scanner;

public class ProgressManager {
    private static final String FILE_PATH = "savegame.txt";

    public void saveProgress(Player player) throws GameDataException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            StringBuilder invPart = new StringBuilder();
            Map<String, Integer> inv = player.getInventory();
            if (!inv.isEmpty()) {
                for (Map.Entry<String, Integer> entry : inv.entrySet()) {
                    if (invPart.length() > 0) invPart.append(",");
                    invPart.append(entry.getKey()).append("-").append(entry.getValue());
                }
            }
            String invStr = invPart.length() > 0 ? invPart.toString() : "none";

            writer.write(player.getName() + ":" +
                    player.getScore() + ":" +
                    player.getCurrentStoryScene() + ":" +
                    invStr + ":" +
                    player.hasSagesScroll() + ":" +
                    player.getXp() + ":" +
                    player.getLevel());
            System.out.println("Progress saved successfully!");
        } catch (IOException e) {
            throw new GameDataException("Error saving progress to file: " + FILE_PATH, e);
        }
    }

    public void loadProgress(Player player) throws GameDataException {
        try (Scanner scanner = new Scanner(new File(FILE_PATH))) {
            if (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(":");
                if (parts.length >= 2) {
                    player.setScore(Integer.parseInt(parts[1]));
                }
                if (parts.length >= 3) {
                    player.setCurrentStoryScene(parts[2]);
                }
                if (parts.length >= 4 && !parts[3].equals("none")) {
                    String[] items = parts[3].split(",");
                    for (String itemEntry : items) {
                        String[] kv = itemEntry.split("-");
                        if (kv.length == 2) {
                            int count = Integer.parseInt(kv[1]);
                            for (int i = 0; i < count; i++) {
                                player.addItem(kv[0]);
                            }
                        }
                    }
                }
                if (parts.length >= 5) {
                    player.setSagesScroll(Boolean.parseBoolean(parts[4]));
                }
                if (parts.length >= 6) {
                    player.setXp(Integer.parseInt(parts[5]));
                }
                if (parts.length >= 7) {
                    player.setLevel(Integer.parseInt(parts[6]));
                }
            }
        } catch (FileNotFoundException e) {
            throw new GameDataException("No save file found: " + FILE_PATH, e);
        } catch (Exception e) {
            throw new GameDataException("Progress file corrupted: " + FILE_PATH, e);
        }
    }
}