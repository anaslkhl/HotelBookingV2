package repository;

import model.Payment;
import model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository {

    void save(Payment payment);

    boolean existByUuid(UUID uuid);

    boolean existByTransactionReference(String transactionReference);

    Payment getByUuid(UUID uuid);

    void delete(UUID id);

    List<Payment> findByReservationId(UUID reservationId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByAmountGreaterThan(BigDecimal amount);

    List<Payment> findAll();
}