package src.UI;

import src.agents.UIAgent;
import src.db.Contract;
import src.db.User;
import src.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ViewContract implements ActionListener {
    Base base;
    User user;
    UIAgent uiAgent;
    public ViewContract(UIAgent uiAgent, User user, int bidId) {
        this.base = new Base();
        this.user = user;
        this.uiAgent = uiAgent;
        base.centerPanel.setLayout(new GridBagLayout());

        Contract contract = Contract.getWithBidId(uiAgent.db, bidId);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(10,10,10,10);
        JLabel description = Utils.getJLabel("<html>" + contract.description + "</html", 15);
        description.setPreferredSize(new Dimension(100, 300));
        JButton acceptBtn = new JButton("Accept");
        acceptBtn.addActionListener(e -> {
            uiAgent.callForContractUpdate(user.id, bidId, true);
            base.frame.dispose();
            new Home(uiAgent, user);
        });
        JButton rejectBtn = new JButton("Reject");
        rejectBtn.addActionListener(e -> {
            uiAgent.callForContractUpdate(user.id, bidId, false);
            base.frame.dispose();
            new Home(uiAgent, user);
        });
        acceptBtn.setFocusable(false);
        rejectBtn.setFocusable(false);

        base.centerPanel.add(description, c);
        c.gridy = 1;
        c.weightx = 0.5;
        base.centerPanel.add(rejectBtn, c);
        c.gridx = 1;
        base.centerPanel.add(acceptBtn, c);

        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == base.backButton) {
            base.frame.dispose();
            new Home(uiAgent, user);
        }
    }
}
