package src.UI;

import src.agents.UIAgent;

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
    UIAgent uiAgent;

    public SignupProvider(UIAgent uiAgent) {
        this.uiAgent = uiAgent;
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

        base.centerPanels[0][0].add(new JLabel("Username:"), BorderLayout.EAST);
        base.centerPanels[1][0].add(new JLabel("Password:"), BorderLayout.EAST);
        base.centerPanels[2][0].add(new JLabel("Confirm Password:"), BorderLayout.EAST);
        base.centerPanels[3][0].add(new JLabel("Name:"), BorderLayout.EAST);
        base.centerPanels[4][0].add(new JLabel("Website:"), BorderLayout.EAST);
        base.centerPanels[5][0].add(new JLabel("Logo:"), BorderLayout.EAST);
        base.centerPanels[6][0].add(new JLabel("Resume:"), BorderLayout.EAST);
        base.centerPanels[7][0].add(new JLabel("Hourly Compensation:"), BorderLayout.EAST);
        base.centerPanels[8][0].add(new JLabel("keywords:"), BorderLayout.EAST);

        base.centerPanels[0][1].add(username);
        base.centerPanels[1][1].add(password);
        base.centerPanels[2][1].add(passwordConfirmation);
        base.centerPanels[3][1].add(name);
        base.centerPanels[4][1].add(website);
        base.centerPanels[5][1].add(logo);
        base.centerPanels[6][1].add(resume);
        base.centerPanels[7][1].add(hourly_compensation);
        base.centerPanels[8][1].add(keywords);
        base.centerPanels[9][1].add(signup);
        base.centerPanels[10][1].add(response);

        base.backButton.addActionListener(this);
        base.topPanels[0][0].add(base.backButton);

        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == base.backButton) {
            base.frame.dispose();
            new Launch(uiAgent);
        }

        if (e.getSource() == signup) {
            response.setText("Passwords mismatch");
        }
    }
}
