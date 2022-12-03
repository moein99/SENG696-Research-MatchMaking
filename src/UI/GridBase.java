package src.UI;

import javax.swing.*;
import java.awt.*;

public class GridBase extends Base {
    JPanel[][] centerPanels;

    GridBase(int rows, int columns) {
        super();
        centerPanels = new JPanel[rows][columns];
        centerPanel.setLayout(new GridLayout(rows, columns, 20, 20));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j ++) {
                JPanel panel = new JPanel(new BorderLayout());
//                panel.setBackground(Color.GRAY);
                centerPanels[i][j] = panel;
                centerPanel.add(panel);
            }
        }
    }
}
