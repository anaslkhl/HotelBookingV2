package exception;

public class AuthenticationException extends RuntimeException{

    public AuthenticationException(String mesaage){
        super(mesaage);
    }
}
