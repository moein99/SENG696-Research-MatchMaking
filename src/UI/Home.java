package src.UI;

import src.agents.UIAgent;
import src.db.Bid;
import src.db.Project;
import src.db.User;
import src.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Home implements ActionListener {
    UIAgent uiAgent;
    User user;
    GridBase base;
    JButton createProjectBtn;
    JButton showBidsBtn;
    JButton showActiveProjectsBtn;
    public Home(UIAgent agent, User dbUser) {
        uiAgent = agent;
        user = dbUser;
        base = new GridBase(1, 1);
        int topPanelCounter = 1;

        if (user != null && user.user_type.equals(User.PROVIDER_TYPE)) {
            createProjectBtn = new JButton("Create Project");
            createProjectBtn.addActionListener(this);
            createProjectBtn.setFocusable(false);
            base.topPanels[0][topPanelCounter++].add(createProjectBtn);

            showBidsBtn = new JButton("Incoming Bids");
            showBidsBtn.addActionListener(this);
            showBidsBtn.setFocusable(false);
            base.topPanels[0][topPanelCounter++].add(showBidsBtn);
        }

        showActiveProjectsBtn = new JButton("Active Projects");
        showActiveProjectsBtn.addActionListener(this);
        showActiveProjectsBtn.setFocusable(false);
        base.topPanels[0][topPanelCounter++].add(showActiveProjectsBtn);

        ArrayList<Project> projects = uiAgent.getProjects();
        JScrollPane sp = new JScrollPane();
        sp.setViewportView(getViewPort(projects));

        base.centerPanels[0][0].add(sp);
        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    private JPanel getViewPort(ArrayList<Project> projects) {
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(0, 1, 20,20));

        for (Project project: projects) {
            JLabel titleLabel = new JLabel(project.title);
            titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            JLabel descriptionLabel = new JLabel(project.description);
            descriptionLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
            JLabel deadlineLabel = new JLabel("Deadline: " + Utils.convertDateToString(project.deadline));
            deadlineLabel.setFont(new Font("Consolas", Font.PLAIN, 12));

            titleLabel.setBorder(new EmptyBorder(10, 10,10,10));
            descriptionLabel.setBorder(new EmptyBorder(10, 10,10,10));
            deadlineLabel.setBorder(new EmptyBorder(10, 10,10,10));

            JPanel itemPanel = new JPanel();
            itemPanel.setBorder(new EmptyBorder(10, 10,10,10));
            itemPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 0;
            itemPanel.add(titleLabel, c);

            c.gridy = 1;
            itemPanel.add(descriptionLabel, c);

            c.gridy = 2;
            itemPanel.add(deadlineLabel, c);


            if (user != null) {
                c.gridy = 3;
                JLabel hourlyCompensationLabel = new JLabel("Provider's hourly rate: " + User.get_with_id(uiAgent.db, project.ownerId).hourly_compensation + "$");
                hourlyCompensationLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
                hourlyCompensationLabel.setBorder(new EmptyBorder(10, 10,10,10));
                itemPanel.add(hourlyCompensationLabel, c);
            }

            if (user != null && user.user_type.equals(User.CLIENT_TYPE)) {
                Bid bid = Bid.getById(uiAgent.db, user.id, project.id);
                if (bid == null) {
                    JButton applyBtn = new JButton("Apply");
                    applyBtn.setFocusable(false);
                    applyBtn.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            base.frame.dispose();
                            new Apply(uiAgent, user, project.id);
                        }
                    });

                    c.gridwidth = 1;
                    c.gridx = 2;
                    c.gridy = 3;
                    itemPanel.add(applyBtn);
                }
            }

            itemPanel.setBackground(Color.GRAY);
            cell.add(itemPanel);
        }

        return cell;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == base.backButton) {
            base.frame.dispose();
            new Launch(uiAgent);
        }

        if (e.getSource() == createProjectBtn) {
            base.frame.dispose();
            new CreateProject(uiAgent, user);
        }

        if (e.getSource() == showBidsBtn) {
            base.frame.dispose();
            new ProviderBids(uiAgent, user);
        }

        if (e.getSource() == showActiveProjectsBtn) {
            base.frame.dispose();
            new ProjectsList(uiAgent, user);
        }
    }
}
