import javax.swing.*;
import java.awt.*;

public class LucienneGame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public LucienneGame() {
        setTitle("Lucienne: Quest for Quality Education");
        setSize(360, 640); // Smartphone resolution [cite: 67]
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add Screens here
        mainPanel.add(createWelcomeScreen(), "Welcome");
        
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createWelcomeScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Welcome to Lucienne", SwingConstants.CENTER);
        JButton startBtn = new JButton("Start Journey");
        
        // This is how you switch screens
        startBtn.addActionListener(e -> cardLayout.show(mainPanel, "Village"));
        
        panel.add(label, BorderLayout.CENTER);
        panel.add(startBtn, BorderLayout.SOUTH);
        return panel;
    }

    public static void main(String[] args) {
        // Run from command line as required [cite: 99]
        SwingUtilities.invokeLater(() -> new LucienneGame());
    }
}