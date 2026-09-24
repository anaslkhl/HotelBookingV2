package util;

import exception.InvalidPaymentInfoException;
import model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentValidation {

    public static boolean validateReservationId(UUID reservationId){

        if(reservationId == null){
            throw new InvalidPaymentInfoException("Reservation id is required !");
        }
        return true;
    }

    public static boolean validateAmount(BigDecimal amount){

        if(amount == null){
            throw new InvalidPaymentInfoException("Amount must be valid !");
        }

        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidPaymentInfoException("Amount must be positive !");
        }
        return true;
    }

    public static boolean validatePaymentDate(LocalDateTime paymentDate){

        if(paymentDate == null){
            throw new InvalidPaymentInfoException("Payment date is required !");
        }

        if(paymentDate.isAfter(LocalDateTime.now())){
            throw new InvalidPaymentInfoException("Payment date cannot be in the future !");
        }
        return true;
    }

    public static boolean validateStatus(PaymentStatus status){

        if(status == null){
            throw new InvalidPaymentInfoException("Payment status is required !");
        }
        return true;
    }

    public static boolean validatePaymentMethod(String paymentMethod){

        if(paymentMethod == null || paymentMethod.isBlank()){
            throw new InvalidPaymentInfoException("Payment method is required !");
        }
        return true;
    }

    public static boolean validateTransactionReference(String transactionReference){

        if(transactionReference == null || transactionReference.isBlank()){
            throw new InvalidPaymentInfoException("Transaction reference is required !");
        }
        return true;
    }
}