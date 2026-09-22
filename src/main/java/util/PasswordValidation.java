package util;

import exception.PasswordIncorrectException;

public class PasswordValidation {


    public static boolean validatePassword(String password){

        if(password == null || password.isEmpty() || password.length() < 6){

            throw new PasswordIncorrectException("Password must be over 6 characters ");
        }
            return true;
    }
}
