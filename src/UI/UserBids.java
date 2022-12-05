package src.UI;

import org.json.JSONObject;
import src.agents.UIAgent;
import src.db.Contract;
import src.db.User;
import src.utils.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

public class UserBids implements ActionListener {
    UIAgent uiAgent;
    User user;
    GridBase base;
    public UserBids(UIAgent uiAgent, User user) {
        this.uiAgent = uiAgent;
        this.user = user;
        this.base = new GridBase(1, 1);

        JSONObject openBids = this.uiAgent.getProviderActiveBids(this.user.id);
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
            String bidId = it.next();
            JSONObject bidValues = openBids.getJSONObject(bidId);

            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.7;
            c.weighty = 1;
            c.insets = new Insets(10,10,10,10);
            c.anchor = GridBagConstraints.WEST;
            c.gridx = 0;
            c.gridy = 0;

            JLabel titleLabel = Utils.getJLabel(bidValues.getString("project_title"), 20);
            itemPanel.add(titleLabel, c);

            JLabel descriptionLabel = Utils.getJLabel(openBids.getJSONObject(bidId).getString("bid_description"), 15);
            c.gridy = 1;
            itemPanel.add(descriptionLabel, c);

            c.weightx = 0.15;
            JLabel amountLabel = Utils.getJLabel("Proposed Rate: " + bidValues.getString("bid_amount"), 15);
            c.gridx = 1;
            c.gridy = 0;
            itemPanel.add(amountLabel, c);

            JLabel bidderUsernameLabel = Utils.getJLabel("Username: " + bidValues.getString("bidder_username"), 15);
            bidderUsernameLabel.setForeground(new Color(0, 150, 255));
            bidderUsernameLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    base.frame.dispose();
                    new Profile(uiAgent, user, Integer.parseInt(bidValues.getString("bidder_id")), Profile.BACK_BIDS);
                }
            });
            c.gridx = 1;
            c.gridy = 1;
            itemPanel.add(bidderUsernameLabel, c);

            String contractStatus = bidValues.getString("contract_status");

            JButton viewContractBtn = new JButton("View Contract");
            viewContractBtn.addActionListener(e -> {
                base.frame.dispose();
                new ViewContract(uiAgent, user, Integer.parseInt(bidId));
            });
            viewContractBtn.setFocusable(false);
            c.gridx = 2;
            c.gridy = 1;

            if (contractStatus.equals(Contract.NOT_CREATED)) {
                if (user.user_type.equals(User.PROVIDER_TYPE)) {
                    JButton acceptButton = new JButton("Accept");
                    c.anchor = GridBagConstraints.EAST;
                    acceptButton.setMaximumSize(new Dimension(20, 25));
                    acceptButton.setPreferredSize(new Dimension(20, 25));
                    acceptButton.addActionListener(e -> {
                        uiAgent.callForBidAccept(Integer.parseInt(bidId));
                        base.frame.dispose();
                        new UserBids(uiAgent, user);
                    });
                    acceptButton.setFocusable(false);
                    itemPanel.add(acceptButton, c);
                } else {
                    JLabel state = Utils.getJLabel("Waiting for Provider", 15);
                    itemPanel.add(state, c);
                }
            } else if (contractStatus.equals(Contract.WAITING_FOR_BOTH)) {
                itemPanel.add(viewContractBtn, c);
            } else if (contractStatus.equals(Contract.WAITING_FOR_CLIENT)) {
                if (user.id == bidValues.getInt("bidder_id")) {
                    itemPanel.add(viewContractBtn, c);
                } else {
                    JLabel status = Utils.getJLabel("Waiting for Client", 15);
                    itemPanel.add(status, c);
                }
            } else if (contractStatus.equals(Contract.WAITING_FOR_PROVIDER)) {
                if (user.id == bidValues.getInt("bidder_id")) {
                    JLabel status = Utils.getJLabel("Waiting for Provider", 15);
                    itemPanel.add(status, c);
                } else {
                    itemPanel.add(viewContractBtn, c);
                }
            }

            itemPanel.setBackground(Color.WHITE);
            itemPanel.setBorder(new LineBorder(Color.BLACK, 1, true));
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
