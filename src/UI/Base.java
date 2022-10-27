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

    public Base() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setTitle("Matchmaking");
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

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

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(centerPanel, BorderLayout.CENTER);
    }
}
