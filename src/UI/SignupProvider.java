package src.UI;

import org.json.JSONObject;
import src.agents.UIAgent;
import src.utils.validators.FieldValidator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SignupProvider implements ActionListener {
    Base base;
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
        base = new Base();
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

        base.centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.666;

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(11, 2, 10,10));
        JPanel[][] infoPanels = new JPanel[11][2];
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 2; j ++) {
                JPanel panel = new JPanel(new BorderLayout());
                infoPanels[i][j] = panel;
                infoPanel.add(panel);
            }
        }

        infoPanels[0][0].add(new JLabel("Username:"), BorderLayout.EAST);
        infoPanels[1][0].add(new JLabel("Password:"), BorderLayout.EAST);
        infoPanels[2][0].add(new JLabel("Confirm Password:"), BorderLayout.EAST);
        infoPanels[3][0].add(new JLabel("Name:"), BorderLayout.EAST);
        infoPanels[4][0].add(new JLabel("Website:"), BorderLayout.EAST);
        infoPanels[5][0].add(new JLabel("Logo:"), BorderLayout.EAST);
        infoPanels[6][0].add(new JLabel("Resume:"), BorderLayout.EAST);
        infoPanels[7][0].add(new JLabel("Hourly Compensation:"), BorderLayout.EAST);
        infoPanels[8][0].add(new JLabel("keywords (separated by comma):"), BorderLayout.EAST);

        infoPanels[0][1].add(usernameInput);
        infoPanels[1][1].add(passwordInput);
        infoPanels[2][1].add(passwordConfirmationInput);
        infoPanels[3][1].add(nameInput);
        infoPanels[4][1].add(websiteInput);
        infoPanels[5][1].add(logoInput);
        infoPanels[6][1].add(resumeInput);
        infoPanels[7][1].add(hourlyCompensationInput);
        infoPanels[8][1].add(keywordsInput);
        infoPanels[9][1].add(signup);
        infoPanels[10][1].add(responseLabel);
        base.centerPanel.setBorder(new CompoundBorder(
                new LineBorder(Color.black,1,true),
                new EmptyBorder(10, 10, 10, 10)
                )
        );
        base.centerPanel.add(infoPanel, c);
        c.weightx = 0.333;
        c.gridx = 1;
        JPanel contract = new JPanel();
        JLabel contractLabel = new JLabel("<html>By clicking signup you will accept the system's terms of use for providers. If you do not want to accept it, you can signup as a client.</html>");
        contractLabel.setPreferredSize(new Dimension(200, 300));
        contract.add(contractLabel);
        base.centerPanel.add(contract, c);

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
