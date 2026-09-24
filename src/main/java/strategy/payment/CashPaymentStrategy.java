package strategy.payment;

import model.Payment;

import java.util.UUID;

public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(Payment payment) {

        String transactionReference = "CASH-" + UUID.randomUUID();

        return new PaymentResult(
                true,
                transactionReference,
                "Cash payment successful"
        );
    }
}