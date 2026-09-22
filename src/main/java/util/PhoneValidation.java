package util;

import exception.InvalidClientInfoException;

public class PhoneValidation {


    public static boolean validatePhone(String phone){

        if(phone == null || phone.isEmpty()){
            throw new InvalidClientInfoException("Phone number is required !");
        }
            return true;
    }
}
