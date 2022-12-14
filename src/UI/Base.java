package src.UI;

import javax.swing.*;
import java.awt.*;

public class Base extends JFrame {
    public JFrame frame;
    public JPanel leftPanel;
    public JPanel topPanel;
    public JPanel rightPanel;
    public JPanel bottomPanel;
    public JPanel centerPanel;
    JButton backButton;
    JPanel[][] topPanels;

    public Base() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setTitle("Matchmaking");
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(20, 20));
        backButton = new JButton("Back");
        backButton.setFocusable(false);

        leftPanel = new JPanel();
        topPanel = new JPanel();
        rightPanel = new JPanel();
        bottomPanel = new JPanel();
        centerPanel = new JPanel();

        leftPanel.setPreferredSize(new Dimension(100, 100));
        topPanel.setPreferredSize(new Dimension(100, 100));
        rightPanel.setPreferredSize(new Dimension(100, 100));
        bottomPanel.setPreferredSize(new Dimension(100, 100));
        centerPanel.setPreferredSize(new Dimension(100, 100));

        topPanels = new JPanel[1][7];
        topPanel.setLayout(new GridLayout(1, 7, 20, 20));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        for (int j = 0; j < 7; j ++) {
            JPanel panel = new JPanel(new BorderLayout());
            topPanels[0][j] = panel;
            topPanel.add(panel);
        }

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(centerPanel, BorderLayout.CENTER);
    }
}
