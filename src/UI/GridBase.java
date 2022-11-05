package src.UI;

import javax.swing.*;
import java.awt.*;

public class GridBase extends Base {
    JPanel[][] centerPanels;
    JPanel[][] topPanels;
    JButton backButton;


    GridBase(int rows, int columns) {
        super();
        centerPanels = new JPanel[rows][columns];
        topPanels = new JPanel[1][7];
        backButton = new JButton("Back");
        backButton.setFocusable(false);

        centerPanel.setLayout(new GridLayout(rows, columns, 20, 20));
        topPanel.setLayout(new GridLayout(1, 7, 20, 20));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j ++) {
                JPanel panel = new JPanel(new BorderLayout());
//                panel.setBackground(Color.GRAY);
                centerPanels[i][j] = panel;
                centerPanel.add(panel);
            }
        }

        for (int j = 0; j < 7; j ++) {
            JPanel panel = new JPanel(new BorderLayout());
//            panel.setBackground(Color.GRAY);
            topPanels[0][j] = panel;
            topPanel.add(panel);
        }
    }
}
