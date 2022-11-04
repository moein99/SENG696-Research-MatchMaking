package src.utils.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldValidator {

    // simple regex
    //private static final String USERNAME_PATTERN = "^[a-z0-9\\._-]{5,20}$";

    // strict regex
    private static final String USERNAME_PATTERN =
            "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])"
            + "(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*[!@#$%^&+=])"
            + "(?=\\S+$).{8,20}$";

    private static final Pattern usernamePattern = Pattern.compile(USERNAME_PATTERN);
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isUsernameValid(final String username) {
        Matcher matcher = usernamePattern.matcher(username);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {
        Matcher matcher = passwordPattern.matcher(password);
        return matcher.matches();
    }

}
