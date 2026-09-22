package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Invoice {

    private UUID id;
    private UUID reservationId;
    private String invoiceNumber;
    private BigDecimal subtotalHt;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal totalTtc;
    private LocalDateTime issuedAt;

    public Invoice(UUID reservationId, BigDecimal subtotalHt, BigDecimal vatRate, BigDecimal vatAmount, BigDecimal totalTtc) {
        this.id = UUID.randomUUID();
        this.invoiceNumber = "INV-" + UUID.randomUUID();
        this.reservationId = reservationId;
        this.subtotalHt = subtotalHt;
        this.vatRate = vatRate;
        this.vatAmount = vatAmount;
        this.totalTtc = totalTtc;
        this.issuedAt = LocalDateTime.now();
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

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getSubtotalHt() {
        return subtotalHt;
    }

    public void setSubtotalHt(BigDecimal subtotalHt) {
        this.subtotalHt = subtotalHt;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalTtc() {
        return totalTtc;
    }

    public void setTotalTtc(BigDecimal totalTtc) {
        this.totalTtc = totalTtc;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}