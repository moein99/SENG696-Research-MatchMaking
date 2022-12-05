package src.UI;

import org.json.JSONObject;
import src.agents.UIAgent;
import src.db.Project;
import src.db.User;
import src.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Apply implements ActionListener {
    GridBase base;
    UIAgent uiAgent;
    User user;
    int projectId;
    public JTextField descriptionField;
    public JTextField proposedAmountField;
    public JLabel responseLabel;
    JButton submitBtn;
    public Apply(UIAgent uiAgent, User user, int projectId) {
        this.uiAgent = uiAgent;
        this.projectId = projectId;
        this.user = user;
        base = new GridBase(7, 3);
        Project project = Project.get_with_id(uiAgent.db, projectId);

        JLabel titleLabel = new JLabel(project.title);
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
        JLabel descriptionLabel = new JLabel(project.description);
        descriptionLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        JLabel deadlineLabel = new JLabel("Deadline: " + Utils.convertDateToString(project.deadline));
        deadlineLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        JLabel hourlyCompensationLabel = new JLabel("Provider's hourly rate: " + User.get_with_id(uiAgent.db, project.ownerId).hourly_compensation + "$");
        hourlyCompensationLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        JLabel descriptionFieldLabel = new JLabel("Description:", SwingConstants.RIGHT);
        JLabel amountFieldLabel = new JLabel("Amount:", SwingConstants.RIGHT);
        descriptionFieldLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        amountFieldLabel.setFont(new Font("Consolas", Font.PLAIN, 15));


        descriptionField = new JTextField();
        proposedAmountField = new JTextField();
        responseLabel = new JLabel();
        submitBtn = new JButton("Submit");
        descriptionField.setFont(new Font("Consolas", Font.PLAIN, 15));
        proposedAmountField.setFont(new Font("Consolas", Font.PLAIN, 15));
        responseLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        responseLabel.setForeground(Color.RED);
        submitBtn.addActionListener(this);

        titleLabel.setBorder(new EmptyBorder(10, 10,10,10));
        descriptionLabel.setBorder(new EmptyBorder(10, 10,10,10));
        deadlineLabel.setBorder(new EmptyBorder(10, 10,10,10));
        hourlyCompensationLabel.setBorder(new EmptyBorder(10, 10,10,10));

        base.centerPanels[0][0].add(titleLabel);
        base.centerPanels[1][0].add(descriptionLabel);
        base.centerPanels[0][1].add(deadlineLabel);
        base.centerPanels[1][1].add(hourlyCompensationLabel);

        base.centerPanels[3][0].add(descriptionFieldLabel);
        base.centerPanels[4][0].add(amountFieldLabel);
        base.centerPanels[3][1].add(descriptionField);
        base.centerPanels[4][1].add(proposedAmountField);
        base.centerPanels[5][1].add(submitBtn);
        base.centerPanels[6][1].add(responseLabel);

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
            String description = descriptionField.getText();
            String proposedAmount = proposedAmountField.getText();
            String message = validate(description, proposedAmount);
            if (message != null) {
                responseLabel.setText(message);
            } else {
                JSONObject response;
                response = uiAgent.callForBidCreation(projectId, user.id, description, Integer.parseInt(proposedAmount));
                if (response.getInt("id") == -1) {
                    responseLabel.setText("internal error");
                } else {
                    base.frame.dispose();
                    new Home(uiAgent, user);
                }
            }
        }
    }

    private String validate(String description, String proposedAmount) {
        if (description.length() == 0) {
            return "description can not be empty";
        }
        int amount;
        try {
            amount = Integer.parseInt(proposedAmount);
        } catch (Exception ex) {
            return "amount should be a positive integer";
        }
        if (amount <= 0) {
            return "amount should be a positive integer";
        }
        return null;
    }
}
