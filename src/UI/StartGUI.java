package src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartGUI implements ActionListener {
    private JFrame frame;
    private JPanel panel;
    private JButton button;
    private JLabel label;
    private int counter = 0;
//    private
    public StartGUI () {
        this.frame = new JFrame();
        this.panel = new JPanel();
        this.panel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        this.button = new JButton("Oh Maaaan!");
        this.button.addActionListener(this);
        this.label = new JLabel("Number of clicks: 0");

        this.panel.add(this.button);
        this.panel.add(this.label);
        panel.setLayout(new GridLayout(0, 1));
        this.frame.add(this.panel, BorderLayout.CENTER);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(1000,1000);
        frame.setTitle("Matchmaking");
//        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.counter++;
        this.label.setText("Number of clicks: " + this.counter);
    }
}
