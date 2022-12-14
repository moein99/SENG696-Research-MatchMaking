package src.UI;

import src.agents.UIAgent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Launch implements ActionListener {
    GridBase base;
    JButton loginButton;
    JButton signupClientButton;
    JButton signupProviderButton;
    JButton viewAsGuestButton;
    UIAgent uiAgent;
    public Launch(UIAgent uiAgent) {
        this.uiAgent = uiAgent;
        base = new GridBase(6, 3);

        loginButton = new JButton("Login");
        signupClientButton = new JButton("Signup as a Client");
        signupProviderButton = new JButton("Signup as a Provider");
        viewAsGuestButton = new JButton("View as a Guest");
        loginButton.setFocusable(false);
        signupClientButton.setFocusable(false);
        signupProviderButton.setFocusable(false);
        viewAsGuestButton.setFocusable(false);
        loginButton.addActionListener(this);
        signupClientButton.addActionListener(this);
        signupProviderButton.addActionListener(this);
        viewAsGuestButton.addActionListener(this);

        base.centerPanels[1][1].add(loginButton);
        base.centerPanels[2][1].add(signupClientButton);
        base.centerPanels[3][1].add(signupProviderButton);
        base.centerPanels[4][1].add(viewAsGuestButton);
        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            base.frame.dispose();
            new Login(uiAgent);
        } else if (e.getSource() == signupClientButton) {
            base.frame.dispose();
            new SignupClient(uiAgent);
        } else if (e.getSource() == signupProviderButton) {
            base.frame.dispose();
            new SignupProvider(uiAgent);
        } else if (e.getSource() == viewAsGuestButton) {
            base.frame.dispose();
            new Home(uiAgent, null);
        }
    }
}
