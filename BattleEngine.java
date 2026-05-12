package Project;

public class BattleEngine {
    public String getMotivationalMessage(int score, int totalQuestions) {
        double percentage = ((double) score / totalQuestions) * 100;

        if (percentage >= 80) {
            return "Outstanding! You are a true Guardian of Wisdom!";
        } else if (percentage >= 50) {
            return "Good job! Lucienne's light is growing stronger.";
        } else if (percentage >= 20) {
            return "Keep trying! The Memory Fragments are still strong.";
        } else {
            return "Don't give up! Education is a journey, not a race.";
        }
    }
}