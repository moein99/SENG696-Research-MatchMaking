package src.UI;

import org.json.JSONArray;
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
import java.io.File;
import java.io.IOException;

public class Profile implements ActionListener {
    UIAgent uiAgent;
    User visitorUser;
    User targetUser;
    Base base;
    String back;
    int targetUserId;
    final static int PREMIUM_PRICE = 10;
    final static String BACK_HOME = "home";
    final static String BACK_BIDS = "bids";

    public Profile(UIAgent uiAgent, User visitorUser, int targetUserId, String back) {
        this.uiAgent = uiAgent;
        this.visitorUser = visitorUser;
        this.visitorUser.refreshFromDB(uiAgent.db);
        this.targetUserId = targetUserId;
        this.targetUser = User.get_with_id(uiAgent.db, targetUserId);
        this.back = back;
        base = new Base();
        base.centerPanel.setLayout(new GridBagLayout());

        JSONArray feedbacks = uiAgent.retrieveUserFeedbacks(targetUserId);
        GridBagConstraints c = getInitialConstraints();
        setFeedbackPanel(feedbacks, c);
        c.gridx += 1;
        setDetailsPanel(feedbacks, c);

        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    private void setFeedbackPanel(JSONArray feedbacks, GridBagConstraints c) {
        JScrollPane sp = new JScrollPane();
        sp.setViewportView(getViewPort(feedbacks));
        JPanel feedbacksPanel = new JPanel();
        feedbacksPanel.setLayout(new GridLayout(1, 1));
        feedbacksPanel.add(sp);
        feedbacksPanel.setPreferredSize(new Dimension(500, 500));
        base.centerPanel.add(feedbacksPanel, c);
    }

    private GridBagConstraints getInitialConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.insets = new Insets(10,10,10,10);
        return c;
    }

    private void setDetailsPanel(JSONArray feedbacks, GridBagConstraints detailsConstraints) {
        JPanel details = new JPanel();
        details.setBackground(Color.GRAY);
        details.setPreferredSize(new Dimension(300, 500));
        details.setLayout(new GridBagLayout());

        // Info for everyone
        GridBagConstraints cbc = new GridBagConstraints();
        cbc.anchor = GridBagConstraints.NORTHWEST;
        cbc.fill = GridBagConstraints.HORIZONTAL;
        cbc.insets = new Insets(10,10,10,10);
        cbc.gridx = 0;
        cbc.gridy = 0;
        cbc.weighty = 1;
        cbc.weightx = 1;

        int numberOfProjects = Project.getUserProjects(uiAgent.db, targetUser.id, Project.FINISHED).size();
        String averageRate = "No Feedback Yet";
        if (feedbacks.length() != 0) {
            int sum = 0;
            for (int i = 0; i < feedbacks.length(); i++) {
                JSONObject feedback = feedbacks.getJSONObject(i);
                sum += feedback.getInt("rate");
            }
            float average = (float) sum / feedbacks.length();
            averageRate = String.valueOf(average);
        }
        JLabel numOfProjectsLabel = Utils.getJLabel("Projects Finished: " + numberOfProjects, 12);
        JLabel averageRateLabel = Utils.getJLabel("Average Rate: " + averageRate, 12);

        details.add(numOfProjectsLabel, cbc);
        cbc.gridy += 1;
        details.add(averageRateLabel, cbc);
        if (targetUser.user_type.equals(User.PROVIDER_TYPE)) {
            JLabel websiteLabel = Utils.getJLabel("Website: " + targetUser.website, 12);
            cbc.gridy += 1;
            details.add(websiteLabel, cbc);

            JLabel logoLabel = Utils.getJLabel("Logo: ", 12);
            ImageIcon logoIcon = new ImageIcon(targetUser.logo_address);
            Image image = logoIcon.getImage();
            Image newimg = image.getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH);
            logoIcon = new ImageIcon(newimg);
            logoLabel.setIcon(logoIcon);
            logoLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            cbc.gridy += 1;
            details.add(logoLabel, cbc);

            String verified = "No";
            if (targetUser.is_verified) {
                verified = "Yes";
            }
            JLabel isVerified = Utils.getJLabel("Verified User: " + verified, 12);
            cbc.gridy += 1;
            details.add(isVerified, cbc);

            if (visitorUser != null) {
                JLabel salary = Utils.getJLabel("Hourly Compensation: " + targetUser.hourly_compensation, 12);
                cbc.gridy += 1;
                details.add(salary, cbc);
            }

            JButton resumeBtn = new JButton("Resume");
            resumeBtn.setFocusable(false);
            resumeBtn.addActionListener(e -> {
                if (Desktop.isDesktopSupported()) {
                    try {
                        File myFile = new File(targetUser.resume_address);
                        Desktop.getDesktop().open(myFile);
                    } catch (IOException ex) {
                        System.out.println("no application registered for PDFs");
                    }
                }
            });
            cbc.gridy += 1;
            details.add(resumeBtn, cbc);
        }


        // Personal Info
        if (visitorUser != null && visitorUser.id == targetUser.id) {
            JLabel balanceLabel = Utils.getJLabel("Balance: " + visitorUser.balance, 12);
            JLabel responseLabel = Utils.getJLabel("", 12);
            responseLabel.setForeground(Color.RED);
            JButton subscribeBtn = new JButton("Upgrade to Premium");
            subscribeBtn.setFocusable(false);
            subscribeBtn.addActionListener(e -> {
                if (visitorUser.balance < PREMIUM_PRICE) {
                    responseLabel.setText("Your Balance is less than 10$");
                } else {
                    uiAgent.callForSubscription(visitorUser.id, PREMIUM_PRICE);
                    base.frame.dispose();
                    new Profile(uiAgent, visitorUser, targetUserId, BACK_HOME);
                }
            });

            cbc.gridx = 0;
            cbc.gridy += 1;
            details.add(balanceLabel, cbc);

            if (visitorUser.user_type.equals(User.PROVIDER_TYPE)) {
                if (visitorUser.subscription_ends == null || !visitorUser.isSubscriptionActive()) {
                    cbc.gridy += 1;
                    details.add(subscribeBtn, cbc);
                    cbc.gridy += 1;
                    details.add(responseLabel, cbc);
                } else {
                    cbc.gridy += 1;
                    JLabel subscriptionEndsLabel = Utils.getJLabel("Subscription End Date: " + Utils.convertDateToString(visitorUser.subscription_ends), 12);
                    details.add(subscriptionEndsLabel, cbc);
                }
            }
        }
        base.centerPanel.add(details, detailsConstraints);
    }

    private JPanel getViewPort(JSONArray feedbacks) {
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(0, 1, 20,20));

        for (int i = 0; i < feedbacks.length(); i++) {
            JSONObject feedback = feedbacks.getJSONObject(i);
            JPanel itemPanel = new JPanel();
            itemPanel.setBorder(new EmptyBorder(10, 10,10,10));
            itemPanel.setLayout(new GridLayout(2, 1));
            itemPanel.setBackground(Color.GRAY);

            JLabel comment = new JLabel("Comment: " + feedback.getString("comment"));
            JLabel rate = new JLabel("Rate Given: " + feedback.getInt("rate"));
            comment.setFont(new Font("Consolas", Font.PLAIN, 15));
            rate.setFont(new Font("Consolas", Font.PLAIN, 15));

            itemPanel.add(comment);
            itemPanel.add(rate);

            cell.add(itemPanel);
        }

        return cell;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == base.backButton) {
            base.frame.dispose();
            if (back.equals(BACK_HOME)) {
                new Home(uiAgent, visitorUser);
            } else if (back.equals(BACK_BIDS)) {
                new ProviderBids(uiAgent, visitorUser);
            }
        }
    }
}
