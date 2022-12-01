package src.UI;

import org.json.JSONObject;
import src.agents.UIAgent;
import src.utils.validators.FieldValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SignupProvider implements ActionListener {
    GridBase base;
    JTextField usernameInput;
    JPasswordField passwordInput;
    JPasswordField passwordConfirmationInput;
    JTextField nameInput;
    JTextField websiteInput;
    JTextField logoInput;
    JTextField resumeInput;
    JTextField hourlyCompensationInput;
    JTextField keywordsInput;
    JButton signup;
    JLabel responseLabel;
    UIAgent uiAgent;

    public SignupProvider(UIAgent uiAgent) {
        this.uiAgent = uiAgent;
        base = new GridBase(12, 3);
        usernameInput = new JTextField();
        passwordInput = new JPasswordField();
        passwordConfirmationInput = new JPasswordField();
        nameInput = new JTextField();
        websiteInput = new JTextField();
        logoInput = new JTextField();
        resumeInput = new JTextField();
        hourlyCompensationInput = new JTextField();
        keywordsInput = new JTextField();
        signup = new JButton("Signup");
        responseLabel = new JLabel();

        usernameInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        passwordInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        passwordConfirmationInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        nameInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        websiteInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        logoInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        resumeInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        hourlyCompensationInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        keywordsInput.setFont(new Font("Consolas", Font.PLAIN, 20));
        responseLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        responseLabel.setForeground(Color.RED);

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
        base.centerPanels[8][0].add(new JLabel("keywords (separated by comma):"), BorderLayout.EAST);

        base.centerPanels[0][1].add(usernameInput);
        base.centerPanels[1][1].add(passwordInput);
        base.centerPanels[2][1].add(passwordConfirmationInput);
        base.centerPanels[3][1].add(nameInput);
        base.centerPanels[4][1].add(websiteInput);
        base.centerPanels[5][1].add(logoInput);
        base.centerPanels[6][1].add(resumeInput);
        base.centerPanels[7][1].add(hourlyCompensationInput);
        base.centerPanels[8][1].add(keywordsInput);
        base.centerPanels[9][1].add(signup);
        base.centerPanels[10][1].add(responseLabel);

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
            String username = usernameInput.getText();
            char[] password = passwordInput.getPassword();
            char[] passwordConfirmation = passwordConfirmationInput.getPassword();
            String name = nameInput.getText();
            String website = nameInput.getText();
            String logoAddress = logoInput.getText();
            String resumeAddress = resumeInput.getText();
            String hourlyCompensation = hourlyCompensationInput.getText();
            String keywords = keywordsInput.getText();
            String response = validate_inputs(
                    username,
                    String.valueOf(password),
                    String.valueOf(passwordConfirmation),
                    logoAddress,
                    resumeAddress
            );

            if (response != null) {
                responseLabel.setText(response);
            } else {
                JSONObject jsonResponse = uiAgent.callForProviderSignup(
                        username,
                        String.valueOf(password),
                        name,
                        website,
                        logoAddress,
                        resumeAddress,
                        hourlyCompensation,
                        keywords
                );
                if (jsonResponse.getBoolean("status") && jsonResponse.getLong("id") != -1) {
                    responseLabel.setText("Please login now");
                    base.frame.dispose();
                    new Login(uiAgent);
                } else {
                    responseLabel.setText(jsonResponse.getString("message"));
                }
            }
        }
    }

    private String validate_inputs(String username, String password, String passwordConfirmation, String logoAddress, String resumeAddress) {
        if (!FieldValidator.isUsernameValid(username)) {
            return "Username is not valid";
        } else if (!password.equals(passwordConfirmation)) {
            return "Passwords mismatch";
        } else if (!FieldValidator.isPasswordValid(password)) {
            return "Password is not valid";
        }

        File logo = new File(logoAddress);
        File resume = new File(resumeAddress);
        if (!logo.exists() || logo.isDirectory()) {
            return "Logo address is not valid";
        }
        if (!resume.exists() || resume.isDirectory()) {
            return "Resume address is not valid";
        }

        return null;
    }
}
