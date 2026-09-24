package strategy.payment;

import model.Payment;

import java.util.UUID;

public class CardPaymentStrategy implements PaymentStrategy {


    @Override
    public PaymentResult pay(Payment payment) {

        String transactionReference = "CARD-" + UUID.randomUUID();

        return new PaymentResult(
                true,
                transactionReference,
                "Card payment successful"
        );
    }
}