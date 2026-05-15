package Project;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    private String sceneId;
    private String narrativeText;
    private List<Choice> choices;
    private boolean isEnding;

    public Scene(String sceneId, String narrativeText) {
        this.sceneId = sceneId;
        this.narrativeText = narrativeText;
        this.choices = new ArrayList<>();
        this.isEnding = false;
    }

    public static class Choice {
        private String choiceText;
        private String nextSceneId;

        public Choice(String choiceText, String nextSceneId) {
            this.choiceText = choiceText;
            this.nextSceneId = nextSceneId;
        }

        public String getChoiceText() {
            return choiceText;
        }

        public String getNextSceneId() {
            return nextSceneId;
        }
    }

    public String getSceneId() {
        return sceneId;
    }

    public String getNarrativeText() {
        return narrativeText;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void addChoice(String choiceText, String nextSceneId) {
        choices.add(new Choice(choiceText, nextSceneId));
    }

    public boolean isEnding() {
        return isEnding;
    }

    public void setEnding(boolean ending) {
        isEnding = ending;
    }
}
