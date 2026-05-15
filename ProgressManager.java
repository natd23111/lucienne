package Project;

import java.io.*;
import java.util.Scanner;

public class ProgressManager {
    private static final String FILE_PATH = "savegame.txt";

    // Method to save score - MUST use try-catch for marks
    public void saveProgress(Player player) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(player.getName() + ":" + player.getScore() + ":" + player.getCurrentStoryScene());
            System.out.println("Progress saved successfully!");
        } catch (IOException e) {
            System.err.println("Error saving progress: " + e.getMessage());
        }
    }

    // Method to load score
    public void loadProgress(Player player) {
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
            System.out.println("No save file found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("Progress file corrupted.");
        }
    }
}