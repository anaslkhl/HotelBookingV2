package model;

import model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {

    private UUID id;
    private UUID reservationId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionReference;

    public Payment(UUID reservationId, BigDecimal amount, PaymentStatus status, String paymentMethod, String transactionReference) {
        this.id = UUID.randomUUID();
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentDate = LocalDateTime.now();
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}