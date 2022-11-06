package src.UI;

import com.mysql.cj.log.Log;
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

        base.centerPanels[2][1].add(usernameInput);
        base.centerPanels[2][0].add(new JLabel("Username:"), BorderLayout.EAST);
        base.centerPanels[3][1].add(passwordInput);
        base.centerPanels[3][0].add(new JLabel("Password:"), BorderLayout.EAST);
        base.centerPanels[4][1].add(passwordConfirmationInput);
        base.centerPanels[4][0].add(new JLabel("Confirm Password:"), BorderLayout.EAST);
        base.centerPanels[5][1].add(signup);
        base.centerPanels[6][1].add(responseLabel, BorderLayout.CENTER);

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
            String response = validate_inputs(
                username,
                String.valueOf(password),
                String.valueOf(passwordConfirmation)
            );
            if (response != null) {
                responseLabel.setText(response);
            } else {
                JSONObject jsonResponse = uiAgent.call_for_client_signup(
                    username,
                    String.valueOf(password)
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
