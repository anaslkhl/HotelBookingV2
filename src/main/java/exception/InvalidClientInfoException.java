package exception;

public class InvalidClientInfoException extends RuntimeException{

    public InvalidClientInfoException(String message){
        super(message);
    }
}
