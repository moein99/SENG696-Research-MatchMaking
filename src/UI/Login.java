package src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Login implements ActionListener {
    GridBase base;
    JTextField username;
    JPasswordField password;
    JButton login;
    JLabel response;

    public Login() {
        base = new GridBase(7, 3);

        username = new JTextField();
        password = new JPasswordField();
        response = new JLabel();
        response.setForeground(Color.RED);
        username.setFont(new Font("Consolas", Font.PLAIN, 25));
        password.setFont(new Font("Consolas", Font.PLAIN, 25));
        login = new JButton("Login");
        login.setFocusable(false);
        login.addActionListener(this);

        base.panels[2][1].add(username);
        base.panels[2][0].add(new JLabel("Username:"), BorderLayout.EAST);
        base.panels[3][1].add(password);
        base.panels[3][0].add(new JLabel("Password:"), BorderLayout.EAST);
        base.panels[4][1].add(login);
        base.panels[5][1].add(response, BorderLayout.CENTER);
        base.frame.setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == login) {
            System.out.println(username.getText());
            System.out.println(password.getPassword());
            response.setText("Wrong Password!");
        }
    }
}
