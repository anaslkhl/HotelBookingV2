package util;

import exception.InvalidClientInfoException;

import java.util.regex.Pattern;

public class EmailValidation {

    public static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static boolean validateEmail(String email) {

        if (email != null && EMAIL_REGEX.matcher(email).matches()) {
            return true;
        }else {

            throw new InvalidClientInfoException("Email format is invalid !!");
        }
    }
}
