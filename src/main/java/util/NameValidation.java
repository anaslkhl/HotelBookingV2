package util;

import exception.InvalidClientInfoException;

public class NameValidation {

    public static boolean validateName(String name){

        if(name == null || name.isEmpty()){
            throw new InvalidClientInfoException("Name is required !! ");
        }
            return true;
    }
}
