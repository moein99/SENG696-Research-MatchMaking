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

public class History implements ActionListener {
    UIAgent uiAgent;
    User user;
    GridBase base;
    String back;
    final static String BACK_HOME = "home";
    final static String BACK_BIDS = "bids";

    public History(UIAgent uiAgent, User user, int userId, String back) {
        this.uiAgent = uiAgent;
        this.user = user;
        this.back = back;
        base = new GridBase(1, 1);

        JSONArray feedbacks = uiAgent.retrieveUserFeedbacks(userId);
        JScrollPane sp = new JScrollPane();
        sp.setViewportView(getViewPort(feedbacks));

        base.centerPanels[0][0].add(sp);
        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
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
                new Home(uiAgent, user);
            } else if (back.equals(BACK_BIDS)) {
                new ProviderBids(uiAgent, user);
            }
        }
    }
}
