package src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignupClient implements ActionListener {
    GridBase base;
    JTextField username;
    JPasswordField password;
    JPasswordField passwordConfirmation;
    JButton signup;
    JLabel response;

    public SignupClient() {
        base = new GridBase(7, 3);

        username = new JTextField();
        password = new JPasswordField();
        passwordConfirmation = new JPasswordField();
        response = new JLabel();
        response.setForeground(Color.RED);
        username.setFont(new Font("Consolas", Font.PLAIN, 25));
        password.setFont(new Font("Consolas", Font.PLAIN, 25));
        passwordConfirmation.setFont(new Font("Consolas", Font.PLAIN, 25));
        response.setFont(new Font("Consolas", Font.PLAIN, 25));
        signup = new JButton("Signup");
        signup.setFocusable(false);
        signup.addActionListener(this);

        base.panels[2][1].add(username);
        base.panels[2][0].add(new JLabel("Username:"), BorderLayout.EAST);
        base.panels[3][1].add(password);
        base.panels[3][0].add(new JLabel("Password:"), BorderLayout.EAST);
        base.panels[4][1].add(passwordConfirmation);
        base.panels[4][0].add(new JLabel("Confirm Password:"), BorderLayout.EAST);
        base.panels[5][1].add(signup);
        base.panels[6][1].add(response, BorderLayout.CENTER);
        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == signup) {
            response.setText("Passwords mismatch!");
        }
    }
}
