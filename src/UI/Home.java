package src.UI;

import src.agents.UIAgent;
import src.db.User;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Home implements ActionListener {
    UIAgent uiAgent;
    User user;
    GridBase base;
    public Home(UIAgent agent, User dbUser) {
        uiAgent = agent;
        user = dbUser;
        base = new GridBase(7, 3);

        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);
        base.frame.setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
