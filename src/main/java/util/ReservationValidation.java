package util;

import exception.ClientNotFoundException;
import exception.InvalidReservationInfoException;
import exception.RoomNotFoundException;

import java.time.LocalDate;
import java.util.UUID;

public class ReservationValidation {



    public static boolean validateUser(UUID client){

        if(client == null){
            throw new ClientNotFoundException("Client not found exception !");
        }
        return true;
    }

    public static boolean validateRoom(String room){

        if(room == null){
            throw new RoomNotFoundException("Room not found !");
        }
        return true;
    }

    public static boolean validateCheckIn(LocalDate checkin){

        if(checkin == null){

            throw new InvalidReservationInfoException("Invalid reservation checking date !");
        }
        return true;
    }

    public static boolean validateCheckOut(LocalDate checkout){

        if(checkout == null){

            throw new InvalidReservationInfoException("Invalid reservation checkout date !");
        }
        return true;
    }

    public static boolean numberOfGuests(int number){

        if(number <= 0){

            throw new InvalidReservationInfoException("Number of guests must be positive !");
        }
        return true;
    }


}
