package src.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignupProvider implements ActionListener {
    GridBase base;
    JTextField username;
    JPasswordField password;
    JPasswordField passwordConfirmation;
    JTextField name;
    JTextField website;
    JTextField logo;
    JTextField resume;
    JTextField hourly_compensation;
    JTextField keywords;
    JButton signup;
    JLabel response;

    public SignupProvider() {
        base = new GridBase(12, 3);
        username = new JTextField();
        password = new JPasswordField();
        passwordConfirmation = new JPasswordField();
        name = new JTextField();
        website = new JTextField();
        logo = new JTextField();
        resume = new JTextField();
        hourly_compensation = new JTextField();
        keywords = new JTextField();
        signup = new JButton("Signup");
        response = new JLabel();

        username.setFont(new Font("Consolas", Font.PLAIN, 20));
        password.setFont(new Font("Consolas", Font.PLAIN, 20));
        passwordConfirmation.setFont(new Font("Consolas", Font.PLAIN, 20));
        name.setFont(new Font("Consolas", Font.PLAIN, 20));
        website.setFont(new Font("Consolas", Font.PLAIN, 20));
        logo.setFont(new Font("Consolas", Font.PLAIN, 20));
        resume.setFont(new Font("Consolas", Font.PLAIN, 20));
        hourly_compensation.setFont(new Font("Consolas", Font.PLAIN, 20));
        keywords.setFont(new Font("Consolas", Font.PLAIN, 20));
        response.setFont(new Font("Consolas", Font.PLAIN, 15));
        response.setForeground(Color.RED);

        signup.addActionListener(this);
        signup.setFocusable(false);

        base.panels[0][0].add(new JLabel("Username:"), BorderLayout.EAST);
        base.panels[1][0].add(new JLabel("Password:"), BorderLayout.EAST);
        base.panels[2][0].add(new JLabel("Confirm Password:"), BorderLayout.EAST);
        base.panels[3][0].add(new JLabel("Name:"), BorderLayout.EAST);
        base.panels[4][0].add(new JLabel("Website:"), BorderLayout.EAST);
        base.panels[5][0].add(new JLabel("Logo:"), BorderLayout.EAST);
        base.panels[6][0].add(new JLabel("Resume:"), BorderLayout.EAST);
        base.panels[7][0].add(new JLabel("Hourly Compensation:"), BorderLayout.EAST);
        base.panels[8][0].add(new JLabel("keywords:"), BorderLayout.EAST);

        base.panels[0][1].add(username);
        base.panels[1][1].add(password);
        base.panels[2][1].add(passwordConfirmation);
        base.panels[3][1].add(name);
        base.panels[4][1].add(website);
        base.panels[5][1].add(logo);
        base.panels[6][1].add(resume);
        base.panels[7][1].add(hourly_compensation);
        base.panels[8][1].add(keywords);
        base.panels[9][1].add(signup);
        base.panels[10][1].add(response);

        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == signup) {
            response.setText("Passwords mismatch");
        }
    }
}
