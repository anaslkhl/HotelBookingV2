package service;

import model.Payment;
import model.enums.PaymentStatus;
import repository.PaymentRepository;
import repository.ReservationRepository;
import strategy.payment.CardPaymentStrategy;
import strategy.payment.CashPaymentStrategy;
import strategy.payment.PaymentStrategy;
import util.PaymentValidation;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PaymentService implements PaymentRepository {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(PaymentRepository paymentRepository, ReservationRepository reservationRepository){
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void save(Payment payment) {

        if(payment == null){
            return;
        }
        if(!PaymentValidation.validateReservationId(payment.getReservationId()) ||
            !PaymentValidation.validateAmount(payment.getAmount()) ||
            !PaymentValidation.validateStatus(payment.getStatus())){
            throw new IllegalArgumentException("Invalid Payment data ! ");
        }

        paymentRepository.save(payment);

    }

    public Payment processPayment(Payment payment, String paymentMethod){

        if(payment == null){throw new IllegalArgumentException("Payment not found !");}

        if(payment.getStatus() != PaymentStatus.PENDING){
            throw new IllegalStateException("Payment is not pending !");
        }

        if(!PaymentValidation.validatePaymentMethod(paymentMethod)){
            throw new IllegalArgumentException("Invalid payment method !");
        }

        payment.setPaymentMethod(paymentMethod);

        PaymentStrategy strategy;

        switch (paymentMethod){
            case "CASH":
                strategy = new CashPaymentStrategy();
            case "CARD":
                strategy = new CardPaymentStrategy();
            default:
                throw new IllegalArgumentException("Unsupported paument method !");

        }

        return payment;
    }

    @Override
    public boolean existByUuid(UUID uuid) {
        return false;
    }

    @Override
    public boolean existByTransactionReference(String transactionReference) {
        return false;
    }

    @Override
    public Payment getByUuid(UUID uuid) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }

    @Override
    public List<Payment> findByReservationId(UUID reservationId) {
        return List.of();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return List.of();
    }

    @Override
    public List<Payment> findByAmountGreaterThan(BigDecimal amount) {
        return List.of();
    }

    @Override
    public List<Payment> findAll() {
        return List.of();
    }
}
