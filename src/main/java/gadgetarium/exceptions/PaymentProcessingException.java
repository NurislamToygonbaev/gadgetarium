package gadgetarium.exceptions;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException() {
    }

    public PaymentProcessingException(String message) {
        super(message);
    }
}
