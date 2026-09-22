package exception;

public class RoomCapacityException extends RuntimeException{

    public RoomCapacityException(String message){
        super(message);
    }
}
