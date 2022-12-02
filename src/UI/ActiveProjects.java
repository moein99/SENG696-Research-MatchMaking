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

public class ActiveProjects implements ActionListener {
    GridBase base;
    UIAgent uiAgent;
    User user;

    public ActiveProjects(UIAgent uiAgent, User user) {
        this.uiAgent = uiAgent;
        this.user = user;
        base = new GridBase(1, 1);

        JSONArray projects = uiAgent.getUserActiveProjects(user);
        JScrollPane sp = new JScrollPane();
        sp.setViewportView(getViewPort(projects));

        base.centerPanels[0][0].add(sp);
        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    private Component getViewPort(JSONArray projects) {
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(0, 1, 20,20));

        for (int i = 0 ; i < projects.length(); i++) {
            JSONObject project = projects.getJSONObject(i);

            JPanel itemPanel = new JPanel();
            itemPanel.setBorder(new EmptyBorder(10, 10,10,10));
            itemPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            JLabel titleLabel = new JLabel(project.getString("title"));
            titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            titleLabel.setBorder(new EmptyBorder(10, 30, 10, 30));
            itemPanel.add(titleLabel, c);

            JButton view = new JButton("View Dashboard");
            view.setFocusable(false);
            view.setFont(new Font("Consolas", Font.PLAIN, 20));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 0;
            view.setBorder(new EmptyBorder(10, 30, 10, 30));
            view.addActionListener(e -> {
                base.frame.dispose();
                new ProjectDashboard(uiAgent, user, project);
            });
            itemPanel.add(view, c);

            itemPanel.setBackground(Color.GRAY);
            cell.add(itemPanel);
        }

        return cell;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == base.backButton) {
            base.frame.dispose();
            new Home(uiAgent, user);
        }
    }
}
