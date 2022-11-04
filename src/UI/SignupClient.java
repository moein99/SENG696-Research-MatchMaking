package src.UI;

import org.json.JSONObject;
import src.agents.UIAgent;
import src.utils.validators.FieldValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignupClient implements ActionListener {
    GridBase base;
    JTextField usernameInput;
    JPasswordField passwordInput;
    JPasswordField passwordConfirmationInput;
    JButton signup;
    JLabel responseLabel;
    UIAgent uiAgent;

    public SignupClient(UIAgent uiAgent) {
        this.uiAgent = uiAgent;
        base = new GridBase(7, 3);

        usernameInput = new JTextField();
        passwordInput = new JPasswordField();
        passwordConfirmationInput = new JPasswordField();
        responseLabel = new JLabel();
        responseLabel.setForeground(Color.RED);
        usernameInput.setFont(new Font("Consolas", Font.PLAIN, 25));
        passwordInput.setFont(new Font("Consolas", Font.PLAIN, 25));
        passwordConfirmationInput.setFont(new Font("Consolas", Font.PLAIN, 25));
        responseLabel.setFont(new Font("Consolas", Font.PLAIN, 15));
        signup = new JButton("Signup");
        signup.setFocusable(false);
        signup.addActionListener(this);

        base.panels[2][1].add(usernameInput);
        base.panels[2][0].add(new JLabel("Username:"), BorderLayout.EAST);
        base.panels[3][1].add(passwordInput);
        base.panels[3][0].add(new JLabel("Password:"), BorderLayout.EAST);
        base.panels[4][1].add(passwordConfirmationInput);
        base.panels[4][0].add(new JLabel("Confirm Password:"), BorderLayout.EAST);
        base.panels[5][1].add(signup);
        base.panels[6][1].add(responseLabel, BorderLayout.CENTER);
        base.frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == signup) {
            String username = usernameInput.getText();
            char[] password = passwordInput.getPassword();
            char[] passwordConfirmation = passwordConfirmationInput.getPassword();
            String response = validate_inputs(
                username,
                String.valueOf(password),
                String.valueOf(passwordConfirmation)
            );
            if (response != null) {
                responseLabel.setText(response);
            } else {
                JSONObject jsonResponse = uiAgent.call_for_signup(
                    username,
                    String.valueOf(password)
                );
                if (jsonResponse.getBoolean("status") && jsonResponse.getLong("id") != -1) {
                    responseLabel.setText("Done!");
                    // move to another page
                } else {
                    responseLabel.setText(jsonResponse.getString("message"));
                }
            }
        }
    }

    private String validate_inputs(String username, String password, String passwordConfirmation) {
        if (!FieldValidator.isUsernameValid(username)) {
            return "Username is not valid";
        } else if (!password.equals(passwordConfirmation)) {
            return "Passwords mismatch";
        } else if (!FieldValidator.isPasswordValid(password)) {
            return "Password is not valid";
        }
        return null;
    }
}
