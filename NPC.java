package Project;

import java.util.ArrayList;
import java.util.List;

public class NPC {
    private String name;
    private int x;
    private int y;
    private String spritePath;
    private List<String> dialogue;

    public NPC(String name, int x, int y, String spritePath) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.spritePath = spritePath;
        this.dialogue = new ArrayList<>();
    }

    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getSpritePath() { return spritePath; }
    public List<String> getDialogue() { return dialogue; }
    public void addDialogue(String line) { dialogue.add(line); }
}
