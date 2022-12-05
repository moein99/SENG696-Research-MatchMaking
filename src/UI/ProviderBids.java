package src.UI;

import org.json.JSONObject;
import src.agents.UIAgent;
import src.db.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

public class ProviderBids implements ActionListener {
    UIAgent uiAgent;
    User user;
    GridBase base;
    public ProviderBids(UIAgent uiAgent, User user) {
        this.uiAgent = uiAgent;
        this.user = user;
        this.base = new GridBase(1, 1);

        JSONObject openBids = this.uiAgent.getProviderOpenBids(this.user.id);
        JScrollPane sp = new JScrollPane();
        sp.setViewportView(getViewPort(openBids));

        base.centerPanels[0][0].add(sp);
        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    private JPanel getViewPort(JSONObject openBids) {
        JPanel cell = new JPanel();
        cell.setLayout(new GridLayout(0, 1, 20,20));

        for (Iterator<String> it = openBids.keys(); it.hasNext(); ) {
            String key = it.next();

            JPanel itemPanel = new JPanel();
            itemPanel.setBorder(new EmptyBorder(10, 10,10,10));
            itemPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            JLabel titleLabel = new JLabel(openBids.getJSONObject(key).getString("project_title"));
            titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            titleLabel.setBorder(new EmptyBorder(10, 30, 10, 30));
            itemPanel.add(titleLabel, c);

            JLabel descriptionLabel = new JLabel(openBids.getJSONObject(key).getString("bid_description"));
            titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 1;
            descriptionLabel.setBorder(new EmptyBorder(10, 30, 10, 30));
            itemPanel.add(descriptionLabel, c);

            JLabel amountLabel = new JLabel("Proposed Rate: " + openBids.getJSONObject(key).getString("bid_amount"));
            titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 0;
            amountLabel.setBorder(new EmptyBorder(10, 30, 10, 30));
            itemPanel.add(amountLabel, c);

            JLabel bidderUsernameLabel = new JLabel("Username: " + openBids.getJSONObject(key).getString("bidder_username"));
            bidderUsernameLabel.setForeground(new Color(0, 150, 255));
            bidderUsernameLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    base.frame.dispose();
                    new Profile(uiAgent, user, Integer.parseInt(openBids.getJSONObject(key).getString("bidder_id")), Profile.BACK_BIDS);
                }
            });
            titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 1;
            bidderUsernameLabel.setBorder(new EmptyBorder(10, 30, 10, 30));
            itemPanel.add(bidderUsernameLabel, c);

            JButton acceptButton = new JButton("Accpet");
            titleLabel.setFont(new Font("Consolas", Font.PLAIN, 20));
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 2;
            c.gridy = 1;
            acceptButton.addActionListener(e -> {
                uiAgent.callForBidAccept(Integer.parseInt(key));
                base.frame.dispose();
                new ProviderBids(uiAgent, user);
            });

            acceptButton.setFocusable(false);
            acceptButton.setBorder(new EmptyBorder(10, 30, 10, 30));
            itemPanel.add(acceptButton, c);

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
