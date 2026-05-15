package Project;

import java.io.*;
import java.util.Scanner;

public class ProgressManager {
    private static final String FILE_PATH = "savegame.txt";

    // Method to save score - MUST use try-catch for marks
    public void saveProgress(Player player) throws GameDataException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(player.getName() + ":" + player.getScore() + ":" + player.getCurrentStoryScene());
            System.out.println("Progress saved successfully!");
        } catch (IOException e) {
            throw new GameDataException("Error saving progress to file: " + FILE_PATH, e);
        }
    }

    // Method to load score
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
            }
        } catch (FileNotFoundException e) {
            throw new GameDataException("No save file found: " + FILE_PATH, e);
        } catch (Exception e) {
            throw new GameDataException("Progress file corrupted: " + FILE_PATH, e);
        }
    }
}