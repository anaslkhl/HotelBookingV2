package exception;

public class InvalidPaymentInfoException extends RuntimeException {

    public InvalidPaymentInfoException(String message){
        super(message);
    }

}