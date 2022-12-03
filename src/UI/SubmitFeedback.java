package src.UI;

import org.json.JSONObject;
import src.agents.UIAgent;
import src.db.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SubmitFeedback implements ActionListener {
    GridBase base;
    UIAgent uiAgent;
    User user;
    JSONObject project;
    JLabel responseLabel;

    public SubmitFeedback(UIAgent uiAgent, User user, JSONObject project) {
        this.uiAgent = uiAgent;
        this.user = user;
        this.project = project;
        base = new GridBase(5, 3);

        JLabel titleLabel = new JLabel("Submit a feedback for '" + project.getString("title") + "' project");
        JLabel commentLabel = new JLabel("Comment: ");
        JLabel rateLabel = new JLabel("Rate (1 - 5): ");
        JTextField commentField = new JTextField();
        JTextField rateField = new JTextField();
        JButton submitBtn = new JButton("Submit");
        responseLabel = new JLabel();
        submitBtn.addActionListener(e -> {
            boolean valid = true;
            String comment = commentField.getText();
            String rate = rateField.getText();
            rateField.setText("");
            if (comment.length() == 0) {
                responseLabel.setText("Comment cannot be empty");
                valid = false;
            }
            try {
                int rateValue = Integer.parseInt(rate);
                if (rateValue < 1 || rateValue > 5) {
                    responseLabel.setText("Rate should be between 1 - 5");
                    valid = false;
                }
            } catch (Exception ex) {
                responseLabel.setText("Rate should be an integer");
                valid = false;
            }
            if (valid) {
                uiAgent.callForFeedbackCreation(project, comment, Integer.parseInt(rate), user);
                base.frame.dispose();
                new ProjectsList(uiAgent, user);
            }
        });
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        commentLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        rateLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        submitBtn.setFont(new Font("Consolas", Font.PLAIN, 15));
        commentField.setFont(new Font("Consolas", Font.PLAIN, 15));
        rateField.setFont(new Font("Consolas", Font.PLAIN, 15));
        responseLabel.setForeground(Color.red);

        base.centerPanels[0][1].add(titleLabel);
        base.centerPanels[1][0].add(commentLabel);
        base.centerPanels[2][0].add(rateLabel);

        base.centerPanels[1][1].add(commentField);
        base.centerPanels[2][1].add(rateField);
        base.centerPanels[3][1].add(submitBtn);
        base.centerPanels[4][1].add(responseLabel);

        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == base.backButton) {
            base.frame.dispose();
            new ProjectsList(uiAgent, user);
        }
    }
}
