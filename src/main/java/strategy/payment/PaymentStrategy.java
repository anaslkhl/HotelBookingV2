package strategy.payment;

import model.Payment;

public interface PaymentStrategy {

    PaymentResult pay(Payment payment);
}
