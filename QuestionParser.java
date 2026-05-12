package Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionParser {
    private static final String FILE_PATH = "questions.txt";

    public Map<String, List<Question>> parseQuestions() {
        Map<String, List<Question>> categorizedQuestions = new HashMap<>();
        String currentCategory = "Default"; // A default category if none is specified initially

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("Category:")) {
                    currentCategory = line.substring("Category:".length()).trim();
                    categorizedQuestions.putIfAbsent(currentCategory, new ArrayList<>());
                } else if (line.startsWith("Multiple Choice:") || line.startsWith("True/False:")) {
                    String type;
                    String questionText;
                    if (line.startsWith("Multiple Choice:")) {
                        type = "Multiple Choice";
                        questionText = line.substring("Multiple Choice:".length()).trim();
                    } else { // True/False
                        type = "True/False";
                        questionText = line.substring("True/False:".length()).trim();
                    }

                    List<String> options = new ArrayList<>();
                    String answerLine = null;

                    // Read options for Multiple Choice
                    if ("Multiple Choice".equals(type)) {
                        while ((line = reader.readLine()) != null && !line.trim().startsWith("Answer:")) {
                            if (!line.trim().isEmpty()) {
                                options.add(line.trim());
                            }
                        }
                        answerLine = line; // This line should be the "Answer:" line
                    } else { // True/False, just read the next line for the answer
                        answerLine = reader.readLine();
                    }

                    if (answerLine != null && answerLine.trim().startsWith("Answer:")) {
                        String correctAnswer = answerLine.substring("Answer:".length()).trim();
                        Question question;
                        if ("Multiple Choice".equals(type)) {
                            question = new Question(currentCategory, type, questionText, options, correctAnswer);
                        } else {
                            question = new Question(currentCategory, type, questionText, correctAnswer);
                        }
                        categorizedQuestions.computeIfAbsent(currentCategory, k -> new ArrayList<>()).add(question);
                    } else {
                        System.err.println("Error: Could not find answer for question: " + questionText);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading questions file: " + e.getMessage());
        }
        return categorizedQuestions;
    }

    // Example main method for testing
    public static void main(String[] args) {
        QuestionParser parser = new QuestionParser();
        Map<String, List<Question>> questions = parser.parseQuestions();

        for (Map.Entry<String, List<Question>> entry : questions.entrySet()) {
            System.out.println("Category: " + entry.getKey());
            for (Question q : entry.getValue()) {
                System.out.println("  " + q.getQuestionText());
                if (!q.getOptions().isEmpty()) {
                    q.getOptions().forEach(option -> System.out.println("    " + option));
                }
                System.out.println("  Correct Answer: " + q.getCorrectAnswer());
                System.out.println("---");
            }
        }
    }
}