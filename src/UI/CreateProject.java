package src.UI;

import org.json.JSONObject;
import src.agents.UIAgent;
import src.db.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateProject implements ActionListener {
    UIAgent uiAgent;
    User user;
    GridBase base;
    JTextField titleInput;
    JTextField descriptionInput;
    JTextField durationInput;
    JLabel responseLabel;
    JButton submitBtn;

    public CreateProject(UIAgent agent, User dbUser) {
        uiAgent = agent;
        user = dbUser;
        base = new GridBase(7, 3);

        titleInput = new JTextField();
        descriptionInput = new JTextField();
        durationInput = new JTextField();
        submitBtn = new JButton("Create");
        submitBtn.addActionListener(this);
        submitBtn.setFocusable(false);
        responseLabel = new JLabel();

        titleInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        descriptionInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        durationInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        responseLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        responseLabel.setForeground(Color.RED);

        base.centerPanels[1][0].add(new JLabel("Title:"), BorderLayout.EAST);
        base.centerPanels[2][0].add(new JLabel("Description:"), BorderLayout.EAST);
        base.centerPanels[3][0].add(new JLabel("Duration (days):"), BorderLayout.EAST);

        base.centerPanels[1][1].add(titleInput);
        base.centerPanels[2][1].add(descriptionInput);
        base.centerPanels[3][1].add(durationInput);
        base.centerPanels[4][1].add(submitBtn);
        base.centerPanels[5][1].add(responseLabel);

        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == base.backButton) {
            base.frame.dispose();
            new Home(uiAgent, user);
        }

        if (e.getSource() == submitBtn) {
            String title = titleInput.getText();
            String description = descriptionInput.getText();
            String duration = durationInput.getText();
            int durationDays;
            try {
                durationDays = Integer.parseInt(duration);
            } catch (NumberFormatException ex) {
                responseLabel.setText("Duration should be an integer");
                return;
            }
            JSONObject response = uiAgent.call_for_project_creation(user.id, title, description, durationDays);
            if (response.getInt("id") != -1) {
                responseLabel.setText("Created!");
                base.frame.dispose();
                new Home(uiAgent, user);
            } else {
                responseLabel.setText(response.getString("message"));
            }
        }
    }
}
