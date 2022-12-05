package src.UI;

import src.agents.UIAgent;
import src.db.*;
import src.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Home implements ActionListener {
    UIAgent uiAgent;
    User user;
    Base base;
    JButton createProjectBtn;
    JButton showBidsBtn;
    JButton showProjectsListBtn;
    JButton showUserFeedbacksBtn;
    JScrollPane projectsScrollPane;
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
        }

        if (user != null) {
            showBidsBtn = new JButton("My Bids");
            showBidsBtn.addActionListener(this);
            showBidsBtn.setFocusable(false);
            base.topPanels[0][topPanelCounter++].add(showBidsBtn);

            showProjectsListBtn = new JButton("My Projects");
            showProjectsListBtn.addActionListener(this);
            showProjectsListBtn.setFocusable(false);
            base.topPanels[0][topPanelCounter++].add(showProjectsListBtn);

            showUserFeedbacksBtn = new JButton("My Profile");
            showUserFeedbacksBtn.addActionListener(this);
            showUserFeedbacksBtn.setFocusable(false);
            base.topPanels[0][topPanelCounter++].add(showUserFeedbacksBtn);
        }

        setFilterPanel();
        setProjectsPanel();

        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    private void setFilterPanel() {
        JPanel p = new JPanel();
        p.setBorder(new LineBorder(Color.BLACK,1,true));
        p.setPreferredSize(new Dimension(200, 510));

        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JLabel keywordsLabel = Utils.getJLabel("Keywords (separate with ,): ", 12);
        JLabel minLabel = Utils.getJLabel("Min Salary: ", 12);
        JLabel maxLabel = Utils.getJLabel("Max Salary: ", 12);
        JLabel projectsDoneLabel = Utils.getJLabel("Projects Done (+): ", 12);
        JLabel responseLabel = Utils.getJLabel("", 12);
        responseLabel.setForeground(Color.RED);
        JTextField keywordsField = Utils.getJTextField(15);
        JTextField minField = Utils.getJTextField(15);
        JTextField maxField = Utils.getJTextField(15);
        JTextField projectsDoneField = Utils.getJTextField(15);
        JButton filterBtn = new JButton("Filter");
        JButton resetBtn = new JButton("Reset");

        filterBtn.addActionListener(e -> {
            String keywords = keywordsField.getText();
            String minI = minField.getText();
            String maxI = maxField.getText();
            if (user == null && (!maxI.equals("") || !minI.equals(""))) {
                minField.setText("");
                maxField.setText("");
                responseLabel.setText("Guests can not search based on salary");
                return;
            }
            String projectsDoneI = projectsDoneField.getText();
            String verificationResponse = filterParametersValid(minI, maxI, projectsDoneI);
            if (verificationResponse.equals("")) {
                responseLabel.setText("");
                int min, max, projectsDone;
                if (minI.equals("")) {
                    min = 0;
                } else {
                    min = Integer.parseInt(minI);
                }

                if (maxI.equals("")) {
                    max = 999999999;
                } else {
                    max = Integer.parseInt(maxI);
                }

                if (projectsDoneI.equals("")) {
                    projectsDone = -1;
                } else {
                    projectsDone = Integer.parseInt(projectsDoneI);
                }

                ArrayList<Project> projects = uiAgent.retrieveProjects(min, max, projectsDone, keywords);
                projectsScrollPane.setViewportView(getViewPort(projects));
            } else {
                responseLabel.setText(verificationResponse);
            }
        });

        resetBtn.addActionListener(e -> {
            ArrayList<Project> projects = uiAgent.retrieveProjects(0, 9999999, 0, "");
            projectsScrollPane.setViewportView(getViewPort(projects));
            minField.setText("");
            maxField.setText("");
            projectsDoneField.setText("");
            keywordsField.setText("");
        });

        resetBtn.setFocusable(false);
        filterBtn.setFocusable(false);

        c.gridy = 0;
        c.gridx = 0;
        c.weighty = 1;
        c.insets = new Insets(10, 10, 0, 10);
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.weightx = 0.1;
        p.add(keywordsLabel, c);
        c.weightx = 0.9;
        c.gridx = 1;
        p.add(keywordsField, c);

        c.gridy = 1;

        c.gridx = 0;
        c.weightx = 0.1;
        p.add(minLabel, c);
        c.gridx = 1;
        c.weightx = 0.9;
        p.add(minField, c);

        c.gridy = 2;

        c.gridx = 0;
        c.weightx = 0;
        p.add(maxLabel, c);
        c.gridx = 1;
        c.weightx = 0;
        p.add(maxField, c);

        c.gridy = 3;

        c.gridx = 0;
        c.weightx = 0.9;
        p.add(projectsDoneLabel, c);
        c.gridx = 1;
        c.weightx = 0.1;
        p.add(projectsDoneField, c);

        c.gridy = 4;

        c.gridx = 0;
        c.weightx = 0.5;
        p.add(resetBtn, c);
        c.gridx = 1;
        c.weightx = 0.5;
        p.add(filterBtn, c);

        c.gridy = 5;

        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        p.add(responseLabel, c);

        base.centerPanel.add(p, getFilterPanelConstraints());
    }

    private String filterParametersValid(String minI, String maxI, String projectsDoneI) {
        int min, max, projectsDone;
        try {
            if (minI.equals("")) {
                min = 0;
            } else {
                min = Integer.parseInt(minI);
            }

            if (maxI.equals("")) {
                max = 999999;
            } else {
                max = Integer.parseInt(maxI);
            }
            if (projectsDoneI.equals("")) {
                projectsDone = 0;
            } else {
                projectsDone = Integer.parseInt(projectsDoneI);
            }
        } catch (Exception ex) {
            return "Min, Max, and Projects done should be Integers";
        }
        if (projectsDone < 0) {
            return "Projects done can not be negative";
        }
        if (max < 1) {
            return "Max should be more than 0";
        }
        if (min < 0) {
            return "Min should be more than 0";
        }
        if (min > max) {
            return "Max should be more than Min";
        }
        return "";
    }

    private GridBagConstraints getFilterPanelConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0,10,0,10);
        c.weightx = 0.333;
        return c;
    }

    private void setProjectsPanel() {
        ArrayList<Project> projects = uiAgent.retrieveProjects(0, 9999999, 0, "");
        projectsScrollPane = new JScrollPane();
        projectsScrollPane.setViewportView(getViewPort(projects));
        JPanel projectsPanel = new JPanel();
        projectsPanel.setLayout(new GridLayout(1, 1, 20, 20));
        projectsPanel.add(projectsScrollPane);
        projectsPanel.setPreferredSize(new Dimension(200, 510));
        base.centerPanel.add(projectsPanel, getProjectPanelConstraints());
    }

    private GridBagConstraints getProjectPanelConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0,10,0,10);
        c.weightx = 0.666;
        return c;
    }

    private JPanel getViewPort(ArrayList<Project> projects) {
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(0, 1, 20,20));

        ArrayList<Project> subscriptionProjects = new ArrayList<>();
        ArrayList<Project> verifiedProjects = new ArrayList<>();
        ArrayList<Project> normalProjects = new ArrayList<>();

        for (Project project : projects) {
            User owner = User.get_with_id(uiAgent.db, project.ownerId);
            if (owner.isSubscriptionActive()) {
                subscriptionProjects.add(project);
                continue;
            }
            if (owner.is_verified) {
                verifiedProjects.add(project);
                continue;
            }
            normalProjects.add(project);
        }

        sortProjectsBasedOnApprovalRating(subscriptionProjects);
        sortProjectsBasedOnApprovalRating(verifiedProjects);
        sortProjectsBasedOnApprovalRating(normalProjects);

        setProjectsComponents(subscriptionProjects, cell);
        setProjectsComponents(verifiedProjects, cell);
        setProjectsComponents(normalProjects, cell);

        return cell;
    }

    private void sortProjectsBasedOnApprovalRating(ArrayList<Project> projects) {
        projects.sort((p1, p2) -> {
            User p1Owner = User.get_with_id(uiAgent.db, p1.ownerId);
            User p2Owner = User.get_with_id(uiAgent.db, p2.ownerId);
            float p1OwnerApprovalRate = Feedback.getUserApprovalRate(uiAgent.db, p1Owner);
            float p2OwnerApprovalRate = Feedback.getUserApprovalRate(uiAgent.db, p2Owner);
            return Float.compare(p2OwnerApprovalRate, p1OwnerApprovalRate);
        });
    }

    private void setProjectsComponents(ArrayList<Project> projects, JPanel cell) {
        for (Project project: projects) {
            JPanel itemPanel = new JPanel();
            itemPanel.setBackground(Color.WHITE);
            itemPanel.setBorder(new LineBorder(Color.BLACK, 2,true));
            itemPanel.setLayout(new GridBagLayout());

            JLabel titleLabel = Utils.getJLabel(project.title, 15);
            JLabel descriptionLabel = Utils.getJLabel(project.description, 15);
            JLabel deadlineLabel = Utils.getJLabel("Deadline: " + Utils.convertDateToString(project.deadline), 15);
            User owner = User.get_with_id(uiAgent.db, project.ownerId);
            ArrayList<String> keywords = owner.getKeywords(uiAgent.db);
            JLabel keywordsLabel = Utils.getJLabel("Keywords: " + Utils.concatStrsWithCommas(keywords), 12);
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
                    new Profile(uiAgent, user, project.ownerId, Profile.BACK_HOME);
                }
            });
            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;
            c.insets = new Insets(10,10,10,10);
            c.anchor = GridBagConstraints.WEST;
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

            c.gridy = 4;
            itemPanel.add(keywordsLabel, c);


            if (user != null) {
                c.gridy = 5;
                JLabel hourlyCompensationLabel = new JLabel("Provider's hourly rate: " + User.get_with_id(uiAgent.db, project.ownerId).hourly_compensation + "$");
                hourlyCompensationLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
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
                    c.gridy = 5;
                    itemPanel.add(applyBtn);
                }
            }

            cell.add(itemPanel);
        }
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
            new UserBids(uiAgent, user);
        }

        if (e.getSource() == showProjectsListBtn) {
            base.frame.dispose();
            new ProjectsList(uiAgent, user);
        }

        if (e.getSource() == showUserFeedbacksBtn) {
            base.frame.dispose();
            new Profile(uiAgent, user, user.id, Profile.BACK_HOME);
        }
    }
}
