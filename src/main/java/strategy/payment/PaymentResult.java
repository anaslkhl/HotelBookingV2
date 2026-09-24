package strategy.payment;

public class PaymentResult {


    private final boolean success;
    private final String transactionReference;
    private final String message;

    public PaymentResult(
            boolean success,
            String transactionReference,
            String message
    ) {
        this.success = success;
        this.transactionReference = transactionReference;
        this.message = message;
    }


    public boolean isSuccess() {
        return success;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getMessage() {
        return message;
    }
}
