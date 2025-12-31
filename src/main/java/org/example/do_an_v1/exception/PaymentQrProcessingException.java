package org.example.do_an_v1.exception;

public class PaymentQrProcessingException extends RuntimeException {

    public PaymentQrProcessingException(String message) {
        super(message);
    }

    public PaymentQrProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
