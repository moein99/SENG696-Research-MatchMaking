package src.UI;

import src.agents.UIAgent;
import src.db.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

public class Login implements ActionListener {
    GridBase base;
    JTextField usernameField;
    JPasswordField passwordField;
    JButton login;
    JLabel responseLabel;
    UIAgent uiAgent;


    public Login(UIAgent uiAgent) {
        this.uiAgent = uiAgent;
        base = new GridBase(7, 3);

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        responseLabel = new JLabel();
        responseLabel.setForeground(Color.RED);
        usernameField.setFont(new Font("Consolas", Font.PLAIN, 25));
        passwordField.setFont(new Font("Consolas", Font.PLAIN, 25));
        responseLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        login = new JButton("Login");
        login.setFocusable(false);
        login.addActionListener(this);

        base.centerPanels[2][1].add(usernameField);
        base.centerPanels[2][0].add(new JLabel("Username:"), BorderLayout.EAST);
        base.centerPanels[3][1].add(passwordField);
        base.centerPanels[3][0].add(new JLabel("Password:"), BorderLayout.EAST);
        base.centerPanels[4][1].add(login);
        base.centerPanels[5][1].add(responseLabel, BorderLayout.CENTER);
        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);

        base.frame.setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == login) {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();

            User user = uiAgent.call_for_login(username, String.valueOf(password));
            if (user != null) {
                base.frame.dispose();
                new Home(uiAgent, user);
            } else {
                responseLabel.setText("Credentials are not valid");
            }
        } else if (e.getSource() == base.backButton) {
            base.frame.dispose();
            new Launch(uiAgent);
        }
    }
}
