import java.io.*;
import java.util.Scanner;

public class ProgressManager {
    private static final String FILE_PATH = "savegame.txt";

    // Method to save score - MUST use try-catch for marks
    public void saveScore(String playerName, int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(playerName + ":" + score);
            System.out.println("Progress saved successfully!");
        } catch (IOException e) {
            System.err.println("Error saving progress: " + e.getMessage());
        }
    }

    // Method to load score
    public int loadScore() {
        try (Scanner scanner = new Scanner(new File(FILE_PATH))) {
            if (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(":");
                return Integer.parseInt(parts[1]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No save file found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("Progress file corrupted.");
        }
        return 0;
    }
}