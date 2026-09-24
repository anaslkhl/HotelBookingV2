package util;

import exception.InvalidPasswordException;

public class PasswordValidation {


    public static boolean validatePassword(String password){

        if(password == null || password.isEmpty() || password.length() < 6){

            throw new InvalidPasswordException("Password must be over 6 characters ");
        }
            return true;
    }
}
