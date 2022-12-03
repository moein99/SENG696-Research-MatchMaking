package src.UI;

import org.json.JSONObject;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Home implements ActionListener {
    UIAgent uiAgent;
    User user;
    Base base;
    JButton createProjectBtn;
    JButton showBidsBtn;
    JButton showProjectsListBtn;
    JButton showUserFeedbacksBtn;
    final static String verifiedIconAddress = "data/check.png";
    public Home(UIAgent agent, User dbUser) {
        uiAgent = agent;
        user = dbUser;
        base = new Base();
        base.centerPanel.setLayout(new GridBagLayout());
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

        if (user != null) {
            showProjectsListBtn = new JButton("My Projects");
            showProjectsListBtn.addActionListener(this);
            showProjectsListBtn.setFocusable(false);
            base.topPanels[0][topPanelCounter++].add(showProjectsListBtn);

            showUserFeedbacksBtn = new JButton("My Feedbacks");
            showUserFeedbacksBtn.addActionListener(this);
            showUserFeedbacksBtn.setFocusable(false);
            base.topPanels[0][topPanelCounter++].add(showUserFeedbacksBtn);
        }

        ArrayList<Project> projects = uiAgent.getProjects();
        JScrollPane sp = new JScrollPane();
        sp.setViewportView(getViewPort(projects));
        JPanel projectsPanel = new JPanel();
        projectsPanel.setLayout(new GridLayout(1, 1));
        projectsPanel.add(sp);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0,10,0,10);
        c.weightx = 0.333;

        JPanel p = new JPanel();
        p.setBackground(Color.GRAY);
        p.setPreferredSize(new Dimension(200, 510));
        base.centerPanel.add(p, c);
        c.gridx = 1;
        c.weightx = 0.666;
        projectsPanel.setPreferredSize(new Dimension(200, 510));
        base.centerPanel.add(projectsPanel, c);
        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    private JPanel getViewPort(ArrayList<Project> projects) {
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(0, 1, 20,20));
        projects.sort((p1, p2) -> {
            boolean p1Verified = p1.isOwnerVerified(uiAgent.db);
            boolean p2Verified = p2.isOwnerVerified(uiAgent.db);
            if ((p1Verified && p2Verified) || (!p1Verified && !p2Verified)) {
                return 0;
            } else if (p1Verified) {
                return -1;
            } else {
                return 1;
            }
        });

        for (Project project: projects) {
            JLabel titleLabel = new JLabel(project.title);
            titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            JLabel descriptionLabel = new JLabel(project.description);
            descriptionLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
            JLabel deadlineLabel = new JLabel("Deadline: " + Utils.convertDateToString(project.deadline));
            deadlineLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
            User owner = User.get_with_id(uiAgent.db, project.ownerId);
            JLabel providerLabel = new JLabel("Owner: " + owner.username);
            providerLabel.setForeground(new Color(0, 150, 255));
            if (owner.is_verified) {
                ImageIcon verifiedIcon = new ImageIcon(verifiedIconAddress);
                Image image = verifiedIcon.getImage();
                Image newimg = image.getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
                verifiedIcon = new ImageIcon(newimg);
                providerLabel.setIcon(verifiedIcon);
            }
            providerLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    base.frame.dispose();
                    new History(uiAgent, user, project.ownerId, History.BACK_HOME);
                }
            });

            titleLabel.setBorder(new EmptyBorder(10, 10,10,10));
            descriptionLabel.setBorder(new EmptyBorder(10, 10,10,10));
            deadlineLabel.setBorder(new EmptyBorder(10, 10,10,10));
            providerLabel.setBorder(new EmptyBorder(10, 10,10,10));

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

            c.gridy = 3;
            itemPanel.add(providerLabel, c);


            if (user != null) {
                c.gridy = 4;
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
                    applyBtn.addActionListener(e -> {
                        base.frame.dispose();
                        new Apply(uiAgent, user, project.id);
                    });

                    c.gridwidth = 1;
                    c.gridx = 2;
                    c.gridy = 4;
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

        if (e.getSource() == showProjectsListBtn) {
            base.frame.dispose();
            new ProjectsList(uiAgent, user);
        }

        if (e.getSource() == showUserFeedbacksBtn) {
            base.frame.dispose();
            new History(uiAgent, user, user.id, History.BACK_HOME);
        }
    }
}
