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
        // Normalize answers for comparison (e.g., trim whitespace, ignore case for True/False)
        if ("True/False".equalsIgnoreCase(type)) {
            return correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
        }
        return correctAnswer.trim().equals(userAnswer.trim());
    }

    @Override
    public String toString() {
        return "Category: " + category + ", Type: " + type + ", Question: " + questionText;
    }
}