package src.UI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Launch implements ActionListener {
    GridBase base;
    JButton loginButton;
    JButton signupClientButton;
    JButton signupProviderButton;
    public Launch() {
        base = new GridBase(6, 3);

        loginButton = new JButton("Login");
        signupClientButton = new JButton("Signup as a Client");
        signupProviderButton = new JButton("Signup as a Provider");
        loginButton.setFocusable(false);
        signupClientButton.setFocusable(false);
        signupProviderButton.setFocusable(false);
        loginButton.addActionListener(this);
        signupClientButton.addActionListener(this);
        signupProviderButton.addActionListener(this);

        base.panels[2][1].add(loginButton);
        base.panels[3][1].add(signupClientButton);
        base.panels[4][1].add(signupProviderButton);
        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            base.frame.dispose();
            new Login();
        } else if (e.getSource() == signupClientButton) {
            base.frame.dispose();
            new SignupClient();
        } else if (e.getSource() == signupProviderButton) {
            base.frame.dispose();
            new SignupProvider();
        }
    }
}
