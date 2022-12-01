package src.UI;

import org.json.JSONArray;
import org.json.JSONObject;
import src.agents.UIAgent;
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
    JScrollPane chatPanel;

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
        JScrollBar vertical = chatPanel.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private void setProjectInfo(JSONObject project) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.weighty = 0.5;

        JLabel titleLabel = new JLabel(project.getString("title"));
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
        c.gridx = 0;
        c.gridy = 0;
        base.centerPanel.add(titleLabel, c);

        JLabel descriptionLabel = new JLabel(project.getString("description"));
        descriptionLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        c.gridy = 1;
        base.centerPanel.add(descriptionLabel, c);

        User user = User.get_with_id(uiAgent.db, project.getInt("assignee_id"));
        JLabel assigneeLabel = new JLabel("Assignee: " + user.username);
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        c.gridy = 2;
        base.centerPanel.add(assigneeLabel, c);

        JLabel deadline = new JLabel("Estimated finish date: " + project.getString("deadline"));
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        c.gridy = 3;
        base.centerPanel.add(deadline, c);

        JLabel hoursWorked = new JLabel("hours worked so far: " + project.getInt("hours_worked"));
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        c.gridy = 4;
        base.centerPanel.add(hoursWorked, c);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(project.getInt("progress"));
        progressBar.setStringPainted(true);
        c.gridy = 5;
        base.centerPanel.add(progressBar, c);

        c.gridy = 0;
        c.gridx = 1;
        base.centerPanel.add(new JPanel(), c);

        chatPanel = new JScrollPane();
        chatPanel.setPreferredSize(new Dimension(400,100));
        chatPanel.setViewportView(getChatViewPort());
        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 0;
        c.weightx = 1;
        c.gridheight = 5;
        c.gridwidth = 2;
        base.centerPanel.add(chatPanel, c);
        c.gridwidth = 1;

        chatInput = new JTextField();
        chatInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 5;
        c.weightx = 0.75;
        base.centerPanel.add(chatInput, c);


        sendBtn = new JButton("Send");
        c.gridx = 3;
        c.gridy = 5;
        c.weightx = 0.25;
        sendBtn.addActionListener(this);
        base.centerPanel.add(sendBtn, c);
    }

    private Component getChatViewPort() {
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(0, 3, 20,20));
        cell.setBorder(new EmptyBorder(10, 10, 10, 10));
        JSONArray messages = uiAgent.retrieveChat(project.getInt("id"));

        for (int i = 0; i < messages.length(); i++) {
            JSONObject message = messages.getJSONObject(i);
            JPanel first = new JPanel();
            JPanel second = new JPanel();
            JPanel third = new JPanel();
            if (message.getInt("sender_id") == user.id) {
                first.add(new JLabel(message.getString("text")));
            } else {
                third.add(new JLabel(message.getString("text")));
            }
            cell.add(first);
            cell.add(second);
            cell.add(third);
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
            chatPanel.setViewportView(getChatViewPort());
        }
    }
}
