package Project;

import java.util.List;
import java.util.ArrayList;

public class Question {
    private String category;
    private String type; // e.g., "Multiple Choice", "True/False"
    private String questionText;
    private List<String> options; // For multiple choice
    private String correctAnswer;

    public Question(String category, String type, String questionText, String correctAnswer) {
        this.category = category;
        this.type = type;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.options = new ArrayList<>(); // Initialize even if not used for True/False
    }

    // Constructor for Multiple Choice questions
    public Question(String category, String type, String questionText, List<String> options, String correctAnswer) {
        this.category = category;
        this.type = type;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public boolean checkAnswer(String userAnswer) {
        String user = userAnswer.trim();
        String correct = correctAnswer.trim();

        if ("Multiple Choice".equalsIgnoreCase(type)) {
            // Check if user clicked a button like "A) Text" when correct is "A"
            // or if the button text matches the answer key exactly.
            return user.equalsIgnoreCase(correct) || user.startsWith(correct + ")");
        }

        // For True/False, handle cases like "False (explanation...)" matching "False"
        return correct.toLowerCase().startsWith(user.toLowerCase());
    }

    @Override
    public String toString() {
        return "Category: " + category + ", Type: " + type + ", Question: " + questionText;
    }
}