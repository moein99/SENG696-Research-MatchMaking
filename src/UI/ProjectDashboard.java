package src.UI;

import org.json.JSONArray;
import org.json.JSONObject;
import src.agents.UIAgent;
import src.db.Project;
import src.db.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProjectDashboard implements ActionListener {
    GridBase base;
    UIAgent uiAgent;
    User user;
    JButton sendBtn;
    JTextField chatInput;
    JSONObject project;
    JScrollPane chatPane;
    JButton extendSubmit;
    JTextField extendField;
    JTextField progressField;
    JTextField hoursWorkedField;
    JButton progressSubmit;
    JButton hoursWorkedSubmit;
    JButton endProjectBtn;

    public ProjectDashboard(UIAgent uiAgent, User user, JSONObject project) {
        this.uiAgent = uiAgent;
        this.user = user;
        this.project = project;
        base = new GridBase(4, 3);
        base.centerPanel.setLayout(new GridBagLayout());
        base.centerPanel.setBackground(Color.GRAY);
        base.centerPanel.setBorder(new EmptyBorder(15,15,15,15));

        setProjectInfo(project);

        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
        JScrollBar vertical = chatPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private void setProjectInfo(JSONObject project) {
        int maxHeight = 0;
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 0.5;
        c.weighty = 0.5;

        JLabel titleLabel = new JLabel(project.getString("title"));
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
        c.gridx = 0;
        c.gridy = 0;
        base.centerPanel.add(titleLabel, c);

        JLabel descriptionLabel = new JLabel(project.getString("description"));
        descriptionLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        c.gridy = 1;
        base.centerPanel.add(descriptionLabel, c);

        User assignedUser = User.get_with_id(uiAgent.db, project.getInt("assignee_id"));
        JLabel assigneeLabel = new JLabel("Assignee: " + assignedUser.username);
        assigneeLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        c.gridy = 2;
        base.centerPanel.add(assigneeLabel, c);

        JLabel hoursWorked = new JLabel("hours worked so far: " + project.getInt("hours_worked"));
        hoursWorked.setFont(new Font("Consolas", Font.PLAIN, 12));
        c.gridy = 3;
        base.centerPanel.add(hoursWorked, c);

        JLabel deadline = new JLabel("Estimated finish date: " + project.getString("deadline"));
        deadline.setFont(new Font("Consolas", Font.PLAIN, 12));
        c.gridy = 4;
        base.centerPanel.add(deadline, c);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(project.getInt("progress"));
        progressBar.setStringPainted(true);
        c.gridy = 5;
        c.gridx = 0;
        base.centerPanel.add(progressBar, c);

        if (user.user_type.equals(User.PROVIDER_TYPE)) {
            JLabel extendLabel = new JLabel("Extend for: ");
            extendLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
            c.gridy = 6;
            base.centerPanel.add(extendLabel, c);

            extendField = new JTextField();
            extendLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
            c.gridy = 6;
            c.gridx = 1;
            extendField.setPreferredSize(new Dimension(50, 20));
            base.centerPanel.add(extendField, c);

            JPanel extendPanel = new JPanel();
            extendPanel.setLayout(new BorderLayout(10,10));
            extendPanel.setBorder(new EmptyBorder(0,20,0,20));
            extendPanel.setBackground(Color.GRAY);
            extendSubmit = new JButton("Extend");
            c.gridy = 6;
            c.gridx = 2;
            extendSubmit.setFocusable(false);
            extendSubmit.addActionListener(this);
            extendPanel.add(extendSubmit);
            base.centerPanel.add(extendPanel, c);

            if (project.getInt("progress") == 100) {
                endProjectBtn = new JButton("End Project");
                c.gridy = 7;
                c.gridx = 0;
                endProjectBtn.setFocusable(false);
                endProjectBtn.addActionListener(this);
                base.centerPanel.add(endProjectBtn, c);
            }

            maxHeight = c.gridy;
        } else {
            // Update Progress Bar
            JLabel progressLabel = new JLabel("Set Progress %: ");
            progressLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
            c.gridy = 6;
            base.centerPanel.add(progressLabel, c);

            progressField = new JTextField();
            progressField.setFont(new Font("Consolas", Font.PLAIN, 12));
            c.gridy = 6;
            c.gridx = 1;
            progressField.setPreferredSize(new Dimension(50, 20));
            base.centerPanel.add(progressField, c);

            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BorderLayout(10,10));
            progressPanel.setBorder(new EmptyBorder(0,20,0,20));
            progressPanel.setBackground(Color.GRAY);
            progressSubmit = new JButton("Update");
            c.gridy = 6;
            c.gridx = 2;
            progressSubmit.setFocusable(false);
            progressSubmit.addActionListener(this);
            progressPanel.add(progressSubmit);
            base.centerPanel.add(progressPanel, c);

            // Update Hours Worked

            JLabel hoursWorkedLabel = new JLabel("Add Hours: ");
            hoursWorkedLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
            c.gridy = 7;
            c.gridx = 0;
            base.centerPanel.add(hoursWorkedLabel, c);

            hoursWorkedField = new JTextField();
            hoursWorkedField.setFont(new Font("Consolas", Font.PLAIN, 12));
            c.gridy = 7;
            c.gridx = 1;
            hoursWorkedField.setPreferredSize(new Dimension(50, 20));
            base.centerPanel.add(hoursWorkedField, c);

            JPanel hoursWorkedPanel = new JPanel();
            hoursWorkedPanel.setLayout(new BorderLayout(10,10));
            hoursWorkedPanel.setBorder(new EmptyBorder(0,20,0,20));
            hoursWorkedPanel.setBackground(Color.GRAY);
            hoursWorkedSubmit = new JButton("Submit");
            c.gridy = 7;
            c.gridx = 2;
            hoursWorkedSubmit.setFocusable(false);
            hoursWorkedSubmit.addActionListener(this);
            hoursWorkedPanel.add(hoursWorkedSubmit);
            base.centerPanel.add(hoursWorkedPanel, c);

            maxHeight = c.gridy;
        }

        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout(10,10));
        chatPanel.setBorder(new EmptyBorder(10,10,10,10));
        chatPanel.setBackground(Color.GRAY);
        chatPane = new JScrollPane();
        chatPane.setPreferredSize(new Dimension(400,100));
        chatPane.setViewportView(getChatViewPort());
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 3;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 0;
        c.weightx = 1;
        c.gridheight = maxHeight;
        c.gridwidth = 2;
        chatPanel.add(chatPane);
        base.centerPanel.add(chatPanel, c);
        c.gridwidth = 1;

        JPanel chatFieldPanel = new JPanel();
        chatFieldPanel.setLayout(new BorderLayout(10,10));
        chatFieldPanel.setBorder(new EmptyBorder(0,20,0,20));
        chatFieldPanel.setBackground(Color.GRAY);
        chatInput = new JTextField();
        chatInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = maxHeight;
        c.weightx = 0.75;
        chatFieldPanel.add(chatInput);
        base.centerPanel.add(chatFieldPanel, c);


        sendBtn = new JButton("Send");
        c.gridx = 4;
        c.gridy = maxHeight;
        c.weightx = 0.25;
        sendBtn.addActionListener(this);
        base.centerPanel.add(sendBtn, c);
    }

    private Component getChatViewPort() {
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(0, 2, 20,20));
        cell.setBorder(new EmptyBorder(10, 10, 10, 10));
        JSONArray messages = uiAgent.retrieveChat(project.getInt("id"));

        for (int i = 0; i < messages.length(); i++) {
            JSONObject message = messages.getJSONObject(i);
            JPanel left = new JPanel();
            JPanel right = new JPanel();
            if (message.getInt("sender_id") == user.id) {
                left.add(new JLabel(message.getString("text")));
                left.setBackground(Color.lightGray);
            } else {
                right.add(new JLabel(message.getString("text")));
                right.setBackground(Color.white);
            }
            cell.add(left);
            cell.add(right);
        }
        return cell;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == base.backButton) {
            base.frame.dispose();
            new ActiveProjects(uiAgent, user);
        }
        if (e.getSource() == sendBtn) {
            String message = chatInput.getText();
            chatInput.setText("");
            int senderId, receiverId;
            senderId = user.id;
            if (user.id == project.getInt("owner_id")) {
                receiverId = project.getInt("assignee_id");
            } else {
                receiverId = project.getInt("owner_id");
            }
            uiAgent.callForMessage(message, senderId, receiverId, project.getInt("id"));
            chatPane.setViewportView(getChatViewPort());
            JScrollBar vertical = chatPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        }
        if (e.getSource() == extendSubmit) {
            String daysText = extendField.getText();
            extendField.setText("");
            try {
                int extendAmount = Integer.parseInt(daysText);
                uiAgent.callForProjectExtension(extendAmount, project.getInt("id"));
                base.frame.dispose();
                new ProjectDashboard(uiAgent, user, Project.get_with_id(uiAgent.db, project.getInt("id")).json());
            } catch (Exception ex) {
                System.out.println("can't convert extend amount to integer");
            }
        }
        if (e.getSource() == progressSubmit) {
            String progress = progressField.getText();
            progressField.setText("");
            try {
                int progressAmount = Integer.parseInt(progress);
                if (progressAmount >= 0 && progressAmount <= 100) {
                    uiAgent.callForProgressUpdate(progressAmount, project.getInt("id"));
                    base.frame.dispose();
                    new ProjectDashboard(uiAgent, user, Project.get_with_id(uiAgent.db, project.getInt("id")).json());
                }
            } catch (Exception ex) {
                System.out.println("can't convert extend amount to integer");
            }
        }
        if (e.getSource() == hoursWorkedSubmit) {
            String hours = hoursWorkedField.getText();
            hoursWorkedField.setText("");
            try {
                int hoursAmount = Integer.parseInt(hours);
                uiAgent.callForHoursUpdate(hoursAmount, project.getInt("id"), project.getInt("hours_worked"));
                base.frame.dispose();
                new ProjectDashboard(uiAgent, user, Project.get_with_id(uiAgent.db, project.getInt("id")).json());
            } catch (Exception ex) {
                System.out.println("can't convert extend amount to integer");
            }
        }
        if (e.getSource() == endProjectBtn) {
            System.out.println("done");
            uiAgent.callForProjectEnding(project.getInt("id"));
        }
    }
}
