package Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StoryParser implements ResourceParser<Scene> {
    private static final String FILE_PATH = "story.txt";

    @Override
    public Map<String, Scene> parse() throws GameDataException {
        Map<String, Scene> scenes = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            Scene currentScene = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("Scene:")) {
                    String id = line.substring("Scene:".length()).trim();
                    currentScene = new Scene(id, "");
                    scenes.put(id, currentScene);
                } else if (line.startsWith("Text:") && currentScene != null) {
                    String text = line.substring("Text:".length()).trim();
                    // Handle explicit newlines in the text file
                    text = text.replace("\\n", "\n");
                    // In a real scenario, you might want to builder multiple lines of text
                    currentScene = new Scene(currentScene.getSceneId(), text);
                    scenes.put(currentScene.getSceneId(), currentScene);
                } else if (line.startsWith("Choice") && currentScene != null) {
                    int arrowIndex = line.indexOf("->");
                    int colonIndex = line.indexOf(":");
                    if (arrowIndex != -1 && colonIndex != -1) {
                        String choiceText = line.substring(colonIndex + 1, arrowIndex).trim();
                        // Remove quotes
                        if (choiceText.startsWith("\"") && choiceText.endsWith("\"")) {
                            choiceText = choiceText.substring(1, choiceText.length() - 1);
                        }
                        String nextSceneId = line.substring(arrowIndex + 2).trim();
                        currentScene.addChoice(choiceText, nextSceneId);
                    }
                } else if (line.startsWith("End:") && currentScene != null) {
                    currentScene.setEnding(true);
                }
            }
        } catch (IOException e) {
            throw new GameDataException("Error reading story file: " + FILE_PATH, e);
        } catch (Exception e) { // Catch any other unexpected parsing errors
            throw new GameDataException("Error parsing story file: " + FILE_PATH, e);
        }
        return scenes;
    }
}